import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        int fragment = 0;
        try {
            InputStream in = this.server.getInputStream();

            Lock lock = new ReentrantLock();

            while (true){
                byte[] buffer = new byte[8192 - UDP_Packet.n_bytes];
                int bytesRead = 0;

                lock.lock();
                bytesRead = in.read(buffer);
                lock.unlock();

                byte[] buff = Arrays.copyOf(buffer, bytesRead);

                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, buff);
                if(buff.length > 0){
                    fragment++;
                }

                DatagramSocket socket = new DatagramSocket();
                lock.lock();
                socket.send(packet.toDatagramPacket());
                lock.unlock();
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
                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 666, this.client_ID, "fechou".getBytes());
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet.toDatagramPacket());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
