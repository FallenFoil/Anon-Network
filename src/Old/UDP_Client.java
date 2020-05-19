package Old;

import java.io.IOException;
import java.net.DatagramPacket;
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
            if(c != null){
                Socket so = c.getSocket();

                System.out.println("O 1º Anon recebeu isto:\n" + new String(p.getData()));

                try {
                    TCP.send(so.getOutputStream(), p.getData());
                    this.anon.cleanClient(p.getClient_id());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("An error has occurred. Client object (client_ID = " + p.getClient_id() + ") doesn't exists. Terminating service.");
            }
        }
        else{
            try {
                Socket target = new Socket(this.anon.getTargetServer(), this.anon.getPort());

                TCP.send(target.getOutputStream(), p.getData());

                target.setSoTimeout(0);

                byte[] response = TCP.read(target.getInputStream());

                target.close();

                InetAddress from = this.packet.getAddress();

                System.out.println("O 2º Anon recebeu isto:\n" + new String(response));

                UDP_Packet udp_response = new UDP_Packet(p.getSequence() + 1, true, 1, 1, from, 6666, p.getClient_id(), response);

                UDP.send(udp_response);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
