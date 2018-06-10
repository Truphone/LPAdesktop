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
import java.util.HashMap;
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

        //rsp-demo
        //System.setProperty("javax.net.ssl.trustStore","/Users/amilcar.pereira/Keys/rspdemo_es9.jks");
        //String serverAddress="https://rsp-demo.truphone.com:25101";
        //acme
        String serverAddress = "https://rsp-acme-test.truphone.com:25105";
        System.setProperty("javax.net.ssl.trustStore", "/Users/amilcar.pereira/Keys/digicertks.jks");

        // Your ApduChannelImpl implementation
        ApduChannelImpl apduChannel = new ApduChannelImpl();

        LocalProfileAssistantImpl lpa = new LocalProfileAssistantImpl(apduChannel, serverAddress);

        String eid = lpa.getEID();

        System.out.println("EID:" + eid);

        DownloadProgress dwnProgress = new DownloadProgress();
        dwnProgress.setProgressListener(new ProgressListener() {
            @Override
            public void onAction(String phase, String step, Double percentage, String message) {

                System.out.println(phase + "|" + step + "|" + percentage.toString() + "|" + message);
            }
        });

        Progress progress = new Progress();
        
        //lpa.downloadProfile("1-4W-KOUQ8Y", dwnProgress);
        //lpa.deleteProfile("A0000005591010FFFFFFFF8900001A00", progress);
        //lpa.processPendingNotifications();

    }

   
}
