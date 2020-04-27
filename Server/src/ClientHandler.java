import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ClientHandler extends Thread {
    final Socket s;
    final BlockingQueue<Message> queue;
    final Object monitor;
    final DataInputStream dis;
    final DataOutputStream dos;

    public ClientHandler(Socket s, BlockingQueue<Message> queue, Object monitor, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.queue = queue;
        this.monitor = monitor;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String msg = null;
        long time = 0;
        while (true) {
            try {
                // validates the message from client
                int msg_ok = 1;

                // validates the message from client
                int time_ok = 1;
                do {
                    // encourage the client to send the message
                    dos.writeUTF("Type your message" + (msg_ok == 0 ? " again" : "") + ": ");
                    msg_ok = 0;

                    // get the message from client
                    try {
                        if ((msg = dis.readUTF()).isEmpty()) {

                            // throw an exception if message is empty
                            throw new MessageNotValidException();
                        }
                    } catch (MessageNotValidException e) {
                        dos.writeUTF(e.getLocalizedMessage());
                        System.out.println("Client: " + this.s + " wanted to send a message but encountered an error: " + e.getLocalizedMessage());
                        System.out.println("Requested the client to type the message again.");
                        continue;
                    }
                    msg_ok = 1;
                    dos.writeUTF("OK");
                } while (msg_ok != 1);

                // close client connection if requests
                if (msg.equalsIgnoreCase("Exit")) {
                    System.out.println("Closing this connection...");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // encourage the client to send the message
                dos.writeUTF("Type time for the message to get you notified (in seconds): ");

                // get the message from client
                try {
                    try {
                        if ((time = Long.parseLong(dis.readUTF())) < 0)

                            // throw an exception if time of not a valid format (time < 0)
                            throw new TimeNotValidException();
                    } catch(NumberFormatException e) {

                        // throw an exception if time of not a valid format (time not Integer type)
                        throw new TimeNotValidException();
                    }
                } catch (TimeNotValidException e) {
                    dos.writeUTF(e.getLocalizedMessage());
                    System.out.println("Client: " + this.s + " wanted to send a message but encountered an error: " + e.getLocalizedMessage());
                    System.out.println("Client's message aborted.");
                    continue;
                }
                dos.writeUTF("OK");

                // print the message
                System.out.println("Client: " + this.s + " sends message: \"" + msg + "\", to be notified in: " + time + " seconds.");

                try {
                    queue.put(new Message(msg, time * 1000));
                } catch (Exception e) {
                    dos.writeUTF("Server is busy. Please try again later.");
                    System.out.println("An error occurred while trying to handle client's message: " + e.getLocalizedMessage());
                    System.out.println("Client's message aborted.");
                    continue;
                }

                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        dos.writeUTF("Server is busy. Please try again later.");
                        System.out.println("An error occurred while trying to handle client's message: " + e.getLocalizedMessage());
                        System.out.println("Client's message aborted.");
                    }
                }
            } catch (SocketException e) {

                // an exception to handle client's early connection termination
                System.out.println("Client: " + this.s + " terminated connection early.");
                System.out.println("Trying to close this connection...");

                // try to close the connection
                try {
                    this.s.close();
                } catch (IOException ioException) {

                    // if cannot close the connection
                    System.out.println("An error occurred: " + e.getLocalizedMessage());
                    return;
                }

                // on successful closure
                System.out.println("Connection closed");
                break;
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getLocalizedMessage());
                return;
            }
        }

        try {

            // closing resources
            this.dis.close();
            this.dos.close();
        } catch(IOException e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
        }
    }
}
