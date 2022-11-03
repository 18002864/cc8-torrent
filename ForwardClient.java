import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import utils.Constants;

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
            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(this.destiny).get("atraves"); // ruta segun Distance Vector
            //System.out.println(through);
            //through = "G";
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
            //System.out.println(ip);

            
            // Envio de solicitud de archivo (pendiente cambiar a JSON)
            Socket socketClient = new Socket(ip, this.fordwardPort);
            PrintWriter outSocket = new PrintWriter(socketClient.getOutputStream(), true);
            String info = "From:" + distanceVectorAlgorithm.myNode;
            info += "\n" + "To:" + this.destiny;
            info += "\n" + "Name:" + this.fileName;
            info += "\n" + "Size:" + this.fileSize;
            info += "\n" + "EOF";
            
            outSocket.println(info);
            outSocket.close();
            socketClient.close();

            logAplicacion.add("From:" + distanceVectorAlgorithm.myNode);
            logAplicacion.add("To:" + this.destiny);
            logAplicacion.add("Name:" + this.fileName);
            logAplicacion.add("Size:" + this.fileSize);
            logAplicacion.add("EOF");        
            logAplicacion.add("---- End Send Request ----\n");
            

        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
}
