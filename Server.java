import java.net.*;

public class Server implements Runnable {
    Log log = null;
    Integer port = 0;
    Integer wait_reconnection = 1;
    ServerSocket socketServer;
    DistanceVectorAlgorithm distanceVectorAlgorithm;
    ClientListener clientListener;

    public Server(
            Log log,
            Integer port,
            Integer wait_reconnection,
            DistanceVectorAlgorithm distanceVectorAlgorithm) {
        this.log = log;
        this.port = port;
        this.wait_reconnection = wait_reconnection;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
    }

    public void run() {
        try {
            log.add("Routing Server en " + this.port);
            socketServer = new ServerSocket(this.port);
            while (true) {
                try {
                    ServerListener server = new ServerListener(
                            this.socketServer.accept(),
                            this.distanceVectorAlgorithm,
                            this.wait_reconnection,
                            this.log);
                    new Thread(server).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
