/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drawmetry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 *
 *
 * @author Erik
 */
public class Group {

    /*
     * If a group is top level, then:
     * a) It is not contained in any other group
     * b) Its instance variable topLevel is true
     * c) It is contained in TOP_LEVEL_GROUPS
     * 
     * All non-top level groups are contained in another group. 
     * All DrawableEntities of a non-top level group are contained in the 
     * all groups that contain the group.
     * If two groups contain the same non-PointVar, one is contained (directly
     * or indirectly) in the other. 
     * A group must contain at least one DrawableEntity that is not a PointVar
     */
    private static final Set<Group> TOP_LEVEL_GROUPS = new HashSet<Group>();
    private boolean topLevel = true;
    private List<Group> subGroups = new ArrayList<Group>();
    private Set<DrawableEntity> drawables = new HashSet<DrawableEntity>();

    public static Group makeGroup(Selection selection) {
        Group group = new Group();
        for (DrawableEntity de : selection.getHits()) {
            if (!(de instanceof PointVar)) {
                boolean alreadyInAGroup = false;
                for (Iterator<Group> i = TOP_LEVEL_GROUPS.iterator(); i.hasNext();) {
                    Group g = i.next();
                    if (g.contains(de)) {
                        group.addGroup(g);
                        g.topLevel = false;
                        i.remove();
                        alreadyInAGroup = true;
                        break; //No other top level group can contain de.
                    }
                }
                if (!alreadyInAGroup) {
                    group.addDrawableEntity(de);
                }
            }
        }
        if (group.drawables.isEmpty()) {
            return null;
        }
        for (DrawableEntity de : selection.getHits()) {
            if (de instanceof PointVar) {
                group.addDrawableEntity(de);
            }
        }
        if (group.subGroups.size() == 1) {
            Group singleSubGroup = group.subGroups.get(0);
            TOP_LEVEL_GROUPS.add(singleSubGroup);
            singleSubGroup.topLevel = true;
            return singleSubGroup;
        }
        TOP_LEVEL_GROUPS.add(group);
        return group;
    }

    public static void updateSelection(Selection selection) {
        for (Iterator<DrawableEntity> i = selection.drawablesIterator(); i.hasNext();) {
            DrawableEntity entity = i.next();
            if(entity instanceof PointVar) {
                break;
            }
            for (Group g : TOP_LEVEL_GROUPS) {
                if (g.contains(entity)) {
                    for(DrawableEntity de: g.drawables) {
                        selection.addHit(de);
                    }
                    break; //
                }
            }
        }
    }

    public static boolean removeGroup(Group group) {
        if (!group.topLevel) {
            return false; //Cannot remove a non-top level group
        }
        group.topLevel = false;
        TOP_LEVEL_GROUPS.remove(group);
        for (Group g : group.subGroups) {
            g.topLevel = true;
            TOP_LEVEL_GROUPS.add(g);
        }
        return true;
    }

    public static Group getGroup(DrawableEntity entity) {
        if (entity instanceof PointVar) {
            return null;
        }
        for (Group g : TOP_LEVEL_GROUPS) {
            if (g.contains(entity)) {
                return g;
            }
        }
        return null;
    }

    public static List<Group> getGroups(PointVar p) {
        List<Group> result = new ArrayList<Group>();
        for (Group g : TOP_LEVEL_GROUPS) {
            if (g.contains(p)) {
                result.add(g);
            }
        }
        return result;
    }

    public Set<PointVar> getPointVars() {
        HashSet<PointVar> resultSet = new HashSet<PointVar>();
        for (DrawableEntity de : drawables) {
            if (de instanceof PointVar) {
                resultSet.add((PointVar) de);
            }
        }
        return resultSet;
    }

    public List<DrawableEntity> getDrawableEntities() {
        return new ArrayList<DrawableEntity>(drawables); //defensive copy
    }

    public boolean contains(DrawableEntity de) {
        for (DrawableEntity de2 : drawables) {
            if (de2.equals(de)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Group group) {
        for (Group g : subGroups) {
            if (g.equals(group) || g.contains(group)) {
                return true;
            }
        }
        return false;
    }

    private boolean addGroup(Group group) {
        if (!topLevel) {
            return false;
        }
        if (!group.topLevel) {
            return false;
        }
        if (group.getDrawableEntities().isEmpty()) {
            return false;
        }
        if (contains(group)) {
            return false;
        }
        boolean added = subGroups.add(group);
        if (added) {
            group.topLevel = false;
            drawables.addAll(group.getDrawableEntities());
        }
        return added;
    }

    private boolean addDrawableEntity(DrawableEntity de) {
        if (!topLevel) {
            return false;
        }
        if (!(de instanceof PointVar)) {
            for (Group g : TOP_LEVEL_GROUPS) {
                if (g.contains(de)) {
                    return false;
                }
            }
        }
        return drawables.add(de);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.drawables != null ? this.drawables.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if (this.drawables != other.drawables
                && (this.drawables == null || !this.drawables.equals(other.drawables))) {
            return false;
        }
        return true;
    }
}
