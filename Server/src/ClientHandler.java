import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ClientHandler extends Thread {
    final Socket s;
    final BlockingQueue<Message> queue;
    final DataInputStream dis;
    final DataOutputStream dos;

    public ClientHandler(Socket s, BlockingQueue<Message> queue, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.queue = queue;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String msg;
        long time;
        while (true) {
            try {
                msg = dis.readUTF();
                time = dis.readLong();

                // print the message
                System.out.println("Client: " + this.s + " sends message: \"" + msg + "\", to be notified in: " + time + " seconds.");

                try {
                    queue.put(new Message(msg, time * 1000));
                } catch (Exception e) {
                    System.out.println("An error occurred while trying to handle client's message: " + e.getLocalizedMessage());
                    System.out.println("Client's message aborted.");
                }
            } catch (SocketException e) {
                // an exception to handle client's early connection termination
                System.out.println("Client: " + this.s + " terminated connection early.");
                System.out.println("Trying to close this connection...");

                // try to close the connection
                try {
                    this.s.close();
                } catch (IOException ex) {
                    // if cannot close the connection
                    System.out.println("An error occurred: " + ex.getLocalizedMessage());
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
