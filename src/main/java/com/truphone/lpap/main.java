/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.truphone.apdu.channel.simulator.LpadApduChannelSimulator;
import com.truphone.apdu.channel.simulator.persistence.MapPersistence;
import com.truphone.lpa.impl.LocalProfileAssistantImpl;
import com.truphone.lpa.progress.DownloadProgress;
import com.truphone.lpad.progress.Progress;
import com.truphone.lpad.progress.ProgressListener;
import com.truphone.util.LogStub;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.smartcardio.CardException;

/**
 *
 * @author amilcar.pereira
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException, Exception {
        LogStub.getInstance().setLogLevel(Level.ALL);
        Properties prop = readProperties();

        //default values
        String serverAddress = "https://rsp-acme-test.truphone.com:25105";
        String keystore_file = "/Users/amilcar.pereira/Keys/digicertks.jks";

        //red properties from file
        if (prop != null) {
            serverAddress = prop.getProperty("serverAddress");
            keystore_file = prop.getProperty("keystore_file");
        }

        System.setProperty("javax.net.ssl.trustStore", keystore_file);

        /*//rsp-demo
        //System.setProperty("javax.net.ssl.trustStore","/Users/amilcar.pereira/Keys/rspdemo_es9.jks");
        //String serverAddress="https://rsp-demo.truphone.com:25101";
        //acme
        String serverAddress = "https://rsp-acme-test.truphone.com:25105";
        System.setProperty("javax.net.ssl.trustStore", "/Users/amilcar.pereira/Keys/digicertks.jks");

        //String serverAddress = "https://rsp-entrust.truphone.com";
        //System.setProperty("javax.net.ssl.trustStore", "/Users/amilcar.pereira/Keys/entrust.jks");*/
        // Your ApduChannelImpl implementation
        ApduChannelImpl apduChannel = new ApduChannelImpl();

        LocalProfileAssistantImpl lpa = new LocalProfileAssistantImpl(apduChannel, serverAddress);

        DownloadProgress dwnProgress = new DownloadProgress();
        dwnProgress.setProgressListener(new ProgressListener() {
            @Override
            public void onAction(String phase, String step, Double percentage, String message) {

                System.out.println(phase + "|" + step + "|" + percentage.toString() + "|" + message);
            }
        });

        Progress progress = new Progress();

        String eid = lpa.getEID();
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("+           Local Profile Assistant               +");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("EID: " + eid);

        List<Map<String, String>> profiles = lpa.getProfiles();

        System.out.println("\nProfiles List:");
        System.out.println(profiles);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int option;
        while (true) {
            System.out.println("------------------------------");
            System.out.println("1-List Profiles");
            System.out.println("2-Download Profile");
            System.out.println("3-Enable Profile");
            System.out.println("4-Disable Profile");
            System.out.println("5-Delete Profile");
            System.out.println("6-Get EID Profile");
            System.out.println("7-Process Notifications");
            System.out.println("0-Exit");

            System.out.println("------------------------------");

            System.out.println(">");

            try {
                option = Integer.parseInt(reader.readLine());
            } catch (Exception ex) {
                continue;
            }

            String iccid;
            switch (option) {
                case 0:
                    System.in.read();
                    System.exit(0);

                case 1:
                    System.out.println("\nProfiles List:");
                    System.out.println(profiles);
                    break;

                case 2:
                    System.out.println("Enter Matching ID or Activation code: ");
                    String activationCode = reader.readLine();

                    try {
                        lpa.downloadProfile(activationCode, dwnProgress);

                    } catch (Exception ex) {
                        System.out.println("Download profile failed. ");
                        ex.printStackTrace();

                    }
                    break;
                case 3:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {
                        lpa.enableProfile(iccid, progress);

                    } catch (Exception ex) {
                        System.out.println("Enable profile failed. ");
                        ex.printStackTrace();

                    }

                    break;
                case 4:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {
                        lpa.disableProfile(iccid, progress);

                    } catch (Exception ex) {
                        System.out.println("Disable profile failed. ");
                        ex.printStackTrace();

                    }
                    break;

                case 5:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {
                        lpa.deleteProfile(iccid, progress);

                    } catch (Exception ex) {
                        System.out.println("Delete profile failed.");
                        ex.printStackTrace();

                    }
                    break;

                case 6:

                    System.out.println("EID: " + lpa.getEID());
                    break;
                case 7:
                    try {
                        lpa.processPendingNotifications();

                    } catch (Exception ex) {
                        System.out.println("Process Notifications failed. ");
                        ex.printStackTrace();

                    }

                    break;
                default:
                    break;

            }
        }

    }

    private static Properties readProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("app.properties");

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

}
