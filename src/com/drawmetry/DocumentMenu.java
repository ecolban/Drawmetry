package com.drawmetry;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Erik
 */
public class DocumentMenu extends JMenu implements PropertyChangeListener {

    private DmFrame frame;

    DocumentMenu(final DmFrame frame) {

        this.frame = frame;
        setMnemonic('D');
        setText("Documents");

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof FileContext &&
                evt.getPropertyName().equals(FileContext.PROP_CURRENT_DOCUMENT)) {

            String[] names = (String[]) evt.getNewValue();
            final FileContext source = (FileContext) evt.getSource();

            boolean first = true;
            removeAll();
            int i = 0;
            for (String name : names) {
                if (first) {
                    frame.setTitle("Drawmetry - " + name);
                    first = false;
                }
                final int index = i++;
                add(new JMenuItem(new AbstractAction(name) {

                    public void actionPerformed(ActionEvent e) {
                        source.selectFile(index);
                    }
                }));
            }
        }
    }
}
