/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.runeagent.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

/**
 * Manages application theming with light and dark mode support
 */
public class ThemeManager {
    
    // Light theme colors
    private static final Color LIGHT_BACKGROUND = new Color(240, 240, 240);
    private static final Color LIGHT_FOREGROUND = new Color(0, 0, 0);
    private static final Color LIGHT_CONTROL = new Color(214, 217, 223);
    private static final Color LIGHT_TEXT_AREA_BG = new Color(255, 255, 255);
    private static final Color LIGHT_SELECTION_BG = new Color(57, 105, 138);
    
    // Dark theme colors
    private static final Color DARK_BACKGROUND = new Color(50, 50, 50);
    private static final Color DARK_FOREGROUND = new Color(230, 230, 230);
    private static final Color DARK_CONTROL = new Color(70, 70, 70);
    private static final Color DARK_TEXT_AREA_BG = new Color(40, 40, 40);
    private static final Color DARK_SELECTION_BG = new Color(104, 93, 156);
    
    // Syntax highlighting colors for dark mode
    private static final Color DARK_COMMENT = new Color(128, 180, 128);       // Light green
    private static final Color DARK_KEYWORD = new Color(204, 153, 255);       // Light purple
    private static final Color DARK_STRING = new Color(255, 204, 153);        // Light orange
    private static final Color DARK_NUMBER = new Color(180, 230, 255);        // Lighter blue (was 153, 204, 255)
    private static final Color DARK_OPERATOR = new Color(255, 180, 180);      // Light pink/red (was 240, 240, 240)
    private static final Color DARK_IDENTIFIER = new Color(220, 220, 220);    // Light gray
    private static final Color DARK_FUNCTION = new Color(255, 204, 204);      // Light pink
    private static final Color DARK_SEPARATOR = new Color(255, 200, 200);     // Light pink for parentheses
    
    /**
     * Applies the specified theme to the application
     * 
     * @param frame The main application frame
     * @param isDarkMode Whether to apply dark mode
     * @return A message indicating the result of the theme change
     */
    public static String applyTheme(JFrame frame, boolean isDarkMode) {
        try {
            // First reset to default look and feel to ensure clean state
            resetLookAndFeel();
            
            // Apply theme settings
            if (isDarkMode) {
                applyDarkTheme();
                applySyntaxHighlightingDark();
            } else {
                applyLightTheme();
                applySyntaxHighlightingLight();
            }
            
            // Update the UI for the frame and all components
            SwingUtilities.updateComponentTreeUI(frame);
            
            // Force explicit component updates for components that might not update properly
            updateComponentsRecursively(frame, isDarkMode);
            
            // Force repaint of all components
            frame.repaint();
            
            return "Theme changed to " + (isDarkMode ? "Dark Mode" : "Light Mode");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error applying theme: " + e.getMessage();
        }
    }
    
    /**
     * Applies syntax highlighting colors for dark mode
     */
    private static void applySyntaxHighlightingDark() {
        // We can't set global syntax highlighting, so we'll apply it to each RSyntaxTextArea
        // when we find one in the component tree during updateComponentsRecursively
    }
    
    /**
     * Applies syntax highlighting colors for light mode
     */
    private static void applySyntaxHighlightingLight() {
        // We can't set global syntax highlighting, so we'll apply it to each RSyntaxTextArea
        // when we find one in the component tree during updateComponentsRecursively
    }
    
