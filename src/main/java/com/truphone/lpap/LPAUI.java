/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.truphone.lpad.progress.ProgressListener;
import com.truphone.rsp.dto.asn1.rspdefinitions.EuiccConfiguredAddressesResponse;
import com.truphone.util.LogStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.json.JSONObject;

/**
 *
 * @author amilcar.pereira
 */
public class LPAUI extends javax.swing.JFrame {

    private static java.util.logging.Logger LOG = null;
    private WaitingDialog waitDlg;

    LpaSrc lpa;
    //String serverAddress = "", ssl_validation = "", keystore_file = "";
    String cardReaderToUse = "", cardReaderFromProps = "";
    private Point initialClick;
    List<Map<String, String>> profiles;

    /**
     * Creates new form LPAUI
     */
    public LPAUI() {

        String loggingConfigFile = "logging.properties";


        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            loggingConfigFile = "contents/java/lib/logging.properties";
        } else {
            loggingConfigFile = "config/logging.properties";
        }

        File propFile = new File(loggingConfigFile);

        if (propFile.exists()) {
            System.setProperty("java.util.logging.config.file",
                    loggingConfigFile);
        } else {
            Util.showMessageDialog(this, "Couldn't find logging configuration");
            System.exit(0);
        }
        LOG = Logger.getLogger(LPAUI.class.getName());

//        LocalDateTime today = LocalDateTime.now();
//
//        if (today.getYear() >= 2019 && today.getMonthValue() > 12) {
//            // TODO add your handling code here:
//            StringBuilder sb = new StringBuilder();
//
//            String version = getAppVersion();
//            if (version != null && version.length() > 0) {
//                version = String.format("(V%s) ", version);
//            } else {
//                version = "";
//            }
//
//            sb.append(String.format("This version of Truphone LPAdesktop %sis no longer valid.", version)).append(System.getProperty("line.separator"));
//            sb.append("Please contact Truphone (DevicesxSIMTechnologies&Roaming@truphone.com)").append(System.getProperty("line.separator"));
//
//            Util.showMessageDialog(this, sb.toString());
//            System.exit(0);
//        }
        initComponents();

        LogStub.getInstance().setAndroidLog(true);

        LogStub.getInstance().setLogLevel(Level.ALL);
        LOG.log(Level.INFO, "STARTING");

        try {
            refreshReadersList();
        } catch (CardException ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to list available readers\nReason: %s\nCheck the logs for more info", ex.getMessage()));
        }

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
//        }

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

        //SET APP VERSION ON TITLE BAR
        String appTitle = "LPA";

        String version = getAppVersion();
        if (version != null && version.length() > 0) {
            appTitle += " v" + version;
        }

        lblTitleBar.setText(appTitle);

        listProfiles.setCellRenderer(new MyProfileCellRenderer());

