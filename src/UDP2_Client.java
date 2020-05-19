import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class UDP2_Client implements Runnable{
    private AnonGW anon;
    private Socket target;
    private InetAddress parent_node;
    private int client_id;

    public UDP2_Client(AnonGW a, Socket s, InetAddress p, int id){
        this.anon = a;
        this.target = s;
        this.parent_node = p;
        this.client_id = id;
    }

    @Override
    public void run() {/*
        try{
            UDP_Packet p;

            while(true){
                if(this.parent_node == null){
                    this.anon.clients_lock.lock();
                    while(this.anon.my_clients_packets_queue.get(this.client_id).size() == 0 || (p = this.anon.my_clients_packets_queue.containsPacket(client_id)) != null){
                        this.anon.clients_con.wait();
                    }

                    enviar;
                    remover da list;
                    this.anon.clients_lock.unlock();
                }
                else{
                    this.anon.nodes_lock.lock();
                    while(this.anon.packets_in_queue.get(this.parent_node).get(client_id).size() == 0){
                        this.anon.node_con.wait();
                    }

                    this.anon.nodes_lock.unlock();
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }
}
