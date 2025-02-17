package org.jar.sample;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;


public class JarExecutorMediator extends AbstractMediator {

    @Override
    public boolean mediate(MessageContext context) {
        try {
            // Path to the JAR file in WSO2 lib directory
            String jarFilePath = "/repository/lib/helloworld.jar";
            
            // Load the JAR class using ClassLoader
            File jarFile = new File(jarFilePath);
            URL jarURL = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});

            // Load the 'org.example.helloWorld' class
            Class<?> helloWorldClass = classLoader.loadClass("org.example.helloWorld");

            // Create an instance of the class
            Object helloWorldInstance = helloWorldClass.getDeclaredConstructor().newInstance();

            // Get the 'getMessage' method and invoke it
            String input = "Hello from WSO2!";
            String message = (String) helloWorldClass.getMethod("getMessage", String.class).invoke(helloWorldInstance, input);

            // Log the message (you can also modify the messageContext if needed)
            log.info("Message from helloWorld: " + message);

            // Optionally, set the result in the message context for further processing
            context.setProperty("responseMessage", message);

            // Close the class loader
            classLoader.close();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in executing JAR method: " + e.getMessage());
            return false;
        }
        return true;
    }
}
