/*
 * Parser.java
 *
 * Created on August 25, 2004, 3:23 AM
 */
package com.drawmetry;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;

import com.drawmetry.constraints.*;

/**
 *
 * @author  Erik Colban
 */
public class Parser {

    private static enum Token {

        ANCHOR,
        AND,
        ANGLE,
        CELL,
        CENTER,
        CLIP,
        COLINEAR,
        COMMA,
        DISTANCE,
        DIV,
        EOF,
        EOL,
        EQ,
        FUNC,
        EQUIDISTANT,
        HCENTER,
        HDISTANCE,
        HORIZONTAL,
        LINE,
        LPAR,
        MINUS,
        MOD,
        MULT,
        NEGNUM,
        NUM,
        OTHER,
        PARALLEL,
        PARALLELOGRAM,
        PERPENDICULAR,
        PI,
        PLUS,
        POINT,
        POWER,
        PROJECT,
        ROTATE,
        RPAR,
        SIGN,
        SLOPE,
        TRACE,
        VCENTER,
        VDISTANCE,
        VERTICAL,
        VERTICES,
        WEIGHT,
        WORD
    }
    private static String[] ANCHOR_WORDS = {"anchor", "acr", "anc", "anch", "achr"};
    private static String[] AND_WORDS = {"and"};
    private static String[] ANGLE_WORDS = {"angle", "ang", "angl"};
    private static String[] CENTER_WORDS = {"center", "centre", "ctr"};
    private static String[] CLIP_WORDS = {"clip"};
    private static String[] COLINEAR_WORDS = {"colinear", "col", "cln", "align", "algn"};
    private static String[] DISTANCE_WORDS = {"distance", "dist", "dst"};
    private static String[] EQUIDISTANT_WORDS = {"equidistant", "edist", "edst", "equidist", "equidst"};
    private static String[] HCENTER_WORDS = {"hcenter", "hcentre", "hctr"};
    private static String[] HDISTANCE_WORDS = {"hdistance", "hdist", "hdst"};
    private static String[] HORIZONTAL_WORDS = {"horizontal", "hz", "hor"};
    private static String[] PARALLEL_WORDS = {"parallel", "par", "pll"};
    private static String[] PARALLELOGRAM_WORDS = {"parallelogram", "prm", "plm"};
    private static String[] PERPENDICULAR_WORDS = {"perpendicular", "per", "orthogonal", "ortho"};
    private static String[] PI_WORDS = {"pi"};
    private static String[] PROJECT_WORDS = {"project", "proj"};
    private static String[] ROTATE_WORDS = {"rotate", "rot"};
    private static String[] SLOPE_WORDS = {"slope", "slp"};
    private static String[] VCENTER_WORDS = {"vcenter", "vcentre", "vctr"};
    private static String[] VDISTANCE_WORDS = {"vdistance", "vdist", "vdst"};
    private static String[] VERTICAL_WORDS = {"vertical", "vt", "vert"};
    private static String[] VERTICES_WORDS = {"vertices", "endpoints"};
    private static String[] WEIGHT_WORDS = {"weight", "wt"};
    private Token[] lookAhead = new Token[2];
    private double[] lookAheadNumber = new double[2];
    private String[] lookAheadWord = new String[2];
    private DrawingModel model;
    private StreamTokenizer streamTokenizer;
    private String line;

    /** Creates a new instance of Parser */
    public Parser() {
    }

    public void initialize(String line) {
        this.line = line;
        initialize(new StreamTokenizer(new StringReader(line)));
    }

