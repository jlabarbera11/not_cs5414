package client.test;

import static org.junit.Assert.*;

import java.net.Socket;

import jvm.JVM;
import client.*;
import server.*;

import messaging.Message;
import messaging.Messaging;
import messaging.MessagingException;
import messaging.Ping;
import messaging.ReplicaID;

import org.junit.Test;

public class TestPrimaryFailures {
	
	private int serial=0;
	
	private String newSerial(){
		serial++;
		return "" + serial;
	}
	
	public static void wait (int n){
        long t0,t1;
        t0=System.currentTimeMillis();
        do{
            t1=System.currentTimeMillis();
        }
        while (t1-t0<1000);
	}
	
	/**
	 * This test creates a bunch of jvms, fails some of them, and makes sure
	 * that the remaining replicas work as expected.
	 * @throws InterruptedException
	 * @throws MessagingException
	 */
	@Test
	public void testFailures() throws InterruptedException, MessagingException {
		
		JVM jvm1 = new JVM(1);
		JVM jvm2 = new JVM(2);
		JVM jvm3 = new JVM(3);
		JVM jvm4 = new JVM(4);
		JVM jvm5 = new JVM(5);
		
		jvm1.start();
		jvm2.start();
		jvm3.start();
		jvm4.start();
		jvm5.start();
		
		Client client = new Client(1);
		
		//------------------ Test Deposit ---------------------------
		
		//invalid account
		client.depositAccount.setText("abc");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid amount
		client.depositAccount.setText("01.55555");
		client.depositAmount.setText("abc");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid deposit amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//valid deposit
		client.depositAccount.setText("01.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 100.0");
		
		
		Messaging messaging = new Messaging();
		
		/*
		 * Calling kill will cause the servers to throw exceptions. This is normal.
		 */
		jvm1.kill();
		wait(1);
		boolean failed = false;
		try {
			messaging.sendToReplicaNoResponse(new ReplicaID(1,1), new Message());
			wait(1);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,1), new Message());
			wait(1);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,1), new Message());
		} catch (MessagingException e){
			failed = true;
		}
		assertEquals(true, failed);
		
		wait(1);
		
		//valid deposit with new primary
		client.depositAccount.setText("01.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 200.0");		
		
		//kill new primary
		jvm2.kill();
		wait(1);
		failed = false;
		try {
			messaging.sendToReplicaNoResponse(new ReplicaID(1,2), new Message());
			wait(1);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,2), new Message());
			wait(1);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,2), new Message());
		} catch (MessagingException e){
			failed = true;
		}
		assertEquals(true, failed);
		
		wait(10);
		
		//valid deposit with new primary
		client.depositAccount.setText("01.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		System.out.println("TEST: 4");
		wait(1);
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 300.0");
		
		//kill new primary
		jvm3.kill();
		wait(3);
		failed = false;
		try {
			messaging.sendToReplicaNoResponse(new ReplicaID(1,3), new Message());
			wait(3);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,3), new Message());
			wait(3);
			messaging.sendToReplicaNoResponse(new ReplicaID(1,3), new Message());
		} catch (MessagingException e){
			failed = true;
		}
		assertEquals(true, failed);
		
		//valid deposit with new primary --> this shows that the servers can tolerate 
		//failures of more than half of FDSs
		client.depositAccount.setText("01.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 400.0");
		
		jvm4.kill();
		jvm5.kill();
		
	}
	

}







