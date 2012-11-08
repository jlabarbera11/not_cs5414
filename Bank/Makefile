all: client server oracle messaging
jar: client-jar server-jar oracle-jar

client:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/client/*.java
	
client-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/client/test/ClientTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore client.test.ClientTest

server:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/server/*.java

oracle:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/oracle/*.java

messaging:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java

messaging-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/messaging/test/MessagingTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore messaging.test.MessagingTest

client-jar: client messaging
	jar cfe client.jar client.Client -C bin .

server-jar: server messaging
	jar cfe server.jar server.Server -C bin .

oracle-jar: server messaging
	jar cfe oracle.jar oracle.Oracle -C bin .


clean:
	rm -rf bin/*
