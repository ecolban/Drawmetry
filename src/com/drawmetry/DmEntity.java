/*
 * DmEntity.java
 *
 * Created on December 12, 2005, 9:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.drawmetry;

import java.util.Set;

/**
 * This is the base class for all objects that may be constrained.
 * 
 * All <code>DmEntity</code> subtypes must implement <code>Clonable</code>.
 * @author Erik Colban
 */
public interface DmEntity extends Cloneable {
    
    /**
     * The possible types of a DmEntity. A DmEntity can have more than one type.
     * For instance, an entity that has type <tt>TRACE</tt> can also have type
     * <tt>PATH</tt>
     */
    static public enum Type {
        /**
         * Used as the target type of an <tt>AnchorConstraint</tt>. Anchor-able
         * entities such traces, cells and images are associated with an
         * entity of type <tt>ANCHOR_VAR</tt>.
         * @see drawmetry.constraints.AnchorConstraint
         */
        ANCHOR_VAR,
        /**
         * All DmEntities have type <tt>ANY</tt>. Used as predecessor and target
         * type of a <tt>NullConstraint</tt>.
         * @see drawmetry.constraints.NullConstraint
         */
        ANY,
        /**
         * Deferred.
         */
        ARC,
        /**
         * Used as target type of <tt>DistanceConstraint</tt> and 
         * <tt>TopAngleConstraint</tt>, and as predecessor type of 
         * <tt>OnCircleConstraint</tt>. An entity of this type is determined by
         * the center and radius.
         * @see drawmetry.constraints.DistanceConstraint
         * @see drawmetry.constraints.TopAngleConstraint
         * @see drawmetry.constraints.OnCircleConstraint
         * 
         */
        CIRCLE_VAR,
        /**
         * This is the type of a line segment or a poly-line. It is used as
         * target type of an <tt>MLineConstraint</tt> or
         * <tt>MPolyLineConstraint</tt>.
         * @see drawmetry.constraints.MLineConstraint
         * @see drawmetry.constraints.MPolylineConstraint
         */
        LINE,
        /**
         * Used as target type of <tt>ColinearConstraint</tt>,
         * <tt>AngleConstraint</tt>, and many other constraints where the
         * geometric lieu of points that satisfy the constraint is a line. It is
         * also used as predecessor type of
         * <tt>OnLineConstraint</tt>. An entity of this type is determined by
         * the three coefficients of the line equation.
         *
         * @see drawmetry.constraints.ColinearConstraint
         * @see drawmetry.constraints.AngleConstraint
         * @see drawmetry.constraints.OnLineConstraint
         *
         */
        LINE_VAR,
        /**
         * The type of a DmEntity that is used to hold a numeric value. It is
         * used as predecessor or target type in many constraints.
         */
        NUM_VAR, 
        /**
         * Used as predecessor type of a <tt>ShapeConstraint</tt>.
         * @see drawmetry.constraints.ShapeConstraint 
         */
        PATH,
        /**
         * The type of a DmEntity that is used to hold a point as a value. It is
         * used as predecessor or target type in many constraints.
         */
        POINT_VAR,
        /**
         * Used as target type of a <tt>ShapeConstraint</tt>.
         * @see drawmetry.constraints.ShapeConstraint 
         */
        SHAPE,
        /**
         * Deferred
         */
        TEXT_VAR,
        /**
         * This is used as the target type of an <tt>MTraceConstraint</tt>.
         * @see drawmetry.constraints.MTraceConstraint
         */
        TRACE;
    }

    /**
     * Clones the entity.
     * @return the clone.
     * @throws CloneNotSupportedException
     */
    public DmEntity clone() throws CloneNotSupportedException;
    
    /**
     * Checks if the entity has a given type.
     * @param type the given type
     * @return true if at least one of the entity's types is the given type
     */
    public boolean hasType(Type type);

    /**
     * Gets an identifier of the entity.
     * @return the entity id.
     */
    public String getEntityID();
    
    /**
     * Gets the entity's constraint.
     * @return the entity's constraint, or <tt>null</tt> if the entity is not 
     * constrained. 
     */
    public Constraint getConstraint();
    
    /**
     * Sets the constraint of the entity to a given constraint.
     * @param constraint The given constraint.
     */
    public void setConstraint(Constraint constraint);

    /**
     * Gets the successors of the entity.
     * @return The set of successors.
     */
    public Set<Constraint> getSuccessors();

    /**
     * Determines if a constraint is required. Some entities, if they are not
     * constrained, are removed by the constraint manager.
     *
     * @return true if this entity requires a constraint.
     *
     * @see com.drawmetry.ConstraintManager
     */
    public boolean isConstraintRequired();

    /**
     * Determines if a successor is required. Some entities, if they do not
     * have at least one successor, are removed by the constraint manager.
     *
     * @return true if this entity requires a successor.
     *
     * @see com.drawmetry.ConstraintManager
     */
    public boolean isSuccessorRequired();

    /**
     * Makes an <tt>EntityRecord</tt>, which is used for undo/redo purposes, of
     * the entity.
     *
     * @param zIndex, the placement of the entity with respect to other entities
     * @return an <t>EntityRecord</tt> of the entity
     *
     * @see com.drawmetry.EntityRecord
     */
    public EntityRecord makeRecord(int zIndex);

    /**
     * Updates the state of the entity so that it matches the parameters in a
     * given <tt>EntityRecord</tt>.
     * @param entityRecord the given <tt>EntityRecord</tt>.
     */
    public void setParams(EntityRecord entityRecord);
    
    /**
     * Adds (deltaX, deltaY) to the entity's current position.
     * @param deltaX the distance in points to increase the entity's horizontal
     * position.
     *
     * @param deltaY the distance in points to increase the entity's vertical
     * position.
     */
    public void translate(double deltaX, double deltaY);

    /**
     * Adds (deltaX, deltaY) to the entity's position when initMove() was last
     * called and updates the entity's current position to that position.
     *
     * @param deltaX the distance in points to increase the entity's horizontal
     * position.
     * @param deltaY the distance in points to increase the entity's vertical
     * position.
     */
    public void move(double deltaX, double deltaY);

    /**
     * Records the entity's current position, which affects the result of
     * <tt>move()</tt>.
     *
     * @see #move(double, double)
     */
    public void initMove();
    
    
}
