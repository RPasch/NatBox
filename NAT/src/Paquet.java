
import java.io.Serializable;


public class Paquet implements Serializable {

    private String senderIP;
    private String recvIP;
    private String senderMac;
    private int port;
    private String msg;
    private int number;
    private int InEx;

    /**
     * The constructor of the Paquet class. Initializes all variable of the
     * instance
     *
     * @param senderIP
     * @param recvIP
     * @param senderMac
     * @param port
     * @param msg
     * @param number
     * @param InEx
     */
    public Paquet(String senderIP, String recvIP, String senderMac, int port, String msg, int number, int InEx) {
        this.senderIP = senderIP;
        this.recvIP = recvIP;
        this.senderMac = senderMac;
        this.port = port;
        this.msg = msg;
        this.number = number;
        this.InEx = InEx;
    }

    /**
     * Return InEx
     *
     * @return
     */
    public int getInEx() {
        return InEx;
    }

    /**
     * Sets InEx
     *
     * @param InEx
     */
    public void setInEx(int InEx) {
        this.InEx = InEx;
    }

    /**
     * Returns the senderIP
     *
     * @return
     */
    public String getSenderIP() {
        return senderIP;
    }

    /**
     * Sets the SenderIP
     *
     * @param senderIP
     */
    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    /**
     * Returns the receiverIP
     *
     * @return
     */
    public String getRecvIP() {
        return recvIP;
    }

    /**
     * Sets the receiverIP
     *
     * @param recvIP
     */
    public void setRecvIP(String recvIP) {
        this.recvIP = recvIP;
    }

    /**
     * Gets the sender MAC
     *
     * @return
     */
    public String getSenderMac() {
        return senderMac;
    }

    /**
     * Sets the SenderMac
     *
     * @param senderMac
     */
    public void setSenderMac(String senderMac) {
        this.senderMac = senderMac;
    }

    /**
     * Gets the port
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the msg
     *
     * @return
     */
    public String getMsg() {
        return msg;
    }

    /**
     * sets the msg
     *
     * @param msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     *
     * @return
     */
    public int getNumber() {
        return number;
    }

    /**
     *
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

}
