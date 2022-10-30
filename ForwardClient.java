import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import utils.Constants;

public class ForwardClient {
    protected Log logAplicacion;
    protected Integer fordward_port;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm;
    


    public ForwardClient(Log logAplicacion, DistanceVectorAlgorithm distanceVectorAlgorithm) {
        this.logAplicacion = logAplicacion;
        this.fordward_port = Constants.FORWARD_PORT;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
    }

    public HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    public Integer totalLen = 0;
    public String name = "";
    public Boolean esperando = false;
    public Semaphore sp = new Semaphore(1);
    public Boolean hayMsg = false;
    public String msg = "";
    public boolean lockConsola = false;
    public Integer localSize = 0;

    public void solicitar(String destino, String name, String size) {
        try {
            this.lockConsola = true;
            logAplicacion.add("solicitar(" + destino + "," + name + "," + size + ")");
            this.name = name;
            this.totalLen = 0;
            this.chunks = new HashMap<Integer, String>();
            this.esperando = true;
            this.hayMsg = false;
            this.msg = "";
            this.localSize = Integer.parseInt(size);

            logAplicacion.add("Forward Cliente: solicitar archivo " + name + " from:" + distanceVectorAlgorithm.myNode + " to:" + destino);
            String atraves = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(destino).get("atraves"); // ruta segun Distance Vector
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(atraves).get("ip");
            Socket socketClient = new Socket(ip, this.fordward_port);
            PrintWriter outSocket = new PrintWriter(socketClient.getOutputStream(), true);
            String info = "From:" + distanceVectorAlgorithm.myNode;
            info += "\n" + "To:" + destino;
            info += "\n" + "Name:" + name;
            info += "\n" + "Size:" + size;
            info += "\n" + "EOF";
            outSocket.println(info);
            outSocket.close();
            socketClient.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostrar() {
        try {
            File dir = new File("./files");
            String[] children = dir.list();
            if (children == null) {
                System.out.println("No hay archivos");
            } else {
                System.out.println("\n"+" ".repeat(10) + "Listado de archivos\n");
                for (int i = 0; i < children.length; i++) {
                    String filename = children[i];
                    File temp = new File("./files/"+filename);
                    String texto = (i + 1) + " > " + filename;
                    String espacio = "_".repeat(30-texto.length());
                    System.out.println(texto + espacio + temp.length() + " bytes");
                }
                System.out.println();
            }
            if(this.lockConsola){
                this.lockConsola = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
