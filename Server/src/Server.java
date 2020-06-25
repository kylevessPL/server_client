import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

public class Server {
    static final int PORT_NUMBER = 59090;
    public static void main(String[] args) {

        // initialize a new socket on port 59090
        try (ServerSocket ss = new ServerSocket(PORT_NUMBER)) {
            // infinite loop for client request
            while (true) {

                // initialize new Socket
                Socket s = null;

                // accept if a new client requests to connect
                try {

                    // listen for new connections
                    s = ss.accept();

                    // show client info on successful connection
                    System.out.println("New client connected: " + s);

                    // initialize new queue for upcoming messages
                    BlockingQueue<Message> queue = new DelayQueue<Message>();

                    // obtaining input and output streams
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                    Thread t = new ClientHandler(s, queue, dis, dos);

                    // Invoking the start() method
                    t.start();

                    Consumer consumer = new Consumer(s, queue, dos);

                    //starting consumer to consume queue from queue
                    new Thread(consumer).start();
                } catch (Exception e) {
                    System.out.println("An error occurred: " + e.getLocalizedMessage());
                    if (s != null)
                        s.close();
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            System.out.println("Cannot listen on port: " + PORT_NUMBER);
            System.out.println("Is the port busy or something?");
        }
    }
}

