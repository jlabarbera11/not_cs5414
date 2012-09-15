package messaging;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class Messaging {

    private String account;
    private Map<String, Set<String>> topology;

    public void Messaging(String new_acnt) {
        account = new_acnt;

        topology = new HashMap<String, Set<String>>();
        Scanner scanner = new Scanner("topology.txt");
        while (scanner.hasNextLine()) {
            String[] a = scanner.nextLine().split(" ");
            if (topology.containsKey(a[0])) {
                topology.get(a[0]).add(a[1]);
            }
            else {
                Set<String> s = new HashSet<String>();
                s.add(a[1]);
                topology.put(a[0], s);
            }
        }
    }

    public void Deposit(String acnt, float amt, float ser_number) {
    }


}
