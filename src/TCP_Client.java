import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCP_Client implements Runnable{
    private Socket client;
    private AnonGW anon;

    public TCP_Client(AnonGW a, Socket so){
        this.anon = a;
        this.client = so;
    }

    @Override
    public void run() {
        try{
            InputStream client_in = this.client.getInputStream();

            byte[] buff = TCP.read(client_in, 0);

            InetAddress client_address = this.client.getInetAddress();

            Client c = this.anon.createNewClient(client_address, this.client);

            UDP_Packet packet = new UDP_Packet(c.getNext_sequence(), this.anon.getRandomNode(), 6666, c.getId(), buff, false);

            UDP.send(packet);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Connection closed");
    }
}
