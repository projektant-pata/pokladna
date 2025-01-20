package gui;

import javax.swing.*;
import javax.swing.border.Border;

import data.Core;
import data.Product;
import data.SourceException;
import fileManagers.ProductFile;
import fileManagers.ProductsCSV;
import fileManagers.ReceiptGenerator;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MainFrame extends JFrame {
    private Core core;
    private JPanel topPanel, productsPanel, rightPanel, cartPanel;
    private JButton addProductButton, addBillButton, cancelBillButton, showReceiptButton;
    private JList<String> receiptList;
    // dysajn
    Border border = BorderFactory.createLineBorder(Color.BLUE, 2);

    public MainFrame() {
        core = new Core();
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

        loadProducts();
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
                core.addProduct(newProduct);
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
    try {
        core.saveReceipt();
        refreshRecipes();
        updateCartDisplay();
    } catch (SourceException e) {
        JOptionPane.showMessageDialog(this, "Chyba při ukládání účtenky: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
    }
}

    /**
     * tlacitko zrusit objednavku
     */
    private void cancelNewReceipt() {
        core.clearCart();
        updateCartDisplay();
    }

    /**
     * tlacitko ukazat uctenku
     */
    private void showReceiptDetails(String receiptName) {
        try {
            List<Product> receiptProducts = core.showReceipt(receiptName);
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
        List<Product> products = core.getProducts();

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
        core.addCartItem(product);

        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartPanel.removeAll();
        List<Product> cartItems = core.getCartItems();

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
        try {
            receiptList.setListData(core.getReceipts().toArray(new String[0]));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání účtenek: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
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
