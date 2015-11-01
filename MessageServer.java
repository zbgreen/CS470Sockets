/**
 * Code from http://cs.lmu.edu/~ray/notes/javanetexamples/
 * Modified by zach on 10/27/15.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Server for sending and receiving messages between clients.
 *
 * String formats for Clients
 * Join: "name Hi"
 * Send: "receiver message"
 */
public class MessageServer {

    //Server Port
    private static final int PORT = 9000;
    //Set of all the client nodes
    private static HashSet<Node> nodes = new HashSet<>();
    //Set of all the client names. This set is for convenience.
    private static HashSet<String> names = new HashSet<>();
    //Keeps track of number of nodes
    private static int count = 1;

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The message server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Node node;
        private String name = "";
        private int num = count;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            System.out.println("Client " + num + " connecting.");

            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    String firstMsg = in.readLine();

                    if (firstMsg == null) {
                        return;
                    }
                    //Checks if "Hi" is at end of message. If true then it is the first message.
                    //Checking if it contains Hi first helps prevent IndexOutOfBounds.
                    if (firstMsg.contains("Hi")) {
                        //Checks to make sure if Hi is at the end of the message
                        if (firstMsg.substring(firstMsg.length() - 2, firstMsg.length()).equals("Hi")) {
                            name = firstMsg.substring(0, firstMsg.length() - 3);
                        } else { //Hi is not at the end of the message.
                            out.println("ERROR ");
                        }
                    } else { //Hi is not in the message which is incorrect.
                        out.println("ERROR ");
                    }
                    //Checks if the name is already taken
                    if (!names.contains(name)) {
                        //Creates new node and adds node name to names and the node to nodes.
                        node = new Node(name, out, num);
                        nodes.add(node);
                        names.add(name);
                        node.setLastMsg(firstMsg);
                        break;
                    }
                }
                System.out.println("Client " + num + " " + name + ": has connected.");
                out.println("MESSAGE Welcome " + name);
                count++;

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    //Checks null messages
                    if (input == null) {
                        return;
                    }

                    //Add input to lastMsg
                    node.setLastMsg(input);

                    //Checks for target message format
                    if (!input.substring(input.length() - 2, input.length()).equals("Hi")) {
                        System.out.println("Client " + num + " " + name + ": Incoming message");
                        int msgEnd = input.indexOf(' ');
                        String receiver = input.substring(0, msgEnd);

                        //Checks if receiver exists
                        if (names.contains(receiver)) {
                            System.out.println("Message for: " + receiver);
                            Iterator<Node> i = nodes.iterator();
                            while(i.hasNext()) {
                                Node n = i.next();
                                if (n.getName().equals(receiver)) {
                                    //Send messsage to receiving node
                                    PrintWriter send = n.getOut();
                                    send.println("MESSAGE " + node.getName() + ": " + input.substring(msgEnd + 1));
                                }
                            }
                        } else {//receiver doesn't exist
                            out.println("MESSAGE " + receiver + " is not here");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (node != null) {
                    nodes.remove(node);
                }
                if (names != null) {
                    names.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}