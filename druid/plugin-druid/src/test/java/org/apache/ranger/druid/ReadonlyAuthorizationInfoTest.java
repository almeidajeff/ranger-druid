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

import org.junit.Test;
import static org.junit.Assert.*;
import io.druid.server.security.Access;
import io.druid.server.security.Action;
import io.druid.server.security.Resource;

public class ReadonlyAuthorizationInfoTest {

  @Test
  public void testReadAllowed()  throws Throwable  {
      ReadonlyAuthorizationInfo authorizationInfo = new ReadonlyAuthorizationInfo();
      Access access0 = authorizationInfo.isAuthorized((Resource) null, Action.READ);
      assertTrue(access0.isAllowed());
  }

  @Test
  public void testWriteAllowed()  throws Throwable  {
      ReadonlyAuthorizationInfo readonlyAuthorizationInfo0 = new ReadonlyAuthorizationInfo();
      Access access0 = readonlyAuthorizationInfo0.isAuthorized((Resource) null, Action.WRITE);
      assertFalse(access0.isAllowed());
  }
}