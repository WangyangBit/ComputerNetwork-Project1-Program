import com.sun.media.sound.SF2InstrumentRegion;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.SplittableRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoBackN {
    /**
     * 窗口大小
     */
    int WINDOW_SIZE;
    /**
     * 待发送包的数目
     */
    int DATA_NUMBER_SED;
    /**
     * 接受方接受的包的数目
     */
    int DATA_NUMBER_REV;
    int TIME_OUT;
    String hostname;
    int port;
    int nextSeq = 1;
    int base = 1;
    InetAddress destAddress;
    int destPort = 80;
    int missingRate = 10;
    int errorrate = 10;
    /*接收方*/
    int expectSeq = 1;
    int lastSave = 0;

    DatagramSocket sendSocket;
    DatagramSocket receiveSocket;
    int MaxDataPacketSize = 0;
    boolean isSenData = false;
    boolean isRevData = false;
    ArrayList<String> Datalist = new ArrayList<String>();

//    String infilePath = new String();
    String outfilePath = new String();

    Logger sendLog, receiveLog;

    //统计数据
    int totalSend = 0;
    int totalReceive = 0;
    int RTSend = 0;
    int TOSend = 0;
    int daErrReceive = 0;
    int noErrReceive = 0;


    public GoBackN(int myPort, int toPort, int WINDOW_SIZE, int TIME_OUT, String name, String destname) throws SocketException, UnknownHostException {
        this.WINDOW_SIZE = WINDOW_SIZE;
        this.destPort = toPort;
        this.port = myPort;
        this.TIME_OUT = TIME_OUT;
        this.hostname = name;

        sendSocket = new DatagramSocket();
        receiveSocket = new DatagramSocket(myPort);
        destAddress = InetAddress.getLocalHost();
    }

    public void setMissingRate(int n){
        this.missingRate = n;
    }
    public void setErrorrate(int n){
        this.errorrate = n;
    }

    /**
     * 将文件数据进行分组打包
     */
    public void sendData(String filename, int MaxDataPacketSize) throws IOException {
        File file = new File(filename);
        sendLog = new Logger(filename+"{" + hostname +"__send-logger}.txt");
        sendLog.openLog();
        this.MaxDataPacketSize = MaxDataPacketSize;
        if(file.length() == 0){
            System.out.println("empty file");
            sendLog.closeLog();
            return;
        }
        try{
            Package pk = new Package(filename, MaxDataPacketSize);
            Datalist = pk.pack();
            DATA_NUMBER_SED = Datalist.size();
            isSenData = true;
            System.out.println(hostname + ":file is divided into " + DATA_NUMBER_SED + "Packages.");
            sendLog.writeLog(hostname + ":file is divided into " + DATA_NUMBER_SED + "Packages.");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        send();
    }


    /**
     * 发送报文
     */
    public void send() throws IOException {
        long t1 = System.currentTimeMillis();
        int maxACK = 0;
        String Status = "T0";
        while(true){
            while(nextSeq < base + WINDOW_SIZE && nextSeq <= DATA_NUMBER_SED){
                if(Math.random() < 1.0/missingRate){
                    System.out.println(hostname + "simulate missing pakage: Seq=" + nextSeq);
                    sendLog.writeLog("pdu_to_send=" + nextSeq + ", status=New, ackedNo=" + maxACK);

                    totalSend++;
                    nextSeq ++;
                    continue;
                }
                Package pk = new Package("", 0);
                String data = Datalist.get(nextSeq - 1);
                String sendLabel = hostname + ": Sending to port "+destPort+", Seq = " + nextSeq
                        +", isDataCarried = "+isSenData+" length = " + data.length() + ", DATA_NUMBER = "+ DATA_NUMBER_SED + "########";
                data = pk.addCRC(data);
                if(Math.random() < 1.0/errorrate){//误码
                    data += "0";
                }
                data+="########";
                byte[] datagram = (sendLabel+data).getBytes();

                DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length, destAddress, destPort);
                sendSocket.send(datagramPacket);

                System.out.println(hostname + " send to Port:" + destPort + ", Seq = " + nextSeq);
                sendLog.writeLog("pdu_to_send=" + nextSeq + ", status=New, ackedNo=" + maxACK);

                totalSend++;
                nextSeq++;

                try{
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 发送完之后开始接受ack帧
            byte[] bytes = new byte[4096];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            sendSocket.setSoTimeout(1000*TIME_OUT);

            try{
                sendSocket.receive(datagramPacket);
            } catch (SocketTimeoutException ex){
                System.out.println(hostname + " is waiting for ACK:Seq = " + base + " out of time.");
                timeOut(maxACK, "TO");
                continue;
            }

            // 将接受到的byte转换为String
            String[] fS = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).split("####");
            String fromSend = fS[0];
            Status = fS[1];
            if(Status.equals("RT")){
                timeOut(maxACK, "RT");
            }
            int ack = Integer.parseInt(fromSend.substring(fromSend.indexOf("ACK: ") + "ACK: ".length()).trim());
            maxACK = Math.max(ack, maxACK);
            //ack确认最后收到的报文，nextseq发送完本轮报文期待下一轮收到的报文
            base = maxACK + 1;
            System.out.println(hostname + " received the last and biggest ACK: " + maxACK);

            if(maxACK == DATA_NUMBER_SED){
                //发送完毕
                long t2 = System.currentTimeMillis();
                System.out.println("\n"+hostname+"'s sending is complete.\n" + (t2-t1) + " ms" );
                sendLog.writeLog("\n"+hostname+"'s sending is complete.\n" + (t2-t1) + " ms" + " totalSend:"+totalSend+" TR:"+RTSend+" TO:"+TOSend);
                return;
            }
        }
    }

    //接受数据模块
    public void receiveData(String filename, int MaxDataPacketSize) throws IOException {
        long t1 = System.currentTimeMillis();
        isRevData = true;
        this.MaxDataPacketSize = MaxDataPacketSize;
        if(!filename.isEmpty()){
            outfilePath = filename;
            receive();
            long t2 = System.currentTimeMillis();
            System.out.println((t2-t1)+" ms");
            receiveLog.writeLog("TimeCost: "+(t2-t1)+" ms" + " total:"+totalReceive+" dataErr:"+daErrReceive+" noErr:"+noErrReceive);
            receiveLog.closeLog();
        }else{
            System.out.println("Please input filepath!");
        }
    }

    public void receive() throws IOException {
        File output = null;
        FileOutputStream fos = null;
        if(isRevData){
            output = new File(outfilePath);
            fos = new FileOutputStream(output);
        }
        receiveLog = new Logger(outfilePath + "{" + hostname+ "__receive-logger}.txt");
        receiveLog.openLog();
        while(true){
            byte[] receivedData = new byte[Math.max(4096, MaxDataPacketSize + 500)];
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            receiveSocket.setSoTimeout(1000*TIME_OUT);
            try{
                receiveSocket.receive(receivePacket);
            } catch (IOException e) {
                System.out.println(hostname + " is waitting for Seq = " + expectSeq);
                continue;
            }

            String receivedLabel = new String(receivedData, 0, receivedData.length);
            String[] R = receivedLabel.split("########");
            String label = R[0];
            int labelSize = (label + "########").getBytes().length;

            String pattern = "\\w*: Sending to port \\d+, Seq = (\\d+), isDataCarried = (true|false) length = (\\d+), DATA_NUMBER = (\\d+)";
            Matcher matcher = Pattern.compile(pattern).matcher(label);
            boolean checkdata = (CRC16.getCRC(R[1].getBytes()).equals(R[2]));

            if(!matcher.find()){
                System.out.println(hostname + " received error data " + label);
                sendACK(expectSeq-1, "RT",receivePacket.getAddress(), receivePacket.getPort());
                totalReceive++;
                daErrReceive++;
                continue;
            }


            int receivedSeq = Integer.parseInt(matcher.group(1));
            isRevData = Boolean.parseBoolean(matcher.group(2));
            int dataLength = Integer.parseInt(matcher.group(3));
            DATA_NUMBER_REV = Integer.parseInt(matcher.group(4));


            if(receivedSeq == expectSeq){
                if(!checkdata){
                    System.out.println(hostname + " received error data " + label);
                    receiveLog.writeLog("pdu_exp=" + expectSeq + ", pdu_recv=" + receivedSeq + ", status=DataErr");
                    sendACK(expectSeq-1, "RT",receivePacket.getAddress(), receivePacket.getPort());
                    totalReceive++;
                    daErrReceive++;
                    continue;
                }
                System.out.println(hostname + " received expected data, send ACK: Seq = " + expectSeq);
                receiveLog.writeLog("pdu_exp=" + expectSeq + ", pdu_recv=" + receivedSeq + ", status=OK");
                if(isRevData && lastSave == receivedSeq - 1){
                    System.out.println(hostname + " is writting data " + expectSeq);
//                    receiveLog.writeLog(hostname + " is writting data " + expectSeq);
                    fos.write(receivedData, labelSize, dataLength);
                    lastSave = receivedSeq;
                }

                //发送ACK
                //ACK丢失保留
                sendACK(expectSeq, "OK",receivePacket.getAddress(), receivePacket.getPort());
                totalReceive++;

                if(expectSeq == DATA_NUMBER_REV){
                    System.out.println(hostname + " complete receiving");
                    if(isRevData){
                        fos.flush();
                        fos.close();
                    }
                    return;
                }
                expectSeq++;
            }else{
                System.out.println(hostname + " actually received Seq = " + receivedSeq + " while expected Seq = " + expectSeq + ". Packet Wasted.");
                receiveLog.writeLog("pdu_exp=" + expectSeq + ", pdu_recv=" + receivedSeq + ", status=NoErr");
                totalReceive++;
                noErrReceive++;
                sendACK(expectSeq-1, "TO",receivePacket.getAddress(), receivePacket.getPort());
            }
        }
    }

    public void timeOut(int maxACK, String Status) throws IOException{

        System.out.println(hostname + "wait for ACK out of time, resend Seq:" + base + "--" + (nextSeq-1));
        Package pk = new Package("", 0);
        for (int i = base; i < nextSeq; i++)
        {
            String data = Datalist.get(i-1);
            String sendDataLabel = hostname + ": Sending to port " + destPort + ", Seq = " + i
                    +", isDataCarried = "+isSenData+" length = "+data.length() +", DATA_NUMBER = "+DATA_NUMBER_SED+"########";
            data = pk.addCRC(data);
            if(Math.random() < 1.0/errorrate){
                data += "0";
            }
            data += "########";
            byte[] datagram = (sendDataLabel + data).getBytes();

            DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length, destAddress, destPort);
            sendSocket.send(datagramPacket);

            System.out.println(hostname + " resend to " + destPort + " Seq = " + i);
            if(i != base){
                Status = "New";
            }
            sendLog.writeLog("pdu_to_send=" + i + ", status="+ Status +", ackedNo=" + maxACK);
            if(Status.equals("RT")){
                RTSend++;
            }
            if(Status.equals("TO")){
                TOSend++;
            }
            totalSend++;
        }
    }


    protected void sendACK(int seq,String type, InetAddress toAddr, int toport) throws IOException {
        String response = hostname + " response ACK: " + seq + "####" + type;
        byte[] responseData = response.getBytes();

        DatagramPacket responsePacket = new DatagramPacket(responseData, response.length(), toAddr, toport);
        receiveSocket.send(responsePacket);
    }

    public String getHostname(){return hostname;}

    public void setDestAddress(InetAddress destAddress){this.destAddress = destAddress;}

    public int getDestPort(){return destPort;}

    public void setDestPort(int destPort){this.destPort = destPort;}


}
