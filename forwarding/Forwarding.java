package forwarding;

import utils.Constants;

public class Forwarding {
    
    protected Integer fordward_port;

    public Forwarding() {
        this.fordward_port = Constants.FORWARD_PORT;
    }

    public void solicitar(String destino, String name, String size) {
        try {
            
           System.out.println("Se obtiene IP del vecino por el cual DV calculo la mejor ruta");
            

            //Socket socketClient = new Socket(ip, this.port);
            //PrintWriter outSocket = new PrintWriter(socketClient.getOutputStream(), true);
            
            System.out.println("Se envia el siguiente mensaje: \n");
            
            String info = "From:" + Constants.MY_NODE;
            info += "\n" + "To:" + destino;
            info += "\n" + "Name:" + name;
            info += "\n" + "Size:" + size;
            info += "\n" + "EOF";
            //outSocket.println(info);
            //outSocket.close();
            //socketClient.close();

            System.out.println(info);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
