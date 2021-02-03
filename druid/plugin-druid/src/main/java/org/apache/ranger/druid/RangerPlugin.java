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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

import io.druid.guice.JsonConfigProvider;
import io.druid.initialization.DruidModule;
import io.druid.server.initialization.jetty.ServletFilterHolder;

/**
 * This class is created by the druid processes, 
 * and here it can contribute various settings and functionalities - currently it installs a ServletFilter, 
 * which intercepts all the requests.
 *
 */
public class RangerPlugin implements DruidModule {

    @Override
    public void configure(Binder binder) {
        Multibinder.newSetBinder(binder, ServletFilterHolder.class)
            .addBinding()
            .to(RangerFilterHolder.class);
        JsonConfigProvider.bind(binder, "ranger.config", PluginConfig.class);
    }

    @Override
    public List<? extends Module> getJacksonModules() {
        return Collections.emptyList();
    }

}