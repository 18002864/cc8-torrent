import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForwardClientThread implements Runnable {
    ForwardClient forwardClient;
    Log logAplicacion;

    public ForwardClientThread(ForwardClient forwardClient, Log logAplicacion) {
        this.forwardClient = forwardClient;
        this.logAplicacion = logAplicacion;
    }

    public void run() {
        try {
            Long now = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - now >= (3 * 1000)) { // cada 3 segundos
                    // Verificar si el archivo ya se recibio completo
                    if (forwardClient.waitingFlag) {
                        this.forwardClient.sema.acquire();
                        logAplicacion.add((100*forwardClient.fileLength/forwardClient.fileSize) + "%, se han recibido " + forwardClient.fileLength + " de " + forwardClient.fileSize + " bytes");
                        this.forwardClient.sema.release();
                        now = System.currentTimeMillis();
                        continue;
                    } else {
                        // Verificar si se presento un error en la transmisión
                        if (this.forwardClient.errorFlag) {
                            logAplicacion.add("Hubo un error! con el archivo " + this.forwardClient.fileName + " error: " + this.forwardClient.message);
                            this.forwardClient.consoleFlag = false;
                        } else {
                            
                            // Construcción de archivo
                            this.logAplicacion.add("Voy a intentar formar el archivo " + this.forwardClient.fileName);
                            String fileString = "";
                            this.forwardClient.sema.acquire();
                            if(this.forwardClient.chunks.size() == 0){
                                this.logAplicacion.add("Hubo un error! no hay chunks :( " + this.forwardClient.fileName);
                                this.forwardClient.consoleFlag = false;
                                this.forwardClient.sema.release();                                
                                break;
                            }
                            this.logAplicacion.add("Se recibieron " + this.forwardClient.chunks.size() + " chunks");
                            for (Integer i = 1; i <= this.forwardClient.chunks.size(); i++) {
                                fileString += this.forwardClient.chunks.get(i);
                            }
                            this.forwardClient.sema.release();
                            byte[] fileByte = hexStringToByteArray(fileString);
                            // File file = new File("Archivos/" + nombreArchivo);

                            Path path = Paths.get(this.forwardClient.fileName);
                            String file = System.currentTimeMillis() + path.getFileName().toString();
                            OutputStream stream = new FileOutputStream("./files/" + file);
                            stream.write(fileByte);
                            stream.close();
                            System.out.println("Todo bien :D : se guardo el archivo " + this.forwardClient.fileName);
                            if(forwardClient.consoleFlag){
                                forwardClient.consoleFlag = false;
                            }
                            
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
