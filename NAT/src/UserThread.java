
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

class UserThread extends Thread {

    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    private int maxUsers;
    private Socket userSocket = null;
    private UserThread[] users = null;
    private boolean internal;
    private String assigned_ip;
    private String NAT_IP;
    private String NAT_Mac;
    private static Hashtable<String, String> NAT_Table = new Hashtable<String, String>();
    private static NAT_Box box;

    public UserThread(ObjectInputStream input, ObjectOutputStream output, UserThread[] users, String assigned_ip, boolean internal, String NAT_IP, String NAT_Mac, NAT_Box nat, Socket userSocket) {
        this.input = input;
        this.output = output;
        this.users = users;
        maxUsers = users.length;
        this.assigned_ip = assigned_ip;
        this.internal = internal;
        this.NAT_IP = NAT_IP;
        this.NAT_Mac = NAT_Mac;
        this.userSocket = userSocket;
        this.box = nat;
    }

    public void run() {
        while (true) {
            try {

                Paquet packet = (Paquet) input.readObject();
                String dest = packet.getDestIP();

                if (dest.equals("quit")) {
                    output.writeObject(null);
                    box.closeSocket(assigned_ip);
                    assigned_ip = "gone";
                    break;
                }
                if (dest.equals(NAT_IP)) {
                    //message from external user
                    //Look up in the NAT table, if not there drop packet

                    if (!inNatTable(packet.getSourceIP())) {
                        //drop packet
                        System.out.println("Dropped packet from IP(" + packet.getSourceIP() + ")");
                        Paquet err = new Paquet(NAT_IP, packet.getSourceIP(), NAT_Mac, 8000, "Your IP address is not in the NAT table: message(\"" + packet.getPayload() + "\") not sent.");
                        output.writeObject(err);
                    } else {
                        //in the table, update packet and forward to internal user
                        String intDest = NAT_Table.get(packet.getSourceIP());
                        packet.setDestIP(intDest);
                        synchronized (this) {
                            for (int i = 0; i < maxUsers; i++) {
                                if (intDest.equals(users[i].assigned_ip)) {
                                    users[i].output.writeObject(packet);
                                    removeFromNat(packet.getSourceIP());
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    //message from internal
                    boolean flag = false;
                    synchronized (this) {
                        for (int i = 0; i < maxUsers; i++) {
                            if (users[i] != null) {
                                if (dest.equals(users[i].assigned_ip)) {
                                    flag = true;
                                    boolean destInternal = users[i].internal;
                                    //internal to internal
                                    if (destInternal && internal) {
                                        //Sender and receiver are both internal
                                        users[i].output.writeObject(packet);
                                        break;
                                    }

                                    //internal to external
                                    if (internal && !destInternal) {
                                        add_to_NAT(packet);
                                        packet.setSourceIP(NAT_IP);
                                        packet.setSourceMac(NAT_Mac);
                                        users[i].output.writeObject(packet);
                                        break;
                                    }

                                }
                            }
                        }
                        if (!flag) {
                            Paquet err = new Paquet(NAT_IP, packet.getSourceIP(), NAT_Mac, 8000, "Invalid IP address: message(\"" + packet.getPayload() + "\") not sent.");
                            output.writeObject(err);
                        }
                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(UserThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UserThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            System.out.println("User left");
            input.close();
            output.close();
            userSocket.close();

        } catch (IOException ex) {

        }
    }

    public boolean inNatTable(String sourceIP) {
        boolean resp;
        if (NAT_Table.containsKey(sourceIP)) {
            resp = true;
        } else {
            resp = false;
        }
        return resp;
    }

    public void add_to_NAT(Paquet p) {
        NAT_Table.put(p.getDestIP(), p.getSourceIP());

    }

    public void removeFromNat(String sourceIP) {
        NAT_Table.remove(sourceIP);
    }
}