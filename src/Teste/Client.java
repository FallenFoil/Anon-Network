import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private int id;
    private InetAddress address;
    private Socket so;

    public Client(int id, int next_sequence, InetAddress address, Socket so) {
        this.id = id;
        this.address = address;
        this.so = so;
    }

    public int getId() {
        return id;
    }


    public Socket getSocket() {
        return so;
    }
}
