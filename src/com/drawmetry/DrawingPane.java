package com.drawmetry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.drawmetry.constraints.*;

/**
 *
 * @author Erik
 */
@SuppressWarnings("serial")
class DrawingPane extends JPanel implements
        ChangeListener, DropTargetListener, PropertyChangeListener {

    static final String PROP_FEEDBACK = "feedback";
    static final String PROP_MOUSE_LISTENER_MODE = "mouseListenerMode";
    static final String PROP_FOCUS_ENTITY = "focusEntity";
    static final String PROP_SELECTION = "selection";
    static final String PROP_TRACE_ENABLED = "traceEnabled";
    static final String PROP_STROKE_COLOR = "strokeColor";
    static final String PROP_STROKE = "stroke";
    static final String PROP_CELL_FORMAT = "fontFormat";
    static final String PROP_FILL_COLOR = "fillColor";
    static final String PROP_START_ARROW = "startArrow";
    static final String PROP_END_ARROW = "endArrow";
    static final String PROP_OPACITY = "opacity";
    static final String PROP_COPY_ENABLED = "copyEnabled";
    static final String PROP_PASTE_ENABLED = "pasteEnabled";
    static final String PROP_VIEW_MAP = "viewMap";
    private boolean copyEnabled = false;
    private boolean pasteEnabled = false;
    private static int doubleClickTimeout = 300;
    private DataFlavor[] acceptableDataFlavors = {DataFlavor.javaFileListFlavor};
    private int acceptableDropActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private FocusListener focusListener = new FocusListener() {

        public void focusGained(FocusEvent e) {
            popMouseInputListener();
            model.refreshDrawables();
        }

        public void focusLost(FocusEvent e) {
        }
    };

    /**
     * @return the windingRule
     */
    public int getWindingRule() {
        return windingRule;
    }

    static enum MOUSE_LISTENER_MODE {

        INSERT_CELL, INSERT_LINE, INSERT_POINT, PAN_VIEW, SELECT
    };
    private MOUSE_LISTENER_MODE currentMode;
    private HashMap<MOUSE_LISTENER_MODE, StatefulMouseInputListener> mouseListeners =
            new HashMap<MOUSE_LISTENER_MODE, StatefulMouseInputListener>(7);
    private String feedback;
    private DmEntity focusEntity;
    private DrawingModel model;
    private Selection selection = new Selection();
    private Rectangle selectRect;
    private BasicStroke selectRectStroke =
            new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10.0f, new float[]{5.0f, 3.0f}, 0.0f);
    ////////////////////////////////////////////////////////////////////////////
    // MouseInputListeners
    ////////////////////////////////////////////////////////////////////////////
    private StatefulMouseInputListener currentMouseListener;
    private StatefulMouseInputListener cachedMouseListener;
    private StatefulMouseInputListener nullMouseInputAdapter = new StatefulMouseInputListener() {

        @Override
        protected void initialMousePressed(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void beforeDraggingMouseReleased(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void initialMouseDragged(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void subsequentMouseDragged(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void afterDraggingMouseReleased(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void doubleClickPressed(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void doubleClickReleased(MouseEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void enter() {
        }

        @Override
        protected void exit() {
        }
    };
    private PointVar focusPoint;
    private AnchorVar focusAnchor;
    private CellContent focusCellContent;
    private ShapeVar focusShape;
    private boolean traceOn = false;
    private Parser parser = new Parser();
    private MTrace[] traces;
    private boolean showMarkers;
    private double traceThreshold = 1.0;
    //default cell format:
    private MCell.CellFormat cellFormat = new MCell.CellFormat(
            new Font("Arial", Font.PLAIN, 12),
            Color.BLACK, MCell.CellFormat.CENTER, MCell.CellFormat.CENTER);
    private BasicStroke stroke = StrokeHandler.DEFAULT_STROKE;
    private Color strokeColor = Color.BLACK;
    private Color fillColor = null;
    private double opacity = 1.0;
    private Arrowhead startArrowhead = null;
    private Arrowhead endArrowhead = null;
    private int windingRule = GeneralPath.WIND_NON_ZERO;
    private Object foreground;
    private Object background;
    private BufferedImage backgroundImage;
    private Graphics2D backgroundGraphics;
    private Map<Object, Object> qualityHints = new HashMap<Object, Object>();
    private Map<Object, Object> speedHints = new HashMap<Object, Object>();
    private ViewMap viewMap = new ViewMap(0.0, 0.0, 1.0);
    private AffineTransform modelToViewTransform =
            new AffineTransform(viewMap.zoom, 0, 0, viewMap.zoom, -viewMap.x * viewMap.zoom, -viewMap.y * viewMap.zoom);
    private AffineTransform viewToModelTransform =
            new AffineTransform(1 / viewMap.zoom, 0, 0, 1 / viewMap.zoom, viewMap.x, viewMap.y);

    DrawingPane() {
        super();
        selection = new Selection();
        qualityHints.put(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        speedHints.put(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        mouseListeners.put(MOUSE_LISTENER_MODE.INSERT_CELL, new InsertCellMouseInputHandler(this));
        mouseListeners.put(MOUSE_LISTENER_MODE.INSERT_LINE, new InsertLineMouseInputHandler(this));
        mouseListeners.put(MOUSE_LISTENER_MODE.INSERT_POINT, new InsertPointMouseInputHandler(this));
        mouseListeners.put(MOUSE_LISTENER_MODE.PAN_VIEW, new MoveViewMouseInputHandler(this));
        mouseListeners.put(MOUSE_LISTENER_MODE.SELECT, new SelectMouseInputHandler(this));
        addFocusListener(focusListener);
        addKeyListener(new DirectionalKeyHandler(this));
        setDropTarget(new DropTarget(this, acceptableDropActions, this, true));
    }

    /**
     * @return the doubleClickTimeout
     */
    static int getDoubleClickTimeout() {
        return doubleClickTimeout;
    }

    /**
     * @param aDoubleClickTimeout the doubleClickTimeout to set
     */
    static void setDoubleClickTimeout(int aDoubleClickTimeout) {
        doubleClickTimeout = aDoubleClickTimeout;
    }

    void cut() {
        //TODO: implement cutAction
    }

    void copy() {
        assert model != null && selection != null;
        selection.copy();
        setPasteEnabled(true);
    }

    void paste() {
        assert model != null && selection != null;
        selection.paste();
        model.refreshDrawables();

    }

    public void undo() {
        if (currentMouseListener.isLocked()) {
            return;
        }
        assert getModel() != null;
        getModel().undo();
    }

    public void redo() {
        if (currentMouseListener.isLocked()) {
            return;
        }
        assert getModel() != null;
        getModel().redo();
    }
//    @Override

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == background) {
            validateSelection();
            refreshBackground();
            repaint();
        } else if (e.getSource() == foreground) {
            repaint();
        } else {
            // No other sources that this object is listening to for the moment.
        }
    }

    void delete() {
        if (model != null && !selection.isEmpty()) {
            model.beginUpdate();
            selectionDelete();
            model.endUpdate();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property getters and setters
    ////////////////////////////////////////////////////////////////////////////
    void setCopyEnabled(boolean enabled) {
        boolean oldValue = copyEnabled;
        copyEnabled = enabled;
        firePropertyChange(PROP_COPY_ENABLED, oldValue, copyEnabled);
    }

    void setPasteEnabled(boolean enabled) {
        boolean oldValue = pasteEnabled;
        pasteEnabled = enabled;
        firePropertyChange(PROP_PASTE_ENABLED, oldValue, pasteEnabled);
    }

    void setTraceOnEnabled(boolean enabled) {
        boolean oldValue = traceOn;
        traceOn = enabled;
        firePropertyChange(PROP_TRACE_ENABLED, oldValue, traceOn);
    }

    boolean getTraceOnEnabled() {
        return traceOn;
    }

    void setFeedback(String feedback) {
        String oldValue = this.feedback;
        this.feedback = feedback;
        firePropertyChange(PROP_FEEDBACK, oldValue, feedback);
    }

    void setMode(MOUSE_LISTENER_MODE newMode) {
        MOUSE_LISTENER_MODE oldValue = currentMode;
        currentMode = newMode;
        if (currentMode != oldValue) {
            setMouseInputListener(mouseListeners.get(currentMode));
            firePropertyChange(PROP_MOUSE_LISTENER_MODE, oldValue, currentMode);
        }
    }

    private void setFocusEntity(DmEntity newFocus) { //newValue may be null
        DmEntity oldFocus = focusEntity;
        focusEntity = newFocus;
        firePropertyChange(PROP_FOCUS_ENTITY, oldFocus, focusEntity);
        setFeedback(focusEntity == null ? "" : focusEntity.getEntityID());
    }

    DmEntity getFocusEntity() {
        return focusEntity;
    }

    void setModel(DrawingModel model) {
        if (this.model != null) {
            this.model.removeChangeListener(this);
        }

        if (selection == null) {
            selection = new Selection();
        } else {
            selectionClear();
        }

        selection.setModel(model);
        this.model = model;
        setMode(MOUSE_LISTENER_MODE.SELECT);
        background = model.getBackground();
        foreground = model.getForeground();
        model.addChangeListener(this);
        model.refreshDrawables();
    }

    DrawingModel getModel() {
        return model;
    }

    private void setMouseInputListener(StatefulMouseInputListener mil) {
        if (mil == currentMouseListener) {
            return;
        }

        if (mil == null && currentMouseListener == nullMouseInputAdapter) {
            return;
        }
        if (currentMouseListener != null) {
            currentMouseListener.exit();
        }
        removeMouseListener(currentMouseListener);
        removeMouseMotionListener(currentMouseListener);
        if (mil == null) {
            currentMouseListener = nullMouseInputAdapter;
        } else {
            currentMouseListener = mil;
            currentMouseListener.enter();
        }

        addMouseListener(currentMouseListener);
        addMouseMotionListener(currentMouseListener);

    }

    private void pushMouseInputListener(StatefulMouseInputListener listener) {
        if (currentMouseListener != null) {
            cachedMouseListener = currentMouseListener;
        } else {
        }
        setMouseInputListener(listener);
    }

    private void popMouseInputListener() {
        if (cachedMouseListener != null) {
            setMouseInputListener(cachedMouseListener);
            cachedMouseListener = null;
        } else {
        }
    }

    void resetMode() {
        setMode(MOUSE_LISTENER_MODE.SELECT);
        model.refreshDrawables();
    }

    void setStroke(BasicStroke pen) {
        if (!pen.equals(this.stroke)) {
            updateStroke(pen);
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasStroke()
                        && (pen != null && !pen.equals(de.getStroke())
                        || pen == null && de.getStroke() != null)) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setStroke(de, pen);
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setStrokeColor(Color color) {
        Color cellFontColor = cellFormat.getColor();
        if (color == null && cellFontColor != null
                || color != null && !color.equals(cellFontColor)) {
            updateStrokeColor(color);
//            updateCellFormat(getCellFormat());
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasStroke()
                        && (color == null && de.getStrokeColor() != null
                        || color != null && !color.equals(de.getStrokeColor()))) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setStrokeColor(de, color);
                } else if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    Color fontColor = cell.getFontColor();
                    if (color == null && fontColor != null
                            || color != null && !color.equals(fontColor)) {
                        if (!changed) {
                            model.beginUpdate();
                            changed = true;
                        }
                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                cell.getFont(),
                                color,
                                cell.getHorizontalAlignment(),
                                cell.getVerticalAlignment());
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setFillColor(Color color) {
        if (color == null && fillColor != null
                || color != null && !color.equals(fillColor)) {
            updateFillColor(color);
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasFill()
                        && (color == null && de.getFillColor() != null
                        || color != null && !color.equals(de.getFillColor()))) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setFillColor(de, color);
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setOpacity(double opacity) {
        assert 0 <= opacity && opacity <= 1.00;
        if (opacity != this.opacity) {
            updateOpacity(opacity);
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasFill() && de.getOpacity() != opacity) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setOpacity(de, opacity);
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    Selection getSelection() {
        return selection;
    }

    void selectionDelete() {
        selection.delete();
        selectionClear();
        model.refreshDrawables();
    }

    void selectionClear() {
        selection.clear();
        setFocusEntity(null);
        focusPoint = null;
        focusCellContent = null;
        focusAnchor = null;
        focusShape = null;
//        traceOn = false;
        requestFocusInWindow();
    }

    void selectAll() {
        selection.clear();
        for (Iterator<DrawableEntity> i = model.getDrawableEntityIterator(DrawingModel.BOTTOM_TO_TOP); i.hasNext();) {
            selection.addHit(i.next());
        }
        updateSelection(true);
        model.refreshDrawables();
    }

    void updateSelection(boolean inner) {
        focusPoint = null;
        focusCellContent = null;
        focusAnchor = null;
        focusCellContent = null;
//        traceOn = false;

        if (selection.isEmpty()) {
            setCopyEnabled(false);
            setFocusEntity(null);
            requestFocusInWindow();
            return;
        }
        if (selection.getHits().size() == 1) {
            DmEntity hit = selection.getOneHit();
            if (hit instanceof PointVar) {
                focusPoint = (PointVar) hit;
                setFocusEntity(focusPoint);
            } else {
                if (hit instanceof MCell) {
                    MCell cell = (MCell) hit;
                    if (inner) {
                        focusCellContent = cell.getContent();
                        setFocusEntity(focusCellContent);
                    } else {
                        focusAnchor = cell.getAnchor();
                        setFocusEntity(focusAnchor);
                    }
                    updateCellFormat(cell.getFormat());
                } else if (hit instanceof MImage) {
                    MImage image = (MImage) hit;
                    if (inner) {
                        focusShape = image.getShape();
                        setFocusEntity(focusShape);
                    } else {
                        focusAnchor = image.getAnchor();
                        setFocusEntity(focusAnchor);
                    }
                } else if (hit instanceof MTrace) {
                    MTrace trace = (MTrace) hit;
                    focusAnchor = trace.getAnchor();
                    setFocusEntity(focusAnchor);
                } else {
                    setFocusEntity(hit);
                }
                if (hit instanceof DrawableEntity) {
                    DrawableEntity de = (DrawableEntity) hit;
                    if (de.hasStroke()) {
                        updateStroke(de.getStroke());
                        updateStrokeColor(de.getStrokeColor());
                        updateStartArrow(de.getStartArrowhead());
                        updateEndArrow(de.getEndArrowhead());
                    }
                    if (de.hasFill()) {
                        updateFillColor(de.getFillColor());
                        updateOpacity(de.getOpacity());
                    }
                }
            }
        } else {
            setFocusEntity(null);
        }

        setCopyEnabled(true);
        requestFocusInWindow();
    }

    void setSelectionRectangle(Rectangle rect) {
        selectRect = rect;
    }

    void setShowMarkers(boolean b) {
        showMarkers = b;
        if (model != null) {
            model.refreshDrawables();
        }
    }

    void traceStart(List<DmEntity> includeOnly) {
        if (!traceOn) {
            return;
        }

        int numPointsInSelection = 0;
        for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
            DrawableEntity de = i.next();
            if (de instanceof PointVar && includeOnly.contains(de)) {
                numPointsInSelection++;
            }
        }
        if (numPointsInSelection == 0) {
            traces = null;
            return;
        }


        model.beginUpdate();
        traces = new MTrace[numPointsInSelection];
        int ind = 0;
        for (Iterator<DrawableEntity> i = selection.drawablesIterator();
                i.hasNext();) {
            DrawableEntity de = i.next();
            if (de instanceof PointVar && includeOnly.contains(de)) {
                PointVar pv = (PointVar) de;
                traces[ind] = new MTrace(strokeColor, fillColor, opacity, stroke,
                        new AnchorVar(), startArrowhead, endArrowhead, getWindingRule());
                traces[ind].setEntityID("S_" + model.getTraceNum());
                traces[ind].setThreshold(traceThreshold);
                traces[ind].addInitial(pv);
                model.addDrawableEntity(traces[ind]);
                try {
                    model.addConstraint(new MTraceConstraint(),
                            new DmEntity[]{pv}, traces[ind]);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (ConstraintGraphException ex) {
                    ex.printStackTrace();
                }

                ind++;
            }

        }
        model.endUpdate();
    }

    void traceStop() {
        if (traces != null) {
            for (int i = 0; i
                    < traces.length; i++) {
                model.removeConstraint(traces[i]);
                traces[i].end();
                try {
                    model.addConstraint(
                            new NullConstraint(1),
                            new DmEntity[]{traces[i].getAnchor()}, traces[i]);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (ConstraintGraphException ex) {
                    ex.printStackTrace();
                }

            }
            traces = null;
        }

    }

    Color getStrokeColor() {
        return strokeColor;
    }

    Color getFillColor() {
        return fillColor;
    }

    double getOpacity() {
        return opacity;
    }

    BasicStroke getStroke() {
        return stroke;
    }

    Arrowhead getStartArrowhead() {
        return startArrowhead;
    }

    void setStartArrowhead(Arrowhead a) {
        if (a != startArrowhead) {
            updateStartArrow(a);
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasStroke() && de.getStartArrowhead() != a) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setStartArrowhead(de, a);
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    Arrowhead getEndArrowhead() {
        return endArrowhead;
    }

    void setEndArrowhead(Arrowhead a) {
        if (a != endArrowhead) {
            updateEndArrow(a);
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
                DrawableEntity de = i.next();
                if (de.hasStroke() && de.getEndArrowhead() != a) {
                    if (!changed) {
                        model.beginUpdate();
                        changed = true;
                    }
                    model.setEndArrowhead(de, a);
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void requestTextInput(TextInputListener listener) {
        pushMouseInputListener(TextOutputMouseInputListener.getInstance(listener, this));
    }

    private void parseInput(DrawingModel model, String line) throws ParseException {
        parser.setModel(model);
        parser.initialize(line);
        if (focusPoint != null) {
            parsePointConstraint(focusPoint);
        } else if (focusAnchor != null) {
            parseAnchorConstraint(focusAnchor);
        } else if (focusCellContent != null) {
            parseCellContent(focusCellContent);
        } else if (focusShape != null) {
            parseShapeConstraint(focusShape);
        }

        model.refreshDrawables();
        requestFocusInWindow();

        popMouseInputListener();

    }

    void parseInput(String line) throws ParseException {
        parseInput(model, line);
    }

    private void parsePointConstraint(PointVar fPoint) throws ParseException {
        assert fPoint != null;
        assert model.getUpdateLevel() == 0;
        model.beginUpdate();
        PointVar cp = null;
        try {
            cp = parser.pointConstraint();
            assert cp != null;
            if (fPoint.getConstraint() != null) {
                model.removeConstraint(fPoint);
            }

            try {
                model.mergeEntities(fPoint, cp);
            } catch (ConstraintGraphException ex) {
                throw new ParseException(ex.getMessage(), 0);
            }

            model.endUpdate();
        } catch (ParseException e) {
            model.rollBack();
            throw e;
        }

    }

    private void parseAnchorConstraint(AnchorVar fAnchor) {
        assert fAnchor != null;
        assert model.getUpdateLevel() == 0;
        model.beginUpdate();
        try {
            AnchorVar a = parser.anchorConstraint();
            if (a == null && fAnchor.getConstraint() != null) {
                model.removeConstraint(fAnchor);
                model.endUpdate();
            } else if (a.getConstraint() != null) {
                if (fAnchor.getConstraint() != null) {
                    model.removeConstraint(fAnchor);
                }
                fAnchor.initMove();
                try {
                    //TODO: The next line modifies the anchor and the change
                    // should be propagated.
                    fAnchor.prepareFor((AnchorConstraint) a.getConstraint());
                } catch (NoninvertibleTransformException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                }
                try {
                    model.mergeEntities(fAnchor, a);
                } catch (ConstraintGraphException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                }
                model.endUpdate();
            }

        } catch (ParseException e) {
            setFeedback(e.getMessage());
            model.rollBack();
        }
    }

    private void parseCellContent(CellContent focusCellContent) {
        assert focusCellContent != null;
        assert model.getUpdateLevel() == 0;
        model.beginUpdate();
        try {
            CellContent cc = parser.cellContent();
            if (focusCellContent.getConstraint() != null) {
                model.removeConstraint(focusCellContent);
            }

            if (cc.getConstraint() != null) {
                model.mergeEntities(focusCellContent, cc);
            } else { //cc  has no constraint
                model.getDependents(focusCellContent);
                model.updateCellContents(focusCellContent, cc);
                model.update();
                model.disableUpdate();
            }

            model.endUpdate();
        } catch (Exception e) {
            setFeedback(e.getMessage());
            model.rollBack();
        }

    }

    private void parseShapeConstraint(ShapeVar focusShape) {
        assert focusShape != null;
        assert model.getUpdateLevel() == 0;
        model.beginUpdate();
        try {
            ShapeVar shapeVar = parser.shapeConstraint();
            if (focusShape.getConstraint() != null) {
                model.removeConstraint(focusShape);
            }
            if (shapeVar != null && shapeVar.getConstraint() != null) {
                model.mergeEntities(focusShape, shapeVar);
            }
            model.endUpdate();
        } catch (Exception e) {
            setFeedback(e.getMessage());
            model.rollBack();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // View 
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Sets the the coordinates of the point in the model that corresponds to
     * the origin in the view. In other words, after calling
     * <tt>setViewMap(x, y, zoom)</tt>, a point placed at the top left corner in the
     * view will have model coordinates <tt>(x, y)</tt>.
     */
    private void setViewMap(final double x, final double y, final double zoom) {
        ViewMap oldValue = viewMap;
        viewMap = new ViewMap(x, y, zoom);
        modelToViewTransform.setTransform(
                zoom, 0, 0, zoom, -x * zoom, -y * zoom);
        viewToModelTransform.setTransform(
                1 / zoom, 0, 0, 1 / zoom, x, y);
        firePropertyChange(PROP_VIEW_MAP, oldValue, viewMap);
        refreshBackground();
        repaint();
    }

    void setOrigin(double x, double y) {
        setViewMap(x, y, viewMap.zoom);
    }

    void setZoom(double zoom) {
        if (zoom > 0.0) {
            int wOver2 = this.getWidth() / 2;
            int hOver2 = this.getHeight() / 2;
            double x0 = modelX(wOver2);
            double y0 = modelY(hOver2);
            setViewMap(x0 - wOver2 / zoom, y0 - hOver2 / zoom, zoom); // the origin is
            //set such as to keep the center of the drawing pane centered
        }
    }

    /**
     * @return The zoom applied between model and view.
     */
    double getZoom() {
        return viewMap.zoom;
    }

    /**
     * @return - The model x-coordinate of the upper left corner of the view
     */
    double getOriginX() {
        return viewMap.x;
    }

    /**
     * @return - The model y-coordinate of the upper left corner of the view
     */
    double getOriginY() {
        return viewMap.y;
    }

    /** Transformation from View x-coordinate to Model x-coordinate.
     *@param  - the View x-coordinate.
     *@return - the Model x-coordinate.
     */
    double modelX(int viewX) {
        return viewMap.x + viewX / viewMap.zoom;
    }

    /** Transformation from View y-coordinate to Model y-coordinate
     *@param  - the View y-coordinate.
     *@return - the Model y-coordinate.
     */
    double modelY(int viewY) {
        return viewMap.y + viewY / viewMap.zoom;
    }

    /** Transformation from a width in the View to the corresponding width in
     * the Model.
     *@param  - the View width.
     *@return - the Model width.
     */
    double modelDX(int w) {
//        assert w >= 0;
        return w / viewMap.zoom;
    }

    /** Transformation from a height in the View to the corresponding height in
     * the Model.
     *@param  - the View height.
     *@return - the Model height.
     */
    double modelDY(int h) {
//        assert h >= 0;
        return h / viewMap.zoom;
    }

    static ViewObject getViewInstance(
            DrawableEntity de, AffineTransform at, boolean selected, boolean showMarkers) {
        if (de instanceof PointVar) {
            PointVar p = (PointVar) de;
            return PointMarker.getInstance(p, at, selected, showMarkers && p.isVisible());
        } else if (de instanceof MCell) {
            MCell c = (MCell) de;
            return VCell.getInstance(c, at, selected, showMarkers && c.isVisible());
        } else if (de instanceof MTrace) {
            return VTrace.getInstance((MTrace) de, at, selected);
        } else if (de instanceof MPolyline) {
            return VPolyline.getInstance((MPolyline) de, at, selected);
        } else if (de instanceof MImage) {
            return VImage.getInstance((MImage) de, at, selected);
        } else {
            assert false;
        }

        return null;
    }

    PointVar hitPoint(int x, int y, List<? extends DmEntity> exclude) {
        Iterator<PointVar> points = model.getPointIterator(true);
        while (points.hasNext()) {
            PointVar pVar = points.next();
            if (exclude != null && exclude.contains(pVar)) {
                continue;
            }

            PointMarker pm = PointMarker.getInstance(pVar, modelToViewTransform, false, false);
            if (pm.hits(x, y)) {
                return pVar;
            }

        }
        return null;
    }

    DrawableEntity hitEntity(int x, int y) {
        Iterator<DrawableEntity> drawables = model.getDrawableEntityIterator(DrawingModel.TOP_TO_BOTTOM);
        while (drawables.hasNext()) {
            DrawableEntity de = drawables.next();
            ViewObject vo = getViewInstance(de, modelToViewTransform, false, false);
            if (vo != null && vo.hits(x, y)) {
                return de;
            }

        }
        return null;
    }

    /**
     * If the point (x, y), in view coordinates, hits an MPolyline, 
     * the two adjacent PointVars are returned.
     * @param x view coordinate
     * @param y viw coordinate
     * @param exclude list DmEntities to exclude
     * @return an array of two adjacent PointVars or null
     */
    MPolyline hitLine(int x, int y, List<DmEntity> exclude) {
        Iterator<DrawableEntity> drawables = model.getDrawableEntityIterator(DrawingModel.TOP_TO_BOTTOM);
        while (drawables.hasNext()) {
            DrawableEntity de = drawables.next();
            if (!(de instanceof MPolyline)
                    || exclude != null && exclude.contains(de)) {
                continue;
            }

            VPolyline vp = (VPolyline) getViewInstance(de, modelToViewTransform, false, false);
            if (vp != null && vp.hits(x, y)) {
                return (MPolyline) de;
            }
        }
        return null;
    }

    MPolyline hitLineWithEndpoint(PointVar pv) {
        Iterator<DrawableEntity> drawables = model.getDrawableEntityIterator(DrawingModel.TOP_TO_BOTTOM);
        while (drawables.hasNext()) {
            DrawableEntity de = drawables.next();
            if (de instanceof MPolyline) {
                MPolyline mpl = (MPolyline) de;
                if (!mpl.isClosed()) {
                    MPolylineConstraint mplc = mpl.getConstraint();
                    assert mplc != null;
                    DmEntity[] preds = mplc.getPreds();
                    assert preds.length > 1;
                    if (preds[0] == pv || preds[preds.length - 1] == pv) {
                        return mpl;
                    }
                }
            }
        }
        return null;
    }

    PointVar[] getAdjacentPoints(MPolyline mpl, int x, int y) {
        VPolyline vp = VPolyline.getInstance(mpl, modelToViewTransform, false);
        int i = vp.getAdjacentPoints(x, y);
        if (i >= 0) {
            PointVar[] preds = (PointVar[]) mpl.getConstraint().getPreds();
            if (i < preds.length - 1) {
                return new PointVar[]{preds[i], preds[i + 1]};
            } else if (i == preds.length - 1) {
                return new PointVar[]{preds[i], preds[0]};
            }
        }
        return null;
    }

    int edgeHit(MCell de, int x, int y) {
        VCell vc = (VCell) getViewInstance(de, modelToViewTransform, false, false);

        return vc != null ? vc.edgeHits(x, y) : 0;
    }

    int edgeHit(MImage de, int x, int y) {
        VImage vi = (VImage) getViewInstance(de, modelToViewTransform, false, false);

        return vi != null ? vi.edgeHits(x, y) : 0;
    }

    boolean addRectToSelection(Selection selection, Rectangle rect) {

        Iterator<DrawableEntity> drawables = model.getDrawableEntityIterator(DrawingModel.BOTTOM_TO_TOP);
        boolean added = false;
        while (drawables.hasNext()) {
            DrawableEntity de = drawables.next();
            ViewObject vo =
                    getViewInstance(de, modelToViewTransform, false, false);
            if (vo != null && vo.containedIn(rect)) {
                added = selection.addHit(de);
            }

        }

        return added;
    }

    private void updateCellFormat(MCell.CellFormat newValue) {

        MCell.CellFormat oldValue = cellFormat;
        cellFormat = newValue;
        strokeColor = newValue.getColor();
        firePropertyChange(PROP_CELL_FORMAT, oldValue, newValue);
    }

    private void updateStroke(BasicStroke newValue) {
        if (newValue != null) {
            BasicStroke oldValue = stroke;
            stroke = newValue;
            firePropertyChange(PROP_STROKE, oldValue, newValue);
        }
    }

    private void updateStrokeColor(Color newValue) {
        Color oldValue = strokeColor;
        strokeColor = newValue;
        if (strokeColor == null && cellFormat.getColor() != null
                || strokeColor != null && !strokeColor.equals(cellFormat.getColor())) {
            MCell.CellFormat oldCellFormatValue = cellFormat;
            cellFormat = new MCell.CellFormat(cellFormat.getFont(), strokeColor,
                    cellFormat.getHorizontalAlignment(),
                    cellFormat.getVerticalAlignment());
            firePropertyChange(PROP_CELL_FORMAT, oldCellFormatValue, cellFormat);
        } else {
            firePropertyChange(PROP_STROKE_COLOR, oldValue, newValue);
        }
    }

    private void updateFillColor(Color newValue) {
        Color oldValue = fillColor;
        fillColor = newValue;
        firePropertyChange(PROP_FILL_COLOR, oldValue, newValue);
    }

    private void updateOpacity(double newValue) {
        double oldValue = opacity;
        opacity = newValue;
        if (oldValue != newValue) {
            firePropertyChange(PROP_OPACITY, new Double(oldValue), new Double(newValue));
        }
    }

    private void updateStartArrow(Arrowhead newValue) {
        Arrowhead oldValue = startArrowhead;
        startArrowhead = newValue;
        firePropertyChange(PROP_START_ARROW, oldValue, newValue);
    }

    private void updateEndArrow(Arrowhead newValue) {
        Arrowhead oldValue = endArrowhead;
        endArrowhead = newValue;
        firePropertyChange(PROP_END_ARROW, oldValue, newValue);
    }

    private void validateSelection() {
        boolean changed = false;
        for (Iterator<DrawableEntity> i = selection.getHits().iterator(); i.hasNext();) {
            if (!model.contains(i.next())) {
                i.remove();
                changed = true;
            }
        }
        if (changed) {
            updateSelection(true);
        }
    }

    private Iterator<ViewObject> viewIterator(
            final Iterator<DrawableEntity> modelIterator) {
//        showMarkers = getShowMarkers();
        return new Iterator<ViewObject>() {

            ViewObject nextVO;

//            @Override
            public boolean hasNext() {
                DrawableEntity nextDE;
                while (modelIterator.hasNext()) {
                    nextDE = modelIterator.next();
                    nextVO = DrawingPane.getViewInstance(
                            nextDE, modelToViewTransform, selection.contains(nextDE), showMarkers);
                    if (nextVO != null) {
                        return true;
                    }

                }
                return false;
            }

//            @Override
            public ViewObject next() {
                assert nextVO != null;
                return nextVO;
            }

//            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

    private void refreshBackground() {
        int w = this.getWidth();
        int h = this.getHeight();
        if (h == 0 && w == 0) {
            return;
        }
        if (backgroundImage == null) {
            backgroundImage = (BufferedImage) this.createImage(w, h);
            backgroundGraphics = backgroundImage.createGraphics();
            backgroundGraphics.setRenderingHints(qualityHints);
        } else if (backgroundImage.getWidth() != w || backgroundImage.getHeight() != h) {
            backgroundGraphics.dispose();
            backgroundImage = (BufferedImage) this.createImage(w, h);
            backgroundGraphics = backgroundImage.createGraphics();
            backgroundGraphics.setRenderingHints(qualityHints);
        }
        backgroundGraphics.setColor(Color.WHITE);
        backgroundGraphics.fillRect(0, 0, w, h);
        Iterator<ViewObject> backgroundIterator = viewIterator(model.getBackgroundIterator());
        while (backgroundIterator.hasNext()) {
            backgroundIterator.next().draw(backgroundGraphics);
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(speedHints);
        if (backgroundImage == null
                || backgroundImage.getWidth() != this.getWidth()
                || backgroundImage.getHeight() != this.getHeight()) {
            refreshBackground();
        }
        g2.drawImage(backgroundImage, 0, 0, this);
        Iterator<ViewObject> foregroundIterator = viewIterator(model.getForegroundIterator());
        while (foregroundIterator.hasNext()) {
            foregroundIterator.next().draw(g2);
        }
        if (selectRect != null) {
            g2.setStroke(selectRectStroke);
            g2.setColor(Color.GRAY);
            g2.draw(selectRect);
        }
        g2.dispose();
    }

    void setFontFamily(String fontFamily) {
//        this.fontFamily = fontFamily;
//        if (cellFormat == null || !cellFormat.getFont().getFamily().equals(fontFamily)) {
//            updateCellFormat(getCellFormat());
//        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (DrawableEntity de : selection.getHits()) {
                if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    if (!cell.getFormat().getFont().getFamily().equals(fontFamily)) {
                        if (!changed) {
                            changed = true;
                            model.beginUpdate();
                        }
                        Font newFont = new Font(fontFamily, cell.getFont().getStyle(), cell.getFont().getSize());
                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                newFont,
                                cell.getFontColor(),
                                cell.getHorizontalAlignment(),
                                cell.getVerticalAlignment());
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setFontSize(int size) {
//        this.fontSize = size;
//        if (cellFormat == null || cellFormat.getFont().getSize() != size) {
//            updateCellFormat(getCellFormat());
//        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (DrawableEntity de : selection.getHits()) {
                if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    if (cell.getFormat().getFont().getSize() != size) {
                        if (!changed) {
                            model.beginUpdate();
                            changed = true;
                        }
                        Font newFont = new Font(cell.getFont().getFamily(), cell.getFont().getStyle(), size);
                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                newFont,
                                cell.getFontColor(),
                                cell.getHorizontalAlignment(),
                                cell.getVerticalAlignment());
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setfontStyle(int style) {
//        this.fontStyle = style;
//        if (cellFormat == null || cellFormat.getFont().getStyle() != style) {
//            updateCellFormat(getCellFormat());
//        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (DrawableEntity de : selection.getHits()) {
                if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    if (cell.getFormat().getFont().getStyle() != style) {
                        if (!changed) {
                            changed = true;
                            model.beginUpdate();
                        }
                        Font newFont = new Font(cell.getFont().getFamily(), style, cell.getFont().getSize());
                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                newFont,
                                cell.getFontColor(),
                                cell.getHorizontalAlignment(),
                                cell.getVerticalAlignment());
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setHorizontalAlignment(String hAlign) {
        int horizontalAlignment = MCell.CellFormat.LEFT;
        if (hAlign.equals("left")) {
            horizontalAlignment = MCell.CellFormat.LEFT;
        } else if (hAlign.equals("center")) {
            horizontalAlignment = MCell.CellFormat.CENTER;
        } else if (hAlign.equals("right")) {
            horizontalAlignment = MCell.CellFormat.RIGHT;
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (DrawableEntity de : selection.getHits()) {
                if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    if (cell.getHorizontalAlignment() != horizontalAlignment) {
                        if (!changed) {
                            model.beginUpdate();
                            changed = true;
                        }

                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                cell.getFont(),
                                cell.getFontColor(),
                                horizontalAlignment,
                                cell.getVerticalAlignment());
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    void setVerticalAlignment(String hAlign) {
        int verticalAlignment = MCell.CellFormat.TOP;
        if (hAlign.equals("top")) {
            verticalAlignment = MCell.CellFormat.TOP;
        } else if (hAlign.equals("center")) {
            verticalAlignment = MCell.CellFormat.CENTER;
        } else if (hAlign.equals("bottom")) {
            verticalAlignment = MCell.CellFormat.BOTTOM;
        }
        if (!selection.getHits().isEmpty()) {
            boolean changed = false;
            for (DrawableEntity de : selection.getHits()) {
                if (de instanceof MCell) {
                    MCell cell = (MCell) de;
                    if (cell.getVerticalAlignment() != verticalAlignment) {
                        if (!changed) {
                            model.beginUpdate();
                            changed = true;
                        }

                        MCell.CellFormat newFormat = new MCell.CellFormat(
                                cell.getFont(),
                                cell.getFontColor(),
                                cell.getHorizontalAlignment(),
                                verticalAlignment);
                        model.setCellFormat(cell, newFormat);
                    }
                }
            }
            if (changed) {
                model.endUpdate();
                model.refreshDrawables();
            }
        }
    }

    MCell.CellFormat getCellFormat() {
        return cellFormat;
    }

//    private MCell.CellFormat getCellFormat() {
//        return cellFormat == null? getDefaultCellFormat() : cellFormat;
//    }
    double getTraceThreshold() {
        return traceThreshold;
    }

    void setTraceThreshold(double d) {
        traceThreshold = d;
    }

    void sendToBack() {
        if (selection.getHits() != null && selection.getHits().size()
                > 0) {
            Iterator<DrawableEntity> i = selection.drawablesIterator();
            model.beginUpdate();
            while (i.hasNext()) {
                model.sendToBack((DrawableEntity) i.next());
            }


            model.endUpdate();
            model.refreshDrawables();
            selectionClear();
        }
    }

    void sendToFront() {
        if (selection.getHits() != null && selection.getHits().size()
                > 0) {
            Iterator<DrawableEntity> i = selection.drawablesIterator();
            model.beginUpdate();
            while (i.hasNext()) {
                model.sendToFront((DrawableEntity) i.next());
            }


            model.endUpdate();
            model.refreshDrawables();
            selectionClear();
        }
    }
    /*
     *
     * DROP SUPPORT
     *
     */

    public void dragEnter(DropTargetDragEvent e) {
        if (!isDragOk(e)) {
            e.rejectDrag();
            return;
        }
        e.acceptDrag(acceptableDropActions);
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragOver(DropTargetDragEvent e) {
        if (isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }
        e.acceptDrag(acceptableDropActions);
    }

    public void dropActionChanged(DropTargetDragEvent e) {
        if (!isDragOk(e)) {
            e.rejectDrag();
            return;
        }
        e.acceptDrag(acceptableDropActions);
    }

    public void drop(DropTargetDropEvent e) {
        int dropAction = isDropOk(e);
        if (dropAction == DnDConstants.ACTION_NONE) {
            e.rejectDrop();
            return;
        }
        e.acceptDrop(dropAction);
        List fileList = null;
        try {
            Object data = e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (data instanceof List) {
                fileList = (List) data;
            }
            if (fileList == null || fileList.size() != 1) {
                return;
            }
            Object file = fileList.get(0);
            if (file instanceof File) {
                Point insertionPoint = e.getLocation();
                insertFile((File) file, insertionPoint);
            }
        } catch (UnsupportedFlavorException ex) {
            assert false;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isDragOk(DropTargetDragEvent e) {
        boolean flavorOK = false;
        for (DataFlavor flavor : acceptableDataFlavors) {
            if (e.isDataFlavorSupported(flavor)) {
                flavorOK = true;
                break;
            }
        }
        if (flavorOK && ((acceptableDropActions & e.getDropAction()) != 0)) {
            return true;
        }
        return false;
    }

    private int isDropOk(DropTargetDropEvent e) {
        boolean flavorOK = false;
        for (DataFlavor flavor : acceptableDataFlavors) {
            if (e.isDataFlavorSupported(flavor)) {
                flavorOK = true;
                break;
            }
        }
        if (flavorOK) {
            return acceptableDropActions & e.getDropAction();
        } else {
            return 0;
        }
    }

    private void insertFile(File file, Point dropPoint)
            throws IOException {
        double z = getZoom();
        double modelDropX = getOriginX() + dropPoint.x / z;
        double modelDropY = getOriginY() + dropPoint.y / z;
        String fileName = file.getName();
        if (fileName.toLowerCase().matches(".+\\.png")
                || fileName.toLowerCase().matches(".+\\.jpg")) {
            BufferedImage image = ImageIO.read(file);
            try {
                MImage mImage = new MImage(image);
                mImage.setFileName(fileName);
                mImage.setEntityID("I_" + model.getImageNum());
                model.addDrawableEntity(mImage);
                try {
                    model.addConstraint(new NullConstraint(2), new DmEntity[]{new AnchorVar(), new ShapeVar()}, mImage);
                    mImage.getAnchor().setCtf(1.0, 0.0, 0.0, 1.0, modelDropX, modelDropY);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (ConstraintGraphException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            model.refreshDrawables();
        } else if (fileName.toLowerCase().matches(".+\\.svg")) {
            try {
                XMLReader xmlReader;
                xmlReader = XMLReaderFactory.createXMLReader();
                DmReader handler = new DmReader(model, file, modelDropX, modelDropY);
                xmlReader.setContentHandler(handler);
                xmlReader.setErrorHandler(handler);
                model.beginUpdate();
                try {
                    xmlReader.parse(new InputSource(new FileReader(file)));
                } finally {
                    model.endUpdate();
                }
            } catch (SAXException ex) {
                ex.printStackTrace();
            }
            model.refreshDrawables();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof FileContext
                && evt.getPropertyName().equals(FileContext.PROP_CURRENT_DOCUMENT)) {
            FileContext source = (FileContext) evt.getSource();
            setModel(source.getModel());
            setViewMap(source.getOriginX(), source.getOriginY(), source.getZoom());
            selectionClear();
            model.refreshDrawables();
        }
    }
}
