import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 5) {
            System.out.println("Usage: java Client <hostname> <port> <filepath> <buffer length> <delay b/w retransmission (in ms)>");
            System.exit(1);
        }
        System.out.println("Client started!");
        File logFile = new File("Client.log");
        PrintStream stream = new PrintStream(logFile);
        System.setOut(stream);
        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        int bufferLength = Integer.parseInt(args[3]);
        long delay = Integer.parseInt(args[4]);
        PipelinedClient pipelinedClient = new PipelinedClient(bufferLength,address,port,delay);
        BufferedReader in = new BufferedReader(new FileReader(args[2]));
        String value;
        int cnt = 1;
        while ((value = in.readLine())!=null) {
            System.out.println("Trying to push message number: " + cnt +" in the pipeline:\n\t" + value);
            pipelinedClient.sendData(value);
            cnt++;
        }
        pipelinedClient.stopTransmission();
        in.close();
        System.out.println("Done");
    }
}