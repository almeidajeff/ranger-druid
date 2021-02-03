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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.druid.server.security.Access;
import io.druid.server.security.Action;
import io.druid.server.security.AuthorizationInfo;
import io.druid.server.security.Resource;

/**
 * AuthorizationInfo implementation which allows all 'read' operations to succeed.
 *
 */
public class ReadonlyAuthorizationInfo implements AuthorizationInfo {
    private final static Logger LOG = LoggerFactory.getLogger(ReadonlyAuthorizationInfo.class);

    public ReadonlyAuthorizationInfo() {
    }

    @Override
    public Access isAuthorized(Resource resource, Action action) {
        if (action == Action.READ) {
            LOG.info("readonly access on {} granted", resource);
            return new Access(true);
        } else {
            LOG.info("Access denied on {} for {}", resource, action);
            return new Access(false,"read.only.access.allowed");
        }
    }

}