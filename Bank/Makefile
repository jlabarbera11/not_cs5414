all: messaging
test: messaging-test

messaging:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java

messaging-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/messaging/test/MessagingTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore messaging.test.MessagingTest

clean:
	rm -rf bin/messaging
