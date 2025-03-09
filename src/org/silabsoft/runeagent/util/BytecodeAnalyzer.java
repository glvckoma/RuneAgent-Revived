package org.silabsoft.runeagent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

/**
 * A utility class for analyzing bytecode to identify potential hooking points.
 * This class uses BCEL to analyze JAR files and identify classes and methods
 * that are likely related to packet handling.
 */
public class BytecodeAnalyzer {
    
    /**
     * Analyzes a JAR file and identifies potential packet handling classes and methods.
     * 
     * @param jarFile The JAR file to analyze
     * @return A map of class names to lists of method signatures
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, List<MethodInfo>> analyzeJar(File jarFile) throws IOException {
        Map<String, List<MethodInfo>> result = new HashMap<>();
        
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    
                    try {
                        ClassParser parser = new ClassParser(jarFile.getPath(), entry.getName());
                        JavaClass javaClass = parser.parse();
                        
                        List<MethodInfo> methods = analyzeClass(javaClass);
                        
                        if (!methods.isEmpty()) {
                            result.put(className, methods);
                        }
                    } catch (Exception e) {
                        System.err.println("Error analyzing class " + className + ": " + e.getMessage());
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Analyzes a class and identifies potential packet handling methods.
     * 
     * @param javaClass The class to analyze
     * @return A list of method signatures
     */
    private static List<MethodInfo> analyzeClass(JavaClass javaClass) {
        List<MethodInfo> result = new ArrayList<>();
        
        // Look for methods that might be related to packet handling
        for (Method method : javaClass.getMethods()) {
            if (isPacketHandlingMethod(javaClass, method)) {
                result.add(new MethodInfo(
                    method.getName(),
                    method.getSignature(),
                    method.getAccessFlags(),
                    getMethodPattern(javaClass, method)
                ));
            }
        }
        
        return result;
    }
    
    /**
     * Determines whether a method is likely related to packet handling.
     * 
     * @param javaClass The class containing the method
     * @param method The method to analyze
     * @return true if the method is likely related to packet handling, false otherwise
     */
    private static boolean isPacketHandlingMethod(JavaClass javaClass, Method method) {
        // Look for methods with names that suggest packet handling
        String name = method.getName().toLowerCase();
        
        if (name.contains("packet") || name.contains("opcode") || name.contains("frame") ||
            name.contains("write") || name.contains("read") || name.contains("buffer") ||
            name.contains("stream") || name.contains("byte") || name.contains("network")) {
            return true;
        }
        
        // Look for methods that operate on byte arrays
        String signature = method.getSignature();
        if (signature.contains("[B")) {
            return true;
        }
        
        // Look for methods that might be related to network I/O
        try {
            MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), new ConstantPoolGen(javaClass.getConstantPool()));
            InstructionList instructionList = methodGen.getInstructionList();
            
            if (instructionList != null) {
                for (Instruction instruction : instructionList.getInstructions()) {
                    String instructionString = instruction.toString();
                    
                    if (instructionString.contains("Socket") || instructionString.contains("InputStream") ||
                        instructionString.contains("OutputStream") || instructionString.contains("Channel")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during bytecode analysis
        }
        
        return false;
    }
    
    /**
     * Extracts a pattern from a method's bytecode that can be used to identify the method
     * even if it's obfuscated.
     * 
     * @param javaClass The class containing the method
     * @param method The method to analyze
     * @return A pattern that can be used to identify the method
     */
    private static String getMethodPattern(JavaClass javaClass, Method method) {
        StringBuilder pattern = new StringBuilder();
        
        try {
            MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), new ConstantPoolGen(javaClass.getConstantPool()));
            InstructionList instructionList = methodGen.getInstructionList();
            
            if (instructionList != null) {
                for (Instruction instruction : instructionList.getInstructions()) {
                    pattern.append(instruction.getOpcode()).append(" ");
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during bytecode analysis
        }
        
        return pattern.toString().trim();
    }
    
    /**
     * A class representing information about a method.
     */
    public static class MethodInfo {
        private final String name;
        private final String signature;
        private final int accessFlags;
        private final String pattern;
        
        public MethodInfo(String name, String signature, int accessFlags, String pattern) {
            this.name = name;
            this.signature = signature;
            this.accessFlags = accessFlags;
            this.pattern = pattern;
        }
        
        public String getName() {
            return name;
        }
        
        public String getSignature() {
            return signature;
        }
        
        public int getAccessFlags() {
            return accessFlags;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        @Override
        public String toString() {
            return name + signature + " (access flags: " + accessFlags + ")";
        }
    }
    
    /**
     * Main method for testing the BytecodeAnalyzer.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: BytecodeAnalyzer <jar-file>");
            return;
        }
        
        try {
            File jarFile = new File(args[0]);
            Map<String, List<MethodInfo>> result = analyzeJar(jarFile);
            
            System.out.println("Potential packet handling classes and methods:");
            
            for (Map.Entry<String, List<MethodInfo>> entry : result.entrySet()) {
                System.out.println(entry.getKey() + ":");
                
                for (MethodInfo method : entry.getValue()) {
                    System.out.println("  " + method);
                }
            }
        } catch (IOException e) {
            System.err.println("Error analyzing JAR file: " + e.getMessage());
        }
    }
}
