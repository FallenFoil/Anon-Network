import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.ServerSocket;


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
                new Thread(new TCP_Client(this.anon, ss.accept())).start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
