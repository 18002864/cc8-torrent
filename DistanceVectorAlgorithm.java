import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class DistanceVectorAlgorithm {

    // representa que nodo soy en la topologia
    public String minodo = "";
    // contiene todos los destinos [A,B,C]
    public LinkedList<String> destinos = new LinkedList<String>();
    /* Distance Vector */
    public HashMap<String, HashMap<String, HashMap<String, String>>> vecinos = new HashMap<String, HashMap<String, HashMap<String, String>>>();
    public HashMap<String, HashMap<String, HashMap<String, String>>> dv = new HashMap<String, HashMap<String, HashMap<String, String>>>();

    public String config = "";
    public HashMap<String, HashMap<String, String>> hostVecinos = new HashMap<String, HashMap<String, String>>();
    // referencia al log
    // public LogProyecto log_p;
    // contiene la información de las rutas
    public HashMap<String, String> datos = new HashMap<String, String>();
    // flag que indica si hay cambios que transmitir
    public Boolean hayCambios = false;

    // para modificaciones concurrentes en el Distance Vector
    // We'll start with java.util.concurrent.Semaphore.
    // We can use semaphores to limit the number of concurrent threads accessing a
    // specific resource.
    public Semaphore sp = new Semaphore(1);

    /* Para ver el estatus y el comportamiento */
    public HashMap<String, Boolean> vecinoNotificado = new HashMap<String, Boolean>(); // Cliente
    public HashMap<String, Boolean> vecinoEscuchando = new HashMap<String, Boolean>(); // Cliente
    public HashMap<String, Boolean> vecinoConectado = new HashMap<String, Boolean>(); // Server

    public DistanceVectorAlgorithm() {

    }

    public void readConfigFile() {

    }

    public void initDistanceVectorAlgorithm() {

    }

    public void stablishRoutes() {

    }

    public void calculateBellmanFord() {

    }

}
