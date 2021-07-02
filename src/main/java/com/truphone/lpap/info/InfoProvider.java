/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap.info;

/**
 *
 * @author frederico.palma
 */
public class InfoProvider {
        
    public String getAppVersion() {
        final String version = getClass().getPackage().getImplementationVersion();
        return version == null ? "1.0.0" : version;
    }
    
    public String getAboutInfo() {
        final StringBuilder aboutStringBuilder = new StringBuilder("<html><head><body>");
        aboutStringBuilder.append("<p><strong>Truphone LPAdesktop V").append(getAppVersion()).append("</strong></p>");

        aboutStringBuilder.append("<p>Copyright 2019 Truphone</p>");
        aboutStringBuilder.append("<p>Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at</p>");
        aboutStringBuilder.append("<p><a href='http://www.apache.org/licenses/LICENSE-2.0'>http://www.apache.org/licenses/LICENSE-2.0</a></p>");
        aboutStringBuilder.append("<p>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.</p>");
        
        return aboutStringBuilder.toString();
    }
}
