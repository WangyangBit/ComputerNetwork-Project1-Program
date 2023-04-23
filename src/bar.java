import java.util.stream.Stream;

public class bar {
    char incomplete = '░'; // U+2591 Unicode Character 表示还没有完成的部分
    char complete = '█'; // U+2588 Unicode Character 表示已经完成的部分
    int total = 100;
    int num;
    int tmp = 0;
    int past;
    StringBuilder builder = new StringBuilder();
    public bar(int num){
        this.num = num;
        Stream.generate(()->incomplete).limit((total)).forEach(builder::append);
    }
    public void setnum(int n){
        this.num = n;
    }
    public void progressBar(int k){
            past = tmp;
            tmp = (int) Math.ceil(total * (double)(k/num));
            for(int i = past; i < tmp && i < total; i++)
            {
                builder.replace(i, i+1, String.valueOf(complete));
                String progressBar = "\r" + builder;
                String percent = " " + (1+i)+ "%";
                System.out.print(progressBar+percent);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {

                }
            }

    }

}
