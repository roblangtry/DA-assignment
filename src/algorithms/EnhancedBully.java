
package algorithms;
//from http://www.mdpi.com/2073-431X/1/1/3
import java.io.*;
import java.net.*;
import trader.*;
import java.util.*;
public class EnhancedBully extends AbstractAlgorithm{
    public void upgradeHost(Trader trader){
        ConnectionModule connection;
        try{
            trader.upgrade();
        } catch (IOException e){
            return;
        }
        for(String s : trader.getServers()){
            if(ModifiedBully.determine_value(s) < ModifiedBully.determine_value(trader.self())){
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
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Starting election");
        int denials = 0;
        ConnectionModule connection;
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
        largest = null;
        for(String s : candidates){
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
        if(denials == 0){
            for(String s : ordinary){
                if(EnhancedBully.determine_value(s) > EnhancedBully.determine_value(trader.self())){
                    try{
                        System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending ordinary election notice to \"" + s + "\"");
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
        }
        else {
            try{
                connection = new ConnectionModule(largest);
                connection.send("N" + trader.self());
                connection.close();
                System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending victory notice to \"" + largest + "\"");
            } catch(IOException e){
                return;
            }
            return;
        }
        if(denials == 0 && trader.isLeader() == false){
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
        else if(denials>0){
            try{
                connection = new ConnectionModule(largest);
                connection.send("N" + trader.self());
                connection.close();
                System.out.println("[ \u001B[36mEnhancedBully\u001B[0m ] Sending victory notice to \"" + largest + "\"");
            } catch(IOException e){
                return;
            }
        }
    }

}