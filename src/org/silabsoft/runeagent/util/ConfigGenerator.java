package org.silabsoft.runeagent.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * A utility class for generating RuneAgent configuration files.
 * This class uses the BytecodeAnalyzer and PatternMatcher to identify
 * packet handling methods in a client JAR and generate a configuration
 * file that can be used with RuneAgent.
 */
public class ConfigGenerator {
    
    /**
     * Generates a RuneAgent configuration file for the given client JAR.
     * 
     * @param jarFile The client JAR file
     * @param outputFile The output configuration file
     * @throws IOException If an I/O error occurs
     */
    public static void generateConfig(File jarFile, File outputFile) throws IOException {
        // Find packet handling methods using PatternMatcher
        List<PatternMatcher.MethodPattern> patterns = PatternMatcher.createCommonPatterns();
        List<PatternMatcher.MatchedMethod> matches = PatternMatcher.findMethods(jarFile, patterns);
        
        // Generate configuration file
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("// RuneAgent Configuration for " + jarFile.getName() + "\n\n");
            
            // Find the Buffer class (or equivalent)
            String bufferClass = findBufferClass(matches);
            
            if (bufferClass != null) {
                writer.write("// Define the target class (" + bufferClass + ")\n");
                writer.write("var bufferClass = \"" + bufferClass + "\";\n\n");
                
                writer.write("// Create a ClassModifier for the Buffer class\n");
                writer.write("var bufferModifier = new Packages.org.silabsoft.runeagent.transformer.ClassModifier(bufferClass);\n\n");
                
                writer.write("// Implement the GenericByteStream interface\n");
                writer.write("bufferModifier.setInterface(\"org.silabsoft.runeagent.util.GenericByteStream\");\n\n");
                
                writer.write("// Add method wrappers for key packet methods\n\n");
                
                // Add method wrappers for each matched method
                for (PatternMatcher.MatchedMethod match : matches) {
                    if (match.getClassName().equals(bufferClass)) {
                        String methodName = match.getMethodName();
                        String methodSignature = match.getMethodSignature();
                        
                        writer.write("// " + methodName + "\n");
                        writer.write("bufferModifier.addMethodWrapper(\n");
                        writer.write("    \"" + methodName + "\",\n");
                        writer.write("    \"" + methodSignature + "\",\n");
                        writer.write("    \"{\\\n" +
                                     "  java.lang.System.out.println(\\\"[Packet] " + methodName + ": \\\" + $1);\\\n" +
                                     "  original($1);\\\n" +
                                     "}\\\n" +
                                     "\"\n");
                        writer.write(");\n\n");
                    }
                }
                
                writer.write("// Create a RuneTekFourTransformer and add the Buffer modifier\n");
                writer.write("var transformer = new Packages.org.silabsoft.runeagent.transformer.RuneTekFourTransformer();\n");
                writer.write("transformer.addClassModifier(bufferModifier);\n\n");
                
                writer.write("// Export the transformer\n");
                writer.write("transformer;\n");
            } else {
                writer.write("// Could not find Buffer class or equivalent\n");
                writer.write("// Please manually specify the Buffer class in the configuration\n");
            }
        }
    }
    
    /**
     * Finds the Buffer class (or equivalent) in the matched methods.
     * 
     * @param matches The matched methods
     * @return The name of the Buffer class, or null if not found
     */
    private static String findBufferClass(List<PatternMatcher.MatchedMethod> matches) {
        for (PatternMatcher.MatchedMethod match : matches) {
            String className = match.getClassName();
            
            // Look for classes with names that suggest they're Buffer classes
            if (className.contains("Buffer") || className.contains("Stream") || className.contains("Packet")) {
                return className;
            }
        }
        
        // If no class with a suggestive name is found, return the class with the most matched methods
        return findClassWithMostMatches(matches);
    }
    
    /**
     * Finds the class with the most matched methods.
     * 
     * @param matches The matched methods
     * @return The name of the class with the most matched methods, or null if no matches
     */
    private static String findClassWithMostMatches(List<PatternMatcher.MatchedMethod> matches) {
        if (matches.isEmpty()) {
            return null;
        }
        
        // Count matches per class
        java.util.Map<String, Integer> matchCounts = new java.util.HashMap<>();
        
        for (PatternMatcher.MatchedMethod match : matches) {
            String className = match.getClassName();
            int count = matchCounts.getOrDefault(className, 0);
            matchCounts.put(className, count + 1);
        }
        
        // Find class with most matches
        String bestClass = null;
        int bestCount = 0;
        
        for (java.util.Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestClass = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        
        return bestClass;
    }
    
    /**
     * Main method for testing the ConfigGenerator.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ConfigGenerator <jar-file> <output-file>");
            return;
        }
        
        try {
            File jarFile = new File(args[0]);
            File outputFile = new File(args[1]);
            
            generateConfig(jarFile, outputFile);
            
            System.out.println("Configuration file generated: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating configuration file: " + e.getMessage());
        }
    }
}
