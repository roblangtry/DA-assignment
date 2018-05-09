package trader;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.Random;
import java.util.concurrent.Semaphore;
import algorithms.*;

public class Trader implements ITrader{
    private ArrayList<String> serverList;
    private HashMap<String, Integer> inventory;
    private HashMap<String, Integer> accounts;
    private HashMap<String, Integer> prices;
    private HashMap<String, HashMap<String,Integer>> user_inventory;
    private boolean remote;
    private Semaphore inventory_semaphore;
    private Semaphore accounts_semaphore;
    private Semaphore user_inventory_semaphore;
    private Semaphore server_semaphore;
    private Random rand;
    private RemoteTrader remote_trader = null;
    private ProxyServer connection = null;
    private int serverport;
    private MarketLoop loop;
    private static final String[] item_ids = {"apple", "banana", "coconut", "dragonfruit"};
    public Trader(int serverport) throws IOException{
      this.rand = new Random();
      this.loop = new MarketLoop(this);
      this.serverList = new ArrayList<String>();
      this.inventory = new HashMap<String, Integer>();
      this.accounts = new HashMap<String, Integer>();
      this.prices = new HashMap<String, Integer>();
      this.user_inventory = new HashMap<String, HashMap<String,Integer>>();
      for(String id: this.item_ids){
        this.inventory.put(id, 100);
        this.prices.put(id, 10);
      }
      this.remote = false;
      this.serverport = serverport;
      this.setup_proxy();
      this.inventory_semaphore = new Semaphore(1);
      this.accounts_semaphore = new Semaphore(1);
      this.user_inventory_semaphore = new Semaphore(1);
      this.server_semaphore = new Semaphore(1);
      this.serverList.add(this.self());
      loop.start();
      // Setup a main server
    }
    public Trader(String hostAddress, int hostPort, int serverport) throws IOException{
      this.rand = new Random();
      this.serverList = new ArrayList<String>();
      this.inventory = new HashMap<String, Integer>();
      this.accounts = new HashMap<String, Integer>();
      this.prices = new HashMap<String, Integer>();
      this.user_inventory = new HashMap<String, HashMap<String,Integer>>();
      this.remote = true;
      this.serverport = serverport;
      this.inventory_semaphore = new Semaphore(1);
      this.accounts_semaphore = new Semaphore(1);
      this.user_inventory_semaphore = new Semaphore(1);
      this.server_semaphore = new Semaphore(1);
      // System.out.println("[ \u001B[36minternal\u001B[0m ] Connecting to remote trader");
      try {
        this.remote_trader = new RemoteTrader(this, new ConnectionModule(hostAddress, hostPort), serverport);
        // System.out.println("[ \u001B[36minternal\u001B[0m ] Remote trader connected!");
      } catch (IOException e) {
        // System.out.println("[ \u001B[36minternal\u001B[0m ] Remote trader refuced connection!");
      }
      // Setup a main server
    }
    public void setup_proxy() throws IOException{
      this.connection = new ProxyServer(this, this.serverport);
      this.connection.start();
    }

