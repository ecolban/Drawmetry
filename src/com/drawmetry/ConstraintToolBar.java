/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
class ConstraintToolBar extends JToolBar {

    private JTextField constraintText;

    ConstraintToolBar(final DrawingPane drawingPane) {
        setLayout(new BorderLayout(2, 0));
        setFloatable(false);

        JLabel inputLabel = new JLabel();
        inputLabel.setText("  =");
        inputLabel.setPreferredSize(new Dimension(20, 20));
        add(inputLabel, BorderLayout.LINE_START);

        constraintText = new JTextField();
        constraintText.setText(" ");
        constraintText.addActionListener(new ActionListener() {

//            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    drawingPane.parseInput(constraintText.getText());
                } catch (ParseException ex) {
                    drawingPane.setFeedback(ex.getMessage());
                }
            }
        });
        constraintText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (constraintText.isFocusOwner()) {
                        drawingPane.requestFocusInWindow();
                    }
                }
            }
        });
        constraintText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent evt) {
                if (drawingPane.getFocusEntity() != null) {
                    drawingPane.requestTextInput(constraintTextInputListener);
                } else {
                    drawingPane.requestFocusInWindow();
                }

            }

            @Override
            public void focusLost(FocusEvent evt) {
                constraintTextFocusLost(evt);
            }
        });
        add(constraintText, BorderLayout.CENTER);

        drawingPane.addPropertyChangeListener(DrawingPane.PROP_FOCUS_ENTITY,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        DmEntity focusEntity = (DmEntity) evt.getNewValue();
                        if (focusEntity != null) {
                            if (focusEntity.getConstraint() != null) {
                                constraintText.setText("=" + focusEntity.getConstraint().
                                        toString());
                            } else if (focusEntity instanceof CellContent) {
                                constraintText.setText(((CellContent) focusEntity).getSValue());
                            } else {
                                constraintText.setText("");
                            }
                        } else {
                            constraintText.setText("");
                        }
                    }
                });

    }
    private TextInputListener constraintTextInputListener = new TextInputListener() {

        public void receiveText(String text) {
            try {
                constraintText.getDocument().insertString(constraintText.getCaretPosition(), text, null);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }

        }
    };

    private void constraintTextFocusLost(FocusEvent evt) {
    }
}