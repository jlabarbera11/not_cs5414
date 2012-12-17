package jvm.test;

import org.junit.*;
import org.junit.Assert.*;

import jvm.*;
import messaging.*;
import messaging.Messaging.replicaState;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JVMTest {

  @Test public void testVoting() throws Exception {

    JVM jvm1 = new JVM(1);
    JVM jvm2 = new JVM(2);
    JVM jvm3 = new JVM(3);
    JVM jvm4 = new JVM(4);
    JVM jvm5 = new JVM(5);

    jvm1.start();
    jvm2.start();
    jvm3.start();
    jvm4.start();
    jvm5.start();

    Thread.sleep(3000); /*Sleep to let everything start*/

    for(int i=1; i<=5; i++) {
      for(int j=1; j<=5; j++) {
        Socket socket = new Socket("localhost", 7770+i);
        new ObjectOutputStream(socket.getOutputStream()).writeObject(new StatusQuery(j));
        StatusQueryResponse sqr = (StatusQueryResponse)(new ObjectInputStream(socket.getInputStream())).readObject();
        Assert.assertEquals(replicaState.running, sqr.status);
        socket.close();
      }
    }

    jvm1.kill();
    Thread.sleep(10000); /*Let timestamps expire*/
    for(int i=2; i<=5; i++) {
      Socket socket = new Socket("localhost", 7770+i);
      new ObjectOutputStream(socket.getOutputStream()).writeObject(new StatusQuery(1));
      StatusQueryResponse sqr = (StatusQueryResponse)(new ObjectInputStream(socket.getInputStream())).readObject();
      Assert.assertEquals(replicaState.failed, sqr.status);
      socket.close();
    }
  }
}
