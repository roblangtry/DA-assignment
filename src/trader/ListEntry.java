package trader;

public class ListEntry {
    private String itemId;
    private int quantity;
    public ListEntry(String itemId, int quantity){
        this.itemId = itemId;
        this.quantity = quantity;
    }
    public int getQuantity(){
        return this.quantity;
    }
    public String getItemId(){
        return this.itemId;
    }
}