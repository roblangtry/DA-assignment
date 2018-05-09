package trader;
import java.io.*;
import java.net.*;

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

    }
    public void sendRedirect() throws IOException{
        this.send("R" + this.address + "~" + this.port);
    }
    public String receive() throws IOException{
        return reader.readLine();
    }
    public String receive(int time) throws IOException{
        this.socket.setSoTimeout(time);
        return reader.readLine();
    }
    public void close() throws IOException{
        this.socket.close();
    }
}