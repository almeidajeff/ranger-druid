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

import java.util.Set;

import org.apache.ranger.plugin.policyengine.RangerAccessRequestImpl;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.apache.ranger.plugin.service.RangerBasePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.druid.server.security.Access;
import io.druid.server.security.Action;
import io.druid.server.security.AuthorizationInfo;
import io.druid.server.security.Resource;

/**
 * Ranger specific implementation of the {@link AuthorizationInfo} interface. 
 * This object will be stored on the http servlet request.
 * Druid will call the isAuthorized method for the actual decisions.
 *
 */
public class RangerAuthorizationInfo implements AuthorizationInfo {

    private final static Logger LOG = LoggerFactory.getLogger(RangerAuthorizationInfo.class);

    private final String userName;
    private final Set<String> userGroups;
    private final String clientIpAddress;
    private final RangerBasePlugin plugin;

    public RangerAuthorizationInfo(RangerBasePlugin plugin, String userName, Set<String> userGroups, String clientIpAddress) {
        this.plugin = plugin;
        this.userName = userName;
        this.userGroups = userGroups;
        this.clientIpAddress = clientIpAddress;
    }

    @Override
    public Access isAuthorized(Resource resource, Action action) {
        LOG.info("check authorization for resource={}, action={} for user={}", new Object[] { resource, action, userName });
        final RangerAccessRequestImpl req = new RangerAccessRequestImpl(
                new DruidResource(resource), action.name(), userName, userGroups);
        req.setClientIPAddress(clientIpAddress);
        final RangerAccessResult result = plugin.isAccessAllowed(req);
        LOG.info("result is {} for {} to {}", new Object[] { result, resource.getName(), action});
        return new Access(result.getIsAllowed(), result.getReason());
    }

    public String getUserName() {
        return userName;
    }

    public Set<String> getUserGroups() {
        return userGroups;
    }

}