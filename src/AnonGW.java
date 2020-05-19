import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnonGW {
    private List<InetAddress> nodes;
    private String targetServer;
    private int port;
    private Lock rand_lock;
    private Random rand;

    public Map<Integer, Client> my_clients;
    public Map<Integer, Integer> my_clients_last_packet;
    public Map<Integer, List<UDP_Packet>> my_clients_packets_queue;
    public int next_client_ID;
    public Lock clients_lock;
    public Condition clients_con;

    public Map<InetAddress, Map<Integer, Integer>> last_packet_sent; // <Node,  <Client_ID, Last_Packet>>
    public Map<InetAddress, Map<Integer, List<UDP_Packet>>> packets_in_queue; // <Node,  <Client_ID, UDP_Packets>>
    public Map<InetAddress, Map<Integer, Socket>> targetSockets; // <Node, <Client_ID, Server_Socket>>
    public Lock nodes_lock;
    public Condition node_con;

    public UDP_Packet proximoEnviar(InetAddress addr, int client_ID){
        int last_packet;
        if(addr == null){
            last_packet = this.my_clients_last_packet.get(client_ID);

            for(UDP_Packet p : this.my_clients_packets_queue.get(client_ID)){
                if(p.getFragment() == last_packet + 1){
                    return p;
                }
            }
        }
        else{
            last_packet = this.last_packet_sent.get(addr).get(client_ID);

            for(UDP_Packet p : this.packets_in_queue.get(addr).get(client_ID)){
                if(p.getFragment() == last_packet + 1){
                    return p;
                }
            }
        }

        return null;
    }

    public AnonGW(){
        this.nodes = new ArrayList<>();
        this.rand = new Random();
        this.rand_lock = new ReentrantLock();

        this.my_clients = new HashMap<>();
        this.my_clients_last_packet = new HashMap<>();
        this.my_clients_packets_queue = new HashMap<>();
        this.next_client_ID = 0;
        this.clients_lock = new ReentrantLock();
        this.clients_con = this.clients_lock.newCondition();

        this.last_packet_sent = new HashMap<>();
        this.packets_in_queue = new HashMap<>();
        this.targetSockets = new HashMap<>();
        this.nodes_lock = new ReentrantLock();
        this.node_con = this.nodes_lock.newCondition();
    }

    public void addNodes(InetAddress node){
        this.nodes.add(node);
    }

    public String getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public UDP_Packet getSmallestFragment(InetAddress addr, int client_id){
        List<UDP_Packet> list;

        if(addr == null){
            this.clients_lock.lock();
            list = this.my_clients_packets_queue.get(client_id);
            this.clients_lock.unlock();
        }
        else{
            this.nodes_lock.lock();
            list = this.packets_in_queue.get(addr).get(client_id);
            this.nodes_lock.unlock();
        }

        UDP_Packet smallest = null;

        for(UDP_Packet p : list){
            if(smallest == null){
                smallest = p;
            }
            else{
                if(p.getFragment() < smallest.getFragment()){
                    smallest = p;
                }
            }
        }

        return smallest;
    }

    public InetAddress getRandomNode(){
        this.rand_lock.lock();
        int index = this.rand.nextInt(this.nodes.size());
        this.rand_lock.unlock();

        return this.nodes.get(index);
    }

    public Client createNewClient(InetAddress addr, Socket so){
        this.clients_lock.lock();

        int id = this.next_client_ID;
        Client c = new Client(id, 0, addr, so);
        this.my_clients.put(id, c);
        this.my_clients_last_packet.put(id, -1);
        List<UDP_Packet> list = new ArrayList<>();
        this.my_clients_packets_queue.put(id,list);
        this.next_client_ID++;

        this.clients_lock.unlock();

        return c;
    }

    public Client getClient(int id){
        this.clients_lock.lock();
        Client c = this.my_clients.get(id);
        this.clients_lock.unlock();

        return c;
    }

    public void cleanClient(int id){
        this.clients_lock.lock();
        this.my_clients.remove(id);
        this.my_clients_last_packet.remove(id);
        this.my_clients_packets_queue.remove(id);
        this.clients_lock.unlock();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Target Server: ").append(this.targetServer).append("\n");
        sb.append("Port: ").append(this.port).append("\n");
        sb.append("Nodes: ");
        if(this.nodes.isEmpty()){
            sb.append("none\n");
        }
        else{
            sb.append("\n");
            for(InetAddress str : this.nodes){
                sb.append("    ").append(str.toString()).append("\n");
            }
        }

        return sb.toString();
    }

    private static boolean init_configure(String[] args, AnonGW me) throws UnknownHostException {
        boolean error = false;
        boolean checkTargetServer = false;
        boolean checkPort = false;
        int currentParams = 0;
        int argumentIndex = 0;

        for(String arg : args){
            if(error){
                break;
            }

            switch(arg){
                case "target-server":
                    if((currentParams != 0 && currentParams != 3) || argumentIndex == args.length - 1){
                        error = true;
                    }
                    else{
                        currentParams = 1;
                    }
                    break;
                case "port":
                    if((currentParams != 0 && currentParams != 3) || argumentIndex == args.length - 1){
                        error = true;
                    }
                    else{
                        currentParams = 2;
                    }
                    break;
                case "overlay-peers":
                    if(currentParams != 0){
                        error = true;
                    }
                    else{
                        currentParams = 3;
                    }
                    break;
                default:
                    if(currentParams == 1){
                        me.setTargetServer(arg);
                        checkTargetServer = true;
                        currentParams = 0;
                    }
                    else{
                        if(currentParams == 2){
                            me.setPort(Integer.parseInt(arg));
                            checkPort = true;
                            currentParams = 0;
                        }
                        else{
                            if(currentParams == 3){
                                me.addNodes(InetAddress.getByName(arg));
                            }
                            else{
                                error = true;
                            }
                        }
                    }
                    break;
            }

            argumentIndex++;
        }

        if(error){
            System.out.println("An error occurred. Check if the parameters are correct.");
            return false;
        }

        if(!checkTargetServer){
            System.out.println("Missing target server IP address.");
            return false;
        }

        if(!checkPort){
            System.out.println("Missing port.");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        AnonGW me = new AnonGW();

        try{
            if(!init_configure(args, me)){
                return;
            }
        }
        catch(UnknownHostException e){
            e.printStackTrace();
            return;
        }

        System.out.println(me.toString());

        // UDP
        new Thread(new UDP2(me)).start();

        // TCP
        new Thread(new TCP(me)).start();

        Scanner scan = new Scanner(System.in);

        while(true){
            String cmd = scan.nextLine();

            if(cmd.equals("reset")){
                me.my_clients.clear();
                me.my_clients_last_packet.clear();
                me.my_clients_packets_queue.clear();
                me.next_client_ID = 0;

                me.last_packet_sent.clear();
                me.packets_in_queue.clear();
                me.targetSockets.clear();
            }
            if(cmd.equals("print")){
                System.out.println(me.my_clients.toString() + "\n");
                System.out.println(me.my_clients_last_packet.toString() + "\n");
                System.out.println(me.my_clients_packets_queue.toString() + "\n");
                System.out.println(me.next_client_ID + "\n");

                System.out.println(me.last_packet_sent.toString() + "\n");
                System.out.println(me.packets_in_queue.toString() + "\n");
                System.out.println(me.targetSockets.toString() + "\n");
            }
        }
    }
}
