
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
     * @param senderIP  sender ip address
     * @param recvIP    receiver ip address
     * @param senderMac sender's mac address
     * @param port  port number
     * @param msg   message
     * @param number    number
     * @param InEx  inex
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
     * @return  inex
     */
    public int getInEx() {
        return InEx;
    }

    /**
     * Sets InEx
     *
     * @param InEx  inex
     */
    public void setInEx(int InEx) {
        this.InEx = InEx;
    }

    /**
     * Returns the senderIP
     *
     * @return  sender ip address
     */
    public String getSenderIP() {
        return senderIP;
    }

    /**
     * Sets the SenderIP
     *
     * @param senderIP  sender ip address
     */
    public void setSenderIP(String senderIP) {
        this.senderIP = senderIP;
    }

    /**
     * Returns the receiverIP
     *
     * @return  receiver ip address
     */
    public String getRecvIP() {
        return recvIP;
    }

    /**
     * Sets the receiverIP
     *
     * @param recvIP    receiver ip address
     */
    public void setRecvIP(String recvIP) {
        this.recvIP = recvIP;
    }

    /**
     * Gets the sender MAC
     *
     * @return  sender mac address
     */
    public String getSenderMac() {
        return senderMac;
    }

    /**
     * Sets the SenderMac
     *
     * @param senderMac sender mac address
     */
    public void setSenderMac(String senderMac) {
        this.senderMac = senderMac;
    }

    /**
     * Gets the port
     *
     * @return  port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     *
     * @param port  port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the msg
     *
     * @return  message
     */
    public String getMsg() {
        return msg;
    }

    /**
     * sets the msg
     *
     * @param msg   message
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * gets the number
     * 
     * @return  the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * sets the number
     * 
     * @param number the number
     */
    public void setNumber(int number) {
        this.number = number;
    }

}
