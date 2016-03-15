/*
 * FileContext.java
 *
 * Created on June 14, 2006, 4:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and openFile the template in the editor.
 */
package com.drawmetry;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.drawmetry.constraints.NullConstraint;

/**
 *
 * @author Erik
 */
class FileContext implements PropertyChangeListener {

    public static final String PROP_SAVE_ENABLED = "saveEnabled";
    public static final String PROP_UNDO_ENABLED = "undoEnabled";
    public static final String PROP_REDO_ENABLED = "redoEnabled";
    public static final String PROP_CURRENT_DOCUMENT = "currentDocument";
    private static final String fileSaveService = "javax.jnlp.FileSaveService";
    private static final String fileOpenService = "javax.jnlp.FileOpenService";
    private static final String printService = "javax.jnlp.PrintService";
    private boolean secure = true;
    private boolean saveEnabled = false;
    private boolean undoEnabled = false;
    private boolean redoEnabled = false;
    private String currentDirectoryPath;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private PageFormat pageFormat;

    private static class Document {

        private String name;
        private File file;
        private boolean saved;
        private boolean undoEnabled = false;
        private boolean redoEnabled = false;
        private DrawingModel model;
        private Point2D.Double origin;
        private double zoom;

        private Document() {
            super();
        }

        private Document(String name, DrawingModel model) {
            super();
            this.name = name;
            this.saved = true;
            this.model = model;
        }

        String getName() {
            return name;
        }

        DrawingModel getModel() {
            return model;
        }
    }
    private DmFrame frame;
    private List<Document> docs = new ArrayList<Document>();
    private Document currentDoc;
    private int n;

    /**
     * Creates a new instance of FileContext
     */
    private FileContext(boolean secure) {
        super();
        this.secure = secure;
    }

    static FileContext getFileContext(DmFrame parent, boolean secure) {
        FileContext fc = new FileContext(secure);
        fc.frame = parent;
        return fc;
    }

    private String[] getNames() {
        String[] names = new String[docs.size()];
        int i = 0;
        for (Document e : docs) {
            names[i++] = e.getName();
        }
        return names;
    }

    private void addDocument() {
        String[] oldNames = getNames();
        currentDoc = new Document("Document" + n++, new DrawingModel());
        docs.add(0, currentDoc);
        currentDoc.model.addPropertyChangeListener(this);
        currentDoc.origin = new Point2D.Double(0.0, 0.0);
        currentDoc.zoom = 1.0;
        resetCurrentDocument(oldNames, getNames());
    }

