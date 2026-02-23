package mca.browser; // Assignment 4: User defined packages

import javax.swing.*;    // Assignment 4: In-built packages
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.time.LocalTime;

// Modern Web Engine Imports
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

// Assignment 2a: Implement the concept of Inheritance and Abstraction
abstract class CoreBrowserWindow extends JFrame {
    abstract void navigateTo(String urlText);
}

// Inherits from CoreBrowserWindow
public class App extends CoreBrowserWindow {

    private String sessionUser;
    private JTabbedPane tabbedPane;
    private boolean isAddingTab = false; 
    private boolean isGhostMode = false;

    // MODERN TORCH THEME: Charcoal & Neon Ember
    private final Color torchDark = new Color(18, 18, 18);      
    private final Color torchToolbar = new Color(30, 30, 30);   
    private final Color torchText = new Color(230, 230, 230);   
    private final Color torchAccent = new Color(255, 69, 0);    
    private final Color torchBorder = new Color(50, 50, 50);    

    public App(String sessionUser) {
        this.sessionUser = sessionUser;
        buildUI();
        runBackgroundSync();      // Assignment 5: Thread 1 (Background task)
    }

    private void buildUI() {
        // Assignment 3: Take user info as input and display it as a header
        setTitle("Torch Browser | Profile: " + sessionUser);
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(torchDark);

        UIManager.put("TabbedPane.background", torchDark);
        UIManager.put("TabbedPane.foreground", torchText);
        UIManager.put("TabbedPane.selected", torchToolbar);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);

        // Assignment 9: Display form boxes using swing
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(torchDark);
        tabbedPane.setFocusable(false);

        tabbedPane.addTab("+", new JLabel()); 
        
        isAddingTab = true;
        createNewTab("New Tab");
        isAddingTab = false;

