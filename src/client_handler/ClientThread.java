package client_handler;
import java.net.*;
import java.io.*;
import trader.*;

public class ClientThread extends Thread {
    private Socket socket = null;
    private ITrader trader = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String username = null;
    private boolean running = true;
    private static final boolean debug = true;
    public ClientThread(Socket socket, ITrader trader) {
        this.socket = socket;
        this.trader = trader;
    }
    
    private void setupConnection() throws IOException{
        System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up connection..");

        System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up PrintWriter on socket");
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        System.out.println("[ \u001B[36minternal\u001B[0m ] PrintWriter is set up");

        System.out.println("[ \u001B[36minternal\u001B[0m ] Setting up BufferedReader on socket");
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        System.out.println("[ \u001B[36minternal\u001B[0m ] BufferedReader is set up");

        System.out.println("[ \u001B[36minternal\u001B[0m ] Receiving Username");
        this.username = this.in.readLine();
        System.out.println("[ \u001B[36minternal\u001B[0m ] Username \"" + this.username + "\" received");
        try{
            this.trader.register(this.username);
            System.out.println("[ \u001B[36minternal\u001B[0m ] User successfully registered");
            this.out.print('A');
        } catch (TraderException e){
            this.running = false;
            System.out.println("[ \u001B[36minternal\u001B[0m ] User registration error!");
            this.out.print('F');
        }
        this.out.flush();
        System.out.println("[ \u001B[36minternal\u001B[0m ] Connection setup for " + this.username);
    }
    private void processCommands() throws IOException{
        char command;
        int input;
        boolean first;
        String payload;
        String[] components;
        String itemId;
        int quantity;
        ListEntry[] list;
        int val;
        // System.out.println("[ \u001B[36minternal\u001B[0m ] Accepting commands from " + this.username);
        while(this.running){
            input = this.in.read();
            if(input == -1) throw new IOException();
            command = (char)input;
            switch(command){
                case 'L':
                    try {
                        list = this.trader.list();
                        first = true;
                        for(ListEntry entry : list)
                        {
                            if(first) first = false;
                            else this.out.write('#');
                            this.out.printf("|%14s|%3d|", entry.getItemId(), entry.getQuantity());
                        }
                        this.out.write('\n');
                    } catch(TraderException e) { 
                        this.out.write('F');
                    } finally {
                        this.out.flush(); 
                    }
                    break;
                case 'B':
                    payload = this.in.readLine();
                    components = payload.split("\\s+");
                    itemId = components[0];
                    quantity = Integer.parseInt(components[1]);
                    try {
                        this.trader.buy(this.username, itemId, quantity);
                        this.out.write('S');
                    } catch(TraderException e) { 
                        if(e instanceof InventoryException){
                            this.out.write('I');
                        }
                        else if(e instanceof UnknownItemException){
                            this.out.write('U');
                        }
                        else{
                            this.out.write('F');
                        }
                    } finally {
                        this.out.flush();
                    }
                    break;
                case 'S':
                    payload = this.in.readLine();
                    components = payload.split("\\s+");
                    itemId = components[0];
                    quantity = Integer.parseInt(components[1]);
                    try {
                        this.trader.sell(this.username, itemId, quantity);
                        this.out.write('S');
                    } catch(TraderException e) { 
                        this.out.write('F');
                    } finally {
                        this.out.flush(); 
                    }
                    break;
                case 'I':
                    try {
                        list = this.trader.inventory(this.username);
                        first = true;
                        for(ListEntry entry : list)
                        {
                            if(first) first = false;
                            else this.out.write('#');
                            this.out.printf("|%14s|%3d|", entry.getItemId(), entry.getQuantity());
                        }
                        this.out.write('\n');
                    } catch(TraderException e) { 
                        this.out.write('F');
                    } finally {
                        this.out.flush(); 
                    }
                    break;
                case 'P':
                    try {
                        list = this.trader.prices();
                        first = true;
                        for(ListEntry entry : list)
                        {
                            if(first) first = false;
                            else this.out.write('#');
                            this.out.printf("|%14s|%3d|", entry.getItemId(), entry.getQuantity());
                        }
                        this.out.write('\n');
                    } catch(TraderException e) { 
                        this.out.write('F');
                    } finally {
                        this.out.flush(); 
                    }
                    break;
                case 'A':
                    try {
                        val = trader.balance(this.username);
                        this.out.write('S');
                        this.out.println(val);
                    } catch (TraderException e){
                        this.out.write('F');
                    } finally {
                        this.out.flush();
                    }
                    break;
                case 'E':
                    input = this.in.read();
                    if(input == -1) throw new IOException();
                    command = (char)input;
                    this.out.write('S');
                    trader.election(command);
                    this.out.flush();
                    break;
            }
        }
    }
    public void run() {
        try{
        this.setupConnection();
        this.processCommands();
        }
        catch(IOException e){
            System.out.println("[ \u001B[36minternal\u001B[0m ] Error with client " + this.username);
        }
        System.out.println("[ \u001B[36minternal\u001B[0m ] Closing ClientThread for " + this.username);
    }


}


//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html