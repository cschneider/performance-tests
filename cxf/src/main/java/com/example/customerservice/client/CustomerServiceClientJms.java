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
package com.example.customerservice.client;



import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;

import com.example.customerservice.CustomerService;

public final class CustomerServiceClientJms {

    private CustomerServiceClientJms() {
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

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setAddress("jms://");
        factory.setServiceClass(CustomerService.class);
        factory.setFeatures(Arrays.asList(jmsConfigFeature));
        CustomerService customerService = factory.create(CustomerService.class);
        
        CustomerServiceTester tester = new CustomerServiceTester();
        tester.setCustomerService(customerService);
        
        tester.testCustomerService(160000, 10, CallType.oneway);
        System.exit(0);
    }
}