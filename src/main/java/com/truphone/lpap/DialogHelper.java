/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

/**
 *
 * @author amilcar.pereira
 */
public class DialogHelper {

    public static void setUpMacShortcuts(javax.swing.InputMap im) {
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
    }

    public static Optional<String> showInputDialog(JFrame parent, String text, String predefinedValue) {
        InputDialog id = new InputDialog(parent);
        id.setText(text);
        id.setPredefinedValue(predefinedValue);
        id.setVisible(true);

        return Optional.ofNullable(id.isOkPressed() ? id.getValue() : null);
    }
    
    
    public static Optional<String> showInputActivationCodeDialog(JFrame parent, String text, String predefinedValue) {
        InputActivationCodeDialog id = new InputActivationCodeDialog(parent);

        id.setPredefinedMatchingId(predefinedValue);
        id.setVisible(true);

        String matchingID = id.getMatchingId();
        String server = id.getServerURL();
        
        //returns the activation code
        return Optional.ofNullable(id.isOkPressed() ? String.format("1$%s$%s", server, matchingID) : null);
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