    private void initialize(StreamTokenizer st) {
        this.streamTokenizer = st;
        st.wordChars((int) '_', (int) '_');
        st.ordinaryChar((int) '/');
        st.ordinaryChar((int) '-');
        st.ordinaryChar((int) '*');
        st.ordinaryChar((int) '+');

//        st.ordinaryChar((int) '.');
        lookAhead[0] = this.nextToken();
        if (lookAhead[0] == Token.NUM) {
            lookAheadNumber[0] = st.nval;
        } else if (lookAhead[0] == Token.POINT
                || lookAhead[0] == Token.CELL
                || lookAhead[0] == Token.WORD) {
            lookAheadWord[0] = st.sval;
        }
        if (lookAhead[0] != Token.EOF) {
            lookAhead[1] = nextToken();
            if (lookAhead[1] == Token.NUM) {
                lookAheadNumber[1] = st.nval;
            } else if (lookAhead[1] == Token.POINT
                    || lookAhead[1] == Token.CELL
                    || lookAhead[1] == Token.WORD) {
                lookAheadWord[1] = st.sval;
            }
        }
    }

    private Token nextToken() {
        try {
            switch (streamTokenizer.nextToken()) {
                case StreamTokenizer.TT_EOF:
                    return Token.EOF;
                case StreamTokenizer.TT_EOL:
                    return Token.EOL;
                case StreamTokenizer.TT_NUMBER:
                    return Token.NUM;
                case StreamTokenizer.TT_WORD:
                    return getWordToken(streamTokenizer.sval);
                case '=':
                    return Token.EQ;
                case '(':
                    return Token.LPAR;
                case ')':
                    return Token.RPAR;
                case '+':
                    return Token.PLUS;
                case '-':
                    return Token.MINUS;
                case '%':
                    return Token.MOD;
                case '*':
                    return Token.MULT;
                case '/':
                    return Token.DIV;
                case ',':
                    return Token.COMMA;
                case '^':
                    return Token.POWER;
                default:
                    return Token.OTHER;
            }
        } catch (IOException ex) {
            return Token.OTHER;
        }
    }

    private Token getWordToken(String s) {

        if (s.matches("P_[0-9]+")) {
            return Token.POINT;
        }

        if (s.matches("C_[0-9]+")) {
            return Token.CELL;
        }

        if (s.matches("S_[0-9]+")) {
            return Token.TRACE;
        }

        if (s.matches("L_[0-9]+")) {
            return Token.LINE;
        }



        for (int i = 0; i < ANCHOR_WORDS.length; i++) {
            if (s.equals(ANCHOR_WORDS[i])) {
                return Token.ANCHOR;
            }
        }

        for (int i = 0; i < AND_WORDS.length; i++) {
            if (s.equals(AND_WORDS[i])) {
                return Token.AND;
            }
        }

        for (int i = 0; i < ANGLE_WORDS.length; i++) {
            if (s.equals(ANGLE_WORDS[i])) {
                return Token.ANGLE;
            }
        }

        for (int i = 0; i < CENTER_WORDS.length; i++) {
            if (s.equals(CENTER_WORDS[i])) {
                return Token.CENTER;
            }
        }

        for (int i = 0; i < CLIP_WORDS.length; i++) {
            if (s.equals(CLIP_WORDS[i])) {
                return Token.CLIP;
            }
        }

        for (int i = 0; i < COLINEAR_WORDS.length; i++) {
            if (s.equals(COLINEAR_WORDS[i])) {
                return Token.COLINEAR;
            }
        }

        for (int i = 0; i < DISTANCE_WORDS.length; i++) {
            if (s.equals(DISTANCE_WORDS[i])) {
                return Token.DISTANCE;
            }
        }
        for (int i = 0; i < EQUIDISTANT_WORDS.length; i++) {
            if (s.equals(EQUIDISTANT_WORDS[i])) {
                return Token.EQUIDISTANT;
            }
        }
        for (int i = 0; i < HCENTER_WORDS.length; i++) {
            if (s.equals(HCENTER_WORDS[i])) {
                return Token.HCENTER;
            }
        }

        for (int i = 0; i < HDISTANCE_WORDS.length; i++) {
            if (s.equals(HDISTANCE_WORDS[i])) {
                return Token.HDISTANCE;
            }
        }

        for (int i = 0; i < HORIZONTAL_WORDS.length; i++) {
            if (s.equals(HORIZONTAL_WORDS[i])) {
                return Token.HORIZONTAL;
            }
        }

        for (int i = 0; i < PARALLEL_WORDS.length; i++) {
            if (s.equals(PARALLEL_WORDS[i])) {
                return Token.PARALLEL;
            }
        }

        for (int i = 0; i < PARALLELOGRAM_WORDS.length; i++) {
            if (s.equals(PARALLELOGRAM_WORDS[i])) {
                return Token.PARALLELOGRAM;
            }
        }

        for (int i = 0; i < PERPENDICULAR_WORDS.length; i++) {
            if (s.equals(PERPENDICULAR_WORDS[i])) {
                return Token.PERPENDICULAR;
            }
        }

        for (int i = 0; i < PI_WORDS.length; i++) {
            if (s.equals(PI_WORDS[i])) {
                return Token.PI;
            }
        }

        for (int i = 0; i < PROJECT_WORDS.length; i++) {
            if (s.equals(PROJECT_WORDS[i])) {
                return Token.PROJECT;
            }
        }

        for (int i = 0; i < ROTATE_WORDS.length; i++) {
            if (s.equals(ROTATE_WORDS[i])) {
                return Token.ROTATE;
            }
        }

        for (int i = 0; i < SLOPE_WORDS.length; i++) {
            if (s.equals(SLOPE_WORDS[i])) {
                return Token.SLOPE;
            }
        }

        for (int i = 0; i < VCENTER_WORDS.length; i++) {
            if (s.equals(VCENTER_WORDS[i])) {
                return Token.VCENTER;
            }
        }
        for (int i = 0; i < VDISTANCE_WORDS.length; i++) {
            if (s.equals(VDISTANCE_WORDS[i])) {
                return Token.VDISTANCE;
            }
        }

        for (int i = 0; i < VERTICAL_WORDS.length; i++) {
            if (s.equals(VERTICAL_WORDS[i])) {
                return Token.VERTICAL;
            }
        }

        for (int i = 0; i < VERTICES_WORDS.length; i++) {
            if (s.equals(VERTICES_WORDS[i])) {
                return Token.VERTICES;
            }
        }

        for (int i = 0; i < WEIGHT_WORDS.length; i++) {
            if (s.equals(WEIGHT_WORDS[i])) {
                return Token.WEIGHT;
            }
        }
        return Token.WORD;
    }