        ComponentListener l = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                // next line possible if list is of type JXList
                // list.invalidateCellSizeCache();
                // for core: force cache invalidation by temporarily setting fixed height
                listProfiles.setFixedCellHeight(10);
                listProfiles.setFixedCellHeight(-1);
            }

        };

        listProfiles.addComponentListener(l);

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
        miCopyIccid = new javax.swing.JMenuItem();
        miCopyAid = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miEnableProfile = new javax.swing.JMenuItem();
        miDisableProfile = new javax.swing.JMenuItem();
        miDeleteProfile = new javax.swing.JMenuItem();
        mainPanel = new javax.swing.JPanel();
        cmbReaders = new javax.swing.JComboBox<>();
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
        lblTitleBar = new javax.swing.JLabel();
        btnCloseApp = new javax.swing.JButton();
        btnCloseApp2 = new javax.swing.JButton();
        btnHandleNotifications = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listProfiles = new javax.swing.JList<>();

        miCopyIccid.setText("Copy ICCID");
        miCopyIccid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCopyIccidActionPerformed(evt);
            }
        });
        popUpProfiles.add(miCopyIccid);

        miCopyAid.setText("Copy AID");
        miCopyAid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCopyAidActionPerformed(evt);
            }
        });
        popUpProfiles.add(miCopyAid);
        popUpProfiles.add(jSeparator1);

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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));

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
        btnAddProfile.setText("Download");
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

        lblProgress.setForeground(new java.awt.Color(0, 50, 63));
        lblProgress.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wait_2.gif"))); // NOI18N
        lblProgress.setText("Processing");

        headerPanel.setBackground(new java.awt.Color(0, 50, 63));
        headerPanel.setToolTipText("");

        lblTitleBar.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        lblTitleBar.setForeground(new java.awt.Color(255, 255, 255));
        lblTitleBar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/truphone-brand-small.png"))); // NOI18N
        lblTitleBar.setText("LPA");

        btnCloseApp.setBackground(new java.awt.Color(0, 50, 63));
        btnCloseApp.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        btnCloseApp.setForeground(new java.awt.Color(255, 255, 255));
        btnCloseApp.setText("X");
        btnCloseApp.setBorder(null);
        btnCloseApp.setBorderPainted(false);
        btnCloseApp.setBounds(new java.awt.Rectangle(0, 0, 97, 29));
        btnCloseApp.setContentAreaFilled(false);
        btnCloseApp.setFocusPainted(false);
        btnCloseApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseAppActionPerformed(evt);
            }
        });

        btnCloseApp2.setBackground(new java.awt.Color(0, 50, 63));
        btnCloseApp2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        btnCloseApp2.setForeground(new java.awt.Color(255, 255, 255));
        btnCloseApp2.setText("?");
        btnCloseApp2.setBorder(null);
        btnCloseApp2.setBorderPainted(false);
        btnCloseApp2.setBounds(new java.awt.Rectangle(0, 0, 97, 29));
        btnCloseApp2.setContentAreaFilled(false);
        btnCloseApp2.setFocusPainted(false);
        btnCloseApp2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseApp2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitleBar, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCloseApp2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCloseApp, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblTitleBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCloseApp)
                .addComponent(btnCloseApp2))
        );

        btnHandleNotifications.setForeground(new java.awt.Color(0, 50, 63));
        btnHandleNotifications.setText("Process Notifications");
        btnHandleNotifications.setEnabled(false);
        btnHandleNotifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHandleNotificationsActionPerformed(evt);
            }
        });

        listProfiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listProfiles.setComponentPopupMenu(popUpProfiles);
        jScrollPane1.setViewportView(listProfiles);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addGap(0, 190, Short.MAX_VALUE)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                        .addComponent(btnHandleNotifications)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnSetSMDPAddress))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cmbReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnRefreshReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addComponent(lblProgress)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAddProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRefreshReaders, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbReaders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2)))
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1))
                    .addComponent(btnConnect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSetSMDPAddress)
                    .addComponent(btnHandleNotifications))
                .addGap(26, 26, 26)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProgress)
                    .addComponent(btnAddProfile))
                .addContainerGap())
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
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshReadersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshReadersActionPerformed
        try {
            refreshReadersList();
        } catch (CardException ex) {
            LOG.log(Level.SEVERE, ex.toString());
            Util.showMessageDialog(this, String.format("Failed to refresh readers list\nReason: %s\nPlease check the log.", ex.getMessage()));
        }
    }//GEN-LAST:event_btnRefreshReadersActionPerformed

    private void miEnableProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEnableProfileActionPerformed

        int idx = listProfiles.getSelectedIndex();
        String isdp_aid = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ISDP_AID");

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

        int idx = listProfiles.getSelectedIndex();
        String isdp_aid = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ISDP_AID");

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

        int idx = listProfiles.getSelectedIndex();
        String isdp_aid = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ISDP_AID");
        String iccid = Util.swapNibblesOnString(((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ICCID"));
        String profileName = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("PROFILE_NAME");

        //if (JOptionPane.showConfirmDialog(this, String.format("Are you sure you want to delete the profile %s - ICCID %s - AID %s", profileName, iccid, isdp_aid), "Delete Profile", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        if (Util.showConfirmDialog(this, String.format("Are you sure you want to delete the profile %s - ICCID %s - AID %s", profileName, iccid, isdp_aid), "Delete Profile") == JOptionPane.YES_OPTION) {

            SwingWorker sw = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    setProcessing(true);
                    try {

                        lpa.disableProfile(isdp_aid);
                        lpa.deleteProfile(isdp_aid);

                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, ex.toString());
                        Util.showMessageDialog(null, "Failed to Enable the profile with AID " + isdp_aid + ". Please check the log.");
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
                if (lpa != null) {
                    lpa.disconnect();
                }
                lpa = new LpaSrc((String) cmbReaders.getSelectedItem());
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

            }
        });

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {

                setProcessing(true);

                try {
                    listProfiles();
                    updateEuiccInfo();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, ex.toString());
                    Util.showMessageDialog(null, String.format("Failed to read card info \nReason: %s \nPlease check the log for more info.", ex.getMessage()));
                }

                setProcessing(false);

                btnAddProfile.setEnabled(true);
                btnSetSMDPAddress.setEnabled(true);

                return null;
            }
        };

        sw.execute();


    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnAddProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProfileActionPerformed
