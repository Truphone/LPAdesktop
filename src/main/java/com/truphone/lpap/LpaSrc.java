/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.truphone.lpap.card.ApduChannelImpl;
import com.truphone.lpa.impl.LocalProfileAssistantImpl;
import com.truphone.lpa.progress.DownloadProgress;
import com.truphone.lpad.progress.Progress;
import com.truphone.lpad.progress.ProgressListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.smartcardio.CardException;

/**
 *
 * @author amilcar.pereira
 */
public class LpaSrc {
    private static java.util.logging.Logger LOG = Logger.getLogger(LpaSrc.class.getName());
    LocalProfileAssistantImpl lpa;
    DownloadProgress dwnProgress;
    Progress progress;
    ApduChannelImpl apduChannel;

    String cardReaderName;

    public LpaSrc(String cardReader) throws CardException {
        
        
        this.cardReaderName = cardReader;
        //START THE LPA
        apduChannel = new ApduChannelImpl(cardReader);

        lpa = new LocalProfileAssistantImpl(apduChannel);

        dwnProgress = new DownloadProgress();
//        dwnProgress.setProgressListener(new ProgressListener() {
//            @Override
//            public void onAction(String phase, String step, Double percentage, String message) {
//                LOG.log(Level.FINE, message);
//                //System.out.println(phase + "|" + step + "|" + percentage.toString() + "|" + message);
//            }
//        });

        progress = new Progress();
    }

    public String getEID() {
        return lpa.getEID();
    }

    public List<Map<String, String>> getProfiles() {
        return lpa.getProfiles();
    }

//    public DownloadProgress getdownloadProgress(){
//        return dwnProgress;
//    }
    
    public void downloadProfile(String activationCode) throws Exception {
        lpa.downloadProfile(activationCode, dwnProgress);
        resetLpa();
        processPendingNotificaitons();
    }

    public void enableProfile(String aidOrIccid) throws CardException {
        lpa.enableProfile(aidOrIccid, progress);
        resetLpa();
        processPendingNotificaitons();
    }

    public void disableProfile(String aidOrIccid) throws CardException {
        lpa.disableProfile(aidOrIccid, progress);
        resetLpa();
        processPendingNotificaitons();
    }

    public void deleteProfile(String aidOrIccid) throws CardException {
        lpa.deleteProfile(aidOrIccid, progress);
        resetLpa();
        processPendingNotificaitons();
    }
    
    public void processPendingNotificaitons(){
        lpa.processPendingNotifications();
    }

    public void resetLpa() throws CardException {

        apduChannel.close();

        apduChannel = new ApduChannelImpl(cardReaderName);
        lpa = new LocalProfileAssistantImpl(apduChannel);

//        dwnProgress = new DownloadProgress();
//        dwnProgress.setProgressListener(new ProgressListener() {
//            @Override
//            public void onAction(String phase, String step, Double percentage, String message) {
//
//                System.out.println(phase + "|" + step + "|" + percentage.toString() + "|" + message);
//            }
//        });
//
//        progress = new Progress();

    }
    
    public void setSMDPAddress(String dpAddress){
         lpa.setDefaultSMDP(dpAddress, progress);
    }
    
    public String getSMDPAddress(){
        return lpa.getDefaultSMDP();
    }
    
    public void setProgressListener(ProgressListener listener){
        this.progress.setProgressListener(listener);
    }
    
    public void disconnect() {
        try{
            apduChannel.close();
            
        }catch(CardException ex){
           LOG.warning("Failed to close ApduChannel. " + ex.getMessage());
        }
        
        lpa=null;
        
    }
    
    public void smdpRetrieveEvents(){
        lpa.smdsRetrieveEvents(progress);
        //resetLpa();
        //processPendingNotificaitons();
    }
}
