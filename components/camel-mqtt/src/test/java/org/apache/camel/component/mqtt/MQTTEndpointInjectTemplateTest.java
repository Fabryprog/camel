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
package org.apache.camel.component.mqtt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.junit.Test;

public class MQTTEndpointInjectTemplateTest extends SpringMQTTTest {
    
    public final static int NUMBER_OF_MESSAGES = 10;
    public final static String TEST_TOPIC = "CamelMQTTSubscribeTopic";
    
    @Test
    public void testProduce() throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost(MQTTTestSupport.getHostForMQTTEndpoint());
        final BlockingConnection subscribeConnection = mqtt.blockingConnection();
        subscribeConnection.connect();
        Topic topic = new Topic(TEST_TOPIC, QoS.AT_MOST_ONCE);
        Topic[] topics = {topic};
        subscribeConnection.subscribe(topics);
        final CountDownLatch latch = new CountDownLatch(NUMBER_OF_MESSAGES * 2);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < NUMBER_OF_MESSAGES * 2; i++) {
                    try {
                        Message message = subscribeConnection.receive();
                        message.ack();
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        thread.start();

        Producer producer = context.getEndpoint("direct:foo").createProducer();
        for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
            Exchange exchange = new ExchangeBuilder(context).build();
            exchange.getIn().setBody("test message " + i);
            exchange.getIn().setHeader(MQTTConfiguration.MQTT_PUBLISH_TOPIC, TEST_TOPIC);
            
            producer.process(exchange);
        }
        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Messages not consumed = " + latch.getCount(), latch.getCount() == 0);
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:foo")
            	.to("mqtt:spring-dsl?host=tcp://127.0.0.1:1101&qualityOfService=AtMostOnce")
                    .to("direct:spring-dsl");
            }
        };
    }
}
