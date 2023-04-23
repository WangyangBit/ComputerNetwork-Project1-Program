import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PC2 {
    public static void main(String[] args) throws IOException {
        GoBackN PC2 = new GoBackN(42073, 42072, 40, 2, "PC2", "PC1");
//        PC2.receiveData("E:/rec.txt", 4096);
////        PC2.sendData("E:/res.txt", 4096);
            PC2_receive receive =new  PC2_receive("E:/tmp.txt", 4096, PC2);
            receive.start();
////        PC2_send send = new PC2_send("E:/tmp.txt", 4096, PC2);
////        send.start();
    }
}

class PC2_send extends Thread{
    String filename;
    int size;
    GoBackN PC;

    public PC2_send(String path, int size, GoBackN PC)
    {
        this.PC = PC;
        this.filename = path;
        this.size = size;
    }

    @Override
    public void run(){
        try {
            PC.sendData(filename, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class PC2_receive extends Thread{
    String filename;
    int size;
    GoBackN PC;
    public PC2_receive(String path, int size, GoBackN PC){
        this.PC = PC;
        this.filename = path;
        this.size = size;
    }

    @Override
    public void run(){
        try {
            PC.receiveData(filename, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
