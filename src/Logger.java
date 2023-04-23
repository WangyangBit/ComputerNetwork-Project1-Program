import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.Timer;

public class Logger {

    String content;
    String filePath;
    File file;
    FileOutputStream fos;
    int num = 0;

    public Logger(String filePath){
        this.filePath = filePath;
    }
    public void openLog() throws FileNotFoundException {
        file = new File(filePath);
        fos = new FileOutputStream(file);
    }
    public void writeLog(String content) throws IOException {
        String tab = new String();
        for(int i = 0; i < 3-num/10000; i++){
            tab += "\t";
        }
        content = ++num +", "+content + "\n";
        fos.write(content.getBytes());
    }

    public void closeLog() throws IOException {
        fos.flush();
        fos.close();
    }
}
