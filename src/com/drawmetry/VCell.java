/*
 * NewClass.java
 *
 * Created on May 9, 2006, 9:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

final class VCell implements ViewObject {

    private static BasicStroke nonSelectedStroke = new BasicStroke(0.5F);
    private static BasicStroke selectedStroke = new BasicStroke(2.0F);
    private static BasicStroke edgeStroke;
    private static Color edgeColor;
    private static Color textColor;
    private static AffineTransform savedTransform;
    private static Shape savedClip;
    private static AffineTransform modelTransform = new AffineTransform();
    private static AffineTransform transform = new AffineTransform();
    private static GeneralPath vBounds = new GeneralPath();
    private static VCell vc = new VCell();
    private static String textString;
    private static Rectangle2D mBounds;
    private static boolean visible;
    private static AttributedString attributedString;
    private static LineBreakMeasurer lineMeasurer;
    private static AttributedCharacterIterator paragraph;
    private static int paragraphStart;
    private static int paragraphEnd;
    private static int horizontalAlignment;
    private static int verticalAlignment;
//    private static MCell.CellFormat format;

    private VCell() {
    }

    public static VCell getInstance(
            MCell mc, AffineTransform modelToView, boolean selected, boolean visible) {
        edgeStroke = selected ? selectedStroke : nonSelectedStroke;
        switch (mc.dof()) {
            case 0:
                edgeColor = Color.red;
                break;
            case 1:
                edgeColor = Color.orange;
                break;
            case 2:
                edgeColor = Color.green;
                break;
            default:
                assert false;
        }
        VCell.visible = visible;
        mc.getTransform(transform);
        transform.preConcatenate(modelToView);
        mBounds = mc.getBounds();
        vBounds.reset();
        vBounds.moveTo((float) mBounds.getX(), (float) mBounds.getY());
        vBounds.lineTo(
                (float) (mBounds.getX() + mBounds.getWidth()),
                (float) mBounds.getY());
        vBounds.lineTo(
                (float) (mBounds.getX() + mBounds.getWidth()),
                (float) (mBounds.getY() + mBounds.getHeight()));
        vBounds.lineTo(
                (float) mBounds.getX(),
                (float) (mBounds.getY() + mBounds.getHeight()));
        vBounds.closePath();
        vBounds.transform(transform);

        assert mc.getContent() != null;
        textString = mc.getContent().getSValue();
        attributedString = new AttributedString(textString);
        attributedString.addAttribute(TextAttribute.FONT, mc.getFont());
        paragraph = attributedString.getIterator();
        paragraphStart = paragraph.getBeginIndex();
        paragraphEnd = paragraph.getEndIndex();
        textColor = mc.getFontColor();
        horizontalAlignment = mc.getHorizontalAlignment();
        verticalAlignment = mc.getVerticalAlignment();
//        format = mc.getFormat();
        return vc;
    }

//    @Override
    public boolean hits(int x, int y) {
        return vBounds.intersects(x - 2, y - 2, 4, 4);
    }

    public int edgeHits(int x, int y) {

        if (vBounds.intersects(x - 2, y - 2, 4, 4)
                && !vBounds.contains(x - 2, y - 2, 4, 4)) {
            if (hitsN(x, y)) {
                if (hitsW(x, y)) {
                    return Cursor.NW_RESIZE_CURSOR;
                } else if (hitsE(x, y)) {
                    return Cursor.NE_RESIZE_CURSOR;
                } else {
                    return Cursor.N_RESIZE_CURSOR;
                }
            } else if (hitsS(x, y)) {
                if (hitsW(x, y)) {
                    return Cursor.SW_RESIZE_CURSOR;
                } else if (hitsE(x, y)) {
                    return Cursor.SE_RESIZE_CURSOR;
                } else {
                    return Cursor.S_RESIZE_CURSOR;
                }
            } else {
                if (hitsW(x, y)) {
                    return Cursor.W_RESIZE_CURSOR;
                } else if (hitsE(x, y)) {
                    return Cursor.E_RESIZE_CURSOR;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    private boolean hitsN(int x, int y) {
        return vBounds.contains(x, y + 2) && !vBounds.contains(x, y - 2);
    }

    private boolean hitsS(int x, int y) {
        return vBounds.contains(x, y - 2) && !vBounds.contains(x, y + 2);
    }

    private boolean hitsW(int x, int y) {
        return vBounds.contains(x + 2, y) && !vBounds.contains(x - 2, y);
    }

    private boolean hitsE(int x, int y) {
        return vBounds.contains(x - 2, y) && !vBounds.contains(x + 2, y);
    }

//    @Override
    public boolean containedIn(Rectangle2D rect) {
        return rect.contains(vBounds.getBounds());
    }

//    @Override
    public void draw(Graphics2D g2) {

        g2.setStroke(edgeStroke);
        g2.setColor(edgeColor);
        if (visible) {
            g2.draw(vBounds);
        }
        if (modelTransform.getDeterminant() != 0.0) {
            savedTransform = g2.getTransform();
            savedClip = g2.getClip();
            g2.setClip(vBounds);
            g2.transform(transform);
            g2.setColor(textColor);

            //****************************
            lineMeasurer = new LineBreakMeasurer(paragraph, g2.getFontRenderContext());
//            float leftMargin = 0F;
//            float rightMargin = 0F;
//            float topMargin = 0F;
//            float bottomMargin = 0F;

            float cellWidth = (float) mBounds.getWidth();// - leftMargin - rightMargin;

            if (cellWidth > 0.0F) {
                lineMeasurer.setPosition(paragraphStart);
                float leftBound = (float) mBounds.getX(); // + leftMargin;
                float drawPosY = (float) mBounds.getY(); // + topMargin;
                TextLayout layout = null;
                if (verticalAlignment == MCell.CellFormat.CENTER
                        || verticalAlignment == MCell.CellFormat.BOTTOM) {
                    float height = 0;
                    while (lineMeasurer.getPosition() < paragraphEnd) {
                        layout = lineMeasurer.nextLayout(cellWidth);
                        height += layout.getAscent() + layout.getDescent() + layout.getLeading();
                    }
                    height -= layout.getLeading();
                    if (verticalAlignment == MCell.CellFormat.CENTER) {
                        drawPosY += ((float) mBounds.getHeight() - height) * 0.5F;
                    } else {
                        drawPosY += ((float) mBounds.getHeight() - height);
                    }
                }
                lineMeasurer.setPosition(paragraphStart);
                while (lineMeasurer.getPosition() < paragraphEnd) {
                    float drawPosX = leftBound;
                    layout = lineMeasurer.nextLayout(cellWidth);
                    if (horizontalAlignment == MCell.CellFormat.CENTER) {
                        drawPosX += (cellWidth - layout.getVisibleAdvance()) * 0.5F;
                    } else if (horizontalAlignment == MCell.CellFormat.RIGHT) {
                        drawPosX += (cellWidth - layout.getVisibleAdvance());
                    }
                    drawPosY += layout.getAscent();
                    layout.draw(g2, drawPosX, drawPosY);
                    drawPosY += layout.getDescent() + layout.getLeading();
                }
            }
        }
        g2.setTransform(savedTransform);
        g2.setClip(savedClip);
    }
}
