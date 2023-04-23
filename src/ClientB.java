import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class ClientB {
    public static void main(String[] args){
        DatagramSocket ds;
        try{
            ds = new DatagramSocket(42071);
            System.out.println("PortB:" + ds.getLocalPort());
            ReadB rb = new ReadB(ds);
            rb.start();
            SendB sb = new SendB(ds);
            sb.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}

class ReadB extends Thread {
    byte[] by = new byte[1024];
    DatagramSocket ds;
    DatagramPacket dp;

    public ReadB(DatagramSocket ds) {
        this.ds = ds;
    }

    @Override
    public void run() {
        while (true) {
            dp = new DatagramPacket(by, by.length);
            try {
                //接收A端发送的数据报
                ds.receive(dp);// 阻塞方法
                //接收的数据包转成字符串形式输出
                String str = new String(dp.getData(), 0, dp.getLength());
                System.out.println("端口："+dp.getPort()+"->"+str);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

class SendB extends Thread {
    Scanner sc = new Scanner(System.in);
    byte[] by;
    DatagramPacket dp;
    DatagramSocket ds;

    public SendB(DatagramSocket ds) {
        this.ds = ds;
    }

    @Override
    public void run() {
        while (true) {
            String str = sc.next();
            by = str.getBytes();
            dp = new DatagramPacket(by, by.length, new InetSocketAddress(
                    "localhost", 42073));
            try {
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
