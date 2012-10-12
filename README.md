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
3. Enter the following command:
        > LAUNCH.cmd
4. Using the 4 GUIs, try some test deposits, withdrawals, transfers, and queries.
   To make a deposit, click on the first GUI that was created. Enter deposit account 
   01.11111 and deposit amount 100 and click "Deposit". Enter query account 
   01.11111 and click "query". Enter withdrawal amount 10 and click "Withdraw". Enter
   transfer to account 02.11111 and transfer from account 01.11111 and transfer amount
   50 and click "Transfer". Click on the second GUI generated. Enter query account 02.11111
   and click query.

Names and Descriptions of Files:

The client directory contains files to create and run the GUI. 

The messaging directory contains the messaging class, several helper classes,
such as a callback, and wrappers for Message objects. Additionally, there is a
test directory to test some of the checks performed at the Messaging level.

The server directory contains files for running the server.

Phase II was based on our own Phase I solution.
