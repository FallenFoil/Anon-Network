package Teste;

import java.io.IOException;
import java.net.*;

public class UDP_Reader implements Runnable{
    private AnonGW anon;
    private DatagramSocket socket;
    public static final int Packet_Size = 8192;

    public UDP_Reader(AnonGW a){
        this.anon = a;

        try{
            socket = new DatagramSocket(6666);
        }
        catch(SocketException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] buff = new byte[UDP_Reader.Packet_Size];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                socket.receive(packet);

                UDP_Packet p = new UDP_Packet(packet);

                this.anon.lock_tcp.lock();
                this.anon.queue_tcp.add(p);
                this.anon.lock_tcp.unlock();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
