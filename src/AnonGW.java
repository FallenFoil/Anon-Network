import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
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

    public byte[] readFromClient(InputStream client_in) throws IOException {
        System.out.println("Ler a Mensagem\n");

        int clientInCount = 0;
        int msgSize = 0;
        List<byte[]> buffOfBuffs = new ArrayList<>();
        byte[] buff = new byte[4096];

        while ((clientInCount = client_in.read(buff)) != -1){
            msgSize += clientInCount;
            buffOfBuffs.add(buff);
            buff = new byte[4096];
        }

        byte[] res = new byte[msgSize];
        int index = 0;
        for(byte[] arr: buffOfBuffs){
            for(byte b : arr){
                if(index >= msgSize){
                    break;
                }
                else{
                    res[index++] = b;
                }
            }
        }

        System.out.println("Mensagem lida\n");
        return res;
    }

    public void sendToClient(OutputStream client_out, byte[] buff) throws IOException {
        System.out.println("Enviar resposta\n");
        client_out.write(buff);
        client_out.flush();
        System.out.println("Resposta enviada\n");
    }

    public byte[] readFromTarget(InputStream target_in) throws IOException {
        System.out.println("Receber resposta\n");

        int targetInCount = 0;
        int msgSize = 0;
        List<byte[]> buffOfBuffs = new ArrayList<>();
        byte[] buff = new byte[4096];

        while((targetInCount = target_in.read(buff)) != -1){
            msgSize += targetInCount;
            buffOfBuffs.add(buff);
            buff = new byte[4096];
        }

        byte[] res = new byte[msgSize];
        int index = 0;
        for(byte[] arr: buffOfBuffs){
            for(byte b : arr){
                if(index >= msgSize){
                    break;
                }
                else{
                    res[index++] = b;
                }
            }
        }

        System.out.println("Resposta lida\n");
        return res;
    }

    public void sendToTarget(OutputStream target_out, byte[] buff) throws IOException {
        System.out.println("Enviar a mensagem\n");
        target_out.write(buff);
        target_out.flush();
        System.out.println("Mensagem Enviada\n");
    }

    private static boolean[] configure(String[] args, AnonGW me){
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
                                me.addNodes(arg);
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

        return new boolean[]{error, checkTargetServer, checkPort};
    }

    public static void main(String[] args) {
        AnonGW me = new AnonGW();

        boolean[] configureRes = configure(args, me);
        boolean error = configureRes[0];
        boolean checkTargetServer = configureRes[1];
        boolean checkPort = configureRes[2];

        if(error){
            System.out.println("An error occurred. Check if the parameters are correct.");
            return;
        }

        if(!checkTargetServer){
            System.out.println("Missing target server IP address.");
            return;
        }

        if(!checkPort){
            System.out.println("Missing port.");
            return;
        }

        System.out.println(me.toString());

        try{
            ServerSocket ss = new ServerSocket(me.getPort());

            while(true){
                new Thread(new AnonGWClient(me, ss.accept())).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
