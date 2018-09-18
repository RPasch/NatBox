
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;


public class NAT_Box {

    private final static int max = 255;
    private static ServerSocket socketServ = null;
    private static Socket socketUs = null;
    private static UserThread[] users = new UserThread[max];
    private static String NatBoxMac = "AA:AA:AA:AA:AA:AA";
    private static String NatBoxPrivateIp = "192.168.0.0";
    private static String NatBoxPublicIp = "69:69:69:69";
    private static Hashtable<String, String> IPs = new Hashtable<String, String>();
    public static int number;
    private static List<String> MACs = new ArrayList<String>();
    private static List<String> extIPs = new ArrayList<String>();
    private static Queue<UserThread> userThreads = new LinkedList<>();

    /**
     * This is the constructor that starts the NatBox
     *
     * @param PortNumber
     */
    public NAT_Box(int PortNumber) {
        try {
            socketServ = new ServerSocket(PortNumber);
        } catch (IOException e) {
            System.out.println("Server could not be created");
        }
        generateAvailableIP();
        extIPs.add(NatBoxPublicIp);
        MACs.add(NatBoxMac);
        System.out.println("Private IP  " + NatBoxPrivateIp);
        System.out.println("Public IP   " + NatBoxPublicIp);
        System.out.println("MAC   " + NatBoxMac);
    }

    /**
     * This method closes all the sockets as well as removes the IPss from the
     * NatBox
     *
     * @param ip
     */
    public void closeSocket(String ip) {
        IPs.remove(ip);
        IPs.put(ip, "false");
    }

    /**
     * This method generates all available IPs from the list of IPs given to the
     * program
     */
    public void generateAvailableIP() {
        try {
            Scanner sc = new Scanner(new File("ip.txt"));
            while (sc.hasNext()) {
                String line = sc.nextLine();
                Scanner scLine = new Scanner(line);
                String temp = scLine.next();
                IPs.put(temp, "false");
            }
        } catch (Exception e) {
            System.out.println("Error loading IP adresses: " + e);
        }
    }

    /**
     * This method generates random Mac addresses and it is called in
     * generateMAC()
     *
     * @return
     */
    public static String randomMACAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte) (macAddr[0] & (byte) 254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {

            if (sb.length() > 0) {
                sb.append(":");
            }

            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * This method is called when a MAC address has to be assigned to a new user
     *
     * @return
     */
    public static String generateMAC() {
        String mac = "";
        boolean carryOn = true;

        while (carryOn) {
            mac = randomMACAddress();
            if (!MACs.contains(mac)) {
                MACs.add(mac);
                carryOn = false;
            }
        }

        return mac;
    }

    /**
     * Removes a user from the UserThreads list once it disconnects
     *
     */
    public static void removeUser() {
        if (!userThreads.isEmpty()) {
            userThreads.remove();
        }

    }

    /**
     * It generates a IP address for an external user
     *
     * @return
     */
    public static String generateExternalIP() {
        Random r = new Random();

        String ip = r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
        while (extIPs.contains(ip)) {
            ip = generateExternalIP();
        }
        return ip;
    }

    /**
     * It classifies the incoming message to either internal or external and
     * depends on who sent the msg
     *
     * @param ip
     * @param mac
     * @param num
     * @return
     */
    public static String generateMessage(String ip, String mac, int num) {
        String msg = "100";

        if (ip.equals(NatBoxPrivateIp)) {
            msg = "1";
        } else if (ip.equals(NatBoxPublicIp)) {
            msg = "2";
        } else if (mac.equals(NatBoxMac)) {
            msg = "1";
        } else if (num == 0) {
            msg = "0";
        }
        for (int i = 0; i < extIPs.size(); i++) {
            if (extIPs.contains(ip)) {
                msg = "0";
            }

        }
        return msg;
    }

    /**
     * It does all the sending. It sends all needed information the the client
     *
     * @param msg
     * @param ip
     * @param mac
     * @param number
     * @param output
     * @throws IOException
     */
    public static void sendInfo(String msg, String ip, String mac, int number, ObjectOutputStream output) throws IOException {
        output.writeObject(msg);
        output.writeObject(ip);
        output.writeObject(mac);
        output.writeObject(number);

    }

    /**
     * The main method. It calls most of the other methods and is the part of
     * the program that does almost everything
     *
     * @param args
     */
    public static void main(String[] args) {
        NAT_Box nat = new NAT_Box(8000);
        while (true) {
            try {
                nat.socketUs = socketServ.accept();
                ObjectInputStream inStream = new ObjectInputStream(socketUs.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socketUs.getOutputStream());
                System.out.println("\n **************\n A new user is connected \n ************** \n");
                String inter = "";

                int InEx = 2;
                try {
                    InEx = (int) inStream.readObject();
                } catch (ClassNotFoundException ex) {
                    System.err.println("could not read object : " + ex);
                }
                String givenIP = "empty";

                boolean internal = true;
                if (InEx == 0) {
                    internal = true;
                    for (int i = 1; i < max; i++) {
                        String temp = IPs.get("192.168.0." + i);
                        String genIP = "192.168.0." + i;
                        if (temp.equals("false")) {
                            givenIP = genIP;
                            number = i;
                            break;
                        }
                    }
                } else if (InEx == 1) {
                    internal = false;
                    number = 0;
                    givenIP = generateExternalIP();
                    while (extIPs.contains(givenIP)) {
                        givenIP = generateExternalIP();
                    }
                }
                String givenMAC = generateMAC();
                String message = generateMessage(givenIP, givenMAC, number);

                sendInfo(message, givenIP, givenMAC, number, outStream);

                if (givenIP.equals("empty")) {
                    outStream.close();
                    socketUs.close();
                } else {
                    IPs.put(givenIP, "true");
                    int i;
                    for (i = 0; i < max; i++) {
                        if (users[i] == null) {
                            users[i] = new UserThread(inStream, outStream, users, givenIP, internal, NatBoxPublicIp, NatBoxMac, nat, socketUs, i, InEx);
                            users[i].start();
                            userThreads.add(users[i]);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not accept user." + e);
            }
        }
    }
}
