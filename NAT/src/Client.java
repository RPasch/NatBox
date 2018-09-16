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
    public int number;
    private static boolean internal;
    public String givenIP;
    private String macAddress;
    private static String natIP;
    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static ObjectOutputStream os = null;
    // The input stream
    public static ObjectInputStream is = null;
    
    private static final int portNumber = 8000;

    private static BufferedReader inputLine = null;
    public boolean closed = false;
    private boolean joinedNetwork = false;
    private Queue<Paquet> paquets = new LinkedList<>();
   
    public static void setEverything(){
        inputLine = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("NAT-Box IP?");
            try {
                natIP = inputLine.readLine();
                String temp = "";
                boolean carryOn = true;

                while (carryOn) {
                    System.out.println("Internal or External client?");
                    temp = inputLine.readLine();
                    switch (temp) {
                        case "internal":
                            internal = true;
                            carryOn = false;
                            break;
                        case "external":
                            internal = false;
                            carryOn = false;
                            break;
                        default:
                            System.out.println("Only enter either 'internal' or 'external'");
                            break;
                    }
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }

            /*
             * Open a socket on a given host and port. Open input and output streams.
             */
            try {
                clientSocket = new Socket(natIP, portNumber);
                os = new ObjectOutputStream(clientSocket.getOutputStream());
                is = new ObjectInputStream(clientSocket.getInputStream());
            } catch (UnknownHostException e) {
                System.err.println("Host : " + natIP+ " does not exist \n error :"+ e);
            } catch (IOException e) {
                System.err.println("I/O error with host :  " + natIP + " error : "+ e);
            }
    }
    public Client() {

        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */
        boolean allGood = (clientSocket != null) && (os != null) && (is != null);
        
        if (allGood) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new ListenerThread(this)).start();

                int InEx ;
                if (internal) {
                    InEx = 0;
                } else {
                    InEx = 1;
                }
                
                os.writeObject(InEx);

                while (!closed) {
                    while (!isJoined()) {}
                    
                    if (givenIP.equals("empty")) {
                        System.out.println("Could not connect to server");
                    }

                    System.out.println("Enter destination IP address : ");
                    String dest = inputLine.readLine();
                    String payload;
                    Paquet send ;
                    if (dest.equals("exit")) {
                        payload = "";
                        send = new Paquet(givenIP, dest, macAddress, portNumber, payload, number, InEx);
                        break;
                    } else {
                        System.out.println("Enter message:");
                        payload = inputLine.readLine();
                        send = new Paquet(givenIP, dest, macAddress, portNumber, payload,number,InEx);
                    }
                    paquets.add(send);
                    os.writeObject(send);

                }
                
                /*
                 * Close the output stream, close the input stream, close the socket.
                 */
                System.out.println("You are disconnected");
                os.close();
                is.close();
                clientSocket.close();

            } catch (IOException e) {
                System.err.println("Could not create client " + e);
            }
        }
    }

    public synchronized boolean isJoined() {
        return joinedNetwork;
    }

//    public String getIpGiven() {
//        return givenIP;
//    }

    public void setIpGiven(String ipGiven) {
        this.givenIP = ipGiven;
    }

//    public String getMacAddr() {
//        return macAddress;
//    }

    public void setMacAddr(String macAddr) {
        this.macAddress = macAddr;
    }

//    public ObjectInputStream getIs() {
//        return is;
//    }

    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    public void setJoinedNetwork(boolean joinedNetwork) {
        this.joinedNetwork = joinedNetwork;
    }

//    public boolean isClosed() {
//        return closed;
//    }

    public synchronized void setClosed(boolean closed) {
        this.closed = closed;
    }

    public static void main(String[] args) {
        setEverything();

        Client n = new Client();
    }
}