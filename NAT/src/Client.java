
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {

    /**
     * max number of clients
     *
     * @param num
     */
    public int num;
    /**
     * whether the user is internal or external
     *
     * @param internal
     */
    private static boolean internal;
    /**
     * The generated IP of the client
     *
     * @param givenIP
     */
    public String givenIP;
    /**
     * The generated MAC of the client
     *
     * @param MACgiven
     */
    private String MACgiven;
    /**
     * The generated IP of the NATbox
     *
     * @param NATboxIP
     */
    private static String NATboxIP;
    private static Socket clientSocket = null;
    private static ObjectOutputStream outStream = null;
    public static ObjectInputStream inStream = null;
    private static final int port = 8000;
    private static BufferedReader inLine = null;
    public boolean isActive = false;
    private boolean isIn = false;
    private Queue<Paquet> paquets = new LinkedList<>();

    /**
     * This method gets all the input from the user and set all associated
     * variables. It creates all sockets and input/output streams as well.
     */
    public static void setEverything() {
        inLine = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("NAT-Box IP?");
        try {
            NATboxIP = inLine.readLine();
            String temp = "";
            boolean cont = true;

            while (cont) {
                System.out.println("Internal or External client?");
                temp = inLine.readLine();
                switch (temp) {
                    case "internal":
                        internal = true;
                        cont = false;
                        break;
                    case "external":
                        internal = false;
                        cont = false;
                        break;
                    default:
                        System.out.println("Only enter either 'internal' or 'external'");
                        break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }

        try {
            clientSocket = new Socket(NATboxIP, port);
            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Host : " + NATboxIP + " does not exist \n error :" + e);
        } catch (IOException e) {
            System.err.println("I/O error with host :  " + NATboxIP + " error : " + e);
        }
    }

    /**
     * The constructor of the Client. It starts the listener thread and receives
     * all messages. It talks directly to the NatBox.
     */
    public Client() {

        boolean allGood = (clientSocket != null) && (outStream != null) && (inStream != null);

        if (allGood) {
            try {

                new Thread(new ListenerThread(this)).start();

                int InEx;
                if (internal) {
                    InEx = 0;
                } else {
                    InEx = 1;
                }

                outStream.writeObject(InEx);

                while (!isActive) {
                    while (!isJoined()) {
                    }

                    if (givenIP.equals("empty")) {
                        System.out.println("Could not connect to server");
                    }

                    System.out.println("Enter destination IP address : ");
                    String recvIP = inLine.readLine();
                    String msg;
                    Paquet toSend;
                    if (recvIP.equals("exit")) {
                        msg = "";
                        toSend = new Paquet(givenIP, recvIP, MACgiven, port, msg, num, InEx);
                        break;
                    } else {
                        System.out.println("Enter message:");
                        msg = inLine.readLine();
                        toSend = new Paquet(givenIP, recvIP, MACgiven, port, msg, num, InEx);
                    }
                    paquets.add(toSend);
                    outStream.writeObject(toSend);

                }

                System.out.println("You are disconnected");
                outStream.close();
                inStream.close();
                clientSocket.close();

            } catch (IOException e) {
                System.err.println("Could not create client " + e);
            }
        }
    }

    /**
     * It checks if the client has joined the NatBox
     *
     * @return
     */
    public synchronized boolean isJoined() {
        return isIn;
    }

    /**
     * It sets the generated IP address of the client
     *
     * @param ipGiven
     */
    public void setIpGiven(String ipGiven) {
        this.givenIP = ipGiven;
    }

    /**
     * It sets the generated MAC address of the client
     *
     * @param macAddr
     */
    public void setMacAddr(String macAddr) {
        this.MACgiven = macAddr;
    }

    /**
     * It sets the input stream of the client
     *
     * @param inStream
     */
//    public void setInStream(ObjectInputStream inStream) {
//        this.inStream = inStream;
//    }
    /**
     *
     * @param isIn
     */
    public void setIsIn(boolean isIn) {
        this.isIn = isIn;
    }

    /**
     * Sets the IsActive variable
     *
     * @param isActive
     */

    public synchronized void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * The main method that create an instance of Client
     *
     * @param args
     */
    public static void main(String[] args) {
        setEverything();

        Client n = new Client();
    }
}
