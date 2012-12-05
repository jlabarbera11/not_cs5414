package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

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
    int clientNumber;
    public Messaging messaging;
    boolean waitingForResponse;
    int number=0;
    private Map<Integer, String> serialsSeen;
    
    public boolean debug = false;
    
    public void printIfDebug(String s){
    	if (debug){
    		System.out.println(s);
    	}
    }

    public String GetResult(){
    	return result1.getText();
    }

    private Message receiveMessage() throws IOException, ClassNotFoundException{
    	ReplicaInfo myInfo = messaging.getClientInfo(clientNumber);
    	ServerSocket serversocket = new ServerSocket(myInfo.port);
    	serversocket.setReuseAddress(true);
    	printIfDebug("client listening on port " + myInfo.port);
        Socket clientSocket = serversocket.accept();
        printIfDebug("client accepted connection");
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        Message message = (Message)ois.readObject();
        printIfDebug("read object");
        serversocket.close();
        return message;

    }

    private ResponseClient sendRequest(RequestClient message, int branchNumber) throws MessagingException {
    	ResponseClient result = null;
    	for (int i=0; i<3; i++){
    		try {
    			messaging.sendToPrimaryNoResponse(branchNumber, message);
    			result = (ResponseClient)receiveMessage();
    			return result;
    		} catch (Exception e){
    			printIfDebug("send " + i + " failed");
    			e.printStackTrace();
    		}
    	}
    	printIfDebug("send failed in sendRequest (3 timeouts)");
    	throw new MessagingException(MessagingException.Type.FAILED_MESSAGE_SEND);
    }

	private DepositResponse sendDeposit(int branchNumber, int accountNumber, float amountFloat, int serialNum) throws MessagingException {
		DepositRequest dr = new DepositRequest(accountNumber, amountFloat, serialNum);
		return (DepositResponse)sendRequest(dr, branchNumber);
	}

	private WithdrawResponse sendWithdrawal(int branchNumber, int accountNumber, float amountFloat, int serialNumber) throws MessagingException {
		WithdrawRequest wr = new WithdrawRequest(accountNumber, amountFloat, serialNumber);
		return (WithdrawResponse)sendRequest(wr, branchNumber);
	}

	private TransferResponse sendTransfer(int branchNumberFrom, int accountNumberFrom, int branchNumberTo, int accountNumberTo, float amountFloat, int serialNum) throws MessagingException {
		TransferRequest tr = new TransferRequest(branchNumberTo, accountNumberFrom, accountNumberTo, amountFloat, serialNum);
		return (TransferResponse)sendRequest(tr, branchNumberFrom);
	}

	private QueryResponse sendQuery(int branchNumber, int accountNumber, int serialNumber) throws MessagingException {
		QueryRequest qr = new QueryRequest(accountNumber, serialNumber);
		return (QueryResponse)sendRequest(qr, branchNumber);
	}

	  public Client(int clientNum) {
	    super("Bank GUI for Branch " + clientNum);
	    this.clientNumber = clientNum;
	    setSize(400, 700);
            this.serialsSeen = new HashMap<Integer, String>();
	    JPanel mainPanel = new JPanel();
	    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
	    mainPanel.setLayout(layout);
	    createDepositBox(mainPanel);
	    createWithdrawalBox(mainPanel);
	    createTransferBox(mainPanel);
	    createQueryBox(mainPanel);
	    createSnapshotBox(mainPanel);
	    createResultBox(mainPanel);
	    getContentPane().add(mainPanel);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true);
	    waitingForResponse=false;
	    messaging = new Messaging();
	    System.out.println("Client started");
	  }

	  private void createDepositBox(JPanel panel){
	      JLabel title = new JLabel("Make a deposit:");
	      title.setAlignmentY(Component.LEFT_ALIGNMENT);
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
			  printIfDebug("checkSerial is returning false on input " + input);
			  return false;
		  }
	  }

	public void handleDeposit(){
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
			int branchNumber = Integer.parseInt(account.substring(0, 2));
			if (branchNumber != clientNumber){
				printIfDebug("got invalid client number");
				result1.setText("Can only deposit to this branch.");
				result2.setText("");
				return;
			}
			int accountNumber = Integer.parseInt(account.substring(3, account.length()));
			float amountFloat = Float.parseFloat(amount);
			int serialNumber = Integer.parseInt(serial);
			DepositResponse response;
			try {
				printIfDebug("passing in serial " + serialNumber);
				response = sendDeposit(branchNumber, accountNumber, amountFloat, serialNumber*100 + clientNumber);
				if (response.GetSuccess()){
					result1.setText("Deposit successful");
					result2.setText("Balance: " + response.getBalance());
				} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
				}
			} catch (MessagingException e1) {
				result1.setText("An error occurred");
				result2.setText("");
			}

		}
	}

	public void handleWithdrawal(){
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
	    	int branchNumber = Integer.parseInt(account.substring(0, 2));
			if (branchNumber != clientNumber){
				result1.setText("Can only withdraw from this branch.");
				result2.setText("");
				return;
			}
	    	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
	    	float amountFloat = Float.parseFloat(amount);
	    	int serialNumber = Integer.parseInt(serial);
	    	WithdrawResponse response;
	    	try {
	    		response = sendWithdrawal(branchNumber, accountNumber, amountFloat, serialNumber*100+clientNumber);
	        	if (response.GetSuccess()){
					result1.setText("Withdrawal successful");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("An error occurred");
				result2.setText("");
	    	}
	    }
	}

	public void handleTransfer(){
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
	    	int branchNumberTo = Integer.parseInt(accountTo.substring(0, 2));
	    	int accountNumberTo = Integer.parseInt(accountTo.substring(3, accountTo.length()));
	    	int branchNumberFrom = Integer.parseInt(accountFrom.substring(0, 2));
			if (branchNumberFrom != clientNumber){
				result1.setText("Can only transfer from this branch.");
				result2.setText("");
				return;
			}
	    	int accountNumberFrom = Integer.parseInt(accountFrom.substring(3, accountFrom.length()));
	    	float amountFloat = Float.parseFloat(amount);
	    	int serialNumber = Integer.parseInt(serial);
	    	TransferResponse response;
	    	try {
	        	response = sendTransfer(branchNumberFrom, accountNumberFrom, branchNumberTo, accountNumberTo, amountFloat, serialNumber*100 + clientNumber);
	        	printIfDebug("response is " + response);
                        if (response.GetSuccess()){
					result1.setText("Transfer successful");
					result2.setText("Balance in source account: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("An error occurred");
				result2.setText("");
	    	}
	    }
	}

	public void handleQuery(){
	    String account = queryAccount.getText();
	    String serial = querySerial.getText();
	    if (!checkAccountNumber(account)){
	    	result1.setText("Invalid account number format.");
	    	result2.setText("Example account number: 12.34567.");
	    } else if (!checkSerial(serial)){
	    	result1.setText("Invalid serial number.");
	    	result2.setText("");
	    } else {
	    	int branchNumber = Integer.parseInt(account.substring(0, 2));
			if (branchNumber != clientNumber){
				result1.setText("Can only query accounts in this branch.");
				result2.setText("");
				return;
			}
	    	int accountNumber = Integer.parseInt(account.substring(3, account.length()));
	    	int serialNumber = Integer.parseInt(serial);
	    	QueryResponse response;
	    	try {
	    		response = sendQuery(branchNumber, accountNumber, serialNumber+100 + clientNumber);
	        	if (response.GetSuccess()){
					result1.setText("Query successful.");
					result2.setText("Balance: " + response.getBalance());
	        	} else {
					result1.setText(response.GetFailureReason());
					result2.setText("");
	        	}
	    	} catch (MessagingException e2){
				result1.setText("An error occurred");
				result2.setText("");
	    	}
	    }
	}

	private void lockGUI(){
		printIfDebug("locking");
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
		        printIfDebug("Invalid action type received from GUI");
		    }
	    } else {
	    	printIfDebug("Request ignored because another request is pending");
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

		Client client = new Client(Integer.parseInt(clientNum));

	}

}
