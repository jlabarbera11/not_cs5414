package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {
    
    JTextField depositAccount = new JTextField(16);
    JTextField depositAmount = new JTextField(16);
    JTextField withdrawalAccount = new JTextField(16);
    JTextField withdrawalAmount = new JTextField(16);
    JTextField transferFromAccount = new JTextField(16);
    JTextField transferToAccount = new JTextField(16);
    JTextField transferAmount = new JTextField(16);
    JTextField queryAccount = new JTextField(16);
    
  public Client() {
    super("Bank GUI");
    setSize(300, 410);
    this.setResizable(false);
    JPanel mainPanel = new JPanel();
    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(layout);
    JPanel depositBox = createDepositBox();
    JPanel withdrawalBox = createWithdrawalBox();
    JPanel transferBox = createTransferBox();
    JPanel queryBox = createQueryBox();
    //layout.putConstraint(SpringLayout.SOUTH, depositBox, 5, SpringLayout.NORTH, withdrawalBox);
    
    mainPanel.add(depositBox);
    mainPanel.add(withdrawalBox);
    mainPanel.add(transferBox);
    mainPanel.add(queryBox);
    getContentPane().add(mainPanel);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setVisible(true);
  }
  
  private JPanel createDepositBox(){
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(new JLabel("Make a deposit:"));
      panel.add(createRow("Account number: ", depositAccount));
      panel.add(createRow("Deposit amount:  ", depositAmount));
      JButton button = new JButton("Deposit");
      button.addActionListener(this);
      button.setActionCommand("deposit");
      panel.add(button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createWithdrawalBox(){
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(new JLabel("Make a withdrawal:"));
      panel.add(createRow("Account number:       ", withdrawalAccount));
      panel.add(createRow("Withdrawal amount: ", withdrawalAmount));
      JButton button = new JButton("Withdraw");
      button.addActionListener(this);
      button.setActionCommand("withdrawal");
      panel.add(button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createTransferBox(){
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(new JLabel("Transfer money between accounts:"));
      panel.add(createRow("Transfer from account: ", transferFromAccount));
      panel.add(createRow("Transfer to account:      ", transferToAccount));
      panel.add(createRow("Transfer amount:            ", transferAmount));
      JButton button = new JButton("Transfer");
      button.addActionListener(this);
      button.setActionCommand("transfer");
      panel.add(button);
      panel.add(new JLabel(" "));
      return panel;
  }
  
  private JPanel createQueryBox(){
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(new JLabel("View account balance:"));
      panel.add(createRow("Account number: ", queryAccount));
      JButton button = new JButton("Query");
      button.addActionListener(this);
      button.setActionCommand("query");
      panel.add(button);
      panel.add(new JLabel(" "));
      return panel;
  }

  private JPanel createRow(String title, JTextField field){
    JPanel topRowPanel = new JPanel();
    topRowPanel.setLayout(new BoxLayout(topRowPanel, BoxLayout.X_AXIS));
    topRowPanel.add(new JLabel(title));
    topRowPanel.add(field);
    return topRowPanel;
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

public static void main(String[] argv)
{
//For thread safety this should be utilized in the dispatch thread
  javax.swing.SwingUtilities.invokeLater(new Runnable()
  { // Anonymous class
    public void run()
    {
      Client example = new Client();
    }
  });
}

}