package blocking;

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
                new Thread(new AnonGWClient(this.anon, ss.accept())).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
