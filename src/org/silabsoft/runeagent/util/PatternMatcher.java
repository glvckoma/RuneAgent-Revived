package org.silabsoft.runeagent.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

/**
 * A utility class for matching methods based on bytecode patterns.
 * This class can be used to identify methods in obfuscated code by
 * matching their bytecode patterns.
 */
public class PatternMatcher {
    
    /**
     * A class representing a method pattern.
     */
    public static class MethodPattern {
        private final String className;
        private final String methodName;
        private final String methodSignature;
        private final String bytecodePattern;
        private final Pattern compiledPattern;
        
        /**
         * Creates a new method pattern.
         * 
         * @param className The name of the class containing the method
         * @param methodName The name of the method (can be null for obfuscated methods)
         * @param methodSignature The signature of the method (can be null for obfuscated methods)
         * @param bytecodePattern A regex pattern matching the method's bytecode
         */
        public MethodPattern(String className, String methodName, String methodSignature, String bytecodePattern) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.bytecodePattern = bytecodePattern;
            this.compiledPattern = Pattern.compile(bytecodePattern);
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public String getMethodSignature() {
            return methodSignature;
        }
        
        public String getBytecodePattern() {
            return bytecodePattern;
        }
        
        public Pattern getCompiledPattern() {
            return compiledPattern;
        }
    }
    
    /**
     * A class representing a matched method.
     */
    public static class MatchedMethod {
        private final String className;
        private final String methodName;
        private final String methodSignature;
        private final int accessFlags;
        
        public MatchedMethod(String className, String methodName, String methodSignature, int accessFlags) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.accessFlags = accessFlags;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public String getMethodSignature() {
            return methodSignature;
        }
        
        public int getAccessFlags() {
            return accessFlags;
        }
        
        @Override
        public String toString() {
            return className + "." + methodName + methodSignature + " (access flags: " + accessFlags + ")";
        }
    }
    
    /**
     * Finds methods in a JAR file that match the given patterns.
     * 
     * @param jarFile The JAR file to search
     * @param patterns The patterns to match
     * @return A list of matched methods
     * @throws IOException If an I/O error occurs
     */
    public static List<MatchedMethod> findMethods(File jarFile, List<MethodPattern> patterns) throws IOException {
        List<MatchedMethod> result = new ArrayList<>();
        Map<String, List<MethodPattern>> patternsByClass = groupPatternsByClass(patterns);
        
        // Use BytecodeAnalyzer to get potential packet handling classes and methods
        Map<String, List<BytecodeAnalyzer.MethodInfo>> potentialMethods = BytecodeAnalyzer.analyzeJar(jarFile);
        
        // For each class with potential methods
        for (Map.Entry<String, List<BytecodeAnalyzer.MethodInfo>> entry : potentialMethods.entrySet()) {
            String className = entry.getKey();
            List<BytecodeAnalyzer.MethodInfo> methods = entry.getValue();
            
            // Get patterns for this class
            List<MethodPattern> classPatterns = patternsByClass.getOrDefault(className, new ArrayList<>());
            
            // If there are no patterns for this class, skip it
            if (classPatterns.isEmpty()) {
                continue;
            }
            
            // For each method in the class
            for (BytecodeAnalyzer.MethodInfo method : methods) {
                // For each pattern
                for (MethodPattern pattern : classPatterns) {
                    // If the method name and signature match (if provided)
                    if ((pattern.getMethodName() == null || pattern.getMethodName().equals(method.getName())) &&
                        (pattern.getMethodSignature() == null || pattern.getMethodSignature().equals(method.getSignature()))) {
                        
                        // If the bytecode pattern matches
                        if (pattern.getCompiledPattern().matcher(method.getPattern()).matches()) {
                            result.add(new MatchedMethod(
                                className,
                                method.getName(),
                                method.getSignature(),
                                method.getAccessFlags()
                            ));
                            break;
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Groups patterns by class name for more efficient matching.
     * 
     * @param patterns The patterns to group
     * @return A map of class names to lists of patterns
     */
    private static Map<String, List<MethodPattern>> groupPatternsByClass(List<MethodPattern> patterns) {
        Map<String, List<MethodPattern>> result = new HashMap<>();
        
        for (MethodPattern pattern : patterns) {
            String className = pattern.getClassName();
            List<MethodPattern> classPatterns = result.computeIfAbsent(className, k -> new ArrayList<>());
            classPatterns.add(pattern);
        }
        
        return result;
    }
    
    /**
     * Creates a pattern for the Buffer.writeOpcode method.
     * 
     * @return A pattern for the Buffer.writeOpcode method
     */
    public static MethodPattern createWriteOpcodePattern() {
        // This pattern matches methods that write a value to a buffer and might be the writeOpcode method
        return new MethodPattern(
            "com.osroyale.Buffer",
            "writeOpcode",
            "(I)V",
            ".*16 54 16 25 178.*" // ILOAD, ISTORE, ILOAD, ALOAD, GETSTATIC (typical pattern for writing an opcode)
        );
    }
    
    /**
     * Creates a pattern for the Buffer.writeByte method.
     * 
     * @return A pattern for the Buffer.writeByte method
     */
    public static MethodPattern createWriteBytePattern() {
        // This pattern matches methods that write a byte to a buffer
        return new MethodPattern(
            "com.osroyale.Buffer",
            "writeByte",
            "(I)V",
            ".*16 25 180 16 126.*" // ILOAD, ALOAD, GETFIELD, ILOAD, IADD (typical pattern for writing a byte)
        );
    }
    
    /**
     * Creates a pattern for the Buffer.writeShort method.
     * 
     * @return A pattern for the Buffer.writeShort method
     */
    public static MethodPattern createWriteShortPattern() {
        // This pattern matches methods that write a short to a buffer
        return new MethodPattern(
            "com.osroyale.Buffer",
            "writeShort",
            "(I)V",
            ".*16 25 180 16 126 132.*" // ILOAD, ALOAD, GETFIELD, ILOAD, IADD, I2B (typical pattern for writing a short)
        );
    }
    
    /**
     * Creates a pattern for the Buffer.writeDWord method.
     * 
     * @return A pattern for the Buffer.writeDWord method
     */
    public static MethodPattern createWriteDWordPattern() {
        // This pattern matches methods that write a dword (int) to a buffer
        return new MethodPattern(
            "com.osroyale.Buffer",
            "writeDWord",
            "(I)V",
            ".*16 25 180 16 124 132.*" // ILOAD, ALOAD, GETFIELD, ILOAD, ISHR, I2B (typical pattern for writing a dword)
        );
    }
    
    /**
     * Creates a list of common packet handling method patterns.
     * 
     * @return A list of common packet handling method patterns
     */
    public static List<MethodPattern> createCommonPatterns() {
        List<MethodPattern> patterns = new ArrayList<>();
        
        patterns.add(createWriteOpcodePattern());
        patterns.add(createWriteBytePattern());
        patterns.add(createWriteShortPattern());
        patterns.add(createWriteDWordPattern());
        
        return patterns;
    }
    
    /**
     * Main method for testing the PatternMatcher.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: PatternMatcher <jar-file>");
            return;
        }
        
        try {
            File jarFile = new File(args[0]);
            List<MethodPattern> patterns = createCommonPatterns();
            List<MatchedMethod> matches = findMethods(jarFile, patterns);
            
            System.out.println("Matched methods:");
            
            for (MatchedMethod match : matches) {
                System.out.println("  " + match);
            }
        } catch (IOException e) {
            System.err.println("Error analyzing JAR file: " + e.getMessage());
        }
    }
}
