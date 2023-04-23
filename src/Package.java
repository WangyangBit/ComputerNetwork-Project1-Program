import com.sun.swing.internal.plaf.synth.resources.synth_zh_TW;
import org.omg.CORBA.WStringSeqHelper;

import javax.script.ScriptEngine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Package {

    /**
     * 组帧分隔符
     */
    protected String Split = "########";

    String adr;
    int Datasize;
    byte[] pac;
    public Package(String adr, int size)
    {
        this.adr = adr;
        this.Datasize = size;
    }
    public ArrayList<String> pack() throws IOException {
        ArrayList<String> al = new ArrayList();
        FileInputStream fis = new FileInputStream(adr);
        pac = new byte[Datasize];
        int read = 0;
        while((read = fis.read(pac)) != -1) {
            if (read != Datasize) {
                al.add(new String(pac).substring(0,read));
            }
            else{
                al.add(new String(pac));
            }
        }
        System.out.println();
        fis.close();
        return al;
    }
/**
 * 将数据加上CRC循环冗余校验
 * @param Data 待发送的数据
 */
   public String addCRC(String Data){
        String DataWithCRC;
        String crc = CRC16.getCRC(stringToHexString(Data));
        DataWithCRC = Data + Split + crc;
        return DataWithCRC;
   }

   /**
    * 组帧格式：主机+端口+帧序列+是否为数据帧+包序列号+data+crc
    */
 public String _frame(String Data, String hostname, int port, int seq, boolean isData, int num){
       String frame = new String();
       Data = addCRC(Data);
       frame = hostname + Split + port + Split + seq + Split + isData + Split + num + Split + Data;
       return frame;
 }

   /**
    * 将String转换为16进制字符串
    */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

}
