package com.example.customerservice.server.purejms;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.lang.time.StopWatch;

public class PureJmsClient {
    static String request = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:getCustomersByName xmlns:ns2=\"http://customerservice.example.com/\"><name>test2</name></ns2:getCustomersByName></soap:Body></soap:Envelope>";

    public static void main(String[] args) throws JMSException, IOException, InterruptedException {
        ActiveMQConnectionFactory origcf = new ActiveMQConnectionFactory(
                "tcp://localhost:61616");
        PooledConnectionFactory cf = new PooledConnectionFactory(origcf);
        cf.setMaxConnections(20);

        final Connection con = cf.createConnection();
        con.start();
        int numMessages = 20000;

        requestReplyAll(con, numMessages);
        
        StopWatch watch = new StopWatch();
        watch.start();
        requestReplyAll(con, numMessages);
        watch.stop();
        System.out.println(numMessages * 1000 / watch.getTime());
        System.out.println(watch.toString());

        con.stop();
        con.close();
        cf.stop();
    }

    private static void requestReplyAll(final Connection con, int numMessages) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int c=0; c<numMessages ; c++) {
            pool.execute(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        doRequestReply(con);
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10000, TimeUnit.SECONDS);
    }

    private static void doRequestReply(Connection con) throws JMSException {
        Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue dest = session.createQueue("customerservice");
        TextMessage message = session.createTextMessage(request);
        Queue replyDest = session.createQueue("customerservicereply");
        message.setJMSReplyTo(replyDest);
        MessageProducer prod = session.createProducer(dest);
        prod.send(message);
        prod.close();
        
        MessageConsumer consumer = session.createConsumer(replyDest);
        Message replyMessage = consumer.receive();
        if (replyMessage instanceof TextMessage) {
            String content = ((TextMessage)replyMessage).getText();
            //System.out.println(content);
        }
        consumer.close();
        session.close();
    }
}
