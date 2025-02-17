package org.jar.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JarExecutorMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(JarExecutorMediator.class);
    private static final String JAR_NAME = "helloworld.jar"; // Changed to lowercase to match actual filename
    private static final String JAR_CLASS = "org.example.helloWorld";

    public boolean mediate(MessageContext context) {
        log.info("Starting JAR existence check mediator");
        
        // Check if JAR exists in the expected directories
        checkJarInDirectory("/workspace/CustomMediator/libs");
        checkJarInDirectory(System.getProperty("carbon.home") + "/repository/components/lib");
        checkJarInDirectory(System.getProperty("user.dir") + "/libs");
        
        // Try to load the class from the JAR
        try {
            log.info("Attempting to load class: " + JAR_CLASS);
            Class<?> loadedClass = Class.forName(JAR_CLASS);
            log.info("Successfully loaded class: " + loadedClass.getName());
            context.setProperty("jarCheckResult", "JAR class loaded successfully");
            return true;
        } catch (ClassNotFoundException e) {
            log.error("Failed to load class directly: " + JAR_CLASS, e);
            
            // Try to load the JAR dynamically with case-insensitive search
            try {
                File jarFile = findJarFileIgnoreCase();
                if (jarFile != null && jarFile.exists()) {
                    log.info("Found JAR at: " + jarFile.getAbsolutePath());
                    URLClassLoader child = new URLClassLoader(
                            new URL[] {jarFile.toURI().toURL()},
                            this.getClass().getClassLoader()
                    );
                    Class<?> classToLoad = Class.forName(JAR_CLASS, true, child);
                    log.info("Dynamically loaded class: " + classToLoad.getName());
                    context.setProperty("jarCheckResult", 
                            "Dynamically loaded JAR class from: " + jarFile.getAbsolutePath());
                    
                    // Try to create an instance to verify the class is fully loadable
                    try {
                        Object instance = classToLoad.getDeclaredConstructor().newInstance();
                        log.info("Successfully created instance of: " + classToLoad.getName());
                        context.setProperty("instanceCreated", "true");
                    } catch (Exception ex) {
                        log.warn("Could not instantiate class: " + ex.getMessage(), ex);
                        context.setProperty("instanceCreated", "false - " + ex.getMessage());
                    }
                } else {
                    log.error("Could not find JAR file, even with case-insensitive search");
                    context.setProperty("jarCheckResult", "JAR file not found in any directory");
                }
            } catch (Exception ex) {
                log.error("Failed to dynamically load JAR: " + ex.getMessage(), ex);
                context.setProperty("jarCheckResult", 
                        "Failed to dynamically load JAR: " + ex.getMessage());
            }
        }
        
        return true;
    }
    
    private void checkJarInDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            log.info("Directory does not exist: " + dirPath);
            return;
        }
        
        // First try exact match
        File jarFile = new File(dir, JAR_NAME);
        if (jarFile.exists()) {
            log.info("JAR found at: " + jarFile.getAbsolutePath());
        } else {
            log.info("JAR not found with exact name in directory: " + dirPath);
            
            // If not found, try case-insensitive search
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equalsIgnoreCase(JAR_NAME)) {
                        log.info("JAR found with case-insensitive match at: " + file.getAbsolutePath());
                        break;
                    }
                }
            }
        }
        
        // Log all files in the directory for debugging
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            log.info("Files in directory " + dirPath + ":");
            for (File file : files) {
                log.info("  - " + file.getName());
            }
        } else {
            log.info("Directory is empty or cannot be read: " + dirPath);
        }
    }
    
    private File findJarFileIgnoreCase() {
        String[] possibleDirs = {
            "/workspace/CustomMediator/libs",
            System.getProperty("carbon.home") + "/repository/components/lib",
            System.getProperty("user.dir") + "/libs"
        };
        
        for (String dir : possibleDirs) {
            File directory = new File(dir);
            if (!directory.exists() || !directory.isDirectory()) {
                continue;
            }
            
            // First try exact match
            File exactFile = new File(directory, JAR_NAME);
            if (exactFile.exists()) {
                return exactFile;
            }
            
            // If not found, try case-insensitive search
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equalsIgnoreCase(JAR_NAME)) {
                        return file;
                    }
                }
            }
        }
        
        return null;
    }
}