    public ListEntry[] list_order() throws TraderException{
      ArrayList<ListEntry> l = new ArrayList<ListEntry>();
      for(String key : this.inventory.keySet()){
        l.add(new ListEntry(key, this.inventory.get(key)));
      }
      // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mlist\u001B[0m ]");
      return l.toArray(new ListEntry[l.size()]);
    }
    public ListEntry[] list() throws TraderException{
      return this.list_order();
    }
    private void buy_order(String user_id, String item_id, int quantity) throws TraderException{
      int price;
      int acc;
      HashMap<String,Integer> inv;
      try {
        this.inventory_semaphore.acquire();
        try {
          this.accounts_semaphore.acquire();
          try {
            this.user_inventory_semaphore.acquire();
            int inventoryQuantity;
            int oQuantity = 0;
            try {
              oQuantity = this.inventory.get(item_id);
              price = this.prices.get(item_id);
              acc = this.accounts.get(user_id);
              inventoryQuantity = oQuantity;
              if(inventoryQuantity < quantity){
                // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[32mbuy\u001B[0m ] %s attempted to buy %d %s, not enough quantity exists in system\n", user_id, quantity, item_id);
                this.user_inventory_semaphore.release();
                this.accounts_semaphore.release();
                this.inventory_semaphore.release();
                throw new InventoryException();
              }
              if(price * quantity > acc){
                // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[32mbuy\u001B[0m ] %s attempted to buy %d %s, not enough quantity exists in system\n", user_id, quantity, item_id);
                this.user_inventory_semaphore.release();
                this.accounts_semaphore.release();
                this.inventory_semaphore.release();
                throw new InventoryException();
              }

              inventoryQuantity = inventoryQuantity - quantity;
              this.inventory.put(item_id, inventoryQuantity);
              this.accounts.put(user_id, acc - price * quantity);
              inv = this.user_inventory.get(user_id);
              try {
                inv.put(item_id, inv.get(item_id) + quantity);
              } catch(NullPointerException e){
                inv.put(item_id, quantity);
              }
              this.user_inventory.put(user_id, inv);
              // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[32mbuy\u001B[0m ] %s bought %d %s, total quantity available now is %d\n", user_id, quantity, item_id, oQuantity);
              pushUser(user_id, item_id);
              pushProduct(item_id);
            } catch (NullPointerException e){
              // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[32mbuy\u001B[0m ] %s attempted to buy %d %s, this product does not exist in the system\n", user_id, quantity, item_id);
              this.user_inventory_semaphore.release();
              this.accounts_semaphore.release();
              this.inventory_semaphore.release();
              throw new UnknownItemException();
            }
            this.user_inventory_semaphore.release();
            this.accounts_semaphore.release();
            this.inventory_semaphore.release();
          } catch (InterruptedException e){
            this.user_inventory_semaphore.release();
            throw new InterruptedException();
          }
        } catch (InterruptedException e){
          this.accounts_semaphore.release();
          throw new InterruptedException();
        }
      } catch (InterruptedException e){
        this.inventory_semaphore.release();
        throw new InternalTraderException();
      }
    }
    private void sell_order(String user_id, String item_id, int quantity) throws TraderException{
      HashMap<String,Integer> inv;
      int val;
      try {
        this.inventory_semaphore.acquire();
        try {
          this.accounts_semaphore.acquire();
          try {
            this.user_inventory_semaphore.acquire();
            inv = user_inventory.get(user_id);
            try{
              val = inv.get(item_id);
            } catch(NullPointerException e){
              // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[31msell\u001B[0m ] Couldn't sell %d %s, User has no %s\n", quantity, item_id, item_id);
              this.user_inventory_semaphore.release();
              this.accounts_semaphore.release();
              this.inventory_semaphore.release();
              throw new MissingQuantityException();
            }
            if(val < quantity){
              // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[31msell\u001B[0m ] Couldn't sell %d %s, User doesn't have required quantity\n", quantity, item_id);
              this.user_inventory_semaphore.release();
              this.accounts_semaphore.release();
              this.inventory_semaphore.release();
              throw new MissingQuantityException();
            }
            inv.put(item_id, val - quantity);
            user_inventory.put(user_id, inv);
            val = this.accounts.get(user_id);
            this.accounts.put(user_id, val + quantity * prices.get(item_id));
            int inventoryQuantity;
            try {
              inventoryQuantity = this.inventory.get(item_id) + quantity;
            } catch (NullPointerException e){
              inventoryQuantity = quantity;
            }
            this.inventory.put(item_id, inventoryQuantity);
            // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[31msell\u001B[0m ] Selling %d %s, total quantity available now is %d\n", quantity, item_id, inventoryQuantity);
            pushUser(user_id, item_id);
            pushProduct(item_id);
            this.user_inventory_semaphore.release();
            this.accounts_semaphore.release();
            this.inventory_semaphore.release();
          } catch (InterruptedException e){
            this.user_inventory_semaphore.release();
            throw new InterruptedException();
          }
        } catch (InterruptedException e){
          this.accounts_semaphore.release();
          throw new InterruptedException();
        }
      } catch (InterruptedException e){
        this.inventory_semaphore.release();
        throw new InternalTraderException();
      }
    }



    public void buy(String user_id, String item_id, int quantity) throws TraderException{
      if(this.remote){
        this.remote_trader.buy(user_id, item_id, quantity);
      }
      else{
        this.buy_order(user_id, item_id, quantity);
      }
    }
    public void sell(String user_id, String item_id, int quantity) throws TraderException{
      if(this.remote){
        this.remote_trader.sell(user_id, item_id, quantity);
      }
      else{
        this.sell_order(user_id, item_id, quantity);
      }
    }


