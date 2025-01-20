package fileManagers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import data.Product;

public class ReceiptGenerator {
    private String path;
    public ReceiptGenerator(String path){
        this.path = path;
    }

    public void generateReceipt(List<Product> products) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // Horní část účtenky
            writer.write("=====================================");
            writer.newLine();
            writer.write("              U Ahóje!               ");
            writer.newLine();
            writer.write("          Datum: 18.01.2025          ");
            writer.newLine();
            writer.write("=====================================");
            writer.newLine();
            writer.newLine();

            // Produkty (vlevo jméno, vpravo cena)
            writer.write(String.format("%-25s %10s", "Produkt", "Cena (Kč)"));
            writer.newLine();
            writer.write("-------------------------------------");
            writer.newLine();
            double total = 0;
            for (Product product : products) {
                writer.write(String.format("%-25s %10.2f", product.getName(), (double) product.getPrice()));
                writer.newLine();
                total += product.getPrice();
            }
            writer.write("-------------------------------------");
            writer.newLine();

            // Spodní část účtenky
            writer.write(String.format("%-25s %10.2f", "CELKEM:", total));
            writer.newLine();
            writer.write("=====================================");
            writer.newLine();
            writer.write("      Děkujeme za váš nákup!         ");
            writer.newLine();
            writer.write("=====================================");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
