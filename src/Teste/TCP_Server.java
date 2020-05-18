package Teste;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

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
                byte[] buffer = new byte[8192];

                int bytesRead = in.read(buffer);

                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, buffer);
                fragment++;
                UDP.send(packet);

                if (bytesRead == -1)
                    break;
            }

            this.anon.targetSockets.get(this.client_ID).close();

            this.anon.last_packet_sent.remove(this.client_ID);
            this.anon.targetSockets.remove(this.client_ID);
            this.anon.packets_in_queue.remove(this.client_ID);
        } catch (IOException e) {
            // Read/write failed --> connection is broken --> exit the thread
        }
    }
}
