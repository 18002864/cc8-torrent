import java.net.Socket;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import utils.Constants;

public class ForwardServerThread implements Runnable {

    protected Socket socket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm = null;
    protected ForwardClient forwardClient;
    protected Log logForwardServer;
    protected String neighbour = "";
    protected Integer fordwardPort = Constants.FORWARD_PORT;

    private PrintWriter writer;
    private BufferedReader in;

    protected HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    protected Integer totalLen = 0;

    public ForwardServerThread(Socket socket, DistanceVectorAlgorithm distanceVectorAlgorithm, Log logForwardServer, ForwardClient forwardClient) {
        this.socket = socket;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.logForwardServer = logForwardServer;
        this.forwardClient = forwardClient;
    }

    public void run() {
        try {
            
            writer = new PrintWriter(this.socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            
            this.logForwardServer.add("---- Start Receive Request ----");
            
            String from = "", to = "", name = "", size = "", data = "", frag = "", msg = "";
            while (true) {
                // Ciclo para esperar a recibir el mensaje completo
                while (true) {
                    
                    String message = in.readLine();
                    this.logForwardServer.add(message);
                    
                    if (message == null){
                        break;
                    }
                    if (message == ""){
                        continue;
                    }

                    String[] splitMessage = message.split(":");

                    
                    switch (splitMessage[0].trim()){
                            case "From":{
                                from = splitMessage[1].trim();
                                continue;
                            }
                            case "To":{
                                to = splitMessage[1].trim();
                                continue;
                            }
                            case "Name":{
                                name = splitMessage[1].trim();
                                continue;
                            }
                            case "Size":{
                                size = splitMessage[1].trim();
                                continue;
                            }
                            case "Data":{
                                data = splitMessage[1].trim();
                                continue;
                            }
                            case "Frag":{
                                frag = splitMessage[1].trim();
                                continue;
                            }
                            case "Msg":{
                                msg = splitMessage[1].trim();
                                continue;
                            }
                            case "EOF":{
                                break;
                            }
                            default: {
                                this.logForwardServer.add("Error in Request\n");
                                break;
                            }
                        
                    }
                    break;
                }

                this.logForwardServer.add("---- End Receive Request ----\n");

                if (this.distanceVectorAlgorithm.myNode.contains(to)) {
                    this.logForwardServer.add("---- El archivo es para mi ----\n");
                    
                    break;
                } else {

                    this.logForwardServer.add("---- El archivo no es para mi ----\n");
                    break;
                }
            
            }



            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}