Tutorial:


1. Unzip out submission to a directory named BigRedBank.
2. Open a command prompt. Change to the BigRedBank directory you just created.
3. To make the server, enter the following commands:
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/server/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java
        > jar cfe server.jar server.Server -C bin .
4. To make the client, enter the following commands:
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/client/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java
        > jar cfe client.jar client.Client -C bin .
5. To make the oracle, enter the following commands:
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/oracle/*.java
        > javac -Xlint:unchecked -sourcepath src/ -d bin/ src/messaging/*.java
        > jar cfe oracle.jar oracle.Oracle -C bin .
6. Enter the following command:
        > LAUNCH.cmd
7. Using the 4, try some test deposits, withdrawals, transfers, and queries.
   Use ctrl-c to kill a processor, register this on the oracle, start the 
   processor again, register this on the oracle, verify that everything still works.


Names and Descriptions of Files:

The client directory contains files to create and run the GUI. 

The messaging directory contains the messaging class, several helper classes,
such as a callback, and wrappers for Message objects. Additionally, there is a
test directory to test some of the checks performed at the Messaging level.

The server directory contains files for running the server.

The oracle directory contains files for running the oracle GUI and logic.

Phase III was based on our own Phase I solution.
