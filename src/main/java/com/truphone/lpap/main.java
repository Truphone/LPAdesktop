/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import ch.qos.logback.core.FileAppender;
import com.truphone.lpa.impl.LocalProfileAssistantImpl;
import com.truphone.lpa.progress.DownloadProgress;
import com.truphone.lpad.progress.Progress;
import com.truphone.lpad.progress.ProgressListener;
import com.truphone.util.LogStub;
import com.truphone.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author amilcar.pereira
 */
public class main {

    private static java.util.logging.Logger LOG =null;

    static {
        InputStream stream = main.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            LOG = Logger.getLogger(main.class.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException, Exception {

        LogStub.getInstance().setAndroidLog(true);

        LogStub.getInstance().setLogLevel(Level.ALL);
        LOG.log(Level.INFO, "STARTING");

        //GET SERVER ADDRESS AND KEYSTORE FROM PROPERTIES
        Properties prop = readProperties();

        String serverAddress = "", keystore_file = "";
        String cardReaderToUse = "", cardReaderFromProps = "";

        //red properties from file
        if (prop != null && !StringUtils.isEmpty(prop.getProperty("serverAddress")) && !StringUtils.isEmpty(prop.getProperty("keystore_file"))) {
            serverAddress = prop.getProperty("serverAddress");
            keystore_file = prop.getProperty("keystore_file");

            if (!StringUtils.isEmpty(prop.getProperty("card_reader"))) {
                cardReaderFromProps = prop.getProperty("card_reader");
            }
        } else {
            throw new Exception("serverAddress and keystore_file must be set on app.properties file");
        }

        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        CardTerminals cardTerminals = terminalFactory.terminals();

        if (cardTerminals.list().size() == 0) {
            throw new Exception("No card readers detected!");
        }
        if (cardTerminals.list().size() == 1) {
            cardReaderToUse = cardTerminals.list().get(0).getName();
        } else {
            CardTerminal terminal = cardTerminals.getTerminal(cardReaderFromProps);
            if (terminal == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("The card reader set on properties file (").append(cardReaderFromProps).append(") could not be found.").append(System.getProperty("line.separator"));
                sb.append("Readers available: ").append(System.getProperty("line.separator"));
                for (CardTerminal t : cardTerminals.list()) {
                    sb.append(t.getName()).append(System.getProperty("line.separator"));
                }

                sb.append("Please update the name or remove the property to use the default reader");
                throw new Exception(sb.toString());
            }

            cardReaderToUse = terminal.getName();

        }

        System.setProperty("javax.net.ssl.trustStore", keystore_file);

        LpaSrc lpa = new LpaSrc(cardReaderToUse);
        String eid = lpa.getEID();
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("+           Local Profile Assistant               +");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("EID: " + eid);

//        List<Map<String, String>> profiles = lpa.getProfiles();
//
//        System.out.println("\nProfiles List:");
//        System.out.println(profiles);
        System.out.println("\nProfiles List:");
        System.out.println(lpa.getProfiles());

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int option;
        while (true) {
            System.out.println("------------------------------");
            System.out.println("1-List Profiles");
            System.out.println("2-Download Profile");
            System.out.println("3-Enable Profile");
            System.out.println("4-Disable Profile");
            System.out.println("5-Delete Profile");
            System.out.println("6-Get EID");
            System.out.println("7-Process Notifications");
            System.out.println("9-Reset LPA");
            System.out.println("10-Set SMDP Address");
            System.out.println("11-Get SMDP Address");
            System.out.println("21-Download in bulk (test purposes)");

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
                    System.exit(0);

                case 1:
                    System.out.println("\nProfiles List:");
                    System.out.println(lpa.getProfiles());
                    break;

                case 2:
                    System.out.println("Enter Matching ID or Activation code: ");
                    String activationCode = reader.readLine();

                    try {
                        lpa.downloadProfile(activationCode);
                    } catch (Exception ex) {
                        System.out.println("Download profile failed. ");
                        System.out.println(ex.toString());

                        System.out.println("PRESS ANY KEY TO CONTINUE");
                        System.in.read();
                        lpa.processPendingNotificaitons();
                    }

                    break;
                case 3:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {

                        lpa.enableProfile(iccid);

                    } catch (Exception ex) {
                        System.out.println("Enable profile failed. ");
                        ex.printStackTrace();

                    }

                    break;
                case 4:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {

                        lpa.disableProfile(iccid);

                    } catch (Exception ex) {
                        System.out.println("Disable profile failed. ");
                        ex.printStackTrace();

                    }
                    break;

                case 5:
                    System.out.println("Enter ICCID or AID:");
                    iccid = reader.readLine();

                    try {

                        lpa.deleteProfile(iccid);

                    } catch (Exception ex) {
                        System.out.println("Delete profile failed.");
                        ex.printStackTrace();

                    }
                    break;

                case 6:

                    System.out.println("EID: " + lpa.getEID());
                    break;
                case 7:
//                    try {
                    lpa.processPendingNotificaitons();

//                    } catch (Exception ex) {
//                        System.out.println("Process Notifications failed. ");
//                        ex.printStackTrace();
//
//                    }
                    break;

                case 9:
                    //RESTART THE LPA
                    lpa.resetLpa();

                    break;
                case 10:
                    //set SMDP Address

                    System.out.println("Enter DP+ Address:");
                    String dpAddress = reader.readLine();

                    lpa.setSMDPAddress(Util.ASCIIToHex(dpAddress));

                    break;
                case 11:
                    //set SMDP Address
                    System.out.println("Default SMDP Address: " + lpa.getSMDPAddress());

                    break;
                case 21:

                    //download in bulk, for test purposes. 
                    System.out.println("Enter the file with Activation Codes (one per line)");
                    String filename = reader.readLine();

                    File f = new File(filename);
                    if (!f.exists()) {
                        System.out.println("File not found");
                        break;
                    }

                    List<String> activationCodes = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);

                    for (String ac : activationCodes) {
                        if (ac.length() == 0 || ac.startsWith("//")) {
                            continue;
                        }

                        System.out.println("------------------------------------------------");
                        System.out.println("    Start processing AC " + ac);
                        System.out.println("------------------------------------------------");

                        //---------install
                        try {
                            lpa.downloadProfile(ac);
                        } catch (Exception ex) {
                            System.out.println("ERROR: Download profile failed for Activation Code " + ac);
                            ex.printStackTrace();
                            continue;
                        }

                        //---------retrieve the last profile installed
                        List<Map<String, String>> profiles = lpa.getProfiles();
                        Map<String, String> profile = profiles.get(profiles.size() - 1);
                        iccid = profile.get("ICCID");

                        //---------delete
                        try {

                            lpa.deleteProfile(iccid);

                        } catch (Exception ex) {
                            System.out.println("ERROR: Delete profile failed.");
                            ex.printStackTrace();

                        }

                        System.out.println("------------------------------------------------");
                        System.out.println("    End processing AC " + ac);
                        System.out.println("------------------------------------------------");

                    }

                    //get the filename
                    break;
                default:
                    break;

            }
        }

    }

    private static Properties readProperties() throws URISyntaxException {
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

}
