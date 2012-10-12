package client;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import server.BankAccount;
import server.Channel;
import server.SSInfo;
import server.Server;
import messaging.*;

/**
 * 
 *  private Map<Integer, Channel> channels;
    private Set<BankAccount> branchState;

    public Snapshot(SSInfo ss) 
    {
        channels = ss.getChannels();
        branchState = ss.getBranchState();
    }
 *
 */

public class ClientSnapshot extends JFrame implements Callback {
    public void callback(Message message) {
	    System.out.println("displaying callback");
	    SnapshotResponse response = (SnapshotResponse)message;
	    Snapshot snapshot = response.getSnapshot();
	    JFrame newFrame = new JFrame();
	    String[][] rows = new String[snapshot.branchState.size()][2];
	    System.out.println("branch state size is: " + snapshot.branchState.size());
	    String[] column_names = {"Account Number", "Balance:"};
	    Iterator<List<Object>> iterator = snapshot.branchState.iterator();
	    int i=0;
	    while(iterator.hasNext()){
	    	List<Object> account = iterator.next();
	    	rows[i][0]= (String)account.get(0)+"."+(String)account.get(1);
	    	rows[i][1]= (String)account.get(2);
	    	i++;
	    }
	    JTable table = new JTable(rows, column_names);
	    JScrollPane scrollPane = new JScrollPane(table);
	    newFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
	  
	    newFrame.pack();
	    newFrame.setVisible(true);
	    
	    ClientSnapshot object = new ClientSnapshot(snapshot);
    }

        
  public ClientSnapshot(Snapshot snapshot) {
    super("Displaying snapshot information");
    setSize(400, 700);
    //this.setResizable(false);
    JPanel mainPanel = new JPanel();
    BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(layout);
    mainPanel.add(makeAnotherWindow(snapshot));
    //mainPanel.add(withdrawalBox);
    //mainPanel.add(transferBox);
    //mainPanel.add(queryBox);
    getContentPane().add(mainPanel);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setVisible(true);
  }

	public ClientSnapshot() {
	// TODO Auto-generated constructor stub
}


	private static JPanel makeAnotherWindow(Snapshot snapshot) {
		
		//JFrame newFrame = new JFrame();
		//newFrame.getContentPane().add(new JLabel("Showing messages in the channel"), BorderLayout.CENTER);
		//newFrame.getContentPane().add(new JLabel("text"), BorderLayout.CENTER);
		
		
		
	    //private Map<Integer, Channel> channels;
	    JPanel panel = new JPanel();
	    
	    panel.add(new JLabel("Messages in channel"));
	    
	    for (Map.Entry<Integer, List<Message>> entry : snapshot.channels.entrySet()) {
	    	panel.add(new JLabel("Messages in channel from branch " + entry.getKey()));
	    	panel.add(new JLabel(entry.getValue().toString()));
	    	
	    	//newFrame.getContentPane().add(new JLabel("Messages in channel from branch " + entry.getKey()), BorderLayout.CENTER);
	    	//Channel channel = entry.getValue();
	    	List<Message> messages = entry.getValue(); //channel.getMessages();
	    	for (Message m: messages){
	    		panel.add(new JLabel(m.toString()), BorderLayout.CENTER);
	    	}
	    }
		
	    
		  
	    //newFrame.pack();
	    //newFrame.setVisible(true);
		return panel;
	}
    
	public static void main(String[] args){
		//makeAnotherWindow();
		
	}
    
}









