import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

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

                if(!p.isResponse()){
                    if(this.anon.targetSockets.containsKey(p.getClient_id())){
                        so = this.anon.targetSockets.get(p.getClient_id());
                    }
                    else{
                        so = new Socket(this.anon.getTargetServer(), this.anon.getPort());
                        this.anon.targetSockets.put(p.getClient_id(), so);

                        new Thread(new TCP_Server(this.anon, so, packet.getAddress(), p.getClient_id())).start();
                    }
                }
		else{
			so = this.anon.getClient(p.getClient_id()).getSocket();
		}

                new Thread(new UDP_Client(this.anon, p, so)).start();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
