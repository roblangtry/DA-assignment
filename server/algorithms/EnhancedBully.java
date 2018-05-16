
package algorithms;
import java.io.*;
import java.net.*;
import trader.*;
import java.util.*;
public class EnhancedBully extends AbstractAlgorithm{
    public void upgradeHost(Trader trader){
        ConnectionModule connection;
        super.upgradeHost(trader);
        for(String s : trader.getServers()){
            if(ModifiedBully.determine_value(s) < ModifiedBully.determine_value(trader.self())){
                try{
                System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending coordination notice to \"" + s + "\"");
                    connection = new ConnectionModule(s);
                    connection.send("c" + trader.self());
                    connection.close();
                } catch(IOException e){
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Error contacting \"" + s + "\"");
                    trader.removeServer(s);
                }
            }
        }
    }
    public ArrayList<String> getCandidates(Trader trader){
        ArrayList<String> candidates = new ArrayList<String>();
        ArrayList<String> ordinary = new ArrayList<String>(Arrays.asList(trader.getServers()));
        String largest;
        while(candidates.size() < ordinary.size()){
            largest = null;
            for (String s : ordinary){
                if (largest == null)
                    largest = s;
                else if(EnhancedBully.determine_value(s) > EnhancedBully.determine_value(largest))
                    largest = s;
            }
            ordinary.remove(largest);
            candidates.add(largest);
        }
        return candidates;
    }
    public ArrayList<String> getOrdinary(Trader trader){
        ArrayList<String> candidates = new ArrayList<String>();
        ArrayList<String> ordinary = new ArrayList<String>(Arrays.asList(trader.getServers()));
        String largest;
        while(candidates.size() < ordinary.size()){
            largest = null;
            for (String s : ordinary){
                if (largest == null)
                    largest = s;
                else if(EnhancedBully.determine_value(s) > EnhancedBully.determine_value(largest))
                    largest = s;
            }
            ordinary.remove(largest);
            candidates.add(largest);
        }
        return ordinary;
    }
    private String lookup(ArrayList<String> list, Trader trader){
        ConnectionModule connection;
        String largest = null;
        int denials = 0;
        for(String s : list){
            if(EnhancedBully.determine_value(s) > EnhancedBully.determine_value(trader.self())){
                try{
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending candidate election notice to \"" + s + "\"");
                    connection = new ConnectionModule(s);
                    connection.send("n" + trader.self());
                    connection.receive(10000);
                    if (largest == null)
                        largest = s;
                    else if(EnhancedBully.determine_value(s) > EnhancedBully.determine_value(largest))
                        largest = s;
                    denials = denials + 1;
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Recieved denial from \"" + s + "\"");
                }catch (IOException e){
                    trader.removeServer(s);
                }
            }
        }
        if(denials == 0)
            return null;
        return largest;
    }
    public void sendVictory(String host, Trader trader){
        ConnectionModule connection;
        try{
            connection = new ConnectionModule(host);
            connection.send("N" + trader.self());
            connection.close();
            System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending victory notice to \"" + host + "\"");
        } catch(IOException e){
            System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Error sending victory notice to \"" + host + "\"");
            return;
        }
    }
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Starting election");
        String host;
        ConnectionModule connection;
        ArrayList<String> candidates = getCandidates(trader);
        ArrayList<String> ordinary = getOrdinary(trader);
        host = lookup(candidates, trader);
        if(host == null){
            host = lookup(ordinary, trader);
        }
        if(host == null && !trader.isLeader()){
            try{
                trader.upgrade();
            } catch (IOException e){
                return;
            }
            for(String s : trader.getServers()){
                if(EnhancedBully.determine_value(s) < EnhancedBully.determine_value(trader.self())){
                    try{
                    System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending coordination notice to \"" + s + "\"");
                        connection = new ConnectionModule(s);
                        connection.send("c" + trader.self());
                        connection.close();
                    } catch(IOException e){
                        trader.removeServer(s);
                    }
                }
            }
        }
        else if(host != null){
            this.sendVictory(host, trader);
        }
    }

}