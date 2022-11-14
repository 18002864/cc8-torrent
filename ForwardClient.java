
import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import utils.Constants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ForwardClient {
    protected Log logAplicacion;
    protected Integer fordwardPort;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm;
    


    public ForwardClient(Log logAplicacion, DistanceVectorAlgorithm distanceVectorAlgorithm) {
        this.logAplicacion = logAplicacion;
        this.fordwardPort = Constants.FORWARD_PORT;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
    }

    public String fileName = "";
    public Integer fileLength = 0;
    public HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    public Integer fileSize = 0;
    public String message = "";
    public String destiny = "";

    
    public Semaphore sema = new Semaphore(1);
    public Boolean waitingFlag = false; // True si espera a que se complete el archivo
    public Boolean errorFlag = false; // True si existen un error en la transmision
    public Boolean consoleFlag = false; // True si se esta imprimiendo en consola
    
    public void request(String destiny, String fileName, String fileSize) {

            this.consoleFlag = true;
            logAplicacion.add("---- Start Send Request ----");
            logAplicacion.add("Parameters " + destiny + " " + fileName + " " + fileSize);
            this.destiny = destiny;
            this.fileName = fileName;
            this.fileSize = Integer.parseInt(fileSize);
            this.waitingFlag = true;
            this.errorFlag = false;
            
            
            // Metodo para enviar petición del archivo
            sendRequest();
            
            // Thread para esperar el archivo
            ForwardClientThread forwardClientThread = new ForwardClientThread(this, logAplicacion);
            new Thread(forwardClientThread).start();
        
    }

    public void sendRequest() {
        try {
            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(this.destiny).get("hop"); // ruta segun Distance Vector
            //System.out.println(through);
            //through = "G";
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
            //System.out.println(ip);

            
            // Envio de solicitud de archivo (Cambio a JSON)
            Socket socket = new Socket(ip, this.fordwardPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            MessageJson messageJson = new MessageJson();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String chunk = chunks.remove(0);

            messageJson.setType(MessageType.NORMAL);
            messageJson.setFileName(this.fileName);
            messageJson.setTotalLength(this.fileSize);
            messageJson.setChunk("");

            //En esta parte transformamos el objeto a un Json string
            writer.println(objectMapper.writeValueAsString(messageJson));
            
            writer.close();
            socket.close();
 
            logAplicacion.add("---- End Send Request ----\n");
            

        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
}
