package algorithms;
//from http://www.cas.mcmaster.ca/borzoo/teaching/15/CAS769/lectures/week4.pdf
import java.io.*;
import java.net.*;
import java.util.*;
import trader.*;

public class Franklin extends AbstractAlgorithm{
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mFranklin\u001B[0m ] Starting election");
        String[] servers = trader.getServers();
        ConnectionModule connection;
        try{
            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending left probe to \"" + this.getLeft(trader) + "\"");
            connection = new ConnectionModule(this.getLeft(trader));
            connection.send("<" + trader.self());
            connection.close();
        } catch(IOException e){
            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Error sending left probe to \"" + this.getLeft(trader) + "\"");
        }
        try{
            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Sending right probe to \"" + this.getRight(trader) + "\"");
            connection = new ConnectionModule(this.getRight(trader));
            connection.send(">" + trader.self());
            connection.close();
        } catch(IOException e){
            System.out.println("[ \u001B[36mFranklin\u001B[0m ] Error sending right probe to \"" + this.getRight(trader) + "\"");
        }
        // this.enterElectionState(trader.getServerSocket());
    }
    public static String getLeft(Trader trader){
        String[] servers = trader.getServers();
        int target = Arrays.asList(servers).indexOf(trader.self()) - 1;
        if(target == -1)
            target = servers.length - 1;
        return servers[target];
    }
    public static String getRight(Trader trader){
        String[] servers = trader.getServers();
        int target = Arrays.asList(servers).indexOf(trader.self()) + 1;
        if(target == servers.length)
            target = 0;
        return servers[target];
    }
    // public static
}