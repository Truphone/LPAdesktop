/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.truphone.lpad.progress.ProgressListener;
import com.truphone.rsp.dto.asn1.rspdefinitions.EuiccConfiguredAddressesResponse;
import com.truphone.util.LogStub;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultEditorKit;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author amilcar.pereira
 */
public class LPAUI extends javax.swing.JFrame {

    private static java.util.logging.Logger LOG = null;
    private WaitingDialog waitDlg;

    static {
//        InputStream stream = main.class.getClassLoader().
//                getResourceAsStream("logging.properties");
        //try {
        System.setProperty("java.util.logging.config.file",
                "logging.properties");
        //LogManager.getLogManager().readConfiguration(stream);
        LOG = Logger.getLogger(main.class.getName());

        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    LpaSrc lpa;
    String serverAddress = "", ssl_validation = "", keystore_file = "";
    String cardReaderToUse = "", cardReaderFromProps = "";
    private Point initialClick;

    /**
     * Creates new form LPAUI
     */
    public LPAUI() {
        initComponents();

        LogStub.getInstance().setAndroidLog(true);

        LogStub.getInstance().setLogLevel(Level.ALL);
        LOG.log(Level.INFO, "STARTING");

        //GET SERVER ADDRESS AND KEYSTORE FROM PROPERTIES
        Properties prop = null;
        try {
            prop = Util.readProperties();
        } catch (URISyntaxException ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to read configuration\nReason: %s", ex.getMessage()));
        }

        //red properties from file
        if (prop != null && !StringUtils.isEmpty(prop.getProperty("serverAddress")) && !StringUtils.isEmpty(prop.getProperty("keystore_file")) && !StringUtils.isEmpty(prop.getProperty("ssl_validation"))) {
            serverAddress = prop.getProperty("serverAddress");
            ssl_validation = prop.getProperty("ssl_validation");
            keystore_file = prop.getProperty("keystore_file");

            if (!StringUtils.isEmpty(prop.getProperty("card_reader"))) {
                cardReaderFromProps = prop.getProperty("card_reader");
            }
        } else {
            Util.showMessageDialog(this, "Failed to load config\nOne of the following paramters is missing: serverAddress, ssl_validation,keystore_file");
        }

        try {
            refreshReadersList();
        } catch (CardException ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to list available readers\nReason: %s\nCheck the logs for more info",ex.getMessage()));
        }

        if (ssl_validation.toLowerCase().compareTo("true") == 0) {
            System.setProperty("javax.net.ssl.trustStore", keystore_file);
        } else {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (GeneralSecurityException e) {
            }
        }

        lblProgress.setVisible(false);

        waitDlg = new WaitingDialog(this, true);

        this.setIconImage(new ImageIcon(getClass().getResource("/tru_logo.png")).getImage());

        setupWindowDragging(headerPanel);
        setupWindowDragging(mainPanel);
        //SET FRAME BORDER
        this.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(0, 50, 63)));

