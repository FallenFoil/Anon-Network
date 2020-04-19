import com.sun.source.tree.NewArrayTree;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnonGW {
    private List<String> nodes;
    private String targetServer;
    private int port;

    public AnonGW(){
        this.nodes = new ArrayList<>();
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public void addNodes(String node){
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
            for(String str : this.nodes){
                sb.append("    ").append(str).append("\n");
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        boolean error = false;
        int currentParams = 0;

        AnonGW me = new AnonGW();

        for(String arg : args){
            if(!error){
                switch(arg){
                    case "target-server":
                        currentParams = 1;
                        break;
                    case "port":
                        currentParams = 2;
                        break;
                    case "overlay-peers":
                        currentParams = 3;
                        break;
                    default:
                        if(currentParams == 1){
                            me.setTargetServer(arg);
                            currentParams = 0;
                        }
                        else{
                            if(currentParams == 2){
                                me.setPort(Integer.parseInt(arg));
                                currentParams = 0;
                            }
                            else{
                                if(currentParams == 3){
                                    me.addNodes(arg);
                                }
                                else{
                                    error = true;
                                }
                            }
                        }
                        break;
                }
            }
        }

        System.out.println(me.toString());

        try{
            ServerSocket ss = new ServerSocket(me.getPort());

            while(true){
                //receber pedido do cliente
                Socket client = ss.accept();
                InputStream in = client.getInputStream();
                int count;
                byte[] bufIn = new byte[1024];
                while ((count = in.read(bufIn)) > 0);

                //enviar pedido para o targetServer
                Socket target = new Socket(me.getTargetServer(),me.getPort());
                OutputStream out = target.getOutputStream();
                byte[] bufOut = Arrays.copyOf(bufIn,bufIn.length);
                out.write(bufOut);
                client.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
