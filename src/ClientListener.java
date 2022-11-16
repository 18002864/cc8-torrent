
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

                this.log.add("");
                this.log.add("---- Greetings Process Started ---- ");

                PrintWriter outSocket = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader inSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.log.add("Initialize protocol with Node " + this.vecino);

                String typeMessage = "Type:HELLO";
                String test = "From:" + distanceVectorAlgorithm.myNode;
                test += "\n" + typeMessage;
                this.log.add("Sending message " + typeMessage);
                outSocket.println(test);
                // esperar welcome
                Boolean welcome = false;
                String vecino = "";
                while (!welcome) {
                    this.log.add("Waiting message from " + this.vecino);
                    String response = inSocket.readLine();
                    // this.log.add("response --->" + response);
                    if (response.contains("From")) {
                        String[] aresponse = response.split(":");
                        vecino = aresponse[1];
                        continue;
                    } else if (response.contains("Type")) {
                        String[] aresponse = response.split(":");
                        this.log.add("Node " + this.vecino + " answer " + aresponse[1]);
                        if (aresponse[1].contains("WELCOME")) {
                            welcome = true;
                            this.log.add("Greetings completed with node " + this.vecino);
                            distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourListening(vecino, true);
                            distanceVectorAlgorithm.threadResource.release();
                        }
                    }
                }
                this.log.add("---- Greetings Process Finish ---- ");
                this.log.add("");

                this.distanceVectorToOtherNodes(outSocket); // la primera vez transmitir el distanceVectorAlgorithm de
                                                            // una!
                Long now = System.currentTimeMillis();
                while (true) {
                    if (System.currentTimeMillis() - now >= (this.wait_retransmission) * 1000) {
                        if (distanceVectorAlgorithm.neighbourConnected.get(this.vecino)) {
                            if (distanceVectorAlgorithm.neighbourListening.get(this.vecino)) {

                                this.log.add("");
                                this.log.add("---- Keep Alive Process Started ---- ");

                                log.add("Neighbour " + this.vecino + " is listening ");
                                // log.add("Transmitir a " + this.vecino);
                                if (distanceVectorAlgorithm.changesFlag
                                        && !distanceVectorAlgorithm.neighbourNotify.get(this.vecino)) {
                                    this.distanceVectorToOtherNodes(outSocket);
                                } else {
                                    log.add("Transmiting Keep Alive to " + this.vecino);
                                    test = "From:" + distanceVectorAlgorithm.myNode;
                                    test += "\n" + "Type:KeepAlive";
                                    outSocket.println(test);
                                    this.log.add("---- Keep Alive Process Finish ---- ");
                                    this.log.add("");
                                }
                                now = System.currentTimeMillis(); // reset timer
                            } else {
                                this.log.add("");
                                this.log.add("---- Keep Alive Process Error ---- ");
                                log.warning("Neighbour " + this.vecino + " is not listening ");
                                this.distanceVectorAlgorithm.threadResource.acquire();
                                this.distanceVectorAlgorithm.updateNeighbourListening(this.vecino, false);
                                this.distanceVectorAlgorithm.threadResource.release();
                                this.log.add("---- Keep Alive Process Error ---- ");
                                this.log.add("");
                                break; // terminar este thread
                            }
                        } else {
                            this.log.add("");
                            this.log.add("---- Keep Alive Process Error ---- ");
                            log.warning("Neighbour " + this.vecino + " is not connected, rejecting client");
                            this.distanceVectorAlgorithm.threadResource.acquire();
                            this.distanceVectorAlgorithm.updateNeighbourListening(this.vecino, false);
                            this.distanceVectorAlgorithm.threadResource.release();
                            this.log.add("---- Keep Alive Process Error ---- ");
                            this.log.add("");
                            break; // terminar este thread
                        }
                    }
                }
            } else {
                this.log.add("");
                this.log.add("---- Keep Alive Process Error ---- ");
                this.log.warning("Neighbour cannot be reached " + this.vecino + " marked as notified");
                distanceVectorAlgorithm.threadResource.acquire();
                distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
                distanceVectorAlgorithm.threadResource.release();
                this.log.add("---- Keep Alive Process Error ---- ");
                this.log.add("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void distanceVectorToOtherNodes(PrintWriter outSocket) {
        try {
            this.log.add("");
            this.log.add("---- Distance Vector Process Started ---- ");

            this.log.add("Transmit Distance Vectors to Node " + this.vecino);

            String test = "From:" + distanceVectorAlgorithm.myNode;
            test += "\n" + "Type:DV";
            test += "\n" + "Len:"
                    + (distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).size() - 1);

            for (String vecinoi : distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode)
                    .keySet()) {

                if (!vecinoi.equals(distanceVectorAlgorithm.myNode)) {
                    test += "\n" + vecinoi + ":" + distanceVectorAlgorithm.distanceVectorHashMap
                            .get(distanceVectorAlgorithm.myNode).get(vecinoi).get("cost");
                }
            }

            outSocket.println(test);
            this.log.add("DV " + test.replaceAll("\n", " "));
            this.log.add("Distance Vector has been transmitted "
                    + this.vecino
                    + " marking as notified");
            distanceVectorAlgorithm.threadResource.acquire();
            distanceVectorAlgorithm.updateNeighbourNotify(this.vecino, true);
            distanceVectorAlgorithm.threadResource.release();
            this.log.add("---- Distance Vector Process Finish ---- ");
            this.log.add("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}