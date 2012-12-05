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

public class ClientTest {
	
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
	 * This test assumes valid 5-jvm text files (topology, replicaResolver, jvmInfo, clientResolver, fdsResolver).
	 * Valid is as defined in the documentation. Our submission contains a valid 5-jvm set of text files.
	 * @throws InterruptedException
	 * @throws MessagingException
	 */
	@Test
	public void testOperations() throws InterruptedException, MessagingException {
		
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
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("abc");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid deposit amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//valid deposit
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("100");
		client.depositSerial.setText("99999");
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 100.0");
		
		//invalid serial
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("100");
		client.depositSerial.setText("99999");
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid serial number.");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.depositAccount.setText("05.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		wait(1);
		assertEquals(client.result1.getText(), "Can only deposit to this branch.");
		assertEquals(client.result2.getText(), "");
		
		
		//------------------ Test Withdrawal ---------------------------
		
		//invalid account
		client.withdrawalAccount.setText("abc");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567.");
		
		//invalid amount
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("abc");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid withdrawal amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//invalid serial
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText("99999");
		client.handleWithdrawal();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid serial number.");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.withdrawalAccount.setText("05.55555");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		wait(1);
		assertEquals(client.result1.getText(), "Can only withdraw from this branch.");
		assertEquals(client.result2.getText(), "");
		
		//valid withdrawal
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("50");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		wait(1);
		assertEquals(client.result1.getText(), "Withdrawal successful");
		assertEquals(client.result2.getText(), "Balance: 50.0");
		
		
		//------------------ Test Query ---------------------------
		
		//invalid account
		client.queryAccount.setText("abc");
		client.querySerial.setText(newSerial());
		client.handleQuery();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567.");
		
		//invalid serial
		client.queryAccount.setText("01.11111");
		client.querySerial.setText("99999");
		client.handleQuery();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid serial number.");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.queryAccount.setText("05.55555");
		client.querySerial.setText(newSerial());
		client.handleQuery();
		wait(1);
		assertEquals(client.result1.getText(), "Can only query accounts in this branch.");
		assertEquals(client.result2.getText(), "");
		
		//valid query
		client.queryAccount.setText("01.11111");
		client.querySerial.setText(newSerial());
		client.handleQuery();
		wait(1);
		assertEquals(client.result1.getText(), "Query successful.");
		assertEquals(client.result2.getText(), "Balance: 50.0");
		
		
		//------------------ Test Transfer ---------------------------
		
		//invalid source account
		client.transferFromAccount.setText("abc");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567.");
		
		//invalid destination account
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("abc");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567.");
		
		//invalid amount
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("abc");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid transfer amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//invalid serial
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("100");
		client.transferSerial.setText("99999");
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Invalid serial number.");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.transferFromAccount.setText("05.55555");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("100");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Can only transfer from this branch.");
		assertEquals(client.result2.getText(), "");
		
		//valid transfer
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		wait(1);
		assertEquals(client.result1.getText(), "Transfer successful");
		assertEquals(client.result2.getText(), "Balance in source account: 25.0");
		
		System.out.println("Test operations passed, killing all JVMs");
		
		jvm1.kill();
		jvm2.kill();
		jvm3.kill();
		jvm4.kill();
		jvm5.kill();
		wait(10);
	}

}







