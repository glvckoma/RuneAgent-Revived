/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.runeagent.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.silabsoft.runeagent.RuneAgent;
import org.silabsoft.runeagent.event.RuneAgentEvent;
import org.silabsoft.runeagent.event.RuneAgentEventTypes;
import org.silabsoft.runeagent.event.TransformerEvent;
import org.silabsoft.runeagent.listener.RuneAgentEventListener;

/**
 *
 * @author unsignedbyte
 */
public class AgentFrameGeneric extends javax.swing.JFrame implements RuneAgentEventListener, AgentFrame {

    /**
     * Creates new form AgentFrame
     */
    // Flag to track if Settings tab has been added
    private boolean settingsTabAdded = false;
    
    // References to panels for cross-communication
    private GenericOutStreamPanel outStreamPanel;
    private ScriptsPanel scriptsPanel;
    
    // Update checker components
    private JButton checkUpdateButton;
    private JLabel versionLabel;
    
    // Track dark mode state
    private boolean isDarkMode = false;
    
    // Store all log messages for rebuilding the log pane when theme changes
    private static class LogEntry {
        private final String timestamp;
        private final String message;
        
        public LogEntry(String timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final java.util.List<LogEntry> logEntries = new java.util.ArrayList<>();
    
    public AgentFrameGeneric() {
        initComponents();
        
        // Add version label and update button to settings panel
        setupUpdateComponents();
        
        // Add Settings tab at the end
        jTabbedPane1.addTab("Settings", settingsPanel);
        settingsTabAdded = true;
    }
    
    /**
     * Sets up the version label and update checker button
     */
    private void setupUpdateComponents() {
        // Create version label
        versionLabel = new JLabel("Current Version: " + RuneAgent.VERSION);
        versionLabel.setBounds(20, 60, 200, 25);
        settingsPanel.add(versionLabel);
        
        // Create update checker button
        checkUpdateButton = new JButton("Check for Updates");
        checkUpdateButton.setBounds(20, 90, 150, 25);
        checkUpdateButton.addActionListener(evt -> checkForUpdates());
        settingsPanel.add(checkUpdateButton);
    }
    
    /**
     * Checks for updates from the GitHub repository
     */
    private void checkForUpdates() {
        new Thread(() -> {
            try {
                logString("Checking for updates...");
                
                // GitHub API URL for the latest release
                String apiUrl = "https://api.github.com/repos/glvckoma/RuneAgent-Revived/releases/latest";
                
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                
                // Check if the request was successful
                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse the response to get the latest version
                    String responseStr = response.toString();
                    
                    // Extract the tag_name (version) from the response
                    int tagStart = responseStr.indexOf("\"tag_name\":\"") + 12;
                    int tagEnd = responseStr.indexOf("\"", tagStart);
                    
                    if (tagStart > 12 && tagEnd > tagStart) {
                        String latestVersion = responseStr.substring(tagStart, tagEnd);
                        
                        // Remove 'v' prefix if present
                        if (latestVersion.startsWith("v")) {
                            latestVersion = latestVersion.substring(1);
                        }
                        
                        // Compare versions
                        try {
                            double currentVersion = RuneAgent.VERSION;
                            double latestVersionNum = Double.parseDouble(latestVersion);
                            
                            if (latestVersionNum > currentVersion) {
                                // New version available
                                int option = JOptionPane.showConfirmDialog(
                                    this,
                                    "A new version (" + latestVersion + ") is available!\nWould you like to visit the download page?",
                                    "Update Available",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                
                                if (option == JOptionPane.YES_OPTION) {
                                    // Open the GitHub releases page
                                    Desktop.getDesktop().browse(new URI(RuneAgent.GITHUB_REPO + "/releases/latest"));
                                }
                            } else {
                                // Already on the latest version
                                JOptionPane.showMessageDialog(
                                    this,
                                    "You are already using the latest version (" + currentVersion + ").",
                                    "No Updates Available",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        } catch (NumberFormatException e) {
                            logString("Error parsing version number: " + e.getMessage());
                        }
                    } else {
                        logString("Could not find version information in the response");
                    }
                } else {
                    logString("Failed to check for updates. Response code: " + connection.getResponseCode());
                }
                
                connection.disconnect();
                
            } catch (Exception e) {
                logString("Error checking for updates: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Sets the OutStreamPanel reference to access the script area
     */
    public void setOutStreamPanel(GenericOutStreamPanel panel) {
        this.outStreamPanel = panel;
        
        // Create and add Scripts tab if outStreamPanel is available
        if (outStreamPanel != null) {
            this.scriptsPanel = new ScriptsPanel(outStreamPanel.getScriptArea());
            
            // Add Scripts tab before Settings tab
            int settingsIndex = jTabbedPane1.indexOfTab("Settings");
            jTabbedPane1.insertTab("Scripts", null, this.scriptsPanel, "Manage saved scripts", settingsIndex);
            
            // Give the OutStreamPanel a reference to the ScriptsPanel
            this.outStreamPanel.setScriptsPanel(this.scriptsPanel);
        }
    }
    
    /**
     * Gets the ScriptsPanel reference
     */
    public ScriptsPanel getScriptsPanel() {
        return this.scriptsPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        darkModeToggle = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RuneAgent-Revived Ver. "+RuneAgent.VERSION+" by glvckoma (GitHub)");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Rune Agent Log:"));

        jTextPane1.setEditable(false); // Prevent typing in Rune Agent Log
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        darkModeToggle.setText("Enable Dark Mode");
        darkModeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                darkModeToggleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(darkModeToggle)
                .addContainerGap(607, Short.MAX_VALUE))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(darkModeToggle)
                .addContainerGap(328, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Startup", jPanel1);
        // Settings tab will be added at the end of initialization

        jMenu1.setText("GitHub");
        jMenu1.setToolTipText("Visit the GitHub repository");
        jMenu1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jMenu1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jMenu1MouseExited(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 742, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 742, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URL(org.silabsoft.runeagent.RuneAgent.GITHUB_REPO).toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jMenu1MouseClicked

    private void darkModeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_darkModeToggleActionPerformed
        this.isDarkMode = darkModeToggle.isSelected();
        
        // Use the ThemeManager to apply the theme
        String result = ThemeManager.applyTheme(this, isDarkMode);
        
        // Update button text based on current mode
        darkModeToggle.setText(isDarkMode ? "Enable Light Mode" : "Enable Dark Mode");
        
        // Directly apply syntax highlighting to any RSyntaxTextArea components
        if (scriptsPanel != null && scriptsPanel.getScriptArea() != null) {
            ThemeManager.applySyntaxHighlighting(scriptsPanel.getScriptArea(), isDarkMode);
        }
        
        // Apply to the outStreamPanel's script area if available
        if (outStreamPanel != null && outStreamPanel.getScriptArea() != null) {
            ThemeManager.applySyntaxHighlighting(outStreamPanel.getScriptArea(), isDarkMode);
        }
        
        // Apply theme to the Rune Agent Log JTextPane
        ThemeManager.applyTextPaneTheme(jTextPane1, isDarkMode);
        
        // Update the styles in the JTextPane for proper coloring in the current theme
        updateLogStyles(isDarkMode);
        
        // Rebuild the log pane with the new theme
        rebuildLogPane();
        
        // Log the theme change message (add to log entries and display)
        logString(result);
    }//GEN-LAST:event_darkModeToggleActionPerformed
    
    /**
     * Rebuilds the entire log pane with the current theme
     */
    private void rebuildLogPane() {
        // Clear the text pane
        jTextPane1.setText("");
        
        // Re-add all messages with current styling
        for (LogEntry entry : logEntries) {
            addStyledLogMessage(entry.getTimestamp(), entry.getMessage());
        }
    }
    
    /**
     * Updates the text styles in the log JTextPane based on the current theme
     * 
     * @param isDarkMode Whether dark mode is enabled
     */
    private void updateLogStyles(boolean isDarkMode) {
        // Create styles with different colors
        javax.swing.text.Style defaultStyle = jTextPane1.getStyle("Default");
        if (defaultStyle == null) {
            defaultStyle = jTextPane1.addStyle("Default", null);
        }
        javax.swing.text.StyleConstants.setForeground(defaultStyle, 
            isDarkMode ? new java.awt.Color(230, 230, 230) : java.awt.Color.BLACK);
        
        // The other styles (Event, Transformer, Found, Error) are handled by ThemeManager.applyTextPaneTheme
    }
    
    private void jMenu1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseEntered
        // Change appearance on hover
        jMenu1.setForeground(java.awt.Color.BLUE);
        jMenu1.setFont(jMenu1.getFont().deriveFont(java.awt.Font.BOLD));
    }//GEN-LAST:event_jMenu1MouseEntered

    private void jMenu1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseExited
        // Restore original appearance
        jMenu1.setForeground(null); // Reset to default color
        jMenu1.setFont(jMenu1.getFont().deriveFont(java.awt.Font.PLAIN));
    }//GEN-LAST:event_jMenu1MouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AgentFrameGeneric.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AgentFrameGeneric.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AgentFrameGeneric.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AgentFrameGeneric.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AgentFrameGeneric().setVisible(true);
            }
        });
    }

    public void logString(String o) {
        Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        String timestamp = "[" + currentTimestamp + "] - ";
        
        // Store the message in our list
        logEntries.add(new LogEntry(timestamp, o));
        
        // Add the styled message to the text pane
        addStyledLogMessage(timestamp, o);
    }
    
    /**
     * Adds a styled message to the text pane
     * 
     * @param timestamp The timestamp string
     * @param message The message to add
     */
    private void addStyledLogMessage(String timestamp, String message) {
        // Create a styled document
        javax.swing.text.StyledDocument doc = jTextPane1.getStyledDocument();
        
        // Create styles with different colors based on current theme
        javax.swing.text.Style defaultStyle = jTextPane1.addStyle("Default", null);
        javax.swing.text.StyleConstants.setForeground(defaultStyle, 
            isDarkMode ? new java.awt.Color(230, 230, 230) : java.awt.Color.BLACK);
        
        javax.swing.text.Style eventStyle = jTextPane1.addStyle("Event", null);
        javax.swing.text.StyleConstants.setForeground(eventStyle, 
            isDarkMode ? new java.awt.Color(100, 255, 100) : new java.awt.Color(0, 128, 0)); // Green for events
        
        javax.swing.text.Style transformerStyle = jTextPane1.addStyle("Transformer", null);
        javax.swing.text.StyleConstants.setForeground(transformerStyle, 
            isDarkMode ? new java.awt.Color(100, 100, 255) : new java.awt.Color(0, 0, 200)); // Blue for transformers
        
        javax.swing.text.Style foundStyle = jTextPane1.addStyle("Found", null);
        javax.swing.text.StyleConstants.setForeground(foundStyle, 
            isDarkMode ? new java.awt.Color(200, 100, 255) : new java.awt.Color(128, 0, 128)); // Purple for "Found" messages
        
        javax.swing.text.Style errorStyle = jTextPane1.addStyle("Error", null);
        javax.swing.text.StyleConstants.setForeground(errorStyle, 
            isDarkMode ? new java.awt.Color(255, 100, 100) : java.awt.Color.RED); // Red for errors
        
        try {
            // Determine the style based on the content
            javax.swing.text.Style style = defaultStyle;
            
            if (message.contains("AGENT_") || message.contains("_ADDED")) {
                style = eventStyle;
            } else if (message.startsWith("Transformer =>")) {
                if (message.contains("Error") || message.contains("Failed") || message.contains("Exception")) {
                    style = errorStyle;
                } else if (message.contains("Found:")) {
                    style = foundStyle;
                } else {
                    style = transformerStyle;
                }
            } else if (message.contains("Error") || message.contains("Failed") || message.contains("Exception")) {
                style = errorStyle;
            }
            
            // Insert the timestamp with default style
            doc.insertString(doc.getLength(), timestamp, defaultStyle);
            
            // Insert the message with the appropriate style
            doc.insertString(doc.getLength(), message + "\n", style);
            
            // Scroll to the bottom
            jTextPane1.setCaretPosition(doc.getLength());
        } catch (javax.swing.text.BadLocationException e) {
            // Fallback to simple append if styling fails
            System.err.println("Error styling log message: " + e.getMessage());
            jTextPane1.setText(jTextPane1.getText() + timestamp + message + "\n");
            jTextPane1.setCaretPosition(jTextPane1.getText().length());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton darkModeToggle;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onRuneAgentEvent(RuneAgentEvent e) {
        if (e.getType().equals(RuneAgentEventTypes.TRANSFORMER_EVENT)) {
            logString(((TransformerEvent) e).toString());
        } else {
            logString(e.getType() + " ");
        }
    }

    @Override
    public void addTab(Component component, String title) {
        if (settingsTabAdded) {
            // If Settings tab is already added, insert the new tab before it
            int settingsIndex = jTabbedPane1.indexOfTab("Settings");
            jTabbedPane1.insertTab(title, null, component, null, settingsIndex);
        } else {
            // If Settings tab is not yet added, add the tab normally
            jTabbedPane1.addTab(title, component);
        }
    }
}