    /**
     * Directly applies syntax highlighting to an RSyntaxTextArea based on the current theme
     * This can be called directly to ensure syntax highlighting is applied correctly
     * 
     * @param textArea The RSyntaxTextArea to apply highlighting to
     * @param isDarkMode Whether to apply dark mode highlighting
     */
    public static void applySyntaxHighlighting(org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea, boolean isDarkMode) {
        if (textArea == null) {
            return;
        }
        
        System.out.println("Applying " + (isDarkMode ? "dark" : "light") + " syntax highlighting");
        
        // Set background and foreground colors
        textArea.setBackground(isDarkMode ? DARK_TEXT_AREA_BG : Color.WHITE);
        textArea.setForeground(isDarkMode ? DARK_FOREGROUND : Color.BLACK);
        
        // Get the syntax scheme for this text area
        org.fife.ui.rsyntaxtextarea.SyntaxScheme scheme = textArea.getSyntaxScheme();
        
        if (isDarkMode) {
            // Apply dark mode syntax highlighting
            
            // Comments
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_DOCUMENTATION).foreground = DARK_COMMENT;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_EOL).foreground = DARK_COMMENT;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_MULTILINE).foreground = DARK_COMMENT;
            
            // Keywords
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).foreground = DARK_KEYWORD;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD_2).foreground = DARK_KEYWORD;
            
            // Literals
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = DARK_STRING;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_CHAR).foreground = DARK_STRING;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_DECIMAL_INT).foreground = DARK_NUMBER;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_FLOAT).foreground = DARK_NUMBER;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_HEXADECIMAL).foreground = DARK_NUMBER;
            
            // Operators and identifiers
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.OPERATOR).foreground = DARK_OPERATOR;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.IDENTIFIER).foreground = DARK_IDENTIFIER;
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.FUNCTION).foreground = DARK_FUNCTION;
            
            // Separators (parentheses, brackets, etc.)
            scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.SEPARATOR).foreground = DARK_SEPARATOR;
            
                    // Make selected text background yellow with black text for better visibility
                    textArea.setSelectionColor(new Color(255, 220, 0)); // More opaque yellow
                    textArea.setSelectedTextColor(Color.BLACK);
        } else {
            // Reset to default colors for light mode
            textArea.restoreDefaultSyntaxScheme();
            scheme = textArea.getSyntaxScheme(); // Get the fresh scheme
        }
        
        // Apply the updated scheme
        textArea.setSyntaxScheme(scheme);
        textArea.setCaretColor(isDarkMode ? Color.WHITE : Color.BLACK);
        
        // Force repaint to ensure changes are visible
        textArea.repaint();
    }
    
    /**
     * Resets the look and feel to Nimbus
     */
    private static void resetLookAndFeel() throws ClassNotFoundException, InstantiationException, 
            IllegalAccessException, UnsupportedLookAndFeelException {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    }
    
    /**
     * Applies dark theme settings
     */
    private static void applyDarkTheme() {
        // Base colors
        UIManager.put("control", new ColorUIResource(DARK_CONTROL));
        UIManager.put("info", new ColorUIResource(DARK_BACKGROUND));
        UIManager.put("nimbusBase", new ColorUIResource(18, 30, 49));
        UIManager.put("nimbusAlertYellow", new ColorUIResource(248, 187, 0));
        UIManager.put("nimbusDisabledText", new ColorUIResource(128, 128, 128));
        UIManager.put("nimbusFocus", new ColorUIResource(115, 164, 209));
        UIManager.put("nimbusGreen", new ColorUIResource(176, 179, 50));
        UIManager.put("nimbusInfoBlue", new ColorUIResource(66, 139, 221));
        UIManager.put("nimbusLightBackground", new ColorUIResource(18, 30, 49));
        UIManager.put("nimbusOrange", new ColorUIResource(191, 98, 4));
        UIManager.put("nimbusRed", new ColorUIResource(169, 46, 34));
        UIManager.put("nimbusSelectedText", new ColorUIResource(255, 255, 255));
        UIManager.put("nimbusSelectionBackground", new ColorUIResource(DARK_SELECTION_BG));
        UIManager.put("text", new ColorUIResource(DARK_FOREGROUND));
        
        // Component-specific settings
        UIManager.put("Panel.background", new ColorUIResource(DARK_BACKGROUND));
        UIManager.put("Panel.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TabbedPane.background", new ColorUIResource(DARK_BACKGROUND));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TabbedPane.selected", new ColorUIResource(DARK_CONTROL));
        UIManager.put("TextArea.background", new ColorUIResource(DARK_TEXT_AREA_BG));
        UIManager.put("TextArea.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TextField.background", new ColorUIResource(DARK_TEXT_AREA_BG));
        UIManager.put("TextField.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("List.background", new ColorUIResource(DARK_TEXT_AREA_BG));
        UIManager.put("List.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("ScrollPane.background", new ColorUIResource(DARK_BACKGROUND));
        UIManager.put("Button.background", new ColorUIResource(DARK_CONTROL));
        UIManager.put("Button.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("ComboBox.background", new ColorUIResource(DARK_CONTROL));
        UIManager.put("ComboBox.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Label.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Table.background", new ColorUIResource(DARK_TEXT_AREA_BG));
        UIManager.put("Table.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TableHeader.background", new ColorUIResource(DARK_CONTROL));
        UIManager.put("TableHeader.foreground", new ColorUIResource(DARK_FOREGROUND));
    }
    
    /**
     * Applies light theme settings
     */
    private static void applyLightTheme() {
        // Base colors
        UIManager.put("control", new ColorUIResource(LIGHT_CONTROL));
        UIManager.put("info", new ColorUIResource(242, 242, 189));
        UIManager.put("nimbusBase", new ColorUIResource(51, 98, 140));
        UIManager.put("nimbusAlertYellow", new ColorUIResource(255, 220, 35));
        UIManager.put("nimbusDisabledText", new ColorUIResource(142, 143, 145));
        UIManager.put("nimbusFocus", new ColorUIResource(115, 164, 209));
        UIManager.put("nimbusGreen", new ColorUIResource(176, 179, 50));
        UIManager.put("nimbusInfoBlue", new ColorUIResource(47, 92, 180));
        UIManager.put("nimbusLightBackground", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("nimbusOrange", new ColorUIResource(191, 98, 4));
        UIManager.put("nimbusRed", new ColorUIResource(169, 46, 34));
        UIManager.put("nimbusSelectedText", new ColorUIResource(255, 255, 255));
        UIManager.put("nimbusSelectionBackground", new ColorUIResource(LIGHT_SELECTION_BG));
        UIManager.put("text", new ColorUIResource(LIGHT_FOREGROUND));
        
        // Component-specific settings
        UIManager.put("Panel.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("Panel.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TabbedPane.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TabbedPane.selected", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("TextArea.background", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("TextArea.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TextField.background", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("TextField.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("List.background", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("List.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("ScrollPane.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("Button.background", new ColorUIResource(LIGHT_CONTROL));
        UIManager.put("Button.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("ComboBox.background", new ColorUIResource(LIGHT_CONTROL));
        UIManager.put("ComboBox.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Label.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Table.background", new ColorUIResource(LIGHT_TEXT_AREA_BG));
        UIManager.put("Table.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TableHeader.background", new ColorUIResource(LIGHT_CONTROL));
        UIManager.put("TableHeader.foreground", new ColorUIResource(LIGHT_FOREGROUND));
    }
    
    /**
     * Recursively updates all components in the container
     * This ensures components that might not update properly through the Look and Feel
     * are explicitly updated
     */
    private static void updateComponentsRecursively(Container container, boolean isDarkMode) {
        for (Component component : container.getComponents()) {
            // Explicitly set colors for certain components
            if (component instanceof JPanel) {
                component.setBackground(isDarkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND);
                component.setForeground(isDarkMode ? DARK_FOREGROUND : LIGHT_FOREGROUND);
            } else if (component instanceof JTextArea) {
                component.setBackground(isDarkMode ? DARK_TEXT_AREA_BG : LIGHT_TEXT_AREA_BG);
                component.setForeground(isDarkMode ? DARK_FOREGROUND : LIGHT_FOREGROUND);
            } else if (component instanceof org.fife.ui.rsyntaxtextarea.RSyntaxTextArea) {
                // Apply syntax highlighting to RSyntaxTextArea
                org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea = 
                        (org.fife.ui.rsyntaxtextarea.RSyntaxTextArea) component;
                
                // Set background and foreground colors
                textArea.setBackground(isDarkMode ? DARK_TEXT_AREA_BG : LIGHT_TEXT_AREA_BG);
                textArea.setForeground(isDarkMode ? DARK_FOREGROUND : LIGHT_FOREGROUND);
                
                // Get the syntax scheme for this text area
                org.fife.ui.rsyntaxtextarea.SyntaxScheme scheme = textArea.getSyntaxScheme();
                
                if (isDarkMode) {
                    // Apply dark mode syntax highlighting
                    
                    // Comments
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_DOCUMENTATION).foreground = DARK_COMMENT;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_EOL).foreground = DARK_COMMENT;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.COMMENT_MULTILINE).foreground = DARK_COMMENT;
                    
                    // Keywords
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD).foreground = DARK_KEYWORD;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.RESERVED_WORD_2).foreground = DARK_KEYWORD;
                    
                    // Literals
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_STRING_DOUBLE_QUOTE).foreground = DARK_STRING;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_CHAR).foreground = DARK_STRING;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_DECIMAL_INT).foreground = DARK_NUMBER;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_FLOAT).foreground = DARK_NUMBER;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.LITERAL_NUMBER_HEXADECIMAL).foreground = DARK_NUMBER;
                    
                    // Operators and identifiers
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.OPERATOR).foreground = DARK_OPERATOR;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.IDENTIFIER).foreground = DARK_IDENTIFIER;
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.FUNCTION).foreground = DARK_FUNCTION;
                    
                    // Separators (parentheses, brackets, etc.)
                    scheme.getStyle(org.fife.ui.rsyntaxtextarea.TokenTypes.SEPARATOR).foreground = DARK_SEPARATOR;
                    
                    // Make selected text background yellow with black text for better visibility
                    textArea.setSelectionColor(new Color(255, 220, 0)); // More opaque yellow
                    textArea.setSelectedTextColor(Color.BLACK);
                } else {
                    // Reset to default colors for light mode
                    textArea.restoreDefaultSyntaxScheme();
                }
                
                // Apply the updated scheme
                textArea.setSyntaxScheme(scheme);
                textArea.setCaretColor(isDarkMode ? Color.WHITE : Color.BLACK);
            }
            
            // Force update of component UI if it's a Swing component
            if (component instanceof JComponent) {
                ((JComponent) component).updateUI();
            }
            
            // Recursively process child containers
            if (component instanceof Container) {
                updateComponentsRecursively((Container) component, isDarkMode);
            }
        }
    }
}
