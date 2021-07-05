/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import java.io.File;

/**
 *
 * @author amilcar.pereira
 */
public class mainUI {
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        System.setProperty("sun.security.smartcardio.library", "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC");
        
        String loggingConfigFile;

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            loggingConfigFile = "contents/java/lib/logging.properties";
        } else {
            loggingConfigFile = "config/logging.properties";
        }

        File propFile = new File(loggingConfigFile);

        if (propFile.exists()) {
            System.setProperty("java.util.logging.config.file",
                    loggingConfigFile);
        }
        // TODO: handle jar option
        //        else {
//            Util.showMessageDialog(this, "Couldn't find logging configuration");
//            System.exit(0);
//        }

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LPAUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LPAUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LPAUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LPAUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LPAUI().setVisible(true);
            }
        });

    }
}
