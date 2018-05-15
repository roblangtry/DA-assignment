package algorithms;
//Modified Bully algorithm by M.S. Kordafshari et al -- https://arxiv.org/ftp/arxiv/papers/1010/1010.1812.pdf
import java.io.*;
import java.net.*;
import trader.*;
public class ModifiedBully extends AbstractAlgorithm{
    public void upgradeHost(Trader trader){
        ConnectionModule connection;
        super.upgradeHost(trader);
        for(String s : trader.getServers()){
            if(ModifiedBully.determine_value(s) < ModifiedBully.determine_value(trader.self())){
                try{
                System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Sending coordination notice to \"" + s + "\"");
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
        System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Starting election");
        int denials = 0;
        ConnectionModule connection;
        String highest = null;
        String input;
        for(String s : trader.getServers()){
            if(ModifiedBully.determine_value(s) > ModifiedBully.determine_value(trader.self())){
                try{
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Sending election notice to \"" + s + "\"");
                    connection = new ConnectionModule(s);
                    connection.send("g" + trader.self());
                    connection.receive(Experiment.LONG_DELAY);
                    if(highest == null)
                        highest = s;
                    else if(ModifiedBully.determine_value(highest) < ModifiedBully.determine_value(s))
                        highest = s;
                    denials = denials + 1;
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Recieved denial from \"" + s + "\"");
                }catch (IOException e){
                    trader.removeServer(s);
                }
            }
        }
        if(denials == 0 && trader.isLeader() == false){
            try{
                trader.upgrade();
            } catch (IOException e){
                return;
            }
            for(String s : trader.getServers()){
                if(ModifiedBully.determine_value(s) < ModifiedBully.determine_value(trader.self())){
                    try{
                    System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Sending coordination notice to \"" + s + "\"");
                        connection = new ConnectionModule(s);
                        connection.send("c" + trader.self());
                        connection.close();
                    } catch(IOException e){
                        trader.removeServer(s);
                    }
                }
            }
        } else if(denials >0) {
            try{
                System.out.println("[ \u001B[36mModifiedBully\u001B[0m ] Sending GRANT notice to \"" + highest + "\"");
                connection = new ConnectionModule(highest);
                connection.send("G" + trader.self());
                connection.close();
            } catch (IOException e){
                // If we couldn't connect to the new leader to inform them call another election to ensure we have a leader.
                this.selectNewHost(trader);
            }
        }
    }

}