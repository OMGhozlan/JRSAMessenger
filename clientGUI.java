import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class Chatter extends JFrame implements ActionListener, Runnable {

    private String name;
    private Socket cSoc;
    private DataInputStream in;
    private DataOutputStream out;
    private JPanel panel = new JPanel();
    private JTextArea chat = new JTextArea();
    private JScrollPane scroll = new JScrollPane(chat);
    private JButton sendBtn = new JButton("Send");
    private JTextField message = new JTextField(25);
    private RSA rsa = new RSA();

    public Chatter() {
        name = JOptionPane.showInputDialog(this,
                "Enter you name:", "Name"
                , JOptionPane.QUESTION_MESSAGE);
        this.setTitle("Chat window [" + name + "]");
        this.setSize(400, 400);
        this.setLocation(200, 15);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cont = this.getContentPane();
        cont.add(BorderLayout.SOUTH, panel);
        panel.add(message);
        panel.add(sendBtn);
        cont.add(BorderLayout.CENTER, scroll);
        chat.setEditable(false);
        sendBtn.addActionListener(this);
        message.addActionListener(this);
        try {
            cSoc = new Socket("127.0.0.1", 1712);
            rsa.generateKeys(); // Generate public, private key pair
            in = new DataInputStream(cSoc.getInputStream());
            out = new DataOutputStream(cSoc.getOutputStream());
            System.out.println(cSoc.toString());
            System.out.println(rsa.getPub().toString());
            System.out.println(rsa.getMod().toString());
            System.out.println(rsa.getPrv().toString());
            out.writeUTF(name); // Send name to server
            out.writeUTF(rsa.getPub().toString()); // Send public key to server
            out.writeUTF(rsa.getMod().toString()); // Send base to server
            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Connection cannot be established", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void run() {
        try {
            while (true) {
                String data = in.readUTF(); // Read new messages
                String name = data.substring(data.indexOf('[') + 1,(data.indexOf(']'))); // Get name
                String msg = data.substring((data.indexOf(' ') + 1)); // Get message
                if(msg.matches("[0-9]+")) // Check data type
                    msg = RSA.decrypt(new BigInteger(msg), rsa.getPrv(), rsa.getMod()); // Decrypt message
                System.out.println(data.substring((data.indexOf(' ') + 1)));
                System.out.println("Dec: " + msg);
                chat.append("[" + name + "]: " + msg + "\n");
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Disconnected.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void actionPerformed(ActionEvent event) {
        String temp = message.getText();
        if (!temp.trim().equals("")) {
            chat.append("[" + name + "]: " + temp + "\n");
            try{
                String encTemp = RSA.encrypt(temp, rsa.getPrv(), rsa.getMod()).toString(); // Encrypt written text
                out.writeUTF("[" + name + "]: " + encTemp); // Send text to server
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
            message.setText("");
        }
    }
}

public class clientGUI{
    public static void main(String[] args) {
        Chatter cgui = new Chatter();
        cgui.setVisible(true);
    }
}

