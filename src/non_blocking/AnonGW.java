package non_blocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

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

        ServerSocketChannel serverChannel = null;
        Selector selector = null;

        try{
            System.out.println("Initializing server");
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress("127.0.0.1", 8511));

            selector = Selector.open();

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            Map<SocketChannel, byte[]> dataTracking = new HashMap<>();

            System.out.println("Now accepting connections...");
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        System.out.println("Accepting connection");
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        byte[] hello = "Hello from server".getBytes();
                        dataTracking.put(socketChannel, hello);
                    }

                    if (key.isWritable()) {
                        System.out.println("Writing...");
                        SocketChannel channel = (SocketChannel) key.channel();

                        byte[] data = dataTracking.get(channel);
                        dataTracking.remove(channel);

                        channel.write(ByteBuffer.wrap(data));

                        key.interestOps(SelectionKey.OP_READ);
                    }

                    if (key.isReadable()) {
                        System.out.println("Reading connection");
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        readBuffer.clear();

                        int read;
                        try {
                            read = channel.read(readBuffer);
                        }
                        catch(IOException e) {
                            e.printStackTrace();
                            key.cancel();
                            channel.close();
                            return;
                        }

                        if (read == -1) {
                            System.out.println("Nothing was there to be read, closing connection");
                            channel.close();
                            key.cancel();
                            return;
                        }

                        readBuffer.flip();
                        byte[] data = new byte[1000];
                        readBuffer.get(data, 0, read);
                        System.out.println("Received: " + new String(data));

                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        dataTracking.put(socketChannel, data);
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Closing server down");
            try{
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}

