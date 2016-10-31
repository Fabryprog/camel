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
package org.apache.camel.component.hazelcast.executor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.component.hazelcast.HazelcastDefaultEndpoint;
import org.apache.camel.component.hazelcast.HazelcastDefaultProducer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

/**
 * Implementation of Hazelcast Executor {@link Producer}.
 */
public class HazelcastExecutorProducer extends HazelcastDefaultProducer {

    private final IExecutorService executor;

    public HazelcastExecutorProducer(HazelcastInstance hazelcastInstance, HazelcastDefaultEndpoint endpoint, String executorName) {
        super(endpoint);

        this.executor = hazelcastInstance.getExecutorService("executor");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void process(Exchange exchange) throws Exception {

        Map<String, Object> headers = exchange.getIn().getHeaders();

        // get header parameters
        List<Object> args = new LinkedList<Object>();
        if (headers.containsKey(HazelcastConstants.EXECUTOR_PARAMS)) {
            args = (List<Object>)headers.get(HazelcastConstants.EXECUTOR_PARAMS);
        }

        Object obj = null;
        if (args.isEmpty()) {
            obj = Class.forName(HazelcastConstants.EXECUTOR_CLASS).newInstance();
        } else {
            // retrieve argument from header
            List<Class> cls = new ArrayList<Class>(args.size());
            for (Object o : args) {
                cls.add(o.getClass());
            }

            obj = Class.forName(HazelcastConstants.EXECUTOR_CLASS).getConstructor(cls.toArray(new Class[cls.size()])).newInstance(args);
        }

        executor.execute((Runnable)obj);

        // TODO member selector
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
