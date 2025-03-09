/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.runeagent.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Panel for managing saved scripts
 */
public class ScriptsPanel extends JPanel {
    
    private static final String SCRIPTS_FOLDER = "scripts";
    private final JList<String> scriptList;
    private final DefaultListModel<String> listModel;
    private final RSyntaxTextArea scriptArea;
    
    public ScriptsPanel(RSyntaxTextArea scriptArea) {
        this.scriptArea = scriptArea;
        
        // Create scripts directory if it doesn't exist
        createScriptsDirectory();
        
        // Set up the panel layout
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create list model and populate with scripts
        listModel = new DefaultListModel<>();
        loadScriptsList();
        
        // Create script list
        scriptList = new JList<>(listModel);
        scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scriptList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedScript();
                }
            }
        });
        
        // Add components to panel
        add(new JLabel("Saved Scripts:"), BorderLayout.NORTH);
        add(new JScrollPane(scriptList), BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener((ActionEvent e) -> {
            loadSelectedScript();
        });
        
        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener((ActionEvent e) -> {
            renameSelectedScript();
        });
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener((ActionEvent e) -> {
            deleteSelectedScript();
        });
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener((ActionEvent e) -> {
            importScript();
        });
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener((ActionEvent e) -> {
            exportSelectedScript();
        });
        
        // Add buttons in the desired order
        buttonsPanel.add(loadButton);
        buttonsPanel.add(renameButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(exportButton);
        
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the scripts directory if it doesn't exist
     */
    private void createScriptsDirectory() {
        Path scriptsPath = Paths.get(SCRIPTS_FOLDER);
        if (!Files.exists(scriptsPath)) {
            try {
                Files.createDirectory(scriptsPath);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Failed to create scripts directory: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Loads the list of scripts from the scripts directory
     */
    private void loadScriptsList() {
        listModel.clear();
        File scriptsDir = new File(SCRIPTS_FOLDER);
        
        if (scriptsDir.exists() && scriptsDir.isDirectory()) {
            File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".js"));
            
            if (scriptFiles != null) {
                Arrays.sort(scriptFiles);
                for (File file : scriptFiles) {
                    listModel.addElement(file.getName());
                }
            }
        }
    }
    
    /**
     * Loads the selected script into the script editor
     */
    private void loadSelectedScript() {
        String selectedScript = scriptList.getSelectedValue();
        if (selectedScript != null) {
            File scriptFile = new File(SCRIPTS_FOLDER + File.separator + selectedScript);
            
            try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                scriptArea.setText(content.toString());
                scriptArea.setCaretPosition(0);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error loading script: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Saves the current script in the editor
     */
    private void saveCurrentScript() {
        String scriptName = JOptionPane.showInputDialog(this, 
                "Enter script name:", 
                "Save Script", 
                JOptionPane.PLAIN_MESSAGE);
        
        if (scriptName != null && !scriptName.trim().isEmpty()) {
            // Add .js extension if not present
            if (!scriptName.toLowerCase().endsWith(".js")) {
                scriptName += ".js";
            }
            
            File scriptFile = new File(SCRIPTS_FOLDER + File.separator + scriptName);
            
            // Check if file exists and confirm overwrite
            if (scriptFile.exists()) {
                int result = JOptionPane.showConfirmDialog(this, 
                        "Script already exists. Overwrite?", 
                        "Confirm Overwrite", 
                        JOptionPane.YES_NO_OPTION);
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try (FileWriter writer = new FileWriter(scriptFile)) {
                writer.write(scriptArea.getText());
                loadScriptsList(); // Refresh the list
                
                // Select the newly saved script
                scriptList.setSelectedValue(scriptName, true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error saving script: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Renames the selected script
     */
    private void renameSelectedScript() {
        String selectedScript = scriptList.getSelectedValue();
        if (selectedScript != null) {
            String newName = JOptionPane.showInputDialog(this, 
                    "Enter new script name:", 
                    "Rename Script", 
                    JOptionPane.PLAIN_MESSAGE);
            
            if (newName != null && !newName.trim().isEmpty()) {
                // Add .js extension if not present
                if (!newName.toLowerCase().endsWith(".js")) {
                    newName += ".js";
                }
                
                File oldFile = new File(SCRIPTS_FOLDER + File.separator + selectedScript);
                File newFile = new File(SCRIPTS_FOLDER + File.separator + newName);
                
                // Check if new name already exists
                if (newFile.exists()) {
                    JOptionPane.showMessageDialog(this, 
                            "A script with that name already exists.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (oldFile.renameTo(newFile)) {
                    loadScriptsList(); // Refresh the list
                    
                    // Select the renamed script
                    scriptList.setSelectedValue(newName, true);
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Failed to rename script.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Deletes the selected script
     */
    private void deleteSelectedScript() {
        String selectedScript = scriptList.getSelectedValue();
        if (selectedScript != null) {
            int result = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this script?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                File scriptFile = new File(SCRIPTS_FOLDER + File.separator + selectedScript);
                
                if (scriptFile.delete()) {
                    loadScriptsList(); // Refresh the list
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Failed to delete script.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Imports a script from an external file
     */
    private void importScript() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Import Script");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JavaScript Files (*.js)", "js"));
        
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File sourceFile = fileChooser.getSelectedFile();
            String fileName = sourceFile.getName();
            
            // Create destination file in scripts folder
            java.io.File destFile = new java.io.File(SCRIPTS_FOLDER + java.io.File.separator + fileName);
            
            // Check if file already exists
            if (destFile.exists()) {
                int result = JOptionPane.showConfirmDialog(this, 
                        "A script with this name already exists. Overwrite?", 
                        "Confirm Overwrite", 
                        JOptionPane.YES_NO_OPTION);
                
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                // Copy file
                java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Refresh list and select the imported script
                loadScriptsList();
                scriptList.setSelectedValue(fileName, true);
                
                // Load the script content
                loadSelectedScript();
                
                JOptionPane.showMessageDialog(this, 
                        "Script imported successfully.", 
                        "Import Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error importing script: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Exports the selected script to an external file
     */
    private void exportSelectedScript() {
        String selectedScript = scriptList.getSelectedValue();
        if (selectedScript != null) {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Export Script");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JavaScript Files (*.js)", "js"));
            fileChooser.setSelectedFile(new java.io.File(selectedScript));
            
            if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File destFile = fileChooser.getSelectedFile();
                
                // Add .js extension if not present
                if (!destFile.getName().toLowerCase().endsWith(".js")) {
                    destFile = new java.io.File(destFile.getAbsolutePath() + ".js");
                }
                
                // Check if file already exists
                if (destFile.exists()) {
                    int result = JOptionPane.showConfirmDialog(this, 
                            "File already exists. Overwrite?", 
                            "Confirm Overwrite", 
                            JOptionPane.YES_NO_OPTION);
                    
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                try {
                    // Copy file
                    java.io.File sourceFile = new java.io.File(SCRIPTS_FOLDER + java.io.File.separator + selectedScript);
                    java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(), 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    JOptionPane.showMessageDialog(this, 
                            "Script exported successfully.", 
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                            "Error exporting script: " + e.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select a script to export.", 
                    "No Script Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Refreshes the scripts list
     */
    public void refreshScriptsList() {
        loadScriptsList();
    }
    
    /**
     * Returns the script text area
     * @return The RSyntaxTextArea used for editing scripts
     */
    public RSyntaxTextArea getScriptArea() {
        return scriptArea;
    }
}
