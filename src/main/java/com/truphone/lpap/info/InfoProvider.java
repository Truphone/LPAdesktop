/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap.info;

import com.truphone.lpap.LPAUI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frederico.palma
 */
public class InfoProvider {
    
    private static java.util.logging.Logger LOG = null;
    
    private String version = null;
    
    public InfoProvider() {
        LOG = Logger.getLogger(LPAUI.class.getName());
    }
        
    public String getAppVersion() {
        if (version == null) {
            version = getClass().getPackage().getImplementationVersion();
            if (version == null) {
                final InputStream propertiesStream = getClass().getClassLoader().getResourceAsStream("build.properties");
                if (propertiesStream != null) {
                    try {
                        Properties properties = new Properties();
                        properties.load(propertiesStream);
                        propertiesStream.close();
                        version = properties.getProperty("build.version");
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Unable to load build.properties file", ex);
                    }
                }
            }
        }
        return version == null ? "0.0.0" : version;
    }
    
    public String getAboutInfo() {
        final StringBuilder aboutStringBuilder = new StringBuilder("<html><head></head><body>");
        aboutStringBuilder.append("<p><strong>Truphone LPAdesktop V").append(getAppVersion()).append("</strong></p>");

        aboutStringBuilder.append("<p>Copyright 2019 Truphone</p>");
        aboutStringBuilder.append("<p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p>");
        aboutStringBuilder.append("<p><a href='http://www.apache.org/licenses/LICENSE-2.0'>http://www.apache.org/licenses/LICENSE-2.0</a></p>");
        aboutStringBuilder.append("<p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>");
        aboutStringBuilder.append("</body></html>");
        return aboutStringBuilder.toString();
    }
}
