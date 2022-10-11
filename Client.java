
import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.Date;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Client {
    public static void main(String args[]) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String message = "";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Socket socket;
        String host = "127.0.0.1";
        int port = 8080;

        try {
            socket = new Socket(host, port);

            DataInputStream socket_input = new DataInputStream(socket.getInputStream());
            DataOutput socket_output = new DataOutputStream(socket.getOutputStream());

            while (!message.startsWith("EXIT")) {
                System.out.print("Ingrese cadena: ");
                message = bufferedReader.readLine();

                // System.out.println(
                // "> "
                // + socket.getLocalAddress().toString().split("/")[1]
                // + " client "
                // + "["
                // + dateFormat.format(new Date())
                // + "]");

                // System.out.println("TCP: " + message + "");

                String info = "From:" + "Client";
                info += "\n" + "To:" + "Server";
                info += "\n" + "Name:" + message;
                info += "\n" + "Size:" + message.length();
                info += "\n" + "EOF";

                socket_output.writeUTF(info);

                // System.out.println(
                // "< "
                // + socket.getInetAddress().toString().split("/")[1]
                // + " server "
                // + "["
                // + dateFormat.format(new Date()) + "]");
                // System.out.println(socket_input.readUTF());

            }
            System.out.println("[" + dateFormat.format(new Date()) + "] Server Stop");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}