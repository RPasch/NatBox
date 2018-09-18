
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

class UserThread extends Thread {

    private ObjectInputStream inStream = null;
    private ObjectOutputStream outStream = null;
    private int max;
    private Socket socketU = null;
    private UserThread[] userThreads = null;
    private final boolean internal;
    private String given_ip;
    private String NAT_IP;
    private String NAT_Mac;
    private static Hashtable<String, String> NAT_Table = new Hashtable<String, String>();
    private static NAT_Box box;
    public int number;
    public int InternalOrNah = 0;
    private static Queue<Paquet> unsendPaquets = new LinkedList<>();
    private static Queue<Paquet> sendPaquets = new LinkedList<>();

    public UserThread(ObjectInputStream input, ObjectOutputStream output, UserThread[] users, String assigned_ip, boolean internal, String NAT_IP, String NAT_Mac, NAT_Box nat, Socket userSocket, int number, int InternalOrNah) {
        this.inStream = input;
        this.outStream = output;
        this.userThreads = users;
        max = users.length;
        this.given_ip = assigned_ip;
        this.internal = internal;
        this.NAT_IP = NAT_IP;
        this.NAT_Mac = NAT_Mac;
        this.socketU = userSocket;
        this.box = nat;
        this.number = number;
        this.InternalOrNah = InternalOrNah;
    }

    /**
     * This method puts the given Paquet in the NAT_Table
     *
     * @param p
     */
    public void putInNatTable(Paquet p) {
        NAT_Table.put(p.getRecvIP(), p.getSenderIP());

    }

    /**
     * This method takes out the given Paquet in the NAT_Table
     *
     * @param sourceIP
     */
    public void takeOutNatTable(String sourceIP) {
        NAT_Table.remove(sourceIP);
    }

    /**
     * This method is the method that is run when the thread is started. I
     * immediately receives information from the client. It handles messages
     * from internal - internal , internal to external , dropped packets and
     * ect...
     */
    public void run() {
        while (true) {
            try {

                Paquet p = (Paquet) inStream.readObject();
                String ip_of_recvr = p.getRecvIP();
                InternalOrNah = p.getInEx();
                if (ip_of_recvr.equals("exit")) {
                    outStream.writeObject(null);
                    box.closeSocket(given_ip);
                    given_ip = "gone";
                    break;
                }
                if (ip_of_recvr.equals(NAT_IP) && InternalOrNah == 1) {
                    System.out.println("\n +++++++++++++\n FROM EXTERNAL \n +++++++++++++\n ");
                    if (!inNatTable(p.getSenderIP())) {
                        System.out.println("Dropped packet from IP(" + p.getSenderIP() + ")");
                        Paquet errorPacket = new Paquet(NAT_IP, p.getSenderIP(), NAT_Mac, 8000, "Your IP address is not in the NAT table: message(\"" + p.getMsg() + "\") not sent.", number, InternalOrNah);
                        Paquet errP = new Paquet(NAT_IP, p.getSenderIP(), NAT_Mac, 8000, "Your IP address is not in the NAT table: message(\"" + p.getMsg() + "\") not sent.", number, InternalOrNah);
                        outStream.writeObject(errP);
                        unsendPaquets.add(errP);
                    } else {
                        String intDestIP = NAT_Table.get(p.getSenderIP());
                        p.setRecvIP(intDestIP);
                        synchronized (this) {
                            for (int i = 0; i < max; i++) {
                                if (intDestIP.equals(userThreads[i].given_ip)) {
                                    userThreads[i].outStream.writeObject(p);
                                    sendPaquets.add(p);
                                    takeOutNatTable(p.getSenderIP());
                                    break;
                                }
                            }
                        }
                    }
                } else if (InternalOrNah == 0) {
                    System.out.println("\n +++++++++++++\n FROM INTERNAL \n +++++++++++++\n ");

                    boolean boolCheck = false;
                    synchronized (this) {
                        for (int i = 0; i < max; i++) {
                            if (userThreads[i] != null) {
                                if (ip_of_recvr.equals(userThreads[i].given_ip)) {
                                    boolCheck = true;
                                    boolean isInternal = userThreads[i].internal;
                                    if (isInternal && internal) {
                                        Paquet newPacket = p;
                                        sendPaquets.add(newPacket);
                                        userThreads[i].outStream.writeObject(newPacket);
                                        break;
                                    }

                                    if (internal && !isInternal) {
                                        Paquet newPacket = p;
                                        putInNatTable(newPacket);

                                        newPacket.setSenderIP(NAT_IP);
                                        newPacket.setSenderMac(NAT_Mac);

                                        sendPaquets.add(newPacket);
                                        userThreads[i].outStream.writeObject(newPacket);
                                        break;
                                    }

                                }
                            }
                        }
                        if (!boolCheck) {

                            Paquet errP = new Paquet(NAT_IP, p.getSenderIP(), NAT_Mac, 8000, "IP invalid : message (\"" + p.getMsg() + "\") did not send.", number, InternalOrNah);

                            outStream.writeObject(errP);
                            unsendPaquets.add(errP);

                        }
                    }

                }
            } catch (IOException ex) {
                System.err.println(ex);
            } catch (ClassNotFoundException ex) {
                System.err.println(ex);

            }
        }
        closeAll();
    }

    /**
     * This method closes all sockets and all input/output streams
     */
    public void closeAll() {
        try {
            System.out.println("User left");
            inStream.close();
            outStream.close();
            socketU.close();
            NAT_Box.removeUser();

        } catch (IOException ex) {
            System.err.println(ex);

        }
    }

    /**
     * This method checks if the IP address is in the natbox table
     *
     * @params sourceIP
     */
    public boolean inNatTable(String sourceIP) {
        boolean resp;
        if (NAT_Table.containsKey(sourceIP)) {
            resp = true;
        } else {
            resp = false;
        }
        return resp;
    }

}
