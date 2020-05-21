package Sem_Encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TCP_Server implements Runnable{
    private AnonGW anon;
    private Socket server_socket;
    private InetAddress node;
    private int client_ID;

    public TCP_Server(AnonGW anon, Socket so, InetAddress node, int id){
        this.anon = anon;
        this.server_socket = so;
        this.node = node;
        this.client_ID = id;
    }

    @Override
    public void run() {
        int fragment = 0;

        try {
            InputStream in = this.server_socket.getInputStream();
            Lock lock = new ReentrantLock();

            while (true){
                byte[] buffer = new byte[UDP.Packet_Size - UDP_Packet.n_bytes];

                lock.lock();
                int bytesRead = in.read(buffer);
                lock.unlock();

                if (bytesRead < 0) {
                    break;
                }

                byte[] buff = Arrays.copyOf(buffer, bytesRead);

                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, buff);
                if(buff.length > 0){
                    fragment++;
                }

                lock.lock();
                UDP.send(packet);
                lock.unlock();
            }

            this.server_socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.anon.cleanClient(this.node, this.client_ID);
            try {
                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 666, this.client_ID, "fechou".getBytes());
                UDP.send(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
