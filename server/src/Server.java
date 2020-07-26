// Server.java: The main Server thread
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015
 * Here we deal with creating threads, receiving requests, and delegating those
 * requests to free threads.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {

    public static int PORT_DEFAULT = 8080;
    public static int numWorkers = 10;

    public int port;
    public String filename;

    Server(int port, String filename){
        this.port = (port != 0 ? port : PORT_DEFAULT);
        this.filename = filename;
    }

    AtomicInteger numFreeWorkers;
    private WorkerThread[] workers;
    private Dictionary dict;

    private volatile LinkedBlockingQueue<DatagramPacket> requestQueue;


    public void startServer() throws IOException{

        // Register Service on provided port (or default port if none provided)
        DatagramSocket dsock = new DatagramSocket(this.port);
        DatagramSocket sendSock = new DatagramSocket(this.port+1);

        // Establish variables for holding information
        requestQueue = new LinkedBlockingQueue<>();

        // A thread to deal with dictionary operations
        dict = new Dictionary(this.filename);
        dict.start();

        // Make some threads to deal with requests
        numFreeWorkers = new AtomicInteger(numWorkers);
        workers = new WorkerThread[numWorkers];
        for(int i=0; i<numWorkers; i++){
            workers[i] = new WorkerThread(this, sendSock, dict);
            workers[i].start();
        }


        // Accept requests
        while (true) {
            byte[] buffer = new byte[1000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);

            dsock.receive(request);

            // Add requests to the queue and delegate them to a thread
            // (if threads are not free they will respond to requests until the queue is empty)
            System.out.println("[Main] Client Request from " + request.getAddress() + ":" + request.getPort());
            addQueue(request);
            if(numFreeWorkers.get() > 0){
                // There is at least one free thread
                for(WorkerThread worker : workers){
                    if(worker.getState() == Thread.State.WAITING){
                        // Awaken a free thread to handle the request
                        synchronized (worker){
                            System.out.println("[Main] Sending connection to Thread: [Worker " + worker.getId()+"]");
                            worker.notify();
                        }
                        break;
                    }
                }
            }
        }
    }

    // A thread can report that it is free back to the server thread
    public void freeThread(){
        this.numFreeWorkers.incrementAndGet();
    }

    // Threads remove a request from the queue for action
    public DatagramPacket deQueue() throws InterruptedException {
        if(!requestQueue.isEmpty()){
            return requestQueue.take();
        }
        return null;
    }

    // Main loop adds requests to the queue
    public void addQueue(DatagramPacket request){
        requestQueue.add(request);
    }
}