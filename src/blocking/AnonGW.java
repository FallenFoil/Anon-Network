package blocking;

import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnonGW {
    private List<InetAddress> nodes;
    private String targetServer;
    private int port;
    private Random rand;

    private Map<Integer, Client> my_clients;

    private int next_client_ID;

    private Lock lock;

    public AnonGW(){
        this.nodes = new ArrayList<>();
        this.rand = new Random();

        this.my_clients = new HashMap<>();

        this.next_client_ID = 0;

        this.lock = new ReentrantLock();
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

    public InetAddress getRandomNode(){
        int index = this.rand.nextInt(this.nodes.size());

        return this.nodes.get(index);
    }

    public Client createNewClient(InetAddress addr, Socket so){
        this.lock.lock();
        int id = this.next_client_ID;
        Client c = new Client(id, 0, addr, so);
        this.my_clients.put(id, c);
        this.next_client_ID++;
        this.lock.unlock();

        return c;
    }

    public Client getClient(int id){
        return this.my_clients.get(id);
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
        new Thread(new UDP(me)).start();

        // TCP
        new Thread(new TCP(me)).start();
    }
}
