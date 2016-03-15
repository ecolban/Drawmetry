package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

class DmWriterObsolete extends PrintWriter {

    private StringBuffer indentation = new StringBuffer();
    boolean atLineBeginning = true;

    DmWriterObsolete(OutputStream out) {
        super(out);
    }

    private void increaseIndent(int level) {
        String unitS = "  ";
        int unitL = unitS.length();
        if (level > 0) {
            for (int i = 0; i < level; i++) {
                indentation.append(unitS);
            }
        } else if (level < 0) {
            int length = indentation.length();
            if (length <= 0) {
                return;
            }
            for (int i = 0; i > level; i--) {
                if (length >= unitL) {
                    indentation.delete(length - unitL, length);
                    length -= unitL;
                } else {
                    indentation.delete(0, length);
                    length = 0;
                    break;
                }
            }
        } else {
            assert level == 0;
        }
    }

    private void increaseIndent() {
        increaseIndent(1);
    }

    private void decreaseIndent() {
        increaseIndent(-1);
    }

    private void printMCell(MCell mc) {
//        MCell.CellFormat format = mc.getFormat();
        String text = mc.getContent().getSValue();
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, mc.getFont());
        AttributedCharacterIterator paragraph = attributedString.getIterator();
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        LineBreakMeasurer lineMeasurer =
                new LineBreakMeasurer(paragraph, new FontRenderContext(null, true, true));
        lineMeasurer.setPosition(paragraphStart);
        Rectangle2D bounds = mc.getBounds();
        float cellWidth = (float) bounds.getWidth();
        TextLayout layout = null;
        int numLines = 0;
        float height = 0;
        while (lineMeasurer.getPosition() < paragraphEnd) {
            numLines++;
            layout = lineMeasurer.nextLayout(cellWidth);
            height += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }
        height -= layout.getLeading();
        float drawPosY = (float) bounds.getY(); // + topMargin;
        switch (mc.getVerticalAlignment()) {
            case MCell.CellFormat.TOP:
                break;
            case MCell.CellFormat.CENTER:
                drawPosY += ((float) bounds.getHeight() - height) * 0.5F;
                break;
            case MCell.CellFormat.BOTTOM:
                drawPosY += ((float) bounds.getHeight() - height);
                break;
        }

        double[] d = new double[6];
        AffineTransform transform = new AffineTransform();
        mc.getTransform(transform);
        transform.getMatrix(d);

