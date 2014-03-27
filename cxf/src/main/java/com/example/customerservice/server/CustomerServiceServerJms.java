/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.example.customerservice.server;

import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;

import com.example.customerservice.CustomerService;

public class CustomerServiceServerJms {
    
    protected CustomerServiceServerJms() {
    }

    public static void main(String args[]) throws Exception {
        ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        PooledConnectionFactory pcf = new PooledConnectionFactory();
        pcf.setConnectionFactory(cf);

        JMSConfiguration jmsConfig = new JMSConfiguration();
        jmsConfig.setConnectionFactory(pcf);
        jmsConfig.setTargetDestination("customerservice");
        jmsConfig.setReplyDestination("customerservicereply");
        jmsConfig.setExplicitQosEnabled(true);
        jmsConfig.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        JMSConfigFeature jmsConfigFeature = new JMSConfigFeature();
        jmsConfigFeature.setJmsConfig(jmsConfig);

        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setAddress("jms://");
        factory.setServiceClass(CustomerService.class);
        factory.setServiceBean(new CustomerServiceImpl(0));
        factory.setFeatures(Arrays.asList(jmsConfigFeature));
        Server server = factory.create();

        System.in.read();
        server.destroy();
    }
}
