import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.print.DocFlavor.STRING;

public class DistanceVectorAlgorithm {

    // representa que nodo soy en la topologia
    public String myNode = "";
    // contiene todos los destinos [A,B,C]
    public ArrayList<String> otherNodes = new ArrayList<String>();
    /* Distance Vector */
    public HashMap<String, HashMap<String, HashMap<String, String>>> neighbours = new HashMap<String, HashMap<String, HashMap<String, String>>>();
    public HashMap<String, HashMap<String, HashMap<String, String>>> distanceVectorHashMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();

    public String configFile = "";

    public HashMap<String, HashMap<String, String>> hostNeighbours = new HashMap<String, HashMap<String, String>>();
    // referencia al log
    public Log log;
    // contiene la información de las rutas
    public HashMap<String, String> routeMap = new HashMap<String, String>();
    // flag que indica si hay cambios que transmitir
    public Boolean changesFlag = false;

    // para modificaciones concurrentes en el Distance Vector
    // We'll start with java.util.concurrent.Semaphore.
    // We can use semaphores to limit the number of concurrent threads accessing a
    // specific resource.
    public Semaphore threadResource = new Semaphore(1);

    /* Para ver el estatus y el comportamiento */
    // Cliente
    public HashMap<String, Boolean> neighbourNotify = new HashMap<String, Boolean>();
    // Cliente
    public HashMap<String, Boolean> neighbourListening = new HashMap<String, Boolean>();
    // Server
    public HashMap<String, Boolean> neighbourConnected = new HashMap<String, Boolean>();

    public DistanceVectorAlgorithm(Log log) {
        this.log = log;
    }

    public void readConfigFile() {
        try {
            File file = new File("config.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            routeMap = new HashMap<String, String>();
            String readline = "";
            while ((readline = bufferedReader.readLine()) != null) {
                String[] configData = readline.split(",");
                if (configData.length == 1) {
                    myNode = configData[0];
                    continue;
                }
                routeMap.put(configData[0], configData[1]);
                HashMap<String, String> host = new HashMap<String, String>();
                host.put("ip", configData[2]);
                if (configData.length > 3) {
                    host.put("port", configData[3]);
                } else {
                    host.put("port", "9080");
                }
                hostNeighbours.put(configData[0], host);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        readConfigFile();
        log.add("My Node ---> " + myNode);
        distanceVectorAlgorithm(myNode, true);
    }

    public void distanceVectorAlgorithm(String node, Boolean firstTime) {

        HashMap<String, HashMap<String, String>> costMap = new HashMap<String, HashMap<String, String>>();
        HashMap<String, String> whereToGoMap = new HashMap<String, String>();

        /* Agregar mi nodo a los destinos */
        if (!this.otherNodes.contains(myNode)) {
            this.otherNodes.add(myNode);
        }
        whereToGoMap.put("hop", node);
        whereToGoMap.put("cost", "0");
        costMap.put(node, whereToGoMap);

        /* Agregar mis vecinos */
        for (String nodeFromRouteMap : routeMap.keySet()) {
            if (!this.otherNodes.contains(nodeFromRouteMap)) {
                this.otherNodes.add(nodeFromRouteMap);
            }
            whereToGoMap = new HashMap<String, String>();
            if (routeMap.get(nodeFromRouteMap).contains("99")) {
                whereToGoMap.put("hop", "");
            } else {
                whereToGoMap.put("hop", nodeFromRouteMap);
            }
            whereToGoMap.put("cost", routeMap.get(nodeFromRouteMap));
            costMap.put(nodeFromRouteMap, whereToGoMap);
            /* Inicializar variables para control */
            if (firstTime) {
                this.neighbourConnected.put(nodeFromRouteMap, false);
                this.neighbourNotify.put(nodeFromRouteMap, false);
                this.neighbourListening.put(nodeFromRouteMap, false);
            }
        }
        /* El primer Distance Vector */
        this.distanceVectorHashMap.put(node, costMap);
    }

    public void stablishRoutes() {

    }

    public void calculateBellmanFord() {

    }

}
