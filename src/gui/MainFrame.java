package gui;

import javax.swing.*;
import data.Product;
import fileManagers.ProductFile;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private int i = 0;
    private ProductFile pf;
    private List<Product> products;
    private ArrayList<String> cartItems = new ArrayList<>();
    private JPanel topPanel, productsPanel, rightPanel, cartPanel;
    private JButton addProductButton, addBillButton, cancelBillButton, showReceiptButton;
    private JList<String> receiptList;

    public MainFrame() {
        // Inicializace správce souborů
        try {
            pf = new ProductFile("/home/patrik/javaprograms/pokladna/pokladna/src/files/products.dat");
            products = pf.getAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání souboru produktů: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Nastavení hlavního okna
        setTitle("Pokladní systém");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Horní panel s tlačítky
        createTopPanel();

        // Levý panel s produkty
        createProductsPanel();

        // Pravý panel s košíkem a seznamem účtenek
        createRightPanel();

        // Přidání panelů do hlavního okna
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(productsPanel), BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Vytvoří horní panel s tlačítky
     */
    private void createTopPanel() {
        topPanel = new JPanel();
        addProductButton = new JButton("Přidat produkt");
        addBillButton = new JButton("Přidat objednávku");
        cancelBillButton = new JButton("Zrušit");
        showReceiptButton = new JButton("Zobraz účtenku");

        topPanel.add(addProductButton);
        topPanel.add(addBillButton);
        topPanel.add(cancelBillButton);
        topPanel.add(showReceiptButton);

        // Akce pro tlačítko "Přidat produkt"
        addProductButton.addActionListener(e -> addNewProduct());
        addBillButton.addActionListener(e -> addNewReceipt());

        // Akce pro tlačítko "Zobraz účtenku"
        showReceiptButton.addActionListener(e -> {
            String selectedReceipt = receiptList.getSelectedValue();
            if (selectedReceipt != null) {
                showReceiptDetails(selectedReceipt);
            } else {
                JOptionPane.showMessageDialog(this, "Žádná účtenka není vybrána!", "Chyba", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Vytvoří panel s produkty
     */
    private void createProductsPanel() {
        productsPanel = new JPanel();
        productsPanel.setLayout(new GridLayout(0, 4, 10, 10)); // Mřížka s 4 sloupci
        productsPanel.setBackground(Color.CYAN);
        loadProducts();
    }

    /**
     * Vytvoří pravý panel s košíkem a seznamem účtenek
     */
    private void createRightPanel() {
        // Panel košíku
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(Color.RED);

        JScrollPane cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        cartScrollPane.setPreferredSize(new Dimension(400, 200));

        // Seznam účtenek
        receiptList = new JList<>(new String[10]); // Simulace 10 účtenek
        receiptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptList.setBackground(Color.magenta);

        JScrollPane receiptScrollPane = new JScrollPane(receiptList);
        receiptScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        receiptScrollPane.setPreferredSize(new Dimension(400, 200));

        // Pravý panel obsahující košík a účtenky
        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(cartScrollPane, BorderLayout.NORTH);
        rightPanel.add(receiptScrollPane, BorderLayout.SOUTH);
    }

    /**
     * Načte produkty z ProductFile a zobrazí je
     */
    private void loadProducts() {
        productsPanel.removeAll();

        for (Product product : products) {
            JButton productButton = new JButton(product.getName() + " (" + product.getPrice() + " Kč)");
            productButton.setPreferredSize(new Dimension(150, 100));
            productButton.addActionListener(e -> {
                addToCart(product); // Zavolá metodu pro přidání do košíku
            });
            productsPanel.add(productButton);
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void addToCart(Product product) {
        // Přidá název produktu do seznamu položek košíku
        cartItems.add(product.getName() + " (" + product.getPrice() + " Kč)");
    
        // Aktualizuje zobrazení košíku
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartPanel.removeAll(); // Smaže všechny existující položky z košíku
    
        // Projde seznam položek v košíku a přidá je do panelu
        for (String item : cartItems) {
            JLabel cartItemLabel = new JLabel(item);
            cartPanel.add(cartItemLabel);
        }
    
        // Obnoví rozložení panelu košíku
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    /**
     * Přidá nový produkt pomocí dialogového okna
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

                if (name.isEmpty() || price <= 0) {
                    throw new IllegalArgumentException("Název nesmí být prázdný a cena musí být kladná!");
                }

                Product newProduct = new Product();
                newProduct.setName(name);
                newProduct.setPrice(price);
                products.add(newProduct);
                pf.save(newProduct); // Uloží produkt do souboru
                loadProducts(); // Aktualizuje zobrazení
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cena musí být číslo!", "Chyba", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Chyba při ukládání produktu: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addNewReceipt(){
        ProductFile pepik;
        try {
            pepik = new ProductFile("/home/patrik/javaprograms/pokladna/pokladna/src/files/receipts/receipt0.dat");
            products = pepik.getAll();
            for (Product product : products) {
                pepik.save(product);
            }
            pepik.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba při načítání souboru produktů: " + e.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }        
        
    }

    /**
     * Zobrazí detaily vybrané účtenky v dialogovém okně
     */
    private void showReceiptDetails(String receiptName) {
        JDialog receiptDialog = new JDialog(this, "Detail " + receiptName, true);
        receiptDialog.setSize(400, 300);
        receiptDialog.setLayout(new BorderLayout());
        receiptDialog.setLocationRelativeTo(this);

        JTextArea receiptDetails = new JTextArea();
        receiptDetails.setText("Detaily " + receiptName + ":\n\n- Produkt A\n- Produkt B\n- Produkt C");
        receiptDetails.setEditable(false);

        receiptDialog.add(new JScrollPane(receiptDetails), BorderLayout.CENTER);

        JButton closeButton = new JButton("Zavřít");
        closeButton.addActionListener(e -> receiptDialog.dispose());
        receiptDialog.add(closeButton, BorderLayout.SOUTH);

        receiptDialog.setVisible(true);
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
