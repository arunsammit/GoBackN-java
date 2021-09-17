import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 4) {
            System.err.println("Usage: java Server <port number> <probability of error> <probability of dropping> <channel delay (ms)> <buffer Length> ");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        double probabilityOfError = Double.parseDouble(args[1]);
        double probabilityOfDrop = Integer.parseInt(args[2]);
        long channeldelay = Long.parseLong(args[3]);
        int bufferLength = Integer.parseInt(args[4]);
        int maxSeqNum = bufferLength + 1;
        int expectedSeqNum =  1;
        boolean listening = true;
        System.out.println("Server started ...");
        try (DatagramSocket socket = new DatagramSocket(portNumber)) {
            while (listening) {
                byte[] buf = new byte[1024];
                // packet for receiving the message
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                System.out.println("Waiting for client ...");
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
                        System.out.println("Out of order packet received! Hence dropping");
                        continue;
                    }
                    expectedSeqNum = (expectedSeqNum + 1) % maxSeqNum;
                    // expectedSeqNum ++;
                    System.out.println("Received message number " + seqNum + ":\n" + received);
                    
                }
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                int previousSeqNum =  (expectedSeqNum - 1 + maxSeqNum) % maxSeqNum;
                // int previousSeqNum = expectedSeqNum - 1;
                DatagramPacket sendPacket = PacketUtils.createPacket(address, port, previousSeqNum, "ACK");
                choiceOfDrop = Math.random();
                if(choiceOfDrop < probabilityOfDrop) continue;
                socket.send(sendPacket);
            }
        }
    }

}