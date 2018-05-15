package algorithms;
//from ;ecture slides
import java.io.*;
import java.net.*;
import trader.*;


public class Bully extends AbstractAlgorithm{

    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mBully\u001B[0m ] Starting election");
        boolean electionResult = runElection(trader);
        trader = trader;
        // If we won and we arent already the leader handle election victory
        if(electionResult && trader.isLeader() == false){
            electionVictory(trader);
        }
    }
    private boolean runElection(Trader trader){
        int denials = 0;
        String message;
        ConnectionModule connection;
        for(String server : trader.getServers()){
            // Only worry about people "stronger" than us
            if(Bully.determine_value(server) > Bully.determine_value(trader.self())){
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Sending election notice to \"" + server + "\"");
                    try {
                        connection = new ConnectionModule(server);
                    } catch (IOException e){
                        System.out.println("[ \u001B[36mBully\u001B[0m ] Error Connecting to \"" + server + "\"");
                        // if there has been an error then the node is down remove it from the list
                        trader.removeServer(server);
                        continue;
                    }
                    try {
                        connection.send("e" + trader.self());
                    } catch(IOException e){
                        System.out.println("[ \u001B[36mBully\u001B[0m ] Error sending message to \"" + server + "\"");
                        // if there has been an error then the node is down remove it from the list
                        trader.removeServer(server);
                        continue;
                    }
                    try {
                        message = connection.receive();
                    } catch(IOException e){
                        System.out.println("[ \u001B[36mBully\u001B[0m ] Error receiving message from \"" + server + "\"");
                        // if there has been an error then the node is down remove it from the list
                        trader.removeServer(server);
                        continue;
                    }
                    denials = denials + 1;
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Recieved denial from \"" + server + "\"");
            }
        }
        // If no server denied us we have won election.
        return (denials == 0);
    }
    private void electionVictory(Trader trader){
        ConnectionModule connection;
        System.out.println("[ \u001B[36mBully\u001B[0m ] Won election");
            try{
                trader.upgrade();
            } catch (IOException e){
                return;
            }
            for(String server : trader.getServers()){
                if(Bully.determine_value(server) < Bully.determine_value(trader.self())){
                    try{
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Sending coordination notice to \"" + server + "\"");
                        connection = new ConnectionModule(server);
                        connection.send("c" + trader.self());
                        connection.close();
                    } catch(IOException e){
                        trader.removeServer(server);
                    }
                }
            }
    }
}