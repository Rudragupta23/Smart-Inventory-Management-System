import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class MainApp extends JFrame {
    private InventoryDatabase db;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private AnalyticsChart chartPanel; 
    private LineChart lineChartPanel;
    private PieChart pieChartPanel;
    private POSModule posPanel; 
    
    // UI Theme Colors
    private Color PRIMARY_BG = new Color(245, 247, 250);
    private Color PANEL_BG = Color.WHITE;
    private Color TEXT_COLOR = Color.BLACK;
    private final Color HEADER_COLOR = new Color(41, 128, 185);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private JLabel totalItemsLabel, totalValueLabel, lowStockLabel;
    private JTextArea logArea;
    private JTabbedPane tabbedPane;
    private boolean isDarkMode = false; 

    // Session State Variables
    private String generatedResetCode = "";
    private String userResetting = "";
    private String loggedInUser = ""; 

    public MainApp() {
        db = new InventoryDatabase();
        showLoginScreen(); 
    }

    // CALENDAR 
    class CalendarDialog extends JDialog {
        private String selectedDate = "";
        private LocalDate currentDisplayDate = LocalDate.now();

        public CalendarDialog(JFrame parent) {
            super(parent, "Select Target Date", true);
            setSize(320, 320);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(HEADER_COLOR);
            JButton prevBtn = styleButton("<", HEADER_COLOR, Color.WHITE);
            JButton nextBtn = styleButton(">", HEADER_COLOR, Color.WHITE);
            JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
            monthYearLabel.setFont(BOLD_FONT);
            monthYearLabel.setForeground(Color.WHITE);
            
            topPanel.add(prevBtn, BorderLayout.WEST);
            topPanel.add(monthYearLabel, BorderLayout.CENTER);
            topPanel.add(nextBtn, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new GridLayout(7, 7));
            centerPanel.setBackground(PANEL_BG);
            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String d : days) {
                JLabel dayLbl = new JLabel(d, SwingConstants.CENTER);
                dayLbl.setForeground(TEXT_COLOR);
                centerPanel.add(dayLbl);
            }

            JButton[] dayBtns = new JButton[42];
            for (int i = 0; i < 42; i++) {
                dayBtns[i] = new JButton();
                dayBtns[i].setMargin(new Insets(2, 2, 2, 2));
                dayBtns[i].setFocusPainted(false);
                centerPanel.add(dayBtns[i]);
            }
            add(centerPanel, BorderLayout.CENTER);

            Runnable updateCalendar = () -> {
                monthYearLabel.setText(currentDisplayDate.getMonth() + " " + currentDisplayDate.getYear());
                LocalDate firstOfMonth = currentDisplayDate.withDayOfMonth(1);
                int startDay = firstOfMonth.getDayOfWeek().getValue() % 7; 
                int daysInMonth = currentDisplayDate.lengthOfMonth();

                for (int i = 0; i < 42; i++) {
                    for (java.awt.event.ActionListener al : dayBtns[i].getActionListeners()) {
                        dayBtns[i].removeActionListener(al);
                    }
                    if (i >= startDay && i < startDay + daysInMonth) {
                        int day = i - startDay + 1;
                        dayBtns[i].setText(String.valueOf(day));
                        
                        LocalDate thisDate = currentDisplayDate.withDayOfMonth(day);
                        
                        if (thisDate.isBefore(LocalDate.now())) {
                            dayBtns[i].setEnabled(false);
                            dayBtns[i].setBackground(isDarkMode ? new Color(60,60,60) : new Color(220, 220, 220));
                            dayBtns[i].setForeground(Color.GRAY);
                        } else {
                            dayBtns[i].setEnabled(true);
                            dayBtns[i].setBackground(PANEL_BG);
                            dayBtns[i].setForeground(TEXT_COLOR);
                            dayBtns[i].addActionListener(e -> {
                                selectedDate = thisDate.toString();
                                dispose();
                            });
                        }
                    } else {
                        dayBtns[i].setText("");
                        dayBtns[i].setEnabled(false);
                        dayBtns[i].setBackground(PANEL_BG);
                    }
                }
            };

            prevBtn.addActionListener(e -> { currentDisplayDate = currentDisplayDate.minusMonths(1); updateCalendar.run(); });
            nextBtn.addActionListener(e -> { currentDisplayDate = currentDisplayDate.plusMonths(1); updateCalendar.run(); });

            updateCalendar.run();
        }

        public String getPickedDate() { return selectedDate; }
    }

    // AUTHENTICATION ENGINE 
    private void showLoginScreen() {
        JFrame authFrame = new JFrame("Smart Inventory - Authentication");
        authFrame.setSize(950, 650); 
        authFrame.setLocationRelativeTo(null);
        authFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        
        // LEFT PANEL: BEAUTIFUL BRANDING
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(HEADER_COLOR); // Deep blue background
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0; gbcLeft.gridy = 0; gbcLeft.anchor = GridBagConstraints.CENTER;
        
        // 1. Create a transparent wrapper panel for the text monogram
        JPanel monogramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        monogramPanel.setOpaque(false);
        
        // 2. The "S" label (Bright white, large)
        JLabel labelS = new JLabel("S");
        labelS.setFont(new Font("Segoe UI", Font.BOLD, 120));
        labelS.setForeground(Color.WHITE);
        
        // 3. The "I" label (Slightly smaller, yellow/gold accent)
        JLabel labelI = new JLabel("i");
        labelI.setFont(new Font("Segoe UI", Font.BOLD, 120)); 
        labelI.setForeground(new Color(241, 196, 15)); 
        labelI.setBorder(BorderFactory.createEmptyBorder(0, -10, 0, 0)); 
        
        // 4. Assemble the monogram
        monogramPanel.add(labelS);
        monogramPanel.add(labelI);
        
        // Add monogram to branding panel
        gbcLeft.insets = new Insets(0, 0, 10, 0); 
        leftPanel.add(monogramPanel, gbcLeft);
        
        // Brand Title 
        gbcLeft.gridy++; gbcLeft.insets = new Insets(10, 0, 0, 0); 
        JLabel brandTitle = new JLabel("Smart Inventory", SwingConstants.CENTER);
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 38));
        brandTitle.setForeground(Color.WHITE);
        leftPanel.add(brandTitle, gbcLeft);
        
        // Brand Subtitle 
        gbcLeft.gridy++; gbcLeft.insets = new Insets(0, 0, 0, 0); 
        JLabel brandSub = new JLabel("Enterprise Supply Chain System", SwingConstants.CENTER);
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        brandSub.setForeground(new Color(220, 240, 255));
        leftPanel.add(brandSub, gbcLeft);

        // RIGHT PANEL: AUTHENTICATION FORMS
        CardLayout cardLayout = new CardLayout();
        JPanel rightCardPanel = new JPanel(cardLayout);
        rightCardPanel.setBackground(Color.WHITE);
        rightCardPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60)); // Generous padding

        // Form styling helpers
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 32);
        Color titleColor = new Color(44, 62, 80);
        
        java.util.function.Supplier<JTextField> makeModernField = () -> {
            JTextField tf = new JTextField();
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            tf.setPreferredSize(new Dimension(300, 42));
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            return tf;
        };

        java.util.function.Supplier<JPasswordField> makeModernPassField = () -> {
            JPasswordField pf = new JPasswordField();
            pf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            pf.setPreferredSize(new Dimension(300, 42));
            pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
            return pf;
        };

        // 1. LOGIN CARD 
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcLog = new GridBagConstraints();
        gbcLog.fill = GridBagConstraints.HORIZONTAL; gbcLog.weightx = 1.0; gbcLog.gridx = 0; gbcLog.gridy = 0;
        
        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(titleFont); loginTitle.setForeground(titleColor);
        loginPanel.add(loginTitle, gbcLog);
        
        gbcLog.gridy++; gbcLog.insets = new Insets(5, 0, 35, 0);
        JLabel loginSub = new JLabel("Please enter your details to sign in.");
        loginSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); loginSub.setForeground(Color.GRAY);
        loginPanel.add(loginSub, gbcLog);

        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 5, 0);
        JLabel uLbl = new JLabel("Username"); uLbl.setFont(labelFont); loginPanel.add(uLbl, gbcLog);
        
        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 15, 0);
        JTextField loginUserField = makeModernField.get(); loginPanel.add(loginUserField, gbcLog);

        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 5, 0);
        JLabel pLbl = new JLabel("Password"); pLbl.setFont(labelFont); loginPanel.add(pLbl, gbcLog);
        
        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 10, 0);
        JPasswordField loginPassField = makeModernPassField.get(); loginPanel.add(loginPassField, gbcLog);

        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 25, 0); gbcLog.anchor = GridBagConstraints.EAST;
        JButton forgotPwdBtn = new JButton("Forgot Password?");
        forgotPwdBtn.setContentAreaFilled(false); forgotPwdBtn.setBorderPainted(false); 
        forgotPwdBtn.setForeground(HEADER_COLOR); forgotPwdBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel forgotWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        forgotWrapper.setBackground(Color.WHITE); forgotWrapper.add(forgotPwdBtn);
        loginPanel.add(forgotWrapper, gbcLog);

        gbcLog.gridy++; gbcLog.insets = new Insets(0, 0, 15, 0); gbcLog.fill = GridBagConstraints.HORIZONTAL;
        JButton signInBtn = styleButton("Sign In", HEADER_COLOR, Color.WHITE);
        signInBtn.setPreferredSize(new Dimension(300, 45));
        loginPanel.add(signInBtn, gbcLog);

        gbcLog.gridy++;
        JButton goToSignUpBtn = styleButton("Create New Account", new Color(46, 204, 113), Color.WHITE);
        goToSignUpBtn.setPreferredSize(new Dimension(300, 45));
        loginPanel.add(goToSignUpBtn, gbcLog);

        gbcLog.gridy++; gbcLog.weighty = 1.0; loginPanel.add(Box.createVerticalGlue(), gbcLog);

        // 2. SIGN UP CARD 
        JPanel signupPanel = new JPanel(new GridBagLayout());
        signupPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcReg = new GridBagConstraints();
        gbcReg.fill = GridBagConstraints.HORIZONTAL; gbcReg.weightx = 1.0; gbcReg.gridx = 0; gbcReg.gridy = 0;

        JLabel signupTitle = new JLabel("Create Account");
        signupTitle.setFont(titleFont); signupTitle.setForeground(new Color(46, 204, 113));
        signupPanel.add(signupTitle, gbcReg);

        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 20, 0);
        JLabel signupSub = new JLabel("Sign up to start managing your inventory.");
        signupSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); signupSub.setForeground(Color.GRAY);
        signupPanel.add(signupSub, gbcReg);

        // Name
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 5, 0);
        JLabel fnLbl = new JLabel("Full Name"); fnLbl.setFont(labelFont); signupPanel.add(fnLbl, gbcReg);
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 10, 0);
        JTextField nameField = makeModernField.get(); signupPanel.add(nameField, gbcReg);

        // Email
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 5, 0);
        JLabel emLbl = new JLabel("Email Address"); emLbl.setFont(labelFont); signupPanel.add(emLbl, gbcReg);
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 2, 0);
        JTextField emailField = makeModernField.get(); signupPanel.add(emailField, gbcReg);
        
        JLabel emailErrorLbl = new JLabel("Invalid Email Format");
        emailErrorLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11)); emailErrorLbl.setForeground(new Color(231, 76, 60)); 
        emailErrorLbl.setVisible(false); 
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 5, 8, 0);
        signupPanel.add(emailErrorLbl, gbcReg);

        // Dynamic Email Borders
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String email = emailField.getText().trim();
                boolean isValid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
                if (email.isEmpty()) {
                    emailErrorLbl.setVisible(false);
                    emailField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
                } else if (isValid) {
                    emailErrorLbl.setVisible(false);
                    emailField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(46, 204, 113), 2, true), BorderFactory.createEmptyBorder(4, 14, 4, 14)));
                } else {
                    emailErrorLbl.setVisible(true);
                    emailField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2, true), BorderFactory.createEmptyBorder(4, 14, 4, 14)));
                }
            }
        });

        // Phone
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 5, 0);
        JLabel phLbl = new JLabel("Phone Number"); phLbl.setFont(labelFont); signupPanel.add(phLbl, gbcReg);
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 10, 0);
        JTextField phoneField = makeModernField.get(); makeNumericOnly(phoneField, false);
        phoneField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) { if (phoneField.getText().length() >= 10) e.consume(); }
        });
        signupPanel.add(phoneField, gbcReg);

        // Username
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 5, 0);
        JLabel unLbl = new JLabel("Username"); unLbl.setFont(labelFont); signupPanel.add(unLbl, gbcReg);
        
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 10, 0);
        JTextField regUserField = makeModernField.get(); signupPanel.add(regUserField, gbcReg);

        // Password
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 5, 0);
        JLabel pwLbl = new JLabel("Password"); pwLbl.setFont(labelFont); signupPanel.add(pwLbl, gbcReg);
        
        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 20, 0);
        JPasswordField regPassField = makeModernPassField.get(); signupPanel.add(regPassField, gbcReg);

        gbcReg.gridy++; gbcReg.insets = new Insets(0, 0, 10, 0);
        JButton registerBtn = styleButton("Register Account", new Color(46, 204, 113), Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(300, 45));
        signupPanel.add(registerBtn, gbcReg);

        gbcReg.gridy++;
        JButton goToLoginBtn = styleButton("Back to Sign In", new Color(240, 248, 255), HEADER_COLOR);
        goToLoginBtn.setPreferredSize(new Dimension(300, 45));
        signupPanel.add(goToLoginBtn, gbcReg);
        
        gbcReg.gridy++; gbcReg.weighty = 1.0; signupPanel.add(Box.createVerticalGlue(), gbcReg);


        // 3. FORGOT PASSWORD CARD 
        JPanel forgotPanel = new JPanel(new GridBagLayout());
        forgotPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcFwd = new GridBagConstraints();
        gbcFwd.fill = GridBagConstraints.HORIZONTAL; gbcFwd.weightx = 1.0; gbcFwd.gridx = 0; gbcFwd.gridy = 0;

        JLabel forgotTitle = new JLabel("Reset Password");
        forgotTitle.setFont(titleFont); forgotTitle.setForeground(new Color(231, 76, 60));
        forgotPanel.add(forgotTitle, gbcFwd);

        gbcFwd.gridy++; gbcFwd.insets = new Insets(5, 0, 30, 0);
        JLabel forgotSub = new JLabel("<html>Enter your username. We will send a secure<br>recovery code to your registered email.</html>");
        forgotSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); forgotSub.setForeground(Color.GRAY);
        forgotPanel.add(forgotSub, gbcFwd);

        gbcFwd.gridy++; gbcFwd.insets = new Insets(0, 0, 5, 0);
        JLabel fuLbl = new JLabel("Username"); fuLbl.setFont(labelFont); forgotPanel.add(fuLbl, gbcFwd);
        
        gbcFwd.gridy++; gbcFwd.insets = new Insets(0, 0, 30, 0);
        JTextField fwdUserField = makeModernField.get(); forgotPanel.add(fwdUserField, gbcFwd);

        gbcFwd.gridy++; gbcFwd.insets = new Insets(0, 0, 15, 0);
        JButton sendCodeBtn = styleButton("Send Secure Code", new Color(241, 196, 15), Color.BLACK);
        sendCodeBtn.setPreferredSize(new Dimension(300, 45));
        forgotPanel.add(sendCodeBtn, gbcFwd);

        gbcFwd.gridy++;
        JButton cancelForgotBtn = styleButton("Back to Sign In", new Color(253, 242, 240), new Color(231, 76, 60));
        cancelForgotBtn.setPreferredSize(new Dimension(300, 45));
        forgotPanel.add(cancelForgotBtn, gbcFwd);
        
        gbcFwd.gridy++; gbcFwd.weighty = 1.0; forgotPanel.add(Box.createVerticalGlue(), gbcFwd);


        // 4. RESET PASSWORD CARD 
        JPanel resetPanel = new JPanel(new GridBagLayout());
        resetPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcRst = new GridBagConstraints();
        gbcRst.fill = GridBagConstraints.HORIZONTAL; gbcRst.weightx = 1.0; gbcRst.gridx = 0; gbcRst.gridy = 0;

        JLabel resetTitle = new JLabel("Verify Code");
        resetTitle.setFont(titleFont); resetTitle.setForeground(new Color(155, 89, 182));
        resetPanel.add(resetTitle, gbcRst);

        gbcRst.gridy++; gbcRst.insets = new Insets(5, 0, 30, 0);
        JLabel resetSub = new JLabel("Enter the 6-digit code sent to your email.");
        resetSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); resetSub.setForeground(Color.GRAY);
        resetPanel.add(resetSub, gbcRst);

        gbcRst.gridy++; gbcRst.insets = new Insets(0, 0, 5, 0);
        JLabel cLbl = new JLabel("6-Digit Code"); cLbl.setFont(labelFont); resetPanel.add(cLbl, gbcRst);
        
        gbcRst.gridy++; gbcRst.insets = new Insets(0, 0, 15, 0);
        JTextField codeField = makeModernField.get(); resetPanel.add(codeField, gbcRst);

        gbcRst.gridy++; gbcRst.insets = new Insets(0, 0, 5, 0);
        JLabel nPwl = new JLabel("New Password"); nPwl.setFont(labelFont); resetPanel.add(nPwl, gbcRst);
        
        gbcRst.gridy++; gbcRst.insets = new Insets(0, 0, 30, 0);
        JPasswordField newPassField = makeModernPassField.get(); resetPanel.add(newPassField, gbcRst);

        gbcRst.gridy++; gbcRst.insets = new Insets(0, 0, 10, 0);
        JButton confirmResetBtn = styleButton("Confirm & Reset", new Color(155, 89, 182), Color.WHITE);
        confirmResetBtn.setPreferredSize(new Dimension(300, 45));
        resetPanel.add(confirmResetBtn, gbcRst);
        
        gbcRst.gridy++; gbcRst.weighty = 1.0; resetPanel.add(Box.createVerticalGlue(), gbcRst);


        // AUTHENTICATION ACTIONS & UX 
        authFrame.getRootPane().setDefaultButton(signInBtn);

        goToSignUpBtn.addActionListener(e -> {
            cardLayout.show(rightCardPanel, "SIGNUP");
            authFrame.getRootPane().setDefaultButton(registerBtn);
        });
        goToLoginBtn.addActionListener(e -> {
            cardLayout.show(rightCardPanel, "LOGIN");
            authFrame.getRootPane().setDefaultButton(signInBtn);
        });
        forgotPwdBtn.addActionListener(e -> {
            cardLayout.show(rightCardPanel, "FORGOT");
            authFrame.getRootPane().setDefaultButton(sendCodeBtn);
        });
        cancelForgotBtn.addActionListener(e -> {
            cardLayout.show(rightCardPanel, "LOGIN");
            authFrame.getRootPane().setDefaultButton(signInBtn);
        });

        loginUserField.addActionListener(e -> signInBtn.doClick());
        loginPassField.addActionListener(e -> signInBtn.doClick());
        nameField.addActionListener(e -> registerBtn.doClick());
        emailField.addActionListener(e -> registerBtn.doClick());
        regUserField.addActionListener(e -> registerBtn.doClick());
        regPassField.addActionListener(e -> registerBtn.doClick());
        fwdUserField.addActionListener(e -> sendCodeBtn.doClick());
        codeField.addActionListener(e -> confirmResetBtn.doClick());
        newPassField.addActionListener(e -> confirmResetBtn.doClick());

        signInBtn.addActionListener(e -> {
            if (db.authenticateUser(loginUserField.getText(), new String(loginPassField.getPassword()))) {
                loggedInUser = loginUserField.getText(); 
                db.setCurrentUser(loggedInUser); 
                authFrame.dispose();
                launchMainWindow();
            } else {
                JOptionPane.showMessageDialog(authFrame, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            String uname = regUserField.getText(); 
            String pwd = new String(regPassField.getPassword());
            String fname = nameField.getText(); 
            String email = emailField.getText();
            String phone = phoneField.getText().trim(); 

            if(uname.isEmpty() || pwd.isEmpty() || fname.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(authFrame, "All fields are required!"); 
                return;
            }
            if (db.isEmailRegistered(email)) {
                JOptionPane.showMessageDialog(authFrame, 
                    "This email is already associated with an account.\n" +
                    "Please use a different email or use 'Forgot Password'.", 
                    "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(phone.length() != 10) {
                JOptionPane.showMessageDialog(authFrame, "Phone number must be exactly 10 digits!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(db.registerUser(uname, pwd, fname, email, phone)) {
                JOptionPane.showMessageDialog(authFrame, "Account Created Successfully! Please Sign In.");
                cardLayout.show(rightCardPanel, "LOGIN");
                authFrame.getRootPane().setDefaultButton(signInBtn);
                loginUserField.setText(uname); 
                loginPassField.setText("");
            } else {
                JOptionPane.showMessageDialog(authFrame, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        sendCodeBtn.addActionListener(e -> {
            String u = fwdUserField.getText().trim();
            String[] userDetails = db.getUserDetails(u);
            String targetEmail = userDetails[2]; 

            if (!targetEmail.equals("Unknown") && !targetEmail.isEmpty()) {
                userResetting = u;
                generatedResetCode = String.format("%06d", new java.util.Random().nextInt(999999));
                
                String systemEmail = "23rudragupta@gmail.com"; 
                String systemAppPassword = "zumz nzae vhjr lmwo";

                String tempMaskedEmail = targetEmail;
                if(targetEmail.contains("@")) {
                    int at = targetEmail.indexOf("@");
                    tempMaskedEmail = (at > 2 ? targetEmail.substring(0, 2) + "***" : "***") + targetEmail.substring(at);
                }
                final String finalMaskedEmail = tempMaskedEmail;

                sendCodeBtn.setText("Sending...");
                sendCodeBtn.setEnabled(false);

                new Thread(() -> {
                    boolean success = EmailService.sendRealEmail(targetEmail, generatedResetCode, systemEmail, systemAppPassword);
                    SwingUtilities.invokeLater(() -> {
                        sendCodeBtn.setText("Send Secure Code");
                        sendCodeBtn.setEnabled(true);
                        if(success) {
                            JOptionPane.showMessageDialog(authFrame, "Success! A secure reset code has been sent to: " + finalMaskedEmail);
                            cardLayout.show(rightCardPanel, "RESET");
                            authFrame.getRootPane().setDefaultButton(confirmResetBtn);
                        } else {
                            JOptionPane.showMessageDialog(authFrame, "Failed to send email. Check your connection.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).start();
            } else {
                JOptionPane.showMessageDialog(authFrame, "If that username exists, a code has been sent to its registered email.", "System Alert", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        confirmResetBtn.addActionListener(e -> {
            if(codeField.getText().equals(generatedResetCode) && newPassField.getPassword().length > 0) {
                db.updatePassword(userResetting, new String(newPassField.getPassword()));
                JOptionPane.showMessageDialog(authFrame, "Password Reset Successfully! You can now log in.");
                cardLayout.show(rightCardPanel, "LOGIN");
                authFrame.getRootPane().setDefaultButton(signInBtn);
                loginUserField.setText(userResetting);
            } else {
                JOptionPane.showMessageDialog(authFrame, "Invalid Code or Password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        rightCardPanel.add(loginPanel, "LOGIN");
        rightCardPanel.add(signupPanel, "SIGNUP");
        rightCardPanel.add(forgotPanel, "FORGOT");
        rightCardPanel.add(resetPanel, "RESET");
        
        mainContainer.add(leftPanel);
        mainContainer.add(rightCardPanel);
        
        authFrame.add(mainContainer);
        authFrame.setVisible(true);
    }

    private JPanel createBeautifulCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setName("Card"); 
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(40, 50, 40, 50)
        ));
        return p;
    }

    private JPanel createSmallCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setName("Card"); 
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        return p;
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        return gbc;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD_FONT);
        return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(15);
        tf.setFont(MAIN_FONT);
        tf.setPreferredSize(new Dimension(200, 35));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField(15);
        pf.setFont(MAIN_FONT);
        pf.setPreferredSize(new Dimension(200, 35));
        return pf;
    }

    private JButton styleButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT); 
        btn.setBackground(bg); 
        btn.setForeground(fg);
        btn.setFocusPainted(false); 
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void makeNumericOnly(JTextField field, boolean allowDecimal) {
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (allowDecimal) {
                    if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) { e.consume(); }
                    if (c == '.' && field.getText().contains(".")) { e.consume(); } 
                } else {
                    if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) { e.consume(); }
                }
            }
        });
    }

    private void makeAlphabeticOnly(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && !Character.isWhitespace(c) && c != KeyEvent.VK_BACK_SPACE) { 
                    e.consume(); 
                }
            }
        });
    }

    private void autoSaveDatabase() {
        try {
            db.saveToFile();
        } catch (Exception ex) {
            System.out.println("Auto-save failed: " + ex.getMessage());
        }
    }

    private void launchMainWindow() {
        setTitle("Smart Inventory & Supply Chain Application");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PRIMARY_BG);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        topBar.setBackground(PRIMARY_BG);
        
        // Inside launchMainWindow()
        JButton themeBtn = styleButton("Toggle Dark Mode", Color.DARK_GRAY, Color.WHITE);
        themeBtn.addActionListener(e -> {
        toggleTheme();
        themeBtn.setText(isDarkMode ? "Toggle Light Mode" : "Toggle Dark Mode");
    });

        JButton helpBtn = styleButton("How to Use", new Color(52, 152, 219), Color.WHITE);
        helpBtn.addActionListener(e -> showHelpDialog());
        
        JButton profileBtn = styleButton("Profile", new Color(155, 89, 182), Color.WHITE); 
        profileBtn.addActionListener(e -> showProfileDialog());
        
        JButton logoutBtn = styleButton("Logout", new Color(231, 76, 60), Color.WHITE);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                autoSaveDatabase(); 
                loggedInUser = ""; 
                this.dispose(); 
                new MainApp();  
            }
        });

        topBar.add(themeBtn);
        topBar.add(helpBtn);
        topBar.add(profileBtn); 
        topBar.add(logoutBtn);
        add(topBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(BOLD_FONT);
        tabbedPane.setBackground(PANEL_BG);
        posPanel = new POSModule(db, this::refreshAll);
        tabbedPane.addTab("Inventory Dashboard", createInventoryPanel());
        tabbedPane.addTab("Point Of Sale & Billing", posPanel.getPanel()); 
        tabbedPane.addTab("Supply Chain & Logs", createSupplyChainPanel());
        tabbedPane.addTab("Analytics & Charts", createAnalyticsPanel());
        tabbedPane.addTab("Feedback & Team", createFeedbackPanel()); 

        add(tabbedPane, BorderLayout.CENTER);
        updateMetrics(); 
        setVisible(true);

        // Creates a background security timer (Set to 3 Minutes / 180,000 milliseconds)
        Timer idleTimer = new Timer(180000, e -> { 
            autoSaveDatabase();
            loggedInUser = "";
            this.dispose(); 
            
            new MainApp();
            JOptionPane.showMessageDialog(null, "Session expired due to inactivity.\nYour workspace has been locked for security.", "Security Alert", JOptionPane.WARNING_MESSAGE);
        });
        idleTimer.setRepeats(false);
        idleTimer.start();

        // Global listener: If the user types or moves the mouse anywhere in the app, reset the 3-minute timer
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            idleTimer.restart();
        }, java.awt.AWTEvent.KEY_EVENT_MASK | java.awt.AWTEvent.MOUSE_EVENT_MASK | java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);
    } 

    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "User Profile", true);
        profileDialog.setSize(900, 600); 
        profileDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topInfoPanel = new JPanel(new GridBagLayout());
        topInfoPanel.setBackground(PRIMARY_BG);
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(10, 20, 10, 20);

        String[] details = db.getUserDetails(loggedInUser);
        String fullName = details[1];
        String email = details[2];
        String userNotes = details[3];
        String imagePath = details[4];
        String phoneNumber = (details.length >= 6) ? details[5] : "Not Provided"; 

        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(150, 150));
        avatarLabel.setIcon(getCircularAvatar(imagePath, 150, fullName));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarLabel.setToolTipText("Click to view full image");

        // View Full Photo Logic
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String currentPath = db.getUserDetails(loggedInUser)[4]; 
                if (currentPath != null && !currentPath.isEmpty()) {
                    JDialog viewer = new JDialog(profileDialog, "Profile Photo Viewer", true);
                    viewer.setSize(600, 600);
                    viewer.setLocationRelativeTo(profileDialog);
                    JLabel fullImageLabel = new JLabel();
                    fullImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    try {
                        BufferedImage img = ImageIO.read(new File(currentPath));
                        Image scaledImg = img.getScaledInstance(550, 550, Image.SCALE_SMOOTH);
                        fullImageLabel.setIcon(new ImageIcon(scaledImg));
                    } catch (Exception ex) {
                        fullImageLabel.setText("Unable to load full image file.");
                        fullImageLabel.setForeground(Color.RED);
                    }
                    viewer.add(new JScrollPane(fullImageLabel));
                    viewer.setVisible(true);
                }
            }
        });

        // Add Avatar 
        gbcTop.gridx = 0; gbcTop.gridy = 0; gbcTop.gridheight = 5;
        topInfoPanel.add(avatarLabel, gbcTop);

        // Name Label
        gbcTop.gridheight = 1; gbcTop.gridx = 1; gbcTop.gridy = 0; gbcTop.anchor = GridBagConstraints.WEST;
        JLabel nameLbl = new JLabel(fullName); 
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        nameLbl.setForeground(TEXT_COLOR);
        topInfoPanel.add(nameLbl, gbcTop);

        // Email Label
        gbcTop.gridy = 1;
        JLabel emailLbl = new JLabel(email); 
        emailLbl.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        emailLbl.setForeground(TEXT_COLOR);
        topInfoPanel.add(emailLbl, gbcTop);

        // Phone Number Label
        gbcTop.gridy = 2;
        JLabel phoneLbl = new JLabel("+91-" + phoneNumber); 
        phoneLbl.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        phoneLbl.setForeground(TEXT_COLOR);
        topInfoPanel.add(phoneLbl, gbcTop);

        // Username Label 
        gbcTop.gridy = 3;
        JLabel userLbl = new JLabel("@" + loggedInUser); 
        userLbl.setFont(new Font("Segoe UI", Font.ITALIC, 20)); 
        userLbl.setForeground(Color.GRAY);
        topInfoPanel.add(userLbl, gbcTop);

        // Upload Button 
        gbcTop.gridy = 4;
        JButton uploadBtn = styleButton("Upload Photo", new Color(52, 152, 219), Color.WHITE);
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(profileDialog) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                db.updateProfilePic(loggedInUser, file.getAbsolutePath());
                avatarLabel.setIcon(getCircularAvatar(file.getAbsolutePath(), 150, fullName));
            }
        });
        topInfoPanel.add(uploadBtn, gbcTop);

        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        bottomGrid.setBackground(PRIMARY_BG);

        // Left: Notes Panel
        JPanel noteCard = createSmallCard();
        noteCard.setLayout(new BorderLayout(0, 20)); 
        
        JLabel noteTitle = new JLabel("Personal Workspace Notes", SwingConstants.CENTER);
        noteTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        noteTitle.setForeground(new Color(46, 204, 113));
        noteCard.add(noteTitle, BorderLayout.NORTH); 

        JTextArea notesArea = new JTextArea(userNotes);
        notesArea.setFont(MAIN_FONT);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(isDarkMode ? new Color(60, 63, 65) : Color.WHITE);
        notesArea.setForeground(TEXT_COLOR);
        notesArea.setCaretColor(TEXT_COLOR);
        
        JScrollPane scrollNotes = new JScrollPane(notesArea);
        
        Color fieldBgColor = isDarkMode ? new Color(60, 63, 65) : Color.WHITE;
        scrollNotes.setBackground(fieldBgColor);
        scrollNotes.getViewport().setBackground(fieldBgColor);
        
        scrollNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80, 80, 80) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        noteCard.add(scrollNotes, BorderLayout.CENTER);

        JButton saveNotesBtn = styleButton("Save Notes", new Color(46, 204, 113), Color.WHITE);
        saveNotesBtn.setPreferredSize(new Dimension(200, 45)); 
        saveNotesBtn.addActionListener(e -> {
            db.updateUserNotes(loggedInUser, notesArea.getText());
            JOptionPane.showMessageDialog(profileDialog, "Personal notes saved successfully!");
        });
        noteCard.add(saveNotesBtn, BorderLayout.SOUTH); 

        // Right: Security Panel (Password Update + Account Deletion)
        JPanel secCard = createSmallCard();
        secCard.setLayout(new BorderLayout(0, 20)); 

        JLabel passTitle = new JLabel("Security Settings", SwingConstants.CENTER);
        passTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        passTitle.setForeground(new Color(231, 76, 60));
        secCard.add(passTitle, BorderLayout.NORTH); 

        // 1. Password Fields Container
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 1, 0, 15)); 
        fieldsPanel.setBackground(PANEL_BG);

        // Row 1: Current Password
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setBackground(PANEL_BG);
        JLabel opLbl = new JLabel("Current Password:"); 
        opLbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        opLbl.setForeground(TEXT_COLOR); 
        opLbl.setPreferredSize(new Dimension(140, 30)); 
        JPasswordField oldPass = new JPasswordField(); 
        oldPass.setFont(MAIN_FONT);
        oldPass.setBackground(isDarkMode ? new Color(60, 63, 65) : Color.WHITE);
        oldPass.setForeground(TEXT_COLOR); 
        oldPass.setCaretColor(TEXT_COLOR);
        oldPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80, 80, 80) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10) 
        ));
        row1.add(opLbl, BorderLayout.WEST); row1.add(oldPass, BorderLayout.CENTER);

        // Row 2: New Password
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setBackground(PANEL_BG);
        JLabel npLbl = new JLabel("New Password:"); 
        npLbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        npLbl.setForeground(TEXT_COLOR); 
        npLbl.setPreferredSize(new Dimension(140, 30)); 
        JPasswordField newPass = new JPasswordField(); 
        newPass.setFont(MAIN_FONT);
        newPass.setBackground(isDarkMode ? new Color(60, 63, 65) : Color.WHITE);
        newPass.setForeground(TEXT_COLOR); 
        newPass.setCaretColor(TEXT_COLOR);
        newPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80, 80, 80) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        row2.add(npLbl, BorderLayout.WEST); row2.add(newPass, BorderLayout.CENTER);

        fieldsPanel.add(row1);
        fieldsPanel.add(row2);

        JPanel fieldsWrapper = new JPanel(new BorderLayout());
        fieldsWrapper.setBackground(PANEL_BG);
        fieldsWrapper.add(fieldsPanel, BorderLayout.NORTH);
        secCard.add(fieldsWrapper, BorderLayout.CENTER); 

        // 2. Split Button Footer
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0)); 
        buttonRow.setBackground(PANEL_BG);
        buttonRow.setPreferredSize(new Dimension(200, 45));

        // UPDATE PASSWORD BUTTON 
        JButton updatePassBtn = styleButton("Update Password", new Color(46, 204, 113), Color.WHITE);
        updatePassBtn.addActionListener(e -> {
            String oldP = new String(oldPass.getPassword()); 
            String newP = new String(newPass.getPassword());
            if(oldP.isEmpty() || newP.isEmpty()) { 
                JOptionPane.showMessageDialog(profileDialog, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE); 
                return; 
            }
            if(db.authenticateUser(loggedInUser, oldP)) {
                db.updatePassword(loggedInUser, newP);
                JOptionPane.showMessageDialog(profileDialog, "Password securely updated!");
                oldPass.setText(""); newPass.setText("");
            } else {
                JOptionPane.showMessageDialog(profileDialog, "Current password is incorrect.", "Security Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // DELETE ACCOUNT BUTTON
        JButton deleteAccountBtn = styleButton("Delete Account", new Color(231, 76, 60), Color.WHITE);
        deleteAccountBtn.addActionListener(e -> {
            int firstConfirm = JOptionPane.showConfirmDialog(profileDialog, 
                "ARE YOU SURE?\nThis will permanently delete your account and all data.", 
                "Final Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (firstConfirm == JOptionPane.YES_OPTION) {
                JPasswordField pf = new JPasswordField();
                int passConfirm = JOptionPane.showConfirmDialog(profileDialog, pf, 
                    "Enter password to confirm PERMANENT deletion:", JOptionPane.OK_CANCEL_OPTION);

                if (passConfirm == JOptionPane.OK_OPTION) {
                    if (db.authenticateUser(loggedInUser, new String(pf.getPassword()))) {
                        db.deleteUserAccount(loggedInUser);
                        profileDialog.dispose();
                        this.dispose();
                        new MainApp();
                        JOptionPane.showMessageDialog(null, "Your account has been deleted.");
                    } else {
                        JOptionPane.showMessageDialog(profileDialog, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        buttonRow.add(updatePassBtn);
        buttonRow.add(deleteAccountBtn);
        
        secCard.add(buttonRow, BorderLayout.SOUTH);

        bottomGrid.add(noteCard);
        bottomGrid.add(secCard);

        mainPanel.add(topInfoPanel, BorderLayout.NORTH);
        mainPanel.add(bottomGrid, BorderLayout.CENTER);

        profileDialog.add(mainPanel);
        profileDialog.setVisible(true);
    }

    private ImageIcon getCircularAvatar(String imagePath, int size, String fallbackName) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                BufferedImage master = ImageIO.read(new File(imagePath));
                if (master != null) {
                    int min = Math.min(master.getWidth(), master.getHeight());
                    BufferedImage cropped = master.getSubimage((master.getWidth() - min) / 2, (master.getHeight() - min) / 2, min, min);
                    Image scaled = cropped.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    BufferedImage circleBuffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circleBuffer.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.fillOval(0, 0, size, size);
                    g2.setComposite(AlphaComposite.SrcIn);
                    g2.drawImage(scaled, 0, 0, null);
                    g2.dispose();
                    return new ImageIcon(circleBuffer);
                }
            } catch (Exception e) { System.out.println("Could not load image."); }
        }
        
        BufferedImage circleBuffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(155, 89, 182)); 
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        String letter = (fallbackName == null || fallbackName.isEmpty()) ? "?" : fallbackName.substring(0,1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(letter)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(letter, x, y);
        g2.dispose();
        return new ImageIcon(circleBuffer);
    }

private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "System Guide", true);
        helpDialog.setSize(750, 680); 
        helpDialog.setLocationRelativeTo(this);
        helpDialog.setUndecorated(true); 

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 3)); 

        // Premium Header 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        
        JLabel headerLabel = new JLabel("Smart Inventory & Supply Chain Application", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        
        JLabel subHeaderLabel = new JLabel("Complete documentation for managing your smart inventory and supply chain guide", SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subHeaderLabel.setForeground(new Color(220, 240, 255)); 
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(subHeaderLabel, BorderLayout.SOUTH);

        // Scrollable Card Content Area 
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(PRIMARY_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Adding nicely styled individual feature cards
        contentPanel.add(createHelpCard(" 1. Inventory Dashboard", 
            "• Manage Stock: Add products with Lead Times and Suppliers, or use 'Update Stock' to instantly adjust quantities.\n" +
            "• Barcodes & Data: Click 'View Barcode' to export item IDs as images. Bulk add/backup items using 'Import/Export CSV'.\n" +
            "• Search & Filter: Use the dynamic 'Low Stock Filter' pill or search globally by ID/Name.", 
            new Color(46, 204, 113))); 
            
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // NEW POS SECTION
        contentPanel.add(createHelpCard(" 2. Point of Sale (POS) & Billing", 
            "• Digital Register: Search your catalog on the left and click 'Add to Order' to build a customer's shopping cart.\n" +
            "• Smart Checkout: Automatically calculates financial totals and prevents you from selling more stock than you physically have.\n" +
            "• Instant Receipts: Completing a checkout instantly deducts the inventory and saves a professional text-based receipt.", 
            new Color(52, 152, 219))); 
            
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createHelpCard(" 3. Supply Chain & Logs", 
            "• Smart Orders: Select a low-stock item and click 'Send Restock Order' to view AI-recommended order quantities.\n" +
            "• Receiving: Click 'Mark Order Received' to physically add arrived units into your current active stock.\n" +
            "• Audits & Alerts: Export a secure 'Audit Log' of all actions, or click 'Email Alert Report' to send a low-stock summary to your email.", 
            new Color(241, 196, 15))); 
            
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createHelpCard(" 4. Analytics & Feedback", 
            "• Live Charts: The Bar Chart, Line Graph, and Pie Chart instantly redraw the exact second your inventory changes or a POS sale is made.\n" +
            "• Share Experience: Switch to the 'Feedback & Team' tab to rate your experience and send messages directly to the developers.", 
            new Color(155, 89, 182))); 
            
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createHelpCard(" 5. Security & Personalization", 
            "• User Profile: Click 'Profile' in the top bar to set a custom Avatar picture, save Private Workspace Notes, and securely update your password.\n" +
            "• Data Protection: Account deletion requires your password. Forgotten passwords can be securely recovered via email codes.\n" +
            "• Customization: The system auto-saves your data. Click 'Toggle Dark Mode' in the top bar to switch UI themes instantly.", 
            new Color(231, 76, 60)));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(PRIMARY_BG);
        scrollPane.getViewport().setBackground(PRIMARY_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); 

        // Large Footer Button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(PRIMARY_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
        
        JButton closeBtn = styleButton("Got it, Let's Start!", HEADER_COLOR, Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(350, 50));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.addActionListener(e -> helpDialog.dispose());
        footerPanel.add(closeBtn);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        helpDialog.add(mainPanel);
        helpDialog.setVisible(true);
    }

    // Generates UI Cards for the Help Screen
    private JPanel createHelpCard(String title, String content, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80, 80, 80) : new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(accentColor);
        card.add(titleLabel, BorderLayout.NORTH);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        contentArea.setForeground(TEXT_COLOR);
        contentArea.setBackground(PANEL_BG);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setHighlighter(null); 
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        card.add(contentArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Live Category Distribution", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        
        JPanel chartsContainer = new JPanel(new GridLayout(1, 3, 15, 0));
        chartsContainer.setBackground(PRIMARY_BG);

        chartPanel = new AnalyticsChart();
        chartPanel.setBackground(PANEL_BG);
        
        lineChartPanel = new LineChart();
        lineChartPanel.setBackground(PANEL_BG);
        
        pieChartPanel = new PieChart();
        pieChartPanel.setBackground(PANEL_BG);

        chartsContainer.add(chartPanel);
        chartsContainer.add(lineChartPanel); 
        chartsContainer.add(pieChartPanel);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(chartsContainer, BorderLayout.CENTER);

        Map<String, Integer> dist = db.getCategoryDistribution();
        chartPanel.setData(dist);
        lineChartPanel.setData(dist);
        pieChartPanel.setData(dist);

        return panel;
    }

private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.fill = GridBagConstraints.BOTH;
        gbcMain.weighty = 1.0; 

        String[] details = db.getUserDetails(loggedInUser);
        String prefillName = (details[1] != null && !details[1].equals("Unknown")) ? details[1] : "";
        String prefillEmail = (details[2] != null && !details[2].equals("Unknown")) ? details[2] : "";

        // LEFT CARD: Feedback Form (65%)
        JPanel feedCard = createBeautifulCard();
        feedCard.setLayout(new GridBagLayout());
        GridBagConstraints gbcFeed = new GridBagConstraints();
        gbcFeed.fill = GridBagConstraints.HORIZONTAL;
        gbcFeed.insets = new Insets(10, 20, 10, 20);

        JLabel titleLbl = new JLabel("We Value Your Feedback", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLbl.setForeground(HEADER_COLOR);
        gbcFeed.gridx = 0; gbcFeed.gridy = 0; gbcFeed.gridwidth = 2;
        feedCard.add(titleLbl, gbcFeed);

        JLabel subLbl = new JLabel("Help us build a better inventory experience for you.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLbl.setForeground(Color.GRAY);
        gbcFeed.gridy = 1; gbcFeed.insets = new Insets(0, 20, 25, 20);
        feedCard.add(subLbl, gbcFeed);

        gbcFeed.gridwidth = 1;
        gbcFeed.insets = new Insets(8, 20, 8, 10);
        
        gbcFeed.gridy = 2; gbcFeed.gridx = 0; gbcFeed.weightx = 0.05;
        feedCard.add(createLabel("Full Name:"), gbcFeed);
        gbcFeed.gridx = 1; gbcFeed.weightx = 0.95;
        JTextField nameField = createTextField(); 
        nameField.setText(prefillName);
        feedCard.add(nameField, gbcFeed);

        gbcFeed.gridy = 3; gbcFeed.gridx = 0; gbcFeed.weightx = 0.05;
        feedCard.add(createLabel("Email:"), gbcFeed);
        gbcFeed.gridx = 1; gbcFeed.weightx = 0.95;
        JTextField emailField = createTextField(); 
        emailField.setText(prefillEmail);
        feedCard.add(emailField, gbcFeed);

        // Using clean text-based ratings to prevent font box errors
        gbcFeed.gridy = 4; gbcFeed.gridx = 0; gbcFeed.weightx = 0.05;
        feedCard.add(createLabel("Experience:"), gbcFeed);
        gbcFeed.gridx = 1; gbcFeed.weightx = 0.95;
        String[] ratings = {"5 / 5 - Excellent", "4 / 5 - Good", "3 / 5 - Average", "2 / 5 - Poor", "1 / 5 - Terrible"};
        JComboBox<String> ratingBox = new JComboBox<>(ratings);
        ratingBox.setFont(MAIN_FONT);
        ratingBox.setBackground(isDarkMode ? new Color(60,63,65) : Color.WHITE);
        ratingBox.setForeground(TEXT_COLOR);
        feedCard.add(ratingBox, gbcFeed);

        gbcFeed.gridy = 5; gbcFeed.gridx = 0; gbcFeed.weightx = 0.05; gbcFeed.anchor = GridBagConstraints.NORTHWEST;
        feedCard.add(createLabel("Message:"), gbcFeed);
        gbcFeed.gridx = 1; gbcFeed.weightx = 0.95; gbcFeed.weighty = 1.0; gbcFeed.fill = GridBagConstraints.BOTH;
        JTextArea feedArea = new JTextArea();
        feedArea.setFont(MAIN_FONT);
        feedArea.setLineWrap(true);
        feedArea.setWrapStyleWord(true);
        feedArea.setBackground(isDarkMode ? new Color(60, 63, 65) : Color.WHITE);
        feedArea.setForeground(TEXT_COLOR);
        feedArea.setCaretColor(TEXT_COLOR);
        
        JScrollPane scroll = new JScrollPane(feedArea);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80, 80, 80) : new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        feedCard.add(scroll, gbcFeed);

        gbcFeed.gridy = 6; gbcFeed.gridx = 0; gbcFeed.gridwidth = 2; gbcFeed.weighty = 0;
        gbcFeed.anchor = GridBagConstraints.CENTER; gbcFeed.fill = GridBagConstraints.NONE;
        gbcFeed.insets = new Insets(20, 0, 10, 0);
        JButton sendBtn = styleButton("Submit Feedback", new Color(46, 204, 113), Color.WHITE);
        sendBtn.setPreferredSize(new Dimension(250, 45));
        feedCard.add(sendBtn, gbcFeed);

        gbcMain.gridx = 0; gbcMain.gridy = 0; gbcMain.weightx = 0.65;
        gbcMain.insets = new Insets(0, 0, 0, 15);
        panel.add(feedCard, gbcMain);

        // RIGHT CARD: Developer Team (35%)
        JPanel devCard = createBeautifulCard();
        devCard.setLayout(new BorderLayout(0, 15));

        JPanel devHeaderPanel = new JPanel(new BorderLayout());
        devHeaderPanel.setOpaque(false); 
        JLabel devTitle = new JLabel("Meet the Team", SwingConstants.CENTER);
        devTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        devTitle.setForeground(HEADER_COLOR);
        devHeaderPanel.add(devTitle, BorderLayout.CENTER);
        devCard.add(devHeaderPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false); 
        
        String[] devNames = {"Rudra Gupta", "Arnav Shukla", "Rahul Balhara", "Abhigyan Gaurav", "Rishika Sinha"};
        for (String dev : devNames) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            item.setOpaque(false); 
            item.add(new JLabel(getCircularAvatar("", 35, dev)));
            
            JLabel devNameLbl = new JLabel(dev);
            devNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
            devNameLbl.setForeground(TEXT_COLOR); 
            item.add(devNameLbl);
            
            listPanel.add(item);
        }
        
        JScrollPane devScroll = new JScrollPane(listPanel);
        devScroll.setBorder(null);
        devScroll.setOpaque(false);
        devScroll.getViewport().setOpaque(false);
        devCard.add(devScroll, BorderLayout.CENTER);

        JPanel contactPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        contactPanel.setOpaque(false); 
        contactPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JLabel contactTitle = new JLabel("Direct Support", SwingConstants.CENTER);
        contactTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        contactTitle.setForeground(new Color(231, 76, 60)); 
        
        JLabel emailLabel = new JLabel("23rudragupta@gmail.com", SwingConstants.CENTER);
        emailLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        emailLabel.setForeground(TEXT_COLOR); 
        
        contactPanel.add(contactTitle);
        contactPanel.add(emailLabel);

        JPanel footerWrapper = new JPanel(new BorderLayout());
        footerWrapper.setOpaque(false);
        footerWrapper.add(new JSeparator(), BorderLayout.NORTH);
        footerWrapper.add(contactPanel, BorderLayout.CENTER);

        devCard.add(footerWrapper, BorderLayout.SOUTH);

        gbcMain.gridx = 1; gbcMain.weightx = 0.35;
        gbcMain.insets = new Insets(0, 15, 0, 0);
        panel.add(devCard, gbcMain);

        // ACTION LOGIC
        sendBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String text = feedArea.getText().trim();
            String rating = (String) ratingBox.getSelectedItem();

            if(name.isEmpty() || email.isEmpty() || text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out your Name, Email, and Message.", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String finalMessage = "User Rating: " + rating + "\n\nFeedback:\n" + text;
            
            sendBtn.setText("Sending...");
            sendBtn.setEnabled(false);

            new Thread(() -> {
                boolean success = EmailService.sendFeedbackEmail("23rudragupta@gmail.com", name, email, finalMessage, "23rudragupta@gmail.com", "zumz nzae vhjr lmwo");
                SwingUtilities.invokeLater(() -> {
                    sendBtn.setText("Submit Feedback");
                    sendBtn.setEnabled(true);
                    if(success) {
                        JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been sent successfully.");
                        feedArea.setText(""); 
                        ratingBox.setSelectedIndex(0);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to send feedback. Check your connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });

        return panel;
    }
    // ENHANCED INVENTORY PANEL 
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        metricsPanel.setBackground(PRIMARY_BG);
        
        totalItemsLabel = createMetricCard("Total Products", "0");
        totalValueLabel = createMetricCard("Inventory Value", "₹0.00");
        lowStockLabel = createMetricCard("Low Stock Alerts", "0");
        
        metricsPanel.add(totalItemsLabel); metricsPanel.add(totalValueLabel); metricsPanel.add(lowStockLabel);

        JPanel centerWrapper = new JPanel(new BorderLayout(10, 10));
        centerWrapper.setBackground(PRIMARY_BG);

        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setName("Card");
        controlsPanel.setBackground(PANEL_BG);
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setBackground(PANEL_BG);
        searchPanel.setName("Card");
        JTextField searchField = new JTextField(20);
        searchField.setFont(MAIN_FONT);
        searchField.setPreferredSize(new Dimension(300, 38)); 
        JButton searchBtn = styleButton("Search", new Color(52, 152, 219), Color.WHITE);
        JButton resetBtn = styleButton("Clear", Color.LIGHT_GRAY, Color.BLACK);
        
        JLabel searchLbl = new JLabel("Search By ID/Name:");
        searchLbl.setFont(BOLD_FONT);
        searchPanel.add(searchLbl); 
        searchPanel.add(searchField);
        searchPanel.add(searchBtn); 
        searchPanel.add(resetBtn);
        
        // Toggle Button 
        JToggleButton lowStockCheck = new JToggleButton("Low Stock Filter");
        lowStockCheck.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lowStockCheck.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lowStockCheck.setFocusPainted(false);
        lowStockCheck.setContentAreaFilled(false);
        lowStockCheck.setOpaque(true);
        lowStockCheck.setPreferredSize(new Dimension(170, 38)); 
        
        lowStockCheck.setForeground(new Color(231, 76, 60)); 
        lowStockCheck.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2, true));

        JPanel toggleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        toggleWrapper.setOpaque(false); 
        toggleWrapper.add(lowStockCheck);

        // Initial background setup
        lowStockCheck.setBackground(searchPanel.getBackground());

        // Dynamic Hover & Click colors that respect Dark Mode!
        lowStockCheck.addItemListener(e -> {
            if (lowStockCheck.isSelected()) {
                lowStockCheck.setBackground(new Color(231, 76, 60));
                lowStockCheck.setForeground(Color.WHITE);
            } else {
                // Return to the current dynamic panel color when OFF
                lowStockCheck.setBackground(toggleWrapper.getParent() != null ? toggleWrapper.getParent().getBackground() : Color.WHITE); 
                lowStockCheck.setForeground(new Color(231, 76, 60));
            }
        });
        
        searchPanel.add(toggleWrapper);

        // Increased grid layout to 9 columns to fit the new Lead Time input
        JPanel formPanel = new JPanel(new GridLayout(2, 8, 5, 5));
        formPanel.setBackground(PANEL_BG);
        formPanel.setName("Card");
        
        JTextField idField = createTextField(); 
        JTextField nameField = createTextField();
        
        String[] categories = {"Electronics", "Raw Materials", "Packaging", "Displays", "Power/Batteries", "Hardware", "Tools", "Miscellaneous"};
        JComboBox<String> catBox = new JComboBox<>(categories);
        catBox.setFont(MAIN_FONT);
        catBox.setBackground(isDarkMode ? new Color(60,63,65) : Color.WHITE);
        
        catBox.addActionListener(e -> {
            if ("Miscellaneous".equals(catBox.getSelectedItem())) {
                String customCat = JOptionPane.showInputDialog(panel, "Enter custom category name:", "Custom Category", JOptionPane.PLAIN_MESSAGE);
                if (customCat != null && !customCat.trim().isEmpty()) {
                    catBox.addItem(customCat); 
                    catBox.setSelectedItem(customCat); 
                } else {
                    catBox.setSelectedIndex(0); 
                }
            }
        });
        
        JTextField qtyField = createTextField();
        JTextField priceField = createTextField(); 
        JTextField suppField = createTextField();
        JTextField leadTimeField = createTextField();
        
        makeAlphabeticOnly(suppField);
        makeNumericOnly(qtyField, false); 
        makeNumericOnly(priceField, true);  
        makeNumericOnly(leadTimeField, false); 

        formPanel.add(createLabel("Product ID")); formPanel.add(createLabel("Name")); formPanel.add(createLabel("Category"));
        formPanel.add(createLabel("Quantity")); formPanel.add(createLabel("Unit Price (₹)")); 
        formPanel.add(createLabel("Supplier Name")); formPanel.add(createLabel("Delivery Time")); formPanel.add(createLabel(""));
        
        formPanel.add(idField); formPanel.add(nameField); formPanel.add(catBox);
        formPanel.add(qtyField); formPanel.add(priceField); formPanel.add(suppField); formPanel.add(leadTimeField);
        
        JButton addButton = styleButton("Add Item", new Color(46, 204, 113), Color.WHITE);
        formPanel.add(addButton);

        controlsPanel.add(searchPanel, BorderLayout.NORTH);
        controlsPanel.add(formPanel, BorderLayout.CENTER);

        // Added "Total Value" column
        String[] columns = {"ID", "Name", "Category", "Qty", "Unit Price", "Total Value", "Date", "Status", "Supplier", "Delivery Time"};
        tableModel = new DefaultTableModel(columns, 0) { 
            public boolean isCellEditable(int row, int column) { return false; } 
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class; 
                return String.class;
            }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(MAIN_FONT); inventoryTable.setRowHeight(30);
        
        JTableHeader header = inventoryTable.getTableHeader();
        header.setFont(BOLD_FONT); header.setBackground(HEADER_COLOR); header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 35));
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        // Force Qty column (Index 3) to Left Align
        DefaultTableCellRenderer leftAlignRenderer = new DefaultTableCellRenderer();
        leftAlignRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        inventoryTable.getColumnModel().getColumn(3).setCellRenderer(leftAlignRenderer);

        // Status column is now at index 7 due to the new "Total Value" column
        inventoryTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String status = value.toString().toLowerCase();
                    if (status.contains("ordered") || status.contains("delayed")) c.setForeground(new Color(231, 76, 60)); // Red
                    else if (status.contains("processing") || status.contains("transit")) c.setForeground(new Color(241, 196, 15)); // Orange
                    else c.setForeground(new Color(46, 204, 113)); // Green
                } else if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });

        refreshTable(db.getAllProducts());
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        centerWrapper.add(controlsPanel, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(PRIMARY_BG);
        
        JButton editButton = styleButton("Update Stock", new Color(241, 196, 15), Color.BLACK);
        // View Barcode button
        JButton barcodeBtn = styleButton("View Barcode", Color.DARK_GRAY, Color.WHITE);
        JButton dispatchButton = styleButton("Dispatch / Sell", new Color(52, 152, 219), Color.WHITE);
        JButton deleteButton = styleButton("Delete Stock", new Color(231, 76, 60), Color.WHITE);
        JButton exportBtn = styleButton("Export CSV Report", new Color(155, 89, 182), Color.WHITE);
        JButton importBtn = styleButton("Import CSV", new Color(46, 204, 113), Color.WHITE);
        
        // Added barcodeBtn to the layout sequence
        bottomPanel.removeAll(); 
        bottomPanel.add(editButton); bottomPanel.add(barcodeBtn); bottomPanel.add(dispatchButton); bottomPanel.add(deleteButton);
        bottomPanel.add(new JLabel(" | ")); bottomPanel.add(importBtn); bottomPanel.add(exportBtn);

        addButton.addActionListener(e -> {
            try {
                String newId = idField.getText().trim();
                
                if(newId.isEmpty() || nameField.getText().trim().isEmpty() || qtyField.getText().trim().isEmpty() || 
                   priceField.getText().trim().isEmpty() || suppField.getText().trim().isEmpty() || leadTimeField.getText().trim().isEmpty()){
                    JOptionPane.showMessageDialog(this, "Please fill all required fields, including Supplier and Lead Time."); 
                    return;
                }
                
                for (Product existingP : db.getAllProducts()) {
                    if (existingP.getId().equalsIgnoreCase(newId)) {
                        JOptionPane.showMessageDialog(this, "Product ID already exists! Please use a unique ID.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return; 
                    }
                }

                int leadTime = Integer.parseInt(leadTimeField.getText().trim());
                String autoDate = java.time.LocalDate.now().toString(); // Automatically grabs today's date

                Product p = new Product(newId, nameField.getText().trim(), catBox.getSelectedItem().toString(), 
                    Integer.parseInt(qtyField.getText().trim()), Double.parseDouble(priceField.getText().trim()), autoDate, "In Stock", suppField.getText().trim(), leadTime);
                
                db.addProduct(p); 
                autoSaveDatabase(); 
                refreshAll();
                
                idField.setText(""); nameField.setText(""); catBox.setSelectedIndex(0); qtyField.setText(""); priceField.setText(""); suppField.setText(""); leadTimeField.setText("");
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Invalid Format! Please check your numbers."); 
            }
        });

        searchBtn.addActionListener(e -> refreshTable(db.searchProducts(searchField.getText())));
        resetBtn.addActionListener(e -> { searchField.setText(""); lowStockCheck.setSelected(false); sorter.setRowFilter(null); refreshTable(db.getAllProducts()); });

        lowStockCheck.addActionListener(e -> {
            if (lowStockCheck.isSelected()) {
                sorter.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, 20, 3)); 
            } else {
                sorter.setRowFilter(null);
            }
        });

        // Dispatch Logic with Auto-Invoice Generation
        dispatchButton.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                Product p = db.getProduct(modelRow);
                String qtyStr = JOptionPane.showInputDialog(this, "How many units of " + p.getName() + " are you dispatching/selling?", "1");
                
                if (qtyStr != null && qtyStr.matches("\\d+")) {
                    int dispatchQty = Integer.parseInt(qtyStr);
                    if (dispatchQty <= p.getQuantity()) {
                        p.setQuantity(p.getQuantity() - dispatchQty);
                        db.logActivity("Dispatched/Sold " + dispatchQty + " units of " + p.getName());
                        autoSaveDatabase();
                        refreshAll();
                        
                        // GENERATE DIGITAL INVOICE
                        try {
                            String invoiceName = "Invoice_" + p.getId() + "_" + (System.currentTimeMillis() / 1000) + ".txt";
                            File invoiceFile = new File(invoiceName);
                            PrintWriter out = new PrintWriter(new java.io.FileWriter(invoiceFile));
                            out.println("=========================================");
                            out.println("          SMART INVENTORY SYSTEM         ");
                            out.println("             OFFICIAL INVOICE            ");
                            out.println("=========================================");
                            out.println("Date: " + LocalDate.now());
                            out.println("Transaction ID: TXN-" + System.currentTimeMillis());
                            out.println("Generated By: " + loggedInUser);
                            out.println("-----------------------------------------");
                            out.println("Item ID      : " + p.getId());
                            out.println("Item Name    : " + p.getName());
                            out.println("Category     : " + p.getCategory());
                            out.println("-----------------------------------------");
                            out.println("Qty Sold     : " + dispatchQty);
                            out.println("Unit Price   : Rs. " + String.format("%.2f", p.getPrice()));
                            out.println("-----------------------------------------");
                            out.println("TOTAL AMOUNT : Rs. " + String.format("%.2f", (dispatchQty * p.getPrice())));
                            out.println("=========================================");
                            out.println("     Thank you for your business!        ");
                            out.close();
                            
                            JOptionPane.showMessageDialog(this, "Stock dispatched successfully!\nInvoice generated: " + invoiceName);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Stock dispatched, but failed to generate invoice file.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Not enough stock! You only have " + p.getQuantity() + " units available.", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an entry from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) { 
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow); 
                JPasswordField pf = new JPasswordField();
                pf.setFont(MAIN_FONT);
                int okCxl = JOptionPane.showConfirmDialog(this, pf, "Enter your password to confirm deletion:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
                if (okCxl == JOptionPane.OK_OPTION) {
                    String password = new String(pf.getPassword());
                    if (db.authenticateUser(loggedInUser, password)) {
                        db.removeProduct(modelRow); 
                        autoSaveDatabase(); 
                        refreshAll(); 
                        JOptionPane.showMessageDialog(this, "Entry securely deleted.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password! Deletion cancelled.", "Security Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an entry from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        editButton.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow); 
                String newQty = JOptionPane.showInputDialog("Update Quantity (Numbers Only):", db.getProduct(modelRow).getQuantity());
                if (newQty != null && newQty.matches("\\d+")) { 
                    db.getProduct(modelRow).setQuantity(Integer.parseInt(newQty)); 
                    autoSaveDatabase(); 
                    refreshAll(); 
                } else if (newQty != null) {
                    JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an entry from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        exportBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Inventory Report");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheet", "csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if(!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
                try (PrintWriter pw = new PrintWriter(file)) {
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        pw.print(tableModel.getColumnName(i) + (i == tableModel.getColumnCount() - 1 ? "" : ","));
                    }
                    pw.println();
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            pw.print(tableModel.getValueAt(i, j).toString().replace(",", " ") + (j == tableModel.getColumnCount() - 1 ? "" : ","));
                        }
                        pw.println();
                    }
                    JOptionPane.showMessageDialog(this, "Inventory Report successfully exported!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error exporting file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // CSV Import
        importBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select CSV File to Import");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheet", "csv"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line = br.readLine(); 
                    int importCount = 0;
                    int autoChangedCount = 0;
                    
                    while ((line = br.readLine()) != null) {
                        String[] vals = line.split(",");
                        if (vals.length >= 9) {
                            try {
                                String originalId = vals[0].trim();
                                String finalId = originalId;
                                int textSuffix = 1;
                                
                                // SMART ID RESOLVER
                                boolean exists = true;
                                while (exists) {
                                    exists = false;
                                    // Check if finalId is currently in the database
                                    for (Product existingP : db.getAllProducts()) {
                                        if (existingP.getId().equalsIgnoreCase(finalId)) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    
                                    // If it exists, we must mutate finalId and loop again
                                    if (exists) {
                                        try {
                                            // Try to treat it as a number and add 1 (e.g., 3 -> 4)
                                            int numericId = Integer.parseInt(finalId);
                                            finalId = String.valueOf(numericId + 1);
                                        } catch (NumberFormatException ex) {
                                            // If it's text, append a dash and a number (e.g., ITEM -> ITEM-1)
                                            finalId = originalId + "-" + textSuffix;
                                            textSuffix++;
                                        }
                                    }
                                }
                                // Keep track if we had to change the ID
                                if (!finalId.equals(originalId)) {
                                    autoChangedCount++;
                                }

                                // Exclude the "Total Value" calculated column (index 5) when reading back
                                Product p = new Product(finalId, vals[1], vals[2], 
                                    Integer.parseInt(vals[3]), Double.parseDouble(vals[4].replace("₹", "")), 
                                    vals[6], vals[7], vals[8], Integer.parseInt(vals[9].replace(" Days", "").trim()));
                                db.addProduct(p);
                                importCount++;
                            } catch (Exception ex) {
                                // Skip malformed rows silently to continue importing
                            }
                        }
                    }
                    autoSaveDatabase();
                    refreshAll();
                    
                    // Show a detailed summary of what happened
                    String resultMessage = "Successfully imported " + importCount + " products!";
                    if (autoChangedCount > 0) {
                        resultMessage += "\nNote: " + autoChangedCount + " items were given new, auto-incremented IDs to prevent duplicates.";
                    }
                    JOptionPane.showMessageDialog(this, resultMessage);
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error reading file format.", "Import Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Barcode Generation Logic
        barcodeBtn.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                Product p = db.getProduct(modelRow);
                
                final JDialog barcodeDialog = new JDialog(this, "Product Barcode: " + p.getId(), true);
                
                final JPanel drawPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        
                        // Use dynamic theme colors instead of hardcoded White/Black
                        g2d.setColor(PANEL_BG); 
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                        g2d.setColor(TEXT_COLOR); 
                        
                        byte[] bytes = p.getId().getBytes();
                        
                        // Calculate total barcode lines width first to center
                        int totalBarcodeWidth = 0;
                        for (byte b : bytes) {
                            for (char c : String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0').toCharArray()) {
                                totalBarcodeWidth += (c == '1' ? 4 : 2) + 2;
                            }
                        }
                        
                        // Dynamically set the start position to center the barcode lines
                        int startX = (getWidth() - totalBarcodeWidth) / 2;
                        int x = startX; 
                        for (byte b : bytes) {
                            String bin = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                            for (char c : bin.toCharArray()) {
                                int lineWidth = (c == '1') ? 4 : 2;
                                g2d.fillRect(x, 15, lineWidth, 65); 
                                x += lineWidth + 2; 
                            }
                        }
                        
                        // Draw the ID text centered at the bottom
                        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                        g2d.drawString(p.getId(), (getWidth() - g2d.getFontMetrics().stringWidth(p.getId())) / 2, 95);
                    }
                };

                // The Save Button & Image Exporter
                JPanel btnPanel = new JPanel();
                // Make the button panel background match the theme
                btnPanel.setBackground(PANEL_BG); 
                JButton saveBtn = styleButton("Save as PNG", new Color(46, 204, 113), Color.WHITE);
                
                saveBtn.addActionListener(saveEvent -> {
                    try {
                        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = image.createGraphics();
                        drawPanel.paint(g2); 
                        g2.dispose();
                        String filename = "Barcode_" + p.getId() + ".png";
                        File outputFile = new File(filename);
                        javax.imageio.ImageIO.write(image, "png", outputFile);
                        JOptionPane.showMessageDialog(barcodeDialog, "Barcode saved successfully as:\n" + outputFile.getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(barcodeDialog, "Failed to save barcode image.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                btnPanel.add(saveBtn);
                
                barcodeDialog.setLayout(new BorderLayout());
                barcodeDialog.add(drawPanel, BorderLayout.CENTER);
                barcodeDialog.add(btnPanel, BorderLayout.SOUTH);
                
                // Compact Dialog size and center it
                barcodeDialog.setSize(220, 160); 
                barcodeDialog.setLocationRelativeTo(this);
                barcodeDialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the table to view its barcode.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        // Add all panels to the main panel
        panel.add(metricsPanel, BorderLayout.NORTH); panel.add(centerWrapper, BorderLayout.CENTER); panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createSupplyChainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PRIMARY_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setName("Card");
        alertsPanel.setBackground(PANEL_BG);
        alertsPanel.setBorder(BorderFactory.createTitledBorder("Live Supply Chain Alerts"));
        JTextArea alertArea = new JTextArea(); alertArea.setEditable(false);
        alertsPanel.add(new JScrollPane(alertArea), BorderLayout.CENTER);

        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.setName("Card");
        logsPanel.setBackground(PANEL_BG);
        logsPanel.setBorder(BorderFactory.createTitledBorder("Transaction Activity Log"));
        logArea = new JTextArea(); logArea.setEditable(false);
        logsPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // SMART RESTOCK CALCULATOR
        JButton orderBtn = styleButton("Send Restock Order", new Color(46, 204, 113), Color.WHITE);
        orderBtn.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow); 
                Product p = db.getProduct(modelRow);
                
                // Target optimal stock is 100 units. Calculate deficit.
                int optimalStock = 100;
                int recommendedOrder = optimalStock - p.getQuantity();
                if (recommendedOrder <= 0) recommendedOrder = 20; // Default minimum bulk order

                // Ask the manager, providing the AI recommendation
                String orderQtyStr = JOptionPane.showInputDialog(this, 
                    "Low stock detected for: " + p.getName() + "\n" +
                    "Current Stock: " + p.getQuantity() + "\n\n" +
                    "System Recommended Order Qty to reach optimal levels:", recommendedOrder);

                if (orderQtyStr != null && orderQtyStr.matches("\\d+")) {
                    int finalOrderQty = Integer.parseInt(orderQtyStr);
                    p.setSupplierStatus("Ordered");
                    
                    // Log the specific financial amount ordered for the audit trail
                    db.logActivity("Sent Purchase Order to " + p.getSupplierName() + " for " + finalOrderQty + " units of " + p.getName());
                    autoSaveDatabase(); 
                    refreshAll();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Purchase Order for " + finalOrderQty + " units sent to " + p.getSupplierName() + ".\n" +
                        "Expected Arrival in: " + p.getLeadTimeDays() + " days.", 
                        "Order Dispatched", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the Inventory Dashboard first.", "Action Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Fully Closes the Loop for Orders
        JButton receiveBtn = styleButton("Mark Order Received", new Color(155, 89, 182), Color.WHITE);
        receiveBtn.addActionListener(e -> {
            int viewRow = inventoryTable.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                Product p = db.getProduct(modelRow);
                
                if (p.getSupplierStatus().equals("Ordered") || p.getSupplierStatus().equals("Delayed")) {
                    String receivedQty = JOptionPane.showInputDialog(this, "Order arrived! Enter quantity received:", "100");
                    if (receivedQty != null && receivedQty.matches("\\d+")) {
                        p.setQuantity(p.getQuantity() + Integer.parseInt(receivedQty)); // Adds physical stock
                        p.setSupplierStatus("Delivered"); // Resets status
                        db.logActivity("Received " + receivedQty + " units of " + p.getName() + " from " + p.getSupplierName());
                        autoSaveDatabase();
                        refreshAll();
                        JOptionPane.showMessageDialog(this, "Stock physically added to inventory!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "This item is not currently waiting on an active order.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select an ordered item from the Inventory Dashboard first.");
            }
        });

        JButton refreshBtn = styleButton("Refresh Reports", HEADER_COLOR, Color.WHITE);
        refreshBtn.addActionListener(e -> {
            alertArea.setText(""); // Clear old alerts
            boolean hasAlerts = false;
            
            for (Product p : db.getAllProducts()) {
                // 1. Alert for Low Stock
                if (p.getQuantity() < 20) {
                    alertArea.append("⚠️ LOW STOCK: " + p.getName() + " (Only " + p.getQuantity() + " left) - Supplier: " + p.getSupplierName() + "\n");
                    hasAlerts = true;
                }
                // 2. Alert for Pending Deliveries
                if (p.getSupplierStatus().equalsIgnoreCase("Ordered") || p.getSupplierStatus().equalsIgnoreCase("Delayed")) {
                    alertArea.append("🚚 IN TRANSIT: Waiting on delivery for " + p.getName() + " from " + p.getSupplierName() + ".\n");
                    hasAlerts = true;
                }
            }
            
            // 3. Show an "All Clear" message if everything is perfect (prevents it from looking broken!)
            if (!hasAlerts) {
                alertArea.append("✅ All systems normal. Stock levels are healthy and there are no pending deliveries.");
            }
            
            updateLogView();
        });

        // ADDED THIS BRAND NEW BUTTON 
        JButton auditBtn = styleButton("Export Audit Log", new Color(52, 152, 219), Color.WHITE);
        auditBtn.addActionListener(e -> {
            try {
                File logFile = new File("Audit_Log_" + loggedInUser + ".txt");
                PrintWriter pw = new PrintWriter(new java.io.FileWriter(logFile));
                pw.println("=== OFFICIAL AUDIT LOG ===");
                pw.println("User Account: " + loggedInUser);
                pw.println("Export Date: " + java.time.LocalDateTime.now());
                pw.println("==========================");
                for(String log : db.getLogs()) { pw.println(log); }
                pw.close();
                JOptionPane.showMessageDialog(this, "Security Audit Log exported as:\n" + logFile.getName());
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export audit log.");
            }
        });

        JButton emailAlertBtn = styleButton("Email Alert Report", new Color(231, 76, 60), Color.WHITE);
        emailAlertBtn.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            for(Product p : db.getAllProducts()) {
                if(p.getQuantity() < 20) {
                    report.append("- ").append(p.getName()).append(" (ID: ").append(p.getId())
                          .append(") | Qty: ").append(p.getQuantity()).append(" | Supplier: ")
                          .append(p.getSupplierName()).append("\r\n");
                }
            }
            if(report.length() == 0) {
                JOptionPane.showMessageDialog(this, "Stock levels are healthy. No email needed.");
                return;
            }

            String userEmail = db.getUserDetails(loggedInUser)[2];
            String sysEmail = "23rudragupta@gmail.com"; 
            String sysAppPass = "zumz nzae vhjr lmwo";

            emailAlertBtn.setText("Sending..."); emailAlertBtn.setEnabled(false);
            new Thread(() -> {
                boolean success = EmailService.sendLowStockAlert(userEmail, report.toString(), sysEmail, sysAppPass);
                SwingUtilities.invokeLater(() -> {
                    emailAlertBtn.setText("Email Alert Report"); emailAlertBtn.setEnabled(true);
                    if(success) JOptionPane.showMessageDialog(this, "Low stock report emailed to " + userEmail);
                    else JOptionPane.showMessageDialog(this, "Failed to send email.", "Error", JOptionPane.ERROR_MESSAGE);
                });
            }).start();
        });

        JPanel bottomWrapper = new JPanel(new GridLayout(1, 5, 10, 0));
        bottomWrapper.setBackground(PRIMARY_BG);
        bottomWrapper.add(orderBtn); bottomWrapper.add(receiveBtn); bottomWrapper.add(auditBtn); bottomWrapper.add(emailAlertBtn); bottomWrapper.add(refreshBtn);

        JPanel centerSplit = new JPanel(new GridLayout(2, 1, 10, 10));
        centerSplit.add(alertsPanel); centerSplit.add(logsPanel);

        panel.add(centerSplit, BorderLayout.CENTER);
        panel.add(bottomWrapper, BorderLayout.SOUTH);
        return panel;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        
        PRIMARY_BG = isDarkMode ? new Color(30, 30, 30) : new Color(245, 247, 250); 
        PANEL_BG = isDarkMode ? new Color(45, 45, 48) : Color.WHITE;               
        TEXT_COLOR = isDarkMode ? new Color(220, 220, 220) : Color.BLACK;          
        Color FIELD_BG = isDarkMode ? new Color(60, 63, 65) : Color.WHITE;         
        Color BORDER_COLOR = isDarkMode ? new Color(80, 80, 80) : new Color(200, 200, 200);

        UIManager.put("TabbedPane.background", PANEL_BG);
        UIManager.put("TabbedPane.foreground", TEXT_COLOR);
        UIManager.put("TabbedPane.contentAreaColor", PRIMARY_BG);
        UIManager.put("TabbedPane.selected", FIELD_BG);

        if (tabbedPane != null) {
            tabbedPane.setBackground(PANEL_BG);
            tabbedPane.setForeground(TEXT_COLOR);
        }        

        getContentPane().setBackground(PRIMARY_BG);
        
        SwingUtilities.updateComponentTreeUI(this); 
        
        applyDeepThemeColors(this, PRIMARY_BG, PANEL_BG, TEXT_COLOR, FIELD_BG, BORDER_COLOR);
        
        inventoryTable.setBackground(FIELD_BG);
        inventoryTable.setForeground(TEXT_COLOR);
        inventoryTable.setGridColor(BORDER_COLOR);
        inventoryTable.getTableHeader().setBackground(isDarkMode ? new Color(60, 63, 65) : HEADER_COLOR);
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        
        if(chartPanel != null) {
            chartPanel.setBackground(PANEL_BG);
            chartPanel.setDarkMode(isDarkMode);
        }
        if(lineChartPanel != null) {
            lineChartPanel.setBackground(PANEL_BG);
            lineChartPanel.setDarkMode(isDarkMode);
        }
        if(pieChartPanel != null) {
            pieChartPanel.setBackground(PANEL_BG);
            pieChartPanel.setDarkMode(isDarkMode);
        }
        if (posPanel != null) {
            posPanel.setDarkMode(isDarkMode); 
        }
        
        refreshAll();
    }

private void applyDeepThemeColors(Container container, Color primary, Color panel, Color text, Color fieldBg, Color border) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                p.setBackground(p.getName() != null && p.getName().equals("Card") ? panel : primary);
                
                if (p.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder tb = (javax.swing.border.TitledBorder) p.getBorder();
                    tb.setTitleColor(text); 
                    tb.setBorder(BorderFactory.createLineBorder(border)); 
                    p.repaint();
                }
            } 
            else if (c instanceof JLabel) {
                if ("Card".equals(c.getName())) {
                    c.setBackground(panel);
                    ((JComponent)c).setBorder(BorderFactory.createLineBorder(border, 1, true));
                }
                
                // Checks if text is null before trying to read it!
                String lblText = ((JLabel)c).getText();
                if (lblText == null || !lblText.startsWith("<html>")) {
                    c.setForeground(text);
                }
            } 
            // Handles both CheckBoxes and our new Toggle Button 
            else if (c instanceof JToggleButton) { 
                JToggleButton tb = (JToggleButton) c;
                
                // Only change the background to the dark panel color if the button is OFF
                if (!tb.isSelected()) {
                    tb.setBackground(panel); 
                }
                
                // If it's a standard checkbox (like your old ones), update the text color too
                if (c instanceof JCheckBox) {
                    c.setForeground(text);
                }
            }
            else if (c instanceof JComboBox) {
                c.setBackground(fieldBg);
                c.setForeground(text);
            }
            else if (c instanceof JTextField || c instanceof JPasswordField || c instanceof JTextArea) {
                if ("TransparentText".equals(c.getName())) {
                    c.setBackground(panel);
                    c.setForeground(text);
                } else {
                    c.setBackground(fieldBg);
                    c.setForeground(text);
                    if (c instanceof JTextArea) {
                        ((JTextArea)c).setCaretColor(text);
                    } else {
                        ((JTextField)c).setCaretColor(text);
                        ((JComponent)c).setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(border, 1),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ));
                    }
                }
            } 
            else if (c instanceof JScrollPane) {
                c.setBackground(primary);
                ((JScrollPane)c).getViewport().setBackground(primary);
                ((JScrollPane)c).setBorder(BorderFactory.createLineBorder(border));
            }
            
            if (c instanceof Container) {
                applyDeepThemeColors((Container) c, primary, panel, text, fieldBg, border);
            }
        }
    }

    private JLabel createMetricCard(String title, String value) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setOpaque(true); 
        label.setBackground(PANEL_BG);
        label.setName("Card"); 
        // Set a preferred size to ensure the layout manager gives it space
        label.setPreferredSize(new Dimension(200, 100));
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        return label;
    }

    private void refreshAll() {
        refreshTable(db.getAllProducts());
        updateMetrics(); updateLogView();
        if(chartPanel != null) chartPanel.setData(db.getCategoryDistribution());
        if(lineChartPanel != null) lineChartPanel.setData(db.getCategoryDistribution());
        if(pieChartPanel != null) pieChartPanel.setData(db.getCategoryDistribution());
        if(posPanel != null) posPanel.refreshCatalog();
    }

    private void refreshTable(List<Product> list) {
        tableModel.setRowCount(0);
        for (Product p : list) {
            // Calculates the Total Financial Value per row
            double totalRowValue = p.getQuantity() * p.getPrice();
            
            tableModel.addRow(new Object[]{
                p.getId(), 
                p.getName(), 
                p.getCategory(), 
                p.getQuantity(), 
                "₹" + String.format("%.2f", p.getPrice()), 
                "₹" + String.format("%.2f", totalRowValue), 
                p.getTargetDate(), 
                p.getSupplierStatus(), 
                p.getSupplierName(), 
                p.getLeadTimeDays() + " Days"
            });
        }
    }

    private void updateMetrics() {
        if(totalItemsLabel != null) {
            String valueColor = isDarkMode ? "#e0e0e0" : "black";
            String titleColor = isDarkMode ? "#a0a0a0" : "gray";
            
            totalItemsLabel.setText("<html><div style='text-align: center; padding: 10px;'><span style='font-size: 11px; color: " + titleColor + ";'>Total Products</span><br><span style='font-size: 24px; font-weight: bold; color:" + valueColor + ";'>" + db.getAllProducts().size() + "</span></div></html>");
            totalValueLabel.setText("<html><div style='text-align: center; padding: 10px;'><span style='font-size: 11px; color: " + titleColor + ";'>Inventory Value</span><br><span style='font-size: 24px; font-weight: bold; color: #2ecc71;'>₹" + String.format("%.2f", db.calculateTotalValue()) + "</span></div></html>");
            lowStockLabel.setText("<html><div style='text-align: center; padding: 10px;'><span style='font-size: 11px; color: " + titleColor + ";'>Low Stock Alerts</span><br><span style='font-size: 24px; font-weight: bold; color: #e74c3c;'>" + db.getLowStockCount() + "</span></div></html>");
        }
    }

    private void updateLogView() {
        if (logArea != null) {
            logArea.setText("");
            for (String log : db.getLogs()) { logArea.append(log + "\n"); }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}