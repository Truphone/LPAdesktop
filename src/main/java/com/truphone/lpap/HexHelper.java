/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

/**
 *
 * @author frederico.palma
 */
public class HexHelper {
    
    public static String swapNibblesOnString(final String in) {
        final StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < in.length(); i += 2) {
            result.append(in.charAt(i + 1));
            result.append(in.charAt(i));
        }
        
        return result.toString();
    }
    
    public static String byteArrayToHex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
    
    public static byte[] hexStringToByteArray(final String hex) {
        if (hex == null) {
            return null;
        }
        
        String prepared = hex.replaceAll(" ", "").replaceAll(":", "").replaceAll("0x", "").replaceAll("0X", "");
        if (prepared.length() % 2 != 0){
            throw new IllegalArgumentException("The Hex String length cannot be odd.");
        }
        
        final byte[] output = new byte[prepared.length() / 2];
        for (int i = 0; i < prepared.length(); i += 2) {
            output[(i / 2)] = ((byte) Integer.parseInt(prepared.substring(i, i + 2), 16));
        }
        return output;
    }
}
