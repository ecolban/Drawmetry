/*
 * EntityRecord.java
 *
 * Created on April 24, 2005, 10:04 PM
 */

package com.drawmetry;

/**
 *
 * @author Erik
 */
public interface EntityRecord {
    
    public DmEntity getEntity();
    
    public double[] getParams();
    
    public int getZIndex();
}