   private void register_order(String user_id) throws TraderException{
    int val;
    HashMap<String,Integer> map;
    try {
      this.accounts_semaphore.acquire();
      try {
        this.user_inventory_semaphore.acquire();
        try {
          val = this.accounts.get(user_id);
          // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[31mregistration\u001B[0m ] User \"" + user_id + "\" already exists\n");
        } catch (NullPointerException e){
          this.accounts.put(user_id, 100);
          map = new HashMap<String,Integer>();
          map.put(item_ids[0],0);
          this.user_inventory.put(user_id, map);
          // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[31mregistration\u001B[0m ] User \"" + user_id + "\" setup with 100 credits\n");
          pushUser(user_id, item_ids[0]);
        }
      } catch (InterruptedException e){
        this.user_inventory_semaphore.release();
        throw new InterruptedException();
      } finally {
        this.user_inventory_semaphore.release();
      }
    } catch (InterruptedException e){
      this.accounts_semaphore.release();
      // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[31mregistration\u001B[0m ] registration of user with \"" + user_id + "\" failed\n");
      throw new InternalTraderException();
    } finally {
      this.accounts_semaphore.release();
    }
   }
   public void register(String user_id) throws TraderException{
      if(this.remote){
        this.remote_trader.register(user_id);
      }
      else{
        this.register_order(user_id);
      }
   }



   private ListEntry[] inventory_order(String user_id) throws TraderException{
    ArrayList<ListEntry> l = new ArrayList<ListEntry>();
    for(String key : this.user_inventory.get(user_id).keySet()){
      l.add(new ListEntry(key, this.user_inventory.get(user_id).get(key)));
    }
    // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[31minventory\u001B[0m ]\n");
    return l.toArray(new ListEntry[l.size()]);
   }
   public ListEntry[] inventory(String user_id) throws TraderException{
    return this.inventory_order(user_id);
   }


   private ListEntry[] prices_order() throws TraderException{
      ArrayList<ListEntry> l = new ArrayList<ListEntry>();
      for(String key : this.prices.keySet()){
        l.add(new ListEntry(key, this.prices.get(key)));
      }
      // System.out.println("[ \u001B[36mclient\u001B[0m ][ \u001B[33mprices\u001B[0m ]");
      return l.toArray(new ListEntry[l.size()]);
   }
   public ListEntry[] prices() throws TraderException{
    return this.prices_order();
   }


