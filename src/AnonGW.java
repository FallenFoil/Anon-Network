import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

    public static void readFromClient(InputStream client_in, byte[] buff) throws IOException {
        int clientInCount = 0;
        System.out.println("Ler a Mensagem\n");
        while ((clientInCount = client_in.read(buff)) > 0){
            if(clientInCount < 4098){
                break;
            }
        }
        System.out.println("Mensagem lida\n");
    }

    public static void sendToClient(OutputStream client_out, byte[] buff) throws IOException {
        System.out.println("Enviar resposta\n");
        client_out.write(buff);
        client_out.flush();
        System.out.println("Resposta enviada\n");
    }

    public static void readFromTarget(InputStream target_in, byte[] buff) throws IOException {
        int targetInCount = 0;
        System.out.println("Receber resposta\n");
        while((targetInCount = target_in.read(buff)) > 0){
            if(targetInCount < 4098){
                break;
            }
        }
        System.out.println("Resposta lida\n");
    }

    public static void sendToTarget(OutputStream target_out, byte[] buff) throws IOException {
        System.out.println("Enviar a mensagem\n");
        target_out.write(buff);
        target_out.flush();
        System.out.println("Mensagem Enviada\n");
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
                byte[] buff;

                //Cliente
                Socket client = ss.accept();
                InputStream client_in = client.getInputStream();
                OutputStream client_out = client.getOutputStream();

                //targetServer
                Socket target = new Socket(me.getTargetServer(),me.getPort());
                InputStream target_in = target.getInputStream();
                OutputStream target_out = target.getOutputStream();

                buff = new byte[4098];
                readFromClient(client_in, buff);
                sendToTarget(target_out, buff);
                buff = new byte[4098];
                readFromTarget(target_in, buff);
                target.close();
                sendToClient(client_out, buff);
                client.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
