package trader;

import java.io.*;
import java.net.*;
import algorithms.*;
public class ProxyConnection extends Thread{
    Socket socket;
    ProxyServer proxy_server;
    ConnectionModule connection;
    Trader trader;
    public ProxyConnection(Trader trader, Socket socket, ProxyServer proxy_server) throws IOException{
        this.socket = socket;
        this.proxy_server = proxy_server;
        this.trader = trader;
        this.connection = new ConnectionModule(socket);
    }
    public void run() {
        String missive;
        String raw;
        String input;
        String[] params;
        ListEntry[] list;
        String output;
        boolean running = false;
        char command;
        String message;
        int balance;
        try {
            System.out.println("[ \u001B[36mproxy\u001B[0m ] Variables prepared for connection");
            missive = this.connection.receive();
            message = missive.substring(1);
            switch(missive.charAt(0)){
                case 'C':
                    this.connection.send("C");
                    System.out.println("[ \u001B[36mproxy\u001B[0m ] Sent connection message");
                    this.trader.addServer(this.connection.receive());
                    running = true;
                    break;
                case 'c': //Co-ordinator message
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mcordinate\u001B[0m ] Received a Coordination request to move to " + message);
                    trader.downgrade();
                    trader.relocate(message);
                    break;
                case 'g': //election message
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a modified election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    // send own election propaganda
                    // reading = false;
                    break;
                case 'G':
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a GRANT from " + message);
                    break;
                case 'n': //election message
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received an enhanced election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    // send own election propaganda
                    // reading = false;
                    break;
                case 'N':
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received a Enhanced Victory notice from " + message);
                    break;
                case 'e': //election message
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Received an election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33melection\u001B[0m ] Initiating own election message round");
                    new Bully().selectNewHost(this.trader);
                default:
                    this.connection.send("D");
                    this.connection.close();
                    throw new IOException();
            }
            while(running)
            {
                raw = this.connection.receive();
                command = raw.charAt(0);
                input = raw.substring(1);
                switch(command){
                    case 'L':
                        try{
                            list = this.trader.list();
                            this.connection.send("S");
                            output = "";
                            for(ListEntry le : list){
                                output = output + le.getItemId() + "&" + le.getQuantity() + "~";
                            }
                            this.connection.send(output);
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                        break;

                    case 'B':
                        try{
                            params = input.split("~");
                            this.trader.buy(params[0],params[1], Integer.parseInt(params[2]));
                            this.connection.send("S");
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                    
                        break;

                    case 'S':

                        try{
                            params = input.split("~");
                            this.trader.sell(params[0],params[1], Integer.parseInt(params[2]));
                            this.connection.send("S");
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                    params = input.split("~");
                        break;

                    case 'R':

                        try{
                            this.trader.register(input);
                            this.connection.send("S");
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                        break;

                    case 'I':

                        try{
                            list = this.trader.inventory(input);
                            this.connection.send("S");
                            output = "";
                            for(ListEntry le : list){
                                output = output + le.getItemId() + "&" + le.getQuantity() + "~";
                            }
                            this.connection.send(output);
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                        break;

                    case 'P':

                        try{
                            list = this.trader.prices();
                            this.connection.send("S");
                            output = "";
                            for(ListEntry le : list){
                                output = output + le.getItemId() + "&" + le.getQuantity() + "~";
                            }
                            this.connection.send(output);
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                        break;

                    case 'A':

                        try{
                            balance = this.trader.balance(input);
                            this.connection.send("S");
                            output = "";
                            output = output + balance;
                            this.connection.send(output);
                        } catch(TraderException e) {
                            this.connection.send("E");
                        }
                        break;

                }
            }
        } catch (IOException e){
            //
        } catch (NullPointerException e){
            //
        }
    }

}