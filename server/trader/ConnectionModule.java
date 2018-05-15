package trader;
import java.io.*;
import java.net.*;
import algorithms.*;

public class ConnectionModule {
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    String address = null;
    int port = 0;
    public ConnectionModule(Socket socket) throws IOException{
        this.socket = socket;
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
    public ConnectionModule(String address, int port) throws IOException{
        this.socket = new Socket(address, port);
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.address = address;
        this.port = port;
    }
    public ConnectionModule(String address_and_port) throws IOException{
        if(address_and_port == null || address_and_port.split(":").length <2) throw new IOException();
        String address = address_and_port.split(":")[0];
        int port = Integer.parseInt(address_and_port.split(":")[1]);
        this.socket = new Socket(address, port);
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.address = address;
        this.port = port;
    }
    public void send(String message) throws IOException{
        writer.println(message);
        writer.flush();
        if(Experiment.DEBUG_CONNECTIONS) System.out.println("[DEBUG] Sent \"" + message + "\" to " + address + ":" + port);

    }
    public void sendRedirect() throws IOException{
        this.send("R" + this.address + "~" + this.port);
    }
    public String receive() throws IOException{
        String message = reader.readLine();
        if(Experiment.DEBUG_CONNECTIONS) System.out.println("[DEBUG] Received \"" + message + "\" from " + address + ":" + port);
        return message;
    }
    public String receive(int time) throws IOException{
        this.socket.setSoTimeout(time);
        String message = reader.readLine();
        if(Experiment.DEBUG_CONNECTIONS) System.out.println("[DEBUG] Received \"" + message + "\" from " + address + ":" + port);
        return message;
    }
    public void close() throws IOException{
        this.socket.close();
    }
    public String getAddress(){
        return address + ":" + port;
    }
}