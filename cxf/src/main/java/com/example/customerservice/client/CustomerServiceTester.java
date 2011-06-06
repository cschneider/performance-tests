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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.jms.connection.CachingConnectionFactory;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;

public final class CustomerServiceTester {

	// The CustomerService proxy will be injected either by spring or by a
	// direct call to the setter
	CustomerService customerService;

	public CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

	public void testCustomerService() throws Exception {
		//System.in.read();
		int numMessages = 10000;
		int numThreads = 50;
		final Customer customer = new Customer();
		customer.setName("Smith");

		// Only for sending pure jms Messages without CXF
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
		ccf.setSessionCacheSize(numThreads);
		Connection con = cf.createConnection();

		doRun(customer, con, numMessages, numThreads);
		StopWatch watch = new StopWatch();
		watch.start();
		doRun(customer, con, numMessages, numThreads);
		watch.stop();
		con.close();
		System.out.println(numMessages * 1000 / watch.getTime());
		System.out.println(watch.toString());
		ccf.destroy();
	}

	private void doRun(final Customer customer, final Connection con, int numMessages, int numThreads) 
		throws InterruptedException, Exception {
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
					//customerService.updateCustomer(customer);
					sendMessage(con, count);

				}

				private void sendMessage(final Connection con,
						final AtomicInteger count) {
					try {

						Session sess = con.createSession(false,
								Session.AUTO_ACKNOWLEDGE);
						
						TextMessage me = sess.createTextMessage("Test");
						Queue dest = sess.createQueue("example.B");
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
