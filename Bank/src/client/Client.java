package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

import oracle.Oracle;
import oracle.Oracle.replicaState;
import messaging.*;

//TODO: server must return error if it gets duplicate serial!
//TODO: enter a serial for snapshot?

public class Client extends JFrame implements ActionListener {

    public JTextField depositAccount = new JTextField(16);
    public JTextField depositAmount = new JTextField(16);
    public JTextField depositSerial = new JTextField(16);
    public JTextField withdrawalAccount = new JTextField(16);
    public JTextField withdrawalAmount = new JTextField(16);
    public JTextField withdrawalSerial = new JTextField(16);
    public JTextField transferFromAccount = new JTextField(16);
    public JTextField transferToAccount = new JTextField(16);
    public JTextField transferAmount = new JTextField(16);
    public JTextField transferSerial = new JTextField(16);
    public JTextField queryAccount = new JTextField(16);
    public JTextField querySerial = new JTextField(16);
    public JLabel result1 = new JLabel(" ");
    public JLabel result2 = new JLabel(" ");
    //int serialNumber = 0;
    String clientNumber;
    public Messaging messaging;
    boolean waitingForResponse;
    int number=0;
    
    private ConcurrentHashMap<Integer, Set<Integer>> topology;
    private ConcurrentHashMap<String, String[]> resolver;
    private ConcurrentHashMap<String, Oracle.replicaState> replicaStates;
    private ArrayList<String> branchReplicas;
    private String currentPrimary;
    private Map<Integer, String> serialsSeen;
    
    public String GetResult(){
    	return result1.getText();
    }
    
//    private String getClientNumString(){
//    	if (Integer.parseInt(clientNumber) < 10){
//    		return "0" + clientNumber;
//    	} else {
//    		return "" + clientNumber;
//    	}
//    }
    
    //must be called after resolver init
    private ArrayList<String> buildBranchReplicas(){
    	ArrayList<String> output = new ArrayList<String>();
    	for (Map.Entry<String, String[]> entry : resolver.entrySet())
    	{
	    	System.out.println("resolver key is " + entry.getKey().substring(0,2) + " and client number is " + clientNumber);
    	    if (entry.getKey().substring(0,2).equals(clientNumber)){
    	    	//System.out.println("resolver key is " + entry.getKey().substring(0,2) + " and client number is " + getClientNumString());
    	    	output.add(entry.getKey());
    	    }
    	}
    	//a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
    	Collections.sort(output,new Comparator<String>() {
            public int compare(String string1, String string2) {
                return string1.substring(3,5).compareTo(string2.substring(3,5));
            }
        });
    	
    	return output;
    }
    
    private void initializePrimary(){
    	currentPrimary = branchReplicas.get(0);
    }
    
    private void updatePrimary(){
        for (String entry : branchReplicas){
            if (replicaStates.get(entry) == Oracle.replicaState.running){
                if (!entry.equals(currentPrimary)){
                    try {
                        messaging.ClientUpdatePrimary(entry);
                    } catch (MessagingException e) {
                        System.out.println("updating primary failed");
                    }
                }
            }
        }
    }

    private class OracleCallback implements Callback {
        public void Callback(Message message){
            try {
                if (message instanceof FailureOracle){
                    replicaStates.put(((FailureOracle)message).failedReplicaID, replicaState.failed);
                    updatePrimary();
                } else if (message instanceof BackupOracle){
                    replicaStates.put(((BackupOracle)message).recoveredReplicaID, replicaState.running);
                    updatePrimary();
                } else {
                    System.out.println("invalid message type received by client from oracle");
                }
            } catch(Exception e) {
                System.out.println(e.toString() + " thrown from HandleOracleMessages Thread");
                e.printStackTrace();
            }
        }
    }
    
    //public String SetText
    
