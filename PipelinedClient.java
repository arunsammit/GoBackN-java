import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PipelinedClient {
    private final DatagramPacket[] sendPackets;
    // buffer length
    private final int N;
    // max sequence Number
    private final int maxSeqNum;
    // sequence number of the oldest unacknowledged packet
    private int base;
    // sequence number of the next packet to be sent
    private int nextSeqNum;
    // ip address to which the data has to  be sent
    private final InetAddress address;
    // port to which the data has to be sent
    private final int port;
    // datagramSocket for communication with server
    private final DatagramSocket socket;
    // variable to indicate that client is still waiting for acknowledgement packets from server
    private boolean listening;
    // executorService for scheduling timeout events
    ScheduledExecutorService scheduledService;
    // ScheduledFuture representing the future scheduled task
    ScheduledFuture<?> scheduledFuture;

    private long delay;
    //task to be executed in each timeout event
    public PipelinedClient(int bufferLength, InetAddress address, int port) throws SocketException {
        this.sendPackets = new DatagramPacket[bufferLength];
        N = bufferLength;
        maxSeqNum = bufferLength + 1;
        base = 0;
        nextSeqNum = 0;
        this.address = address;
        this.port = port;
        this.listening = true;
        socket = new DatagramSocket();
        delay = 100;
        scheduledService = Executors.newSingleThreadScheduledExecutor();
        Runnable r = this::receivePackets;
        new Thread(r).start();
    }

    private void receivePackets() {
        try {
            byte[] receiveBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            while (listening) {
                socket.receive(receivePacket);
                synchronized (this){
                    var seqNumber = PacketUtils.extractPacket(receivePacket).getSeqNum();
                    System.out.println("received acknowledgement for packet number " + seqNumber);
                    System.out.println("incrementing base to " + seqNumber + 1);
                    base = (seqNumber + 1) % maxSeqNum;
                    scheduledFuture.cancel(true);
                    if(base != nextSeqNum){
                        scheduledFuture = scheduledService.schedule(this::scheduledTask, delay, TimeUnit.MILLISECONDS);
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scheduledTask() {
        System.out.println("Timeout occurred !");
        scheduledFuture = scheduledService.schedule(this::scheduledTask, delay, TimeUnit.MILLISECONDS);
        for (int i = base; i != nextSeqNum; i = (i+1)%maxSeqNum) {
            System.out.println("retransmitting packet number: " + i);
            try{
                socket.send(sendPackets[i]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    

    public synchronized boolean canSend(){
        return ( nextSeqNum + 1 ) % maxSeqNum != base;
    }
    public void stop() {
        listening = false;
    }
    public synchronized void sendData(String data) throws IOException {
        if(canSend()){
            var packet = PacketUtils.createPacket(address, port, nextSeqNum, data);
            sendPackets[nextSeqNum % N] = packet;
            System.out.println("sending the packet with sequence number " + nextSeqNum);
            socket.send(packet);
            if(base == nextSeqNum){
                scheduledFuture = scheduledService.schedule(this::scheduledTask, delay, TimeUnit.MILLISECONDS);
            }
            nextSeqNum = ( nextSeqNum + 1 ) % maxSeqNum;
            return;
        }
        throw new RuntimeException("Buffer Capacity Full");
    }

}
