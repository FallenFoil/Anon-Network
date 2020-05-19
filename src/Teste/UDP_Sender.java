package Teste;

public class UDP_Sender implements Runnable{
    private AnonGW anon;

    public UDP_Sender(AnonGW a){
        this.anon = a;
    }

    @Override
    public void run() {
        while(true){
            while(this.anon.queue_udp.size() == 0){
                try {
                    this.anon.con_udp.await();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            this.anon.lock_udp.lock();
            UDP_Packet p = this.anon.queue_udp.poll();
            this.anon.lock_udp.unlock();

            if(p.isResponse()){

            }
            else{
                // Nodo 1 -> enviar para Nodo 2
            }
        }
    }
}
