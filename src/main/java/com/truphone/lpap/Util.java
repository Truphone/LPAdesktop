/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 *
 * @author amilcar.pereira
 */
public class Util {
     public static Properties readProperties() throws URISyntaxException {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(new File(main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile() + "/app.properties");

            // load a properties file
            prop.load(input);
            System.out.println(prop);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
     
     public static String swapNibblesOnString(String in){
         StringBuilder result = new StringBuilder();
         for (int i = 0; i < in.length(); i+=2) {
             result.append(in.charAt(i+1));
             result.append(in.charAt(i));
         }
         return result.toString();
     }
}
