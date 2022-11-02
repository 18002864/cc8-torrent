import java.net.ServerSocket;
import utils.Constants;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class ForwardServer implements Runnable {
    private PrintWriter writer;
    private BufferedReader in;

    Log logForwardServer = null;
    Integer fordwardPort = 0;
    ServerSocket serverSocket;
    protected Socket socket = null;
    DistanceVectorAlgorithm distanceVectorAlgorithm;
    ForwardClient forwardClient;

    protected String vecino = "";

    protected HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    protected Integer fileLength = 0;

    public ForwardServer(Log logForwardServer, DistanceVectorAlgorithm distanceVectorAlgorithm, ForwardClient forwardClient) {
        this.logForwardServer = logForwardServer;
        this.fordwardPort = Constants.FORWARD_PORT;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.forwardClient = forwardClient;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(this.fordwardPort);
            logForwardServer.add("Forward Server en " + this.fordwardPort);
            while (true) {
                try {
                    
                    this.socket = this.serverSocket.accept();
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    String from = "", to = "", name = "", size = "", data = "", frag = "", msg = "";
                    while (true) {
                        
                        String request = in.readLine();
                        this.logForwardServer.add("Request " + request);
                        

                        String[] aRequest = request.split(":");
                        if (aRequest[0].trim().contains("From")) {
                            from = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("To")) {
                            to = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("Name")) {
                            name = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("Size")) {
                            size = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("Data")) {
                            data = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("Frag")) {
                            frag = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("Msg")) {
                            msg = aRequest[1].trim();
                        } else if (aRequest[0].trim().contains("EOF")) {
                            break;
                        }
                        /*//Aca obtenemos el mensaje enviado desde el cliente en formato JSON
                        //y se transforma a un objeto
                        MessageJson messageJson = objectMapper.readValue(clientMessage, MessageJson.class);
                        String fileName = messageJson.getFileName();
                        int fileLength = messageJson.getTotalLength();
                        String chunkObject = messageJson.getChunk();

                        //De esta forma podemos recibir diferentes tipos de mensajes con
                        //diferentes propiedades
                        if (messageJson.getType() == MessageType.EXTENDED) {
                            MessageJsonExtended messageJsonExtended = objectMapper.readValue(clientMessage,
                                    MessageJsonExtended.class);

                            System.out.printf("--> Message extended received, extended property %s %n",
                                    messageJsonExtended.getExtendedProperty());
                        }

                        String chunk = chunkObject.substring(4);
                        String position = chunkObject.substring(0, 4);
                        Integer chunkPosition = Integer.parseInt(position, 16);
                        int chunkLength = chunk.length();
                        length = length + chunkLength;

                        System.out.printf("Receiving chunk number: %d, with length: %d, chunk: %s %n",
                                chunkPosition, chunkLength, chunk);

                        //Base 64 Decode, aca se toma el chunk enviado desde el cliente
                        //y se transforma a bytes para armar el archivo
                        chunks.put(chunkPosition, Base64.getDecoder().decode(chunk));

                        if (length == fileLength) {
                            String outputFilename = Constants.SERVER_OUTPUT_PATH + fileName;
                            try (OutputStream outputStream = new FileOutputStream(outputFilename)) {
                                for (final byte[] value : chunks.values()) {
                                    outputStream.write(value);
                                }
                            }

                            System.out.println("File name: " + fileName);
                            System.out.println("File length: " + fileLength);
                            System.out.println("Total chunks: " + chunks.size());
                            System.out.println("File created successfully - " + outputFilename);
                        
                            break;
                        }
                        */
                    }           


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
