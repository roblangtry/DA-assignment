package algorithms;
//from project description
import java.io.*;
import java.net.*;
import java.util.*;
import trader.*;

public class ChangRoberts extends AbstractAlgorithm{
    public void upgradeHost(Trader trader){
        ConnectionModule connection;
        try{
            trader.upgrade();
        } catch (IOException e){
            return;
        }
    }
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Starting election");
        String[] servers = trader.getServers();
        ConnectionModule connection;
        int target = Arrays.asList(servers).indexOf(trader.self()) - 1;
        if(target == -1)
            target = servers.length - 1;
        try{
            System.out.println("[ \u001B[36mChangRoberts\u001B[0m ] Sending ring probe to \"" + servers[target] + "\"");
            connection = new ConnectionModule(servers[target]);
            connection.send("q" + trader.self()+"~"+trader.self());
            connection.close();
        } catch(IOException e){
            //
        }
    }

}