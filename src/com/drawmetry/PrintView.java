/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Iterator;

/**
 *
 * @author Erik
 */
public class PrintView implements Printable {

    private double paperWidth = 8.5 * 72.0;
    private double paperHeight = 11.0 * 72.0;
    private double topMargin = 72.0;
    private double leftMargin = 72.0;
    private double imageableWidth = 6.5 * 72.0;
    private double imageableHeight = 9 * 72.0;
    private DrawingModel model;

    PrintView(PageFormat pageFormat, DrawingModel model) {
        super();
        this.model = model;
        if (pageFormat != null) {
            paperWidth = pageFormat.getWidth();
            paperHeight = pageFormat.getHeight();
            topMargin = pageFormat.getImageableY();
            leftMargin = pageFormat.getImageableX();
            imageableWidth = pageFormat.getImageableWidth();
            imageableHeight = pageFormat.getImageableHeight();
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        } else {
            drawToPrinter(graphics);
            return Printable.PAGE_EXISTS;
        }

    }

    private void drawToPrinter(Graphics g) {
        assert model != null;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform transform = AffineTransform.getTranslateInstance(leftMargin, topMargin);
        Rectangle2D bounds = model.getBounds();
        double scale = Math.min(
                imageableWidth / bounds.getWidth(),
                imageableHeight / bounds.getHeight());
        if (scale < 1.0) {
            transform.concatenate(AffineTransform.getScaleInstance(scale, scale));
        }
        transform.concatenate(AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()));

        for (Iterator<DrawableEntity> iterator = model.getBackgroundIterator();
                iterator.hasNext();) {
            DrawingPane.getViewInstance(iterator.next(), transform, false, false).draw(g2);
        }
    }

    void drawToPreview(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double widthScale = width / paperWidth;
        double heightScale = height / paperHeight;
        double scale0 = Math.min(widthScale, heightScale);
        AffineTransform transform = AffineTransform.getTranslateInstance(
                (width - paperWidth * scale0) / 2,
                (height - paperHeight * scale0) / 2);
        transform.concatenate(AffineTransform.getScaleInstance(scale0, scale0));
        g2.setBackground(Color.LIGHT_GRAY);
        g2.clearRect(0, 0, width, height);
        AffineTransform savedTransform = g2.getTransform();
        g2.transform(transform);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, (int) paperWidth, (int) paperHeight);
        g2.setTransform(savedTransform);
        transform.concatenate(AffineTransform.getTranslateInstance(leftMargin, topMargin));
        Rectangle2D bounds = model.getBounds();
        double scale1 = Math.min(
                imageableWidth / bounds.getWidth(),
                imageableHeight / bounds.getHeight());
        if (scale1 < 1.0) {
            transform.concatenate(AffineTransform.getScaleInstance(scale1, scale1));
        }
        transform.concatenate(AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()));

        for (Iterator<DrawableEntity> iterator = model.getBackgroundIterator();
                iterator.hasNext();) {
            DrawingPane.getViewInstance(iterator.next(), transform, false, false).draw(g2);
        }
    }
}
