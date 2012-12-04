Tutorial:

1. Unzip out submission to a directory named BigRedBank.
2. Open a command prompt. Change to the BigRedBank directory you just created.
3. To make the jvm, enter the following commands:
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/jvm/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/server/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java
        > jar cfe jars/jvm.jar jvm.JVM -C bin .
4. To make the client, enter the following commands:
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/client/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java
        > jar cfe jars/client.jar client.Client -C bin .
6. Enter the following command:
        > LAUNCH.cmd
7. Try some test deposits, withdrawals, transfers, and queries.
   Use CRTL-C to kill a processor


Names and Descriptions of Files:

The client directory contains files to create and run the GUI. 

The messaging directory contains the messaging class, several helper classes,
such as a callback, and wrappers for Message objects. Additionally, there is a
test directory to test some of the checks performed at the Messaging level.

The server directory contains files for running the server.

The jvm directory contains files for running and testing the jvm.

Phase IV was based on our own Phase III solution.
