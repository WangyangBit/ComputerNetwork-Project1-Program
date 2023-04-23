import java.io.IOException;
import java.util.Timer;

public class PC1 {
    public static void main(String[] args) throws IOException {
        GoBackN PC1 = new GoBackN(42072, 42073, 80,2,"PC1", "PC2");
//        PC1.sendData("E:/tmp2.txt", 4096);
////        PC1.receiveData("E:/PC1.txt", 4096);
////        PC1_receive receive =new  PC1_receive("E:/PC1.txt", 4096, PC1);
////        receive.start();
//
            PC1.setErrorrate(10);
            PC1.setMissingRate(10);
            PC1_send send = new PC1_send("E:/PC1.txt", 4096, PC1);
            send.start();
//
////        String sendLabel = hostname + ": Sending to port "+destPort+", Seq = " + nextSeq
////                +", isDataCarried = "+isSenData+" length = " + data.length() + ", DATA_NUMBER = "+ DATA _NUMBER_SED + "########";
    }
}

class PC1_send extends Thread{
    String filename;
    int size;
    GoBackN PC;

    public PC1_send(String path, int size, GoBackN PC)
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

class PC1_receive extends Thread{
    String filename;
    int size;
    GoBackN PC;
    public PC1_receive(String path, int size, GoBackN PC){
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
