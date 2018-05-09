package algorithms;
//from ;ecture slides
import java.io.*;
import java.net.*;
import trader.*;
public class Bully extends AbstractAlgorithm{
    public void selectNewHost(Trader trader){
        System.out.println("[ \u001B[36mBully\u001B[0m ] Starting election");
        int denials = 0;
        ConnectionModule connection;
        for(String s : trader.getServers()){
            if(Bully.determine_value(s) > Bully.determine_value(trader.self())){
                try{
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Sending election notice to \"" + s + "\"");
                    connection = new ConnectionModule(s);
                    connection.send("e" + trader.self());
                    connection.receive(10000);
                    denials = denials + 1;
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Recieved denial from \"" + s + "\"");
                }catch (IOException e){
                    
                    trader.removeServer(s);
                }
            }
        }
        if(denials == 0 && trader.coord() == false){
            System.out.println("[ \u001B[36mBully\u001B[0m ] Won election");
            try{
                trader.upgrade();
            } catch (IOException e){
                return;
            }
            for(String s : trader.getServers()){
                if(Bully.determine_value(s) < Bully.determine_value(trader.self())){
                    try{
                    System.out.println("[ \u001B[36mBully\u001B[0m ] Sending coordination notice to \"" + s + "\"");
                        connection = new ConnectionModule(s);
                        connection.send("c" + trader.self());
                        connection.close();
                    } catch(IOException e){
                        trader.removeServer(s);
                    }
                }
            }
        }
    }

}