/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drawmetry;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 *
 * @author erikcolban
 */
public interface PathEntity extends DmEntity{

    public GeneralPath getPath(AffineTransform at);

    public void getTransform(AffineTransform at);

}
