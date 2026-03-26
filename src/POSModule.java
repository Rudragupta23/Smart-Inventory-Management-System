import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class POSModule {
    private InventoryDatabase db;
    private JPanel mainPanel;
    private DefaultTableModel catalogModel;
    private DefaultTableModel cartModel;
    private JTable catalogTable;
    private JTable cartTable;
    private JLabel totalLabel;
    
    // Promoted variables for Bulletproof Dark Mode access
    private JTextField searchField; 
    private JPanel checkoutContainer;
    private JScrollPane catalogScroll; 
    private JScrollPane cartScroll;    
    
    private double currentTotal = 0.0;
    private Runnable onCheckoutSuccess;

    // Premium UI Theme Colors
    private final Color PRIMARY_BG = new Color(245, 247, 250);
    private final Color PANEL_BG = Color.WHITE;
    private final Color HEADER_COLOR = new Color(41, 128, 185);
    private final Color PURPLE_ACCENT = new Color(142, 68, 173);
    private final Color DARK_REGISTER_BG = new Color(44, 62, 80); 
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public POSModule(InventoryDatabase db, Runnable onCheckoutSuccess) {
        this.db = db;
        this.onCheckoutSuccess = onCheckoutSuccess;
        initializeUI();
        refreshCatalog();
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // LEFT SIDE: Product Catalog Card
        JPanel leftCard = createPremiumCard();
        leftCard.setLayout(new BorderLayout(15, 15));

        JPanel leftHeaderPanel = new JPanel(new BorderLayout(10, 15));
        leftHeaderPanel.setOpaque(false);
        
        JLabel catalogTitle = new JLabel("Product Catalog");
        catalogTitle.setFont(TITLE_FONT);
        catalogTitle.setForeground(HEADER_COLOR);
        leftHeaderPanel.add(catalogTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchIcon = new JLabel("Find:  ");
        searchIcon.setFont(BOLD_FONT);
        searchIcon.setForeground(Color.DARK_GRAY);
        
        searchField = new JTextField(15);
        searchField.setFont(MAIN_FONT);
        searchField.setPreferredSize(new Dimension(150, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton searchBtn = createFlatButton("Search", HEADER_COLOR, Color.WHITE);
        searchBtn.setPreferredSize(new Dimension(90, 40));
        
        JButton resetBtn = createFlatButton("Clear", new Color(149, 165, 166), Color.WHITE);
        resetBtn.setPreferredSize(new Dimension(80, 40));
        
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchIcon);
        searchWrapper.add(searchField);
        searchWrapper.add(searchBtn);
        searchWrapper.add(resetBtn);
        leftHeaderPanel.add(searchWrapper, BorderLayout.CENTER);
        
        leftCard.add(leftHeaderPanel, BorderLayout.NORTH);

        String[] catCols = {"ID", "Name", "Price (₹)", "Stock"};
        catalogModel = new DefaultTableModel(catCols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        catalogTable = new JTable(catalogModel);
        styleTable(catalogTable, HEADER_COLOR);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(catalogModel);
        catalogTable.setRowSorter(sorter);
        
        // FIXED: Using class-level variable
        catalogScroll = new JScrollPane(catalogTable);
        catalogScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        catalogScroll.getViewport().setBackground(Color.WHITE);
        leftCard.add(catalogScroll, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addPanel.setOpaque(false);
        addPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton addToCartBtn = createFlatButton("Add to Order  +", SUCCESS_GREEN, Color.WHITE);
        addToCartBtn.setPreferredSize(new Dimension(180, 45));
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addPanel.add(addToCartBtn);
        leftCard.add(addPanel, BorderLayout.SOUTH);

        // RIGHT SIDE: Active Order Cart Card
        JPanel rightCard = createPremiumCard();
        rightCard.setLayout(new BorderLayout(15, 15));

        JPanel rightHeaderPanel = new JPanel(new BorderLayout());
        rightHeaderPanel.setOpaque(false);
        rightHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel cartTitle = new JLabel("Active Register");
        cartTitle.setFont(TITLE_FONT);
        cartTitle.setForeground(PURPLE_ACCENT);
        rightHeaderPanel.add(cartTitle, BorderLayout.WEST);
        
        JButton removeBtn = createFlatButton("Remove Item", new Color(231, 76, 60), Color.WHITE);
        removeBtn.setPreferredSize(new Dimension(130, 35));
        rightHeaderPanel.add(removeBtn, BorderLayout.EAST);
        
        rightCard.add(rightHeaderPanel, BorderLayout.NORTH);

        String[] cartCols = {"ID", "Item Name", "Qty", "Unit Price", "Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartModel);
        styleTable(cartTable, PURPLE_ACCENT);
        
        // FIXED: Using class-level variable
        cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        cartScroll.getViewport().setBackground(Color.WHITE);
        rightCard.add(cartScroll, BorderLayout.CENTER);

        checkoutContainer = new JPanel(new BorderLayout());
        checkoutContainer.setBackground(DARK_REGISTER_BG);
        checkoutContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(34, 49, 63), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JPanel textWrapper = new JPanel(new GridLayout(2, 1, 0, 5));
        textWrapper.setOpaque(false);
        
        JLabel totalTextLbl = new JLabel("AMOUNT DUE");
        totalTextLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalTextLbl.setForeground(new Color(189, 195, 199)); 
        
        totalLabel = new JLabel("₹ 0.00", SwingConstants.LEFT);
        totalLabel.setFont(new Font("Consolas", Font.BOLD, 38)); 
        totalLabel.setForeground(SUCCESS_GREEN);
        
        textWrapper.add(totalTextLbl);
        textWrapper.add(totalLabel);

        JButton checkoutBtn = createFlatButton("CHECKOUT ->", new Color(241, 196, 15), Color.BLACK);
        checkoutBtn.setPreferredSize(new Dimension(200, 60));
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        checkoutContainer.add(textWrapper, BorderLayout.CENTER);
        checkoutContainer.add(checkoutBtn, BorderLayout.EAST);
        
        rightCard.add(checkoutContainer, BorderLayout.SOUTH);

        // MAIN LAYOUT SPLIT
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightCard);
        splitPane.setDividerLocation(560); 
        splitPane.setDividerSize(12);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    public void paint(Graphics g) { 
                        g.setColor(PRIMARY_BG); 
                        g.fillRect(0, 0, getSize().width, getSize().height); 
                    }
                };
            }
        });

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // ACTION LISTENERS
        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim().toLowerCase();
            if (q.isEmpty()) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + q));
        });

        resetBtn.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
        });

        addToCartBtn.addActionListener(e -> {
            int row = catalogTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(mainPanel, "Select a product from the Catalog first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = catalogTable.convertRowIndexToModel(row);
            String id = catalogModel.getValueAt(modelRow, 0).toString();
            String name = catalogModel.getValueAt(modelRow, 1).toString();
            double price = Double.parseDouble(catalogModel.getValueAt(modelRow, 2).toString());
            int availableStock = Integer.parseInt(catalogModel.getValueAt(modelRow, 3).toString());

            String qtyStr = JOptionPane.showInputDialog(mainPanel, "Enter quantity for " + name + ":", "1");
            if (qtyStr != null && qtyStr.matches("\\d+")) {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) return;
                if (qty > availableStock) {
                    JOptionPane.showMessageDialog(mainPanel, "Insufficient stock! Only " + availableStock + " available.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    if (cartModel.getValueAt(i, 0).equals(id)) {
                        int existingQty = (int) cartModel.getValueAt(i, 2);
                        if (existingQty + qty > availableStock) {
                            JOptionPane.showMessageDialog(mainPanel, "Cannot add more. Exceeds available stock.");
                            return;
                        }
                        cartModel.setValueAt(existingQty + qty, i, 2);
                        cartModel.setValueAt((existingQty + qty) * price, i, 4);
                        updateTotal();
                        return;
                    }
                }
                cartModel.addRow(new Object[]{id, name, qty, price, (qty * price)});
                updateTotal();
            }
        });

        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cartModel.removeRow(row);
                updateTotal();
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Select an item in the Active Register to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        checkoutBtn.addActionListener(e -> processCheckout());
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "The active register is empty!", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(mainPanel, 
            "Complete transaction for ₹" + String.format("%.2f", currentTotal) + "?", 
            "Confirm Checkout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            StringBuilder receipt = new StringBuilder();
            String txId = "TXN-" + System.currentTimeMillis();
            
            receipt.append("=========================================\n");
            receipt.append("         SMART INVENTORY - POS           \n");
            receipt.append("           OFFICIAL RECEIPT              \n");
            receipt.append("=========================================\n");
            receipt.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            receipt.append("Transaction ID: ").append(txId).append("\n");
            receipt.append("-----------------------------------------\n");
            receipt.append(String.format("%-15s %-5s %s\n", "Item", "Qty", "Subtotal"));
            receipt.append("-----------------------------------------\n");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String id = cartModel.getValueAt(i, 0).toString();
                String name = cartModel.getValueAt(i, 1).toString();
                int qty = (int) cartModel.getValueAt(i, 2);
                double subtotal = (double) cartModel.getValueAt(i, 4);

                for (Product p : db.getAllProducts()) {
                    if (p.getId().equals(id)) {
                        p.setQuantity(p.getQuantity() - qty);
                        break;
                    }
                }
                
                String shortName = name.length() > 14 ? name.substring(0, 12) + ".." : name;
                receipt.append(String.format("%-15s %-5d ₹%.2f\n", shortName, qty, subtotal));
            }

            receipt.append("-----------------------------------------\n");
            receipt.append(String.format("TOTAL AMOUNT DUE:       ₹%.2f\n", currentTotal));
            receipt.append("=========================================\n");
            receipt.append("      Thank you for your business!       \n");

            try {
                // File receiptFile = new File("Receipt_" + txId + ".txt");
                String user = db.getLoggedInUser();
                File userDir = new File("user_data_" + user);
                if (!userDir.exists()) userDir.mkdirs();
    
                File receiptFile = new File(userDir, "Receipt_" + txId + ".txt");
                PrintWriter out = new PrintWriter(receiptFile);
                out.print(receipt.toString());
                out.close();

                db.logActivity("POS Checkout Completed. TXN: " + txId + " | Total: ₹" + String.format("%.2f", currentTotal));
                db.saveToFile(); 

                JOptionPane.showMessageDialog(mainPanel, "Checkout Successful!\nReceipt generated: " + receiptFile.getName());

                cartModel.setRowCount(0);
                updateTotal();
                refreshCatalog();
                
                if (onCheckoutSuccess != null) onCheckoutSuccess.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Checkout completed, but failed to save receipt file.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshCatalog() {
        catalogModel.setRowCount(0);
        for (Product p : db.getAllProducts()) {
            catalogModel.addRow(new Object[]{p.getId(), p.getName(), p.getPrice(), p.getQuantity()});
        }
    }

    private void updateTotal() {
        currentTotal = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            currentTotal += (double) cartModel.getValueAt(i, 4);
        }
        totalLabel.setText("₹ " + String.format("%.2f", currentTotal));
    }

    // DARK MODE METHOD 
    public void setDarkMode(boolean isDark) {
        SwingUtilities.invokeLater(() -> {
            Color fieldBg = isDark ? new Color(60, 63, 65) : Color.WHITE;
            Color textColor = isDark ? new Color(220, 220, 220) : Color.BLACK;
            Color borderColor = isDark ? new Color(80, 80, 80) : new Color(220, 220, 220);
            
            Color selectionBg = isDark ? new Color(75, 110, 150) : new Color(228, 241, 254);
            Color selectionFg = isDark ? Color.WHITE : Color.BLACK;
            
            // 1. Update Catalog Table & Viewport directly
            catalogTable.setBackground(fieldBg);
            catalogTable.setForeground(textColor);
            catalogTable.setGridColor(borderColor);
            catalogTable.setSelectionBackground(selectionBg); 
            catalogTable.setSelectionForeground(selectionFg);
            catalogTable.getTableHeader().setBackground(isDark ? new Color(60, 63, 65) : HEADER_COLOR);
            if(catalogScroll != null) {
                catalogScroll.getViewport().setBackground(fieldBg);
                catalogScroll.setBackground(fieldBg);
            }
            
            // 2. Update Cart Table & Viewport directly
            cartTable.setBackground(fieldBg);
            cartTable.setForeground(textColor);
            cartTable.setGridColor(borderColor);
            cartTable.setSelectionBackground(selectionBg); 
            cartTable.setSelectionForeground(selectionFg);
            cartTable.getTableHeader().setBackground(isDark ? new Color(60, 63, 65) : PURPLE_ACCENT);
            if(cartScroll != null) {
                cartScroll.getViewport().setBackground(fieldBg);
                cartScroll.setBackground(fieldBg);
            }

            // 3. Update Search Field
            searchField.setBackground(fieldBg);
            searchField.setForeground(textColor);
            searchField.setCaretColor(textColor);
            searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), 
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            // 4. Force Digital Register to stay protected
            checkoutContainer.setBackground(DARK_REGISTER_BG); 
            checkoutContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDark ? new Color(80, 80, 80) : new Color(34, 49, 63), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
            ));
            
            catalogTable.repaint();
            cartTable.repaint();
        });
    }

    private JPanel createPremiumCard() {
        JPanel p = new JPanel();
        p.setName("Card");
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        return p;
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleTable(JTable table, Color headerColor) {
        table.setFont(MAIN_FONT);
        table.setRowHeight(38); 
        table.setSelectionBackground(new Color(228, 241, 254)); 
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(headerColor);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 45)); 
        
        DefaultTableCellRenderer dynamicRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                
                l.setOpaque(true); 
                
                if (!isSelected) {
                    l.setBackground(t.getBackground());
                    l.setForeground(t.getForeground());
                } else {
                    l.setBackground(t.getSelectionBackground());
                    l.setForeground(t.getSelectionForeground());
                }
                return l;
            }
        };
        
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(dynamicRenderer);
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}