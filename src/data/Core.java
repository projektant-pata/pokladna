package data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import fileManagers.*;

public class Core {
    private int i = 0;
    private ProductsCSV pc;
    private List<Product> cartItems;

    public Core() {
        pc = new ProductsCSV();
        cartItems = new ArrayList<>();

    }

    public int getI() {
        return i;
    }

    public List<Product> getProducts() {
        return pc.read();
    }

    public void addProduct(Product product) {
        pc.write(product);
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public void addCartItem(Product product) {
        cartItems.add(product);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<String> getReceipts() throws SourceException {
        try {
            File receiptsDir = new File("/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts");
            File[] files = receiptsDir.listFiles((dir, name) -> name.startsWith("receipt") && name.endsWith(".dat"));

            List<String> receiptNames = new ArrayList<>();

            for (File file : files)
                receiptNames.add(file.getName());

            receiptNames.sort(String::compareTo);
            return receiptNames;
        } catch (Exception e) {
            throw new SourceException(e.getMessage(), e);
        }
    }

    public List<Product> showReceipt(String receiptName) throws SourceException {
        try {
            ProductFile rf = new ProductFile(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts/" + receiptName);
            List<Product> receiptProducts = rf.getAll();
            rf.close();
            return receiptProducts;
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

            mach.clear();
            for (Product product : cartItems)
                mach.save(product);

            mach.close();

            sebestova.generateReceipt(cartItems);

            cartItems.clear();

            i = (i + 1) % 10;
        } catch (Exception e) {
            throw new SourceException(e.getMessage(), e);
        }

        return true;
    }
}
