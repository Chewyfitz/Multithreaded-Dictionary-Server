// Dictionary.java: The dictionary itself
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
*  An interface for the (simple) dictionary protocol
*/
import com.google.gson.*;

import java.io.IOException;
import java.net.*;

public class ServerIO {

    public static final int DEF_TIMEOUT = 3000;

    private InetAddress host;
    private String hostname;
    private Integer port;
    private DatagramSocket dsock;
    ServerIO(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname(){
        return this.hostname;
    }
    public int getPort(){
        return this.port;
    }

    public boolean openSocket(){
        System.out.println("Opening socket to \""+hostname+"\" on port "+port.toString());
        try {
            host = InetAddress.getByName(hostname);
            this.dsock = new DatagramSocket();
            dsock.setSoTimeout(DEF_TIMEOUT);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean closeSocket(){
        if(this.dsock != null && !this.dsock.isClosed()){
            dsock.close();
            return true;
        }
        return false;
    }

    private String sendCommand(String command){
        byte[] message = command.getBytes();
        byte[] recvBuff = new byte[2000];
        DatagramPacket request = new DatagramPacket(message, command.length(), this.host, this.port);
        DatagramPacket response = new DatagramPacket(recvBuff, recvBuff.length);
        try{
            System.out.println("Sending command: "+command);
            dsock.send(request);
            // It's fine to wait for a response in this thread since this is setting up
            // the initial connection
            dsock.receive(response);
            System.out.println(new String(response.getData()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(response.getData()).trim();
    }

    public String[] getWordList() {
        String command = "GETLIST";

        // Parse JSON response as list of strings
        return new Gson().fromJson(sendCommand(command), String[].class);

    }
    public String[] getDefs(String word){
        String command = "GET " + word;

        // Parse JSON response as list of strings
        return new Gson().fromJson(sendCommand(command), String[].class);
    }
    public boolean setDef(String word, int index, String def){
        String command = "PUT " + word+" "+index+" "+def;
        boolean result;
        try{
            result = !(sendCommand(command).equals("ERR 500"));
        } catch (NullPointerException e){
            result = false;
        }
        return result;
    }
    public boolean addWord(String word, String def){
        String command = "SET " + word+" "+def;
        String response = sendCommand(command);
        return response != null && !response.equals("ERR 500");
    }
    public boolean removeWord(String word){
        return removeDef(word, -1);
    }
    public boolean removeDef(String word, int index){
        String command;
        if(index < 0){
            command = "DEL "+word;
        } else {
            command = "DEL "+word+" "+index;
        }
        String response = sendCommand(command);
        return response != null && !response.equals("ERR 500") && !response.equals("ERR 404");
    }




}
