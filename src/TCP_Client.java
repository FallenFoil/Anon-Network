import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class TCP_Client implements Runnable{
    private AnonGW anon;
    private Socket client;

    public TCP_Client(AnonGW anon, Socket so){
        this.anon = anon;
        this.client = so;
    }

    @Override
    public void run() {
        InetAddress client_address = this.client.getInetAddress();

        Client c = this.anon.createNewClient(client_address, this.client);
        int id = c.getId();
        try {
            InputStream in = this.client.getInputStream();
            InetAddress node = this.anon.getRandomNode();

            int fragment = 0;

            while (true) {
                byte[] buffer = new byte[8192 - UDP_Packet.n_bytes];

                int bytesRead = in.read(buffer);

                UDP_Packet packet = new UDP_Packet( false, fragment, node, 6666, c.getId(), buffer);
                fragment++;

                //UDP.send(packet);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet.toDatagramPacket());
                socket.close();

                if (bytesRead == -1)
                    this.anon.cleanClient(c.getId());
                    break;
            }
        }
        catch (IOException e) {
            //this.anon.cleanClient(c.getId());
        }
        finally {
            this.anon.my_clients.remove(id);
            this.anon.my_clients_packets_queue.remove(id);
            this.anon.my_clients_last_packet.remove(id);
        }
    }
}
