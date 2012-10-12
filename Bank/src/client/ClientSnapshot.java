package client;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import server.BankAccount;
import server.Channel;
import server.SSInfo;
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

public class ClientSnapshot implements Callback {
    public void callback(Message message) {
	    System.out.println("displaying callback");
	    SnapshotResponse response = (SnapshotResponse)message;
	    Snapshot snapshot = response.getSnapshot();
	    JFrame newFrame = new JFrame();
	    String[][] rows = new String[snapshot.branchState.size()][2];
	    String[] column_names = {"Account Number", "Balance:"};
	    Iterator<BankAccount> iterator = snapshot.branchState.iterator();
	    int i=0;
	    while(iterator.hasNext()){
	    	BankAccount account = iterator.next();
	    	rows[i][0]=""+account.accountNumber;
	    	rows[i][1]=""+account.getBalance();
	    	i++;
	    }
	    JTable table = new JTable(rows, column_names);
	    JScrollPane scrollPane = new JScrollPane(table);
	    newFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
	  
	    newFrame.pack();
	    newFrame.setVisible(true);
	    
    }
}









