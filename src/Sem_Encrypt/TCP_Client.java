package Sem_Encrypt;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class TCP_Client implements Runnable{
    private AnonGW anon;
    private Socket client_socket;

    public TCP_Client(AnonGW anon, Socket so){
        this.anon = anon;
        this.client_socket = so;
    }

    @Override
    public void run() {
        int client_ID = this.anon.createNewClient(this.client_socket);
        InetAddress node = this.anon.getRandomNode();

        int fragment = 0;

        try {
            InputStream in = this.client_socket.getInputStream();

            while (true) {
                byte[] buffer = new byte[UDP.Packet_Size - UDP_Packet.n_bytes];

                int bytesRead = in.read(buffer);

                if (bytesRead < 0) {
                    break;
                }

                byte[] buff = Arrays.copyOf(buffer, bytesRead);


                UDP_Packet packet = new UDP_Packet( false, fragment, node, 6666, client_ID, buff);
                if(buff.length > 0){
                    fragment++;
                }

                UDP.send(packet);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally{
            this.anon.cleanClient(client_ID);
        }
    }
}
