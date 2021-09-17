import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class PacketUtils {

    static DatagramPacket createPacket(InetAddress address, int port, int sequenceNumber, String message)
            throws IOException {
        ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
        DataOutputStream dOutputStream = new DataOutputStream(bOutputStream);
        dOutputStream.writeInt(sequenceNumber);
        dOutputStream.writeUTF(message);
        dOutputStream.flush();
        byte[] buf = bOutputStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        return packet;
    }
    static PacketInfo extractPacket(DatagramPacket packet) throws IOException {
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(packet.getData()));
        int seqNum = din.readInt();
        String received = din.readUTF();
        var packetInfo = new PacketInfo(seqNum, received);
        return packetInfo;
    }
    
}
