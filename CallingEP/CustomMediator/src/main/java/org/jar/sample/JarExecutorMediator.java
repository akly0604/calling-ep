package org.jar.sample;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.example.helloWorld;

public class JarExecutorMediator extends AbstractMediator {
    @Override
    public boolean mediate(MessageContext context) {
        String inputId = (String) context.getProperty("inputString");

        // Call the method from your JAR (Example: CustomerInfo class)
        String customerDetails = helloWorld.getMessage(inputId);

     
        context.setProperty("responseValue", customerDetails);

        return true;
    }
}