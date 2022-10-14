
import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.*;

public class Log {

    private Logger logger = null;
    private FileHandler fileHandler = null;

    // type
    // distance vector DV
    // forward client FC
    // forward server FS
    // routing client RC
    // routing server RS

    public Log(String type) {

        String route = getRoute(type);
        String name = getName(type);
        System.out.println(route);
        System.out.println(name);
        this.logger = Logger.getLogger(name);
        File file = new File(route);
        if (file.exists()) {
            file.delete();
        }
        try {
            fileHandler = new FileHandler(route, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomFormatter formatter = new CustomFormatter();
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
    }

    public void add(String mensaje) {
        logger.info(mensaje);
    }

    public void warning(String mensaje) {
        logger.warning(mensaje);
    }

    public void severe(String mensaje) {
        logger.severe(mensaje);
    }

    // distance vector DV
    // forward client FC
    // forward server FS
    // routing client RC
    // routing server RS

    public String getRoute(String type) {
        String value = "";
        if (type.equals("DV")) {
            value = "Logs/DistanceVector.log";
        }
        if (type.equals("FC")) {
            value = "Logs/ForwardClient.log";
        }
        if (type.equals("FS")) {
            value = "Logs/ForwardServer.log";
        }
        if (type.equals("RC")) {
            value = "Logs/RoutingClient.log";
        }
        if (type.equals("RS")) {
            value = "Logs/RoutingServer.log";
        }
        return value;
    }

    public String getName(String type) {
        String value = "";
        if (type.equals("DV")) {
            value = "DistanceVector";
        }
        if (type.equals("FC")) {
            value = "ForwardClient";
        }
        if (type.equals("FS")) {
            value = "ForwardServer";
        }
        if (type.equals("RC")) {
            value = "RoutingClient";
        }
        if (type.equals("RS")) {
            value = "RoutingServer";
        }
        return value;
    }

}

class CustomFormatter extends Formatter {
    // Create a DateFormat to format the logger timestamp.
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);

        if (record.getLevel().intValue() == 800) {
            builder.append("   ").append(record.getLevel()).append(" > ");
        } else if (record.getLevel().intValue() == 1000) {
            builder.append(" ").append(record.getLevel()).append(" > ");
        } else {
            builder.append("").append(record.getLevel()).append(" > ");
        }
        builder.append("[" + df.format(new Date(record.getMillis())) + "]").append(" : ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
