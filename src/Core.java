import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import data.Product;
import data.SourceException;
import fileManagers.*;

public class Core {
    private int i = 0;
    private ProductsCSV pc;
    private List<Product> cartItems;

    public int getI() {
        return i;
    }

    public List<Product> getProducts() {
        return pc.read();
    }

    public boolean addProduct(Product product) {
        pc.write(product);
        return true;
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public boolean addCartItem(Product product) {
        cartItems.add(product);
        return true;
    }

    public List<String> getReceipts() throws SourceException {
        try {
            File receiptsDir = new File("/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts");
            File[] files = receiptsDir.listFiles((dir, name) -> name.startsWith("receipt") && name.endsWith(".dat"));

            List<String> receiptNames = new ArrayList<>();

            for (File file : files) {
                receiptNames.add(file.getName());
            }

            receiptNames.sort(String::compareTo);

            return receiptNames;
        } catch (Exception e) {
            throw new SourceException(e.getMessage(), e);
        }
    }

    public boolean saveReceipt() throws SourceException {
        if (cartItems.isEmpty())
            return false;
        try {
            ProductFile mach = new ProductFile(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts/receipt" + i + ".dat");
            ReceiptGenerator sebestova = new ReceiptGenerator(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/printedReceipts/prettyReceipt" + i + ".txt");

            i++;
            if (i >= 10)
                i = 0;

            mach.clear();
            for (Product product : cartItems)
                mach.save(product);

            sebestova.generateReceipt(cartItems);
            cartItems.clear();
            mach.close();

        } catch (Exception e) {
            throw new SourceException(e.getMessage(), e);
        }

        return true;
    }
}
