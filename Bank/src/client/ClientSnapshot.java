package client;

import messaging.Callback;

public class ClientSnapshot implements Callback {
    public void callback() {
        System.out.println("something");
    }
}

