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
package org.apache.ranger.service.druid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.ranger.plugin.client.BaseClient;
import org.apache.ranger.plugin.model.RangerService;
import org.apache.ranger.plugin.model.RangerServiceDef;
import org.apache.ranger.plugin.service.RangerBaseService;
import org.apache.ranger.plugin.service.ResourceLookupContext;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to communicate with the Druid broker.
 *
 */
public class RangerDruidService extends RangerBaseService {
    private static final String DRUID_BROKER_URL = "druid.broker.url";
    private final static Logger LOG = LoggerFactory.getLogger(RangerDruidService.class);
    private CloseableHttpClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(RangerServiceDef serviceDef, RangerService service) {
        super.init(serviceDef, service);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        // defaults are fine
        client = clientBuilder.build();
    }

    @Override
    public Map<String, Object> validateConfig() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RangerDruidService.validateConfig(" + serviceName + ")");
        }
        final URI statusUri;
        try {
            statusUri = getDruidBrokerURI("/status");
        } catch (IllegalArgumentException e) {
            return error("druid.broker.url.missing", "druid.broker.url.missing");
        } catch (URISyntaxException uriException) {
            return error("druid.broker.url.incorrect", uriException.getMessage());
        }
        try (CloseableHttpResponse httpResponse = client.execute(new HttpGet(statusUri))) {
            JsonNode jsonNode = objectMapper.readValue(httpResponse.getEntity().getContent(), JsonNode.class);
            String version = jsonNode.get("version").getTextValue();
            if (version == null || version.length() == 0) {
                return error("version.not.specified", "response:" + jsonNode);
            }
            if (!version.startsWith("0.10.")) {
                return error("unknown.version", "Version:" + version);
            }
            return ok("Connection successful", "Ok");
        }
    }

    private Map<String, Object> ok(String message, String description) {
        Map<String, Object> responseData = new HashMap<>();
        BaseClient.generateResponseDataMap(true, message, description, null, DRUID_BROKER_URL, responseData);
        return responseData;
    }

    private Map<String, Object> error(String message, String description) {
        Map<String, Object> responseData = new HashMap<>();
        BaseClient.generateResponseDataMap(false, message, description, null, DRUID_BROKER_URL, responseData);
        return responseData;
    }

    @Override
    public List<String> lookupResource(ResourceLookupContext context) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RangerDruidService.lookupResource(" + context + ")");
        }
        LOG.warn("lookup " + context);
        final List<String> result = new ArrayList<>();
        final String userInput = context.getUserInput().toLowerCase();
        final HttpGet datasourceList = new HttpGet(getDruidBrokerURI("/druid/v2/datasources"));
        try (CloseableHttpResponse httpResponse = client.execute(datasourceList)) {
            JsonNode jsonNode = objectMapper.readValue(httpResponse.getEntity().getContent(), JsonNode.class);
            if (jsonNode.isArray()) {
                for (int i = 0; i < jsonNode.size(); i++) {
                    JsonNode childNode = jsonNode.get(i);
                    String textValue = childNode.getTextValue();
                    if (textValue.toLowerCase().contains(userInput)) {
                        result.add(textValue);
                    }
                }
            }
        }

        return result;
    }

    private URI getDruidBrokerURI(String relativePath) throws URISyntaxException, IllegalArgumentException {
        final String brokerUrl = configs.get(DRUID_BROKER_URL);
        Validate.notEmpty(brokerUrl, "missing.druid.broker.url");
        return new URI(brokerUrl).resolve(relativePath);
    }

}