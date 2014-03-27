package com.example.customerservice.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.lang.time.StopWatch;

public class JmsSender {
	protected static String mbSt;

	public static void main(String[] args) throws Throwable {
		int numMessages = 5000;
		int numThreads = 5;
		
		mbSt = createMbStrig();
		System.out.println(mbSt.length());
		
		// Only for sending pure jms Messages without CXF
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		cf.setUseAsyncSend(true);
		PooledConnectionFactory ccf = new PooledConnectionFactory(cf);
		Connection con = cf.createConnection();

		doRun(con, numMessages, numThreads);
		StopWatch watch = new StopWatch();
		watch.start();
		doRun(con, numMessages, numThreads);
		watch.stop();
		con.close();
		System.out.println(numMessages * 1000 / watch.getTime());
		System.out.println(watch.toString());

	}

	private static String createMbStrig() {
		StringBuilder testStringBuilder = new StringBuilder();
		for (int c=0; c<100; c++) {
			testStringBuilder.append("0123456789");
		}
		String kbSt = testStringBuilder.toString();
		testStringBuilder = new StringBuilder();
		for (int c=0; c<1000; c++) {
			testStringBuilder.append(kbSt);
		}
		return testStringBuilder.toString();
	}

	private static void doRun(final Connection con, int numMessages, int numThreads) throws InterruptedException, Exception {
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);
		final AtomicInteger count = new AtomicInteger();

		for (int c = 0; c < numMessages; c++) {
			pool.execute(new Runnable() {

				@Override
				public void run() {
					int curCount = count.addAndGet(1);
					if (curCount % 100 == 0) {
						System.out.println(curCount);
					}
					// customerService.updateCustomer(customer);
					sendMessage(con, count);

				}

				private void sendMessage(final Connection con,
						final AtomicInteger count) {
					try {

						Session sess = con.createSession(false,
								Session.AUTO_ACKNOWLEDGE);

						TextMessage me = sess.createTextMessage(mbSt);
						me.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
						Queue dest = sess.createQueue("jmssendertest");
						MessageProducer prod = sess.createProducer(dest);
						prod.send(me);
						prod.close();
						sess.close();
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		pool.shutdown();
		pool.awaitTermination(10000, TimeUnit.SECONDS);
	}
}
