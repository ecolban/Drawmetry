/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drawmetry;

import java.io.PrintStream;

/**
 *
 * @author Erik
 */
public class Debugger {
    static PrintStream pf = System.out;
    public static void setPf(PrintStream ps) {
        pf = ps;
    }
    public static boolean pat(String s) {
        pf.println(s);
        return true;
    }
    
    public static boolean fat(String s, Object... args) {
        pf.format(s, args);
        return true;
        
    }

}
