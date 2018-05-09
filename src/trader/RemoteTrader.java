package trader;

import java.io.*;
import java.net.*;
import java.util.*;
import algorithms.*;
public class RemoteTrader implements ITrader {
    private ConnectionModule connection;
    private Trader trader;
    private Watcher watcher;
    private AbstractAlgorithm algorithm;
    public RemoteTrader(Trader trader, ConnectionModule conn_module, int serverport) throws IOException{
        String connect_message;
        String addr;
        int port;
        boolean hope = true;
        this.algorithm = new EnhancedBully();
        this.connection = conn_module;
        this.trader = trader;
        this.watcher = new Watcher(trader,serverport);
        this.watcher.start();
        while(hope){
            hope = false;
            this.connection.send("C");
            // System.out.println("[ \u001B[36minternal\u001B[0m ] Sent connection message waiting for reply");
            connect_message = this.connection.receive();
            if(connect_message.charAt(0) == 'C'){
                // System.out.println("[ \u001B[36minternal\u001B[0m ] Reply successful, remote trader connection routine beginning");
                this.connection.send(trader.self());
                // System.out.println("[ \u001B[36minternal\u001B[0m ] Remote trader connection established");
            } else if(connect_message.charAt(0) == 'R'){
                addr = connect_message.substring(1).split("~")[0];
                port = Integer.parseInt(connect_message.substring(1).split("~")[1]);
                // System.out.println("[ \u001B[36minternal\u001B[0m ] Received a redirect now attempting connection to " +addr + ":" + port);
                this.connection.send("E");
                this.connection.close();
                this.connection = new ConnectionModule(addr, port);
                hope = true;
            }
        }

    }
    public void shutoff(){
        this.watcher.shutoff();
        try{
            this.connection.close();
        } catch(IOException e){
            //all g
        }
    }
    public ListEntry[] list() throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mlist\u001B[0m ] Forwarding request");
        String response;
        ArrayList<ListEntry> arrayList = new ArrayList<ListEntry>();
        String[] elements;
        String[] breakdown;
        ListEntry[] list = {};
        try {
            this.connection.send("L");
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mlist\u001B[0m ] Success");
                response = this.connection.receive();
                if(response == null || response.equals("")) throw new IOException();
                elements = response.split("~");
                for (String element : elements){
                    breakdown = element.split("&");
                    arrayList.add(new ListEntry(breakdown[0], Integer.parseInt(breakdown[1])));
                }
                list = arrayList.toArray(new ListEntry[arrayList.size()]);
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mlist\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mlist\u001B[0m ] IO Error");
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mlist\u001B[0m ] Forwarding response");
        return list;
    }
    public void buy(String user_id, String item_id, int quantity) throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mbuy\u001B[0m ] Forwarding request");
        String response;
        try {
            this.connection.send("B" + user_id + "~" + item_id + "~" + quantity);
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mbuy\u001B[0m ] Success");
                //do
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mbuy\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mbuy\u001B[0m ] Forwarding response");
    }
    public void sell(String user_id, String item_id, int quantity) throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33msell\u001B[0m ] Forwarding request");
        String response;
        try {
            this.connection.send("S" + user_id + "~" + item_id + "~" + quantity);
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33msell\u001B[0m ] Success");
                //do
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33msell\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33msell\u001B[0m ] Forwarding response");
    }
    public void register(String user_id) throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mregister\u001B[0m ] Forwarding request");
        String response;
        try {
            this.connection.send("R" + user_id);
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mregister\u001B[0m ] Success");
                //do
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mregister\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mregister\u001B[0m ] Forwarding response");
    }
    public ListEntry[] inventory(String user_id) throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33minventory\u001B[0m ] Forwarding request");
        String response;
        ArrayList<ListEntry> arrayList = new ArrayList<ListEntry>();
        String[] elements;
        String[] breakdown;
        ListEntry[] list = {};
        try {
            this.connection.send("I" + user_id);
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33minventory\u001B[0m ] Success");
                elements = this.connection.receive().split("~");
                for (String element : elements){
                    breakdown = element.split("&");
                    arrayList.add(new ListEntry(breakdown[0], Integer.parseInt(breakdown[1])));
                }
                list = arrayList.toArray(new ListEntry[arrayList.size()]);
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33minventory\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33minventory\u001B[0m ] Forwarding response");
        return list;
    }
    public ListEntry[] prices() throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mprices\u001B[0m ] Forwarding request");
        String response;
        ArrayList<ListEntry> arrayList = new ArrayList<ListEntry>();
        String[] elements;
        String[] breakdown;
        ListEntry[] list = {};
        try {
            this.connection.send("P");
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mprices\u001B[0m ] Success");
                elements = this.connection.receive().split("~");
                for (String element : elements){
                    breakdown = element.split("&");
                    arrayList.add(new ListEntry(breakdown[0], Integer.parseInt(breakdown[1])));
                }
                list = arrayList.toArray(new ListEntry[arrayList.size()]);
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mprices\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mprices\u001B[0m ] Forwarding response");
        return list;
    }
    public int balance(String user_id) throws TraderException
    {
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mbalance\u001B[0m ] Forwarding request");
        String response;
        int balance = 0;
        try {
            this.connection.send("A" + user_id);
            response = this.connection.receive();
            if(response == null || response.equals("")) throw new IOException();
            if(response.charAt(0) == 'S'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mbalance\u001B[0m ] Success");
                balance = Integer.parseInt(this.connection.receive());
            } else if(response.charAt(0) == 'F'){
                // System.out.println("[ \u001B[36mremote\u001B[0m ][ \u001B[33mbalance\u001B[0m ] Error");
                throw new TraderException();
            }
        } catch (IOException e) {
            this.handleFailure();
        }
        // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mbalance\u001B[0m ] Forwarding response");
        return balance;
    }
    public void handleFailure() throws TraderException{
        this.algorithm.selectNewHost(this.trader);
        throw new TraderException();
    }
   public void relocate(String server) throws IOException{
    String connect_message;
    this.connection.close();
    this.connection = new ConnectionModule(server);
    this.connection.send("C");
    // System.out.println("[ \u001B[36minternal\u001B[0m ] Sent connection message waiting for reply");
    connect_message = this.connection.receive();
    if(connect_message.charAt(0) == 'C'){
        // System.out.println("[ \u001B[36minternal\u001B[0m ] Reply successful, remote trader connection routine beginning");
        this.connection.send(trader.self());
        // System.out.println("[ \u001B[36minternal\u001B[0m ] Remote trader connection established");
    }
   }
   public void election(char type){
    // System.out.printf("[ \u001B[31melection\u001B[0m ]\n");
   }
}