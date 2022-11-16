import java.net.ServerSocket;
import utils.Constants;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class ForwardServer implements Runnable {

    Log logForwardServer = null;
    Integer fordwardPort = Constants.FORWARD_PORT;
    ServerSocket serverSocket;
    
    DistanceVectorAlgorithm distanceVectorAlgorithm;
    ForwardClient forwardClient;

    protected String neighbour = "";

    protected HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    protected Integer fileLength = 0;

    public ForwardServer(Log logForwardServer, DistanceVectorAlgorithm distanceVectorAlgorithm, ForwardClient forwardClient) {
        this.logForwardServer = logForwardServer;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.forwardClient = forwardClient;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(this.fordwardPort);
            logForwardServer.add("Forward Server is running on port " + this.fordwardPort + "\n");
            while (true) {
                
                try {
                    ForwardServerThread forwardServerThread = new ForwardServerThread(this.serverSocket.accept(), 
                        this.distanceVectorAlgorithm, 
                        this.logForwardServer, 
                        this.forwardClient);
                    new Thread(forwardServerThread).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                
            }           


                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
