package trader;


public class MarketLoop extends Thread {
    private Trader trader;
    private static final int delay = 10000;
    private boolean running;
    public MarketLoop(Trader trader){
        this.trader = trader;
        this.running = true;
    }
    public void run() {
        while(running){
            try {
                Thread.sleep(this.delay);
                trader.price_update();
                Thread.sleep(this.delay);
                trader.inventory_update();
            } catch (InterruptedException e){
                running = false;
            }

        }
    }
    public void shutoff(){
        this.interrupt();
    }
}