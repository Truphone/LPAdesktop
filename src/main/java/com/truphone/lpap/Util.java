/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

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

    public static String swapNibblesOnString(String in) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < in.length(); i += 2) {
            result.append(in.charAt(i + 1));
            result.append(in.charAt(i));
        }
        return result.toString();
    }

    public static void setUpMacShortcuts(javax.swing.InputMap im) {
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
    }

    public static String showInputDialog(JFrame parent, String text, String predefinedValue) {
        InputDialog id = new InputDialog(parent);
        id.setText(text);
        id.setPredefinedValue(predefinedValue);
        id.setVisible(true);

        return id.getValue();
    }

    public static void showMessageDialog(JFrame parent, String text) {
        MessageDialog d = new MessageDialog(parent);
        d.setText(text);

        d.setVisible(true);
    }

    public static int showConfirmDialog(JFrame parent, String text, String title) {
        ConfirmDialog d = new ConfirmDialog(parent);
        d.setTitle(title);
        d.setText(text);

        d.setVisible(true);
        return d.getResult();

    }
}
