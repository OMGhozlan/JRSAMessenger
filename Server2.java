import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

class ConnMngr extends Thread {

    private Socket cSoc; // Socket
    private String data; // Incoming message
    private DataInputStream in; // Input stream (reading incoming data)
    private DataOutputStream out; // Output stream (sending data)
    private static ArrayList<String> names = new ArrayList<String>(); // Client name list
    private static ArrayList<Socket> instances = new ArrayList<Socket>(); // Manager instances
    private static ArrayList<BigInteger> keys = new ArrayList<BigInteger>(); // Public key list
    private static ArrayList<BigInteger> bases = new ArrayList<BigInteger>(); // Base (mod) value list

    public ConnMngr(Socket cSoc, String name, BigInteger key, BigInteger mod) {
        this.cSoc = cSoc;
        instances.add(this.cSoc);
        names.add(name);
        keys.add(key);
        bases.add(mod);
    }

    public static ArrayList getInstances() {
        return instances;
    }

    public String getData() {
        return this.data;
    }

    public void run() { // Connection handler
        try {
            in = new DataInputStream(cSoc.getInputStream()); // Initialize reader
            out = new DataOutputStream(cSoc.getOutputStream()); // Initialize writer
            while (true) {
                data = in.readUTF(); // Read from reader
                String name = data.substring(data.indexOf('[') + 1, (data.indexOf(']'))); // Get sender
                String msg = data.substring((data.indexOf(' ') + 1)); // Get message
                int src = 0;
                for (int i = 0; i < names.size(); i++) { // Use sender's public key to decrypt incoming message
                    if (names.get(i).equals(name)) { // Match name
                        msg = RSA.decrypt(new BigInteger(msg), keys.get(i), bases.get(i)); // Decryption
                        src = i;
                    }
                }
                String to = msg.contains("<") && msg.contains(">") ?
                        msg.substring(msg.indexOf('<') + 1, (msg.indexOf('>'))) : ""; // Check if message is private
                System.out.println("Decryption@Server: " + msg);
                msg = to.isEmpty() ? msg : msg.substring(msg.indexOf('>') + 1); // Strip message of target clien
                data = msg;
                int dest = 0;
                if (!to.isEmpty()) { // Get index of target client to retrieve corresponding key and socket
                    while (!(names.get(dest).equals(to))) {
                        dest++;
                    }
                }
                for (int i = 0; i < instances.size(); i++) {
                    Socket temp = instances.get(i);
                    if (!temp.equals(this.cSoc)) {
                        DataOutputStream of = new DataOutputStream(temp.getOutputStream());
                        if (!to.isEmpty()) { // If message is private, use target's public key to encrypt message and broadcast
                            System.out.println("Private message to " + names.get(dest));
                            if (!names.get(i).equals(names.get(dest)))
                                msg = RSA.encrypt(msg, keys.get(dest), bases.get(dest)).toString();
                        }
                        System.out.println(msg);
                        of.writeUTF("[" + name + "]: " + msg); // Send message
                    }
                }
                System.out.println("Received data from " + cSoc.toString() + "\n" + data);

                if (data.equals("KillNull")) {
                    cSoc.close();
                    out.close();
                    in.close();
                    break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

public class Server2 {
    public static void main(String[] args) {
        try {
            ServerSocket sSoc = new ServerSocket(1712); // Someone's birthday
            while (true) {
                System.out.println("Server listening on port " + Integer.toString(1712) + ".");
                Socket conn = sSoc.accept(); // Accept connection
                DataInputStream init = new DataInputStream(conn.getInputStream());
                String name = init.readUTF();  // Retrieve user's name
                BigInteger pubKey = new BigInteger(init.readUTF()); // Retrieve user's private key
                BigInteger mod = new BigInteger(init.readUTF()); // Retrieve user's base
                System.out.println("Public key for " + name + " is " + pubKey.toString());
                ConnMngr mgr = new ConnMngr(conn, name, pubKey, mod);
                mgr.start();
            }
        } catch (IOException ioe) {
            System.out.println("Connection terminated!");
        }
    }
}