        tabbedPane.addChangeListener(e -> {
            if (!isAddingTab && tabbedPane.getSelectedIndex() == tabbedPane.getTabCount() - 1) {
                isAddingTab = true; 
                SwingUtilities.invokeLater(() -> {
                    createNewTab("New Tab");
                    isAddingTab = false; 
                });
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Custom Component for the "X" on the Tab ---
    private class TabHeader extends JPanel {
        JLabel titleLabel;
        
        public TabHeader(String title, BrowserTab tab) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            setOpaque(false);
            
            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLabel.setForeground(torchText);
            titleLabel.setPreferredSize(new Dimension(140, 22)); 

            JButton closeBtn = new JButton("X");
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            closeBtn.setForeground(new Color(120, 120, 120));
            closeBtn.setContentAreaFilled(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setMargin(new Insets(0, 0, 0, 0));
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            closeBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { closeBtn.setForeground(torchAccent); }
                public void mouseExited(MouseEvent e) { closeBtn.setForeground(new Color(120, 120, 120)); }
            });
            closeBtn.addActionListener(e -> closeTab(tab));
            
            add(titleLabel);
            add(closeBtn);
        }
    }

    private void createNewTab(String title) {
        BrowserTab newTab = new BrowserTab();
        int insertIndex = 0; // Spawn on the left
        
        tabbedPane.insertTab(title, null, newTab, null, insertIndex);
        tabbedPane.setTabComponentAt(insertIndex, new TabHeader(title, newTab));
        tabbedPane.setSelectedIndex(insertIndex);
    }

    private void closeTab(BrowserTab tab) {
        int index = tabbedPane.indexOfComponent(tab);
        if (index != -1) {
            tabbedPane.remove(index);
            Platform.runLater(() -> {
                if(tab.engine != null) {
                    tab.engine.load(null);
                    tab.engine = null;
                }
            });
            System.gc(); // Keep RAM usage low for i3 processor
        }
        if (tabbedPane.getTabCount() == 1) {
            System.exit(0);
        }
    }

    private BrowserTab getCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1 && tabbedPane.getComponentAt(index) instanceof BrowserTab) {
            return (BrowserTab) tabbedPane.getComponentAt(index);
        }
        return null;
    }

    // --- The Torch Architecture ---
    public class BrowserTab extends JPanel {
        public WebEngine engine;
        public WebView webView;
        public JTextField urlInput;
        public JLabel statusText;
        public JProgressBar progressBar;
        public JPanel bookmarksBar; 

        public BrowserTab() {
            setLayout(new BorderLayout());
            
            // Core Toolbar panel
            JPanel toolbarCore = new JPanel(new BorderLayout(10, 0));
            toolbarCore.setBackground(torchToolbar);
            toolbarCore.setBorder(new EmptyBorder(8, 12, 8, 12));

            JPanel navControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            navControls.setOpaque(false);
            
            // NO EMOJIS - Pure Text
            JButton btnBack = createToolButton("< Back");
            JButton btnFwd = createToolButton("Forward >");
            JButton btnReload = createToolButton("Refresh");
            JButton btnHome = createToolButton("Home");
            
            navControls.add(btnBack);
            navControls.add(btnFwd);
            navControls.add(btnReload);
            navControls.add(btnHome);

            urlInput = new JTextField();
            urlInput.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
            urlInput.setBackground(torchDark);
            urlInput.setForeground(torchText);
            urlInput.setCaretColor(torchAccent);
            urlInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(torchBorder, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));

            JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            rightControls.setOpaque(false);
            
            // NO EMOJIS - Pure Text
            JButton btnAddBookmark = createToolButton("[+ Bookmark]");
            JButton btnControlCenter = createToolButton("[Menu]");
            
            rightControls.add(btnAddBookmark);
            rightControls.add(btnControlCenter);

            toolbarCore.add(navControls, BorderLayout.WEST);
            toolbarCore.add(urlInput, BorderLayout.CENTER);
            toolbarCore.add(rightControls, BorderLayout.EAST);

            // --- Bookmarks Bar ---
            bookmarksBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            bookmarksBar.setBackground(torchDark);
            bookmarksBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, torchBorder));
            loadBookmarksBar(); 

            // Progress bar
            progressBar = new JProgressBar();
            progressBar.setPreferredSize(new Dimension(100, 2));
            progressBar.setBackground(torchToolbar);
            progressBar.setForeground(torchAccent);
            progressBar.setBorderPainted(false);
            progressBar.setVisible(false);

            JPanel topContainer = new JPanel(new BorderLayout());
            topContainer.add(toolbarCore, BorderLayout.NORTH);
            topContainer.add(bookmarksBar, BorderLayout.CENTER);
            topContainer.add(progressBar, BorderLayout.SOUTH);

            JFXPanel jfxPanel = new JFXPanel();
            
            // Status Bar
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            statusPanel.setBackground(torchDark);
            statusText = new JLabel(" Ready");
            statusText.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            statusText.setForeground(new Color(150, 150, 150));
            statusPanel.add(statusText);

            // Add the System Browser button directly to the status bar for unrenderable sites
            JButton btnExt = createToolButton("[Force Open in System Browser]");
            statusPanel.add(btnExt);

            add(topContainer, BorderLayout.NORTH);
            add(jfxPanel, BorderLayout.CENTER);
            add(statusPanel, BorderLayout.SOUTH);

            Platform.runLater(() -> {
                webView = new WebView();
                engine = webView.getEngine();
                engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
                jfxPanel.setScene(new Scene(webView));

                engine.titleProperty().addListener((obs, old, newTitle) -> {
                    SwingUtilities.invokeLater(() -> updateTabTitle(newTitle));
                });

                engine.locationProperty().addListener((obs, oldVal, newVal) -> {
                    SwingUtilities.invokeLater(() -> urlInput.setText(newVal != null ? newVal : ""));
                });

                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    SwingUtilities.invokeLater(() -> {
                        if (newState == Worker.State.RUNNING) {
                            progressBar.setVisible(true);
                            statusText.setText(" Connecting...");
                        } else if (newState == Worker.State.SUCCEEDED) {
                            progressBar.setVisible(false);
                            statusText.setText(" Done (If site is blank, use System Browser)");
                        } else if (newState == Worker.State.FAILED) {
                            progressBar.setVisible(false);
                            statusText.setText(" Blocked by modern web security.");
                        }
                    });
                });

                engine.getLoadWorker().workDoneProperty().addListener((obs, oldVal, newVal) -> {
                    SwingUtilities.invokeLater(() -> progressBar.setValue(newVal.intValue()));
                });

                loadTorchHome();
            });

            // Input and Buttons
            urlInput.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) navigateTo(urlInput.getText(), BrowserTab.this);
                }
            });

            btnBack.addActionListener(e -> Platform.runLater(() -> { if(engine.getHistory().getCurrentIndex() > 0) engine.getHistory().go(-1); }));
            btnFwd.addActionListener(e -> Platform.runLater(() -> { if(engine.getHistory().getCurrentIndex() < engine.getHistory().getEntries().size()-1) engine.getHistory().go(1); }));
            btnReload.addActionListener(e -> Platform.runLater(() -> engine.reload()));
            btnHome.addActionListener(e -> loadTorchHome());
            
            btnAddBookmark.addActionListener(e -> {
                saveBookmark(engine.getTitle(), urlInput.getText());
                loadBookmarksBar(); 
            });
            btnControlCenter.addActionListener(e -> openControlCenter());
            btnExt.addActionListener(e -> { 
                try { Desktop.getDesktop().browse(new java.net.URI(urlInput.getText())); } 
                catch (Exception ex) { statusText.setText(" External routing failed."); }
            });
        }

        public void loadBookmarksBar() {
            bookmarksBar.removeAll();
            JButton titleBtn = createToolButton("Saved:");
            titleBtn.setForeground(torchAccent);
            bookmarksBar.add(titleBtn);

            try (BufferedReader reader = new BufferedReader(new FileReader("torch_bookmarks.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        String url = parts[1].trim();
                        if (name.length() > 15) name = name.substring(0, 15) + "..."; 
                        
                        JButton bMarkBtn = createToolButton(name);
                        bMarkBtn.setToolTipText(url);
                        bMarkBtn.addActionListener(e -> navigateTo(url, this));
                        bookmarksBar.add(bMarkBtn);
                    }
                }
            } catch (IOException ex) {
                // Ignore if file doesn't exist yet
            }
            bookmarksBar.revalidate();
            bookmarksBar.repaint();
        }

        private void updateTabTitle(String newTitle) {
            int index = tabbedPane.indexOfComponent(this);
            if (index != -1 && tabbedPane.getTabComponentAt(index) instanceof TabHeader) {
                TabHeader header = (TabHeader) tabbedPane.getTabComponentAt(index);
                String shortTitle = (newTitle == null || newTitle.isEmpty()) ? "New Tab" : newTitle;
                if (shortTitle.length() > 20) shortTitle = shortTitle.substring(0, 20) + "...";
                header.titleLabel.setText(shortTitle);
            }
        }

        private void loadTorchHome() {
            String homeHtml = "<html><body style='background-color:#121212; color:#E0E0E0; font-family:\"Segoe UI\", sans-serif; text-align:center; padding-top:160px; margin:0;'>"
                + "<h1 style='font-size: 80px; margin-bottom: 5px; color:#FF4500; font-weight:900; letter-spacing:8px; text-shadow: 0px 0px 25px rgba(255, 69, 0, 0.8);'>TORCH</h1>"
                + "<h2 style='color:#B0B0B0; font-weight:normal; letter-spacing:2px; font-size: 18px;'>Ignite the Web.</h2>"
                + "</body></html>";
            Platform.runLater(() -> engine.loadContent(homeHtml));
            SwingUtilities.invokeLater(() -> urlInput.setText(""));
        }

        private JButton createToolButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(torchText);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setForeground(torchAccent); }
                public void mouseExited(MouseEvent e) { 
                    if(!text.equals("Saved:")) btn.setForeground(torchText); 
                }
            });
            return btn;
        }
    }

    private void openControlCenter() {
        JDialog controlCenter = new JDialog(this, "Torch Control Center", true);
        controlCenter.setSize(500, 400);
        controlCenter.setLayout(new BorderLayout());
        controlCenter.getContentPane().setBackground(torchDark);

        JTabbedPane ccTabs = new JTabbedPane();
        ccTabs.setBackground(torchDark);
        ccTabs.setForeground(torchText);
        
        // 1. History Tab
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(torchDark);
        JTextArea historyArea = new JTextArea();
        historyArea.setBackground(torchToolbar);
        historyArea.setForeground(torchText);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        try (BufferedReader reader = new BufferedReader(new FileReader("torch_history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                historyArea.append(line + "\n");
            }
        } catch (IOException ex) {
            historyArea.setText("No history found.");
        }
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        // 2. Settings Tab
        JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        settingsPanel.setBackground(torchDark);
        settingsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton btnClearHistory = new JButton("Clear Browsing Data");
        btnClearHistory.setBackground(torchToolbar);
        btnClearHistory.setForeground(new Color(255, 100, 100));
        btnClearHistory.setFocusPainted(false);
        btnClearHistory.addActionListener(e -> {
            try (PrintWriter writer = new PrintWriter("torch_history.txt")) {
                writer.print("");
                historyArea.setText("History Cleared.");
                JOptionPane.showMessageDialog(controlCenter, "Browsing Data Cleared.");
            } catch (FileNotFoundException ex) { }
        });

        JButton btnGhost = new JButton(isGhostMode ? "Ghost Mode: ON" : "Ghost Mode: OFF");
        btnGhost.setBackground(torchToolbar);
        btnGhost.setForeground(isGhostMode ? torchAccent : torchText);
        btnGhost.setFocusPainted(false);
        btnGhost.addActionListener(e -> {
            isGhostMode = !isGhostMode;
            btnGhost.setText(isGhostMode ? "Ghost Mode: ON" : "Ghost Mode: OFF");
            btnGhost.setForeground(isGhostMode ? torchAccent : torchText);
        });

        settingsPanel.add(btnClearHistory);
        settingsPanel.add(btnGhost);

        // 3. User Profile Tab
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        profilePanel.setBackground(torchDark);
        
        JLabel userLabel = new JLabel("Current Profile: ");
        userLabel.setForeground(torchText);
        JTextField userInput = new JTextField(sessionUser, 15);
        JButton btnUpdateProfile = new JButton("Update Sign-in");
        btnUpdateProfile.setBackground(torchToolbar);
        btnUpdateProfile.setForeground(torchAccent);
        
        btnUpdateProfile.addActionListener(e -> {
            sessionUser = userInput.getText();
            setTitle("Torch Browser | Profile: " + sessionUser);
            JOptionPane.showMessageDialog(controlCenter, "Profile switched to " + sessionUser);
        });

        profilePanel.add(userLabel);
        profilePanel.add(userInput);
        profilePanel.add(btnUpdateProfile);

        ccTabs.addTab("History", historyPanel);
        ccTabs.addTab("Settings", settingsPanel);
        ccTabs.addTab("User Sign-in", profilePanel);

        controlCenter.add(ccTabs, BorderLayout.CENTER);
        controlCenter.setLocationRelativeTo(this);
        controlCenter.setVisible(true);
    }

    private void saveBookmark(String title, String urlToSave) {
        if(urlToSave.isEmpty() || (!urlToSave.startsWith("http") && !urlToSave.startsWith("torch"))) {
            JOptionPane.showMessageDialog(this, "Please navigate to a valid website first.");
            return;
        }
        try (FileWriter charWriter = new FileWriter("torch_bookmarks.txt", true);
             BufferedWriter buffWriter = new BufferedWriter(charWriter)) {
            String safeTitle = (title == null || title.isEmpty()) ? "Website" : title;
            buffWriter.write(safeTitle + " | " + urlToSave + "\n");
        } catch (IOException e) { }
    }

    @Override
    public void navigateTo(String urlText) {
        BrowserTab current = getCurrentTab();
        if (current != null) navigateTo(urlText, current);
    }

    public void navigateTo(String urlText, BrowserTab activeTab) {
        if(urlText.trim().isEmpty()) return;
        
        Thread networkThread = new Thread(() -> {
            try {
                String finalUrl = urlText.trim();

                if (!finalUrl.startsWith("http") && !finalUrl.startsWith("file")) {
                    if (!finalUrl.contains(".") || finalUrl.contains(" ")) {
                        finalUrl = "https://www.google.com/search?q=" + URLEncoder.encode(finalUrl, "UTF-8");
                    } else {
                        finalUrl = "https://" + finalUrl;
                    }
                }

                // AI / Heavy Site Fallback
                if (finalUrl.contains("gemini.google.com") || finalUrl.contains("chatgpt.com") || finalUrl.contains("netflix.com") || finalUrl.contains("discord.com")) {
                    final String redirectUrl = finalUrl;
                    SwingUtilities.invokeLater(() -> {
                        activeTab.statusText.setText(" Engine unsupported. Routing to system browser...");
                        try { Desktop.getDesktop().browse(new java.net.URI(redirectUrl)); } catch (Exception ex) { }
                    });
                    return; 
                }
                
                final String targetUrl = finalUrl;
                Platform.runLater(() -> activeTab.engine.load(targetUrl));
                logHistory(targetUrl);
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Routing Error: " + ex.getMessage()));
            }
        });
        networkThread.start();
    }

    private void logHistory(String visitedUrl) {
        if (isGhostMode) return; 
        try (FileWriter charWriter = new FileWriter("torch_history.txt", true);
             BufferedWriter buffWriter = new BufferedWriter(charWriter)) {
            buffWriter.write("[" + LocalTime.now() + "] " + visitedUrl + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void triggerManualSync() {
        if (isGhostMode) return; 
        Thread syncThread = new Thread(() -> {
            try (FileOutputStream byteStream = new FileOutputStream("torch_sync.dat")) {
                String syncData = "USER:" + sessionUser + "|SYNC:" + LocalTime.now() + "\n";
                byteStream.write(syncData.getBytes());
            } catch (IOException e) { }
        });
        syncThread.start();
    }

    private void runBackgroundSync() {
        Thread backgroundSyncThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(120000); 
                    triggerManualSync();
                } catch (InterruptedException e) { break; }
            }
        });
        backgroundSyncThread.setDaemon(true);
        backgroundSyncThread.start();
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d,sw");
        System.setProperty("prism.forceGPU", "true");

        System.out.println("=========================================");
        System.out.println("IGNITING TORCH ENGINE");
        System.out.println("=========================================");
        
        try (Scanner inputScanner = new Scanner(System.in)) {
            System.out.print("Enter Torch Profile Name: ");
            String nameInput = inputScanner.nextLine();
            
            if(nameInput.trim().isEmpty()) {
                nameInput = "Ayush"; 
            }
            
            final String finalName = nameInput;
            SwingUtilities.invokeLater(() -> {
                App webApp = new App(finalName);
                webApp.setVisible(true);
            });
        }
    }
}