import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class MessageNotValidException extends Exception {
    public MessageNotValidException() {
        super("no message provided");
    }
}

class TimeNotValidException extends Exception {
    public TimeNotValidException() {
        super("not a valid time format");
    }
}

// Client class
public class Client {
    static final int PORT_NUMBER = 59090;
    static String currentMsg = null;

    public static void main(String[] args) throws IOException {
        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server on port 59090
            try (Socket s = new Socket(ip, PORT_NUMBER)) {

                new Thread(() -> {
                    try (DataInputStream dis = new DataInputStream(s.getInputStream())) {
                        while (true) {
                            String message = dis.readUTF();

                            System.out.println("Server sends a new notification: " + message);

                            // repeat last message
                            if(currentMsg != null) {
                                System.out.println(currentMsg);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("An error occurred: " + e.getLocalizedMessage());
                        System.out.println("Is there a server running yet?");

                        //stop the thread
                        Thread.currentThread().interrupt();
                    }
                }).start();

                Scanner scn = new Scanner(System.in);

                // obtaining output stream
                try {
                    try (DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
                        // loop for data exchange
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            scn = new Scanner(System.in);
                            try {
                                String msg;
                                long time;

                                currentMsg = "Type your message: ";
                                System.out.println(currentMsg);
                                msg = scn.nextLine();

                                // if msg not valid
                                if (msg.isEmpty()) {
                                    throw new MessageNotValidException();
                                }

                                currentMsg = "Type time: ";
                                System.out.println(currentMsg);
                                time = scn.nextLong();

                                try {
                                    if (time < 0) {
                                        // throw an exception if time of not a valid format
                                        throw new TimeNotValidException();
                                    }
                                } catch (NumberFormatException e) {
                                    // throw an exception if time of not a valid format (time not Integer type)
                                    throw new TimeNotValidException();
                                }

                                // sending msg to server
                                dos.writeUTF(msg);
                                dos.writeLong(time);
                            } catch(MessageNotValidException | TimeNotValidException e) {
                                System.out.println("Error: " + e.getLocalizedMessage());
                            }
                        }
                    }
                } catch (ConnectException e) {
                    System.out.println("An error occurred: " + e.getLocalizedMessage());
                    System.out.println("Is there a server running?");
                } catch (SocketException e) {
                    System.out.println("An error occurred: " + e.getLocalizedMessage());
                    System.out.println("Is there a server running yet?");
                } finally {
                    scn.close();
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
        }
    }
}