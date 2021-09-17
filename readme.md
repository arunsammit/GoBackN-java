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

## Code Structure

1. The Sender side of the GBN protocol is implemented in [PipelinedClient.java](PipelinedClient.java) class. It has four methods. The methods which modify the fields of classes are decleared as synchornized for thread-safety purposes. For calculating the sending time and for inter - thread communication the object AtomicLong sendingTime is being passed through the constructor [Ref](https://stackoverflow.com/a/5999146/10786438). This class is used by [Client](Client.java) process to send the messages to the [Server](Server.java) process
   1. [receivePackets()](PipelinedClient.java#receivePackets): This method is run as a seperate thread from the constructor so that the client can continues to receive acknowledgements in the background without blocking sending of packets 
   2. [retransmitPackets()](PipelinedClient.java#retransmitPackets): This method is run from ScheduledExecutorService as a seperate thread after a given every delay till an acknowledgement is not received for the oldest unacknowledged packet (after which the Scheduler is restarted)
   3. [sendData(String data)](PipelinedClient.java#sendData): This method is used to send the data and packet sequence number as a UDP datagram through the socket. This method blocks the execution of program if there are already N (buffer size) unacknowledged packets present in the buffer.
   4. [stopTransmission()](PipelinedClient.java#stopTransmission): This method tries to stop the transmission by stopping the running threads and closing the open sockets after all the acknowledgements have been received corresponding to the data sent from [sendData] (PipelinedClient.java#sendData)
2. The Receiver side of GBN protocol is directly implemented in main method of [Server](Server.java#main). Also, the mechanism to randomly drop or corrupt packets is implemented here. Channel delay is also simulated in this class using Thread.sleep() method. 