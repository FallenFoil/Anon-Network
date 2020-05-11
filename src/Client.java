import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private int id;
    private int next_sequence;
    private InetAddress address;
    private Socket so;

    public Client(int id, int next_sequence, InetAddress address, Socket so) {
        this.id = id;
        this.next_sequence = next_sequence;
        this.address = address;
        this.so = so;
    }

    public int getId() {
        return id;
    }

    public int getNext_sequence() {
        return next_sequence;
    }

    public Socket getSocket() {
        return so;
    }
}
