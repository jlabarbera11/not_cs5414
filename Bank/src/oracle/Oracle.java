package oracle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
	
	public enum replicaState {running, failed}

    private JTextField failureServerNumber = new JTextField(16);
    private JTextField recoveryServerNumber = new JTextField(16);
    private JLabel result1 = new JLabel(" ");
    private JLabel result2 = new JLabel(" ");
    private ConcurrentHashMap<Integer, Set<Integer>> topology;
    private ConcurrentHashMap<String, String[]> resolver;
    private ConcurrentHashMap<String, replicaState> replicaStates;
    Messaging messaging;
    
    public String GetResult(){
    	return result1.getText();
    }
    
    //public String SetText
    
    public Oracle() {
    	super("Oracle GUI");
	    
	    try {
			topology = Messaging.buildTopologyHelper("topology.txt");
			resolver = Messaging.buildResolverHelper("resolver.txt");
			replicaStates = buildReplicaStates(this.resolver);
		} catch (MessagingException e) {
			System.out.println("reading files failed in oracle");
			e.printStackTrace();
			return;
		}
	    
		try {
			messaging = new Messaging(null, null);
			//messaging.connectToServer(new ClientSnapshot());  //FIXME
			//messaging.makeConnections(); //is this right?
			messaging.OracleAcceptClientConnections();
			messaging.OracleConnectToAllReplicas();
		} catch (MessagingException e) {
			System.out.println("Failure initializeing oracle");
		}
	    
	    //create gui
	    setSize(400, 350);
	    JPanel mainPanel = new JPanel();
	    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
	    mainPanel.setLayout(layout);
	    createFailureBox(mainPanel);
	    createRecoveryBox(mainPanel);
	    createResultBox(mainPanel);
	    getContentPane().add(mainPanel);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true);
    }
    
    public static ConcurrentHashMap<String, replicaState> buildReplicaStates(Map<String, String[]> resolver) {
    	ConcurrentHashMap<String, replicaState> replicaStates = new ConcurrentHashMap<String, replicaState>();
    	for (Map.Entry<String, String[]> entry : resolver.entrySet())
    	{
    	    replicaStates.put(entry.getKey(), replicaState.running);
    	}
		return replicaStates;
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
	  
    private void createRecoveryBox(JPanel panel){
    	JLabel title = new JLabel("Report a processor recovery:");
	    title.setAlignmentY(Component.LEFT_ALIGNMENT);
	    leftAlign(panel, title);
	    panel.add(createRow(" Processor number:             ", recoveryServerNumber));
	    JButton button = new JButton("Report");
	    button.addActionListener(this);
	    button.setActionCommand("recovery");
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
	
	//starting backing up - send backupOracle
	//failed - send failureOracle
	
	
	public void handleFailure(){
		System.out.println("got failure");
		String failureText = failureServerNumber.getText();
		if (checkProcessorNumber(failureText) && replicaStates.containsKey(failureText)){
			System.out.println("got failure from branch replica " + failureText);
			String replica = failureText;
			replicaStates.put(replica, replicaState.failed);
			
			try {
				messaging.OracleRemoveReplicaStreams(replica);
				Message m = new FailureOracle(replica);
				messaging.OracleBroadcastMessage(m);
		    	result1.setText("Failure recorded for replica " + failureText);
		    	result2.setText("");
			} catch (MessagingException e) {
				//e.printStackTrace();
		    	result1.setText("Replica could not be found.");
		    	result2.setText("Please make sure you have entered a valid replica number.");
			}

		} else {
			System.out.println("invalid processor number");
	    	result1.setText("Invalid processor number.");
	    	result2.setText("Example processor number: 12.34");
		}
	}
	
	public void handleRecovery(){
		System.out.println("got recovery");
		String recoveryText = recoveryServerNumber.getText();
		if (checkProcessorNumber(recoveryText) && replicaStates.containsKey(recoveryText)){
			System.out.println("got recover from branch replica " + recoveryText);
			String replica = recoveryText;
			replicaStates.put(replica, replicaState.running);
			
			try {
				messaging.OracleConnectToReplica(replica);
				Message m = new BackupOracle(replica);
				messaging.OracleBroadcastMessage(m);
		    	result1.setText("Recovery recorded for replica " + recoveryText);
		    	result2.setText("");
			} catch (MessagingException e) {
				//e.printStackTrace();
		    	result1.setText("Replica could not be found.");
		    	result2.setText("Please make sure you have entered a valid replica number.");
			}
			
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
		    	handleFailure();
		    } else if (action.equals("recovery")){
		        handleRecovery();
		    } else {
		        System.out.println("Invalid action type received from GUI");
		    }
	}
	
	
	public static void main(String[] args){
		Oracle oracle = new Oracle();
		//client.messaging.branch = clientNum;
	}

}
