package trader;


public class MockTrader implements ITrader{
   public ListEntry[] list() throws TraderException{
        ListEntry l1 = new ListEntry("Apple", 7);
        ListEntry l2 = new ListEntry("Orange", 3);
        ListEntry l3 = new ListEntry("Watermelon", 4);
        ListEntry l[] = {l1, l2, l3};
        System.out.println("[ \u001B[33mlist\u001B[0m ]");
        return l;
   }
   public void buy(String user_id, String item_id, int quantity) throws TraderException{
        System.out.printf("[ \u001B[32mbuy\u001B[0m ] %s %s %d\n", user_id, item_id, quantity);
   }
   public void sell(String user_id, String item_id, int quantity) throws TraderException{
        System.out.printf("[ \u001B[31msell\u001B[0m ] %s %d\n", item_id, quantity);
   }
   public void register(String user_id) throws TraderException{
    System.out.printf("[ \u001B[31mregister\u001B[0m ] %s\n", user_id);
   }
   public ListEntry[] inventory(String user_id) throws TraderException{
    System.out.printf("[ \u001B[31minventory\u001B[0m ] %s \n", user_id);
    return null;
   }
   public ListEntry[] prices() throws TraderException{
    System.out.printf("[ \u001B[31mprices\u001B[0m ]\n");
    return null;
   }
   public int balance(String user_id) throws TraderException{
    System.out.printf("[ \u001B[31mbalance\u001B[0m ]\n");
    return 0;
   }
   public void election(char type){
    System.out.printf("[ \u001B[31melection\u001B[0m ]\n");
   }
}