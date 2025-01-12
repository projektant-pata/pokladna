package data;

public class Product {
    private String name;
    private short price;
    public static final int NAME_LENGTH, NAME_SIZE, DATA_SIZE;

    static {
        NAME_LENGTH = 45;
        NAME_SIZE = NAME_LENGTH * 2;
        DATA_SIZE = NAME_SIZE + 2;
    }

    //GETTERY
    public String getName() {
        return name;
    }
    public short getPrice() {
        return price;
    }

    //SETTERY
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(short price) {
        this.price = price;
    }
}
