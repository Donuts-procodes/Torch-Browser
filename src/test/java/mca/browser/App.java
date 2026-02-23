package mca.browser; // Assignment 4: User defined packages

import javax.swing.*;    // Assignment 4: In-built packages
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.Stack;
import java.time.LocalTime;

// Assignment 2a: Implement the concept of Inheritance and Abstraction
abstract class CoreBrowserWindow extends JFrame {
    abstract void navigateTo(String urlText, boolean isHistoryNavigation);
}

// Inherits from CoreBrowserWindow
public class App extends CoreBrowserWindow {

    private JTextField urlInput;     // Assignment 9: Display form boxes using swing
    private JEditorPane webContent;
    private JLabel statusText;
    private JProgressBar progressBar; // NEW: Visual speed indicator
    private String sessionUser;
    
    // History Stacks
    private Stack<String> backHistory = new Stack<>();
    private Stack<String> forwardHistory = new Stack<>();
    private String currentUrl = "";

    // Dark Theme Colors
    private Color bgDark = new Color(30, 30, 46);
    private Color bgPanel = new Color(49, 50, 68);
    private Color textLight = new Color(205, 214, 244);
    private Color accentCyan = new Color(137, 180, 250);

    public App(String sessionUser) {
        this.sessionUser = sessionUser;
        buildUI();
        runBackgroundClock();      // Assignment 5: Thread 1 (Background task)
    }

    private void buildUI() {
        // Assignment 3: Take user info as input and display it as a header
        setTitle("Midnight Explorer | User: " + sessionUser);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgDark);

        // --- Top Navigation Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20)); 
        headerPanel.setBackground(bgDark);

        // Navigation Buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navButtons.setBackground(bgDark);
        
        JButton backBtn = createStyledButton("< Back");
        JButton forwardBtn = createStyledButton("Forward >");
        JButton reloadBtn = createStyledButton("Reload");
        
        navButtons.add(backBtn);
        navButtons.add(forwardBtn);
        navButtons.add(reloadBtn);

        // Modern Dark URL Bar
        urlInput = new JTextField("http://www.google.com");
        urlInput.setFont(new Font("Consolas", Font.PLAIN, 16));
        urlInput.setBackground(bgPanel);
        urlInput.setForeground(textLight);
        urlInput.setCaretColor(accentCyan);
        urlInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentCyan, 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Action Buttons
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setBackground(bgDark);
        
        JButton extBtn = createStyledButton("Force Open Externally");
        extBtn.setForeground(new Color(243, 139, 168)); // Soft red/pink
        actionButtons.add(extBtn);

        headerPanel.add(navButtons, BorderLayout.WEST);
        headerPanel.add(urlInput, BorderLayout.CENTER);
        headerPanel.add(actionButtons, BorderLayout.EAST);

        // --- Main Content Area ---
        webContent = new JEditorPane();
        webContent.setEditable(false);
        webContent.setBackground(new Color(24, 24, 37));
        webContent.setForeground(textLight);
        JScrollPane scrollPane = new JScrollPane(webContent);
        scrollPane.setBorder(BorderFactory.createLineBorder(bgPanel, 2)); 

        // --- Bottom Status Bar ---
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(bgDark);
        statusPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
        
        // Assignment 8: AWT program that displays simple message with font style and color
        statusText = new JLabel("System Ready.");
        statusText.setFont(new Font("Consolas", Font.BOLD | Font.ITALIC, 13));
        statusText.setForeground(accentCyan);
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setBackground(bgPanel);
        progressBar.setForeground(accentCyan);
        progressBar.setBorderPainted(false);

