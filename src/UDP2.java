import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UDP2 implements Runnable{
    private AnonGW anon;
    private DatagramSocket socket;
    public static final int Packet_Size = 8192;

    public UDP2(AnonGW a){
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
                    if(this.anon.targetSockets.containsKey(addr) && this.anon.targetSockets.get(addr).containsKey(client_id)){
                        so = this.anon.targetSockets.get(addr).get(client_id);
                        if(p.getFragment() == this.anon.last_packet_sent.get(addr).get(client_id) + 1){
                            OutputStream out = so.getOutputStream();
                            try{
                                System.out.println("Enviar Mensagem\n");
                                out.write(p.getData());
                                out.flush();
                                System.out.println("Mensagem Enviada\n");
                            }
                            catch(SocketException e){
                                System.out.println("Cant send to socket");
                            }
                            this.anon.last_packet_sent.get(addr).put(client_id, p.getFragment());
                            while((p = this.anon.proximoEnviar(addr,client_id)) != null){
                                try{
                                    System.out.println("Enviar Mensagem\n");
                                    out.write(p.getData());
                                    out.flush();
                                    System.out.println("Mensagem Enviada\n");
                                }
                                catch(SocketException e){
                                    System.out.println("Cant send to socket");
                                }
                                this.anon.last_packet_sent.get(addr).put(client_id, p.getFragment());
                            }
                        }
                        else{
                            this.anon.packets_in_queue.get(addr).get(client_id).add(p);
                        }
                    }
                    else{
                        so = new Socket(this.anon.getTargetServer(), this.anon.getPort());

                        Map<Integer, Socket> map1 = new HashMap<>();
                        map1.put(client_id, so);


                        Map<Integer, Integer> map2 = new HashMap<>();

                        Map<Integer, List<UDP_Packet>> map3 = new HashMap<>();
                        List<UDP_Packet> list = new ArrayList<>();

                        if(p.getFragment() == 0){
                            OutputStream out = so.getOutputStream();
                            try{
                                System.out.println("Enviar Mensagem\n");
                                out.write(p.getData());
                                out.flush();
                                System.out.println("Mensagem Enviada\n");
                            }
                            catch(SocketException e){
                                System.out.println("Cant send to socket");
                            }
                            map2.put(client_id, 0);
                        }
                        else{
                            list.add(p);
                            map2.put(client_id, -1);
                        }
                        map3.put(client_id, list);

                        //Thread t = new Thread(new UDP2_Client(this.anon, so, addr, client_id));
                        //t.start();

                        this.anon.targetSockets.put(addr, map1);
                        this.anon.last_packet_sent.put(addr, map2);
                        this.anon.packets_in_queue.put(addr, map3);

                        new Thread(new TCP_Server(this.anon, so, addr, client_id)).start();
                    }
                    this.anon.nodes_lock.unlock();
                }
                else{
                    this.anon.clients_lock.lock();
                    so = this.anon.getClient(client_id).getSocket();
                    this.anon.clients_lock.unlock();

                    if(p.getFragment() == this.anon.my_clients_last_packet.get(client_id) + 1){
                        OutputStream out = so.getOutputStream();
                        try{
                            System.out.println("Enviar Mensagem\n");
                            out.write(p.getData());
                            out.flush();
                            System.out.println("Mensagem Enviada\n");
                        }
                        catch(SocketException e){
                            this.anon.cleanClient(client_id);
                            System.out.println("Cant send to socket");
                        }
                        this.anon.my_clients_last_packet.put(client_id, p.getFragment());
                        while((p = this.anon.proximoEnviar(null, client_id)) != null){
                            try{
                                System.out.println("Enviar Mensagem\n");
                                out.write(p.getData());
                                out.flush();
                                System.out.println("Mensagem Enviada\n");
                            }
                            catch(SocketException e){
                                this.anon.cleanClient(client_id);
                                System.out.println("Cant send to socket");
                            }
                            this.anon.my_clients_last_packet.put(client_id, p.getFragment());
                        }
                    }
                    else{
                        this.anon.my_clients_packets_queue.get(client_id).add(p);
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
