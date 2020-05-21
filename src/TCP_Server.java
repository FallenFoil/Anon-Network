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

        String key = this.anon.getTargetServer();

        for(int i=0; key.length() < 16; i++){
            key = key + key.charAt(i);
        }

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

                if(buff.length > 0){
                    if(buff.length > (UDP.Packet_Size - UDP_Packet.n_bytes) / 2){
                        byte[] part1 = AESEncryptionManager.encryptData(key, Arrays.copyOf(buff, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2));
                        byte[] part2 = AESEncryptionManager.encryptData(key, Arrays.copyOfRange(buff, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2, buff.length));

                        UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, part1, (UDP.Packet_Size - UDP_Packet.n_bytes) / 2);
                        lock.lock();
                        UDP.send(packet);
                        lock.unlock();

                        fragment++;

                        int size = buff.length - ((UDP.Packet_Size - UDP_Packet.n_bytes) / 2);

                        packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, part2, size);
                        lock.lock();
                        UDP.send(packet);
                        lock.unlock();

                        fragment++;
                    }
                    else{
                        byte[] bytesEncrypt = AESEncryptionManager.encryptData(key, buff);

                        UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, bytesEncrypt, bytesRead);
                        lock.lock();
                        UDP.send(packet);
                        lock.unlock();

                        fragment++;
                    }
                }
            }

            this.server_socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.anon.cleanClient(this.node, this.client_ID);
            try {
                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 666, this.client_ID, AESEncryptionManager.encryptData(key, "fechou".getBytes()), 6);
                UDP.send(packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
