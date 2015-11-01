import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by zach on 10/30/15.
 * Node class to store information from client.
 */
public class Node {

    private static final int PORT = 9000;
    private static final String macAddr = "b8:70:f4:21:63:44";

    private String name;
    private String ipAddr;
    private String socketAddr;
    private String joinTime;
    private String lastMsg;
    private PrintWriter out;
    private int clientNum;

    public Node (String name, PrintWriter out, int clientNum) {
        this.name = name;
        ipAddr = findAddr();
        socketAddr = ipAddr + " " + PORT;
        joinTime = new Date().toString();
        lastMsg = "";
        this.out = out;
        this.clientNum = clientNum;
    }

    /*
    Finds IP address of the local machine.
     */
    private String findAddr() {
        String ip = "";
        try {
            InetAddress inet = InetAddress.getLocalHost();
            ip = inet.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("UnknownHostException");
        }

        return ip;
    }

    //Setter
    public void setLastMsg (String msg) {
        this.lastMsg = msg;
    }

    /*
    Getters
     */
    public String getName() { return name; }

    public String getIpAddr() { return ipAddr; }

    public String getMacAddr() { return macAddr; }

    public String getSocketAddr() {
        return socketAddr;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public String getLastMsg() { return lastMsg; }

    public PrintWriter getOut() { return out; }

    public int getClientNum() { return clientNum; }

    public void print() {
        System.out.println("name: " + this.getName() +
                "\nIP Address: " + this.getIpAddr() +
                "\nMAC Address: " + this.getMacAddr() +
                "\nSocketAddress: " + this.getSocketAddr() +
                "\nJoin Time: " + this.getJoinTime() +
                "\nLast Message: " + this.getLastMsg() +
                "\nClient Number: " + this.getClientNum());
    }
}
