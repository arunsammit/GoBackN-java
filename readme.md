# GO Back N

Go Back N protocol for reliable data transfer

To compile the file use:

``` bash
javac Client.java
javac Server.java 
```

Run the compiled Server class using: `java
java Server <port_number> <probability_of_error> <probability_of_dropping_packet> <channel_delay (ms)> <output_file_path>` example:

``` shell
java Server 8081 .1 .5 100 received-messages.txt`
```

Run the compiled Client class using `java Client <hostname> <port> <filepath> <buffer_length> <retransmission_delay (in ms)>`
example:

``` shell
java Client localhost 8081 ./messages.txt 5 500
```

The log file of Client will be generated in `Client.log` file and the log file of Server will be generated in `Server.log` file. Upon running the Client will try to send each line contained in the `messages.txt` file as a UDP datagram. The Server will receive the packets and will write them in the `received-messages.txt` file. channel utilization, sending time and transmission time will be printed in Client.log when the transmission is complete.

