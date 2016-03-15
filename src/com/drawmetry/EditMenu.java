package com.drawmetry;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 *
 * @author Erik
 */
public class EditMenu extends JMenu implements PropertyChangeListener {

    private DrawingPane drawingPane;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem;

    EditMenu(final DrawingPane drawingPane) {

        this.drawingPane = drawingPane;
        final int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        setMnemonic('E');
        setText("Edit");

        final JMenuItem cutMenuItem = new JMenuItem();
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, menuMask));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        cutMenuItem.setEnabled(false);
        cutMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.cut();
            }
        });
        add(cutMenuItem);

        copyMenuItem = new JMenuItem();
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuMask));
        copyMenuItem.setMnemonic('C');
        copyMenuItem.setText("Copy");
        copyMenuItem.setEnabled(false);
        copyMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.copy();
            }
        });
        add(copyMenuItem);

        pasteMenuItem = new JMenuItem();
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setEnabled(false);
        pasteMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.paste();
            }
        });
        add(pasteMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem();
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.delete();
            }
        });
        add(deleteMenuItem);

        JMenuItem selectAllMenuItem = new JMenuItem();
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, menuMask));
        selectAllMenuItem.setMnemonic('A');
        selectAllMenuItem.setText("Select all");
        selectAllMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.selectAll();
            }
        });
        add(selectAllMenuItem);
        //--------------------------------------------------------------------
        add(new JSeparator());

        undoMenuItem = new JMenuItem();
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.undo();
            }
        });
        add(undoMenuItem);

        redoMenuItem = new JMenuItem();
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuMask));
        redoMenuItem.setMnemonic('d');
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.redo();
            }
        });
        add(redoMenuItem);

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof FileContext &&
                evt.getPropertyName().equals(FileContext.PROP_UNDO_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            undoMenuItem.setEnabled(enabled);
        } else if (evt.getSource() instanceof FileContext &&
                evt.getPropertyName().equals(FileContext.PROP_REDO_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            redoMenuItem.setEnabled(enabled);
        } else if (evt.getSource()== drawingPane &&
                evt.getPropertyName().equals(DrawingPane.PROP_COPY_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            copyMenuItem.setEnabled(enabled);
        } else if (evt.getSource() == drawingPane &&
                evt.getPropertyName().equals(DrawingPane.PROP_PASTE_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            pasteMenuItem.setEnabled(enabled);
        }
    }
}
