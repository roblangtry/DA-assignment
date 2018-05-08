package algorithms;
import trader.*;
public abstract class AbstractAlgorithm{
    public abstract void selectNewHost(Trader trader);
    public static int determine_value(String serverAddress){
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