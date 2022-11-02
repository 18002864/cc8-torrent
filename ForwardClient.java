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
    
    public void request(String destino, String fileName, String fileSize) {
        try {
            logAplicacion.add("solicitar(" + destino + "," + fileName + "," + fileSize + ")");
            this.fileName = fileName;
            this.fileLength = 0;
            this.chunks = new HashMap<Integer, String>();
            this.message = "";
            this.fileSize = Integer.parseInt(fileSize);
            logAplicacion.add("Forward Cliente: solicitar archivo " + fileName + " from:" + distanceVectorAlgorithm.myNode + " to:" + destino);
            
            String atraves = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(destino).get("atraves"); // ruta segun Distance Vector
            System.out.println(atraves);
            atraves = "G";
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(atraves).get("ip");
            System.out.println(ip);

            // Envio de solicitud de archivo (pendiente cambiar a JSON)
            Socket socketClient = new Socket(ip, this.fordwardPort);
            PrintWriter outSocket = new PrintWriter(socketClient.getOutputStream(), true);
            String info = "From:" + distanceVectorAlgorithm.myNode;
            info += "\n" + "To:" + destino;
            info += "\n" + "Name:" + fileName;
            info += "\n" + "Size:" + fileSize;
            info += "\n" + "EOF";
            outSocket.println(info);
            outSocket.close();
            socketClient.close();
            
            // Metodo para esperar el archivo


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
