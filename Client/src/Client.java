import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

// Client class
public class Client {
    static final int PORT_NUMBER = 59090;
    public static void main(String[] args) throws IOException {
        try {

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server on port 59090
            try (Socket s = new Socket(ip, PORT_NUMBER)) {

                // obtaining input and output streams
                try (DataInputStream dis = new DataInputStream(s.getInputStream())) {
                    try (DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
                        try (Scanner scn = new Scanner(System.in)) {

                            // loop for data exchange
                            while (true) {
                                String msg;
                                String isOk;

                                // loop for message passing
                                do {
                                    System.out.println(dis.readUTF());
                                    msg = scn.nextLine();

                                    // sending the message to server
                                    dos.writeUTF(msg);

                                    // display error message if not OK
                                    if (!(isOk = dis.readUTF()).equals("OK"))
                                        System.out.println(isOk);
                                } while(!isOk.equals("OK"));

                                // close the connection on exit command and break the loop
                                if (msg.equalsIgnoreCase("Exit")) {
                                    System.out.println("Connection closed");
                                    break;
                                }

                                // time passing
                                System.out.println(dis.readUTF());
                                msg = scn.nextLine();

                                // sending time to server
                                dos.writeUTF(msg);

                                // display error message if not OK
                                if (!(isOk = dis.readUTF()).equals("OK")) {
                                    System.out.println(isOk);
                                    continue;
                                }

                                // getting the data from the server
                                String reply = dis.readUTF();
                                System.out.println("Server sends a new notification: " + reply);
                            }
                        }
                    }
                }
            } catch (ConnectException e) {
                System.out.println("An error occurred: " + e.getLocalizedMessage());
                System.out.println("Is there a server running?");
            }
            catch (SocketException e) {
                System.out.println("An error occurred: " + e.getLocalizedMessage());
                System.out.println("Is there a server running yet?");
            }
        }
        catch (Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
        }
    }
}