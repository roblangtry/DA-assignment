package algorithms;
import trader.*;
import java.util.concurrent.Semaphore;
import java.io.IOException;

public abstract class AbstractAlgorithm{
    public static Semaphore electionSemaphore = new Semaphore(1);
    public void callElection(Trader trader){
        boolean allowed;
        // Ensure Class can only run one election at a time
        allowed = this.electionSemaphore.tryAcquire();
        if(allowed){
            this.selectNewHost(trader);
            this.electionSemaphore.release();
        }
    }
    public abstract void selectNewHost(Trader trader);
    public void upgradeHost(Trader trader){
        try{
            trader.upgrade();
        } catch (IOException e){
            return;
        }
    }
    public static int determine_value(String serverAddress){
        if(serverAddress == null || serverAddress.split(":").length < 2) return -1;
        int value = 0;
        String ip = serverAddress.split(":")[0];
        int port = Integer.parseInt(serverAddress.split(":")[1]);
        for(String component : ip.split(".")){
            value = value * 256 + Integer.parseInt(component);
        }
        value = value + port;
        return value;
    }
}