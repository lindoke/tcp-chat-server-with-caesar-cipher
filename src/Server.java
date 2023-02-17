import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(8888);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }
    public void broadcast(String message){
        for (ConnectionHandler ch:connections){
            if(ch!=null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        done = true;
        pool.shutdown();
        try {
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler connectionHandler : connections) {
                connectionHandler.shutdown();
            }
        } catch (IOException e){
            //ignore
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private final CaesarCipher caesarCipher = new CaesarCipher();

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname with /nick command: ");
                nickname = in.readLine();
                while (!nickname.startsWith("/nick")){
                    out.println("No nickname provided.");
                    out.println("Please enter a nickname with /nick command: ");
                    nickname = in.readLine();
                }
                if (nickname.startsWith("/nick")){
                    String[] messageSplit = nickname.split(" ", 2);
                    if (messageSplit.length==2){
                        nickname = messageSplit[1];
                    }
                }
                broadcast(nickname + " joined the chat.");
                String message;
                while ((message=in.readLine())!=null){
                    if (message.startsWith("/quit")){
                        broadcast(nickname + " left the chat.");
                        shutdown();
                    } else {
                        try {
                            message = String.valueOf(caesarCipher.decode(message));
                            broadcast(nickname + ": " + message);
                        } catch (NullPointerException nlp){

                            //offset is unknown, so decode is possible only for words from file
                            broadcast("Decode wasn`t done for this message");
                            broadcast(nickname + ": " + message);
                        }
                    }
                }
            } catch (IOException e){
                shutdown();
            }
        }
        public void sendMessage(String message){
            out.println(message);
        }


        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e){
                //ignore
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run();
    }
}
