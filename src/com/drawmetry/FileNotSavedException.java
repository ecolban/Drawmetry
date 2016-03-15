/*
 * FileNotSavedException.java
 *
 * Created on June 21, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.drawmetry;

import java.io.IOException;

/**
 *
 * @author Erik
 */
public class FileNotSavedException extends IOException {
    
    /** Creates a new instance of FileNotSavedException */
    public FileNotSavedException(String s) {
        super(s);
    }
    
}
