cd Bank/

start java -jar server.jar 01 01
start java -jar server.jar 01 02
start java -jar server.jar 01 03
start java -jar server.jar 02 01
start java -jar oracle.jar
start java -jar client.jar 01
start java -jar client.jar 02

