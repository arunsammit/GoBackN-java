import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 5) {
            System.err.println("Usage: java Server <port number> <probability of error> <probability of dropping> <channel delay (ms)> <output file path>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        double probabilityOfError = Double.parseDouble(args[1]);
        double probabilityOfDrop = Double.parseDouble(args[2]);
        long channeldelay = Long.parseLong(args[3]);
        int expectedSeqNum =  1;
        System.out.println("Server started ...");
        File file = new File(args[4]);
        file.createNewFile();
        File logFile = new File("Server.log");
        PrintStream stream = new PrintStream(logFile);
        System.setOut(stream);
        try(FileWriter outputWriter = new FileWriter(file);
            DatagramSocket socket = new DatagramSocket(portNumber)){
            while (true) {
                byte[] buf = new byte[1024];
                // packet for receiving the message
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                // message from client
                socket.receive(packet);
                // simulating channel delay
                Thread.sleep(channeldelay);
                // simulating error
                double choiceOfError = Math.random();
                double choiceOfDrop = Math.random();
                if (choiceOfDrop < probabilityOfDrop) continue;
                if (choiceOfError < probabilityOfError) {
                    System.out.println("Erroneous packet received");
                } else {
                    PacketInfo info = PacketUtils.extractPacket(packet);
                    int seqNum = info.getSeqNum();
                    String received = info.getMessage();
                    if(seqNum != expectedSeqNum){
                        System.out.println("Out of order packet " + seqNum + " !");
                    } else {
                        expectedSeqNum ++;
                        System.out.println("Received message number " + seqNum);
                        outputWriter.write(seqNum + " " + received + "\n");
                        outputWriter.flush();
                    }
                }
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                int previousSeqNum = expectedSeqNum - 1;
                DatagramPacket sendPacket = PacketUtils.createPacket(address, port, previousSeqNum, "ACK");
                choiceOfDrop = Math.random();
                if(choiceOfDrop < probabilityOfDrop) continue;
                System.out.println("Sending ack for " + previousSeqNum);
                socket.send(sendPacket);
            }
        }
        
    }

}