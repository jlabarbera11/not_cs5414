package client;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;


public class GUI extends JFrame implements ActionListener{
    
    JTextField depositAccount;
    JTextField depositAmount;
    JTextField withdrawalAccount;
    JTextField withdrawalAmount;
    JTextField transferFromAccount;
    JTextField transferToAccount;
    JTextField transferAmount;
    JTextField queryAccount;
	
	public GUI() {
	    setTitle("Bank GUI");
	    setSize(1000,2000);
	    setLocation(100,200);
	}
	
	private void createGUI(){
	       JFrame frame = new GUI();
	        frame.setVisible(true);
	        frame.setSize(320, 200);
	        //Container content = frame.getContentPane();
	        //content.setBackground(Color.white);
	        //content.setLayout(new FlowLayout()); 
	        
	        //JButton withdrawalButton = new JButton("Withdraw");
	        //JButton transferButton = new JButton("Transfer");
	        
	        //content.add(withdrawalButton);
	        //content.add(transferButton);
	        

	        //withdrawalButton.addActionListener(new WithdrawalListener());
	        //transferButton.addActionListener(new TransferListener());
	        
	        //JPanel panel = new JPanel(new SpringLayout());
	        //frame.add(panel);
	        SpringLayout containerLayout = new SpringLayout();
	        JPanel container = new JPanel(containerLayout);
	        
	        SpringLayout depositLayout = new SpringLayout();
	        //SpringLayout.Constraints depositConstraint = new SpringLayout.Constraints();
	        //depositConstraint.setWidth(Spring.constant(200));
	        
	        
	        
	        JPanel depositPanel = new JPanel();
	        depositPanel.setLayout(depositLayout);
	        
	        JLabel depositAccountLabel = new JLabel("Account number:");
	        depositPanel.add(depositAccountLabel);
	        JLabel depositAmountLabel = new JLabel("Deposit amount:");
            depositPanel.add(depositAmountLabel);
	        
	        depositAccount = new JTextField(16);
	        depositPanel.add(depositAccount);
	        depositAmount = new JTextField(16);
            depositPanel.add(depositAmount);
            JButton depositButton = new JButton("Deposit");
            //depositPanel.add(depositButton);
            depositButton.addActionListener(this);
            depositButton.setActionCommand("deposit");
            depositLayout.putConstraint(SpringLayout.WEST, depositAccountLabel, 5, SpringLayout.WEST, depositPanel);
            depositLayout.putConstraint(SpringLayout.NORTH, depositAccountLabel, 5, SpringLayout.NORTH, depositPanel);
            depositLayout.putConstraint(SpringLayout.WEST, depositAccount, 5, SpringLayout.EAST, depositAccountLabel);
            depositLayout.putConstraint(SpringLayout.NORTH, depositAccount, 5, SpringLayout.NORTH, depositPanel);
            depositLayout.putConstraint(SpringLayout.WEST, depositAmountLabel, 5, SpringLayout.WEST, depositPanel);
            depositLayout.putConstraint(SpringLayout.NORTH, depositAmount, 5, SpringLayout.SOUTH, depositAccount);
            depositLayout.putConstraint(SpringLayout.NORTH, depositAmountLabel, 10, SpringLayout.SOUTH, depositAccountLabel);
            depositLayout.putConstraint(SpringLayout.WEST, depositAmount, 10, SpringLayout.EAST, depositAmountLabel);
            
            //container.add(depositPanel);\
            //container.add(depositPanel);
            containerLayout.addLayoutComponent("string", depositPanel);
            JComponent component = container;
            //component.setOpaque(true); //content panes must be opaque
            component.setSize(200, 200);
            frame.setContentPane(container);
            //container.setSize(200, 200);
            //frame.setContentPane(container);
            
            
//            withdrawalAccount = new JTextField("Account number", 16);
//            frame.add(withdrawalAccount);
//            withdrawalAmount = new JTextField("Withdrawal amount", 16);
//            frame.add(withdrawalAmount);
//            JButton withdrawalButton = new JButton("Withdrawal");
//            content.add(withdrawalButton);
//            withdrawalButton.addActionListener(this);
//            withdrawalButton.setActionCommand("withdrawal");
//            
//            transferFromAccount = new JTextField("Transfer from account number", 16);
//            frame.add(transferFromAccount);
//            transferToAccount = new JTextField("Transfer to account number", 16);
//            frame.add(transferToAccount);
//            transferAmount = new JTextField("Transfer amount", 16);
//            frame.add(transferAmount);
//            JButton transferButton = new JButton("Transfer");
//            content.add(transferButton);
//            transferButton.addActionListener(this);
//            transferButton.setActionCommand("transfer");
//            
//            queryAccount = new JTextField("Account number", 16);
//            frame.add(queryAccount);
//            JButton queryButton = new JButton("See account balance");
//            content.add(queryButton);
//            queryButton.addActionListener(this);
//            queryButton.setActionCommand("query");
            
	        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
	    GUI gui = new GUI();
	    gui.createGUI();
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("deposit")){
            System.out.println("got deposit");
            System.out.println("account number is: " + depositAccount.getText());
            System.out.println("deposit amount is: " + depositAmount.getText());
        } else if (action.equals("withdrawal")){
            System.out.println("got withdrawal");
            System.out.println("account number is: " + withdrawalAccount.getText());
            System.out.println("amount is: " + withdrawalAmount.getText());
        } else if (action.equals("transfer")){
            System.out.println("got transfer");
            System.out.println("from account number is: " + transferFromAccount.getText());
            System.out.println("to account number is: " + transferToAccount.getText());
            System.out.println("transfer amount is: " + transferAmount.getText());
        } else if (action.equals("query")){
            System.out.println("got query");
            System.out.println("account number is: " + queryAccount.getText());
        } else {
            System.out.println("Invalid action type received from GUI");
        }
        
        
        
    }
	
	
	
	
	
}
