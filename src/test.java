import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

public class test {

    public static void main(String[] args) throws IOException {
        int total = 100;
        int last;
        int tmp = 0;
        char incomplete = '░'; // U+2591 Unicode Character 表示还没有完成的部分
        char complete = '█'; // U+2588 Unicode Character 表示已经完成的部分
        StringBuilder builder = new StringBuilder();
        Stream.generate(()->incomplete).limit(total).forEach(builder::append);
        for(int j = 0; j < 96; j+=5){
            last = tmp;
            tmp = j;
            for (int i = last; i < tmp && i < total; i++) {
                builder.replace(i, i + 1, String.valueOf(complete));
                String progressBar = "\r" + builder;
                String percent = " " + (i + 1) + "%";
                System.out.print(progressBar + percent);
                try {
                    Thread.sleep(i * 5L);
                } catch (InterruptedException ignored) {

                }
            }
        }

    }
}
