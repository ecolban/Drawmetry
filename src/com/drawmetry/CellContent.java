/*
 * CellContent.java
 *
 * Created on March 23, 2007, 5:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.util.Locale;

import com.drawmetry.constraints.NullConstraint;

/**
 * This class needs some major overhaul!!!!
 *
 * @author Erik
 */
public class CellContent extends NumVar{

    private static int n;
    private String sValue = "";
    String id;

    public CellContent() {
        super();
        this.id = "Cc_" + n++;
    }

    public CellContent(double nValue) {
        super();
        this.id = "Cc_" + n++;
        setNValue(nValue);
    }

    @Override
    public CellContent clone() throws CloneNotSupportedException {
        CellContent clone = (CellContent) super.clone();
        clone.id = "Cc_" + n++;
        return clone;
    }

    public boolean isNaN() {
        return Double.isNaN(getNValue());
    }

    public String getSValue() {
        if (isNaN()) {
            return sValue;
        } else {
            return String.format(Locale.US, "%1.2f", getNValue());
        }
    }

    public void setSValue(String s) {
        try {
            double d = Double.parseDouble(s); //uses US locale
            setNValue(d);
            sValue = s;

        } catch (NumberFormatException e) {
            setNValue(Double.NaN);
            sValue = s;
        }
    }

    @Override
    public boolean isConstraintRequired() {
        return false;
    }

    @Override
    public String toString() {
        return getSValue();
    }

    @Override
    public String getEntityID() {
        for (Constraint c : getSuccessors()) {
            if (c instanceof NullConstraint) {
                assert c.getTarget() != null && c.getTarget() instanceof MCell;
                return ((MCell) c.getTarget()).getEntityID();
            }
        }
        // Orphan CellContent.
        return (getConstraint() == null) ? toString() : getConstraint().toString();

    }
}
