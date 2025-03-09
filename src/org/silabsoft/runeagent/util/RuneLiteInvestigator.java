package org.silabsoft.runeagent.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A utility class for investigating the RuneLite API to determine whether
 * it can be leveraged for packet interception.
 */
public class RuneLiteInvestigator {
    
    /**
     * A class representing a potential packet interception point in the RuneLite API.
     */
    public static class InterceptionPoint {
        private final String className;
        private final String methodName;
        private final String methodSignature;
        private final String description;
        
        public InterceptionPoint(String className, String methodName, String methodSignature, String description) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.description = description;
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
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return className + "." + methodName + methodSignature + " - " + description;
        }
    }
    
    /**
     * Investigates the RuneLite API in the given JAR file to determine whether
     * it can be leveraged for packet interception.
     * 
     * @param jarFile The JAR file containing the RuneLite API
     * @return A list of potential packet interception points
     * @throws IOException If an I/O error occurs
     */
    public static List<InterceptionPoint> investigateRuneLiteApi(File jarFile) throws IOException {
        List<InterceptionPoint> result = new ArrayList<>();
        
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                
                if (entry.getName().endsWith(".class") && entry.getName().contains("runelite")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    
                    // Look for classes and methods that might be related to packet handling
                    if (className.contains("Packet") || className.contains("Network") || className.contains("Protocol")) {
                        result.add(new InterceptionPoint(
                            className,
                            null,
                            null,
                            "Class name suggests packet handling"
                        ));
                    }
                    
                    // Look for event classes that might be related to packet handling
                    if (className.contains("Event") && (className.contains("Game") || className.contains("Client"))) {
                        result.add(new InterceptionPoint(
                            className,
                            null,
                            null,
                            "Event class that might be related to packet handling"
                        ));
                    }
                    
                    // Look for callback interfaces that might be related to packet handling
                    if (className.contains("Callback") && (className.contains("Game") || className.contains("Client"))) {
                        result.add(new InterceptionPoint(
                            className,
                            null,
                            null,
                            "Callback interface that might be related to packet handling"
                        ));
                    }
                }
            }
        }
        
        // If we have access to the RuneLite API classes, we can use reflection to find more interception points
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() });
            
            for (InterceptionPoint point : new ArrayList<>(result)) {
                if (point.getMethodName() == null) {
                    try {
                        Class<?> clazz = classLoader.loadClass(point.getClassName());
                        
                        for (Method method : clazz.getMethods()) {
                            String methodName = method.getName();
                            
                            // Look for methods that might be related to packet handling
                            if (methodName.contains("packet") || methodName.contains("network") || methodName.contains("protocol")) {
                                result.add(new InterceptionPoint(
                                    point.getClassName(),
                                    methodName,
                                    getMethodSignature(method),
                                    "Method name suggests packet handling"
                                ));
                            }
                            
                            // Look for event methods that might be related to packet handling
                            if (methodName.startsWith("on") && (methodName.contains("Game") || methodName.contains("Client"))) {
                                result.add(new InterceptionPoint(
                                    point.getClassName(),
                                    methodName,
                                    getMethodSignature(method),
                                    "Event method that might be related to packet handling"
                                ));
                            }
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        // Ignore classes that can't be loaded
                    }
                }
            }
            
            classLoader.close();
        } catch (Exception e) {
            System.err.println("Error using reflection to investigate RuneLite API: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Gets a string representation of a method's signature.
     * 
     * @param method The method
     * @return A string representation of the method's signature
     */
    private static String getMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder("(");
        
        for (Class<?> parameterType : method.getParameterTypes()) {
            signature.append(getTypeSignature(parameterType));
        }
        
        signature.append(")");
        signature.append(getTypeSignature(method.getReturnType()));
        
        return signature.toString();
    }
    
    /**
     * Gets a string representation of a type's signature.
     * 
     * @param type The type
     * @return A string representation of the type's signature
     */
    private static String getTypeSignature(Class<?> type) {
        if (type.isArray()) {
            return "[" + getTypeSignature(type.getComponentType());
        } else if (type == boolean.class) {
            return "Z";
        } else if (type == byte.class) {
            return "B";
        } else if (type == char.class) {
            return "C";
        } else if (type == double.class) {
            return "D";
        } else if (type == float.class) {
            return "F";
        } else if (type == int.class) {
            return "I";
        } else if (type == long.class) {
            return "J";
        } else if (type == short.class) {
            return "S";
        } else if (type == void.class) {
            return "V";
        } else {
            return "L" + type.getName().replace('.', '/') + ";";
        }
    }
    
    /**
     * Main method for testing the RuneLiteInvestigator.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: RuneLiteInvestigator <jar-file>");
            return;
        }
        
        try {
            File jarFile = new File(args[0]);
            List<InterceptionPoint> points = investigateRuneLiteApi(jarFile);
            
            System.out.println("Potential packet interception points in RuneLite API:");
            
            for (InterceptionPoint point : points) {
                System.out.println("  " + point);
            }
        } catch (IOException e) {
            System.err.println("Error investigating RuneLite API: " + e.getMessage());
        }
    }
}
