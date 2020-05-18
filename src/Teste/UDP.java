package Teste;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UDP implements Runnable{
    private AnonGW anon;
    private DatagramSocket socket;
    public static final int Packet_Size = 8192;

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

                UDP_Packet p = new UDP_Packet(packet);

                Socket so = null;

                InetAddress addr = packet.getAddress();
                int client_id = p.getClient_id();

                p.setFrom_address(addr);

                if(!p.isResponse()){
                    this.anon.nodes_lock.lock();
                    if(this.anon.targetSockets.containsKey(addr)){
                        so = this.anon.targetSockets.get(addr).get(client_id);
                        this.anon.nodes_lock.unlock();
                    }
                    else{
                        this.anon.nodes_lock.unlock();
                        so = new Socket(this.anon.getTargetServer(), this.anon.getPort());

                        Map<Integer, Socket> map1 = new HashMap<>();
                        map1.put(client_id, so);
                        this.anon.nodes_lock.lock();
                        this.anon.targetSockets.put(addr, map1);
                        this.anon.nodes_lock.unlock();

                        Map<Integer, Integer> map2 = new HashMap<>();
                        map2.put(client_id, -1);
                        this.anon.nodes_lock.lock();
                        this.anon.last_packet_sent.put(addr, map2);
                        this.anon.nodes_lock.unlock();

                        Map<Integer, List<UDP_Packet>> map3 = new HashMap<>();
                        List<UDP_Packet> list = new ArrayList<>();
                        map3.put(client_id, list);
                        this.anon.nodes_lock.lock();
                        this.anon.packets_in_queue.put(addr, map3);
                        this.anon.nodes_lock.unlock();

                        new Thread(new TCP_Server(this.anon, so, addr, client_id)).start();
                    }
                }
                else{
                    this.anon.nodes_lock.lock();
                    so = this.anon.getClient(client_id).getSocket();
                    this.anon.nodes_lock.unlock();
                }

                new Thread(new UDP_Client(this.anon, p, so)).start();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