    private Object match(Token givenToken) throws ParseException {
        if (lookAhead[0] != givenToken) {
            throw new ParseException("Unexpected token: " + lookAhead[0] + " Looking for: " + givenToken, 0);
        }
        Object retObj;
        if (givenToken == Token.NUM) {
            retObj = new Double(lookAheadNumber[0]);
        } else if (givenToken == Token.POINT
                || givenToken == Token.CELL
                || givenToken == Token.TRACE
                || givenToken == Token.LINE) {
            retObj = lookAheadWord[0];
        } else if (givenToken == Token.WORD) {
            try {
                retObj = resolveFunction(lookAheadWord[0]);
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        } else {
            retObj = null;
        }
        lookAhead[0] = lookAhead[1];
        lookAheadNumber[0] = lookAheadNumber[1];
        lookAheadWord[0] = lookAheadWord[1];
        if (lookAhead[1] != Token.EOF) {
            lookAhead[1] = nextToken();
            if (lookAhead[1] == Token.NUM) {
                lookAheadNumber[1] = streamTokenizer.nval;
            } else if (lookAhead[1] == Token.POINT
                    || lookAhead[1] == Token.CELL
                    || lookAhead[1] == Token.TRACE
                    || lookAhead[1] == Token.LINE
                    || lookAhead[1] == Token.WORD) {
                lookAheadWord[1] = streamTokenizer.sval;
            }
        }
        return retObj;
    }

    public void setModel(DrawingModel m) {
        model = m;
    }

    public PointVar pointConstraint() throws ParseException {
        PointVar cp;
        if (lookAhead[0] == Token.EOF) {
            return new PointVar();
        }
        match(Token.EQ);
        switch (lookAhead[0]) {
            case ANGLE:
            case COLINEAR:
            case DISTANCE:
            case EQUIDISTANT:
            case HCENTER:
            case HDISTANCE:
            case HORIZONTAL:
            case PARALLEL:
            case PERPENDICULAR:
            case SLOPE:
            case VCENTER:
            case VDISTANCE:
            case VERTICAL:
                try {
                    cp = constraint1degree();
                    if (lookAhead[0] == Token.AND) {
                        match(Token.AND);
                        PointVar cp2 = constraint1degree();
                        try {
                            model.mergeEntities(cp, cp2);
                        } catch (ConstraintGraphException ex) {
                            throw new ParseException(ex.getMessage(), 0);
                        }
                    }
                } catch (ConstraintGraphException ex) {
                    throw new ParseException(ex.getMessage() + "\n" + line, 0);
                } catch (IllegalArgumentException ex) {
                    throw new ParseException(ex.getMessage() + "\n" + line, 0);
                }
                break;
            default:
                try {
                    cp = constraint2degrees();
                } catch (IllegalArgumentException ex) {
                    throw new ParseException(ex.getMessage() + "\n" + line, 0);
                } catch (ConstraintGraphException ex) {
                    throw new ParseException(ex.getMessage() + "\n" + line, 0);
                }
        }
        return cp;
    }

    public AnchorVar anchorConstraint() throws ParseException {
        if (lookAhead[0] == Token.EOF) {
            return null;
        }
        AnchorVar anchor = new AnchorVar();
        try {
            match(Token.EQ);
            switch (lookAhead[0]) {
                case ANCHOR:
                    PointVar cp1,
                     cp2,
                     cp3;
                    match(Token.ANCHOR);
                    match(
                            Token.LPAR);
                    cp1 = pointRef();
                    if (lookAhead[0] == Token.COMMA) {
                        match(Token.COMMA);
                        cp2 = pointRef();
                        if (lookAhead[0] == Token.COMMA) {
                            match(Token.COMMA);
                            cp3 = pointRef();
                            match(Token.RPAR);
                            model.addConstraint(new Anchor3Constraint(),
                                    new DmEntity[]{cp1, cp2, cp3}, anchor);
                        } else {
                            match(Token.RPAR);
                            model.addConstraint(new Anchor2Constraint(), new DmEntity[]{cp1, cp2}, anchor);
                        }
                    } else {
                        match(Token.RPAR);
                        model.addConstraint(new Anchor1Constraint(), new DmEntity[]{cp1}, anchor);
                    }
                    break;
                default:

            }
        } catch (ConstraintGraphException ex) {
            throw new ParseException(ex.getMessage(), 0);
        } catch (IllegalArgumentException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
        return anchor;

    }

    public ShapeVar shapeConstraint() throws ParseException {
        if (lookAhead[0] == Token.EOF) {
            return null;
        }
        ShapeVar shape = new ShapeVar();
        try {
            match(Token.EQ);
            switch (lookAhead[0]) {
                case CLIP:
                    match(Token.CLIP);
                    match(Token.LPAR);
                    DmEntity pred = null;
                    if (lookAhead[0] == Token.TRACE) {
                        pred = traceRef();
                    } else if (lookAhead[0] == Token.LINE) {
                        pred = lineRef();
                    }
                    match(Token.RPAR);
                    model.addConstraint(new ShapeConstraint(), new DmEntity[]{pred}, shape);
                    break;
                default:

            }
        } catch (ConstraintGraphException ex) {
            throw new ParseException(ex.getMessage(), 0);
        } catch (IllegalArgumentException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
        return shape;
    }

    public CellContent cellContent() throws ParseException {
        if (lookAhead[0] == Token.EOF) {
            return new CellContent(0.0);
        }
        try {
            if (lookAhead[0] == Token.EQ) {
                match(Token.EQ);
                NumVar num = numExpr();
                if (lookAhead[0] != Token.EOF) {
                    throw new ParseException("Unable to parse input.", 0);
                }
                CellContent cc = new CellContent(num.getNValue());
                try {
                    model.mergeEntities(cc, num);
                } catch (ConstraintGraphException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                }
                return cc;
            } else {
                //match simple strings
                CellContent cc = new CellContent();
                cc.setSValue(line);
                return cc;
            }
        } catch (ConstraintGraphException ex) {
            throw new ParseException(ex.getMessage(), 0);
        } catch (IllegalArgumentException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    private PointVar constraint1degree()
            throws ParseException, ConstraintGraphException {
        PointVar cp = new PointVar();
//        Token t = lookAhead[0];
        switch (lookAhead[0]) {
            case COLINEAR:
            case EQUIDISTANT:
            case HCENTER:
            case HDISTANCE:
            case HORIZONTAL:
            case PARALLEL:
            case PERPENDICULAR:
            case SLOPE:
            case VCENTER:
            case VDISTANCE:
            case VERTICAL: {
                LineVar cline = lineConstraint();
                LineVar[] preds = new LineVar[]{cline};
                model.addConstraint(new OnLineConstraint(), preds, cp);
                return cp;
            }
            case DISTANCE: {
                DmEntity ce = distanceConstr();
                if (ce instanceof LineVar) {
                    LineVar[] preds = {(LineVar) ce};
                    model.addConstraint(new OnLineConstraint(), preds, cp);
                } else if (ce instanceof CircleVar) {
                    CircleVar[] preds = {(CircleVar) ce};
                    model.addConstraint(new OnCircleConstraint(), preds, cp);
                }
                return cp;
            }
            case ANGLE: {
                DmEntity ce = angleConstr();
                if (ce instanceof LineVar) {
                    LineVar[] preds = {(LineVar) ce};
                    model.addConstraint(new OnLineConstraint(), preds, cp);
                } else if (ce instanceof CircleVar) {
                    CircleVar[] preds = {(CircleVar) ce};
                    model.addConstraint(new OnCircleConstraint(), preds, cp);
                }
                return cp;
            }
            default:
                throw new ParseException("Unexpected token: " + lookAhead[0], 0);
        }
    }

    private PointVar constraint2degrees()
            throws ParseException, ConstraintGraphException {

        PointVar cp = new PointVar();

        switch (lookAhead[0]) {
            case CENTER: {
                PointVar cp1,
                        cp2;
                match(Token.CENTER);
                match(
                        Token.LPAR);
                cp1 = pointRef();
                match(Token.COMMA);
                cp2 = pointRef();
                match(Token.RPAR);
                model.addConstraint(new CenterConstraint(), new DmEntity[]{cp1, cp2}, cp);
            }
            break;
            case PROJECT: {
                PointVar cp1, cp2, cp3;
                match(Token.PROJECT);
                match(Token.LPAR);
                cp1 = pointRef();
                match(Token.COMMA);
                cp2 = pointRef();
                match(Token.COMMA);
                cp3 = pointRef();
                match(Token.RPAR);
                model.addConstraint(
                        new ProjectionConstraint(), new DmEntity[]{cp1, cp2, cp3}, cp);
                break;
            }
            case PARALLELOGRAM: {
                PointVar cp1, cp2, cp3;
                match(Token.PARALLELOGRAM);
                match(Token.LPAR);
                cp1 = pointRef();
                match(Token.COMMA);
                cp2 = pointRef();
                match(Token.COMMA);
                cp3 = pointRef();
                match(Token.RPAR);
                model.addConstraint(
                        new ParallelogramConstraint(), new DmEntity[]{cp1, cp2, cp3}, cp);
                break;
            }
            case ROTATE: {
                PointVar cp1, cp2;
                match(Token.ROTATE);
                match(Token.LPAR);
                cp1 = pointRef();
                match(Token.COMMA);
                cp2 = pointRef();
                match(Token.COMMA);
                NumVar cn = numExpr();
                match(Token.RPAR);
                model.addConstraint(new RotationConstraint(), new DmEntity[]{cp1, cp2, cn}, cp);
                break;
            }
            case WEIGHT: {
                PointVar cp1, cp2;
                NumVar cn1, cn2;
                match(Token.WEIGHT);
                match(Token.LPAR);
                cp1 = pointRef();
                match(Token.COMMA);
                cn1 = numExpr();
                match(Token.COMMA);
                cp2 = pointRef();
                match(Token.COMMA);
                cn2 = numExpr();
                match(Token.RPAR);
                model.addConstraint(
                        new WeightConstraint(), new DmEntity[]{cp1, cn1, cp2, cn2}, cp);
            }
            break;
            default:
                throw new ParseException("Constraint expected.", 0);
        }
        return cp;
    }

    public LineVar lineConstraint() throws ParseException, ConstraintGraphException {
        LineVar cline = new LineVar();

        switch (lookAhead[0]) {

            case COLINEAR: {
                match(Token.COLINEAR);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2};
                try {
                    model.addConstraint(new ColinearConstraint(), preds, cline);
                } catch (IllegalArgumentException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                } catch (ConstraintGraphException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                }
            }
            break;

            case HCENTER: {
                match(Token.HCENTER);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2};
                model.addConstraint(new HCenterConstraint(), preds, cline);
            }
            break;

            case HDISTANCE: {
                match(Token.HDISTANCE);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                NumVar cn = numExpr();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cn};
                model.addConstraint(new HDistanceConstraint(), preds, cline);
            }
            break;

            case HORIZONTAL: {
                match(Token.HORIZONTAL);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1};
                model.addConstraint(new HorizontalConstraint(), preds, cline);
            }
            break;

            case EQUIDISTANT: {
                match(Token.EQUIDISTANT);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2};
                model.addConstraint(new EquidistantConstraint(), preds, cline);

            }
            break;

            case PARALLEL: {
                match(Token.PARALLEL);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.COMMA);
                PointVar cp3 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2, cp3};
                model.addConstraint(new ParallelConstraint(), preds, cline);
            }
            break;

