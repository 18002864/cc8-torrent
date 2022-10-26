
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientListener implements Runnable {

    protected Socket socket = null;
    protected Thread runningThread = null;
    protected String ip = "";
    protected Integer port = 0;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm = null;
    protected Integer wait_conection = 1;
    protected Log log = null;
    protected String vecino = "";
    protected Integer wait_retransmission = 1;

    public ClientListener(
            String ip,
            Integer port,
            DistanceVectorAlgorithm distanceVectorAlgorithm,
            Integer wait_conection,
            Log log,
            String vecino,
            Integer wait_retransmission) {
        this.ip = ip;
        this.port = port;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.wait_conection = wait_conection;
        this.log = log;
        this.vecino = vecino;
        this.wait_retransmission = wait_retransmission;
    }

    public void conectar() {
        Long inicio = System.currentTimeMillis();
        Long fin = inicio + this.wait_conection * 1000;
        while (System.currentTimeMillis() < fin) {
            try {
                this.socket = new Socket(this.ip, this.port);
                if (this.socket != null) {
                    break;
                }
            } catch (Exception e) {
                System.out.print(e);
                if (distanceVectorAlgorithm.neighbourListening.get(this.vecino)) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        conectar();
        try {
            if (this.socket != null) {
                this.log.add("Se conecto con " + this.vecino);
                PrintWriter outSocket = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.log.add("Iniciar protocolo con " + this.vecino);
                /*
                 * outSocket.println("From:" + this.distanceVectorAlgorithm.minodo);
                 * outSocket.println("Type:HELLO");
                 */
                String test = "From:" + distanceVectorAlgorithm.myNode;
                test += "\n" + "Type:HELLO";
                outSocket.println(test);
                // esperar welcome
                Boolean welcome = false;
                String vecino = "";
                while (!welcome) {
                    this.log.add("Esperando mensaje de " + this.vecino);
                    String response = inSocket.readLine();
                    this.log.add("response --->" + response);
                    this.log.add(this.vecino + " contesto " + response);
                    if (response.contains("From")) {
                        String[] aresponse = response.split(":");
                        vecino = aresponse[1];
                        continue;
                    } else if (response.contains("Type")) {
                        String[] aresponse = response.split(":");
                        if (aresponse[1].contains("WELCOME")) {
                            welcome = true;
                            this.log.add("Saludo completado con " + this.vecino);
                            distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourListening(vecino, true);
                            distanceVectorAlgorithm.threadResource.release();
                        }
                    }
                }

                this.transmitirdv(outSocket); // la primera vez transmitir el distanceVectorAlgorithm de una!
                Long now = System.currentTimeMillis();
                while (true) {
                    if (System.currentTimeMillis() - now >= (this.wait_retransmission) * 1000) {
                        if (distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {
                            if (distanceVectorAlgorithm.neighbourListening.get(this.vecino)) {
                                log.add("El vecino " + this.vecino + " esta escuchando ");
                                log.add("Transmitir a " + this.vecino);
                                if (distanceVectorAlgorithm.changesFlag
                                        && !distanceVectorAlgorithm.neighbourNotify.get(this.vecino)) {
                                    this.transmitirdv(outSocket);
                                } else {
                                    log.add("Transmitir KA a " + this.vecino);
                                    /*
                                     * outSocket.println("From:" + distanceVectorAlgorithm.minodo);
                                     * outSocket.println("Type:KeepAlive");
                                     */
                                    test = "From:" + distanceVectorAlgorithm.myNode;
                                    test += "\n" + "Type:KeepAlive";
                                    outSocket.println(test);
                                }
                                now = System.currentTimeMillis(); // reset timer
                            } else {
                                log.warning("El vecino " + this.vecino + " no esta escuchando ");
                                this.distanceVectorAlgorithm.threadResource.acquire();
                                this.distanceVectorAlgorithm.updateNeighbourListening(this.vecino, false);
                                this.distanceVectorAlgorithm.threadResource.release();
                                break; // terminar este thread
                            }
                        } else {
                            log.warning("El vecino " + this.vecino + " no esta conectado, se desconecta cliente");
                            this.distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourListening(this.vecino, false);
                            this.distanceVectorAlgorithm.threadResource.release();
                            break; // terminar este thread
                        }
                    }
                }
            } else {
                this.log.warning("No se conecto con " + this.vecino + " marcar como notificado");
                distanceVectorAlgorithm.threadResource.acquire();
                distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                distanceVectorAlgorithm.threadResource.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void transmitirdv(PrintWriter outSocket) {
        try {
            log.add("Transmitir distanceVectorAlgorithm a " + this.vecino);
            String test = "From:" + distanceVectorAlgorithm.myNode;
            test += "\n" + "Type:DV";
            test += "\n" + "Len:"
                    + (distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).size() - 1);
            // this.log.add("que sale? vecinoi ---->" + vecinoi);
            // test += "\n" + "que sale? distanceVectorAlgorithm.myNode ---->" +
            // distanceVectorAlgorithm.myNode;
            // this.log.add("que sale?" + !vecinoi.equals(distanceVectorAlgorithm.myNode));
            for (String vecinoi : distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode)
                    .keySet()) {
                // this.log.add("que sale? vecinoi ---->" + vecinoi);
                // this.log.add("que sale? distanceVectorAlgorithm.myNode ---->" +
                // distanceVectorAlgorithm.myNode);
                // this.log.add("que sale?" + !vecinoi.equals(distanceVectorAlgorithm.myNode));

                if (!vecinoi.equals(distanceVectorAlgorithm.myNode)) {
                    test += "\n" + vecinoi + ":" + distanceVectorAlgorithm.distanceVectorHashMap
                            .get(distanceVectorAlgorithm.myNode).get(vecinoi).get("cost");
                }
            }

            outSocket.println(test);
            this.log.add("DV " + test.replaceAll("\n", " "));
            this.log.add("Se notifico a " + this.vecino + " marcar como notificado");
            distanceVectorAlgorithm.threadResource.acquire();
            distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
            distanceVectorAlgorithm.threadResource.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}