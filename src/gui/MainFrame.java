package gui;

import javax.swing.*;
import javax.swing.border.Border;

import data.Product;
import fileManagers.ProductFile;
import fileManagers.ProductsCSV;
import fileManagers.ReceiptGenerator;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MainFrame extends JFrame {
    private int i = 0;
    private ProductsCSV pc;
    private List<Product> products, cartItems;
    private JPanel topPanel, productsPanel, rightPanel, cartPanel;
    private JButton addProductButton, addBillButton, cancelBillButton, showReceiptButton;
    private JList<String> receiptList;
    // dysajn
    Border border = BorderFactory.createLineBorder(Color.BLUE, 2);

    public MainFrame() {
        try {
            pc = new ProductsCSV();
            products = pc.read();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání souboru produktů: " + e.getMessage(), "Chyba",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        cartItems = new ArrayList<Product>();

        // config okna
        setTitle("Pokladní systém");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // inicializace panelu
        createTopPanel();
        createProductsPanel();
        createRightPanel();

        // pridani panelu
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(productsPanel), BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        refreshRecipes();
    }

    /**
     * HORNI CAST
     */
    private void createTopPanel() {
        topPanel = new JPanel();
        addProductButton = new JButton("Přidat produkt");
        addBillButton = new JButton("Přidat objednávku");
        cancelBillButton = new JButton("Zrušit objednávku");
        showReceiptButton = new JButton("Zobrazit účtenku");

        topPanel.add(addProductButton);
        topPanel.add(addBillButton);
        topPanel.add(cancelBillButton);
        topPanel.add(showReceiptButton);

        // Akce pro tlacitka
        addProductButton.addActionListener(e -> addNewProduct());
        addBillButton.addActionListener(e -> addNewReceipt());
        cancelBillButton.addActionListener(e -> cancelNewReceipt());
        showReceiptButton.addActionListener(e -> {
            String selectedReceipt = receiptList.getSelectedValue();
            if (selectedReceipt != null) {
                showReceiptDetails(selectedReceipt);
            } else {
                JOptionPane.showMessageDialog(this, "Žádná účtenka není vybrána!", "Chyba",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * TLACITKA LISTENERY
     */
    /**
     * Tlacitko pridat produkt
     */
    private void addNewProduct() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] message = {
                "Název produktu:", nameField,
                "Cena produktu:", priceField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Přidat nový produkt", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                short price = Short.parseShort(priceField.getText());

                if (name.isEmpty() || price <= 0 || name.length() > 45)
                    throw new IllegalArgumentException("Název nesmí být prázdný a cena musí být kladná!");

                Product newProduct = new Product();
                newProduct.setName(name);
                newProduct.setPrice(price);
                products.add(newProduct);
                pc.write(newProduct);
                loadProducts();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cena musí být číslo!", "Chyba", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Chyba při ukládání produktu: " + e.getMessage(), "Chyba",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * tlacitko pridat objednavku
     */
    private void addNewReceipt() {
        ProductFile mach;
        ReceiptGenerator sebestova;
        try {
            mach = new ProductFile(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts/receipt" + i + ".dat");
            sebestova = new ReceiptGenerator(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/printedReceipts/prettyReceipt" + i + ".txt");
            i++;
            if (i >= 10)
                i = 0;

            mach.clear();
            if (cartItems.isEmpty())
                JOptionPane.showMessageDialog(this, "Nic neni v kosiku", "Chyba",
                        JOptionPane.ERROR_MESSAGE);

            for (Product product : cartItems) {
                mach.save(product);
            }
            sebestova.generateReceipt(cartItems);
            cartItems.clear();
            updateCartDisplay();
            mach.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání souboru produktů: " + e.getMessage(), "Chyba",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        refreshRecipes();
    }

    /**
     * tlacitko zrusit objednavku
     */
    private void cancelNewReceipt() {
        cartItems.clear();
        updateCartDisplay();
    }

    /**
     * tlacitko ukazat uctenku
     */
    private void showReceiptDetails(String receiptName) {
        try {
            ProductFile rf = new ProductFile(
                    "/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts/" + receiptName);
            List<Product> receiptProducts = rf.getAll();
            int ultimatePrice = 0;
            JDialog receiptDialog = new JDialog(this, "Detail " + receiptName, true);
            receiptDialog.setSize(400, 300);
            receiptDialog.setLayout(new BorderLayout());
            receiptDialog.setLocationRelativeTo(this);

            JPanel receiptPanel = new JPanel();
            receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));

            for (Product product : receiptProducts) {
                JPanel productPanel = new JPanel();
                productPanel.setLayout(new BorderLayout());

                JLabel productNameLabel = new JLabel(product.getName());
                productNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
                JLabel productPriceLabel = new JLabel(product.getPrice() + " Kč");
                productPriceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                productPanel.add(productNameLabel, BorderLayout.WEST);
                productPanel.add(productPriceLabel, BorderLayout.EAST);

                receiptPanel.add(productPanel);
                ultimatePrice += product.getPrice();
            }
            JPanel productPanel = new JPanel();
            productPanel.setLayout(new BorderLayout());
            JLabel productNameLabel = new JLabel("Celková cena");
            JLabel productPriceLabel = new JLabel(String.valueOf(ultimatePrice) + " Kč");
            productPanel.add(productNameLabel, BorderLayout.WEST);
            productPanel.add(productPriceLabel, BorderLayout.EAST);

            receiptPanel.add(productPanel);

            JScrollPane receiptScrollPane = new JScrollPane(receiptPanel);
            receiptDialog.add(receiptScrollPane, BorderLayout.CENTER);

            JButton closeButton = new JButton("Zavřít");
            closeButton.addActionListener(e -> receiptDialog.dispose());
            receiptDialog.add(closeButton, BorderLayout.SOUTH);

            receiptDialog.setVisible(true);
            rf.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání souboru produktů: " + e.getMessage(), "Chyba",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * LEVA CAST
     */
    private void createProductsPanel() {
        productsPanel = new JPanel();
        productsPanel.setLayout(new GridLayout(0, 5, 10, 10)); // Mřížka s 4 sloupci
        productsPanel.setBackground(Color.CYAN);
        loadProducts();
    }

    /**
     * Načte produkty z ProductFile a zobrazí je
     */
    private void loadProducts() {
        productsPanel.removeAll();
        productsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Vodorovné uspořádání s mezerami

        for (Product product : products) {
            String buttonText = String.format("<html>%s<br>%d Kč</html>", product.getName(), product.getPrice());

            // Vytvoření tlačítka s pevnými rozměry
            JButton productButton = new JButton(buttonText);
            productButton.setPreferredSize(new Dimension(120, 120)); // Pevná šířka a výška tlačítek
            productButton.addActionListener(e -> addToCart(product));

            productsPanel.add(productButton); // Přidání tlačítka do panelu
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void addToCart(Product product) {
        cartItems.add(product);

        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartPanel.removeAll();

        for (Product item : cartItems) {
            JLabel cartItemLabel = new JLabel(item.getName() + " " + item.getPrice() + " Kč");
            cartPanel.add(cartItemLabel);
        }

        cartPanel.revalidate();
        cartPanel.repaint();
    }

    /**
     * PRAVA CAST
     */

    private void createRightPanel() {
        // Kosik
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(Color.RED);

        JScrollPane cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        cartScrollPane.setPreferredSize(new Dimension(200, 300));

        // Uctenky
        receiptList = new JList<>(new String[10]);
        receiptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptList.setBackground(Color.magenta);

        // Inicializace
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, getHeight() / 2));
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(cartScrollPane, BorderLayout.NORTH);
        rightPanel.add(receiptList, BorderLayout.SOUTH);
    }

    private void refreshRecipes() {
        File receiptsDir = new File("/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts");
        File[] files = receiptsDir.listFiles((dir, name) -> name.startsWith("receipt") && name.endsWith(".dat"));

        List<String> receiptNames = new ArrayList<>();

        for (File file : files) {
            receiptNames.add(file.getName());
        }

        receiptNames.sort(String::compareTo);

        receiptList.setListData(receiptNames.toArray(new String[0]));
    }

    /**
     * Spustí aplikaci
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
