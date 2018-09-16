import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NAT_Box {

    private final static int MAX_USERS = 255;
    private static ServerSocket serverSocket = null;
    private static Socket userSocket = null;
    private static UserThread[] users = new UserThread[MAX_USERS];
    private static String myMacAddress = "AA:AA:AA:AA:AA:AA";
    private static String myPrivateIPAddress = "192.168.0.0";
    private static String myPublicIPAddress = "69:69:69:69";
    private static Hashtable<String, String> available_ip = new Hashtable<String, String>();
    public static int number;
    private static List<String> MacAddresses = new ArrayList<String>();
    private static List<String> externalIPs = new ArrayList<String>();
    private static Queue<UserThread> userThreads = new LinkedList<>();
    public NAT_Box(int PortNumber) {
        try {
            serverSocket = new ServerSocket(PortNumber);
        } catch (IOException e) {
            System.out.println("Server could not be created");
        }
        generateAvailableIP();
        externalIPs.add(myPublicIPAddress);
        MacAddresses.add(myMacAddress);
        System.out.println("Private IP  " + myPrivateIPAddress);
        System.out.println("Public IP   " + myPublicIPAddress);
        System.out.println("MAC   " + myMacAddress);
    }

    

    public void closeSocket(String ip) {
        available_ip.remove(ip);
        available_ip.put(ip, "false");
    }

    public void generateAvailableIP() {
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

    public static String randomMACAddress(){
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){

            if(sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }


        return sb.toString();
    }
    
    public static String generateMAC() {
        String mac = "";
        boolean carryOn = true;

        while (carryOn) {
         mac = randomMACAddress();
            if (!MacAddresses.contains(mac)) {
                MacAddresses.add(mac);
                carryOn = false;
            }
        }

        return mac;
    }
    public static void removeUser(){
        if(!userThreads.isEmpty()){
            userThreads.remove();
        }
        
    }
    public static String generateExternalIP() {
       Random r = new Random();
       
       String ip = r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
       while(externalIPs.contains(ip)){
           ip = generateExternalIP();
       }
       return ip;
    }
   public static String generateMessage(String ip , String mac , int num){
        String msg = "100";
        
        if ( ip.equals(myPrivateIPAddress)){
            msg = "1";
        } else if (ip.equals(myPublicIPAddress)){
            msg = "2";
        }else if (mac.equals(myMacAddress)){
            msg = "1";
        } else if (num == 0){
            msg = "0";
        }
        for ( int i = 0 ; i <externalIPs.size(); i ++ ){
            if ( externalIPs.contains(ip)){
                msg = "0";
            }
        
        }
        return msg;
    }
   
    public static void sendInfo(String msg , String ip, String mac,int number,ObjectOutputStream output) throws IOException{
        output.writeObject(msg);
        output.writeObject(ip);
        output.writeObject(mac);
        output.writeObject(number);
    
    }
    public static void main(String[] args) {
        NAT_Box nat = new NAT_Box(8000);
//        n.startNAT();
        while (true) {
            try {
                nat.userSocket = serverSocket.accept();

                //setup input and output streams for new user
                ObjectInputStream input = new ObjectInputStream(userSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(userSocket.getOutputStream());
                System.out.println("\n **************\n A new user is connected \n ************** \n");
                //determine if user is internal or external
                String inter = "";
                
                int InEx =2;
                try {
                    InEx = (int) input.readObject();
                } catch (ClassNotFoundException ex) {
                    System.err.println("could not read object : "+ ex);
                }
                String assigned_ip = "empty";//assume no addresses are available
                
                boolean internal = true;
                if (InEx == 0) {
                    internal = true;
                     for (int i = 1; i < MAX_USERS; i++) {
                         String temp = available_ip.get("192.168.0." + i);
                         String ass = "192.168.0." + i;
                        if (temp.equals("false")) {
                            assigned_ip = ass;
                            number = i;
                            break;
                        }
                    }
                } else if (InEx == 1) {
                    internal = false;
                    number = 0;
                    assigned_ip = generateExternalIP();
                    while (externalIPs.contains(assigned_ip)) {
                        assigned_ip = generateExternalIP();
                    }
                }
                String assigned_mac = generateMAC();
                String message = generateMessage(assigned_ip, assigned_mac, number);
                
                sendInfo(message , assigned_ip, assigned_mac,number , output);
                
                if (assigned_ip.equals("empty")) {
                    output.close();
                    userSocket.close();
                } else {
                    available_ip.put(assigned_ip, "true");
                    int i;
                    for (i = 0; i < MAX_USERS; i++) {
                        if (users[i] == null) {
                            users[i] = new UserThread(input, output, users, assigned_ip, internal, myPublicIPAddress, myMacAddress, nat, userSocket,i,InEx);
                            users[i].start();
                            userThreads.add(users[i]);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not accept user."+ e);
            }
        }
    }
}