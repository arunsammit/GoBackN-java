import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PipelinedClient {
    private final DatagramPacket[] sendPackets;
    // buffer length
    private final int N;
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
    private boolean endSending;
    // executorService for scheduling timeout events
    ScheduledExecutorService scheduledService;
    // ScheduledFuture representing the future scheduled task
    ScheduledFuture<?> scheduledFuture;
    // total time the client is busy pushing packets in the channel
    private AtomicLong sendingTime;
    // time to wait after which all unacknowledged packets are retransmitted
    private final long delay;
    //task to be executed in each timeout event
    public PipelinedClient(int bufferLength, InetAddress address, int port, long delay, AtomicLong sendingTIme) throws SocketException {
        this.sendPackets = new DatagramPacket[bufferLength];
        N = bufferLength;
        base = 1;
        nextSeqNum = 1;
        this.address = address;
        this.port = port;
        this.sendingTime = sendingTIme;
        socket = new DatagramSocket();
        this.delay = delay;
        scheduledService = Executors.newSingleThreadScheduledExecutor();
        Runnable r = this::receivePackets;
        new Thread(r,"receiving").start();
    }

    private void receivePackets() {
        try {
            byte[] receiveBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            while (true) {
                socket.receive(receivePacket);
                synchronized (this){
                    var seqNumber = PacketUtils.extractPacket(receivePacket).getSeqNum();
                    System.out.print("received ack for packet  " + seqNumber);
                    base = seqNumber + 1;
                    System.out.println(", setting base = " + base);
                    this.notify();
                    scheduledFuture.cancel(true);
                    if(base != nextSeqNum){
                        scheduledFuture = scheduledService.schedule(this::retransmitPackets, delay, TimeUnit.MILLISECONDS);
                    } else if (endSending){
                        System.out.println("Stopping receiving packets since endSending flag is true");
                        synchronized(sendingTime){
                            sendingTime.notify();
                        }
                        socket.close();
                        scheduledService.shutdown();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void retransmitPackets() {

        long currentTime = System.currentTimeMillis();
        System.out.println("Timeout occurred ! base is " + base + " nextSeqNum is " + nextSeqNum);
        scheduledFuture = scheduledService.schedule(this::retransmitPackets, delay, TimeUnit.MILLISECONDS);
        for (int i = base; i < nextSeqNum; i = i + 1) {
            System.out.println("retransmitting packet number: " + i);
            try{
                socket.send(sendPackets[i%N]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        sendingTime.set(sendingTime.get() + System.currentTimeMillis() - currentTime); 
    }

    public void stopTransmission() {
        System.out.println("Stop Transmission called . . .");
        endSending = true;
    }
    public synchronized void sendData(String data) throws IOException, InterruptedException {
        while (nextSeqNum >= base + N) {
            System.out.println("buffer full, waiting for ack of sent packet : " + base);
            this.wait();
        }
        long currentTime = System.currentTimeMillis();
        var packet = PacketUtils.createPacket(address, port, nextSeqNum, data);
        sendPackets[nextSeqNum % N] = packet;
        System.out.println("sending packet: " + nextSeqNum);
        socket.send(packet);
        sendingTime.set(sendingTime.get() + System.currentTimeMillis() - currentTime); 
        if(base == nextSeqNum){
            scheduledFuture = scheduledService.schedule(this::retransmitPackets, delay, TimeUnit.MILLISECONDS);
        }
        nextSeqNum = nextSeqNum + 1 ;
        System.out.println("nextSeqNum = " + nextSeqNum);
    }

}
