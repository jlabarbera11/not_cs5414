package client.test;

import static org.junit.Assert.*;
import client.*;
import server.*;

import org.junit.Test;

public class ClientTest {
	
	private int serial=0;
	
	private String newSerial(){
		serial++;
		return "" + serial;
	}

	@Test
	public void test() {
		Server server = new Server(1);
		server.run();
		
		Client client = new Client(1);
		
		//------------------ Test Deposit ---------------------------
		
		//invalid account
		client.depositAccount.setText("abc");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid amount
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("abc");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		assertEquals(client.result1.getText(), "Invalid deposit amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//valid deposit
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("100");
		client.depositSerial.setText("99999");
		client.handleDeposit();
		assertEquals(client.result1.getText(), "Deposit successful");
		assertEquals(client.result2.getText(), "Balance: 100");
		
		//invalid serial
		client.depositAccount.setText("01.11111");
		client.depositAmount.setText("100");
		client.depositSerial.setText("99999");
		client.handleDeposit();
		assertEquals(client.result1.getText(), "Invalid serial number");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.depositAccount.setText("05.55555");
		client.depositAmount.setText("100");
		client.depositSerial.setText(newSerial());
		client.handleDeposit();
		assertEquals(client.result1.getText(), "A network error occurred");
		assertEquals(client.result2.getText(), "");
		
		
		//------------------ Test Withdrawal ---------------------------
		
		//invalid account
		client.withdrawalAccount.setText("abc");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid amount
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("abc");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "Invalid withdrawal amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//invalid serial
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText("99999");
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "Invalid serial number");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.withdrawalAccount.setText("05.55555");
		client.withdrawalAmount.setText("100");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "A network error occurred");
		assertEquals(client.result2.getText(), "");
		
		//valid withdrawal
		client.withdrawalAccount.setText("01.11111");
		client.withdrawalAmount.setText("50");
		client.withdrawalSerial.setText(newSerial());
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "Withdrawal successful");
		assertEquals(client.result2.getText(), "Balance: 50");
		
		
		//------------------ Test Query ---------------------------
		
		//invalid account
		client.queryAccount.setText("abc");
		client.querySerial.setText(newSerial());
		client.handleWithdrawal();
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid serial
		client.queryAccount.setText("01.11111");
		client.querySerial.setText("99999");
		client.handleQuery();
		assertEquals(client.result1.getText(), "Invalid serial number");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.queryAccount.setText("05.55555");
		client.querySerial.setText(newSerial());
		client.handleQuery();
		assertEquals(client.result1.getText(), "A network error occurred");
		assertEquals(client.result2.getText(), "");
		
		//valid query
		client.queryAccount.setText("01.11111");
		client.querySerial.setText(newSerial());
		client.handleQuery();
		assertEquals(client.result1.getText(), "Query successful");
		assertEquals(client.result2.getText(), "Balance: 50");
		
		
		//------------------ Test Transfer ---------------------------
		
		Server server2 = new Server(2);
		server2.run();
		
		//invalid source account
		client.transferFromAccount.setText("abc");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid destination account
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("abc");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		assertEquals(client.result1.getText(), "Invalid account number format.");
		assertEquals(client.result2.getText(), "Example account number: 12.34567");
		
		//invalid amount
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("abc");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		assertEquals(client.result1.getText(), "Invalid transfer amount. Make sure you have entered a number");
		assertEquals(client.result2.getText(), "greater than 0 but less than 10,000,000.");
		
		//invalid serial
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("100");
		client.transferSerial.setText("99999");
		client.handleTransfer();
		assertEquals(client.result1.getText(), "Invalid serial number");
		assertEquals(client.result2.getText(), "");
		
		//messaging error
		client.transferFromAccount.setText("05.55555");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("100");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		assertEquals(client.result1.getText(), "A network error occurred");
		assertEquals(client.result2.getText(), "");
		
		//valid transfer
		client.transferFromAccount.setText("01.11111");
		client.transferToAccount.setText("02.22222");
		client.transferAmount.setText("25");
		client.transferSerial.setText(newSerial());
		client.handleTransfer();
		assertEquals(client.result1.getText(), "Transfer successful");
		assertEquals(client.result2.getText(), "Balance in source account: 25");
		
		
		//------------------ Test Snapshot ---------------------------
		
		//valid withdrawal
		client.handleSnapshot();
		assertEquals(client.result1.getText(), "Taking snapshot...");
		assertEquals(client.result2.getText(), "");
		
	}
	
	
	

}







