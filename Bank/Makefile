all: jvm client server messaging
jar: jvm-jar client-jar

jvm:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/jvm/*.java

jvm-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/jvm/test/JVMTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore jvm.test.JVMTest

client:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/client/*.java

client-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/client/test/ClientTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore client.test.ClientTest

server:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/server/*.java

messaging:
	javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java

messaging-test:
	javac -Xlint:unchecked -sourcepath src/ -cp lib/junit-4.10.jar -d bin src/messaging/test/MessagingTest.java
	java -cp lib/junit-4.10.jar:bin/ org.junit.runner.JUnitCore messaging.test.MessagingTest

jvm-jar: jvm server messaging
	jar cfe jars/jvm.jar jvm.JVM -C bin .

client-jar: client messaging
	jar cfe jars/client.jar client.Client -C bin .

clean:
	rm -rf bin/*
	rm -rf jars/*
