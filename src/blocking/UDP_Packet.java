package blocking;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UDP_Packet{
    private int sequence;
    private int response;

    private InetAddress address;
    private int port;

    private int client_id;

    private byte[] data;
    private int data_size;

    public UDP_Packet(DatagramPacket packet){
        this.address = packet.getAddress();
        this.port = packet.getPort();

        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());

        this.sequence = wrapped.getInt();
        this.client_id = wrapped.getInt();
        this.response = wrapped.getInt();
        int packet_size = wrapped.getInt();

        if(packet_size > 16){
            byte[] buff = new byte[packet_size - 16];

            wrapped.get(data);
            this.data_size = data.length;
        }
    }

    public UDP_Packet(int sequence, InetAddress addr, int port, int client_id, byte[] data, boolean response){
        this.sequence = sequence;
        this.address = addr;
        this.port = port;
        this.client_id = client_id;

        this.data = data;
        this.data_size = data.length;

        if(response){
            this.response = 1;
        }
        else{
            this.response = 0;
        }
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

    public int getSequence(){
        return this.sequence;
    }

    public byte[] getData(){
        return this.data;
    }

    public DatagramPacket toDatagramPacket(){
        int packet_size = 4*4 + this.data_size;
        ByteBuffer b = ByteBuffer.allocate(packet_size);

        b.putInt(this.sequence);
        b.putInt(this.client_id);
        b.putInt(this.response);
        b.putInt(packet_size);

        if(this.data_size > 0){
            b.put(this.data);
        }

        byte[] buff = b.array();

        return new DatagramPacket(buff, buff.length, address, port);
    }
}
