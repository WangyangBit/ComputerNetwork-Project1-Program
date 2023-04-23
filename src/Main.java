import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public Main() throws IOException {
    }

    public static void main(String[] args) throws IOException {

        GBN gbn = new GBN(100, 10, 20, 1, "pc1");
        gbn.setDestPort(100);
        Sen sen = new Sen("E:/tmp.txt", 2048, gbn);
        Rev rev = new Rev("E:/rev.txt", 2048, gbn);
        sen.start();
        rev.start();
    }



}
class Sen extends Thread{
    String filename;
    int datasize;

    GBN gbn;
    public Sen(String filename, int MDPS, GBN gbn){
        this.datasize = MDPS;
        this.filename = filename;
        this.gbn = gbn;
    }
    @Override
    public void run() {
        try {
            gbn.sendData(filename, datasize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Rev extends Thread{
    String filename;
    int datasize;

    GBN gbn;

    public Rev(String filename, int MDPS, GBN gbn){
        this.datasize = MDPS;
        this.filename = filename;
        this.gbn = gbn;
    }

    @Override
    public void run(){
        try {
            gbn.receiveData(filename, datasize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}