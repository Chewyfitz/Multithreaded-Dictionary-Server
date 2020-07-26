// DictionaryServer.java: A server for DictionaryClient.java
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
 * A simple networked application in two parts to communicate a simple dictionary service over a distributed system
 * using UDP.
 */

import java.io.*;

public class DictionaryServer {

    public static void main(String[] args) throws IOException {
        Server server = parse_args(args);
        server.startServer();
    }

    private static Server parse_args(String[] args) throws IOException{
        if(args.length > 2 || args.length < 1){
            System.err.println("Requires 1 or 2 Arguments.");
            System.err.println("Usage: java -jar DictionaryServer.jar [port] <dictionary-file>");
            System.exit(1);
        }
        int port = 0;
        String filename;
        try{
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Port omitted, using default (8080)");
        }

        if(port != 0) {
            filename = args[1];
        } else {
            filename = args[0];
        }
        FileReader test_existence;
        try{
            test_existence = new FileReader(filename);
            test_existence.close();
        } catch (FileNotFoundException e){
            System.err.println("File not found: " + (port!=0?args[1]:args[0]));
            System.err.println("Usage: java -jar DictionaryServer.jar [port] <dictionary-file>");
            System.exit(1);
        }
        return new Server(port, filename);
    }
}
