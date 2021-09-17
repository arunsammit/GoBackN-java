public class PacketInfo {
    private int seqNum;
    private String message;

    public PacketInfo(int seqNum, String message) {
        this.seqNum = seqNum;
        this.message = message;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
