/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mqtt.bean;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mqtt.MQTTEndpointInjectTemplateTest;

public class MQTTBean implements CamelContextAware {
    @EndpointInject(uri = "mqtt:spring-dsl?host=tcp://127.0.0.1:1101&qualityOfService=AtMostOnce")
    private ProducerTemplate mqttProducerTemplate;

    private CamelContext context;

    public void send(@Body Object body, @Headers Map<String, Object> headers) throws Exception {
        
        
        for (int i = 0; i < MQTTEndpointInjectTemplateTest.NUMBER_OF_MESSAGES; i++) {
            Exchange exchange = new ExchangeBuilder(context).build();
            exchange.getIn().setBody(body);
            exchange.getIn().setHeaders(headers);

            mqttProducerTemplate.send(exchange);
        }
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return context;
    }
}
