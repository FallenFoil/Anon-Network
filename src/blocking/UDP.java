package blocking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP implements Runnable{
    private AnonGW anon;
    private DatagramSocket socket;

    public UDP(AnonGW a) throws SocketException {
        this.anon = a;
        socket = new DatagramSocket(6666);
    }

    @Override
    public void run() {
        boolean first_packet = true;
        byte[] buff = new byte[4096];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
