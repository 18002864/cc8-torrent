

import app.App;

public class Torrent {
    public static void main(String[] arg) throws Exception {

        // Distance Vector Algorithm
        Log logsDistanceVector = new Log("DV");
        DistanceVectorAlgorithm distanceVectorAlgorithm = new DistanceVectorAlgorithm(logsDistanceVector);
        distanceVectorAlgorithm.init();

        // BufferedReader inConsole = new BufferedReader(new
        // InputStreamReader(System.in));
        // String userInput = "";
        // while (!userInput.contains("exit")) {
        // System.out.println("Ingrese comando: ");
        // userInput = inConsole.readLine();
        // String[] commands = userInput.split(" ");

        // // debe cumplir con esta estructura
        // // hacer una lista de comandos

        // // [0] = destiny
        // // [1] = name
        // // [2] = size
        // System.out.println(commands[0]);
        // // if (commands.length == 3) {

        // // } else if (commands.length == 1) {
        // // if (commands[0].contains("mostrar")) {

        // // } else if (commands[0].contains("printdv")) {

        // // } else {
        // // System.out.println("Comando no valido");
        // // continue;
        // // }
        // // } else {
        // // System.out.println("Comando no valido");
        // // continue;
        // // }
        // }

    }
}