package com.drawmetry;

import java.awt.geom.GeneralPath;
import java.util.HashSet;
import java.util.Set;

import com.drawmetry.DmEntity.Type;
import com.drawmetry.constraints.NullConstraint;
import com.drawmetry.constraints.ShapeConstraint;

/**
 * A ShapeVar instance is an entity that can be constrained by a ShapeConstraint.
 * An MImage
 *
 *
 * @see com.drawmetry.constraints.ShapeConstraint
 *
 * @author Erik
 */
public class ShapeVar implements DmEntity {

    private ShapeConstraint constraint = null;
    private Set<Constraint> successors = new HashSet<Constraint>();
    private GeneralPath path;

    @Override
    public ShapeVar clone() throws CloneNotSupportedException {
        ShapeVar clone = (ShapeVar) super.clone();
        clone.constraint = null;
        clone.successors = new HashSet<Constraint>();
        clone.path = null;
        return clone;
    }

//    @Override
    public boolean hasType(DmEntity.Type type) {
        return type == Type.SHAPE;
    }

//    @Override
    public String getEntityID() {
        for (Constraint c : getSuccessors()) {
            if (c instanceof NullConstraint) {
                assert c.getTarget() != null;
                return c.getTarget().getEntityID();
            }
        }
        return "";
    }

//    @Override
    public Constraint getConstraint() {
        return constraint;
    }

//    @Override
    public void setConstraint(Constraint constraint) {
        if (constraint instanceof ShapeConstraint) {
            this.constraint = (ShapeConstraint) constraint;

        } else {
            this.constraint = null;
        }
    }

//    @Override
    public Set<Constraint> getSuccessors() {
        return successors;
    }

//    @Override
    public boolean isConstraintRequired() {
        return false;
    }

//    @Override
    public boolean isSuccessorRequired() {
        return true;
    }

    static private class ShapeVarRecord implements EntityRecord {
        private ShapeVar shapeVar;
        private int zIndex;
        private GeneralPath path;

        ShapeVarRecord(ShapeVar var, int z) {
            this.shapeVar = var;
            this.zIndex = z;
            this.path = var.path == null ? null : (GeneralPath) var.path.clone();
        }

        public DmEntity getEntity() {
            return this.shapeVar;
        }

        public double [] getParams() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public int getZIndex() {
            return this.zIndex;
        }
    }

    public EntityRecord makeRecord(final int zIndex) {
        return new ShapeVarRecord(this, zIndex);
    }

//    @Override
    public void setParams(EntityRecord er) {
        if (er.getEntity() == this && (er instanceof ShapeVarRecord)) {
            ShapeVarRecord svr = (ShapeVarRecord) er;
            path = svr.path;
        }
    }

//    @Override
    public void translate(double deltaX, double deltaY) {
    }

//    @Override
    public void initMove() {
    }

//    @Override
    public void move(double deltaX, double deltaY) {
    }

    @Override
    public String toString() {
        return "A ShapeVar";
    }

    public GeneralPath getPath() {
        if (path != null && constraint != null) {
            return (GeneralPath) path.clone();
        } else {
//            path = null;
            return null;
        }
    }

    public void setPath(GeneralPath path) {
        this.path = path;
    }
}
