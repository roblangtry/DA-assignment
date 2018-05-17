package trader;
import algorithms.*;
import java.io.*;
import java.net.*;
import java.util.*;
public class Watcher  extends Thread{
    private ServerSocket server;
    private Trader trader;
    private boolean running;
    private boolean callElections;
    private boolean electionPossible;
    private boolean franklinRight; // last minute hack to hide extra messages will fix!
    private boolean franklinLeft; // last minute hack to hide extra messages will fix!
    private String fRight; // last minute hack to hide extra messages will fix!
    private String fLeft; // last minute hack to hide extra messages will fix!
    public Watcher(Trader trader, int hostPort) throws IOException{
        this.server = new ServerSocket(hostPort);
        this.server.setSoTimeout(Experiment.SERVER_REFRESH_RATE);
        this.trader = trader;
        this.running = true;
        this.callElections = false;
        this.electionPossible = false;
        franklinRight = false; // last minute hack to hide extra messages will fix!
        franklinLeft = false; // last minute hack to hide extra messages will fix!
    }
    public Watcher(Trader trader, ServerSocket serverSocket) throws IOException{
        this.server = serverSocket;
        this.server.setSoTimeout(Experiment.SERVER_REFRESH_RATE);
        this.trader = trader;
        this.running = true;
        this.callElections = false;
        this.electionPossible = false;
        franklinRight = false; // last minute hack to hide extra messages will fix!
        franklinLeft = false; // last minute hack to hide extra messages will fix!
    }
    public void run(){
        Socket socket;
        // System.out.println("[ \u001B[36mwatcher\u001B[0m ] Awaiting input");
        while(this.running){
            try{
                socket = this.server.accept();
                socket.setSoTimeout(Experiment.LONG_DELAY);
                // System.out.println("[ \u001B[36mwatcher\u001B[0m ] Received connection");
                this.processInput(socket);
            }catch(SocketTimeoutException e){
                // re loop
            }catch(IOException e){
                this.running = false;
            }
        }
    }
    public void callForElections(){
        this.callElections = true;
    }
    public ProxyServer convert() throws IOException{
        this.running = false;
        return new ProxyServer(this.trader, this.server);
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
        ConnectionModule out;
        String raw;
        char command;
        String message;
        boolean reading;
        String[] servers;
        int target;
        try {
            connection = new ConnectionModule(socket);
            reading = true;
            while(reading){
                raw = connection.receive();
                try {
                    Thread.sleep(Experiment.MESSAGE_DELAY);
                } catch(InterruptedException e){
                    //do nothing
                }
                if(raw == null || raw.equals(""))
                    throw new IOException();
                command = raw.charAt(0);
                message = raw.substring(1);
                switch(command){
                    case '<':
                        this.franklinLeft = true;
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
                            if(this.franklinRight){
                                connection = new ConnectionModule(Franklin.getLeft(trader));
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left coordination to \"" + Franklin.getLeft(trader) + "\"");
                                if(Franklin.determine_value(message) < Franklin.determine_value(this.fRight)){
                                    connection.send(")" + this.fRight);
                                    if(trader.self().equals(this.fRight)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(this.fRight);
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
                                this.fLeft = trader.self();
                            }
                            else {
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left probe to \"" + Franklin.getLeft(trader) + "\"");
                                connection = new ConnectionModule(Franklin.getLeft(trader));
                                connection.send("<" + message);
                                connection.close();
                                this.fLeft = message;
                            }
                        }
                        break;
                    case '(':
                        this.franklinRight = false;
                        this.franklinLeft = false;
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received left coordination");
                        if(message.equals(trader.self())){
                            new Franklin().upgradeHost(this.trader);
                        } else {
                            connection = new ConnectionModule(Franklin.getLeft(trader));
                            connection.send("(" + message);
                            connection.close();
                            if(trader.isLeader()) trader.downgrade();
                            trader.relocate(message);
                        }
                        break;
                    case '>':
                        this.franklinRight = true;
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
                            if(this.franklinLeft){
                                connection = new ConnectionModule(Franklin.getRight(trader));
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right coordination to \"" + Franklin.getRight(trader) + "\"");
                                if(Franklin.determine_value(message) < Franklin.determine_value(this.fLeft)){
                                    connection.send(")" + this.fLeft);
                                    if(trader.self().equals(this.fLeft)) {
                                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Won election");
                                        new Franklin().upgradeHost(this.trader);
                                    }
                                    else{
                                        if(trader.isLeader()) trader.downgrade();
                                        trader.relocate(this.fLeft);
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
                                this.fRight = trader.self();
                            }
                            else {
                                System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right probe to \"" + Franklin.getRight(trader) + "\"");
                                connection = new ConnectionModule(Franklin.getRight(trader));
                                connection.send(">" + message);
                                connection.close();
                                this.fRight = message;
                            }
                        }
                        break;
                    case ')':
                        this.franklinRight = false;
                        this.franklinLeft = false;
                        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Received right coordination");
                        if(message.equals(trader.self())){
                            new Franklin().upgradeHost(this.trader);
                        } else {
                            connection = new ConnectionModule(Franklin.getRight(trader));
                            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right coordination to \"" + Franklin.getRight(trader) + "\"");
                            connection.send(")" + message);
                            connection.close();
                            if(trader.isLeader()) trader.downgrade();
                            trader.relocate(message);
                        }
                        break;
                    case 'A':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33maccounts\u001B[0m ] Update");
                        this.trader.accounts_set(message);
                        break;
                    case 'I':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33minventory\u001B[0m ] Update");
                        this.trader.inventory_set(message);
                        break;
                    case 'U':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33muser_inventory\u001B[0m ] Update");
                        this.trader.user_inv_set(message);
                        break;
                    case 'P':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mprices\u001B[0m ] Update");
                        this.trader.prices_set(message);
                        break;
                    case 'S':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mservers\u001B[0m ] Update");
                        this.trader.servers_set(message);
                        this.electionPossible = true;
                        break;
                    case 'C':
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ][ \u001B[33mredirect\u001B[0m ] Received a connection request, Redirecting..");
                        connection.send("R" + this.trader.getLeader());
                        connection.close();
                        reading = false;
                        break;
                    case 'c': //Co-ordinator message
                        System.out.println("[ \u001B[36mElection\u001B[0m ] Received a Coordination request to move to " + message);
                        trader.relocate(message);
                        reading = false;
                        break;
                    case 'g': //election message
                        System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Received a modified election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        reading = false;
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'G':
                        System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Received a GRANT from " + message);
                        System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Won election");
                        new ModifiedBully().upgradeHost(this.trader);
                        reading = false;
                        break;
                    case 'n': //election message
                        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Received an enhanced election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        reading = false;
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'N':
                        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Received a Enhanced Victory notice from " + message);
                        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Won election");
                        new EnhancedBully().upgradeHost(this.trader);
                        reading = false;
                        break;
                    case 'e': //election message
                        System.out.println("[ \u001B[36Bully\u001B[0m ] Received an election request from " + message + " -- denying");
                        connection.send("D"); //deny the lower process
                        connection.close();
                        System.out.println("[ \u001B[36Bully\u001B[0m ] Initiating own election message round");
                        new Bully().callElection(this.trader);
                        // send own election propaganda
                        // reading = false;
                        break;
                    case 'q':
                        servers = trader.getServers();
                        target = Arrays.asList(servers).indexOf(trader.self()) - 1;
                        if(target == -1)
                            target = servers.length - 1;
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Receiving ring probe from \"" + message.split("~")[0] + "\"");
                        if(message.split("~")[1].equals(this.trader.self())){
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Won election");
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring target to \"" + servers[target] + "\"");
                            connection = new ConnectionModule(servers[target]);
                            connection.send("Q" + trader.self()+"~"+trader.self());
                            connection.close();
                            new ChangRoberts().upgradeHost(this.trader);
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
                        reading = false;
                        break;
                    case 'Q':
                        servers = trader.getServers();
                        target = Arrays.asList(servers).indexOf(trader.self()) - 1;
                        if(target == -1)
                            target = servers.length - 1;
                        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Receiving ring target from \"" + message.split("~")[0] + "\"");
                        if(message.split("~")[1].equals(this.trader.self())){
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Finished");
                        }
                        else{
                            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring target to \"" + servers[target] + "\"");
                            connection = new ConnectionModule(servers[target]);
                            connection.send("Q" + trader.self()+"~"+message.split("~")[1]);
                            connection.close();
                            trader.relocate(message.split("~")[1]);
                        }
                        reading = false;
                        break;
                    case 'E': // end connection
                        // System.out.println("[ \u001B[36mwatcher\u001B[0m ] Closed Connection");
                        reading = false;
                        if(this.callElections && this.electionPossible){
                            this.callElections = false;
                            Experiment.CONNECTION_ALGORITHM.callElection(this.trader);
                        }
                        break;
                }
            }
        } catch(IOException e){
            // System.out.println("[ \u001B[36mwatcher\u001B[0m ] Error on connection");
        }
    }
}