import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Message implements Delayed {
    private final String msg;
    private long time = 0;

    public Message(String msg, long time) {
        this.msg = msg;
        this.time = System.currentTimeMillis() + time;
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.time - ((Message) o).getTime());
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = time - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    public long getTime() {
        return time;
    }

    public String getMessage() {
        return msg;
    }

    public boolean isEmpty() {
        return msg.isEmpty();
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return "[message=" + msg + ", time=" + formatter.format(date) + "]";
    }
}
