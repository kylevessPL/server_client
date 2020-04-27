import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Consumer implements Runnable {
    final Socket s;
    final BlockingQueue<Message> queue;
    final Object monitor;
    final DataOutputStream dos;
    public Consumer(Socket s, BlockingQueue<Message> queue, Object monitor, DataOutputStream dos) {
        this.s = s;
        this.queue = queue;
        this.monitor = monitor;
        this.dos = dos;
    }

    @Override
    public void run() {
        try {
            Message msg;

            // consuming upcoming messages
            while (!(msg = queue.take()).isEmpty()) {

                // send the reply to client
                dos.writeUTF(msg.toString());

                // delivery confirmation log
                System.out.println("Notification to client: " + this.s + " successfully sent: " + msg.toString());

                // notify ClientHandler that the resources are ready
                synchronized (this.monitor) {
                    monitor.notifyAll();
                }

                TimeUnit.MILLISECONDS.sleep(10);
            }
        } catch (SocketException e) {
            System.out.println("An error occurred while performing server tasks: " + e.getLocalizedMessage());

            synchronized (this.monitor) {
                monitor.notifyAll();
            }

            //stop the thread
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
        }
    }
}
