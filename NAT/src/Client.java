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
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {

    private boolean internal;
    private String ipGiven;
    private String macAddr;
    private String natIP;
    // The client socket
    private Socket clientSocket = null;
    // The output stream
    private ObjectOutputStream os = null;
    // The input stream
    private ObjectInputStream is = null;

    private BufferedReader inputLine = null;
    private boolean closed = false;
    private boolean joinedNetwork = false;

    public Client() {
        inputLine = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input NAT-box IP address");
        try {
            natIP = inputLine.readLine();
            String temp;
            while (true) {
                System.out.println("Are you an internal or external client?");
                temp = inputLine.readLine();
                if (temp.equals("internal") || temp.equals("external")) {
                    break;
                } else {
                    System.out.println("Only enter either 'internal' or 'external'");
                }
            }
            if (temp.equals("internal")) {
                internal = true;
            } else {
                internal = false;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        int portNumber = 8000;

        /*
        * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(natIP, portNumber);
            os = new ObjectOutputStream(clientSocket.getOutputStream());
            is = new ObjectInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + natIP);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + natIP);
        }

        /*
        * If everything has been initialized then we want to write some data to the
        * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && os != null && is != null) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new listenerThread(this)).start();

                String inter = "external";
                if (internal) {
                    inter = "internal";
                }
                os.writeObject(inter);

                while (!closed) {

                    while (!isJoined()) {

                    }
                    if (ipGiven.equals("none")) {
                        System.out.println("Server is too busy. Try again later.");
                    }

                    System.out.println("Enter destination IP:");
                    String dest = inputLine.readLine();
                    String payload;
                    if (dest.equals("quit")) {
                        payload = "";

                        Paquet send = new Paquet(ipGiven, dest, macAddr, portNumber, payload);
                        os.writeObject(send);
                        break;
                    } else {
                        System.out.println("Enter message:");
                        payload = inputLine.readLine();
                        Paquet send = new Paquet(ipGiven, dest, macAddr, portNumber, payload);
                        os.writeObject(send);
                    }

                }
                /*
            * Close the output stream, close the input stream, close the socket.
                 */
                System.out.println("Goodbye");
                os.close();
                is.close();
                clientSocket.close();

            } catch (IOException e) {

            }
        }
    }

    public synchronized boolean isJoined() {
        return joinedNetwork;
    }

    public String getIpGiven() {
        return ipGiven;
    }

    public void setIpGiven(String ipGiven) {
        this.ipGiven = ipGiven;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public ObjectInputStream getIs() {
        return is;
    }

    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    public void setJoinedNetwork(boolean joinedNetwork) {
        this.joinedNetwork = joinedNetwork;
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized void setClosed(boolean closed) {
        this.closed = closed;
    }

    public static void main(String[] args) {
        Client n = new Client();
    }
}