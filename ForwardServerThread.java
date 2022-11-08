import java.net.Socket;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import utils.Constants;

public class ForwardServerThread implements Runnable {

    protected Socket socket = null;
    protected DistanceVectorAlgorithm distanceVectorAlgorithm = null;
    protected ForwardClient forwardClient;
    protected Log logForwardServer;
    protected String neighbour = "";
    protected Integer fordwardPort = Constants.FORWARD_PORT;

    private PrintWriter writer;
    private BufferedReader in;

    protected HashMap<Integer, String> chunks = new HashMap<Integer, String>();
    protected Integer fileLength = 0;
    protected Integer nChunks = 0;

    private String from = "", to = "", name = "", size = "", data = "", frag = "", msg = "";

    public ForwardServerThread(Socket socket, DistanceVectorAlgorithm distanceVectorAlgorithm, Log logForwardServer, ForwardClient forwardClient) {
        this.socket = socket;
        this.distanceVectorAlgorithm = distanceVectorAlgorithm;
        this.logForwardServer = logForwardServer;
        this.forwardClient = forwardClient;
    }

    public void run() {
        try {
            
            this.logForwardServer.add("---- Start Receive Request ----");
            while (true) {
                // Metodo para recibir el mensaje completo
                saveRequest();
                this.logForwardServer.add("---- End Receive Request ----\n");
                // Verificar si el request es para mi o si tiene que ser retransmitido
                if (this.distanceVectorAlgorithm.myNode.contains(to)) {
                    //this.logForwardServer.add("---- La solicitud es para mi ----\n");
                    // Verificar si se esta solicitando un archivo
                    if (!name.equals("") && data.equals("")) { 
                        this.logForwardServer.add("---- Start Response File ----");
                        this.responseFile();
                        this.logForwardServer.add("---- End Response File ----");
                        break;
                    } 
                    // Verificar si es un chunck para recibir
                    else if (!name.equals("") && !data.equals("")) { 
                        if (this.forwardClient.fileLength == 0) {
                            this.logForwardServer.add("---- Start Receive Chunk ----");
                        }
                        if (this.forwardClient.waitingFlag) {
                            if (this.forwardClient.fileName.contains(name)) {
                                this.forwardClient.sema.acquire();
                                nChunks++;
                                this.logForwardServer.add("Chunk number " + nChunks);
                                this.forwardClient.chunks.put(Integer.parseInt(frag), data);
                                this.forwardClient.fileLength += data.length() / 2;
                                if (this.forwardClient.fileLength >= Integer.parseInt(size)) {
                                    this.forwardClient.waitingFlag = false;
                                    this.logForwardServer.add("---- End Receive Chunk ----");
                                    this.forwardClient.sema.release();
                                    break;
                                } else {
                                    this.forwardClient.sema.release();
                                }
                            }
                        }
                        continue;
                    } 
                    // El mensaje es un error porque no hay nombre de archivo
                    else { 
                        this.logForwardServer.add("---- Start Receive Error ----");
                        this.saveError();
                        this.logForwardServer.add("---- End Receive Error ----");
                        break;
                    }

                } else {

                    //this.logForwardServer.add("---- La solicitud no es para mi ----\n");
                    // Verificar si el request a ser retransmitido es una solicitud o un paquete de un archivo 
                    if (!name.equals("") && data.equals("")) {
                        this.logForwardServer.add("---- Start Resend Request ----");
                        this.resendRequest();
                        this.logForwardServer.add("---- End Resend Request ----");
                        break;
                    } else if (!name.equals("") && !data.equals("")) {
                        if (fileLength == 0) {
                            this.logForwardServer.add("---- Start Resend File ----");
                        } 
                        chunks.put(Integer.parseInt(frag), data);
                        fileLength += data.length() / 2;
                        if (fileLength == Integer.parseInt(size)) {
                            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(to).get("hop");
                            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
                            Socket socket = new Socket(ip, this.fordwardPort);
                            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                            
                            int head = 1;
                            int tail = this.chunks.size();
                            Boolean init = true;
                            for (int i = 1; i <= this.chunks.size(); i++) {
                                if (init) {
                                    String info = "From:" + from;
                                    info += "\n" + "To:" + to;
                                    info += "\n" + "Name:" + name;
                                    info += "\n" + "Data:" + chunks.get(head);
                                    info += "\n" + "Frag:" + head;
                                    info += "\n" + "Size:" + size;
                                    info += "\n" + "EOF";
                                    head++;
                                    writer.println(info);
                                    init = false;
                                } else {
                                    String info = "From:" + from;
                                    info += "\n" + "To:" + to;
                                    info += "\n" + "Name:" + name;
                                    info += "\n" + "Data:" + chunks.get(tail);
                                    info += "\n" + "Frag:" + tail;
                                    info += "\n" + "Size:" + size;
                                    info += "\n" + "EOF";
                                    tail--;
                                    writer.println(info);
                                    init = true;
                                }
                            }
                            writer.close();
                            socket.close();
                            this.logForwardServer.add("---- End Resend File ----");
                            break;
                        }
                        continue;
                    
                    } else {
                        this.logForwardServer.add("---- Start Resend Error ----");
                        this.resendError();
                        this.logForwardServer.add("---- End Resend Error ----");
                        break;
                    }
                }
            }
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveRequest() {
        try {
            while (true) {
                        
                String message = in.readLine();
                this.logForwardServer.add(message);
                
                if (message == null){
                    break;
                }
                if (message == ""){
                    continue;
                }

                String[] splitMessage = message.split(":");

                
                switch (splitMessage[0].trim()){
                        case "From":{
                            from = splitMessage[1].trim();
                            continue;
                        }
                        case "To":{
                            to = splitMessage[1].trim();
                            continue;
                        }
                        case "Name":{
                            name = splitMessage[1].trim();
                            continue;
                        }
                        case "Size":{
                            size = splitMessage[1].trim();
                            continue;
                        }
                        case "Data":{
                            data = splitMessage[1].trim();
                            continue;
                        }
                        case "Frag":{
                            frag = splitMessage[1].trim();
                            continue;
                        }
                        case "Msg":{
                            msg = splitMessage[1].trim();
                            continue;
                        }
                        case "EOF":{
                            break;
                        }
                        default: {
                            this.logForwardServer.add("Error in Request\n");
                            break;
                        }
                }
                break;
            }
            this.logForwardServer.add("from: " + from + ", to: " + to + ", name: " + name + ", size: " + size + ", data: " + data + ", frag: " + frag + ", msg: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void responseFile() {
        try {
            File file = new File("files/" + name);
            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(from).get("hop"); // ruta segun Distance Vector
            //System.out.println(through);
            //through = "G";
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
            //System.out.println(ip);

            // Verificamos que el archivo exista
            if (file.exists()) {
                
                
                this.logForwardServer.add("File exists");
                fragmentFile(file);
                this.logForwardServer.add("File fragmented");
                sendFile(ip);
                this.logForwardServer.add("File sended");

            } else {
                this.logForwardServer.add("File doesn't exists");
                Socket socket = new Socket(ip, this.fordwardPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                String info = "From:" + distanceVectorAlgorithm.myNode;
                info += "\n" + "To:" + from;
                info += "\n" + "Msg:" + "File doesn't exists";
                info += "\n" + "EOF";
                writer.println(info);
                writer.close();
                socket.close();
                this.logForwardServer.add("Error message send");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fragmentFile(File file) {
        try {
            this.chunks = new HashMap<Integer, String>();
            this.fileLength = 0;
            /* Fragmentar archivo */
            InputStream stream = new FileInputStream(file);
            byte[] buffer = new byte[730];
            int len;
            int pos = 1;
            while ((len = stream.read(buffer)) > 0) {
                /* Dejar solo los bytes del archivo */
                byte[] buffer2 = new byte[len];
                for (int j = 0; j < len; j++) {
                    buffer2[j] = buffer[j];
                }

                String response = bytesToHex(buffer2);
                this.fileLength += response.length();
                this.chunks.put(pos, response);
                pos++;
            }
            stream.close();
            /* Fin Fragmentar archivo */
        } catch (Exception e) {
            e.printStackTrace();
            this.chunks = new HashMap<Integer, String>();
        }
    }

    public void sendFile(String ip) {
        try {
            int head = 1;
            int tail = this.chunks.size();
            Boolean init = true;
            Socket socket = new Socket(ip, this.fordwardPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            for (int i = 1; i <= this.chunks.size(); i++) {
                if (init) {
                    String info = "From:" + distanceVectorAlgorithm.myNode;
                    info += "\n" + "To:" + from;
                    info += "\n" + "Name:" + name;
                    info += "\n" + "Data:" + chunks.get(head);
                    info += "\n" + "Frag:" + head;
                    info += "\n" + "Size:" + fileLength / 2;
                    info += "\n" + "EOF";
                    head++;
                    writer.println(info);
                    init = false;
                } else {
                    String info = "From:" + distanceVectorAlgorithm.myNode;
                    info += "\n" + "To:" + from;
                    info += "\n" + "Name:" + name;
                    info += "\n" + "Data:" + chunks.get(tail);
                    info += "\n" + "Frag:" + tail;
                    info += "\n" + "Size:" + fileLength / 2;
                    info += "\n" + "EOF";
                    tail--;
                    writer.println(info);
                    init = true;
                }
            }
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public void saveError() {
        try {
            this.forwardClient.sema.acquire();
            this.forwardClient.errorFlag = true;
            this.forwardClient.message = msg;
            this.forwardClient.waitingFlag = false;
            this.forwardClient.sema.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resendRequest() {
        try {
            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(to).get("hop");
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
            Socket socket = new Socket(ip, this.fordwardPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            String info = "From:" + from;
            info += "\n" + "To:" + to;
            info += "\n" + "Name:" + name;
            info += "\n" + "Size:" + size;
            info += "\n" + "EOF";
            writer.println(info);
            writer.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resendError() {
        try {
            String through = this.distanceVectorAlgorithm.distanceVectorHashMap.get(distanceVectorAlgorithm.myNode).get(to).get("hop");
            String ip = this.distanceVectorAlgorithm.hostNeighbours.get(through).get("ip");
            Socket socket = new Socket(ip, this.fordwardPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            String info = "From:" + from;
            info += "\n" + "To:" + to;
            info += "\n" + "Msg:" + msg;
            info += "\n" + "EOF";
            writer.println(info);
            writer.close();
            socket.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}