// DictionaryClient.java: A client for DictionaryServer.java
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
* A simple networked application in two parts to communicate a simple dictionary service over a distributed system
* using UDP.
*/

import javafx.application.Application;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
//import com.google.gson.Gson;

public class DictionaryClient{
    InetAddress aHost;
    int PORT;

    public DictionaryClient(){
        this.aHost = null;
        this.PORT = 0;
    }

    DictionaryClient(InetAddress aHost, int PORT){
        this.aHost = aHost;
        this.PORT = PORT;

        this.commandInterface();
    }

    public static void main(String[] args) {
        // This is kind of dumb, but it provides a way to run a command-line program
        // since JavaFX kills all the command-line arguments
        if(args.length>0 && args[0].equals("-cli")){
            String[] args2 = Arrays.copyOfRange(args, 1, args.length);
            CLIent(args2);
        } else {
            new Thread(() -> Application.launch(DictionaryGUI.class)).start();
        }
    }

    private static void CLIent(String[] args){
        if(args.length > 2 || args.length < 1){
            System.err.println("Requires 1 or 2 Arguments.");
            System.err.println("Usage: java -jar DictionaryClient.jar [server-address] <port>");
            System.exit(1);
        }

        InetAddress aHost = null;
        try {
            aHost = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e){
            System.err.println("Connection Failed.\nUnknown Host: "+e.getMessage());
            System.exit(1);
        } catch (SecurityException e){
            System.err.println("Connection Failed.\nSecurity Error: "+e.getMessage());
            System.exit(1);
        }

        int PORT = 8080;
        try{
            PORT = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Port omitted, using default (8080)");
        }

        System.out.println("Distributed dictionary system v1.0");
        System.out.println("Enter Command");

        new DictionaryClient(aHost, PORT);
    }


    private void commandInterface(){

        // Open a socket for communication with the server
        // This could be split off into a separate thread, but for a command line it makes
        // more sense if everything is inline.
        while(true) {
            try (DatagramSocket clientDsock = new DatagramSocket()) {
                // TODO: do this multiple times
                Scanner inputLine = new Scanner(System.in);
                System.out.print("> ");

                String cmdBuff = inputLine.nextLine().trim();

//                Command cmd = new Command(cmdBuff);
                System.out.println("Request: " + cmdBuff);

                String message = cmdBuff;

                byte[] sendBuffer = message.getBytes();

                DatagramPacket request = new DatagramPacket(sendBuffer, message.length(), aHost, PORT);

                clientDsock.send(request);
                byte[] receiveBuffer = new byte[1000];
                DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientDsock.setSoTimeout(3000);
                clientDsock.receive(response);

                System.out.println("Response: " + new String(response.getData()).trim());
            } catch (SocketException e) {
                System.err.println("Socket Error: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("IO Error: " + e.getMessage());
            }
        }
    }
}
