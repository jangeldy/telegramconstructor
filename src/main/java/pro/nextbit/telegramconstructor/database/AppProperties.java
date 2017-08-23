package pro.nextbit.telegramconstructor.database;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class AppProperties {

    private static Properties properties = new Properties();

    public void init(String fileName) {

        Logger log = LogManager.getLogger(AppProperties.class);

        try {

            log.info("Reading data from a file " + fileName + "...");
            ClassLoader classLoader = AppProperties.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(fileName);
            properties.load(inputStream);

        }catch (Exception e){
            e.printStackTrace();
            log.info("Error! The file " + fileName + " was not found or an error occurred while reading the data");
        }

    }

    public static String getProp(String propertyName){
        return properties.getProperty(propertyName);
    }

}
