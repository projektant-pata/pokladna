package fileManagers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import data.Product;

public class ProductsCSV {
    private final String path = "/home/patrik/javaprograms/pokladna/pokladna/src/files/products.csv";

    public List<Product> read() {
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length == 2) { 
                    String name = values[0].trim();
                    short price;

                    try {
                        price = Short.parseShort(values[1].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Chybný formát ceny: " + values[1]);
                        continue;
                    }

                    Product product = new Product();
                    product.setName(name);
                    product.setPrice(price);
                    products.add(product);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void write(Product product) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(product.getName() + "," + product.getPrice());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
