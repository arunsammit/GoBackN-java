import java.net.InetAddress;

public class Test {
    public static void main(String[] args) {
        byte x = Byte.MIN_VALUE;
        System.out.println( Integer.toBinaryString(x) );
        System.out.println(x);

        // System.out.println(Integer.toBinaryString(x - 1));
        x = (byte) (x - 1);
        System.out.println( Integer.toBinaryString(x));
        System.out.println(x);
        byte p = (byte) 0;
    }
}
