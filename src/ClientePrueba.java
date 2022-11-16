
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
// import java.text.SimpleDateFormat;

public class ClientePrueba {
    public static void main(String args[]) {
        // SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String message = "";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Socket socket;
        String host = "127.0.0.1";
        int port = 9080;

        try {
            socket = new Socket(host, port);

            DataInputStream socket_input = new DataInputStream(socket.getInputStream());
            DataOutput socket_output = new DataOutputStream(socket.getOutputStream());

            while (!message.startsWith("EXIT")) {
                System.out.println("Made a connection with server");
                message = bufferedReader.readLine();

                socket_output.writeUTF(message);

                System.out.println("TCP: " + socket_input.readUTF() + "");

            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
