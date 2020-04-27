import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AnonGWClient implements Runnable{
    private Socket client;
    private AnonGW anon;

    public AnonGWClient(AnonGW a, Socket so){
        this.anon = a;
        this.client = so;
    }

    @Override
    public void run() {
        byte[] buff;

        try{
            InputStream client_in = this.client.getInputStream();
            OutputStream client_out = this.client.getOutputStream();

            //targetServer
            Socket target = new Socket(this.anon.getTargetServer(), this.anon.getPort());
            InputStream target_in = target.getInputStream();
            OutputStream target_out = target.getOutputStream();

            buff = this.anon.readFromClient(client_in);
            System.out.println(new String(buff));
            this.anon.sendToTarget(target_out, buff);
            buff = this.anon.readFromTarget(target_in);
            System.out.println(new String(buff));
            this.anon.sendToClient(client_out, buff);

            client.close();
            target.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Connection closed");
    }
}
