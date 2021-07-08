package com.truphone.lpap.qrcode;

import java.awt.FlowLayout;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;

public class WebcamQRCodeScanner extends JDialog {

    private static final long serialVersionUID = 6441489157408381878L;
    
    private QRCodeComponent qrCodeComponent = null;

    private boolean loop = true;

    public int result;
    
    private String activationCode = "";

    public String getActivationCode() {
        return activationCode;

    }

    public WebcamQRCodeScanner() {
        super();
        init();
    }

    public boolean init() {
        setLayout(new FlowLayout());
        setTitle("Read QR / Bar Code With Webcam");

        qrCodeComponent = new QRCodeComponent();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                qrCodeComponent.dispose();
            }

            @Override
            public void windowClosing(WindowEvent e) {
               
            }
        });
        
        qrCodeComponent.addTextListener(new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                testInput();
            }
        });

        this.add(qrCodeComponent);
        this.setSize(500,500);
//        pack();

        this.setModal(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        return true;
    }
    
    private void testInput() {
        final String fullInput = qrCodeComponent.getFullInput();
        final String qrAddress = qrCodeComponent.getAddress();
        final String qrMatchingId= qrCodeComponent.getMatchingId();
        
        if (qrAddress != null && qrMatchingId != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Server address: ").append(qrAddress).append("\n");
            sb.append("MatchingId: ").append(qrMatchingId).append("\n");
//            if (acparts.length > 3) {
//                sb.append("OID: " + acparts[3] + "\n");
//            } else {
//                sb.append("OID: " + "\n");
//            }
//            if (acparts.length > 4) {
//                sb.append("Confirmation Code: " + acparts[4] + "\n");
//            } else {
//                sb.append("Confirmation Code: " + "\n");
//            }
            System.out.println(sb);
            activationCode = fullInput.substring(4);
            System.out.println("CLOSING");
            System.out.println("ACTIVATION CODE: " + activationCode);

            this.dispose();

        } else {
            System.out.println(String.format("Invalid Activation Code (%s)", fullInput));
        }
    }
    
    @Override
    public void dispose() {
        qrCodeComponent.dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        final WebcamQRCodeScanner scanner = new WebcamQRCodeScanner();
        scanner.setVisible(true);
    }
}
