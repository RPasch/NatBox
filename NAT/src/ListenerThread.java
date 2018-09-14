
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class listenerThread extends Thread {

    private Client client;
    private ObjectInputStream is;

    public listenerThread(Client client) {
        this.client = client;
        is = client.getIs();

    }

    public void run() {
        /*
         *  The thread that receives messages from NAT router
         */

        try {
            String ipGiven = (String) is.readObject();
            String macAddr = (String) is.readObject();
            client.setIpGiven(ipGiven);
            client.setMacAddr(macAddr);
            System.out.println("Assigned IP: " + ipGiven);
            System.out.println("Assigned MAC: " + macAddr);
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

        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException:  " + e);
        }
    }
}