   public int balance_order(String user_id) throws TraderException{
    // System.out.printf("[ \u001B[36mclient\u001B[0m ][ \u001B[33m" + user_id + "\u001B[0m ][ \u001B[31mbalance\u001B[0m ]\n");
    int balance = 0;
    try{
      balance = accounts.get(user_id);
    } catch (NullPointerException e){
      throw new TraderException();
    } finally {
      return balance;
    }

   }
   public int balance(String user_id) throws TraderException{
    return this.balance_order(user_id);
   }
   public void price_update(){
    int val;
    for(String id: this.item_ids){
      val = this.prices.get(id);
      val = val + rand.nextInt(4) - 1;
      this.prices.put(id, val);
    }
    // System.out.printf("[ \u001B[36mmarket\u001B[0m ][ \u001B[33mpricing\u001B[0m ] Update\n");
    pushPrices();
   }
   public void inventory_update(){
    int val;
    int oval;
    for(String id: this.item_ids){
      val = this.inventory.get(id);
      oval = val;
      val = val + rand.nextInt(2);
      this.inventory.put(id, val);
      if(oval != val) pushProduct(id);
    }
    // System.out.printf("[ \u001B[36mmarket\u001B[0m ][ \u001B[33minventory\u001B[0m ] Update\n");
   }
   public void accounts_set(String message){
    String[] split = message.split("~");
    try{
      this.accounts_semaphore.acquire();
      accounts.put(split[0], Integer.parseInt(split[1]));
      this.accounts_semaphore.release();
    } catch(InterruptedException e){
      this.accounts_semaphore.release();
    }
   }
   public void inventory_set(String message){
    String[] split = message.split("~");
    try{
      this.inventory_semaphore.acquire();
      inventory.put(split[0], Integer.parseInt(split[1]));
      this.inventory_semaphore.release();
    } catch(InterruptedException e){
      this.inventory_semaphore.release();
    }
   }
   public void user_inv_set(String message){
    String[] split = message.split("~");
    HashMap<String, Integer> map;
    try{
      this.user_inventory_semaphore.acquire();
      map = user_inventory.get(split[0]);
      if(map == null)
        map = new HashMap<String, Integer>();
      map.put(split[1], Integer.parseInt(split[2]));
      user_inventory.put(split[0], map);
      this.user_inventory_semaphore.release();
    } catch(InterruptedException e){
      this.user_inventory_semaphore.release();
    }
   }
   public void servers_set(String message){
    try{
      this.server_semaphore.acquire();
      String[] split = message.split("~");
      ArrayList<String> arr = new ArrayList<String>();
      for(String s : split){
        arr.add(s);
      }
      this.serverList = arr;
      this.server_semaphore.release();
    }catch(InterruptedException e){
      this.server_semaphore.release();
    }
   }
   public void prices_set(String message){
    String[] split = message.split("~");
    int i = 0;
    for(String s : item_ids){
      this.prices.put(s, Integer.parseInt(split[i]));
      i = i + 1;
    }
   }
   public void addServer(String server){
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Adding server \"" + server + "\"");
      try{
    String[] split = server.split(":");
    String address = split[0];
    int port = Integer.parseInt(split[1]);
    String message;
    HashMap<String, Integer> map;
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Establishing Connection");
    ConnectionModule connection = new ConnectionModule(address, port);
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Connection Established");
    try{
      this.accounts_semaphore.acquire();
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitting Accounts data");
      for(String key : accounts.keySet()){
        // System.out.println(key);
        connection.send("A" + key + "~" + accounts.get(key));
      }
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitted Accounts data");
      this.accounts_semaphore.release();
    } catch(InterruptedException e){
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Error transmitting Accounts data");
      this.accounts_semaphore.release();
    }
    try{
      this.user_inventory_semaphore.acquire();
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitting User Inventory data");
      for(String k1 : user_inventory.keySet()){
        for(String k2 : user_inventory.get(k1).keySet()){
          connection.send("U" + k1 + "~" + k2 + "~" + user_inventory.get(k1).get(k2));
        }
      }
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitted User Inventory data");
      this.user_inventory_semaphore.release();
    } catch(InterruptedException e){
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Error transmitting User Inventory data");
      this.user_inventory_semaphore.release();
    }
    try{
      this.inventory_semaphore.acquire();
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitting Inventory data");
      for(String key : inventory.keySet()){
        connection.send("I" + key + "~" + inventory.get(key));
      }
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitted Inventory data");
      this.inventory_semaphore.release();
    } catch(InterruptedException e){
      // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Error transmitting Inventory data");
      this.inventory_semaphore.release();
    }
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitting Prices data");
    message = "";
    for(String s : item_ids){
      message = message + this.prices.get(s) + "~";
    }
    connection.send("P" + message);
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Transmitted Prices data");
    // System.out.println("[ \u001B[36mproxy\u001B[0m ][ \u001B[33madd\u001B[0m ] Ending Connection");
    connection.send("E");
    try{
      this.server_semaphore.acquire();
      if(!this.serverList.contains(server))
        this.serverList.add(server);
      this.server_semaphore.release();
    } catch(InterruptedException e){
      this.server_semaphore.release();
    }
    connection.close();
    pushServers();
      } catch(IOException e){
        // System.out.println("error");
      }
   }
   private void pushServers(){
    String[] split;
    String address;
    int port;
    ConnectionModule connection;
    String message = "";
    boolean first = true;
    for(String s: this.serverList){
      if(first)
        first = false;
      else
        message = message + "~";
      message = message + s;
    }
    for(String server: this.serverList){
      if(!server.equals(this.self())){
        try{
          split = server.split(":");
          address = split[0];
          port = Integer.parseInt(split[1]);
          connection = new ConnectionModule(address, port);
          connection.send("S" + message);
          connection.send("E");
          connection.close();
        } catch(IOException e){
          // System.out.println("error");
        }
      }
    }
   }
   private void pushProduct(String item_id){
    String[] split;
    String address;
    int port;
    ConnectionModule connection;
    String message = "";
    for(String server: this.serverList){
      try{
        split = server.split(":");
        address = split[0];
        port = Integer.parseInt(split[1]);
        connection = new ConnectionModule(address, port);
        message = item_id + "~" + inventory.get(item_id);
        connection.send("I" + message);
        connection.send("E");
        connection.close();
      } catch(IOException e){
        // System.out.println("error");
      }
    }
   }
   private void pushAccount(String user_id){
    String[] split;
    String address;
    int port;
    ConnectionModule connection;
    String message = "";
    for(String server: this.serverList){
      try{
        split = server.split(":");
        address = split[0];
        port = Integer.parseInt(split[1]);
        connection = new ConnectionModule(address, port);
        message = user_id + "~" + accounts.get(user_id);
        connection.send("A" + message);
        connection.send("E");
        connection.close();
      } catch(IOException e){
        // System.out.println("error");
      }
    }
   }
   private void pushUser(String user_id, String item_id){
    String[] split;
    String address;
    int port;
    ConnectionModule connection;
    String message = "";
    for(String server: this.serverList){
      try{
        split = server.split(":");
        address = split[0];
        port = Integer.parseInt(split[1]);
        connection = new ConnectionModule(address, port);
        message = user_id + "~" + item_id + "~" + user_inventory.get(user_id).get(item_id);
        connection.send("U" + message);
        message = user_id + "~" + accounts.get(user_id);
        connection.send("A" + message);
        connection.send("E");
        connection.close();
      } catch(IOException e){
        // System.out.println("error");
      }
    }
   }
   private void pushPrices(){
    String[] split;
    String address;
    int port;
    ConnectionModule connection;
    String message = "";
    for(String s : item_ids){
      message = message + this.prices.get(s) + "~";
    }
    for(String server: this.serverList){
      try{
        split = server.split(":");
        address = split[0];
        port = Integer.parseInt(split[1]);
        connection = new ConnectionModule(address, port);
        connection.send("P" + message);
        connection.send("E");
        connection.close();
      } catch(IOException e){
        // System.out.println("error");
      }
    }
   }
   public String self(){
    try{
      return Inet4Address.getLocalHost().getHostAddress() + ":" + this.serverport;
    } catch(UnknownHostException e){
      return "127.0.0.1"+ ":" + this.serverport;
    }
   }
   public String[] getServers(){
    return this.serverList.toArray(new String[this.serverList.size()]);
   }
   public void removeServer(String server){
    try{
      this.server_semaphore.acquire();
      this.serverList.remove(server);
      this.server_semaphore.release();
    } catch (InterruptedException e){
      this.server_semaphore.release();
    }
   }
   public void upgrade() throws IOException{
    if(!remote) return;
    //do upgrade
    this.remote = false;
    try {
      this.inventory_semaphore.acquire();
      this.accounts_semaphore.acquire();
      this.user_inventory_semaphore.acquire();
      this.user_inventory_semaphore.release();
      this.accounts_semaphore.release();
      this.inventory_semaphore.release();
    } catch( InterruptedException e){
      //do something i guess?
    }
    //kill watcher
    this.remote_trader.shutoff();
    //start market loop
    this.setup_proxy();
    this.loop = new MarketLoop(this);
    this.loop.start();
    //swer
   }
   public void downgrade(){
    if(remote)return;
    this.remote = true;
    try {
      this.inventory_semaphore.acquire();
      this.accounts_semaphore.acquire();
      this.user_inventory_semaphore.acquire();
      this.user_inventory_semaphore.release();
      this.accounts_semaphore.release();
      this.inventory_semaphore.release();
    } catch( InterruptedException e){
      //do something i guess?
    }
    this.loop.shutoff();
    this.connection.shutoff();
    // System.out.println("[ \u001B[36minternal\u001B[0m ][ \u001B[33mdowngrade\u001B[0m ]");
   }
   public boolean coord(){
    return !this.remote;
   }
   public void relocate(String server) throws IOException{
    ConnectionModule conn;
    // System.out.println("[ \u001B[36minternal\u001B[0m ][ \u001B[33mrelocate\u001B[0m ] Relocating RemoteTrader to \"" + server + "\"");
    if(this.remote_trader == null){
      try{
        conn = new ConnectionModule(server);
      } catch(IOException e){
        // System.out.println("Mueller");
        conn = null;
      }
        // System.out.println("A");
      this.remote_trader = new RemoteTrader(this, conn, this.serverport);
        // System.out.println("B");
    }
    this.remote_trader.relocate(server);
   }
   public void election(char type){
      switch(type){
        case 'B':
          new Bully().selectNewHost(this);
          break;
        case 'M':
          new ModifiedBully().selectNewHost(this);
          break;
        case 'E':
          new EnhancedBully().selectNewHost(this);
          break;
        case 'C':
          new ChangRoberts().selectNewHost(this);
          break;
        case 'F':
          // new Franklin().selectNewHost(this);
          break;
      }
   }
}