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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.ws.Response;

import org.apache.commons.lang.time.StopWatch;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.GetCustomersByNameResponse;

public final class CustomerServiceTester {

	CustomerService customerService;

	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

	public void testCustomerService() throws Exception {
		int numMessages = 40000;
		int numThreads = 20;

		doRun(numMessages, numThreads);
		StopWatch watch = new StopWatch();
		watch.start();
		doRun(numMessages, numThreads);
		watch.stop();
		System.out.println(numMessages * 1000 / watch.getTime());
		System.out.println(watch.toString());
	}

	private void doRun(int numMessages, int numThreads) throws InterruptedException, Exception {
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);
		final AtomicInteger count = new AtomicInteger();

		final Customer customer = new Customer();
		for (int c = 0; c < numMessages; c++) {
			pool.execute(new Runnable() {

				@Override
				public void run() {
					int curCount = count.addAndGet(1);
					if (curCount % 100 == 0) {
						System.out.println(curCount);
					}
					
					// At this point you can decide to test the one way or the request / response method
					
					customerService.updateCustomer(customer);
					//callGetCustomerByName();

				}

				private void callGetCustomerByName() {
					Response<GetCustomersByNameResponse> resp = customerService.getCustomersByNameAsync("test");
					try {
						GetCustomersByNameResponse customers = resp.get();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
					    throw new RuntimeException(e);
					}
				}

			});
		}
		pool.shutdown();
		pool.awaitTermination(10000, TimeUnit.SECONDS);
	}

}
