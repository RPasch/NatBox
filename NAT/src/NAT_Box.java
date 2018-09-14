import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NAT_Box {

    private final int MAX_USERS = 255;
    private ServerSocket serverSocket = null;
    private Socket userSocket = null;
    private UserThread[] users = new UserThread[MAX_USERS];
    private String myMacAddress = "AA:AA:AA:AA:AA:AA";
    private String myPrivateIPAddress = "192.168.0.0";
    private String myPublicIPAddress = "69:69:69:69";
    private Hashtable<String, String> available_ip = new Hashtable<String, String>();

    private List<String> MacAddresses = new ArrayList<String>();
    private List<String> externalIPs = new ArrayList<String>();

    public NAT_Box(int PortNumber) {
        try {
            serverSocket = new ServerSocket(PortNumber);
        } catch (IOException e) {
            System.out.println("Could not create server");
        }
        generateAvailableIP_MAC();
        externalIPs.add(myPublicIPAddress);
        MacAddresses.add(myMacAddress);
        System.out.println("Private IP Address: " + myPrivateIPAddress);
        System.out.println("Public IP Address: " + myPublicIPAddress);
        System.out.println("MAC Address: " + myMacAddress);
    }

    public void startNAT() {
        //create a new connection for each user
        while (true) {
            try {
                userSocket = serverSocket.accept();

                //setup input and output streams for new user
                ObjectInputStream input = new ObjectInputStream(userSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(userSocket.getOutputStream());
                System.out.println("New User connected");
                //determine if user is internal or external
                String inter = "";
                try {
                    inter = (String) input.readObject();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(UserThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                boolean internal;
                if (inter.equals("internal")) {
                    internal = true;
                } else {
                    internal = false;
                }
                //assign internal/external user ip and mac addr 
                String assigned_ip = "none";//assume no addresses are available
                String assigned_mac = generateMAC();
                if (internal) {
                    //assign internal address
                    for (int i = 1; i < MAX_USERS; i++) {
                        if (available_ip.get("192.168.0." + i).equals("false")) {
                            assigned_ip = "192.168.0." + i;
                            break;
                        }
                    }

                } else {
                    //assign external address

                    assigned_ip = generateExternalIP();
                    while (externalIPs.contains(assigned_ip)) {
                        assigned_ip = generateExternalIP();
                    }

                }
                output.writeObject(assigned_ip);
                output.writeObject(assigned_mac);

                if (assigned_ip.equals("none")) {
                    output.close();
                    userSocket.close();
                } else {
                    available_ip.put(assigned_ip, "true");
                    int i;
                    for (i = 0; i < MAX_USERS; i++) {
                        if (users[i] == null) {
                            users[i] = new UserThread(input, output, users, assigned_ip, internal, myPublicIPAddress, myMacAddress, this, userSocket);
                            users[i].start();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Could not accept user.");
            }
        }
    }

    public void closeSocket(String ip) {
        available_ip.remove(ip);
        available_ip.put(ip, "false");
    }

    public void generateAvailableIP_MAC() {
        try {
            Scanner scFile = new Scanner(new File("ip.txt"));
            while (scFile.hasNext()) {
                String line = scFile.nextLine();
                Scanner scLine = new Scanner(line);
                String temp_ip = scLine.next();
                available_ip.put(temp_ip, "false");
            }
        } catch (Exception e) {
            System.out.println("Error loading IP adresses: " + e);
        }
    }

    public String generateMAC() {
        String mac = "";
        boolean carryOn = true;

        while (carryOn) {
            Random rn = new Random();
            int n1 = rn.nextInt(256);
            int n2 = rn.nextInt(256);
            int n3 = rn.nextInt(256);
            int n4 = rn.nextInt(256);
            int n5 = rn.nextInt(256);
            int n6 = rn.nextInt(256);

            String p1 = Integer.toHexString(n1);
            String p2 = Integer.toHexString(n2);
            String p3 = Integer.toHexString(n3);
            String p4 = Integer.toHexString(n4);
            String p5 = Integer.toHexString(n5);
            String p6 = Integer.toHexString(n6);

            mac = p1 + ":" + p2 + ":" + p3 + ":" + p4 + ":" + p5 + ":" + p6;

            if (!MacAddresses.contains(mac)) {
                MacAddresses.add(mac);
                carryOn = false;
            }
        }

        return mac;
    }

    public String generateExternalIP() {
        Random rn = new Random();
        int n1 = rn.nextInt(256);
        int n2 = rn.nextInt(256);
        int n3 = rn.nextInt(256);
        int n4 = rn.nextInt(256);
        String ip = "";

        if (n1 == 192) {
            n1 += 1;
        }

        if (n2 == 168) {
            n2 += 1;
        }

        if (n3 == 0) {
            n3 += 1;
        }

        n4 = rn.nextInt(256);

        ip = Integer.toString(n1) + "." + Integer.toString(n2) + "." + Integer.toString(n3) + "." + Integer.toString(n4);
        return ip;
    }

    public static void main(String[] args) {
        NAT_Box n = new NAT_Box(8000);
        n.startNAT();
    }
}