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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.ranger.plugin.policyengine.RangerAccessRequest;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.apache.ranger.plugin.service.RangerBasePlugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.druid.server.security.Access;
import io.druid.server.security.Action;
import io.druid.server.security.Resource;
import io.druid.server.security.ResourceType;

public class RangerAuthorizationInfoTest {

    private static final String GROUP_NAME = "groupName";
    private static final String USER_NAME = "userName";
    private static final String TEST_DATASOURCE = "testDatasource";
    RangerBasePlugin plugin;

    @Before
    public void setup() {
        plugin = Mockito.mock(RangerBasePlugin.class);
    }

    @Test
    public void testAccessGranted() throws Throwable {
        RangerAuthorizationInfo rangerAuthorizationInfo0 = new RangerAuthorizationInfo(plugin, USER_NAME,
                Collections.singleton(GROUP_NAME), "127.0.0.1");
        Resource resource0 = new Resource(TEST_DATASOURCE, ResourceType.DATASOURCE);

        RangerAccessResult result = new RangerAccessResult(null, null, null);
        result.setIsAllowed(true);
        when(plugin.isAccessAllowed(any(RangerAccessRequest.class))).thenReturn(result);
        Access access = rangerAuthorizationInfo0.isAuthorized(resource0, Action.READ);
        assertEquals(true, access.isAllowed());
        ArgumentCaptor<RangerAccessRequest> captor = ArgumentCaptor.forClass(RangerAccessRequest.class);
        verify(plugin).isAccessAllowed(captor.capture());
        RangerAccessRequest capturedValue = captor.getValue();
        assertEquals(USER_NAME, capturedValue.getUser());
        assertEquals(Collections.singleton(GROUP_NAME), capturedValue.getUserGroups());
        assertEquals(Collections.singletonMap("datasource", TEST_DATASOURCE), capturedValue.getResource().getAsMap());
        assertEquals("READ", capturedValue.getAccessType());
    }

    @Test
    public void testAccessDenied() throws Throwable {
        RangerAuthorizationInfo rangerAuthorizationInfo0 = new RangerAuthorizationInfo(plugin, USER_NAME,
                Collections.singleton(GROUP_NAME), "127.0.0.1");
        Resource resource0 = new Resource(TEST_DATASOURCE, ResourceType.DATASOURCE);

        RangerAccessResult result = new RangerAccessResult(null, null, null);
        result.setIsAllowed(false);
        when(plugin.isAccessAllowed(any(RangerAccessRequest.class))).thenReturn(result);
        Access access = rangerAuthorizationInfo0.isAuthorized(resource0, Action.WRITE);
        assertEquals(false, access.isAllowed());
        ArgumentCaptor<RangerAccessRequest> captor = ArgumentCaptor.forClass(RangerAccessRequest.class);
        verify(plugin).isAccessAllowed(captor.capture());
        RangerAccessRequest capturedValue = captor.getValue();
        assertEquals(USER_NAME, capturedValue.getUser());
        assertEquals(Collections.singleton(GROUP_NAME), capturedValue.getUserGroups());
        assertEquals(Collections.singletonMap("datasource", TEST_DATASOURCE), capturedValue.getResource().getAsMap());
        assertEquals("WRITE", capturedValue.getAccessType());
    }

}