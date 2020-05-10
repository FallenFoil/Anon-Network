package blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class TCP implements Runnable{
    private AnonGW anon;

    public TCP(AnonGW a){
        this.anon = a;
    }

    public static void send(OutputStream out, byte[] buff) throws IOException {
        System.out.println("Enviar Mensagem\n");
        out.write(buff);
        out.flush();
        System.out.println("Mensagem Enviada\n");
    }

    /**
     *
     * @param in InputStream onde os bytes seram lidos
     * @param flag Determina se é para receber do Cliente (0) ou do Servidor (1)
     *
     * @return Retorna os bytes lidos
     */
    public static byte[] read(InputStream in, int flag) throws IOException {
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