//        String code = Util.showInputDialog(this, "Enter Activation Code or Truphone MatchingId", "");
        String code = Util.showInputActivationCodeDialog(this, "Enter the MatchingId", "");
       
        if (code == null || code.length() == 0) {
            return;
        }

// This block of code is for building the activation code based on the user's input. It allows the download of any profile from any server        
//        String[] acparts = code.split("\\$");;
//        String activationCode = "";
//        if (code.toLowerCase().startsWith("lpa:1$") && acparts.length >= 3) {
//            //activtion code
//
//            activationCode = code.substring(4);
//        } else if (code.toLowerCase().startsWith("1$") && acparts.length >= 3) {
//            activationCode = code;
//        } else {
//            //matchingId
//            activationCode = "1$rsp.truphone.com$" + code;
//        }

        //We consider the inputed value as a matchingID, it will always use truphones SMDP+. 
        //JOptionPane.showMessageDialog(this, code);
        download(code);
    }//GEN-LAST:event_btnAddProfileActionPerformed

    private void download(String activationCode) {
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
    }
    private void btnSetSMDPAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetSMDPAddressActionPerformed

        String address = JOptionPane.showInputDialog(this, "Enter new SMDP+ address");

        //lpa.setSMDPAddress(com.truphone.util.Util.ASCIIToHex(address));
        setProcessing(true);

        lpa.setSMDPAddress(address);

        try {
            updateEuiccInfo();
        } catch (DecoderException | IOException ex) {
            LOG.log(Level.WARNING, ex.toString());
            Util.showMessageDialog(null, "Failed to read Euicc Info");
        }

        setProcessing(false);
    }//GEN-LAST:event_btnSetSMDPAddressActionPerformed

    private void btnCloseAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseAppActionPerformed
        try {
            lpa.disconnect();
        } catch (Exception ex) {
            //nothing to do
        }
        System.exit(0);
    }//GEN-LAST:event_btnCloseAppActionPerformed

    private void btnCloseApp2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseApp2ActionPerformed
        // TODO add your handling code here:
        StringBuilder sb = new StringBuilder();
        sb.append("Truphone LPAdesktop (Truphone SM-DP+ only)").append(System.getProperty("line.separator"));

        String version = getAppVersion();
        if (version != null && version.length() > 0) {
            sb.append("Version ").append(version).append(System.getProperty("line.separator"));
        }

        sb.append("Copyright (c) 2019 Truphone").append(System.getProperty("line.separator"));
        sb.append("This application shall not be used or distributed without prior permission from Truphone.");

        Util.showMessageDialog(this, sb.toString());

    }//GEN-LAST:event_btnCloseApp2ActionPerformed

    private void btnHandleNotificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHandleNotificationsActionPerformed

        SwingWorker sw = new SwingWorker() {
            @Override

            protected Object doInBackground() throws Exception {
                setProcessing(true);
                try {
                    lpa.processPendingNotificaitons();

                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.toString());

                    Util.showMessageDialog(null, String.format("Something went wrong\nReason: %s\nPlease check the log for more info.", ex.getMessage()));
                }
                setProcessing(false);

                return null;
            }
        };

        sw.execute();


    }//GEN-LAST:event_btnHandleNotificationsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        lpa.disconnect();
    }//GEN-LAST:event_formWindowClosed

    private void miCopyIccidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCopyIccidActionPerformed
        int idx = listProfiles.getSelectedIndex();
        //String isdp_aid = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ISDP_AID");
        String iccid = Util.swapNibblesOnString(((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ICCID"));
        if (iccid.toLowerCase().charAt(iccid.length() - 1) == 'f') {
            //remove the 'f'
            iccid = iccid.substring(0, iccid.length() - 1);
        }
        //String profileName = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("PROFILE_NAME");

        StringSelection stringSelection = new StringSelection((String) iccid);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

    }//GEN-LAST:event_miCopyIccidActionPerformed

    private void miCopyAidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCopyAidActionPerformed
        int idx = listProfiles.getSelectedIndex();
        String isdp_aid = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ISDP_AID");
        //String iccid = Util.swapNibblesOnString(((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("ICCID"));
        //String profileName = ((Map<String, String>) ((DefaultListModel) listProfiles.getModel()).getElementAt(idx)).get("PROFILE_NAME");

        StringSelection stringSelection = new StringSelection((String) isdp_aid);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }//GEN-LAST:event_miCopyAidActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProfile;
    private javax.swing.JButton btnCloseApp;
    private javax.swing.JButton btnCloseApp2;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnHandleNotifications;
    private javax.swing.JButton btnRefreshReaders;
    private javax.swing.JButton btnSetSMDPAddress;
    private javax.swing.JComboBox<String> cmbReaders;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JLabel lblTitleBar;
    private javax.swing.JList<String> listProfiles;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem miCopyAid;
    private javax.swing.JMenuItem miCopyIccid;
    private javax.swing.JMenuItem miDeleteProfile;
    private javax.swing.JMenuItem miDisableProfile;
    private javax.swing.JMenuItem miEnableProfile;
    private javax.swing.JPopupMenu popUpProfiles;
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
//        try {

        profiles = lpa.getProfiles();

        DefaultListModel model = new DefaultListModel();

//
//        ArrayList<Log> loggs = new ArrayList<Log>(logs.values());
//
//        Comparator<Log> compareByTimestamp = new Comparator<Log>() {
//            @Override
//            public int compare(Log o1, Log o2) {
//                return o1.getTimestamp().compareTo(o2.getTimestamp());
//            }
//        };
//
//        ArrayList<String> keys = null;
//        Comparator<String> compareFieldsAlphabetically = new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                return o1.toLowerCase().compareTo(o2.toLowerCase());
//            }
//        };
//
//        DefaultTableModel model = new DefaultTableModel();
//        model.addColumn("Iccid");
//        model.addColumn("Name");
//        model.addColumn("State");
//        model.addColumn("Spn");
//        model.addColumn("Aid");
//        model.addColumn("Class");
        for (Map<String, String> profile : profiles) {
//            String[] fields = new String[profile.size()];
            model.addElement(profile);

        }

        listProfiles.setModel(model);
        if (model.getSize() > 0) {
            listProfiles.setSelectedIndex(0);

        }

//        tblProfiles.getColumnModel().getColumn(5).setPreferredWidth(100);
//        } catch (Exception ex) {
//            LOG.log(Level.WARNING, ex.toString());
//            Util.showMessageDialog(null, String.format("Failed to refresh profiles list \nReason: %s \nPlease check the log for more info.", ex.getMessage()));
//        }
    }

    private void updateEuiccInfo() throws DecoderException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Eid: ").append(lpa.getEID()).append(System.getProperty("line.separator"));

        InputStream is;
        EuiccConfiguredAddressesResponse configuredAddress = new EuiccConfiguredAddressesResponse();
        String rootDsAddress = "", defaultSmdpAddress = "";

        //try {
        is = new ByteArrayInputStream(Hex.decodeHex(lpa.getSMDPAddress().toCharArray()));
        configuredAddress.decode(is);
        rootDsAddress = configuredAddress.getRootDsAddress() != null ? configuredAddress.getRootDsAddress().toString() : "";
        defaultSmdpAddress = configuredAddress.getDefaultDpAddress() != null ? configuredAddress.getDefaultDpAddress().toString() : "";
        //} catch (Exception ex) {
        //    LOG.log(Level.SEVERE, ex.toString());
        //    Util.showMessageDialog(this, String.format("Failed to get SMDP+ & SMDS addresses\nReason:%s\nCheck the logs for more info", ex.getMessage()));
        //}

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
        btnHandleNotifications.setEnabled(!processing);

