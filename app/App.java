package app;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import utils.Constants;
import forwarding.Forwarding;

public class App {
    
    protected Integer fordward_port;

    public App() {
        this.fordward_port = Constants.FORWARD_PORT;
       
    }

    public void start() {
        try {
            BufferedReader inConsole = new BufferedReader(new InputStreamReader(System.in));
            String userInput = "";
        

            while (!userInput.contains("exit")) {
                System.out.print("Ingrese comando: ");
                userInput = inConsole.readLine();
                String[] commands = userInput.split(" ");
                
                if (commands.length == 3) {
                    
                    solicitar(commands[0], commands[1], commands[2]);
                    continue;
                    
                } else {
                    System.out.println("Comando no valido");
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void solicitar(String destino, String name, String size) {
        try {
            
            // Enviar la solicitud atraves del Forwarding
            Forwarding forwarding = new Forwarding();
            forwarding.solicitar(destino, name, size);
        
            // Esperar archivo
            System.out.println("Se espera el archivo por parte del forwarding y al finalizar se espera un nuevo comando \n");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
