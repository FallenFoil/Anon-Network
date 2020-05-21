import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP implements Runnable{
    private AnonGW anon;

    public TCP(AnonGW a){
        this.anon = a;
    }

    @Override
    public void run() {
        try{
            ServerSocket ss = new ServerSocket(this.anon.getPort());

            while(true){
                Socket client = ss.accept();
                new Thread(new TCP_Reader(this.anon, client, this.anon.getRandomNode(), this.anon.createNewClient(client), false)).start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
