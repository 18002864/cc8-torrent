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
        distanceVectorAlgorithm(this.routeMap, myNode, true);
        stablishRoutes(this.routeMap, myNode);
        log.add("this.vecinoNotificado=" + this.neighbourNotify);
        log.add("this.vecinoConectado=" + this.neighbourConnected);
        log.add("this.vecinoEscuchando=" + this.neighbourListening);
        changesFlag = true;
        log.add("this.hayCambios=" + changesFlag);
        dibujar();

        Timer timer = new Timer();
        // para monitorear el distance vector
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (changesFlag) {
                        Integer len = neighbourNotify.size();
                        Integer acc = 0;
                        for (String vecinoi : neighbourNotify.keySet()) {
                            if (!neighbourNotify.get(vecinoi)) {
                                if (!neighbourNotify.get(vecinoi)) {
                                    // si no esta conectado, poner como notificado
                                    neighbourNotify.replace(vecinoi, true);
                                }
                            }
                            acc += neighbourNotify.get(vecinoi) ? 1 : 0;
                        }
                        if (len == acc) {
                            changesFlag = false; // si todos han sido notificados, entonces reset de la variable
                        }
                    }
                    log.add(" ------------- Monitoreo ------------- ");
                    log.add("Notificados " + neighbourNotify);
                    log.add("Conectados " + neighbourConnected);
                    log.add("Escuchando " + neighbourListening);
                    log.add("Cambios en el Distance Vector " + changesFlag);
                    dibujar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, (15000), (15000));

    }

    public void distanceVectorAlgorithm(HashMap<String, String> datos, String node, Boolean firstTime) {

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
        for (String nodeFromRouteMap : datos.keySet()) {
            if (!this.otherNodes.contains(nodeFromRouteMap)) {
                this.otherNodes.add(nodeFromRouteMap);
            }
            whereToGoMap = new HashMap<String, String>();
            if (datos.get(nodeFromRouteMap).contains("99")) {
                whereToGoMap.put("hop", "");
            } else {
                whereToGoMap.put("hop", nodeFromRouteMap);
            }
            whereToGoMap.put("cost", datos.get(nodeFromRouteMap));
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

    public void stablishRoutes(HashMap<String, String> routeMapParam, String neighbour) {
        HashMap<String, HashMap<String, String>> costMap = new HashMap<String, HashMap<String, String>>();
        HashMap<String, String> whereToGoMap = new HashMap<String, String>();

        /* Agregar vecino a los destinos y establecer costo asi mismo */
        if (!this.otherNodes.contains(myNode)) {
            this.otherNodes.add(myNode);
        }
        whereToGoMap.put("hop", neighbour);
        whereToGoMap.put("cost", "0");
        costMap.put(neighbour, whereToGoMap);

        /* Agregar vecinos de mi vecino a destinos y guardar el costo */
        for (String neighbourIndex : routeMapParam.keySet()) { // [Y,X]
            if (!this.otherNodes.contains(neighbourIndex)) {
                this.otherNodes.add(neighbourIndex);
            }
            whereToGoMap = new HashMap<String, String>();
            whereToGoMap.put("hop", neighbourIndex);
            whereToGoMap.put("cost", routeMapParam.get(neighbourIndex));
            costMap.put(neighbourIndex, whereToGoMap);
        }

        /* Si existe el vecino, actualizar */
        if (this.neighbours.containsKey(neighbour)) {
            this.neighbours.remove(neighbour);
        }
        this.neighbours.put(neighbour, costMap);

        /* verificar el costo del vecino si es 99 */
        if (!this.myNode.contains(neighbour)) {
            if (this.neighbours.get(this.myNode).get(neighbour).get("cost").contains("99")) {
                String nuevoCosto = this.neighbours.get(neighbour).get(this.myNode).get("cost");
                this.neighbours.get(this.myNode).get(neighbour).remove("cost");
                this.neighbours.get(this.myNode).get(neighbour).remove("hop");
                this.neighbours.get(this.myNode).get(neighbour).put("cost", nuevoCosto);
                this.neighbours.get(this.myNode).get(neighbour).put("hop", neighbour);
            }
        }

    }

    public void dibujar() {
        String header = " ".repeat(this.myNode.length()) + " |";
        // A | A | B | ...
        String body = this.myNode + " |";
        // A | CA |
        Collections.sort(this.otherNodes);

        for (var destino : this.otherNodes) {
            header += " ".repeat(15) + destino + " ".repeat(15 - destino.length()) + " |";
        }

        for (var i : this.distanceVectorHashMap.values()) {
            List<String> sortedList = new ArrayList<String>(i.keySet());
            Collections.sort(sortedList);
            for (var destino : sortedList) {
                String costo = i.get(destino).get("cost") + i.get(destino).get("hop");
                body += " ".repeat(15) + costo + " ".repeat(15 - costo.length()) + " |";
            }
        }
        log.add("-".repeat(header.length()));
        log.add(header);
        log.add("-".repeat(header.length()));
        log.add(body);
        log.add("-".repeat(header.length()));
    }

    public void calculateBellmanFord(String node) {
        /* Version antes del calculo */
        String antes = this.distanceVectorHashMap.toString();
        this.log.warning("Antes " + antes);

        /* Recorro todos los destinos */
        for (String destino : otherNodes) {
            HashMap<String, String> atraves = new HashMap<String, String>();
            // si el destino no esta en mi dv, inicializarlo con infinito
            if (!this.distanceVectorHashMap.get(this.myNode).containsKey(destino)) {
                atraves = new HashMap<String, String>();
                atraves.put("cost", "99");
                atraves.put("hop", "");
                this.distanceVectorHashMap.get(this.myNode).put(destino, atraves);
            }
            /* Algoritmo de Bellman-Ford */

            // Dx(Y)
            String linea = "";
            linea += "D" + this.myNode + "(" + destino + ")";
            // C(x,v)
            linea += " = C(" + this.myNode + "," + node + ") : ";
            int c_me_to_vecino = 99;
            if (this.neighbours.get(this.myNode).containsKey(node)) {
                c_me_to_vecino = Integer.parseInt(this.neighbours.get(this.myNode).get(node).get("cost"));
            }
            linea += c_me_to_vecino + " + ";
            // Dv(y)
            linea += "D" + node + "(" + destino + ") : ";
            int c_vecino_to_dest = 99;
            if (this.neighbours.get(node).containsKey(destino)) {
                c_vecino_to_dest = Integer.parseInt(this.neighbours.get(node).get(destino).get("cost"));
            }
            linea += Integer.toString(c_vecino_to_dest);
            // Total
            int total = (c_me_to_vecino + c_vecino_to_dest) > 99 ? 99 : (c_me_to_vecino + c_vecino_to_dest);
            linea += " Total: " + total;
            this.log.add(linea);
            // Comparar el costo y actualizar si fuera necesario en el DV
            int costoActual = Integer.parseInt(this.distanceVectorHashMap.get(this.myNode).get(destino).get("cost"));
            if (total < costoActual) {
                this.distanceVectorHashMap.get(this.myNode).remove(destino);
                atraves = new HashMap<String, String>();
                atraves.put("cost", Integer.toString(total));
                atraves.put("hop", node);
                this.distanceVectorHashMap.get(this.myNode).put(destino, atraves);
            }
        }
        /* Evaluar si hubieron cambios e indicar si los hay */
        String despues = this.distanceVectorHashMap.toString();
        this.log.warning("Despues " + despues);
        if (!antes.equals(despues)) {
            this.updateChangeNeighbourNotify();
        }
    }

    public void updateNeighbourListening(String vecino, Boolean escuchando) {
        if (this.neighbourListening.containsKey(vecino)) {
            this.neighbourListening.remove(vecino);
        }
        this.neighbourListening.put(vecino, escuchando);
    }

    public void updateNeighbourNotify(String vecino, Boolean notificado) {
        if (this.neighbourNotify.containsKey(vecino)) {
            this.neighbourNotify.remove(vecino);
        }
        this.neighbourNotify.put(vecino, notificado);
    }

    public void updateNeighbourConnected(String vecino, Boolean conectado) {
        if (this.neighbourConnected.containsKey(vecino)) {
            this.neighbourConnected.remove(vecino);
        }
        this.neighbourConnected.put(vecino, conectado);
    }

    public void updateChangeNeighbourNotify() {
        this.changesFlag = true;
        LinkedList<String> temp = new LinkedList<String>();
        for (var vecino : this.neighbourNotify.keySet()) {
            temp.add(vecino);
        }
        for (var vecino : temp) {
            this.neighbourNotify.remove(vecino);
            this.neighbourNotify.put(vecino, false);
        }
    }

    public void updateNeighbourCost(String vecino, String costo) {
        HashMap<String, String> datos = new HashMap<String, String>();
        this.log.warning("Actualizar costo " + costo + " de " + vecino);
        // poner el costo en mi ruta
        for (String vecinoi : this.neighbours.get(this.myNode).keySet()) {
            if (!this.myNode.contains(vecinoi)) { // no agregarme a mi mismo
                if (vecinoi.equals(vecino)) {
                    datos.put(vecinoi, costo);
                } else {
                    String costooriginal = this.neighbours.get(this.myNode).get(vecinoi).get("costo");
                    datos.put(vecinoi, costooriginal);
                }
            }
        }
        this.distanceVectorAlgorithm(datos, this.myNode, false);
        this.stablishRoutes(datos, this.myNode);
        this.updateChangeNeighbourNotify();
        this.log.warning("Fin actualizar costo " + costo + " de " + vecino);
    }

}
