package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import messaging.*;

public class Client extends JFrame implements ActionListener {
    
    JTextField depositAccount = new JTextField(16);
    JTextField depositAmount = new JTextField(16);
    JTextField withdrawalAccount = new JTextField(16);
    JTextField withdrawalAmount = new JTextField(16);
    JTextField transferFromAccount = new JTextField(16);
    JTextField transferToAccount = new JTextField(16);
    JTextField transferAmount = new JTextField(16);
    JTextField queryAccount = new JTextField(16);
    JLabel result1 = new JLabel(" ");
    JLabel result2 = new JLabel(" ");
    int serialNumber = 0;
    int clientNumber;
    Messaging messaging;
    
  public Client(int clientNum) {
    super("Bank GUI for Branch " + clientNum);
    this.clientNumber = clientNum;
    setSize(400, 480);
    this.setResizable(false);
    JPanel mainPanel = new JPanel();
    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(layout);
    JPanel depositBox = createDepositBox(mainPanel);
    JPanel withdrawalBox = createWithdrawalBox(mainPanel);
    JPanel transferBox = createTransferBox(mainPanel);
    JPanel queryBox = createQueryBox(mainPanel);
    createResultBox(mainPanel);
    //layout.putConstraint(SpringLayout.SOUTH, depositBox, 5, SpringLayout.NORTH, withdrawalBox);
    
    //mainPanel.add(depositBox);
    //mainPanel.add(withdrawalBox);
    //mainPanel.add(transferBox);
    //mainPanel.add(queryBox);
    getContentPane().add(mainPanel);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setVisible(true);
  }
  
