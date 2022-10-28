import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class ServerListener implements Runnable {

    protected Socket socket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm = null;
    protected Integer wait_reconnection = 1;
    protected Log log;
    protected String vecino = "";

    public ServerListener(Socket socket, DistanceVectorAlgorithm distanceVectorAlgorithm, Integer wait_reconnection,
            Log log) {
        this.socket = socket;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.wait_reconnection = wait_reconnection;
        this.log = log;
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        try {
            PrintWriter outSocket = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.log.add("Waiting");
            while (true) {

                String request = inSocket.readLine();
                if (request == null)
                    break;
                if (request.contains("From:")) {
                    String[] arequest = request.split(":");
                    this.vecino = arequest[1];
                    continue;
                } else if (request.contains("Type")) {
                    String[] arequest = request.split(":");
                    String type = arequest[1];
                    this.log.add("Node " + this.vecino + " answer " + arequest[1]);
                    if (type.contains("HELLO")) {

                        String test = "From:" + distanceVectorAlgorithm.myNode;
                        test += "\n" + "Type:WELCOME";
                        outSocket.println(test);

                        this.distanceVectorAlgorithm.threadResource.acquire();
                        this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, true);
                        this.distanceVectorAlgorithm.threadResource.release();
                    } else if (type.contains("DV")) {
                        this.readDistancesVectorFromNeighbour(inSocket, this.vecino);
                    } else if (type.contains("KeepAlive")) {
                        this.log.add("Keep Alive from node " + this.vecino);
                    }
                    continue;
                }
            }
            try {
                this.log.add("Connection lost with node " + this.vecino + " as NULL");
                Long now = System.currentTimeMillis();
                this.distanceVectorAlgorithm.threadResource.acquire();
                this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, false);
                this.distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                this.distanceVectorAlgorithm.threadResource.release();
                while (true) {
                    if (System.currentTimeMillis() - now >= (this.wait_reconnection * 1000)) {
                        if (!this.distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {
                            log.add("Cannot reconnect with node " + this.vecino);

                            this.distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourCost(this.vecino, "99");
                            this.distanceVectorAlgorithm.threadResource.release();
                            break;
                        } else {
                            log.add("Reconnecting");
                            log.add("Connection reached with node " + this.vecino);
                            break;
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (SocketException e) {
            if (e.toString().contains("Connection reset")) {
                try {
                    this.log.add("");
                    this.log.add("---- Server Error ---- ");
                    // this.log.add("Connection reset Se perdio conexion con " + this.vecino);
                    this.log.add("Node lost " + this.vecino + "connection reset");
                    Long now = System.currentTimeMillis();
                    this.distanceVectorAlgorithm.threadResource.acquire();
                    this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, false);
                    this.distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                    this.distanceVectorAlgorithm.threadResource.release();
                    while (true) {
                        if (System.currentTimeMillis() - now >= (this.wait_reconnection * 1000)) {
                            if (!this.distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {

                                this.log.add("Cannot reconnect with neighbour" + this.vecino);
                                this.distanceVectorAlgorithm.threadResource.acquire();
                                this.distanceVectorAlgorithm.updateNeighbourCost(this.vecino, "99");
                                this.distanceVectorAlgorithm.threadResource.release();
                                this.log.add("");
                                this.log.add("---- Server Error ---- ");
                                break;
                            } else {
                                log.add("Reconnecting");
                                log.add("Connection reached with node " + this.vecino);
                                break;
                            }
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readDistancesVectorFromNeighbour(BufferedReader inSocket, String vecino) {
        try {
            boolean leido = false;
            HashMap<String, String> datos = new HashMap<String, String>();
            Integer len = 0;
            Integer locallen = 1;
            while (!leido) {
                String request = inSocket.readLine();
                this.log.add("Node " + this.vecino + " ----> " + request);
                if (request.contains("Len")) {
                    String[] arequest = request.split(":");
                    len = Integer.parseInt(arequest[1]);
                    continue;
                } else {
                    String[] arequest = request.split(":");
                    datos.put(arequest[0], arequest[1]);
                    if (locallen == len) {
                        leido = true;
                    }
                    locallen += 1;
                    continue;
                }
            }

            distanceVectorAlgorithm.threadResource.acquire();
            // this.log.add("distanceVectorAlgorithm.establecerRutas(" + datos + " , " +
            // vecino + ")");
            distanceVectorAlgorithm.stablishRoutes(datos, vecino);
            // this.log.add("distanceVectorAlgorithm.calcular(" + datos + " , " + vecino +
            // ")");
            distanceVectorAlgorithm.calculateBellmanFord(vecino);
            distanceVectorAlgorithm.threadResource.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
