package server;

import java.util.List;
import java.util.LinkedList;

import messaging.Message;

public class Channel
{
    private boolean isOpen;
    private List<Message> messages;

    public Channel()
    {
        isOpen = true;
        messages = new LinkedList<Message>();
    }

    public void closeChannel()
    {
        isOpen = false;
    }

    public boolean isOpen()
    {
        return isOpen;
    }

    public boolean add(Message m)
    {
        if (isOpen)
            messages.add(m);

        return !isOpen;
    }
}