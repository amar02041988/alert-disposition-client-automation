package com.travelex;


import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BootAutomation {

    private static final Logger LOGGER = Logger.getLogger(BootAutomation.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Booting Alert Disposition service auotmation app...");
        startCamel();
    }

    public static void startCamel() throws Exception {
        ClassPathXmlApplicationContext applicationContext =
                        new ClassPathXmlApplicationContext("alertDisposition-context.xml");
    }
}
