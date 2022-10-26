
import java.io.BufferedReader;
import java.io.InputStreamReader;

import app.App;

public class Torrent {
    public static void main(String[] arg) throws Exception {

        // Distance Vector Algorithm
        Log logsDistanceVector = new Log("DV");
        DistanceVectorAlgorithm distanceVectorAlgorithm = new DistanceVectorAlgorithm(logsDistanceVector);
        distanceVectorAlgorithm.init();

        Integer wait_conection = 5;
        Integer wait_retransmission = 20;
        Log logClient = new Log("FC");
        Client client = new Client(wait_conection, wait_retransmission, logClient, distanceVectorAlgorithm);
        new Thread(client).start(); // este thread va a mantener los clientes de Distance Vector

        Integer wait_reconnection = 90;
        Integer port = 9080;
        Log logServer = new Log("FS");
        Server rs = new Server(logServer, port, wait_reconnection, distanceVectorAlgorithm);
        new Thread(rs).start(); // este thread va a mantener el servidor de Distance Vector

        BufferedReader inConsole = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";
        while (!userInput.contains("exit")) {
            System.out.println("Ingrese comando: ");
            userInput = inConsole.readLine();
            String[] commands = userInput.split(" ");

            // debe cumplir con esta estructura
            // hacer una lista de comandos

            // [0] = destiny
            // [1] = name
            // [2] = size
            System.out.println(commands[0]);
            // if (commands.length == 3) {

            // } else if (commands.length == 1) {
            // if (commands[0].contains("mostrar")) {

            // } else if (commands[0].contains("printdv")) {

            // } else {
            // System.out.println("Comando no valido");
            // continue;
            // }
            // } else {
            // System.out.println("Comando no valido");
            // continue;
            // }
        }

    }
}