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
    private static final String JAR_NAME = "helloWorld.jar";
    private static final String JAR_CLASS = "org.example.HelloWorld"; // Change to your main class

    public boolean mediate(MessageContext context) {
        log.info("Starting JAR existence check mediator");
        
        // Check if JAR exists in the expected directories
        checkJarInDirectory("/workspace/CustomMediator/libs");
        checkJarInDirectory(System.getProperty("carbon.home") + "/repository/components/lib");
        checkJarInDirectory(System.getProperty("user.dir") + "/libs");
        
        // Try to load the class from the JAR
        try {
            log.info("Attempting to load class: " + JAR_CLASS);
            Class.forName(JAR_CLASS);
            log.info("Successfully loaded class: " + JAR_CLASS);
            context.setProperty("jarCheckResult", "JAR class loaded successfully");
        } catch (ClassNotFoundException e) {
            log.error("Failed to load class: " + JAR_CLASS, e);
            context.setProperty("jarCheckResult", "Failed to load JAR class: " + e.getMessage());
            
            // Try to load the JAR dynamically
            try {
                File jarFile = findJarFile();
                if (jarFile != null && jarFile.exists()) {
                    URLClassLoader child = new URLClassLoader(
                            new URL[] {jarFile.toURI().toURL()},
                            this.getClass().getClassLoader()
                    );
                    Class<?> classToLoad = Class.forName(JAR_CLASS, true, child);
                    log.info("Dynamically loaded class: " + classToLoad.getName());
                    context.setProperty("jarCheckResult", 
                            "Dynamically loaded JAR class from: " + jarFile.getAbsolutePath());
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
        
        File jarFile = new File(dir, JAR_NAME);
        if (jarFile.exists()) {
            log.info("JAR found at: " + jarFile.getAbsolutePath());
        } else {
            log.info("JAR not found in directory: " + dirPath);
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
    
    private File findJarFile() {
        String[] possibleDirs = {
            "/workspace/CustomMediator/libs",
            System.getProperty("carbon.home") + "/repository/components/lib",
            System.getProperty("user.dir") + "/libs"
        };
        
        for (String dir : possibleDirs) {
            File jarFile = new File(dir, JAR_NAME);
            if (jarFile.exists()) {
                return jarFile;
            }
        }
        
        return null;
    }
}