    private void saveCurrent() throws FileNotFoundException, IOException {
        assert currentDoc != null;

        File file = currentDoc.file;
        if (file == null) {
            throw new FileNotFoundException();
        }
        DmWriter out = null;
        try {
            OutputStream outStream = new FileOutputStream(file);
            try {
                new DmWriter(outStream).writeModel(currentDoc.model);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(FileContext.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(FileContext.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XMLStreamException ex) {
                System.out.println("XMLStreamException");
                Logger.getLogger(FileContext.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentDoc.saved = true;
            setSaveEnabled(false);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private boolean saveCurrentAs() throws IOException {
        assert currentDoc != null;
        DmWriter writer = null;
        try {
            JFileChooser dialog = new JFileChooser(currentDirectoryPath);
            int res = dialog.showSaveDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = dialog.getSelectedFile();
                writer = new DmWriter(new FileOutputStream(file));
                writer.writeModel(currentDoc.getModel());
                String[] oldNames = getNames();
                currentDoc.file = file;
                currentDoc.name = file.getName();
                currentDoc.saved = true;
                currentDirectoryPath = file.getAbsolutePath();
                resetCurrentDocument(oldNames, getNames());
                setSaveEnabled(false);
            } else {
                JOptionPane.showMessageDialog(frame, "File not saved.", "Saving", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            return currentDoc.saved;
        }
    }

    /**
     *
     * @throws FileNotSavedException
     */
    private void closeCurrent() throws FileNotSavedException {
        assert currentDoc != null;
        if (currentDoc.saved) {
            closeCurrentNoSave();
        } else {
            throw new FileNotSavedException(currentDoc.name);
        }
    }

    private void closeCurrentNoSave() {
        assert currentDoc != null;
        currentDoc.model.removePropertyChangeListener(this);
        String[] oldNames = getNames();
        docs.remove(currentDoc);
        if (docs.isEmpty()) {
            addDocument();
        } else {
            currentDoc = docs.get(0);
        }
        resetCurrentDocument(oldNames, getNames());
    }

    Iterator<Document> getDocumentIterator() {
        return docs.iterator();
    }

    boolean exit() {
        boolean notCancelled = true;
        while (docs.size() > 1 && notCancelled) {
            notCancelled = closeFile();
        }
        if (notCancelled) {
            notCancelled = closeFile();
        }
        return notCancelled;
    }

//    void forceExit() {
//        while (docs.size() > 1) {
//            closeFileNoCancel();
//        }
//        closeFileNoCancel();
//    }
    DrawingModel getModel() {
        return currentDoc.getModel();
    }

    double getZoom() {
        if (currentDoc != null) {
            return currentDoc.zoom;
        } else {
            return 1.0;
        }
    }

    double getOriginX() {
        if (currentDoc != null && currentDoc.origin != null) {
            return currentDoc.origin.x;
        } else {
            currentDoc.origin = new Point2D.Double(0.0, 0.0);
            return 0.0;
        }
    }

    double getOriginY() {
        if (currentDoc != null && currentDoc.origin != null) {
            return currentDoc.origin.y;
        } else {
            currentDoc.origin = new Point2D.Double(0.0, 0.0);
            return 0.0;
        }
    }

    void setViewMap(double zoom, double originX, double originY) {
        if (currentDoc != null) {
            currentDoc.zoom = zoom;
            if (currentDoc.origin != null) {
                currentDoc.origin.setLocation(originX, originY);
            } else {
                currentDoc.origin = new Point2D.Double(originX, originY);
            }
        }
    }

//    private void closeFileNoCancel() {
//        try {
//            closeCurrent();
//        } catch (FileNotSavedException ex) {
//            int retVal = JOptionPane.showConfirmDialog(frame,
//                    "Save " + ex.getMessage() + " before closing?",
//                    "Closing " + ex.getMessage(), JOptionPane.YES_NO_OPTION);
//            if (retVal == JOptionPane.YES_OPTION) {
//                try {
//                    saveCurrent();
//                    closeCurrentNoSave();
//                } catch (FileNotFoundException ex2) {
//                    try {
//                        saveCurrentAs();
//                    } catch (IOException ex1) {
//                        JOptionPane.showMessageDialog(frame, ex1.getMessage());
//                    }
//                } catch (IOException ex2) {
//                    JOptionPane.showMessageDialog(frame, ex2.getMessage());
//                }
//            }
//            closeCurrentNoSave();
//        }
//    }
    private void resetCurrentDocument(String[] oldNames, String[] newNames) {
        propertyChangeSupport.firePropertyChange(PROP_CURRENT_DOCUMENT, oldNames, newNames);
        setSaveEnabled(!currentDoc.saved);
        setUndoEnabled(currentDoc.undoEnabled);
        setRedoEnabled(currentDoc.redoEnabled);
    }

    private void setSaveEnabled(boolean b) {
        boolean oldValue = saveEnabled;
        saveEnabled = b;
        propertyChangeSupport.firePropertyChange(PROP_SAVE_ENABLED, oldValue, saveEnabled);

    }

    private void setUndoEnabled(boolean b) {
        boolean oldValue = undoEnabled;
        undoEnabled = b;
        propertyChangeSupport.firePropertyChange(PROP_UNDO_ENABLED, oldValue, undoEnabled);
    }

    private void setRedoEnabled(boolean b) {
        boolean oldValue = redoEnabled;
        redoEnabled = b;
        propertyChangeSupport.firePropertyChange(PROP_REDO_ENABLED, oldValue, redoEnabled);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

//    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof DrawingModel) {
            DrawingModel source = (DrawingModel) evt.getSource();
            for (Document doc : docs) {
                if (doc.model == source) {
                    if (evt.getPropertyName().equals(DrawingModel.PROP_SAVE_ENABLED)) {
                        doc.saved = !((Boolean) evt.getNewValue()).booleanValue();
                        setSaveEnabled(!currentDoc.saved);
                    } else if (evt.getPropertyName().equals(DrawingModel.PROP_UNDO_ENABLED)) {
                        doc.undoEnabled = ((Boolean) evt.getNewValue()).booleanValue();
                        setUndoEnabled(currentDoc.undoEnabled);
                    } else if (evt.getPropertyName().equals(DrawingModel.PROP_REDO_ENABLED)) {
                        doc.redoEnabled = ((Boolean) evt.getNewValue()).booleanValue();
                        setRedoEnabled(currentDoc.redoEnabled);
                    }
                    break;
                }
            }
        } else if (evt.getSource() instanceof DrawingPane) {
            DrawingPane source = (DrawingPane) evt.getSource();
            if (evt.getPropertyName().equals(DrawingPane.PROP_VIEW_MAP)) {
                setViewMap(source.getZoom(), source.getOriginX(), source.getOriginY());
            }
        }
    }

    void selectFile(int index) {
        if (index == 0) {
            return;
        }
        String[] oldNames = getNames();
        Document doc = docs.remove(index);
        docs.add(0, doc);
        currentDoc = doc;
        resetCurrentDocument(oldNames, getNames());

    }

    void newFile() {
        setViewMap(1.0, 0.0, 0.0);
        addDocument();
    }

    void openFile() {
        normalOpenFile();
//        if (secure) {
//            secureOpenFile();
//        } else {
//            normalOpenFile();
//        }
    }

//    private void secureOpenFile() {
//
//        FileOpenService fos;
//
//        try {
//            fos = (FileOpenService) ServiceManager.lookup(fileOpenService);
//        } catch (UnavailableServiceException e) {
//            JOptionPane.showMessageDialog(frame, "Service not available", "Open", JOptionPane.ERROR_MESSAGE);
//            fos = null;
//        }
//
//        if (fos != null) {
//            // ask user to select a file through this service
//            FileContents fc = null;
//            String name = null;
//            try {
//                fc = fos.openFileDialog(currentDirectoryPath, null);
//                name = fc.getName();
//            } catch (IOException ex) {
//                JOptionPane.showMessageDialog(frame, ex.getMessage());
//            }
//            if (fc != null && name != null) {
//                String[] oldNames = getNames();
//                boolean fileOpen = false;
//                for (Document doc : docs) {
//                    if (doc.getName().equals(name)) {
//                        fileOpen = true;
//                        currentDoc = doc;
//                        docs.remove(doc);
//                        docs.add(0, doc);
//                        resetCurrentDocument(oldNames, getNames());
//                        break;
//                    }
//                }
//                if (!fileOpen) {
//                    final Document doc = new Document();
//                    doc.name = name;
//                    doc.saved = true;
//                    DrawingModel model = new DrawingModel();
//                    doc.model = model;
//
//                    try {
//                        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//                        DmReader dmReader = new DmReader(doc.model);
//                        xmlReader.setContentHandler(dmReader);
//                        xmlReader.setErrorHandler(dmReader);
//                        xmlReader.parse(new InputSource(fc.getInputStream()));
//                        doc.origin = new Point2D.Double();
//                        doc.zoom = 1.0;
//                        docs.add(0, doc);
//                        currentDoc = doc;
//                        doc.model.discardAllEdits();
//                        currentDoc.model.addPropertyChangeListener(this);
//                        resetCurrentDocument(oldNames, getNames());
//                    } catch (SAXException e) {
//                        JOptionPane.showMessageDialog(frame, "Error reading file", "Open", JOptionPane.ERROR_MESSAGE);
//                    } catch (IOException e) {
//                        JOptionPane.showMessageDialog(frame, e.getMessage());
//                    }
//                }
//            }
//        }
//    }
    private void normalOpenFile() {
        File file = null;
        JFileChooser dialog = new JFileChooser(currentDirectoryPath);
        if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            file = dialog.getSelectedFile();
            currentDirectoryPath = file.getAbsolutePath();
        }
        if (file != null) {
            String[] oldNames = getNames();
            boolean fileOpen = false;
            for (Document doc : docs) {
                if (doc.getName().equals(file.getName())) {
                    fileOpen = true;
                    currentDoc = doc;
                    docs.remove(doc);
                    docs.add(0, doc);
                    resetCurrentDocument(oldNames, getNames());
                    break;
                }
            }
            if (!fileOpen) {
                final Document doc = new Document();
                doc.name = file.getName();
                doc.file = file;
                doc.saved = true;
                DrawingModel model = new DrawingModel();
                doc.model = model;

                try {
                    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                    DmReader dmReader = new DmReader(doc.model, doc.file);
                    xmlReader.setContentHandler(dmReader);
                    xmlReader.setErrorHandler(dmReader);
                    xmlReader.parse(new InputSource(new FileInputStream(file)));
                    doc.origin = new Point2D.Double();
                    doc.zoom = 1.0;
                    docs.add(0, doc);
                    currentDoc = doc;
                    doc.model.discardAllEdits();
                    currentDoc.model.addPropertyChangeListener(this);
                    resetCurrentDocument(oldNames, getNames());
                } catch (SAXException e) {
                    JOptionPane.showMessageDialog(frame, "Error reading file", "Open", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, e.getMessage());
                }
            }
        }
    }

    void includeFile() {
        normalIncludeFile();
//        if (secure) {
//            secureIncludeFile();
//        } else {
//            normalIncludeFile();
//        }
    }

//    private void secureIncludeFile() {
//        double z = getZoom();
//        double modelDropX = getOriginX() + 10 / z;
//        double modelDropY = getOriginY() + 10 / z;
//        boolean startedUpdate = false;
//
//        FileOpenService fos = null;
//        FileContents fc = null;
//        try {
//            fos = (FileOpenService) ServiceManager.lookup(fileOpenService);
//            if (fos != null) {
//                // ask user to select a file through this service
//                fc = fos.openFileDialog(currentDirectoryPath, null);
//            }
//        } catch (UnavailableServiceException e) {
//            JOptionPane.showMessageDialog(frame, "Service not available", "Include", JOptionPane.ERROR_MESSAGE);
//        } catch (IOException ex) {
//            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Include", JOptionPane.ERROR_MESSAGE);
//        }
//        if (fc != null) {
//            try {
//                if (fc.getName().matches(".+\\.svg")) {
//                    DrawingModel model = currentDoc.model;
//                    try {
//                        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//                        DmReader handler = new DmReader(model, null, modelDropX, modelDropY);
//                        xmlReader.setContentHandler(handler);
//                        xmlReader.setErrorHandler(handler);
//                        model.beginUpdate();
//                        startedUpdate = true;
//                        xmlReader.parse(new InputSource(fc.getInputStream()));
//                        model.endUpdate();
//                    } catch (SAXException ex) {
//                        JOptionPane.showMessageDialog(frame, "Error reading file", "Include", JOptionPane.ERROR_MESSAGE);
//                        if (startedUpdate) {
//                            model.rollBack();
//                        }
//                    } catch (IOException ex) {
//                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Include", JOptionPane.ERROR_MESSAGE);
//                        if (startedUpdate) {
//                            model.rollBack();
//                        }
//                    }
//                    model.refreshDrawables();
//                } else if (fc.getName().matches(".+\\.png")) {
//                    DrawingModel model = currentDoc.model;
//                    BufferedImage image = ImageIO.read(fc.getInputStream());
//                    try {
//                        MImage mImage = new MImage(image);
//                        mImage.setEntityID("I_" + model.getImageNum());
//                        model.addDrawableEntity(mImage);
//                        try {
//                            model.addConstraint(new NullConstraint(2), new DmEntity[]{new AnchorVar(), new ShapeVar()}, mImage);
//                            mImage.getAnchor().setCtf(1.0, 0.0, 0.0, 1.0, modelDropX, modelDropY);
//                        } catch (IllegalArgumentException ex) {
//                            ex.printStackTrace();
//                        } catch (ConstraintGraphException ex) {
//                            ex.printStackTrace();
//                        }
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                    model.refreshDrawables();
//
//                }
//            } catch (IOException ex) {
//                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Include", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//
//    }
    private void normalIncludeFile() {
        double z = getZoom();
        double modelDropX = getOriginX() + 10 / z;
        double modelDropY = getOriginY() + 10 / z;
        boolean startedUpdate = false;

        File file = null;
        JFileChooser dialog = new JFileChooser(currentDirectoryPath);
        if (dialog.showDialog(frame, "Include") == JFileChooser.APPROVE_OPTION) {
            file = dialog.getSelectedFile();
        }

        if (file != null && file.getName().matches(".+\\.svg")) {
            DrawingModel model = currentDoc.model;
            try {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                DmReader handler = new DmReader(model, file, modelDropX, modelDropY);
                xmlReader.setContentHandler(handler);
                xmlReader.setErrorHandler(handler);
                model.beginUpdate();
                startedUpdate = true;
                xmlReader.parse(new InputSource(new FileInputStream(file)));
                model.endUpdate();
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(frame, "Error reading file", "Include", JOptionPane.ERROR_MESSAGE);
                if (startedUpdate) {
                    model.rollBack();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Include", JOptionPane.ERROR_MESSAGE);
                if (startedUpdate) {
                    model.rollBack();
                }
            }
            model.refreshDrawables();
        }
    }

    void saveFile() {
        try {
            saveCurrent();
        } catch (FileNotFoundException exc) {
            saveFileAs();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }

    }

    void saveFileAs() {
        try {
            saveCurrentAs();
//            setViewMap(zoom, originX, originY);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }

    }

    void exportAs() {
        normalExportAs();
//        if (secure) {
//            secureExportAs();
//        } else {
//            normalExportAs();
//        }
    }

//    private void secureExportAs() {
//        String ext = "png";
//        String[] extensions = new String[]{ext};
//        try {
//            FileSaveService fss = (FileSaveService) ServiceManager.lookup(fileSaveService);
//            BufferedImage image = getImage(ext);
//            if (image != null) {
//                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//                ImageIO.write(image, "png", outStream);
//                ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
//                fss.saveFileDialog(null, extensions, inStream, null);
//            }
//        } catch (UnavailableServiceException ex) {
//            JOptionPane.showMessageDialog(frame, "Service unavailable", "Export", JOptionPane.ERROR_MESSAGE);
//        } catch (IOException ex) {
//            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Export", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    private void normalExportAs() {
        File file = null;
        JFileChooser dialog = new JFileChooser(currentDirectoryPath);
        dialog.setFileFilter(new ImageFileFilter());
        if (dialog.showDialog(frame, "Export") == JFileChooser.APPROVE_OPTION) {
            file = dialog.getSelectedFile();
        }
        if (file != null) {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            BufferedImage image = getImage(ext);
            if (image != null) {
                try {
                    ImageIO.write(image, ext, new FileOutputStream(file));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Export", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    void print() {
        normalPrint();
//        if (secure) {
//            securePrint();
//        } else {
//            normalPrint();
//        }
    }

//    private void securePrint() {
//        PrintService ps = null;
//        try {
//            ps = (PrintService) ServiceManager.lookup(printService);
//        } catch (UnavailableServiceException ex) {
//            JOptionPane.showMessageDialog(frame, "Service unavailable", "Print", JOptionPane.ERROR_MESSAGE);
//        }
//        if (ps != null) {
//            if (pageFormat == null) {
//                pageFormat = ps.getDefaultPage();
//            }
//            ps.print(getPrintView());
//        }
//    }
    private void normalPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        if (pageFormat == null) {
            pageFormat = new PageFormat();
        }
        job.setPrintable(getPrintView(), pageFormat);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    PrintView getPrintView() {
        return new PrintView(pageFormat, getModel());
    }

    void pageSetup() {
        PageFormat defaultPageFormat = new PageFormat();
        PrinterJob pj = PrinterJob.getPrinterJob();
        pageFormat = pj.pageDialog(defaultPageFormat);
    }

    /**
     * Closes the current file.
     *
     * @return true if the current file closed successfully
     */
    boolean closeFile() {
        try {
            closeCurrent();
            return true;
        } catch (FileNotSavedException ex) {
            int retVal = JOptionPane.showConfirmDialog(frame,
                    "Save " + ex.getMessage() + " before closing?");
            if (retVal == JOptionPane.YES_OPTION) {
                try {
                    saveCurrent();
                    closeCurrentNoSave();
                    return true;
                } catch (FileNotFoundException e) {
                    try {
                        if (saveCurrentAs()) {
                            closeCurrentNoSave();
                            return true;
                        } else {
                            return false;
                        }
                    } catch (IOException e2) {
                        JOptionPane.showMessageDialog(frame, e2.getMessage());
                        return false;
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, e.getMessage());
                    return false;
                }
            } else if (retVal == JOptionPane.NO_OPTION) {
                closeCurrentNoSave();
                return true;
            } else if (retVal == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            return false;
        }

    }

    private BufferedImage getImage(String extension) {
        DrawingModel model = currentDoc.getModel();
        Rectangle2D bounds = model.getBounds();
        AffineTransform at = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY());
        int w = (int) Math.ceil(bounds.getWidth());
        int h = (int) Math.ceil(bounds.getHeight());
        BufferedImage bufImage = null;
        if (extension.equals("jpeg")
                || extension.equals("jpg")
                || extension.equals("gif")) {
            bufImage = new BufferedImage(w, h, BufferedImage.OPAQUE);
            Graphics2D g2 = bufImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            for (Iterator<DrawableEntity> iterator = model.getBackgroundIterator();
                    iterator.hasNext();) {
                DrawingPane.getViewInstance(iterator.next(), at, false, false).draw(g2);
            }
            g2.dispose();
        } else if (extension.equals("png")) {
            bufImage = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
            Graphics2D g2 = bufImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Iterator<DrawableEntity> iterator = model.getBackgroundIterator();
                    iterator.hasNext();) {
                DrawingPane.getViewInstance(iterator.next(), at, false, false).draw(g2);
            }
            g2.dispose();
        }
        return bufImage;
    }
}
