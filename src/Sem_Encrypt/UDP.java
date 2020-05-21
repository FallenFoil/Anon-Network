package Sem_Encrypt;

import java.io.IOException;
import java.io.OutputStream;
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

    public static void send(UDP_Packet packet) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet.toDatagramPacket());
        socket.close();
    }

    @Override
    public void run() {
        while (true) {
            byte[] buff = new byte[UDP.Packet_Size];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                socket.receive(packet);

                UDP_Packet p = new UDP_Packet(packet, this.anon.getTargetServer(), this.anon.getPort());

                if(p.getData_size() == 0){
                    break;
                }

                Socket so = null;

                InetAddress addr = packet.getAddress();
                int client_id = p.getClient_id();

                if(!p.isResponse()){
                    if(this.anon.containsTargetSocket(addr, client_id)){
                        so = this.anon.getTargetSocket(addr, client_id);
                        if(p.getFragment() == this.anon.getLastPacket(addr, client_id) + 1){
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
                            this.anon.setLastPacket(addr, client_id, p.getFragment());
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
                                this.anon.setLastPacket(addr, client_id, p.getFragment());
                            }
                        }
                        else{
                            this.anon.add2Queue(addr, client_id, p);
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

                        this.anon.putTargetSocket(addr, map1);
                        this.anon.putLastPacket(addr, map2);
                        this.anon.putQueue(addr, map3);

                        new Thread(new TCP_Server(this.anon, so, addr, client_id)).start();
                    }
                }
                else{
                    if(p.getData().length == 6){
                        if(new String(p.getData()).equals("fechou")) {
                            this.anon.cleanClient(p.getClient_id());
                        }
                    }
                    else {
                        so = this.anon.getClientSocket(client_id);

                        int last_packet = this.anon.getMyClient_LastPacket(client_id);
                        if (p.getFragment() == last_packet + 1) {
                            OutputStream out = so.getOutputStream();
                            try {
                                System.out.println("Enviar Mensagem\n");
                                out.write(p.getData());
                                out.flush();
                                System.out.println("Mensagem Enviada\n");
                            } catch (SocketException e) {
                                this.anon.cleanClient(client_id);
                                System.out.println("Cant send to socket");
                            }
                            this.anon.setMyClient_LastPacket(client_id, p.getFragment());
                            while ((p = this.anon.proximoEnviar(null, client_id)) != null) {
                                try {
                                    System.out.println("Enviar Mensagem\n");
                                    out.write(p.getData());
                                    out.flush();
                                    System.out.println("Mensagem Enviada\n");
                                } catch (SocketException e) {
                                    this.anon.cleanClient(client_id);
                                    System.out.println("Cant send to socket");
                                }
                                this.anon.setMyClient_LastPacket(client_id, p.getFragment());
                            }
                        } else {
                            this.anon.add2MyClient_Queue(client_id, p);
                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
