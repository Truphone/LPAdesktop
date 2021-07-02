/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author frederico.palma
 */
public class QRCodeComponent extends JPanel implements WebcamListener {
    
    private static java.util.logging.Logger LOG = null;
    
    private static final JPanel EMPTY_PANEL = new JPanel();
    
    private final JComboBox<String> cmbCamera = new JComboBox<>();
    
    private Webcam webcam = null;

    private WebcamPanel panel = null;
    
    private String address = null;
    
    private String matchingId = null;
    
    private final List<TextListener> textListeners = new ArrayList<>();
    
    public QRCodeComponent() {
        LOG = Logger.getLogger(LPAUI.class.getName());
        initComponents();
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
    
    public static void main(String[] args) {
        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.getContentPane().setLayout(new GridLayout(1, 1));
        frm.setSize(300, 300);
        QRCodeComponent pr = new QRCodeComponent();
        frm.getContentPane().add(pr);
        frm.setVisible(true);
    }
    
    private void initComponents() {
        setBackground(new java.awt.Color(255, 255, 255));
        EMPTY_PANEL.setBackground(new java.awt.Color(255, 255, 255));
        EMPTY_PANEL.setMinimumSize(new java.awt.Dimension(300, 0));
        EMPTY_PANEL.setPreferredSize(new java.awt.Dimension(300, 0));
        EMPTY_PANEL.setSize(new java.awt.Dimension(300, 100));
        fillCmbCamera();
        cmbCamera.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCameraItemStateChanged(evt);
            }
        });
        
        BorderLayout layout = new BorderLayout();
        layout.setHgap(20);
        setLayout(layout);
        
        add(cmbCamera, BorderLayout.NORTH);
    }
    
    protected void fillCmbCamera() {
        if (cmbCamera.getItemCount() > 0) {
            cmbCamera.setSelectedIndex(0);
        }
        final List<String> webcamNames = WebcamHandler.getWebcamNames(true);
        cmbCamera.removeAllItems();
        for (String name : webcamNames) {
            cmbCamera.addItem(name);
        }
        cmbCamera.setSelectedIndex(0);
    }
    
    private void cmbCameraItemStateChanged(java.awt.event.ItemEvent evt) {                                           
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (webcam != null) {
                panel.stop();
                remove(panel);
                
                webcam.removeWebcamListener(this);
                webcam.close();
            } else {
                remove(EMPTY_PANEL);
            }
            webcam = WebcamHandler.getWebcamByName((String) evt.getItem());
            if (webcam != null) {
                LOG.log(Level.INFO, "Using camera: " + webcam.getName());
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.addWebcamListener(this);

                panel = new WebcamPanel(webcam, false);
                panel.setFPSDisplayed(true);
                panel.setMirrored(true);

                add(panel, BorderLayout.CENTER);

                Thread t = new Thread() {

                        @Override
                        public void run() {
                                panel.start();
                        }
                };
                t.setName("example-stoper");
                t.setDaemon(true);
                t.start();
            } else {
                add(EMPTY_PANEL, BorderLayout.CENTER);
            }
            revalidate();
        }
    }
    
    public void dispose() {
        if (webcam != null) {
            panel.stop();
            webcam.close();
        }
    }
}
