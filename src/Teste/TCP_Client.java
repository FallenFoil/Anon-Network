package Teste;

import java.io.IOException;
import java.io.InputStream;
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
        byte[] buffer = new byte[8192];
        try {
            InetAddress client_address = this.client.getInetAddress();

            Client c = this.anon.createNewClient(client_address, this.client);

            InputStream in = this.client.getInputStream();

            InetAddress node = this.anon.getRandomNode();

            int fragment = 0;

            while (true) {
                int bytesRead = in.read(buffer);

                UDP_Packet packet = new UDP_Packet( false, fragment, node, 6666, c.getId(), buffer);
                fragment++;
                UDP.send(packet);

                if (bytesRead == -1)
                    break;
            }

            this.anon.cleanClient(c.getId());
        }
        catch (IOException e) {
            // Read/write failed --> connection is broken --> exit the thread
        }
    }
}
