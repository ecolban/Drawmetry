/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.drawmetry.constraints.AnchorConstraint;
import com.drawmetry.constraints.MPolylineConstraint;
import com.drawmetry.constraints.NullConstraint;

/**
 *
 * @author Erik
 */
class DmReader implements ErrorHandler, ContentHandler {

    private final static float miterLimit = StrokeHandler.DEFAULT_STROKE.getMiterLimit();
    private final static float dashPhase = StrokeHandler.DEFAULT_STROKE.getDashPhase();
    private final static Map<String, Integer> endCapMap = new HashMap<String, Integer>();
    private final static Map<String, Integer> lineJoinMap = new HashMap<String, Integer>();

    static {
        endCapMap.put("butt", new Integer(BasicStroke.CAP_BUTT));
        endCapMap.put("round", new Integer(BasicStroke.CAP_ROUND));
        endCapMap.put("square", new Integer(BasicStroke.CAP_SQUARE));
        lineJoinMap.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        lineJoinMap.put("miter", new Integer(BasicStroke.JOIN_MITER));
        lineJoinMap.put("round", new Integer(BasicStroke.JOIN_ROUND));
    }
    private DrawingModel model;
    private File currentFile;
    private MCell currentCell = null;
    private boolean readingCellContent = false;
    private StringBuilder textBuffer;
    double textTop;
    double textBottom;
    double textLeft;
    double textRight;
    private Parser parser = new Parser();
    private Map<String, String> idMap = new HashMap<String, String>();
    private Point2D.Double origin = new Point2D.Double(0.0, 0.0);
    private double zoom = 1.0;
    private boolean ignoreDefs;
    private Font font;
    private double startTextX;
    private double startTextY;
    private boolean cellDimIncludesX = false;
    private boolean cellDimIncludesY = false;

    DmReader(DrawingModel model) {
        this.model = model;
        parser.setModel(model);
    }

    DmReader(DrawingModel model, File file, double x, double y) {
        this.model = model;
        this.currentFile = file;
        parser.setModel(model);
        this.origin.x = x;
        this.origin.y = y;
    }

    DmReader(DrawingModel model, File file) {
        this.model = model;
        this.currentFile = file;
        parser.setModel(model);
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (ignoreDefs) {
            return;
        }
        if (qName.equals("g")) {
            startGroup(atts);
        } else if (qName.equals("defs")) {
            startDefs(atts);
        } else if (qName.equals("path")) {
            startPath(atts);
        } else if (qName.equals("image")) {
            startImage(atts);
        } else if (qName.equals("line")) {
            startLine(atts);
        } else if (qName.equals("polyline") || qName.equals("polygon")) {
            startPolyline(qName, atts);
        } else if (qName.equals("dm:point")) {
            startPoint(atts);
        } else if (qName.equals("text")) {
            startText(atts);
        } else if (qName.equals("dm:cell-dimension")) {
            startCellDimension(atts);
        } else if (qName.equals("dm:cell-format")) {
            startCellFormat(atts);
        } else if (qName.equals("tspan")) {
            startTspan(atts);
        } else if (qName.equals("dm:constraint")) {
            startConstraint(atts);
        } else if (qName.equals("dm:start-arrow")) {
            startArrow(atts, true);
        } else if (qName.equals("dm:end-arrow")) {
            startArrow(atts, false);
        } else {
//
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("text")) {
            endText();

        } else if (qName.equals("defs")) {
            ignoreDefs = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = String.copyValueOf(ch, start, length).trim();
        if (currentCell != null && readingCellContent && s.length() > 0) {
            AttributedString aString = new AttributedString(s);
            aString.addAttribute(TextAttribute.FONT, font);
            FontRenderContext frc = new FontRenderContext(null, true, true);
            LineBreakMeasurer lbm = new LineBreakMeasurer(aString.getIterator(), frc);
            TextLayout layout = lbm.nextLayout((float) (currentCell.getBounds().getWidth() * 2.0F));
            textLeft = Math.min(textLeft, startTextX);
            textRight = Math.max(textRight, startTextX + layout.getVisibleAdvance());
            textTop = Math.min(textTop, startTextY - layout.getAscent());
            textBottom = Math.max(textBottom, startTextY + layout.getDescent());
            if (textBuffer.length() > 0) {
                textBuffer.append(' ');
            }
            textBuffer.append(s);
        }
    }

//    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

//    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    public void warning(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    Point2D.Double getOrigin() {
        return origin;
    }

    double getZoom() {
        return zoom;
    }

