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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ranger.plugin.service.RangerBasePlugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.druid.server.security.AuthConfig;

public class RangerFilterTest {

    PluginConfig config;
    RangerBasePlugin plugin;
    RangerFilter filter;

    @Before
    public void setup() {
        config = new PluginConfig(true);
        plugin = mock(RangerBasePlugin.class);
        filter = new RangerFilter(config, plugin);
    }

    @Test
    public void testWhiteList() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        setupRequest(request, "GET", "/druid/v2/datasources");
        filter.doFilter(request, response, chain);
        verify(request).setAttribute(eq(AuthConfig.DRUID_AUTH_TOKEN), any(ReadonlyAuthorizationInfo.class));
    }

    @Test
    public void testUnsecureRequestHandling() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        setupRequest(request, "POST", "/druid/v2/datasources/testDatasource");
        setHeader(request, RangerFilter.HEADER_USERNAME, "testUser");
        setHeader(request, RangerFilter.HEADER_GROUPNAMES, "testing,users");
        filter.doFilter(request, response, chain);

        ArgumentCaptor<RangerAuthorizationInfo> captor = ArgumentCaptor.forClass(RangerAuthorizationInfo.class);
        verify(request).setAttribute(eq(AuthConfig.DRUID_AUTH_TOKEN), captor.capture());
        RangerAuthorizationInfo authorizationInfo = captor.getValue();
        assertEquals("testUser", authorizationInfo.getUserName());
        assertEquals(new HashSet<>(Arrays.asList("testing", "users")), authorizationInfo.getUserGroups());
    }

    @Test
    public void testSecurePrincipalRequestHandling() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        setupRequest(request, "POST", "/druid/v2/datasources/testDatasource");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("userNameFromPrincipal");
        when(request.getUserPrincipal()).thenReturn(principal);
        filter.doFilter(request, response, chain);

        ArgumentCaptor<RangerAuthorizationInfo> captor = ArgumentCaptor.forClass(RangerAuthorizationInfo.class);
        verify(request).setAttribute(eq(AuthConfig.DRUID_AUTH_TOKEN), captor.capture());
        RangerAuthorizationInfo authorizationInfo = captor.getValue();
        assertEquals("userNameFromPrincipal", authorizationInfo.getUserName());
        assertEquals(Collections.emptySet(), authorizationInfo.getUserGroups());
    }

    private static void setHeader(HttpServletRequest request, String headerName, String headerValue) {
        when(request.getHeader(headerName)).thenReturn(headerValue);
    }

    private static void setupRequest(HttpServletRequest request, String method, String url) {
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(url);
    }

}