//      }
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

    public String getAppVersion() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            //LMavenXpp3Reader reader = new MavenXpp3Reader();
            //Model model = reader.read(new FileReader("pom.xml"));
            //return model.getVersion();
            getClass().getPackage().getImplementationVersion();
        }
        return getClass().getPackage().getImplementationVersion();
    }

    private void showLogs(String field, String value) {

//        //DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeResult.getModel().getRoot();
//        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Logs related to " + field + "=" + value);
//
//        DefaultTreeModel model = new DefaultTreeModel(top);
//
//        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
//        model.reload(root);
//        treeProfiles.setModel(model);
//
//        ArrayList<Log> loggs = new ArrayList<Log>(logs.values());
//
//        Comparator<Log> compareByTimestamp = new Comparator<Log>() {
//            @Override
//            public int compare(Log o1, Log o2) {
//                return o1.getTimestamp().compareTo(o2.getTimestamp());
//            }
//        };
//
//        ArrayList<String> keys = null;
//        Comparator<String> compareFieldsAlphabetically = new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                return o1.toLowerCase().compareTo(o2.toLowerCase());
//            }
//        };
//
//        Collections.sort(loggs, compareByTimestamp);
//        for (Log l : loggs) {
//            StringBuilder sb = new StringBuilder();
//            String timestamp, transactionId, address, iccid, matchingId, eid, direction, logId;
//
//            JSONObject request = l.getRequest();
//
//            if (request != null) {
//                Object o;
//                //request
//                o = request.get("@timestamp");
//                timestamp = (o == null) ? "" : (String) o;
//
//                o = request.get("address");
//                address = (o == null) ? "" : (String) o;
//
//                if (address.length() == 0) {
//                    //try to get address form response. handleDownloadProgressInfo cases. 
//                    o = l.getResponse().get("address");
//                    address = (o == null) ? "" : (String) o;
//                }
//
//                o = request.get("transactionId");
//                transactionId = (o == null) ? "" : (String) o;
//
//                o = request.get("iccid");
//                iccid = (o == null) ? "" : (String) o;
//
//                o = request.get("matchingId");
//                matchingId = (o == null) ? "" : (String) o;
//
//                o = request.get("eid");
//                eid = (o == null) ? "" : (String) o;
//
//                o = request.get("direction");
//                direction = (o == null) ? "" : (String) o;
//
//                o = request.get("logId");
//                logId = (o == null) ? "" : (String) o;
//
//                sb.append(timestamp).append("\t").append(logId).append("\t").append(address);
//            } else {
//
//                sb.append("???????");
//            }
//
//            TreePath path = treeProfiles.getSelectionPath();
//
//            DefaultMutableTreeNode parentNode = null;
//            if (path == null) {
//                parentNode = (DefaultMutableTreeNode) treeResult.getModel().getRoot();
//            } else {
//                parentNode = (DefaultMutableTreeNode) treeResult.getSelectionPath().getLastPathComponent();
//            }
//
//            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(sb.toString());
//            parentNode.add(newNode);
//
//            DefaultMutableTreeNode requestNode = new DefaultMutableTreeNode("Request");
//            newNode.add(requestNode);
//
//            DefaultMutableTreeNode responseNode = new DefaultMutableTreeNode("Response");
//            newNode.add(responseNode);
//
//            JSONObject requestJsonObject = (JSONObject) l.getRequest();
//
//            if (requestJsonObject != null) {
//
//                keys = new ArrayList<>(requestJsonObject.keySet());
//                Collections.sort(keys, compareFieldsAlphabetically);
//
//                //for (Iterator iterator = requestJsonObject.keySet().iterator(); iterator.hasNext();) {
//                for (String key : keys) {
//                    //String key = (String) iterator.next();
//                    sb = new StringBuilder();
//                    sb.append(key).append(" -> ").append(requestJsonObject.get(key));
//
//                    DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(sb.toString());
//                    requestNode.add(fieldNode);
//                }
//
//            }
//            JSONObject responseJsonObject = (JSONObject) l.getResponse();
//
//            if (responseJsonObject != null) {
//                keys.clear();
//                keys.addAll(l.getResponse().keySet());
//
//                Collections.sort(keys, compareFieldsAlphabetically);
//
//                //for (Iterator iterator = responseJsonObject.keySet().iterator(); iterator.hasNext();) {
//                for (String key : keys) {
//                    //String key = (String) iterator.next();
//                    sb = new StringBuilder();
//                    sb.append(key).append(" -> ").append(responseJsonObject.get(key));
//
//                    DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode(sb.toString());
//                    responseNode.add(fieldNode);
//                }
//
//            }
//            ((DefaultTreeModel) treeResult.getModel()).reload();
//
//            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) treeResult.getCellRenderer();
//            renderer.setTextSelectionColor(Color.white);
//            renderer.setBackgroundSelectionColor(Color.blue);
//            renderer.setBorderSelectionColor(Color.black);
//
//        }
    }

    public class MyProfileCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus) {

            Map<String, String> profile = (Map<String, String>) value;
            StringBuilder sb = new StringBuilder();

            String[] fields = new String[profile.size()];

            boolean enabled = false;
            if (profile.containsKey("ICCID")) {
                String iccidUnswapped = Util.swapNibblesOnString(profile.get("ICCID"));
                fields[0] = iccidUnswapped.toLowerCase().charAt(19) == 'f' ? iccidUnswapped.substring(0, 19) : iccidUnswapped.substring(0, 20);
                sb.append("ICCID: ").append(fields[0]).append("\r\n");
            }

            if (profile.containsKey("NAME")) {
                fields[1] = profile.get("NAME");
                sb.append("NAME: ").append(fields[1]).append("\r\n");

            }

            if (profile.containsKey("PROFILE_STATE")) {
                fields[2] = profile.get("PROFILE_STATE").compareTo("1") == 0 ? "Enabled" : "Disabled";
                sb.append("PROFILE STATE: ").append(fields[2]).append("\r\n");
                if (fields[2].toLowerCase().compareTo("enabled") == 0) {
                    enabled = true;
                }
            }

            if (profile.containsKey("PROVIDER_NAME")) {
                fields[3] = profile.get("PROVIDER_NAME");
                sb.append("PROVIDER NAME: ").append(fields[3]).append("\r\n");

            }

            if (profile.containsKey("ISDP_AID")) {
                fields[4] = profile.get("ISDP_AID");
                sb.append("ISDP AID: ").append(fields[4]).append("\r\n");

            }

            if (profile.containsKey("PROFILE_CLASS")) {
                String profileClass = profile.get("PROFILE_CLASS");
                if (profileClass.compareTo("0") == 0) {
                    fields[5] = "Test";
                    sb.append("PROFILE CLASS: ").append(fields[5]);

                } else if (profileClass.compareTo("1") == 0) {

                    fields[5] = "Provisioning";
                    sb.append("PROFILE CLASS: ").append(fields[5]);
                } else if (profileClass.compareTo("2") == 0) {
                    fields[5] = "Operational";
                    sb.append("PROFILE CLASS: ").append(fields[5]);

                }

            }

            //create panel
            final JPanel p = new JPanel();
            
            p.setLayout(new BorderLayout());

            //icon
            final JPanel IconPanel = new JPanel(new BorderLayout());

            final JLabel l = new JLabel(); //<-- this will be an icon instead of a text
            l.setIcon(new ImageIcon(getClass().getResource("/gsma_esim.png")));
            IconPanel.setBackground(Color.white);
            IconPanel.add(l, BorderLayout.NORTH);
            p.add(IconPanel, BorderLayout.WEST);
            

            

            //text
            final JTextArea ta = new JTextArea();
            //ta.setSize(200, 50);
            ta.setText(sb.toString());
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setEditable(true);
            p.add(ta, BorderLayout.CENTER);
            if (enabled) {
                ta.setBorder(BorderFactory.createLineBorder(Color.green));
            }

            if (isSelected) {
                p.setBackground(new Color(0, 50, 63));
            }

            return p;

        }
    }

}

