
import java.io.IOException;
import java.io.ObjectInputStream;

class ListenerThread extends Thread {

    private Client client;
    private ObjectInputStream inStream;
    private String msg;
    private String givenIP;
    private String MACaddrsGiven;

    public ListenerThread(Client client) {
        this.client = client;
        inStream = client.inStream;
    }

    /**
     * This method reads all information needed and sets the required variables
     * to continue.
     */
    public void readSetInfo() throws IOException, ClassNotFoundException {
        msg = (String) inStream.readObject();
        givenIP = (String) inStream.readObject();
        MACaddrsGiven = (String) inStream.readObject();
        client.num = (int) inStream.readObject();
        client.setIpGiven(givenIP);
        client.setMacAddr(MACaddrsGiven);
        System.out.println("Assigned IP: " + givenIP);
        System.out.println("Assigned MAC: " + MACaddrsGiven);
    }

    /**
     * Checks inex
     * 
     * @param recv  received paquet
     */
    public void checkInEx(Paquet recv) {
        if (recv.getInEx() == 0) {
            System.out.println("packet from Internal \n");
        } else if (recv.getInEx() == 1) {
            System.out.println("packet from external \n");
        }
    }

    /**
     * This method is called when the thread is started. It constantly listens
     * for a new messages and responds accordingly.
     */
    public void run() {

        try {
            readSetInfo();

            synchronized (this) {
                client.setIsIn(true);
            }

            if (msg.equals(client.num + 1)) {
                System.out.println("This user is not valid");
            }
            
            Paquet recvPackect;
            
            try {
                recvPackect = (Paquet) inStream.readObject();
            } catch (ClassNotFoundException ex) {
                System.err.println(ex);
                recvPackect = null;
            }

            while (recvPackect != null) {
                checkInEx(recvPackect);
                System.out.println("Received packet from:  IP(" + recvPackect.getSenderIP() + ") Mac(" + recvPackect.getSenderMac() + ") Message = " + recvPackect.getMsg());
                try {
                    recvPackect = (Paquet) inStream.readObject();
                } catch (ClassNotFoundException ex) {
                    System.err.println(ex);
                    recvPackect = null;
                }
            }
            
            inStream.close();
            
            synchronized (this) {
                client.setIsActive(true);
            }
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found error :  " + e);
        }
    }
}
