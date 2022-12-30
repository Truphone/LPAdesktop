/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author amilcar.pereira
 */
public class mainUI {
    
    private static final java.util.logging.Logger LOG = Logger.getLogger(mainUI.class.getName());
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String loggingConfigFile = "config/logging.properties";

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("sun.security.smartcardio.library", "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC");
            loggingConfigFile = "contents/java/lib/logging.properties";
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            System.setProperty("sun.security.smartcardio.library", "/usr/lib/x86_64-linux-gnu/libpcsclite.so.1");
        }

        File propFile = new File(loggingConfigFile);

        if (propFile.exists()) {
            System.setProperty("java.util.logging.config.file",
                    loggingConfigFile);
        }
        
        initSSLContext();
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
            LOG.log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            LOG.log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            LOG.log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            LOG.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new LPAUI().setVisible(true));

    }
    
    private static void initSSLContext() {
        final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
            LOG.log(Level.SEVERE, "Could not initialize SSL context", e);
        }
   }
}
