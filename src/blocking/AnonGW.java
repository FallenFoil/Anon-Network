package blocking;

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

    public void send(OutputStream out, byte[] buff) throws IOException {
        System.out.println("Enviar Mensagem\n");
        out.write(buff);
        out.flush();
        System.out.println("Mensagem Enviada\n");
    }

    /**
     *
     * @param in InputStream onde os bytes seram lidos
     * @param flag Determina se é para receber do Cliente ou do Servidor
     *
     * @return Retorna os bytes lidos
     */
    public byte[] read(InputStream in, int flag) throws IOException {
        System.out.println("Receber Mensagem\n");

        int inCount = 0;
        int msgSize = 0;
        List<byte[]> buffOfBuffs = new ArrayList<>();
        byte[] buff = new byte[4096];

        while((inCount = in.read(buff)) > 0){
            msgSize += inCount;
            buffOfBuffs.add(buff);
            buff = new byte[4096];

            if(flag == 0 && inCount < 4096){
                break;
            }
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

        System.out.println("Mensagem Recebida\n");
        return res;
    }

    private static boolean init_configure(String[] args, AnonGW me){
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

        if(!init_configure(args, me)){
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
