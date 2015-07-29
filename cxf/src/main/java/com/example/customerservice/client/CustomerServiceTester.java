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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.GetCustomersByNameResponse;
import com.example.customerservice.NoSuchCustomerException;
import com.example.customerservice.server.SpeedTracker;

public final class CustomerServiceTester {

    CustomerService customerService;

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public void testCustomerService(int numMessages, int numThreads, final CallType calltype) throws Exception {
        printInfo(numMessages, numThreads, calltype);
        SpeedTracker tracker = new SpeedTracker();
        doRun(tracker, numMessages, numThreads, calltype);
        tracker.reset();
        doRun(tracker, numMessages, numThreads, calltype);
        printInfo(numMessages, numThreads, calltype);
        tracker.showStats();
    }

    private void printInfo(int nummessages, int numThreads, final CallType calltype) {
        System.out.println("Running performance test sending "+ nummessages + " messages using " + numThreads + " threads, call type " + calltype);
    }

    private void doRun(final SpeedTracker tracker, int numMessages, int numThreads, final CallType calltype)
            throws InterruptedException, Exception {
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        final Customer customer = new Customer();
        Runnable callRunnable = new Runnable() {

            @Override
            public void run() {
                tracker.count();
                if (calltype == CallType.oneway) {
                    customerService.updateCustomer(customer);
                } else if (calltype == CallType.requestReply) {
                    try {
                        List<Customer> customers = customerService.getCustomersByName("test2");
                    } catch (NoSuchCustomerException e) {
                    }
                }  else {
                    try {
                        Future<GetCustomersByNameResponse> resp = customerService.getCustomersByNameAsync("test2");
                        GetCustomersByNameResponse res1 = resp.get();
                        //res1.getReturn();
                    } catch (Exception e) {
                    }
                }
            }

        };
        
        for (int c = 0; c < numMessages; c++) {
            pool.execute(callRunnable);
        }
        pool.shutdown();
        pool.awaitTermination(10000, TimeUnit.SECONDS);
    }

}
