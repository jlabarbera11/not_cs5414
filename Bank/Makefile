all: client server messaging

client:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/client/*.java

server:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/server/*.java

messaging:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java

messaging-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/messaging/test/MessagingTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore messaging.test.MessagingTest

jar-server: server messaging
	jar cfe server.jar server.Server -C bin .

jar-client: client messaging
	jar cfe client.jar client.Client -C bin .

clean:
	rm -rf bin*
