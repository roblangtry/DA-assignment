package algorithms;
//from project description
import java.io.*;
import java.net.*;
import java.util.*;
import trader.*;

public class ChangRoberts extends AbstractAlgorithm{
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Starting election");
        String[] servers = trader.getServers();
        ConnectionModule connection;
        try{
            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring probe to \"" + this.getLeft(trader) + "\"");
            connection = new ConnectionModule(this.getLeft(trader));
            connection.send("q" + trader.self()+"~"+trader.self());
            connection.close();
        } catch(IOException e){
            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Error sending ring probe to \"" + this.getLeft(trader) + "\"");
        }
    }
    public static String getLeft(Trader trader){
        String[] servers = trader.getServers();
        int target = Arrays.asList(servers).indexOf(trader.self()) - 1;
        if(target == -1)
            target = servers.length - 1;
        return servers[target];
    }
}