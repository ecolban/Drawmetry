package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

class DmWriter {

    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    XMLStreamWriter writer;
    private final OutputStream out;
    private ByteArrayOutputStream bout = new ByteArrayOutputStream();

    DmWriter(OutputStream out) throws XMLStreamException {
        this.out = out;
        writer = factory.createXMLStreamWriter(bout, "UTF-8");
    }

    public void close() {
        try {
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void printMCell(MCell mc) throws XMLStreamException {
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
        writer.writeStartElement("text");
        writer.writeAttribute("id", mc.getEntityID());
        writer.writeAttribute("font-family", font.getFamily());
        writer.writeAttribute("font-size", "" + font.getSize());
        writer.writeAttribute("fill",
                String.format("#%06x", mc.getFontColor().getRGB() & 0xffffff));
        writer.writeAttribute("transform",
                String.format(
                "matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)",
                d[0], d[1], d[2], d[3], d[4], d[5]));
        writer.writeEmptyElement("dm:cell-dimension");
        writer.writeAttribute("x", String.format("%.2f", bounds.getX()));
        writer.writeAttribute("y", String.format("%.2f", bounds.getY()));
        writer.writeAttribute("width", String.format("%.2f", bounds.getWidth()));
        writer.writeAttribute("height", String.format("%.2f", bounds.getHeight()));
        writer.writeAttribute("visible", mc.isVisible() ? "true" : "false");
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
        writer.writeEmptyElement("dm:cell-format");
        writer.writeAttribute("halign", halign);
        writer.writeAttribute("valign", valign);
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
            writer.writeStartElement("tspan");
            writer.writeAttribute("x", String.format("%.2f", drawPosX));
            writer.writeAttribute("y", String.format("%.2f", drawPosY));
            textEnd += layout.getCharacterCount();
            writer.writeCharacters(text.substring(textStart, textEnd));
            textStart = textEnd;
            writer.writeEndElement();
            drawPosY += layout.getDescent() + layout.getLeading();
        }
        writer.writeEndElement();
    }

    private void printMLine(MPolyline line) throws XMLStreamException {
        assert line.getPoints().length == 4;
        double[] points = line.getPoints();
        BasicStroke stroke = line.getStroke();
        writer.writeEmptyElement("line");
        writer.writeAttribute("id", line.getEntityID());
        writer.writeAttribute("x1", String.format("%.4f", points[0]));
        writer.writeAttribute("y1", String.format("%.4f", points[1]));
        writer.writeAttribute("x2", String.format("%.4f", points[2]));
        writer.writeAttribute("y2", String.format("%.4f", points[3]));

        Color strokeColor = line.getStrokeColor();
        if (strokeColor == null) {
            writer.writeAttribute("stroke", "none");
        } else {
            writer.writeAttribute("stroke", String.format("#%06x", strokeColor.getRGB() & 0xffffff));
        }
        writer.writeAttribute("stroke-width", String.format("%.1f", stroke.getLineWidth()));
        if (stroke.getDashArray() != null) {
            writer.writeAttribute("stroke-dasharray", dashToString(stroke.getDashArray()));
        }
        if (stroke.getEndCap() != BasicStroke.CAP_BUTT) {
            writer.writeAttribute("stroke-linecap",
                    stroke.getEndCap() == BasicStroke.CAP_ROUND
                    ? "round"
                    : "square");
        }
        if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
            writer.writeAttribute("stroke-linejoin",
                    stroke.getEndCap() == BasicStroke.JOIN_ROUND ? "round" : "bevel");
        }
        Arrowhead arrow = line.getStartArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            writer.writeEmptyElement("dm:start-arrow");
            writer.writeAttribute("line", line.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, points[2], points[3], points[0], points[1],
                    line.getStroke().getLineWidth(), line.getStrokeColor());
        }
        arrow = line.getEndArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the end arrowhead
            writer.writeEmptyElement("dm:end-arrow");
            writer.writeAttribute("line", line.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, points[0], points[1], points[2], points[3],
                    line.getStroke().getLineWidth(), line.getStrokeColor());
        }
    }

    private void printMPolyline(MPolyline polyline) throws XMLStreamException {
        BasicStroke stroke = polyline.getStroke();
        if (polyline.isClosed()) {
            writer.writeEmptyElement("polygon");
        } else {
            writer.writeEmptyElement("polyline");
        }
        writer.writeAttribute("id", polyline.getEntityID());
        Color strokeColor = polyline.getStrokeColor();
        if (strokeColor == null) {
            writer.writeAttribute("stroke", "none");
        } else {
            writer.writeAttribute("stroke",
                    String.format(Locale.US, "#%06x", strokeColor.getRGB() & 0xffffff));
        }
        writer.writeAttribute("stroke-width",
                String.format(Locale.US, "%.1f", stroke.getLineWidth()));
        if (stroke.getEndCap() == BasicStroke.CAP_ROUND) {
            writer.writeAttribute("stroke-linecap", "round");

        } else if (stroke.getEndCap() == BasicStroke.CAP_SQUARE) {
            writer.writeAttribute("stroke-linecap", "square");
        }
        if (stroke.getLineJoin() == BasicStroke.JOIN_ROUND) {
            writer.writeAttribute("stroke-linejoin", "round");

        } else if (stroke.getEndCap() == BasicStroke.JOIN_BEVEL) {
            writer.writeAttribute("stroke-linejoin", "bevel");
        }
        if (stroke.getDashArray() != null) {
            writer.writeAttribute("stroke-dasharray", dashToString(stroke.getDashArray()));
        }
        Color fillColor = polyline.getFillColor();
        if (fillColor == null) {
            writer.writeAttribute("fill", "none");
        } else {
            writer.writeAttribute("fill",
                    String.format(Locale.US, "#%06x", fillColor.getRGB() & 0xffffff));
        }
        double opacity = polyline.getOpacity();
        if (fillColor != null && opacity != 1.0) {
            writer.writeAttribute("fill-opacity",
                    String.format(Locale.US, "%.2f", opacity));
        }
        int windingRule = polyline.getWindingRule();
        if (windingRule == GeneralPath.WIND_EVEN_ODD) {
            writer.writeAttribute("fill-rule", "evenodd");
        }
        double[] points = polyline.getPoints();
        StringBuilder sb = new StringBuilder();
        assert points != null && points.length > 0;
        sb.append(String.format(Locale.US, "%.4f", points[0]));
        for (int i = 1; i < points.length; i++) {
            sb.append(String.format(Locale.US, " %.4f", points[i]));
        }
        writer.writeAttribute("points", sb.toString());
        Arrowhead arrow = polyline.getStartArrowhead();
        double[] dir;
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            dir = polyline.getStartVector();
            writer.writeEmptyElement("dm:start-arrow");
            writer.writeAttribute("path", polyline.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, dir[2], dir[3], dir[0], dir[1],
                    polyline.getStroke().getLineWidth(), polyline.getStrokeColor());
        }
        arrow = polyline.getEndArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            dir = polyline.getEndVector();
            writer.writeEmptyElement("dm:end-arrow");
            writer.writeAttribute("path", polyline.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, dir[0], dir[1], dir[2], dir[3],
                    polyline.getStroke().getLineWidth(), polyline.getStrokeColor());
        }
    }

    private void printPointVar(PointVar p) throws XMLStreamException {
        writer.writeEmptyElement("dm:point");
        writer.writeAttribute("id", p.getEntityID());
        writer.writeAttribute("x", String.format(Locale.US, "%.4f", p.x));
        writer.writeAttribute("y", String.format(Locale.US, "%.4f", p.y));
        if (p.isVisible()) {
            writer.writeAttribute("visible", "true");
        } else {
            writer.writeAttribute("visible", "false");
        }
    }

    private void printTrace(MTrace trace) throws XMLStreamException {
        if (trace.isDegenerate()) {
            return;
        }
        BasicStroke stroke = trace.getStroke();
        writer.writeEmptyElement("path");
        writer.writeAttribute("id", trace.getEntityID());
        Color strokeColor = trace.getStrokeColor();
        if (strokeColor == null) {
            writer.writeAttribute("stroke", "none");
        } else {
            writer.writeAttribute("stroke",
                    String.format(Locale.US, "#%06x", strokeColor.getRGB() & 0xffffff));
        }
        writer.writeAttribute("stroke-width",
                String.format(Locale.US, "%.1f", stroke.getLineWidth()));
        if (stroke.getEndCap() == BasicStroke.CAP_ROUND) {
            writer.writeAttribute("stroke-linecap", "round");
        } else if (stroke.getEndCap() == BasicStroke.CAP_SQUARE) {
            writer.writeAttribute("stroke-linecap", "square");
        }
        if (stroke.getLineJoin() == BasicStroke.JOIN_ROUND) {
            writer.writeAttribute("stroke-linejoin", "round");
        } else if (stroke.getLineJoin() == BasicStroke.JOIN_BEVEL) {
            writer.writeAttribute("stroke-linejoin", "bevel");
        }
        if (stroke.getDashArray() != null) {
            writer.writeAttribute("stroke-dasharray", dashToString(stroke.getDashArray()));
        }
        Color fillColor = trace.getFillColor();
        if (fillColor == null) {
            writer.writeAttribute("fill", "none");
        } else {
            writer.writeAttribute("fill",
                    String.format(Locale.US, "#%06x", fillColor.getRGB() & 0xffffff));
        }
        double opacity = trace.getOpacity();
        if (fillColor != null && opacity != 1.0) {
            writer.writeAttribute("fill-opacity",
                    String.format(Locale.US, "%.2f", opacity));
        }
        int windingRule = trace.getWindingRule();
        if (windingRule == GeneralPath.WIND_EVEN_ODD) {
            writer.writeAttribute("fill-rule", "evenodd");
        }

        AffineTransform at = new AffineTransform();
        trace.getTransform(at);
        double[] d = new double[6];
        StringBuilder sb = new StringBuilder();
        for (PathIterator pit = trace.getPathIterator(at); !pit.isDone(); pit.next()) {
            switch (pit.currentSegment(d)) {
                case (PathIterator.SEG_MOVETO): {
                    sb.append(String.format(Locale.US, "M %.4f %.4f%n", d[0], d[1]));
                    break;
                }
                case (PathIterator.SEG_LINETO): {
                    sb.append(String.format(Locale.US, "L %.4f %.4f%n", d[0], d[1]));
                    break;
                }
                case (PathIterator.SEG_QUADTO): {
                    sb.append(String.format(Locale.US, "Q %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3]));
                    break;
                }
                case (PathIterator.SEG_CUBICTO): {
                    sb.append(String.format(Locale.US, "C %.4f %.4f %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3], d[4], d[5]));
                    break;
                }
                case (PathIterator.SEG_CLOSE): {
                    sb.append(String.format("Z%n"));
                    break;
                }
                default:
            }
        }
        writer.writeAttribute("d", sb.toString());
        Arrowhead arrow = trace.getStartArrowhead();
        double[] dir;
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            dir = trace.getStartVector(at);
            writer.writeEmptyElement("dm:start-arrow");
            writer.writeAttribute("path", trace.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, dir[2], dir[3], dir[0], dir[1],
                    trace.getStroke().getLineWidth(), trace.getStrokeColor());
        }
        arrow = trace.getEndArrowhead();
        if (arrow != null && arrow != Arrowhead.NONE) { // insert the start arrowhead
            dir = trace.getEndVector(at);
            writer.writeEmptyElement("dm:end-arrow");
            writer.writeAttribute("path", trace.getEntityID());
            writer.writeAttribute("type", arrow.toString());
            printArrow(arrow, dir[0], dir[1], dir[2], dir[3],
                    trace.getStroke().getLineWidth(), trace.getStrokeColor());
        }
    }

    private void printImage(MImage mImage) throws XMLStreamException {
        ShapeVar shape = mImage.getShape();
        boolean isClipped = false;
        if (shape.getPath() != null && shape.getConstraint() != null) {//image is clipped
            isClipped = true;
            writer.writeStartElement("g");
            writer.writeAttribute("clip-path",
                    String.format(Locale.US, "url(#%s)", shape.getEntityID()));
        }
        writer.writeStartElement("image");
        writer.writeAttribute("id", mImage.getEntityID());
        writer.writeAttribute("xlink:href", mImage.getFileName());
        writer.writeAttribute("width", String.format(Locale.US, "%d", mImage.getWidth()));
        writer.writeAttribute("height", String.format(Locale.US, "%d", mImage.getHeight()));
        double opacity = mImage.getOpacity();
        if (opacity != 1.0) {
            writer.writeAttribute("fill-opacity", String.format(Locale.US, "%.2f", opacity));
        }
        AffineTransform at = new AffineTransform();
        mImage.getTransform(at);
        double[] d = new double[6];
        at.getMatrix(d);
        writer.writeAttribute("transform",
                String.format(Locale.US, "matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)",
                d[0], d[1], d[2], d[3], d[4], d[5]));
        if (isClipped) {
            writer.writeEndElement();
        }
    }

    void writeModel(DrawingModel model) throws XMLStreamException, TransformerConfigurationException, TransformerException {
        Selection selection = new Selection();
        selection.setModel(model);
        for (Iterator<DrawableEntity> i = model.getDrawableEntityIterator(false); i.hasNext();) {
            selection.addHit(i.next());
        }
        writeSelection(selection, null);
    }

    void writeSelection(Selection selection, Point2D.Double refPoint) throws XMLStreamException, TransformerConfigurationException, TransformerException {
        if (!selection.isMovable()) {
            return;
        }
        DrawingModel model = selection.getModel();
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeComment("Created with Drawmetry (http://www.drawmetry.com/)");
        Rectangle2D rect = selection.getBounds();
        writer.writeStartElement("svg");
        writer.writeAttribute("width", String.format(Locale.US, "%.2f", rect.getWidth() + 20));
        writer.writeAttribute("height", String.format(Locale.US, "%.2f", rect.getHeight() + 20));
        writer.writeDefaultNamespace("http://www.w3.org/2000/svg");
        writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
        writer.writeNamespace("dm", "http://www.drawmetry.com");
        printDefs(model, selection);
        writer.writeStartElement("g");
        if (refPoint == null) {
            writer.writeAttribute("transform",
                    String.format(Locale.US, "translate(%.2f, %.2f)", -rect.getX() + 10, -rect.getY() + 10));
        } else {
            writer.writeAttribute("transform",
                    String.format(Locale.US, "translate(%.2f, %.2f)", -refPoint.x, -refPoint.y));
        }
        printDrawables(selection);
        writer.writeEndElement();
        //
        printConstraints(model, selection);
        model.disableUpdate();
        writer.writeEndDocument();
        writer.flush();
        writer.close();
        TransformerFactory fact = TransformerFactory.newInstance();
        Transformer transformer = fact.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        Source source = new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
        transformer.transform(source, new StreamResult(out));
    }

    private void printDrawables(Selection selection) throws XMLStreamException {
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

    private void printDefs(DrawingModel model, Selection selection) throws XMLStreamException {
        writer.writeStartElement("defs");
        List<DmEntity> dependents = model.getDependents(selection);
        if (dependents != null && !dependents.isEmpty()) {
            for (DmEntity e : dependents) {
                if (e instanceof ShapeVar && e.getConstraint() != null) {
                    ShapeVar shape = (ShapeVar) e;
                    GeneralPath gp = shape.getPath();
                    writer.writeStartElement("clipPath");
                    writer.writeAttribute("id", shape.getEntityID());
                    writer.writeEmptyElement("path");
                    if (gp.getWindingRule() == GeneralPath.WIND_EVEN_ODD) {
                        writer.writeAttribute("fill-rule", "evenodd");
                    }
                    double[] d = new double[6];
                    StringBuilder sb = new StringBuilder();
                    for (PathIterator pit = gp.getPathIterator(null); !pit.isDone(); pit.next()) {
                        switch (pit.currentSegment(d)) {
                            case (PathIterator.SEG_MOVETO): {
                                sb.append(String.format(Locale.US, "M %.4f %.4f%n", d[0], d[1]));
                                break;
                            }
                            case (PathIterator.SEG_LINETO): {
                                sb.append(String.format(Locale.US, "L %.4f %.4f%n", d[0], d[1]));
                                break;
                            }
                            case (PathIterator.SEG_QUADTO): {
                                sb.append(String.format(Locale.US, "Q %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3]));
                                break;
                            }
                            case (PathIterator.SEG_CUBICTO): {
                                sb.append(String.format(Locale.US, "C %.4f %.4f %.4f %.4f %.4f %.4f%n", d[0], d[1], d[2], d[3], d[4], d[5]));
                                break;
                            }
                            case (PathIterator.SEG_CLOSE): {
                                sb.append(String.format("Z%n"));
                                break;
                            }
                            default:
                        }
                    }
                    writer.writeAttribute("d", sb.toString());
                    writer.writeEndElement();
                }
            }
        }
        writer.writeEndElement();
    }

    private void printConstraints(DrawingModel model, Selection selection) throws XMLStreamException {
        List<DmEntity> dependents = model.getDependents(selection);
        if (dependents != null && !dependents.isEmpty()) {
            for (DmEntity e : dependents) {
                if (e.getEntityID() != null) {
                    if (e instanceof CellContent) {
                        CellContent content = (CellContent) e;
                        if (content.getConstraint() != null) {
                            writer.writeEmptyElement("dm:constraint");
                            writer.writeAttribute("dm:target", content.getEntityID());
                            writer.writeAttribute("dm:constraint-text", "=" + content.getConstraint().toString());
                        }
                    } else if (e instanceof AnchorVar) {
                        AnchorVar anchor = (AnchorVar) e;
                        if (anchor.getConstraint() != null) {
                            writer.writeEmptyElement("dm:constraint");
                            writer.writeAttribute("dm:target", anchor.getEntityID());
                            writer.writeAttribute("dm:constraint-text", "=" + anchor.getConstraint().toString());
                        }
                    } else if (e instanceof ShapeVar) {
                        ShapeVar shape = (ShapeVar) e;
                        if (shape.getConstraint() != null) {
                            writer.writeEmptyElement("dm:constraint");
                            writer.writeAttribute("dm:target", shape.getEntityID());
                            writer.writeAttribute("dm:constraint-text", "=" + shape.getConstraint().toString());
                        }
                    } else if (e instanceof MTrace || e instanceof MCell || e instanceof MImage) {
                        //do nothing
                    } else if (e instanceof DrawableEntity && e.getConstraint() != null) {
                        writer.writeEmptyElement("dm:constraint");
                        writer.writeAttribute("dm:target", e.getEntityID());
                        writer.writeAttribute("dm:constraint-text", "=" + e.getConstraint().toString());
                    }
                }
            }
        }
    }

    private void printArrow(Arrowhead a, double refX, double refY,
            double endX, double endY, double scale, Color color) throws XMLStreamException {
        if (a != null) { // insert the start arrowhead
            AffineTransform at = a.getAlignment(refX, refY, endX, endY, scale);
            double[] d = new double[6];
            at.getMatrix(d);
            writer.writeStartElement("g");
            writer.writeAttribute("transform",
                    String.format("matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)",
                    d[0], d[1], d[2], d[3], d[4], d[5]));
            printShape(a.getShape(), a.getFill(), color);
            writer.writeEndElement();
        }

    }

    private void printShape(Shape s, boolean fill, Color color) throws XMLStreamException {
        if (s instanceof Polygon || s instanceof Polyline) {
            int[] xPoints = null;
            int[] yPoints = null;
            if (s instanceof Polygon) {
                Polygon p = (Polygon) s;
                xPoints = p.xpoints;
                yPoints = p.ypoints;
                writer.writeEmptyElement("polygon");
            } else {
                Polyline p = (Polyline) s;
                xPoints = p.xpoints;
                yPoints = p.ypoints;
                writer.writeEmptyElement("polyline");
            }
            writer.writeAttribute("stroke",
                    String.format(Locale.US, "#%1$06x", color.getRGB() & 0xffffff));
            if (fill) {
                writer.writeAttribute("fill",
                        String.format(Locale.US, "#%1$06x", color.getRGB() & 0xffffff));
            } else {
                writer.writeAttribute("fill", "none");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < xPoints.length; i++) {
                sb.append(String.format("%d,%d ", xPoints[i], yPoints[i]));
            }
            writer.writeAttribute("points", sb.toString());
        } else if (s instanceof Line2D) {
            Line2D p = (Line2D.Double) s;
            writer.writeEmptyElement("line");
            writer.writeAttribute("stroke", String.format(Locale.US, "#%06x", color.getRGB() & 0xffffff));
            writer.writeAttribute("x1", String.format(Locale.US, "%.2f", p.getX1()));
            writer.writeAttribute("y1", String.format(Locale.US, "%.2f", p.getY1()));
            writer.writeAttribute("x2", String.format(Locale.US, "%.2f", p.getX2()));
            writer.writeAttribute("y2", String.format(Locale.US, "%.2f", p.getY2()));
        } else if (s instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) s;
            writer.writeEmptyElement("ellipse");
            writer.writeAttribute(null, null);
            writer.writeAttribute("stroke", String.format(Locale.US, "#%06x", color.getRGB() & 0xffffff));
            if (fill) {
                writer.writeAttribute("fill", String.format(Locale.US, "#%06x", color.getRGB() & 0xffffff));
            }
            writer.writeAttribute("cx", String.format(Locale.US, "%.2f", e.getX()));
            writer.writeAttribute("cy", String.format(Locale.US, "%.2f", e.getY()));
            writer.writeAttribute("rx", String.format(Locale.US, "%.2f", e.getWidth() / 2));
            writer.writeAttribute("ry", String.format(Locale.US, "%.2f", e.getHeight() / 2));
        } else {
        }

    }

    private String dashToString(float[] dash) {
        assert dash != null && dash.length > 0;
        StringBuilder sb = new StringBuilder();
        sb.append(dash[0]);
        for (int i = 1; i < dash.length; i++) {
            sb.append(", ");
            sb.append(String.format(Locale.US, "%.1f", dash[i]));
        }
        return sb.toString();
    }
}
