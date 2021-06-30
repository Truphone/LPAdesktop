/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frederico.palma
 */
public class QRCodeProcessor implements WebcamListener {
    
    private static java.util.logging.Logger LOG = null;
    
    private String address = null;
    
    private String matchingId = null;
    
    private final List<TextListener> textListeners = new ArrayList<>();
    
    public QRCodeProcessor() {
        LOG = Logger.getLogger(LPAUI.class.getName());
    }
    
    public void addTextListener(final TextListener listener) {
        textListeners.add(listener);
    }
    
    public void removeTextListener(final TextListener listener) {
        textListeners.remove(listener);
    }
    
    public void notifyTextListeners(TextEvent event) {
        for (TextListener listener : textListeners) {
            listener.textValueChanged(event);
        }
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public String getMatchingId() {
        return matchingId;
    }

    @Override
    public void webcamOpen(WebcamEvent we) {
    }

    @Override
    public void webcamClosed(WebcamEvent we) {
    }

    @Override
    public void webcamDisposed(WebcamEvent we) {
    }

    @Override
    public void webcamImageObtained(WebcamEvent we) {
        Result result = null;
        
        LuminanceSource source = new BufferedImageLuminanceSource(we.getImage());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            result = new MultiFormatReader().decode(bitmap);
        } catch (NotFoundException e) {
            // fall thru, it means there is no QR code in image
        }
        
        if (result != null) {
            if (!updateFields(result.getText())) {
                LOG.log(Level.INFO, "Read invalid qrcode: " + result.getText());
            } else {
                LOG.log(Level.INFO, "Read valid qrcode: " + result.getText());
            }
        }
    }
    
    private boolean updateFields(final String input) {
        if (input != null && input.toLowerCase().startsWith("lpa:1$")) {
            final String[] tokens = input.split("\\$");
            
            if (tokens.length == 3) {
                address = tokens[1];
                matchingId = tokens[2];
                notifyTextListeners(new TextEvent(this, TextEvent.TEXT_VALUE_CHANGED));
                return true;
            }
        }
        address = null;
        matchingId = null;
        notifyTextListeners(new TextEvent(this, TextEvent.TEXT_VALUE_CHANGED));
        return false;
    }
}
