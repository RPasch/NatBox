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

    private int maxUsers = 255;
    private ServerSocket service = null;
    private Socket userSocket = null;
    private UserThread[] users = new UserThread[maxUsers];
    private String myMacAddr = "AA:AA:AA:AA:AA:AA";
    private String myPrivateIPAddr = "192.168.0.0";
    private String myPublicIPAddr = "69:69:69:69";
    private Hashtable<String, String> available_ip = new Hashtable<String, String>();

    private List<String> MacAddresses = new ArrayList<String>();
    private List<String> externalIPs = new ArrayList<String>();

    public NAT_Box(int PortNumber) {
        try {
            service = new ServerSocket(PortNumber);
        } catch (IOException e) {
            System.out.println("Could not create server");
        }
        generateAvailableIP_MAC();
        externalIPs.add(myPublicIPAddr);
        MacAddresses.add(myMacAddr);
        System.out.println("My private IP Address: " + myPrivateIPAddr);
        System.out.println("My public IP Address: " + myPublicIPAddr);
        System.out.println("My MAC Address: " + myMacAddr);
    }

    public void startNAT() {
        //create a new connection for each user
        while (true) {
            try {
                userSocket = service.accept();

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
                String assigned_mac = Generate_mac();
                if (internal) {
                    //assign internal address
                    for (int i = 1; i < maxUsers; i++) {
                        if (available_ip.get("192.168.0." + i).equals("false")) {
                            assigned_ip = "192.168.0." + i;
                            break;
                        }
                    }

                } else {
                    //assign external address

                    assigned_ip = genExternalIP();
                    while (externalIPs.contains(assigned_ip)) {
                        assigned_ip = genExternalIP();
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
                    for (i = 0; i < maxUsers; i++) {
                        if (users[i] == null) {
                            users[i] = new UserThread(input, output, users, assigned_ip, internal, myPublicIPAddr, myMacAddr, this, userSocket);
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
            System.out.println("Error while loading ip adresses: " + e);
        }
    }

    public String Generate_mac() {
        String mac;
        while (true) {
            Random rn = new Random();
            int n1 = rn.nextInt(256);
            int n2 = rn.nextInt(256);
            int n3 = rn.nextInt(256);
            int n4 = rn.nextInt(256);
            int n5 = rn.nextInt(256);
            int n6 = rn.nextInt(256);
            mac = "";

            String p1 = Integer.toHexString(n1);
            String p2 = Integer.toHexString(n2);
            String p3 = Integer.toHexString(n3);
            String p4 = Integer.toHexString(n4);
            String p5 = Integer.toHexString(n5);
            String p6 = Integer.toHexString(n6);

            mac = p1 + ":" + p2 + ":" + p3 + ":" + p4 + ":" + p5 + ":" + p6;

            if (!MacAddresses.contains(mac)) {
                MacAddresses.add(mac);
                break;
            }
        }
        return mac;
    }

    public String genExternalIP() {
        Random rn = new Random();
        int n1 = rn.nextInt(256);
        int n2 = rn.nextInt(256);
        int n3 = rn.nextInt(256);
        int n4 = rn.nextInt(256);
        String ip = "";

        while (n1 == 192) {
            n1 = rn.nextInt(256);
        }

        while (n2 == 168) {
            n2 = rn.nextInt(256);
        }

        while (n3 == 0) {
            n3 = rn.nextInt(256);
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