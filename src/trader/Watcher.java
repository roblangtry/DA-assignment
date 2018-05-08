package trader;
import algorithms.*;
import java.io.*;
import java.net.*;
public class Watcher  extends Thread{
    ServerSocket server;
    Trader trader;
    boolean running;
    public Watcher(Trader trader, int host_port) throws IOException{
        this.server = new ServerSocket(host_port);
        this.server.setSoTimeout(10000);
        this.trader = trader;
        this.running = true;
    }
    public void run(){
        Socket socket;
        System.out.println("[ \u001B[36mwatcher\u001B[0m ] Awaiting input");
        while(this.running){
            try{
                socket = this.server.accept();
                System.out.println("[ \u001B[36mwatcher\u001B[0m ] Received connection");
                this.processInput(socket);
            }catch(SocketTimeoutException e){
                // re loop
            }catch(IOException e){
                this.running = false;
            }
        }
    }
    public void shutoff(){
        this.running = false;
        try{
            this.server.close();
        } catch(IOException e) {
            System.out.println("error");
        }
    }
    private void processInput(Socket socket){
        ConnectionModule connection;
        String raw;
        char command;
        String message;
        boolean reading;
        try {
            connection = new ConnectionModule(socket);
            reading = true;
            while(reading){
                raw = connection.receive();
                if(raw == null || raw.equals(""))
                    throw new IOException();
                command = raw.charAt(0);
                message = raw.substring(1);
                switch(command){
                    case 'A':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33maccounts\u001B[0m ] Update");
                        this.trader.accounts_set(message);
                        break;
                    case 'I':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33minventory\u001B[0m ] Update");
                        this.trader.inventory_set(message);
                        break;
                    case 'U':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33muser_inventory\u001B[0m ] Update");
                        this.trader.user_inv_set(message);
                        break;
                    case 'P':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mprices\u001B[0m ] Update");
                        this.trader.prices_set(message);
                        break;
                    case 'S':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mservers\u001B[0m ] Update");
                        this.trader.servers_set(message);
                        break;
                    case 'C':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mredirect\u001B[0m ] Received a connection request, Redirecting..");
                        connection.sendRedirect();
                        break;
                    case 'c': //Co-ordinator message
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mcordinate\u001B[0m ] Received a Coordination request to move to " + message);
                        trader.relocate(message);
                        reading = false;
                        break;
                    case 'g': //election message
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a modified election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        reading = false;
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'G':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a GRANT from " + message);
                        new ModifiedBully().upgradeHost(this.trader);
                        reading = false;
                        break;
                    case 'n': //election message
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received an enhanced election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        reading = false;
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'N':
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a Enhanced Victory notice from " + message);
                        new EnhancedBully().upgradeHost(this.trader);
                        reading = false;
                        break;
                    case 'e': //election message
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received an election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Initiating own election message round");
                        new Bully().selectNewHost(this.trader);
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'E': // end connection
                        System.out.println("[ \u001B[36mwatcher\u001B[0m ] Closed Connection");
                        reading = false;
                        break;
                }
            }
        } catch(IOException e){
            System.out.println("[ \u001B[36mwatcher\u001B[0m ] Error on connection");
        }
    }
}