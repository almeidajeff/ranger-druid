/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ranger.druid;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.ranger.plugin.audit.RangerDefaultAuditHandler;
import org.apache.ranger.plugin.service.RangerBasePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.druid.server.security.AuthConfig;
import io.druid.server.security.AuthorizationInfo;

/**
 * The servlet filter which provides {@link AuthorizationInfo} in the
 * {@link HttpServletRequest} under the AuthConfig.DRUID_AUTH_TOKEN key. It is
 * possible to switch on the unsecure mode, where - instead of relaying on the
 * Principal which set on the request, the user could provide user name, and
 * group names, to which the current request is belong.
 *
 */
public class RangerFilter implements Filter {

    static final String HEADER_GROUPNAMES = "X-Ranger-Groups";
    static final String HEADER_USERNAME = "X-Ranger-Username";
    private final static Logger LOG = LoggerFactory.getLogger(RangerFilter.class);
    private final static Set<String> whiteListPaths = Collections.singleton("/druid/v2/datasources");

    private PluginConfig config;
    private RangerBasePlugin plugin;

    public RangerFilter(PluginConfig config, RangerBasePlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Initializing RangerFilter");
        if (config.isUnsecureMode()) {
            LOG.warn(
                    "The plugin started in unsecure mode: anyone can impersonate anyone else, use only in dev/test environments!");
        }
        plugin.init(); // this will initialize policy engine and policy
                       // refresher
        plugin.setResultProcessor(new RangerDefaultAuditHandler());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        LOG.info("filter request: " + httpRequest.getRequestURI());
        if (whiteListPaths.contains(httpRequest.getRequestURI()) && "GET".equals(httpRequest.getMethod())) {
            LOG.info("Request white-listed, read-only request granted");
            httpRequest.setAttribute(AuthConfig.DRUID_AUTH_TOKEN, new ReadonlyAuthorizationInfo());
        } else {
            addRangerAuthorization(httpRequest);
        }
        chain.doFilter(httpRequest, response);
    }

    private void addRangerAuthorization(HttpServletRequest httpRequest) {
        Principal userPrincipal = httpRequest.getUserPrincipal();
        if (userPrincipal != null) {
            setAuthorizationInfo(httpRequest, userPrincipal.getName(), Collections.<String>emptySet());
        } else {
            if (config.isUnsecureMode()) {
                final String username = getFromRequest(httpRequest, HEADER_USERNAME, "user.name");
                final Set<String> groups = parseNames(getFromRequest(httpRequest, HEADER_GROUPNAMES, "group.names"));
                setAuthorizationInfo(httpRequest, username, groups);
                return;
            }
            LOG.warn("Unable to get user principal {}", httpRequest.getRemoteAddr());
        }
    }

    private Set<String> parseNames(String groupnames) {
        if (groupnames != null) {
            return new HashSet<>(Arrays.asList(groupnames.split(",")));
        }
        return Collections.<String>emptySet();
    }

    private void setAuthorizationInfo(HttpServletRequest httpRequest, String userName, Set<String> groupNames) {
        LOG.info("Request from username={} groups={} ip={}",
                new Object[] { userName, groupNames, httpRequest.getRemoteAddr() });
        httpRequest.setAttribute(AuthConfig.DRUID_AUTH_TOKEN,
                new RangerAuthorizationInfo(plugin, userName, groupNames, httpRequest.getRemoteAddr()));
    }

    private String getFromRequest(HttpServletRequest request, String header, String parameterName) {
        final String value = request.getHeader(header);
        if (value != null) {
            return value;
        }
        return request.getParameter(parameterName);
    }

    @Override
    public void destroy() {
        LOG.info("Destroying RangerFilter");
    }

}