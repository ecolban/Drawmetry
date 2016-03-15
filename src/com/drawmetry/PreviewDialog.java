package com.drawmetry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author Erik
 */
class PreviewDialog extends java.awt.Dialog {

    FileContext fileContext;

    /** Creates new form PreviewDialogue */
    PreviewDialog(FileContext fileContext, DmFrame parent, boolean modal) {
        super(parent, modal);
        this.fileContext = fileContext;
//        this.printView = printView;
        initComponents();
    }

    private void initComponents() {


        setTitle("Preview");
        setPreferredSize(new Dimension(170, 250));
        JToolBar toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setPreferredSize(new Dimension(13, 24));
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));

        JButton printButton = new JButton();
        printButton.setPreferredSize(new Dimension(50, 20));
        printButton.setText("Print...");
        printButton.setFocusable(false);
        printButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                closeDialog();
                fileContext.print();
            }
        });
        toolBar.add(printButton);

        JButton closeButton = new JButton();
        closeButton.setPreferredSize(new Dimension(50, 20));
        closeButton.setText("Close");
        closeButton.setFocusable(false);
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
        toolBar.add(closeButton);

        add(toolBar, java.awt.BorderLayout.NORTH);

        JPanel previewPane = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                fileContext.getPrintView().drawToPreview(g,getWidth(), getHeight());
            }
        };
        add(previewPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });
        pack();
    }

    /** Closes the dialog */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }
}
