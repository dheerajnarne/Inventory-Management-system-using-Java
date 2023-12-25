import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Product {
    String name;
    int id;
    int quantity;
    float price;

    Product(int id, String name, int quantity, float price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public int buy(int add) {
        return quantity += add;
    }

    public int sell(int sub) {
        if (quantity >= sub) {
            return quantity -= sub;
        } else {
            JOptionPane.showMessageDialog(null, "Not enough quantity in stock.");
            return quantity;
        }
    }

    public String[] toArray() {
        return new String[]{String.valueOf(id), name, String.valueOf(quantity), String.valueOf(price)};
    }
}

public class InventoryManagementSystem {
    private JFrame frame;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private ArrayList<Product> products;
    private ArrayList<Product> sales; // List to store sold products
    private ArrayList<String> users;
    private String currentUser;
    private boolean isAdmin;

    public InventoryManagementSystem() {
        frame = new JFrame("Complex Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);

        products = new ArrayList<>();
        sales = new ArrayList<>(); // Initialize the sales list
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Quantity", "Price"}, 0);
        inventoryTable = new JTable(tableModel);

        JButton loadButton = new JButton("Load Inventory");
        JButton saveButton = new JButton("Save Inventory");
        JButton addButton = new JButton("Add Product");
        JButton sellButton = new JButton("Sell Product");
        JButton buyButton = new JButton("Buy Product");
        JButton searchButton = new JButton("Search Product by ID");

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadInventory();
                updateTable();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveInventory();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });

        sellButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sellProduct();
            }
        });

        buyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buyProduct();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                    
                searchProductByID();
            }
        });

        users = new ArrayList<>();
        users.add("admin:admin"); // Add a default admin user (username:password)
        users.add("user:password"); // Add a default regular user
        currentUser = null;
        isAdmin = false;

        JButton updateButton = new JButton("Update Product Info");
        JButton deleteButton = new JButton("Delete Product");

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isAdmin) {
                    updateProductInfo();
                } else {
                    JOptionPane.showMessageDialog(frame, "Only admins can update product information.");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isAdmin) {
                    deleteProduct();
                } else {
                    JOptionPane.showMessageDialog(frame, "Only admins can delete products.");
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(addButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(buyButton);
        buttonPanel.add(searchButton);  
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        frame.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Product product : products) {
            tableModel.addRow(product.toArray());
        }
    }

    private void saveInventory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("inventory.txt"))) {
            for (Product product : products) {
                writer.println(product.id);
                writer.println(product.name);
                writer.println(product.quantity);
                writer.println(product.price);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadInventory() {
        products.clear();
        tableModel.setRowCount(0);

        try (BufferedReader reader = new BufferedReader(new FileReader("inventory.txt"))) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                int id = Integer.parseInt(line);
                String name = reader.readLine();
                int quantity = Integer.parseInt(reader.readLine());
                float price = Float.parseFloat(reader.readLine());
                Product product = new Product(id, name, quantity, price);
                products.add(product);
                tableModel.addRow(product.toArray());
                index++;
            }
        } catch (IOException | NumberFormatException e) {
            // Handle exceptions if the file doesn't exist or is in an incorrect format
        }
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog(frame, "Enter the name of the product:");
        if (name != null && !name.trim().isEmpty()) {
            int id = products.size() + 1;
            int quantity = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter the quantity:"));
            float price = Float.parseFloat(JOptionPane.showInputDialog(frame, "Enter the price:"));
            Product product = new Product(id, name, quantity, price);
            products.add(product);
            tableModel.addRow(product.toArray());
        }
    }

    private void sellProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            int quantity = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter the quantity to sell:"));
            Product product = products.get(selectedRow);
            if (product.sell(quantity) > 0) {
                sales.add(new Product(product.id, product.name, 1, product.price)); // Add sold item to sales list
                tableModel.setValueAt(product.quantity, selectedRow, 2);
            }
        }
    }

    private void buyProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            int quantity = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter the quantity to buy:"));
            Product product = products.get(selectedRow);
            product.buy(quantity);
            tableModel.setValueAt(product.quantity, selectedRow, 2);
        }
    }

    private void searchProductByID() {
        String idString = JOptionPane.showInputDialog(frame, "Enter the ID of the product:");

        if (idString != null && !idString.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idString);

                for (Product product : products) {
                    if (product.id == id) {
                        JOptionPane.showMessageDialog(frame, "Product Found:\n" +
                                "ID: " + product.id + "\n" +
                                "Name: " + product.name + "\n" +
                                "Quantity: " + product.quantity + "\n" +
                                "Price: " + product.price);
                        return;
                    }
                }

                JOptionPane.showMessageDialog(frame, "Product with ID " + id + " not found.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid ID format. Please enter a valid integer ID.");
            }
        }
    }

    private void updateProductInfo() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            String newName = JOptionPane.showInputDialog(frame, "Enter the new name of the product:");
            String newPrice = JOptionPane.showInputDialog(frame, "Enter the new price:");
            Product product = products.get(selectedRow);

            if (newName != null && !newName.trim().isEmpty()) {
                product.name = newName;
                tableModel.setValueAt(newName, selectedRow, 1);
            }

            try {
                float newProductPrice = Float.parseFloat(newPrice);
                product.price = newProductPrice;
                tableModel.setValueAt(newProductPrice, selectedRow, 3);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid price format. Please enter a valid price.");
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            Product product = products.get(selectedRow);
            products.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(frame, "Product deleted successfully.");
        }
    }

    public void display() {
        if (currentUser == null) {
            // Request user login
            String userCredentials = JOptionPane.showInputDialog(frame, "Enter your credentials (username:password):");
            if (userCredentials != null && users.contains(userCredentials)) {
                currentUser = userCredentials.split(":")[0];
                if (currentUser.equals("admin")) {
                    isAdmin = true;
                }
                frame.setTitle("Complex Inventory Management System - User: " + currentUser);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials. Please try again.");
            }
        }
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                InventoryManagementSystem ims = new InventoryManagementSystem();
                ims.display();
            }
        });
    }
}
