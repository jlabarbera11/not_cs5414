Tutorial:

1. Unzip out submission to a directory named BigRedBank.
2. Open a command prompt. Change to the BigRedBank directory you just created.
3. Enter the following command:
        > make jar-server
4. Enter the following command:
        > make jar-client
3. Enter the following command:
        > LAUNCH.cmd
4. Using the 4 GUIs, try some test deposits, withdrawals, transfers, and queries.
   To make a deposit, click on the first GUI that was created. Enter deposit account 
   01.11111 and deposit amount 100 and click "Deposit". Enter query account 
   01.11111 and click "query". Enter withdrawal amount 10 and click "Withdraw". Enter
   transfer to account 02.11111 and transfer from account 01.11111 and transfer amount
   50 and click "Transfer". Click on the second GUI generated. Enter query account 02.11111
   and click query.

Generic Installation Instructions:

1. Unzip out submission to a directory named BigRedBank.
2. Open a command prompt. Change to the BigRedBank directory you just created.
3. Enter the following command:
        > java -jar server.jar <BRANCH_NUM>
   This will start the branch server.
4. Enter the following command:
        > java -jar client.jar <BRANCH_NUM>
   This will start the client GUI.

Names and Descriptions of Files:

The client directory contains files to create and run the GUI. The messaging directory
contains files for sending and receiving messages. The server directory contains files
for running the server.

