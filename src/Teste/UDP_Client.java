package Teste;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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
	    try{
	        int last_packet;
            int client_id = this.packet.getClient_id();

            if(!this.packet.isResponse()){
                InetAddress from = this.packet.getFrom();

                last_packet = this.anon.last_packet_sent.get(from).get(client_id);

                if(this.packet.getFragment() == last_packet + 1){
                    while(this.packet.getFragment() == last_packet + 1 && this.packet != null){
                        if(!this.target.isClosed()){
                            TCP.send(this.target.getOutputStream(), this.packet.getData());
                        }

                        last_packet++;

                        this.anon.packets_in_queue.get(from).get(client_id).remove(this.packet);

                        this.packet = this.anon.getSmallestFragment(from, client_id);
                    }
                }
                else{
                    this.anon.packets_in_queue.get(from).get(client_id).add(this.packet);
                }
            }
            else{
                last_packet = this.anon.my_clients_last_packet.get(client_id);

                if(this.packet.getFragment() == last_packet + 1){
                    while(this.packet.getFragment() == last_packet + 1 && this.packet != null){
                        if(!this.target.isClosed()){
                            TCP.send(this.target.getOutputStream(), this.packet.getData());
                        }

                        last_packet++;

                        this.anon.my_clients_packets_queue.get(client_id).remove(this.packet);

                        this.packet = this.anon.getSmallestFragment(null, client_id);
                    }
                }
                else{
                    this.anon.my_clients_packets_queue.get(client_id).add(this.packet);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
