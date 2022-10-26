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
            while (true) {
                String request = inSocket.readLine();
                if (request == null)
                    break;
                this.log.add("Request " + request);
                if (request.contains("From:")) {
                    String[] arequest = request.split(":");
                    this.vecino = arequest[1];
                    continue;
                } else if (request.contains("Type")) {
                    String[] arequest = request.split(":");
                    String type = arequest[1];
                    if (type.contains("HELLO")) {
                        /*
                         * outSocket.println("From:" + distanceVectorAlgorithm.myNode);
                         * outSocket.println("Type:WELCOME");
                         */
                        String test = "From:" + distanceVectorAlgorithm.myNode;
                        test += "\n" + "Type:WELCOME";
                        outSocket.println(test);
                        this.log.add(
                                "this.distanceVectorAlgorithm.updateNeighbourConnected(" + this.vecino + "," + true
                                        + ")");
                        this.distanceVectorAlgorithm.threadResource.acquire();
                        this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, true);
                        this.distanceVectorAlgorithm.threadResource.release();
                    } else if (type.contains("DV")) {
                        this.leerDistanceVectorVecino(inSocket, this.vecino);
                    } else if (type.contains("KeepAlive")) {
                        this.log.add("KeepAlive de " + this.vecino);
                    }
                    continue;
                }
            }
            try {
                this.log.add("Null Se perdio conexion con " + this.vecino);
                Long now = System.currentTimeMillis();
                this.distanceVectorAlgorithm.threadResource.acquire();
                this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, false);
                this.distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                this.distanceVectorAlgorithm.threadResource.release();
                while (true) {
                    if (System.currentTimeMillis() - now >= (this.wait_reconnection * 1000)) {
                        if (!this.distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {
                            log.add("No se recupero conexion con " + this.vecino);
                            log.add("this.distanceVectorAlgorithm.updateNeighbourCost(" + this.vecino + ",99);");
                            this.distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourCost(this.vecino, "99");
                            this.distanceVectorAlgorithm.threadResource.release();
                            break;
                        } else {
                            log.add("Se recupero conexion con " + this.vecino);
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
                    this.log.add("Connection reset Se perdio conexion con " + this.vecino);
                    Long now = System.currentTimeMillis();
                    this.distanceVectorAlgorithm.threadResource.acquire();
                    this.distanceVectorAlgorithm.updateNeighbourConnected(this.vecino, false);
                    this.distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                    this.distanceVectorAlgorithm.threadResource.release();
                    while (true) {
                        if (System.currentTimeMillis() - now >= (this.wait_reconnection * 1000)) {
                            if (!this.distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {
                                log.add("No se recupero conexion con " + this.vecino);
                                log.add("this.distanceVectorAlgorithm.updateNeighbourCost(" + this.vecino + ",99);");
                                this.distanceVectorAlgorithm.threadResource.acquire();
                                this.distanceVectorAlgorithm.updateNeighbourCost(this.vecino, "99");
                                this.distanceVectorAlgorithm.threadResource.release();
                                break;
                            } else {
                                log.add("Se recupero conexion con " + this.vecino);
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

    public void leerDistanceVectorVecino(BufferedReader inSocket, String vecino) {
        try {
            boolean leido = false;
            HashMap<String, String> datos = new HashMap<String, String>();
            Integer len = 0;
            Integer locallen = 1;
            while (!leido) {
                String request = inSocket.readLine();
                this.log.add("Request " + request);
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
            this.log.add("distanceVectorAlgorithm.establecerRutas(" + datos + " , " + vecino + ")");
            distanceVectorAlgorithm.stablishRoutes(datos, vecino);
            this.log.add("distanceVectorAlgorithm.calcular(" + datos + " , " + vecino + ")");
            distanceVectorAlgorithm.calculateBellmanFord(vecino);
            distanceVectorAlgorithm.threadResource.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