  private JPanel createDepositBox(JPanel panel){
      //JPanel panel = new JPanel();
      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      JLabel title = new JLabel("Make a deposit:");
      title.setAlignmentY(Component.LEFT_ALIGNMENT);
      //panel.add(title);
      leftAlign(panel, title);
      panel.add(createRow(" Account number:             ", depositAccount));
      panel.add(createRow(" Deposit amount:               ", depositAmount));
      JButton button = new JButton("Deposit");
      button.addActionListener(this);
      button.setActionCommand("deposit");
      leftAlign(panel, button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createWithdrawalBox(JPanel panel){
      //JPanel panel = new JPanel();
     // panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      leftAlign(panel, new JLabel("Make a withdrawal:"));
      panel.add(createRow(" Account number:             ", withdrawalAccount));
      panel.add(createRow(" Withdrawal amount:       ", withdrawalAmount));
      JButton button = new JButton("Withdraw");
      button.addActionListener(this);
      button.setActionCommand("withdrawal");
      leftAlign(panel, button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createTransferBox(JPanel panel){
      //JPanel panel = new JPanel();
      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      leftAlign(panel, new JLabel("Transfer money between accounts:                           "));
      panel.add(createRow(" Transfer from account: ", transferFromAccount));
      panel.add(createRow(" Transfer to account:      ", transferToAccount));
      panel.add(createRow(" Transfer amount:            ", transferAmount));
      JButton button = new JButton("Transfer");
      button.addActionListener(this);
      button.setActionCommand("transfer");
      leftAlign(panel, button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createQueryBox(JPanel panel){
      //JPanel panel = new JPanel();
      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      leftAlign(panel, new JLabel("View account balance:"));
      panel.add(createRow(" Account number:            ", queryAccount));
      JButton button = new JButton("Query");
      button.addActionListener(this);
      button.setActionCommand("query");
      leftAlign(panel, button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private void createResultBox(JPanel panel){
	  leftAlign(panel, new JLabel("Result of last operation: "));
	  result1.setFont(new Font("Serif", Font.PLAIN, 14));
	  result2.setFont(new Font("Serif", Font.PLAIN, 14));
	  //panel.add(result);
	  JPanel newPanel = new JPanel(new BorderLayout());
      newPanel.add(result1);
      result1.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.add(newPanel);
	  JPanel newPanel2 = new JPanel(new BorderLayout());
      newPanel2.add(result2);
      result2.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.add(newPanel2);
	  panel.add(new JLabel(" "));
  }

  private JPanel createRow(String title, JTextField field){
    JPanel topRowPanel = new JPanel();
    topRowPanel.setLayout(new BoxLayout(topRowPanel, BoxLayout.X_AXIS));
    topRowPanel.add(new JLabel(title));
    topRowPanel.add(field);
    return topRowPanel;
  }
  
  private void leftAlign(JPanel panel, JComponent component){
	  JPanel newPanel = new JPanel(new BorderLayout());
	  component.setAlignmentX(Component.LEFT_ALIGNMENT);
	  newPanel.add(component);
	  panel.add(newPanel);
	  
  }
  
  private boolean checkAccountNumber(String input){
	  if (input.length() != 8) { return false; }
	  if (input.charAt(2) != '.') {return false; }
	  try {
		  int branch = Integer.parseInt(input.substring(0,2));
		  int account = Integer.parseInt(input.substring(3, input.length()));
		  return true;
	  } catch (NumberFormatException e) {
		  return false;
	  }
  }
  
  private boolean checkAmount(String input){
	  try {
		  Float amount = Float.parseFloat(input);
		  if (amount > 10000000){
			  return false;
		  } else {
			  return true;
		  }
	  } catch (NumberFormatException e) {
		  return false;
	  }
  }
  
  
@Override
public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    if (action.equals("deposit")){
        System.out.println("got deposit");
        System.out.println("account number is: " + depositAccount.getText());
        System.out.println("deposit amount is: " + depositAmount.getText());
        String account = depositAccount.getText();
        String amount = depositAmount.getText();
        if (!checkAccountNumber(account)){
        	result1.setText("Invalid account number format.");
        	result2.setText("Example account number: 12.34567");
        } else if (!checkAmount(amount)) {
        	result1.setText("Invalid deposit amount. Make sure you have entered a number");
        	result2.setText("greater than 0 but less than 10,000,000.");
        } else {
        	//result1.setText("valid account number and amount");
        	int branchNumber = Integer.parseInt(account.substring(0, 2));
        	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
        	float amountFloat = Float.parseFloat(amount);
        	DepositResponse response;
        	try {
				response = messaging.Deposit(new Integer(branchNumber), new Integer(accountNumber), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
				serialNumber++;
				if (response.getSuccess()){
					result1.setText("Deposit successful");
					result2.setText("Balance: " + response.getBalance());
				} else {
					result1.setText(response.getFailureReason());
					result2.setText("");
				}
			} catch (MessagingException e1) {
				result1.setText("A network error occurred");
				result2.setText("");
			}
        	
        }
    } else if (action.equals("withdrawal")){
        System.out.println("got withdrawal");
        System.out.println("account number is: " + withdrawalAccount.getText());
        System.out.println("amount is: " + withdrawalAmount.getText());
        String account = withdrawalAccount.getText();
        String amount = withdrawalAmount.getText();
        if (!checkAccountNumber(account)){
        	result1.setText("Invalid account number format.");
        	result2.setText("Example account number: 12.34567.");
        } else if (!checkAmount(amount)) {
        	result1.setText("Invalid withdrawal amount. Make sure you have entered a number");
        	result2.setText("greater than 0 but less than 10,000,000.");
        } else {
        	//result1.setText("valid account number and amount");
        	int branchNumber = Integer.parseInt(account.substring(0, 2));
        	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
        	float amountFloat = Float.parseFloat(amount);
        	WithdrawResponse response;
        	try {
	        	response = messaging.Withdraw(new Integer(branchNumber), new Integer(accountNumber), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
	        	serialNumber++;
	        	if (response.getSuccess()){
					result1.setText("Withdrawal successful");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.getFailureReason());
					result2.setText("");
	        	}
        	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
        	}
        }
    } else if (action.equals("transfer")){
        System.out.println("got transfer");
        System.out.println("from account number is: " + transferFromAccount.getText());
        System.out.println("to account number is: " + transferToAccount.getText());
        System.out.println("transfer amount is: " + transferAmount.getText());
        String accountTo = transferToAccount.getText();
        String accountFrom = transferFromAccount.getText();
        String amount = transferAmount.getText();
        if (!checkAccountNumber(accountTo)){
        	result1.setText("Invalid account number format.");
        	result2.setText("Example account number: 12.34567.");
        } else if (!checkAccountNumber(accountFrom)){
        	result1.setText("Invalid account number format.");
        	result2.setText("Example account number: 12.34567.");
        } else if (!checkAmount(amount)) {
        	result1.setText("Invalid withdrawal amount. Make sure you have entered a number");
        	result2.setText("greater than 0 but less than 10,000,000.");
        } else {
        	//result1.setText("valid account number and amount");
        	int branchNumberTo = Integer.parseInt(accountTo.substring(0, 2));
        	int accountNumberTo = Integer.parseInt(accountTo.substring(3, accountTo.length()));
        	int branchNumberFrom = Integer.parseInt(accountFrom.substring(0, 2));
        	int accountNumberFrom = Integer.parseInt(accountFrom.substring(3, accountFrom.length()));
        	float amountFloat = Float.parseFloat(amount);
        	TransferResponse response;
        	try {
	        	response = messaging.Transfer(new Integer(branchNumberFrom), new Integer(accountNumberFrom), new Integer(branchNumberTo), new Integer(accountNumberTo), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
	        	serialNumber++;
	        	if (response.getSuccess()){
					result1.setText("Transfer successful");
					result2.setText("Balance in source account: " + response.getBalance());
	        	} else {
					result1.setText(response.getFailureReason());
					result2.setText("");
	        	}
        	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
        	}
        }
    } else if (action.equals("query")){
        System.out.println("got query");
        System.out.println("account number is: " + queryAccount.getText());
        String account = queryAccount.getText();
        if (!checkAccountNumber(account)){
        	result1.setText("Invalid account number format.");
        	result2.setText("Example account number: 12.34567.");
        } else {
        	//result1.setText("valid account number and amount");
        	int branchNumber = Integer.parseInt(account.substring(0, 2));
        	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
        	QueryResponse response;
        	try {
	        	response = messaging.Query(new Integer(branchNumber), new Integer(accountNumber), (serialNumber*100) + serialNumber);
	        	serialNumber++;
	        	if (response.getSuccess()){
					result1.setText("Query successful.");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.getFailureReason());
					result2.setText("");
	        	}
        	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
        	}
        }
    } else {
        System.out.println("Invalid action type received from GUI");
    } 
}

public static void main(String[] args){
	int clientNum = -1;
	try {
		clientNum = Integer.parseInt(args[0]); 
	} catch (Exception e){
		System.out.println("Please run the program with the client number as the first argument.");
		System.exit(0);
	}
	if ((clientNum < 0) || (clientNum > 99)){
		System.out.println("Please enter a client number between 0 and 99.");
		System.exit(0);
	}
	Client client = new Client(clientNum);
	try {
		client.messaging = new Messaging(new Integer(clientNum), Messaging.Type.CLIENT);
	} catch (MessagingException e) {
		System.out.println("Could not create socket");
	}
	//client.messaging.branch = clientNum;
}


}