package com.truphone.lpap.qrcode;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class WebcamQRCodeExample extends JDialog implements Runnable, ThreadFactory {

    private static final long serialVersionUID = 6441489157408381878L;

    private Executor executor = Executors.newSingleThreadExecutor(this);

    private Webcam webcam = null;
    private WebcamPanel panel = null;
//    private JTextArea textarea = null;
    private JButton btnCancel = null;
    private boolean loop = true;

    public int result;
    private String activationCode = "";

    public String getActivationCode() {
        return activationCode;

    }

    public WebcamQRCodeExample() {
        super();


    }

    public boolean init() {
        setLayout(new FlowLayout());
        setTitle("Read QR / Bar Code With Webcam");

        Dimension size = WebcamResolution.QVGA.getSize();

        List<Webcam> cams = Webcam.getWebcams();

        if (cams.size() == 0 || cams.get(0) == null) {
            return false;
        }

        JPanel mainPanel = new JPanel();
        webcam = cams.get(0);
        webcam.setViewSize(size);

        
        panel = new WebcamPanel(webcam);
        panel.setPreferredSize(size);
        panel.setFPSDisplayed(true);

//        mainPanel.setSize((int) size.getWidth(), (int) (size.getHeight() + 20));
//        mainPanel.add(panel);
        //this.setSize((int)size.getWidth(), (int)(size.getHeight()+20));

//        btnCancel = new JButton("Cancel");
//        btnCancel.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                loop = false;
//
//            }
//        });

        this.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                loop=false;
                webcam.close();
                Webcam.getDiscoveryService().stop();
            }

            public void windowClosing(WindowEvent e) {
               
            }
        });

        
        mainPanel.add(panel);
        this.add(mainPanel);
        this.setSize(500,500);
        pack();

        this.setVisible(true);
        this.setModal(true);
        executor.execute(this);
        
        return true;
    }

    @Override
    public void run() {

        while (loop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Result result = null;
            BufferedImage image = null;

            if (webcam.isOpen()) {

                if ((image = webcam.getImage()) == null) {
                    continue;
                }

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    result = new MultiFormatReader().decode(bitmap);
                } catch (NotFoundException e) {
                    // fall thru, it means there is no QR code in image
                }
            }

            if (result != null) {
                System.out.println(result.getText());
                String text = result.getText();

                String[] acparts = text.split("\\$");
                StringBuilder sb = new StringBuilder();
                if (text.toLowerCase().startsWith("lpa:1$") && acparts.length >= 3) {
                    sb.append("Server address: " + acparts[1] + "\n");
                    sb.append("MatchingId: " + acparts[2] + "\n");
                    if (acparts.length > 3) {
                        sb.append("OID: " + acparts[3] + "\n");
                    } else {
                        sb.append("OID: " + "\n");
                    }
                    if (acparts.length > 4) {
                        sb.append("Confirmation Code: " + acparts[4] + "\n");
                    } else {
                        sb.append("Confirmation Code: " + "\n");
                    }

                    activationCode = text.substring(4);
                    System.out.println("CLOSING");
                    System.out.println("ACTIVATION CODE: " + activationCode);

                    loop = false;
                    dispose();

                } else {
                    sb.append(String.format("Invalid Activation Code (%s)", result));
                    activationCode = "";
                    System.out.println("INVALID");
                }

//                textarea.setText(sb.toString());
            }

        }
        webcam.close();
        Webcam.getDiscoveryService().stop();
        dispose();
    }

    @Override

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "example-runner");
        t.setDaemon(true);
        return t;
    }

    public static void main(String[] args) {
        new WebcamQRCodeExample();
    }
}
