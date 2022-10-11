import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    public static void main(String args[]) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String message = "";
        ServerSocket serverSocket;
        int port = 8080;

        try {
            serverSocket = new ServerSocket(port);
            Socket socket_listener = serverSocket.accept();

            DataInputStream socket_input = new DataInputStream(socket_listener.getInputStream());
            DataOutput socket_output = new DataOutputStream(socket_listener.getOutputStream());

            while (!message.startsWith("EXIT")) {
                message = socket_input.readUTF();

                System.out.println("[" + dateFormat.format(new Date()) + "]");
                System.out.println(message);
                socket_output.writeUTF(message);

            }
            System.out.println("[" + dateFormat.format(new Date()) + "] Server Stop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
