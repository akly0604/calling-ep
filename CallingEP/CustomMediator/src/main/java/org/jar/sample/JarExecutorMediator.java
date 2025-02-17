package org.jar.sample;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JarExecutorMediator {

    public static void main(String[] args) {
        try {
            // Path to the JAR file
            String jarFilePath = "/workspace/CustomMediator/libs/helloworld.jar";
            
            // Create URLClassLoader for the JAR file
            File jarFile = new File(jarFilePath);
            URL jarURL = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});

            // Load the class from the JAR
            Class<?> helloWorldClass = classLoader.loadClass("org.example.helloWorld");

            // Create an instance of the class
            Object helloWorldInstance = helloWorldClass.getDeclaredConstructor().newInstance();

            // Get the 'getMessage' method and invoke it with a sample input
            String input = "Hello, world!";
            String message = (String) helloWorldClass.getMethod("getMessage", String.class).invoke(helloWorldInstance, input);

            // Print the message returned by getMessage
            System.out.println(message);

            // Close the class loader
            classLoader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
