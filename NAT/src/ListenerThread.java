
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class ListenerThread extends Thread {

    private Client client;
    private ObjectInputStream is;

    public ListenerThread(Client client) {
        this.client = client;
        is = client.getIs();

    }

    public void run() {
        /*
         *  The thread that receives messages from NAT router
         */

        try {
            String givenIP = (String) is.readObject();
            String macAddress = (String) is.readObject();
            client.setIpGiven(givenIP);
            client.setMacAddr(macAddress);
            System.out.println("Assigned IP: " + givenIP);
            System.out.println("Assigned MAC: " + macAddress);
            synchronized (this) {
                client.setJoinedNetwork(true);
            }
            Paquet recv;
            try {
                recv = (Paquet) is.readObject();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                recv = null;
            }

            while (recv != null) {
                System.out.println("Received packet from:  IP(" + recv.getSourceIP() + ") Mac(" + recv.getSourceMac() + ") Message = " + recv.getPayload());
                try {
                    recv = (Paquet) is.readObject();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    recv = null;
                }

            }
            is.close();
            synchronized (this) {
                client.setClosed(true);
            }
        } catch (IOException e) {
            System.err.println("IO exception: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException:  " + e);
        }
    }
}
