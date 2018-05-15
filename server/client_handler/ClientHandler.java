package client_handler;
import trader.*;
import java.net.*;
import java.io.*;

public class ClientHandler {
    private ITrader trader;
    private ServerSocket serverSocket;
    private int port;
    public ClientHandler(int port, ITrader trader) throws IOException{
        System.out.println("[ \u001B[36minternal\u001B[0m ] Starting ClientHandler...");
        this.trader = trader;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        System.out.printf("[ \u001B[36minternal\u001B[0m ] Finished setting up ClientHandler on port %d\n", port);
    }
    public void run() {
        System.out.println("[ \u001B[36minternal\u001B[0m ] Running ClientHandler");
        this.acceptConnections();
    }
    public void shutdown() throws IOException{
        System.out.println("[ \u001B[36minternal\u001B[0m ] Shutting down ClientHandler...");
        this.serverSocket.close();
        System.out.println("[ \u001B[36minternal\u001B[0m ] Finished shutting down ClientHandler");

    }
    private void acceptConnections(){
        try { 
            while (true) {
                new ClientThread(this.serverSocket.accept(), this.trader).start();
            }
        } catch (IOException e) {
            System.err.println("[ \u001B[36minternal\u001B[0m ] Could not listen on port " + this.port);
            System.exit(-1);
        }
    }
}