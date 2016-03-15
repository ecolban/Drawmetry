package com.drawmetry;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter
{

    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory()) {
            return true;
        }
        String extension = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            extension = s.substring(i + 1).toLowerCase();
        }
        if (extension != null) {
            if (extension.equals("gif") ||
                    extension.equals("jpeg") ||
                    extension.equals("jpg") ||
                    extension.equals("png")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Filter image files";
    }
}
