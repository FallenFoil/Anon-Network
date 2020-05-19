import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class UDP_Packet{
    public static final int n_bytes = 4*4;
    private int response;
    private int fragment;

    private InetAddress from_address;
    private InetAddress to_address;
    private int port;


    private int client_id;

    private byte[] data;
    private int data_size;

    public UDP_Packet(DatagramPacket packet, String target, int port) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        this.to_address = packet.getAddress();
        this.port = packet.getPort();

        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());

        this.client_id = wrapped.getInt();
        this.response = wrapped.getInt();
        this.fragment = wrapped.getInt();
        this.data_size = wrapped.getInt();

        if(this.data_size > 0){
            this.data = new byte[this.data_size];
            byte[] encrypBytes = new byte[this.data_size];
            wrapped.get(encrypBytes);
            this.data = AESEncryptionManager.decryptData(target + ":" + port, encrypBytes);
        }
    }

    public DatagramPacket toDatagramPacket(){
        int packet_size = UDP_Packet.n_bytes + this.data_size;
        ByteBuffer b = ByteBuffer.allocate(packet_size);

        b.putInt(this.client_id);
        b.putInt(this.response);
        b.putInt(this.fragment);
        b.putInt(this.data_size);

        if(this.data_size > 0){
            b.put(this.data);
        }

        byte[] buff = b.array();

        return new DatagramPacket(buff, buff.length, to_address, port);
    }

    public UDP_Packet(boolean response, int fragment, InetAddress addr, int port, int client_id, byte[] data){
        if(response){
            this.response = 1;
        }
        else{
            this.response = 0;
        }
        this.fragment = fragment;

        this.from_address = null;
        this.to_address = addr;
        this.port = port;

        this.client_id = client_id;

        this.data = data;
        this.data_size = data.length;
    }

    public boolean isResponse(){
        if(this.response == 1){
            return true;
        }
        else{
            return false;
        }
    }

    public int getClient_id(){
        return this.client_id;
    }

    public byte[] getData(){
        return this.data;
    }

    public int getFragment(){
        return this.fragment;
    }

    public InetAddress getFrom(){
        return this.from_address;
    }

    public void setFrom_address(InetAddress from){
        this.from_address = from;
    }

    public int getData_size(){
        return this.data_size;
    }
}
