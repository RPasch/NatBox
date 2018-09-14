import java.io.Serializable;

public class Paquet implements Serializable {

    private String sourceIP;
    private String destIP;
    private String sourceMac;
    private int port;
    private String payload;

    public Paquet(String sourceIP, String destIP, String sourceMac, int port, String payload) {
        this.sourceIP = sourceIP;
        this.destIP = destIP;
        this.sourceMac = sourceMac;
        this.port = port;
        this.payload = payload;
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

}
