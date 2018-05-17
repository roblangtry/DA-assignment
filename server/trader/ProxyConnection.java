package trader;

import java.io.*;
import java.net.*;
import algorithms.*;
import java.util.*;
public class ProxyConnection extends Thread{
    Socket socket;
    ProxyServer proxy_server;
    ConnectionModule connection;
    Trader trader;
    public ProxyConnection(Trader trader, Socket socket, ProxyServer proxy_server) throws IOException{
        socket.setSoTimeout(Experiment.LONG_DELAY);
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
        String[] servers;
        int target;
        boolean franklinRight = false; // last minute hack to hide extra messages will fix!
        boolean franklinLeft = false; // last minute hack to hide extra messages will fix!
        String fRight = ""; // last minute hack to hide extra messages will fix!
        String fLeft = ""; // last minute hack to hide extra messages will fix!
        try {
            // System.out.println("[ \u001B[36mproxy\u001B[0m ] Variables prepared for connection");
            missive = this.connection.receive();
            message = missive.substring(1);
            try {
                Thread.sleep(Experiment.MESSAGE_DELAY);
            } catch(InterruptedException e){
                //do nothing
            }
            switch(missive.charAt(0)){
                case '<':
                    System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received left probe");
                    if(message.equals(trader.self())){
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left coordination to \"" + Franklin.getLeft(trader) + "\"");
                        connection = new ConnectionModule(Franklin.getLeft(trader));
                        connection.send("(" + trader.self());
                        connection.close();
                        new Franklin().upgradeHost(this.trader);
                    }
                    else
                    {
                        if(franklinRight){
                            connection = new ConnectionModule(Franklin.getLeft(trader));
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left coordination to \"" + Franklin.getLeft(trader) + "\"");
                                if(Franklin.determine_value(message) < Franklin.determine_value(fRight)){
                                    connection.send(")" + fRight);
                                    if(trader.self().equals(fRight)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(fRight);
                                    }
                                } else {
                                    connection.send(")" + message);
                                    if(trader.self().equals(message)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(message);
                                    }
                                }
                                connection.close();
                        }
                        else if(Franklin.determine_value(message) < Franklin.determine_value(trader.self())){
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left probe to \"" + Franklin.getLeft(trader) + "\"");
                            connection = new ConnectionModule(Franklin.getLeft(trader));
                            connection.send("<" + trader.self());
                            connection.close();
                            fLeft = trader.self();
                        }
                        else {
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left probe to \"" + Franklin.getLeft(trader) + "\"");
                            connection = new ConnectionModule(Franklin.getLeft(trader));
                            connection.send("<" + message);
                            connection.close();
                            fLeft = message;
                        }
                    }
                    franklinLeft = true;
                    break;
                case '(':
                    franklinLeft = false;
                    franklinRight = false;
                    System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received left coordination");
                    if(message.equals(trader.self())){
                        new Franklin().upgradeHost(this.trader);
                    } else {
                        connection = new ConnectionModule(Franklin.getLeft(trader));
                    System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left coordination to \"" + Franklin.getLeft(trader) + "\"");
                        connection.send("(" + message);
                        connection.close();
                        if(trader.isLeader()) trader.downgrade();
                        trader.relocate(message);
                    }
                    break;
                case '>':
                        franklinRight = true;
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received right probe");
                        if(message.equals(trader.self())){
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                            connection = new ConnectionModule(Franklin.getRight(trader));
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right coordination to \"" + Franklin.getRight(trader) + "\"");
                            connection.send(")" + trader.self());
                            connection.close();
                            new Franklin().upgradeHost(this.trader);
                        }
                        else
                        {
                            if(franklinLeft){
                                connection = new ConnectionModule(Franklin.getRight(trader));
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right coordination to \"" + Franklin.getRight(trader) + "\"");
                                if(Franklin.determine_value(message) < Franklin.determine_value(fLeft)){
                                    connection.send(")" + fLeft);
                                    if(trader.self().equals(fLeft)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(fLeft);
                                    }
                                } else {
                                    connection.send(")" + message);
                                    if(trader.self().equals(message)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(message);
                                    }
                                }
                                connection.close();
                            }
                            else if(Franklin.determine_value(message) < Franklin.determine_value(trader.self())){
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right probe to \"" + Franklin.getRight(trader) + "\"");
                                connection = new ConnectionModule(Franklin.getRight(trader));
                                connection.send(">" + trader.self());
                                connection.close();
                                fRight = trader.self();
                            }
                            else {
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right probe to \"" + Franklin.getRight(trader) + "\"");
                                connection = new ConnectionModule(Franklin.getRight(trader));
                                connection.send(">" + message);
                                connection.close();
                                fRight = message;
                            }
                        }
                    break;
                case ')':
                    franklinLeft = false;
                    franklinRight = false;
                    System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received right coordination");
                    if(message.equals(trader.self())){
                        new Franklin().upgradeHost(this.trader);
                    } else {
                        connection = new ConnectionModule(Franklin.getRight(trader));
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right coordination to \"" + Franklin.getRight(trader) + "\"");
                        connection.send("(" + message);
                        connection.close();
                        if(trader.isLeader()) trader.downgrade();
                        trader.relocate(message);
                    }
                    break;
                case 'C':
                    this.connection.send("C");
                    // System.out.println("[ \u001B[36mproxy\u001B[0m ] Sent connection message");
                    this.trader.addServer(this.connection.receive());
                    running = true;
                    break;
                case 'c': //Co-ordinator message
                    System.out.println("[ \u001B[36mElection\u001B[0m ] Received a Coordination request to move to " + message);
                    trader.downgrade();
                    trader.relocate(message);
                    break;
                case 'g': //election message
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Received a modified election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    // send own election propaganda
                    // reading = false;
                    break;
                case 'G':
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Won election");
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Received a GRANT from " + message);
                    break;
                case 'n': //election message
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Received an enhanced election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    // send own election propaganda
                    // reading = false;
                    break;
                case 'N':
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Won election");
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Received a Enhanced Victory notice from " + message);
                    break;
                case 'e': //election message
                    System.out.println("[ \u001B[Bully\u001B[0m ] Received an election request from " + message + " -- denying");
                    connection.send("D"); //deny the lower process
                    connection.close();
                    System.out.println("[ \u001B[Bully\u001B[0m ] Initiating own election message round");
                    new Bully().callElection(this.trader);
                    break;
                case 'q':
                    servers = trader.getServers();
                    target = Arrays.asList(servers).indexOf(trader.self()) - 1;
                    if(target == -1)
                        target = servers.length - 1;
                    System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Receiving ring probe from \"" + message.split("~")[0] + "\"");
                    if(message.split("~").length >= 2 && message.split("~")[1].equals(this.trader.self())){
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Won election");
                        // only update others target if we are not currently the leader
                        if(!this.trader.isLeader()){
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring target to \"" + servers[target] + "\"");
                            connection = new ConnectionModule(servers[target]);
                            connection.send("Q" + trader.self()+"~"+trader.self());
                            connection.close();
                            new ChangRoberts().upgradeHost(this.trader);
                        } else{
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Finished");
                        }
                    }
                    else{
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring probe to \"" + servers[target] + "\"");
                        connection = new ConnectionModule(servers[target]);
                        if(ChangRoberts.determine_value(message.split("~")[1]) < ChangRoberts.determine_value(trader.self()))
                            connection.send("q" + trader.self()+"~"+trader.self());
                        else
                            connection.send("q" + trader.self()+"~"+message.split("~")[1]);
                        connection.close();
                    }
                    break;
                case 'Q':
                    servers = trader.getServers();
                    target = Arrays.asList(servers).indexOf(trader.self()) - 1;
                    if(target == -1)
                        target = servers.length - 1;
                    System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Receiving ring target from \"" + message.split("~")[0] + "\"");
                    if(message.split("~")[1].equals(trader.self())){
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Finished");
                    }
                    else{
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring target to \"" + servers[target] + "\"");
                        connection = new ConnectionModule(servers[target]);
                        connection.send("Q" + trader.self()+"~"+message.split("~")[1]);
                        connection.close();
                        trader.downgrade();
                        trader.relocate(message.split("~")[1]);
                    }
                    break;
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