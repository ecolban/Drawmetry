package com.drawmetry;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

class EditToolBar extends JToolBar implements PropertyChangeListener {

    private static Icon cutIcon = new ImageIcon(EditToolBar.class.getResource(
            "/toolbarButtonGraphics/general/Cut16.gif"));
    private static Icon copyIcon = new ImageIcon(EditToolBar.class.getResource(
            "/toolbarButtonGraphics/general/Copy16.gif"));
    private static Icon pasteIcon = new ImageIcon(EditToolBar.class.getResource(
            "/toolbarButtonGraphics/general/Paste16.gif"));
    private static Icon undoIcon = new ImageIcon(EditToolBar.class.getResource(
            "/toolbarButtonGraphics/general/Undo16.gif"));
    private static Icon redoIcon = new ImageIcon(EditToolBar.class.getResource(
            "/toolbarButtonGraphics/general/Redo16.gif"));
    private JButton undoButton;
    private JButton redoButton;
    private JButton copyButton;
    private JButton pasteButton;

    EditToolBar(final DrawingPane drawingPane) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setRollover(true);

        JButton cutButton = new JButton();
        cutButton.setIcon(cutIcon);
        cutButton.setToolTipText("Cut");
        cutButton.setBorder(null);
        cutButton.setEnabled(false);
        cutButton.setFocusable(false);
        cutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.cut();
            }
        });
        add(cutButton);

        copyButton = new JButton();
        copyButton.setText(null);
        copyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.copy();
            }
        });
        copyButton.setIcon(copyIcon);
        copyButton.setToolTipText("Copy");
        copyButton.setBorder(null);
        copyButton.setEnabled(false);
        copyButton.setFocusable(false);
        add(copyButton);

        pasteButton = new JButton();
        pasteButton.setText("");
        pasteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.paste();
            }
        });
        pasteButton.setIcon(pasteIcon);
        pasteButton.setToolTipText("Paste");
        pasteButton.setBorder(null);
        pasteButton.setEnabled(false);
        pasteButton.setFocusable(false);
        add(pasteButton);

        undoButton = new JButton();
        undoButton.setText("");
        undoButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                drawingPane.undo();
            }
        });
        undoButton.setIcon(undoIcon);
        undoButton.setToolTipText("Undo");
        undoButton.setBorder(null);
        undoButton.setEnabled(false);
        undoButton.setFocusable(false);
        add(undoButton);

        redoButton = new JButton();
        redoButton.setText("");
        redoButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                assert drawingPane.getModel() != null;
                drawingPane.getModel().redo();
            }
        });
        redoButton.setIcon(redoIcon);
        redoButton.setToolTipText("Redo");
        redoButton.setBorder(null);
        redoButton.setEnabled(false);
        redoButton.setFocusable(false);
        add(redoButton);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(FileContext.PROP_UNDO_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            undoButton.setEnabled(enabled);
        } else if (evt.getPropertyName().equals(FileContext.PROP_REDO_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            redoButton.setEnabled(enabled);
        } else if (evt.getPropertyName().equals(DrawingPane.PROP_COPY_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            copyButton.setEnabled(enabled);
        } else if (evt.getPropertyName().equals(DrawingPane.PROP_PASTE_ENABLED)) {
            boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
            pasteButton.setEnabled(enabled);
        }
    }
}
