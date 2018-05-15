package trader;

import java.io.*;
import java.net.*;
public class ProxyServer  extends Thread{
    ServerSocket server;
    Trader trader;
    boolean running;
    public ProxyServer(Trader trader, int host_port) throws IOException{
        this.server = new ServerSocket(host_port);
        this.server.setSoTimeout(10000);
        this.trader = trader;
        this.running = true;
    }
    public void run(){
        try{
            // System.out.println("[ \u001B[36mproxy\u001B[0m ] Accepting connections");
            while(this.running){
                try{
                    new ProxyConnection(this.trader, this.server.accept(), this).start();
                    // System.out.println("[ \u001B[36mproxy\u001B[0m ] New connection setup!");
                } catch(SocketTimeoutException e) {
                    //do nothing
                }
            }
        }catch(IOException e){
            //
        }
        // System.out.println("[ \u001B[36mproxy\u001B[0m ] Shut down");
    }
    public void shutoff(){
        try{
            this.server.close();
        } catch(IOException e){
            //
        }
        this.running = false;
        this.interrupt();
    }
}