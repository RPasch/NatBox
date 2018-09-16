import java.io.Serializable;

public class Paquet implements Serializable {

    private String sourceIP;
    private String destIP;
    private String sourceMac;
    private int port;
    private String payload;
    private int number ;
    private int InEx;
    
    public Paquet(String sourceIP, String destIP, String sourceMac, int port, String payload, int number, int InEx) {
        this.sourceIP = sourceIP;
        this.destIP = destIP;
        this.sourceMac = sourceMac;
        this.port = port;
        this.payload = payload;
        this.number = number;
        this.InEx = InEx;
    }

    public int getInEx() {
        return InEx;
    }

    public void setInEx(int InEx) {
        this.InEx = InEx;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getDestIP() {
        return destIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public void setSourceMac(String sourceMac) {
        this.sourceMac = sourceMac;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}
