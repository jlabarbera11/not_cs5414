package oracle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

//import client.ClientSnapshot;

import messaging.*;

//TODO: server must return error if it gets duplicate serial!
//TODO: enter a serial for snapshot?

public class Oracle extends JFrame implements ActionListener {
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public enum replicaState {running, failed}
/**
    private JTextField failureServerNumber = new JTextField(16);
    private JTextField recoveryServerNumber = new JTextField(16);
    private JLabel result1 = new JLabel(" ");
    private JLabel result2 = new JLabel(" ");
    NewMessaging newMessaging;
    
    public String GetResult(){
    	return result1.getText();
    }

    public Oracle() {
    	super("Oracle GUI");
	    
    	newMessaging = new NewMessaging();
    	
    	OracleThread thread = new OracleThread(newMessaging);
    	thread.start();
	    
	    //create gui
	    setSize(400, 350);
	    JPanel mainPanel = new JPanel();
	    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
	    mainPanel.setLayout(layout);
	    createFailureBox(mainPanel);
	    createResultBox(mainPanel);
	    getContentPane().add(mainPanel);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true);
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
	  
    private void createFailureBox(JPanel panel){
    	JLabel title = new JLabel("Report a processor failure:");
    	title.setAlignmentY(Component.LEFT_ALIGNMENT);
	    leftAlign(panel, title);
	    panel.add(createRow(" Processor number:             ", failureServerNumber));
	    JButton button = new JButton("Report");
	    button.addActionListener(this);
	    button.setActionCommand("failure");
	    leftAlign(panel, button);
	    panel.add(new JLabel(" "));
	  }
	  
	private void leftAlign(JPanel panel, JComponent component){
		JPanel newPanel = new JPanel(new BorderLayout());
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		newPanel.add(component);
		panel.add(newPanel);
		  
	}
	  
	private JPanel createRow(String title, JTextField field){
		  JPanel topRowPanel = new JPanel();
		  topRowPanel.setLayout(new BoxLayout(topRowPanel, BoxLayout.X_AXIS));
		  topRowPanel.add(new JLabel(title));
		  topRowPanel.add(field);
		  return topRowPanel;
	}
	  
	private boolean checkProcessorNumber(String input){
		if (input.length() != 5) { return false; }
		if (input.charAt(2) != '.') {return false; }
		try {
			float branch = Float.parseFloat(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public void handleFailure() throws MessagingException{
		System.out.println("got failure");
		String failureText = failureServerNumber.getText();
		if (checkProcessorNumber(failureText)){
			System.out.println("got failure from branch replica " + failureText);
			String replica = failureText;
			ReplicaID replicaID = new ReplicaID(Integer.parseInt(failureText.substring(0, 2)), Integer.parseInt(failureText.substring(3,5)));
			//Message m = new FailureOracle(replica);
			newMessaging.recordJvmFailure(replicaID, Oracle.replicaState.failed);
			result1.setText("Failure recorded for replica " + failureText);
			result2.setText("");

		} else {
			System.out.println("invalid processor number");
	    	result1.setText("Invalid processor number.");
	    	result2.setText("Example processor number: 12.34");
		}
	}
	  
	@Override
	public void actionPerformed(ActionEvent e) {
	    String action = e.getActionCommand();
	    if (action.equals("failure")){
	    	try {
				handleFailure();
			} catch (MessagingException e1) {
				e1.printStackTrace();
			}
	    } else {
	        System.out.println("Invalid action type received from GUI");
	    }
	}
	
	
	public static void main(String[] args){
		Oracle oracle = new Oracle();
		//client.messaging.branch = clientNum;
	}
*/


}
