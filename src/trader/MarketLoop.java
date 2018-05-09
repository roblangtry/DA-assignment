package trader;

import algorithms.*;
public class MarketLoop extends Thread {
    private Trader trader;
    private static final int delay = 10000;
    private boolean running;
    private AbstractAlgorithm algorithm;
    public MarketLoop(Trader trader){
        this.trader = trader;
        this.running = true;
        this.algorithm = new ChangRoberts();
    }
    public void run() {
        while(running){
            try {
                Thread.sleep(this.delay);
                trader.price_update();
                this.algorithm.selectNewHost(this.trader);
                Thread.sleep(this.delay);
                trader.inventory_update();
                this.algorithm.selectNewHost(this.trader);
            } catch (InterruptedException e){
                running = false;
            }

        }
    }
    public void shutoff(){
        this.interrupt();
    }
}