/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.github.sarxos.webcam.Webcam;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author frederico.palma
 */
public class WebcamHandler {
    
    private static final String NO_CAMERA = "No Camera";
    
    private static final Map<String, Webcam> webcamByNameMap = new HashMap<>();
    
    private static final List<String> webcamNames = new LinkedList<>();
    
    private WebcamHandler() {}
    
    public static List<String> getWebcamNames(final boolean forceRefresh) {
        if (forceRefresh || webcamNames.isEmpty()) {
            refreshList();
        }
        return new LinkedList<>(webcamNames);
    }
    
    private static void refreshList() {
        final List<Webcam> webcams = Webcam.getWebcams();
        
        webcamByNameMap.clear();
        webcamNames.clear();
        
        webcamNames.add(NO_CAMERA);
        
        for (Webcam webcam : webcams) {
            String name = webcam.getName();
            webcamByNameMap.put(name, webcam);
            webcamNames.add(name);
        }
    }
    
    public static Webcam getWebcamByName(final String name) {
        return webcamByNameMap.get(name);
    }
    
}
