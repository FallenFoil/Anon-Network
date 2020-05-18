import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UDP_Client implements Runnable{
    private AnonGW anon;
    private UDP_Packet packet;
    private Socket target;

    public UDP_Client(AnonGW a, UDP_Packet p, Socket s){
        this.anon = a;
        this.packet = p;
        this.target = s;
    }

    @Override
    public void run() {
	try {
                if(this.anon.last_packet_sent.containsKey(this.packet.getClient_id())){
                    int last_packet = this.anon.last_packet_sent.get(this.packet.getClient_id());

                    if(this.packet.getFragment() != last_packet + 1){
                        this.anon.packets_in_queue.get(this.packet.getClient_id()).add(this.packet);
                    }
                    else{
                        int id = this.packet.getClient_id();
                        while(this.packet.getFragment() == last_packet + 1 && this.packet != null){
                            if(!this.target.isClosed()){
				    TCP.send(this.target.getOutputStream(), this.packet.getData());
			    }
                            last_packet++;

                            this.anon.packets_in_queue.get(this.packet.getClient_id()).remove(this.packet);

                            this.packet = this.anon.getSmallestFragment(this.packet.getClient_id());
                        }

                        this.anon.last_packet_sent.put(id, last_packet);
                    }
                }
                else{
                    if(this.packet.getFragment() == 0){
			if(!this.target.isClosed()){
                        	TCP.send(this.target.getOutputStream(), this.packet.getData());
			}
                        this.anon.last_packet_sent.put(this.packet.getClient_id(), 0);
                    }
                    else{
                        this.anon.last_packet_sent.put(this.packet.getClient_id(), -1);
                        List<UDP_Packet> list = new ArrayList<>();
                        list.add(this.packet);
                        this.anon.packets_in_queue.put(this.packet.getClient_id(), list);
                    }
                }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
