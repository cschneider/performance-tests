package com.example.customerservice.common;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;

public class ConfigFactory {

    public static JMSConfigFeature create() {
        
        ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        PooledConnectionFactory pcf = new PooledConnectionFactory();
        pcf.setConnectionFactory(cf);

        JMSConfiguration jmsConfig = new JMSConfiguration();
        jmsConfig.setConnectionFactory(pcf);
        jmsConfig.setTargetDestination("customerservice");
        jmsConfig.setReplyDestination("customerservicereply");
        jmsConfig.setUseConduitIdSelector(true);
        jmsConfig.setExplicitQosEnabled(true);
        jmsConfig.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        jmsConfig.setTimeToLive(10000);
        JMSConfigFeature jmsConfigFeature = new JMSConfigFeature();        
        jmsConfigFeature.setJmsConfig(jmsConfig);
        return jmsConfigFeature;
    }
}
