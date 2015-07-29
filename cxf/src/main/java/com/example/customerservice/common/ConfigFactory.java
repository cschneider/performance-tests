package com.example.customerservice.common;

import javax.jms.DeliveryMode;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;

public class ConfigFactory {

    public static JMSConfigFeature create() {
        
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        
        // Make sure you use the one from org.apache.activemq.pool
        // as it supports reconnect on failures
        PooledConnectionFactory pcf = new PooledConnectionFactory();
        
        // Make sure to set expiry timeout as the defaullt of 0 prevents reconnect on failures
        pcf.setExpiryTimeout(5000);
        pcf.setConnectionFactory(cf);

        JMSConfiguration jmsConfig = new JMSConfiguration();
        jmsConfig.setConnectionFactory(pcf);
        jmsConfig.setTargetDestination("customerservice");
        jmsConfig.setReplyDestination("customerservicereply");
        //jmsConfig.setExplicitQosEnabled(true);
        jmsConfig.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        jmsConfig.setReceiveTimeout(30000L);
        jmsConfig.setTimeToLive(30000);
        jmsConfig.setConcurrentConsumers(20);
        
        JMSConfigFeature jmsConfigFeature = new JMSConfigFeature();        
        jmsConfigFeature.setJmsConfig(jmsConfig);
        return jmsConfigFeature;
    }
}
