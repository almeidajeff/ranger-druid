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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.apache.ranger.plugin.service.RangerBasePlugin;

import com.google.inject.Inject;

import io.druid.server.initialization.jetty.ServletFilterHolder;

/**
 * Class which contributes the RangerFilter into Druid's servlet stack.
 *
 */
public class RangerFilterHolder implements ServletFilterHolder {

    private final PluginConfig config;

    @Inject
    public RangerFilterHolder(PluginConfig config) {
        this.config = config;
    }

    @Override
    public Filter getFilter() {
        return new RangerFilter(config, new RangerBasePlugin("druid", "druid"));
    }

    @Override
    public Class<? extends Filter> getFilterClass() {
        return RangerFilter.class;
    }

    @Override
    public Map<String, String> getInitParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ranger.config.unsecureMode", String.valueOf(config.isUnsecureMode()));
        return params;
    }

    @Override
    public String getPath() {
        return "/*";
    }

    @Override
    public EnumSet<DispatcherType> getDispatcherType() {
        return null;
    }

}