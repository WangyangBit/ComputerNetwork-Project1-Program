import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Scanner;

public class ClientA {
    public static void main(String[] args){
        DatagramSocket ds;
        try{
            ds = new DatagramSocket(42073);
            System.out.println("portA:" + ds.getLocalPort());
            ReadA ra = new ReadA(ds);
            ra.start();
            SendA sa = new SendA(ds);
            sa.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}

class ReadA extends Thread{
    byte[] by = new byte[1024];
    DatagramSocket ds;
    DatagramPacket dp;

    public ReadA(DatagramSocket ds){
        this.ds = ds;
    }

    @Override
    public void run(){
        while(true){
            dp = new DatagramPacket(by, by.length);
            try{
                ds.receive(dp);
                String str = new String(dp.getData(), 0, dp.getLength());
                System.out.println("port:" + dp.getPort() + "->" +str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class SendA extends Thread{
    Scanner sc = new Scanner(System.in);
    byte[] by;
    DatagramSocket ds;
    DatagramPacket dp;

    public SendA(DatagramSocket ds){
        this.ds = ds;
    }

    @Override
    public void run(){
        while(true){
            String str = sc.next();
            by = str.getBytes();
            dp = new DatagramPacket(by, by.length, new InetSocketAddress("localhost", 42071));
            try{
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