        statusPanel.add(statusText, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        // Assignment 10: Handling Mouse events
        backBtn.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { goBack(); }});
        forwardBtn.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { goForward(); }});
        reloadBtn.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { navigateTo(currentUrl, true); }});
        extBtn.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { openInSystemBrowser(currentUrl.isEmpty() ? urlInput.getText() : currentUrl); }});

        // Assignment 11: Handling Key events (Enter to Search)
        urlInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    navigateTo(urlInput.getText(), false);
                }
            }
        });
    }

    // Helper for sleek developer-style buttons
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 14));
        btn.setForeground(accentCyan);
        btn.setBackground(bgPanel);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(accentCyan); btn.setForeground(bgDark); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bgPanel); btn.setForeground(accentCyan); }
        });
        return btn;
    }

    private void goBack() {
        if (!backHistory.isEmpty()) {
            forwardHistory.push(currentUrl);
            String prevUrl = backHistory.pop();
            urlInput.setText(prevUrl);
            navigateTo(prevUrl, true);
        }
    }

    private void goForward() {
        if (!forwardHistory.isEmpty()) {
            backHistory.push(currentUrl);
            String nextUrl = forwardHistory.pop();
            urlInput.setText(nextUrl);
            navigateTo(nextUrl, true);
        }
    }

    private void openInSystemBrowser(String urlText) {
        try { Desktop.getDesktop().browse(new URI(urlText)); } 
        catch (Exception ex) { statusText.setText("Failed to route to system browser."); }
    }

    // Assignment 2b: Method overloading (Takes a String)
    @Override
    public void navigateTo(String urlText, boolean isHistoryNavigation) {
        statusText.setText("Establishing Connection...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true); // Makes it look busy and fast
        
        // Assignment 5: Multi-thread application (Thread 2 - Network Loader)
        Thread networkThread = new Thread(() -> {
            try {
                // Assignment 7: Demonstrate exception handling
                if (!urlText.startsWith("http")) {
                    throw new IllegalArgumentException("Protocol missing. Use http:// or https://");
                }
                URL targetUrl = new URL(urlText);
                webContent.setPage(targetUrl);
                saveHistoryLogs(urlText);
                
                SwingUtilities.invokeLater(() -> {
                    if (!isHistoryNavigation && !currentUrl.isEmpty()) {
                        backHistory.push(currentUrl);
                        forwardHistory.clear(); 
                    }
                    currentUrl = urlText;
                    statusText.setText("Payload rendered: " + urlText);
                    progressBar.setVisible(false);
                });
            } catch (IOException | IllegalArgumentException ex) {
                SwingUtilities.invokeLater(() -> {
                    statusText.setText("Render Failed.");
                    progressBar.setVisible(false);
                    int choice = JOptionPane.showConfirmDialog(this, 
                        "Swing engine cannot parse this modern payload.\nRoute to system browser?", 
                        "Render Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if(choice == JOptionPane.YES_OPTION) openInSystemBrowser(urlText);
                });
            }
        });
        networkThread.start();
    }

    // Assignment 2b: Method overloading
    public void navigateTo(URL urlObject) {
        navigateTo(urlObject.toString(), false);
    }

    // Assignment 6: Demonstrate concept of byte stream and character stream
    private void saveHistoryLogs(String visitedUrl) {
        try (FileWriter charWriter = new FileWriter("browser_history.txt", true);
             BufferedWriter buffWriter = new BufferedWriter(charWriter)) {
            buffWriter.write(visitedUrl + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        try (FileOutputStream byteStream = new FileOutputStream("system_flags.dat", true)) {
            byteStream.write(("OK\n").getBytes());
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Assignment 5: Multi-thread application (Thread 3 - Background Timer)
    private void runBackgroundClock() {
        Thread timerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) { break; }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    public static void main(String[] args) {
        // Assignment 1a: Display the text content
        System.out.println("Initializing Midnight Engine...");
        
        // Assignment 1b: Use of Scanner class which takes input from the user
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("Authorize session for user: ");
        String nameInput = inputScanner.nextLine();
        
        SwingUtilities.invokeLater(() -> {
            App webApp = new App(nameInput);
            webApp.setVisible(true);
        });
    }
}