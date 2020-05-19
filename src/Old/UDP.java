package Old;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDP implements Runnable{
    private AnonGW anon;
    private DatagramSocket socket;
    public static final int Packet_Size = 4096;

    public UDP(AnonGW a){
        this.anon = a;

        try{
            socket = new DatagramSocket(6666);
        }
        catch(SocketException e){
            e.printStackTrace();
        }
    }

    public static void send(UDP_Packet udp_response) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.send(udp_response.toDatagramPacket());
        socket.close();
    }

    @Override
    public void run() {
        while (true) {
            byte[] buff = new byte[UDP.Packet_Size];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                socket.receive(packet);

                new Thread(new UDP_Client(this.anon, packet)).start();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
