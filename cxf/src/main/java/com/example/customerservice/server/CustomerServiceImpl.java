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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import com.example.customerservice.Customer;
import com.example.customerservice.CustomerService;
import com.example.customerservice.CustomerType;
import com.example.customerservice.GetCustomersByNameResponse;
import com.example.customerservice.NoSuchCustomer;
import com.example.customerservice.NoSuchCustomerException;

public class CustomerServiceImpl implements CustomerService {
	private static final int BASE_SIZE = 428; // Size of the soap reply without filler in the address attrib
    private static int SIZE_SMALL = 500; // 500 bytes
	private static int SIZE_MEDIUM = 10000; // 10 KB
	private static int SIZE_LARGE = 1000 * 1000; // 1 MB
	private static int SIZE_XLARGE = 10 * SIZE_LARGE; // 10 MB
	
	private String address;
    private final SpeedTracker tracker;
    private final SpeedTracker trackerOneWay;

	public CustomerServiceImpl(int size) {
		this.trackerOneWay = new SpeedTracker();
		this.tracker = new SpeedTracker();
        int fillSize = (size - BASE_SIZE > 0)? size - 428 : 0;
        StringBuilder largeString = new StringBuilder();
		for (int c=0; c < fillSize / 10; c++) {
			largeString.append("10 chars..");
		}
		address = largeString.toString();
	}

    public List<Customer> getCustomersByName(String name) throws NoSuchCustomerException {
        tracker.count();

        //System.out.println("Service called");
        if ("None".equals(name)) {
            NoSuchCustomer noSuchCustomer = new NoSuchCustomer();
            noSuchCustomer.setCustomerName(name);
            throw new NoSuchCustomerException("Did not find any matching customer for name=" + name,
                                              noSuchCustomer);
        }

        List<Customer> customers = new ArrayList<Customer>();
        for (int c = 0; c < 1; c++) {
            Customer cust = new Customer();
            cust.setName(name);
            cust.getAddress().add(address);
            Date bDate = new GregorianCalendar(2009, 01, 01).getTime();
            cust.setBirthDate(bDate);
            cust.setNumOrders(1);
            cust.setRevenue(10000);
            cust.setTest(new BigDecimal(1.5));
            cust.setType(CustomerType.BUSINESS);
            customers.add(cust);
        }

        return customers;
    }

    public void updateCustomer(Customer customer) {
        trackerOneWay.count();
    }

	@Override
	public Response<GetCustomersByNameResponse> getCustomersByNameAsync(
			String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<?> getCustomersByNameAsync(String name,
			AsyncHandler<GetCustomersByNameResponse> asyncHandler) {
		// TODO Auto-generated method stub
		return null;
	}

}
