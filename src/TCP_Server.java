import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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

                byte[] bytesEncrypt = Encrypter.encryptData(this.anon.getTargetServer(), buff);

                final int limit = UDP.Packet_Size - UDP_Packet.n_bytes;

                assert bytesEncrypt != null;
                while(bytesEncrypt.length > limit){
                    byte[] send = Arrays.copyOf(bytesEncrypt, limit);

                    UDP_Packet packet = new UDP_Packet( true, fragment, this.node, 6666, this.client_ID, send);
                    if(buff.length > 0){
                        fragment++;
                    }

                    lock.lock();
                    UDP.send(packet);
                    lock.unlock();

                    bytesEncrypt = Arrays.copyOfRange(bytesEncrypt, limit, bytesEncrypt.length);
                }

                //UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, buff);
                UDP_Packet packet = new UDP_Packet(true, fragment, this.node, 6666, this.client_ID, bytesEncrypt);
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
