package Old;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TCP_Client implements Runnable{
    private Socket client;
    private AnonGW anon;



    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    ArrayList<byte[]> buffer = new ArrayList<>();
    boolean sending = false;
    boolean toSend = false;
    boolean reading = true;


    public TCP_Client(AnonGW a, Socket so){
        this.anon = a;
        this.client = so;

    }

    @Override
    public void run() {
        try{
            this.client.setSoTimeout(5000);
            InputStream client_in = this.client.getInputStream();

            byte[] buff = TCP.read(client_in);

            InetAddress client_address = this.client.getInetAddress();

            Client c = this.anon.createNewClient(client_address, this.client);

            UDP_Packet packet = new UDP_Packet(c.getNext_sequence(), false, 0, 1, this.anon.getRandomNode(), 6666, c.getId(), buff);

            UDP.send(packet);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Connection closed");
    }

    /*
    @Override
    public void run() {

        //LER
        new Thread(()->{
            try {
                while(true){
                    byte[] buff = new byte[4096];
                    int count = client.getInputStream().read(buff);
                    if(count == -1) break;

                    while(sending) condition.await();
                    lock.lock();
                    reading = true;
                    buffer.add(buff);
                    toSend = true;
                    reading = false;
                    condition.signal();

                    lock.unlock();
                }
            }catch (Exception e){

            }
        }).start();


        //ENVIAR
        new Thread(()->{
            try{
                while(!toSend || !reading) condition.await();
                lock.lock();
                sending = true;
                byte[] send = buffer.get(1);
                buffer.remove(1);
                InetAddress client_address = this.client.getInetAddress();
                Client c = this.anon.createNewClient(client_address, this.client);
                UDP_Packet packet = new UDP_Packet(c.getNext_sequence(), false, 0, 1, this.anon.getRandomNode(), 6666, c.getId(), send);
                UDP.send(packet);
                if(buffer.size()==0) toSend = false;
                sending = false;
                condition.signal();
                lock.unlock();
            }catch (Exception e){

            }
        }).start();
    }
    */
}
