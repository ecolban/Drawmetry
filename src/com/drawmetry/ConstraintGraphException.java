/*
 * ConstraintGraphException.java
 *
 * Created on March 18, 2006, 7:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.drawmetry;

import java.lang.Exception;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
public class ConstraintGraphException extends Exception{
    
    /** Creates a new instance of ConstraintGraphException */
    public ConstraintGraphException() {
    }
    
    public ConstraintGraphException(String message){
        super(message);
    }
    
}
