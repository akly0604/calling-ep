package org.jar.sample;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import org.example.helloWorld;  // This should exist in the JAR

public class JarExecutorMediator extends AbstractMediator {
    @Override
    public boolean mediate(MessageContext context) {
        String inputId = (String) context.getProperty("inputString");

        try {
            // Load the JAR dynamically from the 'libs' folder
            File jarFile = new File("libs/helloworld.jar");  // Path to the JAR
            if (jarFile.exists()) {
                URL jarUrl = jarFile.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, Thread.currentThread().getContextClassLoader());
                // Load the class from the JAR
                Class<?> helloWorldClass = classLoader.loadClass("org.example.helloWorld");
                // Call the method from the class
                String customerDetails = (String) helloWorldClass.getMethod("getMessage", String.class).invoke(null, inputId);
                
                // Set the response value in the context
                context.setProperty("responseValue", customerDetails);
            } else {
                throw new RuntimeException("helloWorld.jar not found in libs folder");
            }
        } catch (Exception e) {
            // Handle exceptions if any
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
