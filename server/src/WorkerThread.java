// WorkerThread.java: A 'worker' Thread
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
 * This thread is the workhorse of the server, it receives queued requests from
 * the Server thread, parses them, performs the correct action, and responds to
 * the client with either the result of the request or if it was accepted.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class WorkerThread extends Thread{
    // A thread to parse requests and send replies

    Server source;
    DatagramSocket socket;
    Dictionary dict;

    WorkerThread(Server source, DatagramSocket socket, Dictionary dict){
        this.source = source;
        this.socket = socket;
        this.dict = dict;
    }

    @Override
    public void run() {
        while(true) {
            // Lifecycle Loop
            DatagramPacket request;
            synchronized (this) {
                // Check queue
                try {
                    if ((request = source.deQueue()) == null) {
                        // The queue is empty
                        source.freeThread();
                        this.wait();
                        // Notified of A new request
                        continue;
                    }
                    // Got a request

                    // Do something with the request
                    System.out.println("[Worker " + this.getId() + "] Client Request from " + request.getAddress() + ":" + request.getPort());
                    String requestString = new String(request.getData()).trim();
                    System.out.println("[Worker " + this.getId() + "] Request contents: " + requestString);
                    String response;
                    response = parse_request(requestString);

                    byte[] responseBuffer = response.getBytes();

                    // Send a reply
                    DatagramPacket reply = new DatagramPacket(responseBuffer, response.length(), request.getAddress(), request.getPort());
                    int replyTotal = response.length();
                    try {
                        // System.out.println("[Worker " + this.getId() + "] Responding with: " + response);
                        System.out.println("[Worker " + this.getId() + "] Responding...");
                        // wait(1000);
                        socket.send(reply);
                    } catch (IOException e) {
                        System.err.println("[Worker " + this.getId() + "] IOException in worker thread: " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized String parse_request(String requestString) {
        // Parse the request
        String[] vals;
        vals = requestString.split(" ", 2);
        String response = "OK 200";
        boolean success = true;
        if(vals.length > 1) {
            switch (vals[0]) {
                case "SET":
                    success = newDef(vals[1]);
                    break;
                case "DEL":
                    success = delDef(vals[1]);
                    break;
                case "PUT":
                    success = updateDef(vals[1]);
                    break;
                case "GET":
                    response = getWord(vals[1]);
                    break;
                default:
                    response = "ERR 500";
                    break;
            }
        } else if(vals.length == 0) {
            success = false;
        } else if(vals[0].equals("GETLIST")) {
            response = getList();
        } else {
            response = getWord(vals[0]);
        }

        if(!success){
            return "ERR 500";
        }
//        System.out.println("to return: "+response);
        return response;
    }

    private String getWord(String arguments){
        // not writing anything to the database so should be pretty simple to deal with
        String[] args = arguments.split(" ");
        String definition;
        if(args.length > 1){
            definition = dict.getWordDef(args[0], Integer.parseInt(args[1]));
        } else {
            definition = dict.getWord(args[0]);
        }

        if(definition == null){
            definition = "ERR 404: Not found";
        }
        // parse definition variable
        return definition;
    }
    private String getList(){
        return dict.getWordList();
    }
    private synchronized boolean newDef(String arguments){
        String[] args = arguments.split(" ", 2);
        if(args.length == 2) {
            String word = args[0];
            String[] defs = args[1].split("\\|");
            for (String def: defs){
                return dict.updateDef(word, def);
            }
        }
        return false;
    }
    private synchronized boolean delDef(String arguments){
        String[] args = arguments.split(" ");
        if(args.length == 2){
            dict.removeDef(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            dict.removeDef(args[0], -1);
        } else {
            return false;
        }
        return true;
    }
    private synchronized boolean updateDef(String arguments){
        String[] args = arguments.split(" ", 3);
        int defNum;
        String def;
        try{
            defNum = Integer.parseInt(args[1]);
            def = args[2];
        } catch(NumberFormatException e){
            defNum = -1;
            def = args[1];
        }

        return dict.updateDef(args[0], defNum, def);
    }
}