        //SET SHORTCUTS ON MAC
        boolean isMacOs = (System.getProperty("os.name").toLowerCase().contains("mac"));
        if (isMacOs) {
            Util.setUpMacShortcuts(txtEuiccInfo.getInputMap());
        }

    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popUpProfiles = new javax.swing.JPopupMenu();
        miEnableProfile = new javax.swing.JMenuItem();
        miDisableProfile = new javax.swing.JMenuItem();
        miDeleteProfile = new javax.swing.JMenuItem();
        mainPanel = new javax.swing.JPanel();
        cmbReaders = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProfiles = new javax.swing.JTable(){
            public boolean isCellEditable(int row,int column){
                return false;
            }
        }; ;
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnRefreshReaders = new javax.swing.JButton();
        btnAddProfile = new javax.swing.JButton();
        btnConnect = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtEuiccInfo = new javax.swing.JTextArea();
        btnSetSMDPAddress = new javax.swing.JButton();
        lblProgress = new javax.swing.JLabel();
        headerPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        miEnableProfile.setText("Enable");
        miEnableProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEnableProfileActionPerformed(evt);
            }
        });
        popUpProfiles.add(miEnableProfile);

        miDisableProfile.setText("Disable");
        miDisableProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miDisableProfileActionPerformed(evt);
            }
        });
        popUpProfiles.add(miDisableProfile);

        miDeleteProfile.setText("Delete");
        miDeleteProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miDeleteProfileActionPerformed(evt);
            }
        });
        popUpProfiles.add(miDeleteProfile);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TRU LPAd(esktop)");
        setBackground(new java.awt.Color(255, 255, 255));
        setUndecorated(true);
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));

        tblProfiles.setComponentPopupMenu(popUpProfiles);
        jScrollPane1.setViewportView(tblProfiles);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 50, 63));
        jLabel1.setText("eUICC Info");

        jLabel2.setForeground(new java.awt.Color(0, 50, 63));
        jLabel2.setText("Card Reader");

        btnRefreshReaders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/refresh.png"))); // NOI18N
        btnRefreshReaders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshReadersActionPerformed(evt);
            }
        });

        btnAddProfile.setForeground(new java.awt.Color(0, 50, 63));
        btnAddProfile.setText("Download Profile");
        btnAddProfile.setEnabled(false);
        btnAddProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProfileActionPerformed(evt);
            }
        });

        btnConnect.setForeground(new java.awt.Color(0, 50, 63));
        btnConnect.setText("(Re)connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 50, 63));
        jLabel3.setText("Profiles");

        txtEuiccInfo.setEditable(false);
        txtEuiccInfo.setColumns(20);
        txtEuiccInfo.setRows(5);
        jScrollPane2.setViewportView(txtEuiccInfo);

        btnSetSMDPAddress.setForeground(new java.awt.Color(0, 50, 63));
        btnSetSMDPAddress.setText("Set SMDP+ Address");
        btnSetSMDPAddress.setEnabled(false);
        btnSetSMDPAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetSMDPAddressActionPerformed(evt);
            }
        });

        lblProgress.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wait_small.gif"))); // NOI18N
        lblProgress.setText("Processing");

        headerPanel.setBackground(new java.awt.Color(0, 50, 63));
        headerPanel.setToolTipText("");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truphone-brand-small.png"))); // NOI18N
        jLabel4.setText("LPA");

        jButton1.setBackground(new java.awt.Color(0, 50, 63));
        jButton1.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("X");
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.setBounds(new java.awt.Rectangle(0, 0, 97, 29));
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addComponent(jButton1))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addGap(0, 256, Short.MAX_VALUE)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSetSMDPAddress, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefreshReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblProgress)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbReaders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(btnRefreshReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1))
                    .addComponent(btnConnect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSetSMDPAddress)
                .addGap(28, 28, 28)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshReadersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshReadersActionPerformed
        try {
            refreshReadersList();
        } catch (CardException ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to refresh readers list\nReason: %s\nPlease check the log.",ex.getMessage()));
        }
    }//GEN-LAST:event_btnRefreshReadersActionPerformed

    private void miEnableProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEnableProfileActionPerformed

        int selectedRow = tblProfiles.getSelectedRow();
        String isdp_aid = (String) tblProfiles.getValueAt(selectedRow, 4);

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                setProcessing(true);
                try {
                    lpa.enableProfile(isdp_aid);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.toString());
                    Util.showMessageDialog(null, String.format("Something went wrong\n Reason: %s \nPlease check the log for more info.", ex.getMessage()));
                }

                listProfiles();

                setProcessing(false);

                return null;
            }
        };

        sw.execute();

    }//GEN-LAST:event_miEnableProfileActionPerformed

    private void miDisableProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miDisableProfileActionPerformed

        int selectedRow = tblProfiles.getSelectedRow();
        String isdp_aid = (String) tblProfiles.getValueAt(selectedRow, 4);

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                setProcessing(true);
                try {
                    lpa.disableProfile(isdp_aid);

                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.toString());
                    //Util.showMessageDialog(null, "Failed to Disable the profile with AID " + isdp_aid + ". Please check the log.");
                    Util.showMessageDialog(null, String.format("Something wen't wrong\n Reason: %s\nPlease check the log for more info.", ex.getMessage()));
                }

                listProfiles();
                setProcessing(false);

                return null;
            }
        };

        sw.execute();

    }//GEN-LAST:event_miDisableProfileActionPerformed

    private void miDeleteProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miDeleteProfileActionPerformed

        int selectedRow = tblProfiles.getSelectedRow();
        String iccid = (String) tblProfiles.getValueAt(selectedRow, 0);
        String profileName = (String) tblProfiles.getValueAt(selectedRow, 3);
        String isdp_aid = (String) tblProfiles.getValueAt(selectedRow, 4);

        if (JOptionPane.showConfirmDialog(this, String.format("Are you sure you want to delete the profile %s - ICCID %s - AID %s", profileName, iccid, isdp_aid), "Delete Profile", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            SwingWorker sw = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    setProcessing(true);
                    try {

                        lpa.deleteProfile(isdp_aid);

                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, ex.toString());
                        //Util.showMessageDialog(null, "Failed to Enable the profile with AID " + isdp_aid + ". Please check the log.");
                        Util.showMessageDialog(null, String.format("Something went wrong\nReason: %s\nPlease check the log for more info.", ex.getMessage()));
                    }

                    listProfiles();
                    setProcessing(false);
                    return null;

                }
            };
            sw.execute();

        }

    }//GEN-LAST:event_miDeleteProfileActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed

        if (cmbReaders.getItemCount() > 0) {
            try {
                lpa = new LpaSrc(serverAddress, (String) cmbReaders.getSelectedItem());
            } catch (CardException ex) {
                LOG.log(Level.SEVERE, ex.toString());
                //Util.showMessageDialog(this, String.format("Failed to start LPA: %s. Please check the log for more info.", ex.getMessage()));
                Util.showMessageDialog(this, String.format("Failed to start LPA\nReason: %s\nPlease check the log for more info.", ex.getMessage()));
                return;
            }
        } else {
            Util.showMessageDialog(this, "No reader selected");
        }

        lpa.setProgressListener(new ProgressListener() {
            @Override
            public void onAction(String phase, String step, Double percentage, String message) {
//                String statusText = step + "-" + message;
                //lblProgress.setText(statusText);
                //progressBar.setValue(percentage.intValue() * 100);
                //progressBar.updateUI();
//                System.out.println("Phase: " + phase);
//                System.out.println("Step: " + step);
//                System.out.println("Percentage: " + percentage.toString());
//                System.out.println("Message: " + message);
            }
        });

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                setProcessing(true);
                listProfiles();
                updateEuiccInfo();
                setProcessing(false);

                btnAddProfile.setEnabled(true);
                btnSetSMDPAddress.setEnabled(true);

                return null;
            }
        };

        sw.execute();


    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnAddProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProfileActionPerformed

        //String activationCode = JOptionPane.showInputDialog(this, "Enter activation code", "1$rsp.truphone.com$");
      
       
        String activationCode = Util.showInputDialog(this, "Enter activation code","1$rsp.truphone.com$");
        
        if (activationCode == null) {
            return;
        }

        SwingWorker sw = new SwingWorker() {
            @Override

            protected Object doInBackground() throws Exception {
                setProcessing(true);
                try {
                    lpa.downloadProfile(activationCode);

                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.toString());
                    //Util.showMessageDialog(null, "Failed to download the profile. Please check the log.");
                    Util.showMessageDialog(null, String.format("Something went wrong\nReason: %s\nPlease check the log for more info.", ex.getMessage()));
                }
                listProfiles();
                setProcessing(false);

                return null;
            }
        };

        sw.execute();

    }//GEN-LAST:event_btnAddProfileActionPerformed

    private void btnSetSMDPAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetSMDPAddressActionPerformed
       
        String address = JOptionPane.showInputDialog(this, "Enter new SMDP+ address");

        //lpa.setSMDPAddress(com.truphone.util.Util.ASCIIToHex(address));
        setProcessing(true);

        lpa.setSMDPAddress(address);
        updateEuiccInfo();

        setProcessing(false);
    }//GEN-LAST:event_btnSetSMDPAddressActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProfile;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnRefreshReaders;
    private javax.swing.JButton btnSetSMDPAddress;
    private javax.swing.JComboBox<String> cmbReaders;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem miDeleteProfile;
    private javax.swing.JMenuItem miDisableProfile;
    private javax.swing.JMenuItem miEnableProfile;
    private javax.swing.JPopupMenu popUpProfiles;
    private javax.swing.JTable tblProfiles;
    private javax.swing.JTextArea txtEuiccInfo;
    // End of variables declaration//GEN-END:variables

    private void initLPA() throws URISyntaxException, Exception {

    }

    private void refreshReadersList() throws CardException {
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        CardTerminals cardTerminals = terminalFactory.terminals();

        cmbReaders.removeAllItems();
        for (CardTerminal terminal : cardTerminals.list()) {
            cmbReaders.addItem(terminal.getName());
        }
    }

    private void listProfiles() {
        try {

            List<Map<String, String>> profiles = lpa.getProfiles();

            DefaultTableModel model = new DefaultTableModel();

            model.addColumn("Iccid");
            model.addColumn("Name");
            model.addColumn("State");
            model.addColumn("Spn");
            model.addColumn("Aid");
            model.addColumn("Class");

            for (Map<String, String> profile : profiles) {
                String[] fields = new String[profile.size()];

                if (profile.containsKey("ICCID")) {
                    String iccidUnswapped = Util.swapNibblesOnString(profile.get("ICCID"));
                    fields[0] = iccidUnswapped.toLowerCase().charAt(19) == 'f' ? iccidUnswapped.substring(0, 19) : iccidUnswapped.substring(0, 20);

                }

                if (profile.containsKey("NAME")) {
                    fields[1] = profile.get("NAME");
                }

                if (profile.containsKey("PROFILE_STATE")) {
                    fields[2] = profile.get("PROFILE_STATE").compareTo("1") == 0 ? "Enabled" : "Disabled";

                }

                if (profile.containsKey("PROVIDER_NAME")) {
                    fields[3] = profile.get("PROVIDER_NAME");
                }

                if (profile.containsKey("ISDP_AID")) {
                    fields[4] = profile.get("ISDP_AID");
                }

                if (profile.containsKey("PROFILE_CLASS")) {
                    String profileClass = profile.get("PROFILE_CLASS");
                    if (profileClass.compareTo("0") == 0) {
                        fields[5] = "Test";
                    } else if (profileClass.compareTo("1") == 0) {
                        fields[5] = "Provisioning";
                    } else if (profileClass.compareTo("2") == 0) {
                        fields[5] = "Operational";
                    }

                }

                model.addRow(fields);

            }

            tblProfiles.setModel(model);
            if (model.getRowCount() > 0) {
                tblProfiles.setRowSelectionInterval(0, 0);
            }

            tblProfiles.getColumnModel().getColumn(0).setPreferredWidth(130);
//        tblProfiles.getColumnModel().getColumn(1).setPreferredWidth(150);
            tblProfiles.getColumnModel().getColumn(2).setPreferredWidth(50);
            tblProfiles.getColumnModel().getColumn(3).setPreferredWidth(70);
            tblProfiles.getColumnModel().getColumn(4).setPreferredWidth(130);
//        tblProfiles.getColumnModel().getColumn(5).setPreferredWidth(100);

        } catch (Exception ex) {
            LOG.log(Level.WARNING, ex.toString());
            Util.showMessageDialog(null, String.format("Failed to refresh profiles list \nReason: %s \nPlease check the log for more info.", ex.getMessage()));
        }
    }

    private void updateEuiccInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Eid: ").append(lpa.getEID()).append(System.getProperty("line.separator"));

        InputStream is;
        EuiccConfiguredAddressesResponse configuredAddress = new EuiccConfiguredAddressesResponse();
        String rootDsAddress = "", defaultSmdpAddress = "";

        try {
            is = new ByteArrayInputStream(Hex.decodeHex(lpa.getSMDPAddress().toCharArray()));
            configuredAddress.decode(is);
            rootDsAddress = configuredAddress.getRootDsAddress() != null ? configuredAddress.getRootDsAddress().toString() : "";
            defaultSmdpAddress = configuredAddress.getDefaultDpAddress() != null ? configuredAddress.getDefaultDpAddress().toString() : "";
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to get SMDP+ & SMDS addresses\nReason:%s\nCheck the logs for more info",ex.getMessage()));
        }

        sb.append("Root SM-DS: ").append(rootDsAddress).append(System.getProperty("line.separator"));
        sb.append("Default SM-DP: ").append(defaultSmdpAddress).append(System.getProperty("line.separator"));

        txtEuiccInfo.setText(sb.toString());

    }

    private void setProcessing(boolean processing) {
//        Thread t = new Thread() {
//            public void run() {
        lblProgress.setVisible(processing);
        btnRefreshReaders.setEnabled(!processing);
        btnConnect.setEnabled(!processing);
        btnSetSMDPAddress.setEnabled(!processing);
        btnAddProfile.setEnabled(!processing);
        cmbReaders.setEditable(!processing);

//            }
//        };
//        
//        t.start();
    }

//    private void showMessage(JFrame parent, String text) {
//
//        Util.showMessageDialog(parent, String.format("<html>%s</html>", text));
//    }

    private void setupWindowDragging(JPanel panel) {
        //TO MOVE THE WINDOW WITH NEW BAR
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                // get location of Window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });
    }

}
