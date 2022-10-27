
public class Client implements Runnable {

    Integer conection = 5;
    Integer retransmission = 30;
    Log log = null;
    DistanceVectorAlgorithm distanceVectorAlgorithm = null;

    public Client(
            Integer wait_conection,
            Integer wait_retransmission,
            Log log,
            DistanceVectorAlgorithm distanceVectorAlgorithm) {
        this.conection = wait_conection;
        this.retransmission = wait_retransmission;
        this.log = log;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
    }

    public void run() {

        for (var vecino : distanceVectorAlgorithm.hostNeighbours.keySet()) {
            if (!distanceVectorAlgorithm.neighbourListening.get(vecino)) {
                String ip = this.distanceVectorAlgorithm.hostNeighbours.get(vecino).get("ip");
                Integer port = Integer.parseInt(this.distanceVectorAlgorithm.hostNeighbours.get(vecino).get("port"));
                this.log.add("Conectar! con " + vecino + ": ip " + ip + " puerto " + port);
                ClientListener client = new ClientListener(ip, port, this.distanceVectorAlgorithm, this.conection,
                        this.log, vecino,
                        this.retransmission);
                new Thread(client).start();
            }
        }

        Long now = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - now >= (10 * 1000)) {
                for (var vecino : distanceVectorAlgorithm.hostNeighbours.keySet()) {
                    if (distanceVectorAlgorithm.neighbourConnected.get(vecino)
                            && !distanceVectorAlgorithm.neighbourListening.get(vecino)) {
                        String ip = this.distanceVectorAlgorithm.hostNeighbours.get(vecino).get("ip");
                        Integer port = Integer
                                .parseInt(this.distanceVectorAlgorithm.hostNeighbours.get(vecino).get("port"));
                        this.log.add("Conectar con " + vecino + ": ip " + ip + " puerto " + port);
                        ClientListener client = new ClientListener(ip, port, this.distanceVectorAlgorithm,
                                this.conection,
                                this.log,
                                vecino, this.retransmission);
                        new Thread(client).start();
                    }
                }
                now = System.currentTimeMillis();
            }
        }
    }

}