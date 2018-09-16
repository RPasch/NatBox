
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class ListenerThread extends Thread {

    private Client client;
    private ObjectInputStream is;
    private String msg;
    private String givenIP;
    private String macAddress;
    
    public ListenerThread(Client client) {
        this.client = client;
        is = client.is;

    }
    public void readSetInfo() throws IOException, ClassNotFoundException{
        msg = (String) is.readObject();
        givenIP = (String) is.readObject();
        macAddress = (String) is.readObject();
        client.number = (int) is.readObject();
        client.setIpGiven(givenIP);
        client.setMacAddr(macAddress);
        System.out.println("Assigned IP: " + givenIP);
        System.out.println("Assigned MAC: " + macAddress);
    }
    
    public void checkInEx(Paquet recv){
        if(recv.getInEx() == 0){
                System.out.println("packet from Internal \n");

            
            } else if ( recv.getInEx() == 1){
                System.out.println("packet from external \n");
            
            }
    
    }
    
    public void run() {
        /*
         *  The thread that receives messages from NAT router
         */

        try {
            readSetInfo();
            
            synchronized (this) {
                client.setJoinedNetwork(true);
            }
            
            if( msg.equals(client.number+1)){
                System.out.println("This user is not valid");
            }
            Paquet recv;
            try {
                recv = (Paquet) is.readObject();
            } catch (ClassNotFoundException ex) {
                System.err.println(ex);
                recv = null;
            }

            while (recv != null) {
                checkInEx(recv);
                System.out.println("Received packet from:  IP(" + recv.getSourceIP() + ") Mac(" + recv.getSourceMac() + ") Message = " + recv.getPayload());
                try {
                    recv = (Paquet) is.readObject();
                } catch (ClassNotFoundException ex) {
                    System.err.println(ex);
                    recv = null;
                }

            }
            is.close();
            synchronized (this) {
                client.setClosed(true);
            }
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found error :  " + e);
        }
    }
}