        Font font = mc.getFont();
        format(Locale.US, "%s<text id = \"%s\" "
                + "font-family=\"%s\" font-size=\"%d\" fill=\"#%06x\" "
                + "transform = \"matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)\" > %n",
                indentation,
                mc.getEntityID(),
                font.getFamily(), font.getSize(),
                mc.getFontColor().getRGB() & 0xffffff,
                d[0], d[1], d[2], d[3], d[4], d[5]);
        increaseIndent();
        format(Locale.US,
                "%s<dm:cell-dimension x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" visible=\"%s\"/>%n",
                indentation,
                bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(),
                mc.isVisible() ? "true" : "false");
        String halign = null;
        switch (mc.getHorizontalAlignment()) {
            case MCell.CellFormat.LEFT:
                halign = "left";
                break;
            case MCell.CellFormat.CENTER:
                halign = "center";
                break;
            case MCell.CellFormat.RIGHT:
                halign = "right";
                break;
            default:
                assert false;
        }
        String valign = null;
        switch (mc.getVerticalAlignment()) {
            case MCell.CellFormat.TOP:
                valign = "top";
                break;
            case MCell.CellFormat.CENTER:
                valign = "center";
                break;
            case MCell.CellFormat.BOTTOM:
                valign = "bottom";
                break;
            default:
                assert false;
        }
        format(Locale.US,
                "%s<dm:cell-format halign=\"%s\" valign=\"%s\" />%n",
                indentation, halign, valign);
        int textStart = 0;
        int textEnd = 0;
        lineMeasurer.setPosition(paragraphStart);
        float leftBound = (float) bounds.getX(); // + leftMargin;
        while (lineMeasurer.getPosition() < paragraphEnd) {

            layout = lineMeasurer.nextLayout(cellWidth);
            drawPosY += layout.getAscent();
            float drawPosX = leftBound;
            if (mc.getHorizontalAlignment() == MCell.CellFormat.CENTER) {
                drawPosX += (cellWidth - layout.getVisibleAdvance()) * 0.5F;
            } else if (mc.getHorizontalAlignment() == MCell.CellFormat.RIGHT) {
                drawPosX += (cellWidth - layout.getVisibleAdvance());
            }
            format(Locale.US, "%s<tspan x=\"%.2f\" y=\"%.2f\" >%n",
                    indentation, drawPosX, drawPosY);

            increaseIndent();
            textEnd += layout.getCharacterCount();
            format("%s%s%n", indentation, text.substring(textStart, textEnd));
            textStart = textEnd;
            decreaseIndent();
            format("%s</tspan>%n", indentation);
            drawPosY += layout.getDescent() + layout.getLeading();

        }
        decreaseIndent();
        format("%s</text>%n", indentation);
    }

    private void printMLine(MPolyline line) {
        print(indentation);
        assert line.getPoints().length == 4;
        double[] points = line.getPoints();
        BasicStroke stroke = line.getStroke();
        format(Locale.US, "<line id = \"%s\" x1 = \"%.4f\" y1 = \"%.4f\" x2 = \"%.4f\" y2 = \"%.4f\"",
                line.getEntityID(), points[0], points[1], points[2], points[3]);
        Color strokeColor = line.getStrokeColor();
        if (strokeColor == null) {
            format(" stroke=\"none\"");
        } else {
            format(" stroke=\"#%06x\"", strokeColor.getRGB() & 0xffffff);
        }
        format(Locale.US, " stroke-width=\"%.1f\"", stroke.getLineWidth());
        if (stroke.getDashArray() != null) {
            format(dashToString(stroke.getDashArray()));
        }
        if (stroke.getEndCap() != BasicStroke.CAP_BUTT) {
            format(" stroke-linecap=\"%s\"",
                    stroke.getEndCap() == BasicStroke.CAP_ROUND ? "round" : "square");
        }
        if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
            format(" stroke-linejoin=\"%s\"",
                    stroke.getEndCap() == BasicStroke.JOIN_ROUND ? "round" : "bevel");
        }
        format("/>%n");
        Arrowhead arrow = line.getStartArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            print(indentation);
            format("<dm:start-arrow line=\"%s\" type=\"%s\" />%n",
                    line.getEntityID(), arrow.toString());
            printArrow(arrow, points[2], points[3], points[0], points[1],
                    line.getStroke().getLineWidth(), line.getStrokeColor());
        }
        arrow = line.getEndArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the end arrowhead
            print(indentation);
            format("<dm:end-arrow line=\"%s\" type=\"%s\" />%n",
                    line.getEntityID(), arrow.toString());
            printArrow(arrow, points[0], points[1], points[2], points[3],
                    line.getStroke().getLineWidth(), line.getStrokeColor());
        }
    }

    private void printMPolyline(MPolyline polyline) {
        print(indentation);
        BasicStroke stroke = polyline.getStroke();
        if (polyline.isClosed()) {
            format("<polygon id =\"%s\" ", polyline.getEntityID());
        } else {
            format("<polyline id = \"%s\" ", polyline.getEntityID());
        }

        Color strokeColor = polyline.getStrokeColor();
        if (strokeColor == null) {
            format(" stroke=\"none\"");
        } else {
            format(" stroke=\"#%06x\"", strokeColor.getRGB() & 0xffffff);
        }
        format(Locale.US, " stroke-width=\"%.1f\"", stroke.getLineWidth());
        if (stroke.getEndCap() != BasicStroke.CAP_BUTT) {
            format(" stroke-linecap=\"%s\"",
                    stroke.getEndCap() == BasicStroke.CAP_ROUND ? "round" : "square");
        }
        if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
            format(" stroke-linejoin=\"%s\"",
                    stroke.getEndCap() == BasicStroke.JOIN_ROUND ? "round" : "bevel");
        }
        if (stroke.getDashArray() != null) {
            format(dashToString(stroke.getDashArray()));
        }
        Color fillColor = polyline.getFillColor();
        if (fillColor == null) {
            format(" fill=\"none\"");
        } else {
            format(" fill=\"#%06x\"", fillColor.getRGB() & 0xffffff);
        }
        double opacity = polyline.getOpacity();
        if (fillColor != null && opacity != 1.0) {
            format(Locale.US, " fill-opacity=\"%.2f\"", opacity);
        }
        int windingRule = polyline.getWindingRule();
        if (windingRule == GeneralPath.WIND_EVEN_ODD) {
            format(" fill-rule=\"evenodd\"");
        }
        increaseIndent();
        format("%n%spoints = \"", indentation);
        double[] points = polyline.getPoints();
        for (int i = 0; i < points.length; i++) {
            format(Locale.US, " %.4f", points[i]);
        }
        format("\" />%n");
        decreaseIndent();
        Arrowhead arrow = polyline.getStartArrowhead();
        double[] dir;
        if (arrow != null && arrow != Arrowhead.NONE) {
            dir = polyline.getStartVector();
            if (dir != null) {
                print(indentation);
                format("<dm:start-arrow path=\"%s\" type=\"%s\" />%n",
                        polyline.getEntityID(), arrow.toString());
                printArrow(arrow, dir[2], dir[3], dir[0], dir[1],
                        polyline.getStroke().getLineWidth(), polyline.getStrokeColor());
                dir = null;
            }
        }
        arrow = polyline.getEndArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) {
            dir = polyline.getEndVector();
            if (dir != null) {
                print(indentation);
                format("<dm:end-arrow path=\"%s\" type=\"%s\" />%n",
                        polyline.getEntityID(), arrow.toString());
                printArrow(arrow, dir[0], dir[1], dir[2], dir[3],
                        polyline.getStroke().getLineWidth(), polyline.getStrokeColor());
                dir = null;
            }
        }
    }

    private void printPointVar(PointVar p) {
        print(indentation);
        if (p.isVisible()) {
            format(Locale.US, "<dm:point id = \"%s\" x = \"%.4f\" y = \"%.4f\" visible = \"true\"/>%n",
                    p.getEntityID(), p.x, p.y);
        } else {
            format(Locale.US, "<dm:point id = \"%s\" x = \"%.4f\" y = \"%.4f\" visible = \"false\"/>%n",
                    p.getEntityID(), p.x, p.y);

        }
    }

    private void resetIndent() {
        indentation = new StringBuffer();
    }

    private void printTrace(MTrace trace) {
        if (trace.isDegenerate()) {
            return;
        }
        BasicStroke stroke = trace.getStroke();
        format("%s<path id = \"%s\" ", indentation, trace.getEntityID());
        Color strokeColor = trace.getStrokeColor();
        if (strokeColor == null) {
            format(" stroke=\"none\"");
        } else {
            format(" stroke=\"#%06x\"", strokeColor.getRGB() & 0xffffff);
        }
        format(Locale.US, " stroke-width=\"%.1f\"", stroke.getLineWidth());
        if (stroke.getEndCap() != BasicStroke.CAP_BUTT) {
            format(" stroke-linecap=\"%s\"",
                    stroke.getEndCap() == BasicStroke.CAP_ROUND ? "round" : "square");
        }
        if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
            format(" stroke-linejoin=\"%s\"",
                    stroke.getEndCap() == BasicStroke.JOIN_ROUND ? "round" : "bevel");
        }
        if (stroke.getDashArray() != null) {
            format(dashToString(stroke.getDashArray()));
        }
        Color fillColor = trace.getFillColor();
        if (fillColor == null) {
            format(" fill=\"none\"");
        } else {
            format(" fill=\"#%06x\"", fillColor.getRGB() & 0xffffff);
        }
        double opacity = trace.getOpacity();
        if (fillColor != null && opacity != 1.0) {
            format(Locale.US, " fill-opacity=\"%.2f\"", opacity);
        }
        if (trace.getWindingRule() == GeneralPath.WIND_EVEN_ODD) {
            format(" fill-rule=\"evenodd\"");
        }
        format(" d = \"%n");
        AffineTransform at = new AffineTransform();
        trace.getTransform(at);
        increaseIndent();
        double[] d = new double[6];
        for (PathIterator pit = trace.getPathIterator(at); !pit.isDone(); pit.next()) {
            print(indentation);
            switch (pit.currentSegment(d)) {
                case (PathIterator.SEG_MOVETO): {
                    format(Locale.US, "M %.4f %.4f%n", d[0], d[1]);
                    break;
                }
                case (PathIterator.SEG_LINETO): {
                    format(Locale.US, "L %.4f %.4f%n", d[0], d[1]);
                    break;
                }
                case (PathIterator.SEG_QUADTO): {
                    format(Locale.US, "Q %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3]);
                    break;
                }
                case (PathIterator.SEG_CUBICTO): {
                    format(Locale.US, "C %.4f %.4f %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3], d[4], d[5]);
                    break;
                }
                case (PathIterator.SEG_CLOSE): {
                    format("Z%n");
                    break;
                }
                default:
            }
        }
        decreaseIndent();
        format("%s\" />%n", indentation);
        Arrowhead a = trace.getStartArrowhead();
        double[] dir;
        if (a != null && a != Arrowhead.NONE) {
            dir = trace.getStartVector(at);
            if (dir != null) {
                print(indentation);
                format("<dm:start-arrow path=\"%s\" type=\"%s\" />%n",
                        trace.getEntityID(), a.toString());
                printArrow(a, dir[2], dir[3], dir[0], dir[1],
                        trace.getStroke().getLineWidth(), trace.getStrokeColor());
                dir = null;
            }
        }
        a = trace.getEndArrowhead();
        if (a != null && a != Arrowhead.NONE) {
            dir = trace.getEndVector(at);
            if (dir != null) {
                print(indentation);
                format("<dm:end-arrow path=\"%s\" type=\"%s\" />%n",
                        trace.getEntityID(), a.toString());
                printArrow(a, dir[0], dir[1], dir[2], dir[3],
                        trace.getStroke().getLineWidth(), trace.getStrokeColor());
                dir = null;
            }
        }
    }

    private void printImage(MImage mImage) {
        ShapeVar shape = mImage.getShape();
        boolean isClipped = false;
        if (shape.getPath() != null && shape.getConstraint() != null) {//image is clipped
            isClipped = true;
            format("%s<g clip-path=\"url(#%s)\">%n", indentation, shape.getEntityID());
            increaseIndent();
        }
        format("%s<image id = \"%s\" ", indentation, mImage.getEntityID());
        double opacity = mImage.getOpacity();
        if (opacity != 1.0) {
            format(Locale.US, " opacity=\"%.2f\"", opacity);
        }
        format(Locale.US, " xlink:href=\"%s\" width=\"%d\" height=\"%d\"",
                mImage.getFileName(), mImage.getWidth(), mImage.getHeight());
        AffineTransform at = new AffineTransform();
        mImage.getTransform(at);
        double[] d = new double[6];
        at.getMatrix(d);
        format(Locale.US, " transform = \"matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)\" />%n",
                d[0], d[1], d[2], d[3], d[4], d[5]);
        if (isClipped) {
            decreaseIndent();
            format("%s</g>%n", indentation);
        }
    }

    void writeModel(DrawingModel model) {
        Selection selection = new Selection();
        selection.setModel(model);
        for (Iterator<DrawableEntity> i = model.getDrawableEntityIterator(false); i.hasNext();) {
            selection.addHit(i.next());
        }
        writeSelection(selection, null);
    }

    void writeSelection(Selection selection, Point2D.Double refPoint) {
        if (!selection.isMovable()) {
            return;
        }
        DrawingModel model = selection.getModel();
        resetIndent();
        println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        println("<!-- Created with Drawmetry (http://www.drawmetry.com/) -->");
        Rectangle2D rect = selection.getBounds();
        format(Locale.US, "<svg width = \"%.2f\" height = \"%.2f\"%n",
                rect.getWidth() + 20, rect.getHeight() + 20);
        increaseIndent();
        print(indentation);
        println("xmlns:dm=\"http://www.drawmetry.com\"");
        print(indentation);
        println("xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        print(indentation);
        println("xmlns=\"http://www.w3.org/2000/svg\">");
        printDefs(model, selection);
        print(indentation);
        if (refPoint == null) {
            format(Locale.US, "<g transform = \"translate(%.2f, %.2f)\" >%n", -rect.getX() + 10, -rect.getY() + 10);
        } else {
            format(Locale.US, "<g transform = \"translate(%.2f, %.2f)\" >%n", -refPoint.x, -refPoint.y);
        }
        increaseIndent();
        printDrawables(selection);
        decreaseIndent();
        println(indentation + "</g>");
        //
        printConstraints(model, selection);
        model.disableUpdate();
        decreaseIndent();
        print(indentation);
        println("</svg>");
        flush();

    }

    private void printDrawables(Selection selection) {
        for (Iterator<DrawableEntity> i = selection.getHits().iterator(); i.hasNext();) {
            DrawableEntity de = i.next();
            if (de instanceof PointVar) {
                printPointVar((PointVar) de);
            } else if (de instanceof MPolyline) {
                MPolyline mpl = (MPolyline) de;
                if (mpl.getPoints().length == 4) {
                    printMLine(mpl);
                } else {
                    printMPolyline(mpl);
                }
            } else if (de instanceof MTrace) {
                printTrace((MTrace) de);
            } else if (de instanceof MImage) {
                printImage((MImage) de);
            } else if (de instanceof MCell) {
                printMCell((MCell) de);
            }
        }
    }

    private void printDefs(DrawingModel model, Selection selection) {
        format("%s<defs>%n", indentation);
        increaseIndent();
        List<DmEntity> dependents = model.getDependents(selection);
        if (dependents != null && !dependents.isEmpty()) {
            for (DmEntity e : dependents) {
                if (e instanceof ShapeVar && e.getConstraint() != null) {
                    ShapeVar shape = (ShapeVar) e;
                    GeneralPath gp = shape.getPath();
                    format("%s<clipPath id=\"%s\">%n", indentation, shape.getEntityID());
                    increaseIndent();
                    format("%s<path", indentation);
                    if (gp.getWindingRule() == GeneralPath.WIND_EVEN_ODD) {
                        format(" fill-rule=\"evenodd\"");
                    }
                    format(" d = \"%n");
                    increaseIndent();
                    double[] d = new double[6];
                    for (PathIterator pit = gp.getPathIterator(null); !pit.isDone(); pit.next()) {
                        print(indentation);
                        switch (pit.currentSegment(d)) {
                            case (PathIterator.SEG_MOVETO): {
                                format(Locale.US, "M %.4f %.4f%n", d[0], d[1]);
                                break;
                            }
                            case (PathIterator.SEG_LINETO): {
                                format(Locale.US, "L %.4f %.4f%n", d[0], d[1]);
                                break;
                            }
                            case (PathIterator.SEG_QUADTO): {
                                format(Locale.US, "Q %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3]);
                                break;
                            }
                            case (PathIterator.SEG_CUBICTO): {
                                format(Locale.US, "C %.4f %.4f %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3], d[4], d[5]);
                                break;
                            }
                            case (PathIterator.SEG_CLOSE): {
                                format("Z%n");
                                break;
                            }
                            default:
                        }
                    }
                    decreaseIndent();
                    format("%s\" />%n", indentation);
                    decreaseIndent();
                    format("%s</clipPath>%n", indentation);
                }
            }
        }
        decreaseIndent();
        format("%s</defs>%n", indentation);
    }

    private void printConstraints(DrawingModel model, Selection selection) {
        List<DmEntity> dependents = model.getDependents(selection);
        if (dependents != null && !dependents.isEmpty()) {
            for (DmEntity e : dependents) {
                if (e.getEntityID() != null) {
                    if (e instanceof CellContent) {
                        CellContent content = (CellContent) e;
                        if (content.getConstraint() != null) {
                            format("%s<dm:constraint dm:target = \"%s\" dm:constraint-text = \"= %s\" />%n",
                                    indentation, content.getEntityID(), content.getConstraint());
                        }
                    } else if (e instanceof AnchorVar) {
                        AnchorVar anchor = (AnchorVar) e;
                        if (anchor.getConstraint() != null) {
                            format("%s<dm:constraint dm:target = \"%s\" dm:constraint-text = \"= %s\" />%n",
                                    indentation, anchor.getEntityID(), anchor.getConstraint());
                        }
                    } else if (e instanceof ShapeVar) {
                        ShapeVar shape = (ShapeVar) e;
                        if (shape.getConstraint() != null) {
                            format("%s<dm:constraint dm:target = \"%s\" dm:constraint-text = \"= %s\" />%n",
                                    indentation, shape.getEntityID(), shape.getConstraint());
                        }
                    } else if (e instanceof MTrace || e instanceof MCell || e instanceof MImage) {
                        //do nothing
                    } else if (e instanceof DrawableEntity && e.getConstraint() != null) {
                        format("%s<dm:constraint dm:target = \"%s\" dm:constraint-text = \"= %s\" />%n",
                                indentation, e.getEntityID(), e.getConstraint());
                    }
                }
            }
        }
    }

    private void printArrow(Arrowhead a, double refX, double refY,
            double endX, double endY, double scale, Color color) {
        if (a != null) { // insert the start arrowhead
            AffineTransform at = a.getAlignment(refX, refY, endX, endY, scale);
            double[] d = new double[6];
            at.getMatrix(d);
            print(indentation);
            format(Locale.US, "<g transform=\"matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)\" >%n",
                    d[0], d[1], d[2], d[3], d[4], d[5]);
            increaseIndent();
            printShape(a.getShape(), a.getFill(), color);
            decreaseIndent();
            print(indentation);
            println("</g>");
        }

    }

    private void printShape(Shape s, boolean fill, Color color) {
        print(indentation);
        if (s instanceof Polygon) {
            Polygon p = (Polygon) s;
            int[] xPoints = p.xpoints;
            int[] yPoints = p.ypoints;
            if (fill) {
                format("<polygon stroke=\"#%1$06x\" fill=\"#%1$06x\" points=\"",
                        color.getRGB() & 0xffffff);
            } else {
                format("<polygon stroke=\"#%1$06x\" fill=\"none\" points=\"",
                        color.getRGB() & 0xffffff);
            }
            for (int i = 0; i < xPoints.length; i++) {
                format("%d,%d ", xPoints[i], yPoints[i]);
            }
            println("\" />");

        } else if (s instanceof Polyline) {
            Polyline p = (Polyline) s;
            int[] xPoints = p.xpoints;
            int[] yPoints = p.ypoints;
            if (fill) {
                format("<polyline stroke=\"#%1$06x\" fill=\"#%1$06x\" points=\"",
                        color.getRGB() & 0xffffff);
            } else {
                format("<polyline stroke=\"#%1$06x\" fill=\"none\" points=\"",
                        color.getRGB() & 0xffffff);
            }
            for (int i = 0; i < xPoints.length; i++) {
                format("%d,%d ", xPoints[i], yPoints[i]);
            }
            println("\" />");
        } else if (s instanceof Line2D) {
            Line2D p = (Line2D.Double) s;
            format(Locale.US, "<line stroke=\"#%06x\" x1 = \"%.2f\" y1 = \"%.2f\" x2 = \"%.2f\" y2 = \"%.2f\" />%n",
                    color.getRGB() & 0xffffff, p.getX1(), p.getY1(), p.getX2(), p.getY2());
        } else if (s instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) s;
            if (fill) {
                format(Locale.US, "<ellipse stroke=\"#%1$06x\" fill=\"#%1$06x\" "
                        + "cx=\"%2$.2f\" cy=\"%3$.2f\" rx=\"%4$.2f\" ry=\"%5$.2f\" />%n",
                        color.getRGB() & 0xffffff,
                        e.getX() + e.getWidth() / 2, e.getY() + e.getHeight() / 2,
                        e.getWidth() / 2, e.getHeight() / 2);
            } else {
                format(Locale.US, "<ellipse stroke=\"#%1$06x\" fill=\"none\" "
                        + "cx=\"%2$.2f\" cy=\"%3$.2f\" rx=\"%4$.2f\" ry=\"%5$.2f\" />%n",
                        color.getRGB() & 0xffffff,
                        e.getX() + e.getWidth() / 2, e.getY() + e.getHeight() / 2,
                        e.getWidth() / 2, e.getHeight() / 2);
            }
        } else {
            println("<!-- Shape not saved -->");
        }

    }

    private String dashToString(float[] dash) {
        StringBuilder sb = new StringBuilder(" stroke-dasharray=\"");
        for (int i = 0; i < dash.length; i++) {
            sb.append(dash[i]);
            sb.append(i == dash.length - 1 ? "\"" : ", ");
        }
        return sb.toString();
    }
}