	  public Client(String clientNum) {
	    super("Bank GUI for Branch " + clientNum);
	    this.clientNumber = clientNum;
	    setSize(400, 700);
            this.serialsSeen = new HashMap<Integer, String>();
	    //this.setResizable(false);
	    JPanel mainPanel = new JPanel();
	    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
	    mainPanel.setLayout(layout);
	    createDepositBox(mainPanel);
	    createWithdrawalBox(mainPanel);
	    createTransferBox(mainPanel);
	    createQueryBox(mainPanel);
	    createSnapshotBox(mainPanel);
	    createResultBox(mainPanel);
	    //layout.putConstraint(SpringLayout.SOUTH, depositBox, 5, SpringLayout.NORTH, withdrawalBox);
	    
	    //mainPanel.add(depositBox);
	    //mainPanel.add(withdrawalBox);
	    //mainPanel.add(transferBox);
	    //mainPanel.add(queryBox);
	    getContentPane().add(mainPanel);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true);
	    waitingForResponse=false;
	  }
	  
	  private void createDepositBox(JPanel panel){
	      //JPanel panel = new JPanel();
	      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	      JLabel title = new JLabel("Make a deposit:");
	      title.setAlignmentY(Component.LEFT_ALIGNMENT);
	      //panel.add(title);
	      leftAlign(panel, title);
	      panel.add(createRow(" Account number:             ", depositAccount));
	      panel.add(createRow(" Deposit amount:               ", depositAmount));
	      panel.add(createRow(" Deposit serial number:   ", depositSerial));
	      JButton button = new JButton("Deposit");
	      button.addActionListener(this);
	      button.setActionCommand("deposit");
	      leftAlign(panel, button);
	      panel.add(new JLabel(" "));
	  }
	  
	  private void createWithdrawalBox(JPanel panel){
	      //JPanel panel = new JPanel();
	     // panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	      leftAlign(panel, new JLabel("Make a withdrawal:"));
	      panel.add(createRow(" Account number:                    ", withdrawalAccount));
	      panel.add(createRow(" Withdrawal amount:              ", withdrawalAmount));
	      panel.add(createRow(" Withdrawal serial number:  ", withdrawalSerial));
	      JButton button = new JButton("Withdraw");
	      button.addActionListener(this);
	      button.setActionCommand("withdrawal");
	      leftAlign(panel, button);
	      panel.add(new JLabel(" "));
	  }
	  
	  private void createTransferBox(JPanel panel){
	      //JPanel panel = new JPanel();
	      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	      leftAlign(panel, new JLabel("Transfer money between accounts:                           "));
	      panel.add(createRow(" Transfer from account: ", transferFromAccount));
	      panel.add(createRow(" Transfer to account:      ", transferToAccount));
	      panel.add(createRow(" Transfer amount:            ", transferAmount));
	      panel.add(createRow(" Transfer serial number: ", transferSerial));
	      JButton button = new JButton("Transfer");
	      button.addActionListener(this);
	      button.setActionCommand("transfer");
	      leftAlign(panel, button);
	      panel.add(new JLabel(" "));
	  }
	  
	  private void createSnapshotBox(JPanel panel){
		  leftAlign(panel, new JLabel("Create snapsot: "));
		  JButton button = new JButton("Snapshot");
		  button.addActionListener(this);
		  button.setActionCommand("snapshot");
		  leftAlign(panel, button);
		  panel.add(new JLabel(" "));
	  }
	  
	  private void createQueryBox(JPanel panel){
	      //JPanel panel = new JPanel();
	      //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	      leftAlign(panel, new JLabel("View account balance:"));
	      panel.add(createRow(" Account number:            ", queryAccount));
	      panel.add(createRow(" Query serial number:     ", querySerial));
	      JButton button = new JButton("Query");
	      button.addActionListener(this);
	      button.setActionCommand("query");
	      leftAlign(panel, button);
	      panel.add(new JLabel(" "));
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
		  panel.add(new JLabel(" "));
		  panel.add(new JLabel(" "));
		  panel.add(new JLabel(" "));
		  panel.add(new JLabel(" "));
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
	  
	  private boolean checkSerial(String input){
		  try {
			  int serial = Integer.parseInt(input);
			  if (serial < 0){
				  return false;
			  }
			  if (serialsSeen.containsKey(serial)){
				  return false;
			  }
			  serialsSeen.put(serial, "");
			  return true;
		  } catch (NumberFormatException e) {
			  System.out.println("checkSerial is returning false on input " + input);
			  return false;
		  }
	  }
	  
	public void handleDeposit(){
		//System.out.println("got deposit");
		//System.out.println("account number is: " + depositAccount.getText());
		//System.out.println("deposit amount is: " + depositAmount.getText());
		System.out.println("deposit serial is: " + depositSerial.getText());
		String account = depositAccount.getText();
		String amount = depositAmount.getText();
		String serial = depositSerial.getText();
		if (!checkAccountNumber(account)){
			result1.setText("Invalid account number format.");
			result2.setText("Example account number: 12.34567");
		} else if (!checkAmount(amount)) {
			result1.setText("Invalid deposit amount. Make sure you have entered a number");
			result2.setText("greater than 0 but less than 10,000,000.");
		} else if (!checkSerial(serial)){
			result1.setText("Invalid serial number.");
			result2.setText("");
		} else {
			//result1.setText("valid account number and amount");
			String branchNumber = account.substring(0, 2);
			int accountNumber = Integer.parseInt(account.substring(3, account.length()));
			float amountFloat = Float.parseFloat(amount);
			int serialNumber = Integer.parseInt(serial);
			DepositResponse response;
			try {
				System.out.println("passing in serial " + serialNumber);
				response = messaging.Deposit(branchNumber, new Integer(accountNumber), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
				if (response.GetSuccess()){
					result1.setText("Deposit successful");
					result2.setText("Balance: " + response.getBalance());
				} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
				}
			} catch (MessagingException e1) {
				result1.setText("A network error occurred");
				result2.setText("");
			}
			
		}
	}
	
	public void handleWithdrawal(){
		//System.out.println("got withdrawal");
	    //System.out.println("account number is: " + withdrawalAccount.getText());
	    //System.out.println("amount is: " + withdrawalAmount.getText());
		System.out.println("withdrawal serial is: " + withdrawalSerial.getText());
	    String account = withdrawalAccount.getText();
	    String amount = withdrawalAmount.getText();
	    String serial = withdrawalSerial.getText();
	    if (!checkAccountNumber(account)){
	    	result1.setText("Invalid account number format.");
	    	result2.setText("Example account number: 12.34567.");
	    } else if (!checkAmount(amount)) {
	    	result1.setText("Invalid withdrawal amount. Make sure you have entered a number");
	    	result2.setText("greater than 0 but less than 10,000,000.");
	    } else if (!checkSerial(serial)){
	    	result1.setText("Invalid serial number.");
	    	result2.setText("");
	    } else {
	    	//result1.setText("valid account number and amount");
	    	String branchNumber = account.substring(0, 2);
	    	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
	    	float amountFloat = Float.parseFloat(amount);
	    	int serialNumber = Integer.parseInt(serial);
	    	WithdrawResponse response;
	    	try {
	        	response = messaging.Withdraw(branchNumber, new Integer(accountNumber), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
	        	if (response.GetSuccess()){
					result1.setText("Withdrawal successful");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
	    	}
	    }
	}
	
	public void handleTransfer(){
	    //System.out.println("got transfer");
	    //System.out.println("from account number is: " + transferFromAccount.getText());
	    //System.out.println("to account number is: " + transferToAccount.getText());
	    //System.out.println("transfer amount is: " + transferAmount.getText());
		System.out.println("transfer serial is: " + transferSerial.getText());
	    String accountTo = transferToAccount.getText();
	    String accountFrom = transferFromAccount.getText();
	    String amount = transferAmount.getText();
	    String serial = transferSerial.getText();
	    if (!checkAccountNumber(accountTo)){
	    	result1.setText("Invalid account number format.");
	    	result2.setText("Example account number: 12.34567.");
	    } else if (!checkAccountNumber(accountFrom)){
	    	result1.setText("Invalid account number format.");
	    	result2.setText("Example account number: 12.34567.");
	    } else if (!checkAmount(amount)) {
	    	result1.setText("Invalid transfer amount. Make sure you have entered a number");
	    	result2.setText("greater than 0 but less than 10,000,000.");
	    } else if (!checkSerial(serial)){
	    	result1.setText("Invalid serial number.");
	    	result2.setText("");
	    } else {
	    	//result1.setText("valid account number and amount");
	    	String branchNumberTo = accountTo.substring(0, 2);
	    	int accountNumberTo = Integer.parseInt(accountTo.substring(3, accountTo.length()));
	    	String branchNumberFrom = accountFrom.substring(0, 2);
	    	int accountNumberFrom = Integer.parseInt(accountFrom.substring(3, accountFrom.length()));
	    	float amountFloat = Float.parseFloat(amount);
	    	int serialNumber = Integer.parseInt(serial);
	    	TransferResponse response;
	    	try {
	        	response = messaging.Transfer(branchNumberFrom, new Integer(accountNumberFrom), branchNumberTo, new Integer(accountNumberTo), new Float(amountFloat), new Integer((serialNumber*100) + clientNumber));
	        	if (response.GetSuccess()){
					result1.setText("Transfer successful");
					result2.setText("Balance in source account: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
	    	}
	    }
	}
	
	public void handleQuery(){
	    //System.out.println("got query");
	    //System.out.println("account number is: " + queryAccount.getText());
		System.out.println("query serial is: " + querySerial.getText());
	    String account = queryAccount.getText();
	    String serial = querySerial.getText();
	    if (!checkAccountNumber(account)){
	    	result1.setText("Invalid account number format.");
	    	result2.setText("Example account number: 12.34567.");
	    } else if (!checkSerial(serial)){
	    	result1.setText("Invalid serial number.");
	    	result2.setText("");
	    } else {
	    	//result1.setText("valid account number and amount");
	    	String branchNumber = account.substring(0, 2);
	    	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
	    	int serialNumber = Integer.parseInt(serial);
	    	QueryResponse response;
	    	try {
	        	response = messaging.Query(branchNumber, new Integer(accountNumber), Integer.parseInt((serialNumber*100) + clientNumber));
	        	if (response.GetSuccess()){
					result1.setText("Query successful.");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("A network error occurred");
				result2.setText("");
	    	}
	    }
	}
	
	private void lockGUI(){
		System.out.println("locking");
		waitingForResponse=true;
		result1.setText("Waiting for response.");
		result2.setText("Additional requests will not be processed.");
	}
	  
	@Override
	public void actionPerformed(ActionEvent e) {
	    String action = e.getActionCommand();
	    if (!waitingForResponse){
	    	lockGUI();
		    if (action.equals("deposit")){
		    	handleDeposit();
		    } else if (action.equals("withdrawal")){
		        handleWithdrawal();
		    } else if (action.equals("transfer")){
		    	handleTransfer();
		    } else if (action.equals("query")){
		    	handleQuery();
		    } else {
		        System.out.println("Invalid action type received from GUI");
		    }
	    } else {
	    	System.out.println("Request ignored because another request is pending");
	    }
	    waitingForResponse=false;
	}
	
	
	public static void main(String[] args){
                String clientNum = null;
		
		try {
                        clientNum = args[0];
                        Integer.parseInt(clientNum);
		} catch (Exception e){
			System.out.println("Please run the program with the client number as the first argument.");
			System.exit(0);
		}
		
		if ((Integer.parseInt(clientNum) < 0 || Integer.parseInt(clientNum) > 99)){
			System.out.println("Please enter a client number between 0 and 99.");
			System.exit(0);
		}
		
		Client client = new Client(clientNum);
		
		try {
			client.messaging = new Messaging(clientNum, null);
			client.messaging.connectToServer(client.new OracleCallback());
			client.messaging.initializeOracleAddress();
		} catch (MessagingException e) {
			System.out.println("Could not create socket");
		}
		
	    try {
			client.topology = Messaging.buildTopologyHelper("topology.txt");
			client.resolver = Messaging.buildResolverHelper("resolver.txt");
			client.replicaStates = Oracle.buildReplicaStates(client.resolver);
			client.branchReplicas = client.buildBranchReplicas();
			client.initializePrimary();
		} catch (MessagingException e) {
			System.out.println("reading files failed in client");
			e.printStackTrace();
			return;
		}
		
		//client.messaging.branch = clientNum;
	}

}
