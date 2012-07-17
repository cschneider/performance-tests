package com.example.customerservice.server.purejms;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;

import com.example.customerservice.server.SpeedTracker;

public class PureJmsServer {
    
    static String reply = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:getCustomersByNameResponse xmlns:ns2=\"http://customerservice.example.com/\">" +
         "<return><customerId>0</customerId><name>Test</name><address></address><numOrders>1</numOrders><revenue>10000.0</revenue><test>1.5</test><birthDate>2009-02-01+01:00</birthDate><type>BUSINESS</type></return>" +
      "</ns2:getCustomersByNameResponse></soap:Body></soap:Envelope>";
    
    private static final class MyMessageListener implements SessionAwareMessageListener<Message> {
        private final SpeedTracker tracker;

        public MyMessageListener(SpeedTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void onMessage(Message message, Session session)
                throws JMSException {
            tracker.count();
            try {
                Destination replyDest = message.getJMSReplyTo();
                
                //System.out.println("Received message. Sending reply to " + replyDest.toString());
                MessageProducer prod = session.createProducer(replyDest);
                TextMessage replyMessage = session.createTextMessage(reply);
                replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                prod.send(replyMessage);
                prod.close();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
    

    public static void main(String[] args) throws JMSException, IOException {
        ActiveMQConnectionFactory origcf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        final PooledConnectionFactory cf = new PooledConnectionFactory(origcf);
        cf.setMaxConnections(20);
        
        final SpeedTracker tracker = new SpeedTracker();
        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(cf);
        listenerContainer.setMaxConcurrentConsumers(10);
        listenerContainer.setDestinationName("customerservice");
        listenerContainer.setMessageListener(new MyMessageListener(tracker));
        listenerContainer.afterPropertiesSet();
        listenerContainer.start();
        
        System.in.read();
        
        listenerContainer.shutdown(); 
    }

}
