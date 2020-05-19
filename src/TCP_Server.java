import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class TCP_Server implements Runnable{
    private AnonGW anon;
    private Socket server;
    private InetAddress node;
    private int client_ID;

    public TCP_Server(AnonGW anon, Socket so, InetAddress node, int id){
        this.anon = anon;
        this.server = so;
        this.node = node;
        this.client_ID = id;
    }

    @Override
    public void run() {
        try {
            InputStream in = this.server.getInputStream();

            int fragment = 0;

            while (true){
                byte[] buffer = new byte[8192 - UDP_Packet.n_bytes];

                int bytesRead = in.read(buffer);

                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, buffer);
                fragment++;

                DatagramSocket socket = new DatagramSocket();
                socket.send(packet.toDatagramPacket());
                socket.close();

                if (bytesRead == -1)
                    break;
            }

            this.anon.targetSockets.get(this.node).get(this.client_ID).close();
        } catch (IOException e) {
            // Read/write failed --> connection is broken --> exit the thread
        }
        finally {
            this.anon.last_packet_sent.get(this.node).remove(this.client_ID);
            this.anon.targetSockets.get(this.node).remove(this.client_ID);
            this.anon.packets_in_queue.get(this.node).remove(this.client_ID);
            try {
                UDP_Packet packet = new UDP_Packet(true, -1, this.node, 666, this.client_ID, new byte[0]);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet.toDatagramPacket());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
