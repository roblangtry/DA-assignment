package trader;

public interface ITrader {
   public ListEntry[] list() throws TraderException;
   public void buy(String user_id, String item_id, int quantity) throws TraderException;
   public void sell(String item_id, int quantity) throws TraderException;
}