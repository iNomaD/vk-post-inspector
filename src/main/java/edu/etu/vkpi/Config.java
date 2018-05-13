package edu.etu.vkpi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    // target
    public static Integer ownerId = -1; // -73598440
    public static Integer itemId = 1; // 13491726

    // filter parameters
    public static Boolean isWoman = null; // STRONG PARAMETER
    public static Integer cityId = null; // STRONG PARAMETER Moscow (1), Saint-Petersburg (2), Kirov(66)
    public static Integer minAge = null; // WEAK PARAMETER
    public static Integer maxAge = null; // WEAK PARAMETER
    public static Boolean advancedAge = false;

    // user parameters
    public static Integer appId = 0;
    public static String accessToken = "";
    public static Integer userId = 0;
    public static Integer timeout = 333;

    // outputs
    public static Boolean outputToFile = false;
    public static String outputName = "output.txt";


    public static final String CONF_FILE_NAME = "conf.properties";
    public static boolean loadProperties() {
        try {
            Properties prop = new Properties();
            InputStream is = new FileInputStream(CONF_FILE_NAME);
            prop.load(is);

            ownerId = parseInteger(prop, "ownerId", ownerId);
            itemId = parseInteger(prop, "itemId", itemId);

            isWoman = parseBoolean(prop, "isWoman", isWoman);
            cityId = parseInteger(prop, "cityId", cityId);
            minAge = parseInteger(prop, "minAge", minAge);
            maxAge = parseInteger(prop, "maxAge", maxAge);
            advancedAge = parseBoolean(prop, "advancedAge", advancedAge);

            appId = parseInteger(prop, "appId", appId);
            accessToken = parseString(prop, "accessToken", accessToken);
            userId = parseInteger(prop, "userId", userId);
            timeout = parseInteger(prop, "timeout", timeout);

            outputToFile = parseBoolean(prop, "outputToFile", outputToFile);
            outputName = parseString(prop, "outputName", outputName);
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static Integer parseInteger(Properties prop, String key, Integer value){
        Integer result = null;
        try {
            String valueStr = prop.getProperty(key, null);
            result = (valueStr == null || valueStr.isEmpty()) ? value : Integer.parseInt(valueStr);
        }
        catch (Exception e){

        }
        System.out.println(key + " = " + result);
        return result;
    }

    private static Boolean parseBoolean(Properties prop, String key, Boolean value){
        Boolean result = null;
        try {
            String valueStr = prop.getProperty(key, null);
            result = (valueStr == null || valueStr.isEmpty()) ? value : Boolean.parseBoolean(valueStr);
        }
        catch (Exception e){

        }
        System.out.println(key+" = "+result);
        return result;
    }

    private static String parseString(Properties prop, String key, String value){
        String result = null;
        try {
            String valueStr = prop.getProperty(key, null);
            result = (valueStr == null || valueStr.isEmpty()) ? value : valueStr;
        }
        catch (Exception e){

        }
        System.out.println(key+" = "+result);
        return result;
    }
}
