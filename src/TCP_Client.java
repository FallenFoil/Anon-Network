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

            String key = this.anon.getTargetServer();

            for(int i=0; key.length() < 16; i++){
                key = key + key.charAt(i);
            }

            while (true) {
                byte[] buffer = new byte[UDP.Packet_Size - UDP_Packet.n_bytes];

                int bytesRead = in.read(buffer);

                if (bytesRead < 0) {
                    break;
                }

                byte[] buff = Arrays.copyOf(buffer, bytesRead);

                if(buff.length > 0){
                    if(buff.length > (UDP.Packet_Size - UDP_Packet.n_bytes) / 2){
                        byte[] part1 = AESEncryptionManager.encryptData(key, Arrays.copyOf(buff, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2));
                        byte[] part2 = AESEncryptionManager.encryptData(key, Arrays.copyOfRange(buff, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2, buff.length));

                        UDP_Packet packet = new UDP_Packet(false, fragment, node, 6666, client_ID, part1, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2);
                        UDP.send(packet);

                        fragment++;

                        int size = buff.length - ((UDP.Packet_Size - UDP_Packet.n_bytes) / 2);

                        packet = new UDP_Packet(false, fragment, node, 6666, client_ID, part2, size);
                        UDP.send(packet);

                        fragment++;
                    }
                    else{
                        byte[] bytesEncrypt = AESEncryptionManager.encryptData(key, buff);

                        UDP_Packet packet = new UDP_Packet( false, fragment, node, 6666, client_ID, bytesEncrypt, bytesRead);
                        UDP.send(packet);
                        fragment++;
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally{
            this.anon.cleanClient(client_ID);
        }
    }
}