    private void startArrow(Attributes atts, boolean start) {
        DrawableEntity target = null;
        Arrowhead arrow = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("line")) {
                String oldId = atts.getValue(i);
                if (oldId.matches("L_\\d+") || oldId.matches("S_\\d+")) {
                    String newId = idMap.get(oldId);
                    if (newId != null) {
                        target = model.getDrawableEntity(newId);
                    }
                } else {
                }
            } else if (atts.getQName(i).equals("path")) {
                String oldId = atts.getValue(i);
                if (oldId.matches("L_\\d+") || oldId.matches("S_\\d+")) {
                    String newId = idMap.get(oldId);
                    if (newId != null) {
                        target = model.getDrawableEntity(newId);
                    }
                } else {
                }
            } else if (atts.getQName(i).equals("type")) {
                arrow = Arrowhead.getArrowhead(atts.getValue(i));

            }
        }
        if (target != null && arrow != null) {
            if (start) {
                target.setStartArrowhead(arrow);
            } else {
                target.setEndArrowhead(arrow);
            }
        }
    }

    private void startGroup(Attributes atts) {
        for (int i = 0, len = atts.getLength(); i < len; i++) {
            if (atts.getQName(i).equals("transform")) {
                String value = atts.getValue(i);
                if (value.matches("\\s*translate.*")) {
                    Pattern p = Pattern.compile("-?\\s*\\d+?(\\.\\d+)");
                    Matcher m = p.matcher(value); // get a matcher object
                    if (m.find()) {
                        origin.x += Double.parseDouble(value.substring(m.start(), m.end()));
                    }
                    if (m.find()) {
                        origin.y += Double.parseDouble(value.substring(m.start(), m.end()));
                    }
                }
            }
        }
    }

    private void startDefs(Attributes atts) {
        ignoreDefs = true;
    }

    private void startLine(Attributes atts) {
//        BasicStroke stroke = new BasicStroke();
        String newId = "L_" + model.getLineNum();
        Color color = Color.BLACK;
        float width = StrokeHandler.DEFAULT_STROKE.getLineWidth();
        int endCap = StrokeHandler.DEFAULT_STROKE.getEndCap();
        int lineJoin = StrokeHandler.DEFAULT_STROKE.getLineJoin();
        float[] dashArray = null;
        for (int i = 0, l = atts.getLength(); i < l; i++) {
            if (atts.getQName(i).equals("id")) {
                idMap.put(atts.getValue(i), newId);
            } else if (atts.getQName(i).equals("stroke")) {
                if (atts.getValue(i).matches("none")) {
                    color = null;
                }
                try {
                    color = Color.decode(atts.getValue(i));
                } catch (NumberFormatException ex) {
                }
            } else if (atts.getQName(i).equals("stroke-width")) {
                width = Float.parseFloat(atts.getValue(i));
            } else if (atts.getQName(i).equals("stroke-linecap")) {
                endCap = endCapMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-linejoin")) {
                lineJoin = lineJoinMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-dasharray")) {
                String[] values = atts.getValue(i).split(",\\s*");
                dashArray = new float[values.length];
                for (int j = 0; j < dashArray.length; j++) {
                    dashArray[j] = Float.parseFloat(values[j]);
                }
            }
        }
        BasicStroke stroke = new BasicStroke(width, endCap, lineJoin, miterLimit, dashArray, dashPhase);
        MPolyline line = new MPolyline(color, null, 1.0, stroke, Arrowhead.NONE,
                Arrowhead.NONE, GeneralPath.WIND_NON_ZERO);
        line.setEntityID(newId);
        model.addDrawableEntity(line);
    }

    private void startPolyline(String qName, Attributes atts) throws SAXException {
        String newId;
        double[] points = null;
        Color strokeColor = Color.BLACK;
        Color fillColor = null;
        double fillOpacity = 1.0;
        float width = 1.0F;
        int endCap = BasicStroke.CAP_BUTT;
        int lineJoin = BasicStroke.JOIN_MITER;
        float[] dashArray = null;
        int windingRule = GeneralPath.WIND_NON_ZERO;
        String oldId = atts.getValue("id");
        if (oldId == null) {
            return;
        } else {
            newId = "L_" + model.getLineNum();
            idMap.put(oldId, newId);
        }
        for (int i = 0, l = atts.getLength(); i < l; i++) {
            if (atts.getQName(i).equals("points")) {
                String pointsString = atts.getValue(i);
                Pattern p = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher m = p.matcher(pointsString);
                int count = 0;
                while (m.find()) {
                    count++;
                }
                if (count == 0 || count % 2 != 0) {
                    throw new SAXException("Unable to parse polyline " + oldId
                            + ". Attribute points must have a positive even number of coordinates.");
                }
                points = new double[count];
                m.reset();
                int k = 0;
                while (m.find()) {
                    if (k % 2 == 0) {
                        points[k++] = origin.x + Double.parseDouble(pointsString.substring(m.start(), m.end()));
                    } else {
                        points[k++] = origin.y + Double.parseDouble(pointsString.substring(m.start(), m.end()));
                    }
                }
            } else if (atts.getQName(i).equals("stroke")) {
                if (atts.getValue(i).matches("none")) {
                    strokeColor = null;
                }
                try {
                    strokeColor = Color.decode(atts.getValue(i));
                } catch (NumberFormatException ex) {
                }
            } else if (atts.getQName(i).equals("fill")) {
                try {
                    fillColor = Color.decode(atts.getValue(i));
                } catch (NumberFormatException ex) {
                }
            } else if (atts.getQName(i).equals("fill-opacity")) {
                fillOpacity = Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("stroke-width")) {
                width = Float.parseFloat(atts.getValue(i));
            } else if (atts.getQName(i).equals("stroke-linecap")) {
                endCap = endCapMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-linejoin")) {
                lineJoin = lineJoinMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-dasharray")) {
                String[] values = atts.getValue(i).split(",\\s*");
                dashArray = new float[values.length];
                for (int j = 0; j < dashArray.length; j++) {
                    dashArray[j] = Float.parseFloat(values[j]);
                }
            } else if (atts.getQName(i).equals("fill-rule")) {
                windingRule = atts.getValue(i).equals("evenodd") ? GeneralPath.WIND_EVEN_ODD : GeneralPath.WIND_NON_ZERO;
            }
        }
        BasicStroke stroke = new BasicStroke(width, endCap, lineJoin, miterLimit, dashArray, dashPhase);
        MPolyline line = new MPolyline(strokeColor, fillColor, fillOpacity, stroke,
                Arrowhead.NONE, Arrowhead.NONE, windingRule);
        line.setPoints(points);
        line.setEntityID(newId);
        if (qName.equals("polygon")) {
            line.setClosed(true);
        }
        model.addDrawableEntity(line);
    }

    private void startPath(Attributes atts) {
        MTrace tr = new MTrace();
        String newId = "S_" + model.getTraceNum();
        float width = StrokeHandler.DEFAULT_STROKE.getLineWidth();
        int endCap = StrokeHandler.DEFAULT_STROKE.getEndCap();
        int lineJoin = StrokeHandler.DEFAULT_STROKE.getLineJoin();
        float[] dashArray = null;
        Color strokeColor = Color.BLACK;
        Color fillColor = null;
        double fillOpacity = 1.0;
        int windingRule = GeneralPath.WIND_NON_ZERO; //default
        String attrValue = null;
        boolean transformed = false;
        for (int i = 0, l = atts.getLength(); i
                < l; i++) {
            if (atts.getQName(i).equals("id")) {
                idMap.put(atts.getValue(i), newId);
            } else if (atts.getQName(i).equals("stroke")) {
                attrValue = atts.getValue(i);
                if (attrValue.matches("none")) {
                    strokeColor = null;
                } else {
                    try {
                        strokeColor = Color.decode(attrValue);
                    } catch (NumberFormatException ex) {
                    }
                }
            } else if (atts.getQName(i).equals("fill")) {
                attrValue = atts.getValue(i);
                if (attrValue.matches("none")) {
                    fillColor = null;
                } else {
                    try {
                        fillColor = Color.decode(attrValue);
                    } catch (NumberFormatException ex) {
                    }
                }
            } else if (atts.getQName(i).equals("fill-opacity")) {
                fillOpacity = Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("stroke-width")) {
                width = Float.parseFloat(atts.getValue(i));
            } else if (atts.getQName(i).equals("stroke-linecap")) {
                endCap = endCapMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-linejoin")) {
                lineJoin = lineJoinMap.get(atts.getValue(i)).intValue();
            } else if (atts.getQName(i).equals("stroke-dasharray")) {
                String[] values = atts.getValue(i).split(",\\s*");
                dashArray = new float[values.length];
                for (int j = 0; j
                        < dashArray.length; j++) {
                    dashArray[j] = Float.parseFloat(values[j]);
                }

            } else if (atts.getQName(i).equals("transform")) {
                double[] d = new double[6];
                transformed = true;
                attrValue = atts.getValue(i);
                Pattern p = Pattern.compile("-?\\d+\\.\\d+");
                Matcher m = p.matcher(attrValue); // get a matcher object
                int count = 0;
                while (m.find()) {
                    d[count] = Double.parseDouble(attrValue.substring(m.start(), m.end()));
                    count++;

                }
                assert count == 6;
                tr.getAnchor().setCtf(d[0], d[1], d[2], d[3], d[4] + origin.x, d[5] + origin.y);
            } else if (atts.getQName(i).equals("d")) {
                parsePath(tr, atts.getValue(i));
            } else if (atts.getQName(i).equals("fill-rule")) {
                attrValue = atts.getValue(i);
                windingRule = attrValue.equals("evenodd") ? GeneralPath.WIND_EVEN_ODD : GeneralPath.WIND_NON_ZERO;
            } else {
            }
        }
        if (!transformed && (origin.x != 0.0 || origin.y != 0.0)) {
            tr.getAnchor().setCtf(1.0, 0.0, 0.0, 1.0, origin.x, origin.y);
        }

        BasicStroke stroke = new BasicStroke(width, endCap, lineJoin, miterLimit, dashArray, dashPhase);
        tr.setStroke(stroke);
        tr.setStrokeColor(strokeColor);
        tr.setFillColor(fillColor);
        tr.setOpacity(fillOpacity);
        tr.setWindingRule(windingRule);
        tr.setEntityID(newId);


        model.addDrawableEntity(tr);
        try {
            model.addConstraint(new NullConstraint(1), new DmEntity[]{tr.getAnchor()}, tr);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (ConstraintGraphException ex) {
            ex.printStackTrace();
        }

    }

    private void startImage(Attributes atts) {
        MImage mImage = null;
        String newId = "I_" + model.getImageNum();
        double opacity = 1.0;
        double[] d = new double[6];
        String attrValue = null;
        boolean transformed = false;
        for (int i = 0, l = atts.getLength(); i < l; i++) {
            String qName = atts.getQName(i);
            String qValue = atts.getValue(i);
            if (atts.getQName(i).equals("id")) {
                idMap.put(atts.getValue(i), newId);
            } else if (atts.getQName(i).equals("xlink:href")) {
                mImage = readImageFile(atts.getValue(i));
            } else if (atts.getQName(i).equals("opacity")) {
                opacity = Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("transform")) {
                transformed = true;
                attrValue = atts.getValue(i);
                Pattern p = Pattern.compile("-?\\d+\\.\\d+");
                Matcher m = p.matcher(attrValue); // get a matcher object
                int count = 0;
                while (m.find()) {
                    d[count] = Double.parseDouble(attrValue.substring(m.start(), m.end()));
                    count++;
                }
                assert count == 6;
            } else {
            }
        }
        if (mImage == null) {
            return;
        }
        mImage.setOpacity(opacity);
        mImage.setEntityID(newId);
        model.addDrawableEntity(mImage);
        try {
            model.addConstraint(new NullConstraint(2), new DmEntity[]{new AnchorVar(), new ShapeVar()}, mImage);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (ConstraintGraphException ex) {
            ex.printStackTrace();
        }
        if (transformed) {
            mImage.getAnchor().setCtf(d[0], d[1], d[2], d[3], d[4] + origin.x, d[5] + origin.y);
        } else {
            mImage.getAnchor().setCtf(1.0, 0.0, 0.0, 1.0, origin.x, origin.y);
        }
    }

    private void parsePath(SimplePath path, String pathDesc) {

        String[] elmts = pathDesc.trim().split("\\s+");
        int argNum = 0;
        int index = 0;
        double args[] = new double[6];
        while (index < elmts.length) {
            if (elmts[index].equals("M")) {
                argNum = 0;
                index++;

                while (index < elmts.length) {
                    try {
                        args[argNum] = Double.parseDouble(elmts[index]);
                        argNum++;

                        index++;

                    } catch (NumberFormatException e) {
                        break;
                    }
                }
                path.moveTo(args[0], args[1]);
            } else if (elmts[index].equals("C")) {
                argNum = 0;
                index++;

                while (index < elmts.length) {
                    try {
                        args[argNum] = Double.parseDouble(elmts[index]);
                        argNum++;

                        index++;

                    } catch (NumberFormatException e) {
                        break;
                    }

                }
                path.curveTo(args[0], args[1], args[2], args[3], args[4], args[5]);
            } else if (elmts[index].equals("Q")) {
                argNum = 0;
                index++;

                while (index < elmts.length) {
                    try {
                        args[argNum] = Double.parseDouble(elmts[index]);
                        argNum++;

                        index++;

                    } catch (NumberFormatException e) {
                        break;
                    }
                }
                path.quadTo(args[0], args[1], args[2], args[3]);
            } else if (elmts[index].equals("L")) {
                argNum = 0;
                index++;

                while (index < elmts.length) {
                    try {
                        args[argNum] = Double.parseDouble(elmts[index]);
                        argNum++;

                        index++;

                    } catch (NumberFormatException e) {
                        break;
                    }

                }
                path.lineTo(args[0], args[1]);
            } else if (elmts[index].equals("Z")) {
                argNum = 0;
                index++;
                path.close();
            } else {
                index++;
            }
        }
    }

    private void startPoint(Attributes atts) {
        double x = 0.0, y = 0.0;
        boolean visible = true;
        String newId = "P_" + model.getPointNum();

        for (int i = 0, l = atts.getLength(); i < l; i++) {
            if (atts.getQName(i).equals("id")) {
                idMap.put(atts.getValue(i), newId);
            } else if (atts.getQName(i).equals("x")) {
                x = origin.x + Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("y")) {
                y = origin.y + Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("visible")) {
                visible = Boolean.parseBoolean(atts.getValue(i));
            } else {
            }
        }
        PointVar p = new PointVar(x, y, newId);
        p.setVisible(visible);
        model.addPoint(p);
    }

    private void startText(Attributes atts) {
        boolean xAssigned = false;
        boolean yAssigned = false;

        readingCellContent = true;
        String fontFamily = null;
        String matrix = null;
        String newId = "C_" + model.getCellNum();
        int fontSize = 0;
        Color color = null;
        MCell cell = new MCell(newId);
        for (int i = 0, l = atts.getLength(); i < l; i++) {
            if (atts.getQName(i).equals("id")) {
                idMap.put(atts.getValue(i), newId);
            } else if (atts.getQName(i).equals("x")) {
                startTextX = Double.parseDouble(atts.getValue(i));
                xAssigned = true;
            } else if (atts.getQName(i).equals("y")) {
                startTextY = Double.parseDouble(atts.getValue(i));
                yAssigned = true;
            } else if (atts.getQName(i).equals("fill")) {
                color = Color.decode(atts.getValue(i));
            } else if (atts.getQName(i).equals("font-size")) {
                fontSize = Integer.parseInt(atts.getValue(i));
            } else if (atts.getQName(i).equals("font-family")) {
                fontFamily = atts.getValue(i);
            } else if (atts.getQName(i).equals("transform")) {
                double[] d = new double[6];
                matrix = atts.getValue(i);
                Pattern p = Pattern.compile("-?\\d+(\\.\\d+)?");
                Matcher m = p.matcher(matrix); // get a matcher object
                int count = 0;
                while (m.find()) {
                    d[count] = Double.parseDouble(matrix.substring(m.start(), m.end()));
                    count++;
                }
                assert count == 6;
                cell.getAnchor().setCtf(d[0], d[1], d[2], d[3], d[4] + origin.x, d[5] + origin.y);
            } else {
            }
        }
        font = new Font(fontFamily, Font.PLAIN, fontSize);
        if (xAssigned) { // Assume single line text
            textLeft = textRight = startTextX;
        } else {
            textLeft = Double.MAX_VALUE;
            textRight = Double.MIN_VALUE;
        }
        if (yAssigned) { // Assume single line text
            textTop = textBottom = startTextY;
        } else {
            textTop = Double.MAX_VALUE;
            textBottom = Double.MIN_VALUE;
        }
        cell.setFormat(new MCell.CellFormat(font, color, MCell.CellFormat.CENTER, MCell.CellFormat.CENTER));
        model.addCell(cell);
        currentCell = cell;
        textBuffer = new StringBuilder();
    }

    private void startTspan(Attributes atts) {
        assert currentCell != null;
        boolean xAssigned = false;
        boolean yAssigned = false;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("x")) {
                startTextX = Double.parseDouble(atts.getValue(i));
                xAssigned = true;
            } else if (atts.getQName(i).equals("y")) {
                startTextY = Double.parseDouble(atts.getValue(i));
                yAssigned = true;
            }
        }
        if (xAssigned) { // Assume single line text
            textLeft = Math.min(textLeft, startTextX);
            textRight = Math.max(textRight, startTextX);
        }
        if (yAssigned) { // Assume single line text
            textTop = Math.min(textTop, startTextY);
            textBottom = Math.max(textBottom, startTextY);
        }
    }

    private void startCellDimension(Attributes atts) {
        assert currentCell != null;
        double x = 0.0;
        double y = 0.0;
        double w = 0.0;
        double h = 0.0;
        boolean v = true;
        cellDimIncludesX = false;
        cellDimIncludesY = false;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("x")) {
                x = Double.parseDouble(atts.getValue(i));
                cellDimIncludesX = true;
            } else if (atts.getQName(i).equals("y")) {
                y = Double.parseDouble(atts.getValue(i));
                cellDimIncludesY = true;
            } else if (atts.getQName(i).equals("width")) {
                w = Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("height")) {
                h = Double.parseDouble(atts.getValue(i));
            } else if (atts.getQName(i).equals("visible")) {
                v = Boolean.parseBoolean(atts.getValue(i));
            } else {
            }
        }
        currentCell.updateBounds(x, y, w, h);
        currentCell.setVisible(v);
    }

    private void startCellFormat(Attributes atts) {
        assert currentCell != null;
        boolean v = true;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("halign")) {
                currentCell.setHorizontalAlignment(atts.getValue(i));
            } else if (atts.getQName(i).equals("valign")) {
                currentCell.setVerticalAlignment(atts.getValue(i));
            } else {
            }
        }
    }

    private String getNewId(String oldId) throws SAXException {
        String newId = idMap.get(oldId);
        if (newId != null) {
            return newId;
        }

        throw new SAXException();
    }

    private void startConstraint(Attributes atts) throws SAXException {
        DmEntity target = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:target")) {
                String oldId = atts.getValue(i);
                if (oldId.matches("P_\\d+")) {
                    String newId = getNewId(oldId);
                    if (newId == null || (target = model.getPoint(newId)) == null) {
                        throw new SAXException("Unable to parse dm:constraint. "
                                + newId + " is not recognized.");
                    }

                    startPointConstraint((PointVar) target, atts);
                    break;

                } else if (oldId.matches("C_\\d+")) {
                    String newId = getNewId(oldId);
                    if (newId == null || (target = model.getCell(newId)) == null) {
                        throw new SAXException("Unable to parse dm:constraint. "
                                + newId + " is not recognized.");
                    }

                    startCellConstraint((MCell) target, atts);
                    break;

                } else if (oldId.matches("L_\\d+")) {
                    String newId = idMap.get(oldId);
                    if (newId == null || (target = model.getDrawableEntity(newId)) == null) {
                        throw new SAXException("Unable to parse dm:constraint. "
                                + newId + " is not recognized.");
                    }

                    startPolylineConstraint((MPolyline) target, atts);
                    break;

                } else if (oldId.matches("S_\\d+")) {
                    String newId = idMap.get(oldId);
                    if (newId == null || (target = model.getDrawableEntity(newId)) == null) {
                        throw new SAXException("Unable to parse dm:constraint. "
                                + oldId + " is not recognized.");
                    }

                    startPathConstraint((MTrace) target, atts);
                    break;

                } else if (oldId.matches("I_\\d+")) {
                    String newId = idMap.get(oldId);
                    if (newId == null || (target = model.getDrawableEntity(newId)) == null) {
                        throw new SAXException("Unable to parse dm:constraint. "
                                + oldId + " is not recognized.");
                    }

                    startImageConstraint((MImage) target, atts);
                    break;

                } else {
                }
//            } else if (atts.getQName(i).equals("dm:constraint-text")) {
//                constraintText = atts.getValue(i);
            } else {
            }
        }
    }

    private void startPointConstraint(PointVar target, Attributes atts) throws SAXException {
        String constraintText = null;
        for (int i = 0; i
                < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:constraint-text")) {
                constraintText = atts.getValue(i);
                if (constraintText == null || constraintText.length() == 0) {
                    throw new SAXException("Unable to parse dm:constraint -- dm:constraint-text missing.");
                }

            }
        }
        if (constraintText == null || constraintText.length() == 0) {
            throw new SAXException("Unable to parse dm:constraint at " + target);
        }

        assert target.getConstraint() == null;
        String line = substituteOldForNewIds(constraintText);
        parser.initialize(line);
        model.beginUpdate();
        try {
            PointVar cp = parser.pointConstraint();
            assert cp != null;
            model.mergeEntities(target, cp);
        } catch (ParseException ex) {
            throw new SAXException("Unable to parse dm:constraint at " + target);
        } catch (ConstraintGraphException ex) {
            throw new SAXException("Unable to apply dm:constraint to " + target);
        }

        model.endUpdate();
    }

    private void startPolylineConstraint(MPolyline target, Attributes atts) throws SAXException {
        String constraintText = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:constraint-text")) {
                constraintText = atts.getValue(i);
                if (constraintText == null || constraintText.length() == 0) {
                    throw new SAXException("Unable to parse dm:constraint -- dm:constraint-text missing.");
                }

            }
        }
        if (constraintText == null || constraintText.length() == 0) {
            throw new SAXException("Unable to parse dm:constraint at " + target);
        }

        Pattern p = Pattern.compile("=\\s*endpoints\\((P_[0-9]+),\\s*(P_[0-9]+)\\)");
        Matcher m = p.matcher(constraintText);
        if (m.matches()) {
            PointVar pv0 = model.getPoint(idMap.get(m.group(1)));
            PointVar pv1 = model.getPoint(idMap.get(m.group(2)));
            if (pv0 == null || pv1 == null) {
                throw new SAXException("2");
            }

            model.beginUpdate();
            try {
                target.setPoints(new double[4]);
                MPolylineConstraint c0 = new MPolylineConstraint();
                DmEntity[] preds0 = new DmEntity[]{pv0, pv1};
                model.addConstraint(c0, preds0, target);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + target);
            } catch (IllegalArgumentException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + target);
            }

            model.endUpdate();
        } else {
            p = Pattern.compile("=\\s*vertices\\(P_\\d+(,\\s*P_\\d+)+\\)");
            m = p.matcher(constraintText);
            if (m.matches()) {
                p = Pattern.compile("P_\\d+");
                m = p.matcher(constraintText);
                int count = 0;
                while (m.find()) {
                    count++;
                }

                if (count < 2) {
                    throw new SAXException("Unable to parse dm:constraint at " + target);
                }

                PointVar[] preds0 = new PointVar[count];
                count =
                        0;
                m.reset();
                while (m.find()) {
                    preds0[count++] = model.getPoint(idMap.get(constraintText.substring(m.start(), m.end())));
                }

                model.beginUpdate();
                try {
                    target.setPoints(new double[2 * count]);
                    MPolylineConstraint c0 = new MPolylineConstraint(count);
                    model.addConstraint(c0, preds0, target);
                } catch (ConstraintGraphException ex) {
                    throw new SAXException("Unable to parse dm:constraint at " + target.getEntityID());
                } catch (IllegalArgumentException ex) {
                    throw new SAXException("Unable to parse dm:constraint at " + target.getEntityID());
                }

                model.endUpdate();
            }

        }
    }

    private void startPathConstraint(MTrace target, Attributes atts) throws SAXException {
        String constraintText = null;
        for (int i = 0; i
                < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:constraint-text")) {
                constraintText = atts.getValue(i);
                if (constraintText == null || constraintText.length() == 0) {
                    throw new SAXException("Unable to parse dm:constraint -- dm:constraint-text missing.");
                }

            }
        }
        if (constraintText == null || constraintText.length() == 0) {
            throw new SAXException("Unable to parse dm:constraint at " + target);
        }

        if (constraintText.matches("=\\s*anchor.+")) {
            AnchorVar anchor = target.getAnchor();
            assert anchor.getConstraint() == null;
            String line = substituteOldForNewIds(constraintText);
            parser.initialize(line);
            model.beginUpdate();
            try {
                AnchorVar av = null;
                av = parser.anchorConstraint();
                assert av != null;
                anchor.initMove();
                anchor.prepareFor((AnchorConstraint) av.getConstraint());
                model.mergeEntities(anchor, av);
            } catch (ParseException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + target);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + target);
            } catch (NoninvertibleTransformException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + target);
            }

            model.endUpdate();
        } else {
            throw new SAXException("Unable to parse dm:constraint at " + target);
        }

    }

    private void startCellConstraint(MCell mCell, Attributes atts) throws SAXException {
        String constraintText = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:constraint-text")) {
                constraintText = atts.getValue(i);
                if (constraintText == null || constraintText.length() == 0) {
                    throw new SAXException("Unable to parse dm:constraint -- dm:constraint-text missing.");
                }

            }
        }
        if (constraintText == null || constraintText.length() == 0) {
            throw new SAXException("Unable to parse dm:constraint at " + mCell);
        }
        String line = substituteOldForNewIds(constraintText);
        parser.initialize(line);
        model.beginUpdate();
        if (constraintText.matches("=\\s*anchor.+")) {
            AnchorVar anchor = mCell.getAnchor();
            assert anchor.getConstraint() == null;
            model.beginUpdate();
            try {
                AnchorVar av = new AnchorVar();
                av = parser.anchorConstraint();
                assert av != null;
                anchor.initMove();
                anchor.prepareFor((AnchorConstraint) av.getConstraint());
                model.mergeEntities(anchor, av);
            } catch (ParseException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mCell);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mCell);
            } catch (NoninvertibleTransformException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mCell);
            }

            model.endUpdate();
        } else {
            CellContent content = ((MCell) mCell).getContent();
            assert content.getConstraint() == null;
            model.beginUpdate();
            try {
                CellContent cc = null;
                cc = parser.cellContent();
                assert cc != null;
                model.mergeEntities(content, cc);
            } catch (ParseException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mCell);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mCell);
            }
            model.endUpdate();
        }
        model.endUpdate();
    }

    private void startImageConstraint(MImage mImage, Attributes atts) throws SAXException {
        String constraintText = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getQName(i).equals("dm:constraint-text")) {
                constraintText = atts.getValue(i);
                if (constraintText == null || constraintText.length() == 0) {
                    throw new SAXException("Unable to parse dm:constraint -- dm:constraint-text missing.");
                }

            }
        }
        if (constraintText == null || constraintText.length() == 0) {
            throw new SAXException("Unable to parse dm:constraint at " + mImage);
        }

        String line = substituteOldForNewIds(constraintText);
        parser.initialize(line);
        model.beginUpdate();
        if (constraintText.matches("=\\s*anchor.+")) {
            AnchorVar anchor = mImage.getAnchor();
            assert anchor.getConstraint() == null;
            model.beginUpdate();
            try {
                AnchorVar av = new AnchorVar();
                av = parser.anchorConstraint();
                assert av != null;
                anchor.initMove();
                anchor.prepareFor((AnchorConstraint) av.getConstraint());
                model.mergeEntities(anchor, av);
            } catch (ParseException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mImage);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mImage);
            } catch (NoninvertibleTransformException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mImage);
            }

            model.endUpdate();
        } else { // clip constraint
            ShapeVar shapeVar = mImage.getShape();
            assert shapeVar.getConstraint() == null;
            model.beginUpdate();
            try {
                ShapeVar sv = null;
                sv = parser.shapeConstraint();
                assert sv != null;
                model.mergeEntities(shapeVar, sv);
            } catch (ParseException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mImage);
            } catch (ConstraintGraphException ex) {
                throw new SAXException("Unable to parse dm:constraint at " + mImage);
            }
            model.endUpdate();
        }
        model.endUpdate();
    }

    private void startUnknown(String uri, String localName, String qName,
            Attributes atts) {
        Debugger.pat("uri = " + uri
                + " localName = " + localName
                + " qName = " + qName);
        for (int i = 0, l = atts.getLength(); i
                < l; i++) {
            Debugger.pat("   startAttribute uri = " + atts.getURI(i)
                    + " localName = " + atts.getLocalName(i)
                    + " qName = " + atts.getQName(i)
                    + " type = " + atts.getType(i)
                    + " value = " + atts.getValue(i));
        }

    }

    private String substituteOldForNewIds(String constraintText) {
        Pattern p = Pattern.compile("[PLCS]_[0-9]+");
        Matcher m = p.matcher(constraintText);
        String[] parts = p.split(constraintText);
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (m.find()) {
            sb.append(parts[index++]);
            String oldId = constraintText.substring(m.start(), m.end());
            String newId = idMap.get(oldId);
            if (newId != null) {
                sb.append(newId);
            } else {
                sb.append(oldId);
            }
        }
        if (index < parts.length) {
            sb.append(parts[index++]);
        }

        return sb.toString();
    }

    private MImage readImageFile(String fileName) {
        //TODO: Set the pathname of file to be the same as the current document

        File file = new File(currentFile.getParentFile(), fileName);
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                MImage mImage = new MImage(image);
                mImage.setFileName(fileName);
                return mImage;
            } else {
                return null;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    private void endText() {
        if (currentCell != null && readingCellContent) {
            String s = textBuffer.toString();
            currentCell.getContent().setSValue(s);
            double cellX = textLeft;
            double cellY = textTop;
            Rectangle2D rect = currentCell.getBounds();
            if (cellDimIncludesX) {
                cellX = rect.getX();
            } else {
                switch (currentCell.getHorizontalAlignment()) {
                    case MCell.CellFormat.LEFT:
                        cellX = textLeft;
                        break;
                    case MCell.CellFormat.CENTER:
                        cellX = (textLeft + textRight - rect.getWidth()) * 0.5;
                        break;
                    case MCell.CellFormat.RIGHT:
                        cellX = textRight - rect.getWidth();
                        break;
                    default:
                        assert false;
                }
            }
            if (cellDimIncludesY) {
                cellY = rect.getY();
            } else {
                switch (currentCell.getVerticalAlignment()) {

                    case MCell.CellFormat.TOP:
                        cellY = textTop;
                        break;
                    case MCell.CellFormat.CENTER:
                        cellY = (textTop + textBottom - rect.getHeight()) * 0.5;
                        break;
                    case MCell.CellFormat.BOTTOM:
                        cellY = textBottom - rect.getHeight();
                        break;
                    default:
                        assert false;
                }
            }
            currentCell.updateBounds(cellX, cellY, rect.getWidth(), rect.getHeight());
            currentCell = null;
        }
        readingCellContent = false;
    }
}
