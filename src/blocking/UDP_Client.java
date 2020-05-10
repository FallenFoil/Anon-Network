package blocking;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class UDP_Client implements Runnable{
    private AnonGW anon;
    private DatagramPacket packet;

    public UDP_Client(AnonGW a, DatagramPacket p){
        this.anon = a;
        this.packet = p;
    }

    @Override
    public void run() {
        UDP_Packet p = new UDP_Packet(this.packet);

        if(p.isResponse()){
            Client c = this.anon.getClient(p.getClient_id());
            Socket so = c.getSocket();

            try {
                TCP.send(so.getOutputStream(), p.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                Socket target = new Socket(this.anon.getTargetServer(), this.anon.getPort());

                TCP.send(target.getOutputStream(), p.getData());

                byte[] response = TCP.read(target.getInputStream(), 1);

                target.close();

                InetAddress from = this.packet.getAddress();
                UDP_Packet udp_response = new UDP_Packet(p.getSequence() + 1, from, 6666, p.getClient_id(), response, true);

                UDP.send(udp_response);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
