package trader;

public interface ITrader {
   public ListEntry[] list() throws TraderException;
   public void buy(String user_id, String item_id, int quantity) throws TraderException;
   public void sell(String user_id, String item_id, int quantity) throws TraderException;
   public void register(String user_id) throws TraderException;
   public ListEntry[] inventory(String user_id) throws TraderException;
   public ListEntry[] prices() throws TraderException;
   public int balance(String user_id) throws TraderException;
   public void election(char type);
}