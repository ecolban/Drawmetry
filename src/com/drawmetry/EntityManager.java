/*
 * EntityManager.java
 *
 * Created on May 11, 2007, 3:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.drawmetry;

import com.drawmetry.constraints.VarPredsConstraint;

/**
 *
/**
 *
 * @author Erik Colban
 */

public interface EntityManager {
    
    public void entityDetached(DmEntity entity);

}
