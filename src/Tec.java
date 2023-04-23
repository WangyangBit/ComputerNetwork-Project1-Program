public class Tec {
    public static void main(String[] args){
        bar b = new bar(80);
        for(int i =0; i < 80; i++){
            b.progressBar(i+1);
        }
    }
}