            case PERPENDICULAR: {
                match(Token.PERPENDICULAR);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.COMMA);
                PointVar cp3 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2, cp3};
                model.addConstraint(new PerpendicularConstraint(), preds, cline);
            }
            break;

            case SLOPE: {
                match(Token.SLOPE);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                NumVar cn = numExpr();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cn};
                model.addConstraint(new SlopeConstraint(), preds, cline);
            }
            break;

            case VCENTER: {
                match(Token.VCENTER);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                PointVar cp2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cp2};
                model.addConstraint(new VCenterConstraint(), preds, cline);

            }
            break;

            case VDISTANCE: {
                match(Token.VDISTANCE);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.COMMA);
                NumVar cn = numExpr();
                match(Token.RPAR);
                DmEntity[] preds = {cp1, cn};
                model.addConstraint(new VDistanceConstraint(), preds, cline);

            }
            break;

            case VERTICAL: {
                match(Token.VERTICAL);
                match(Token.LPAR);
                PointVar cp1 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {cp1};
                model.addConstraint(new VerticalConstraint(), preds, cline);
            }
            break;
            default:
                throw new ParseException("Constraint expected.", 0);
        }
        return cline;
    }

    private DmEntity angleConstr()
            throws ParseException, ConstraintGraphException {

        match(Token.ANGLE);
        match(Token.LPAR);
        PointVar cp1 = pointRef();
        match(Token.COMMA);
        if (lookAhead[0] == Token.POINT) {
            PointVar cp2 = pointRef();
            match(Token.COMMA);
            NumVar cn = numExpr();
            match(Token.RPAR);
            LineVar cline = new LineVar();
            DmEntity[] preds = {cp1, cp2, cn};
            model.addConstraint(new AngleConstraint(), preds, cline);
            return cline;
        } else {
            NumVar cn = numExpr();
            match(Token.COMMA);
            PointVar cp2 = pointRef();
            match(Token.RPAR);
            CircleVar ccircle = new CircleVar();
            DmEntity[] preds = {cp1, cn, cp2};
            model.addConstraint(new TopAngleConstraint(), preds, ccircle);
            return ccircle;
        }
    }

    private DmEntity distanceConstr() throws ParseException, ConstraintGraphException {
        match(Token.DISTANCE);
        match(Token.LPAR);
        PointVar cp1 = pointRef();
        match(Token.COMMA);
        if (lookAhead[0] == Token.POINT) {
            PointVar cp2 = pointRef();
            match(Token.COMMA);
            NumVar cn = numExpr();
            match(Token.RPAR);
            LineVar cline = new LineVar();
            DmEntity[] preds = {cp1, cp2, cn};
            model.addConstraint(new Distance2Constraint(), preds, cline);
            return cline;
        } else {
            NumVar cn = numExpr();
            match(Token.RPAR);
            CircleVar ccircle = new CircleVar();
            DmEntity[] preds = {cp1, cn};
            model.addConstraint(new DistanceConstraint(), preds, ccircle);
            return ccircle;
        }
    }

    private PointVar pointRef() throws ParseException {
        String pr;
        PointVar cp;
        pr = (String) match(Token.POINT);
        cp = model.getPoint(pr);
        if (cp != null) {
            return cp;
        } else {
            throw new ParseException("Non-existing point: " + pr, 0);
        }
    }

    private MCell cellRef() throws ParseException {
        String cellID;
        MCell mc;
        cellID = (String) match(Token.CELL);
        mc = model.getCell(cellID);
        if (mc != null) {
            return mc;
        } else {
            throw new ParseException("Non-existing cell: " + cellID, 0);
        }

    }

    private MTrace traceRef() throws ParseException {
        String traceId;
        MTrace mt;
        traceId = (String) match(Token.TRACE);
        mt = (MTrace) model.getDrawableEntity(traceId);
        if (mt != null) {
            return mt;
        } else {
            throw new ParseException("Non-existing trace: " + traceId, 0);
        }
    }

    private MPolyline lineRef() throws ParseException {
        String lineId;
        MPolyline mp;
        lineId = (String) match(Token.LINE);
        mp = (MPolyline) model.getDrawableEntity(lineId);
        if (mp != null) {
            return mp;
        } else {
            throw new ParseException("Non-existing line: " + lineId, 0);
        }
    }

    private NumVar numExpr() throws ParseException, ConstraintGraphException {
        return sumExpr();
    }

    private UnaryFuntionConstraint resolveFunction(String string) throws IllegalArgumentException {
        return new UnaryFuntionConstraint(string);
    }

    private NumVar sumExpr() throws ParseException, ConstraintGraphException {
        NumVar aux,
                res;
        res = prodExpr();
        while (lookAhead[0] == Token.PLUS || lookAhead[0] == Token.MINUS) {
            aux = res;
            res = new NumVar();
            if (lookAhead[0] == Token.PLUS) {
                match(Token.PLUS);
                NumVar cn = prodExpr();
                DmEntity[] preds = {aux, cn};
                model.addConstraint(new PlusConstraint(),
                        preds, res);

            } else if (lookAhead[0] == Token.MINUS) {
                match(Token.MINUS);
                NumVar cn = prodExpr();
                DmEntity[] preds = {aux, cn};
                model.addConstraint(new MinusConstraint(),
                        preds, res);
            }
        }
        return res;
    }

    private NumVar prodExpr() throws ParseException, ConstraintGraphException {
        NumVar aux, res;
        res = powExpr();
        while (lookAhead[0] == Token.MULT
                || lookAhead[0] == Token.DIV
                || lookAhead[0] == Token.MOD) {
            aux = res;
            res = new NumVar();
            if (lookAhead[0] == Token.MULT) {
                match(Token.MULT);
                NumVar cn = powExpr();
                DmEntity[] preds = {aux, cn};
                model.addConstraint(new MultConstraint(),
                        preds, res);
            } else if (lookAhead[0] == Token.DIV) {
                match(Token.DIV);
                NumVar cn = powExpr();
                DmEntity[] preds = {aux, cn};
                model.addConstraint(new DivConstraint(),
                        preds, res);
            } else if (lookAhead[0] == Token.MOD) {
                match(Token.MOD);
                NumVar cn = powExpr();
                DmEntity[] preds = {aux, cn};
                model.addConstraint(new ModConstraint(),
                        preds, res);
            }
        }
        return res;
    }

    private NumVar powExpr() throws ParseException, ConstraintGraphException {
//        return signExpr();
        // Could add POW token here
        NumVar aux,
                res;
        res = signExpr();
        if (lookAhead[0] == Token.POWER) {
            aux = res;
            res = new NumVar();
            match(Token.POWER);
            NumVar cn = powExpr();
            DmEntity[] preds = {aux, cn};
            model.addConstraint(new PowConstraint(), preds,
                    res);
        }
        return res;
    }

    private NumVar signExpr() throws ParseException, ConstraintGraphException {
        if (lookAhead[0] == Token.MINUS) {
            match(Token.MINUS);
            NumVar res = new NumVar();
            NumVar cn = atom();
            DmEntity[] preds = {cn};
            model.addConstraint(new OppositeConstraint(),
                    preds, res);
            return res;
        } else {
            return atom();
        }
    }

    private NumVar atom() throws ParseException, ConstraintGraphException {
        switch (lookAhead[0]) {
            case NUM: {
                return new NumVar(
                        ((Double) match(Token.NUM)).doubleValue());
            }
            case LPAR: {
                return parExpr();
            }
            case CELL:
                MCell mc = cellRef();
                assert mc != null;
                NumVar num = new NumVar();
                model.addConstraint(new CellRefNumeric(),
                        new DmEntity[]{mc.getContent()}, num);
                return num;
            default: {
                return funcall();
            }
        }
    }

    private NumVar funcall() throws ParseException, ConstraintGraphException {
        NumVar res = new NumVar();
        switch (lookAhead[0]) {
            case ANGLE: {
                PointVar arg1, arg2,
                        arg3;
                match(Token.ANGLE);
                match(Token.LPAR);
                arg1 = pointRef();
                match(Token.COMMA);
                arg2 = pointRef();
                match(Token.COMMA);
                arg3 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {arg1, arg2, arg3};
                model.addConstraint(new FAngleConstraint(), preds, res);
                return res;
            }
            case DISTANCE: {
                PointVar arg1,
                        arg2;
                match(Token.DISTANCE);
                match(Token.LPAR);
                arg1 = pointRef();
                match(Token.COMMA);
                arg2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {arg1, arg2};
                model.addConstraint(new FdistanceConstraint(),
                        preds, res);
                return res;
            }
            case HDISTANCE: {
                PointVar arg1, arg2;
                match(Token.HDISTANCE);
                match(Token.LPAR);
                arg1 = pointRef();
                match(Token.COMMA);
                arg2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {arg1, arg2};
                model.addConstraint(new FHdistanceConstraint(),
                        preds, res);
                return res;
            }
            case PI: {
                DmEntity[] preds = {};
                match(Token.PI);
                model.addConstraint(new PiConstraint(),
                        preds, res);
                return res;
            }
            case VDISTANCE: {
                PointVar arg1, arg2;
                match(Token.VDISTANCE);
                match(Token.LPAR);
                arg1 = pointRef();
                match(Token.COMMA);
                arg2 = pointRef();
                match(Token.RPAR);
                DmEntity[] preds = {arg1, arg2};
                model.addConstraint(new FVdistanceConstraint(),
                        preds, res);
                return res;
            }
            case WORD: {
                NumVar arg;
                UnaryFuntionConstraint c = (UnaryFuntionConstraint) match(Token.WORD);
                match(Token.LPAR);
                arg = numExpr();
                DmEntity[] preds = {arg};
                model.addConstraint(c, preds, res);
                match(Token.RPAR);
                return res;
            }
            default:
                throw new ParseException("Unexpected Token: " + lookAhead[0] + ".", 0);
        }
    }

    private NumVar parExpr() throws ParseException, ConstraintGraphException {
        NumVar res;
        match(Token.LPAR);
        res = sumExpr();
        match(Token.RPAR);
        return res;
    }
}
