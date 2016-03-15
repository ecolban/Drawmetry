/*
 * DrawablesList.java
 *
 * Created on September 4, 2005, 4:35 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.drawmetry;

import java.util.*;

/**
 *
 * @author Erik Colban
 */
class DrawablesList {
    
    private int separator = 0;
    private ArrayList<DrawableEntity> list = new ArrayList<DrawableEntity>();
    
    /**
     * Creates a new instance of DrawablesList
     */
    DrawablesList() {
        super();
    }
    
    int sendToFront(DrawableEntity obj) {
        int index = list.indexOf(obj);
        if (0 <= index){
            list.remove(index);
            if (index < separator) {
                assert !(obj instanceof PointVar);
                list.add(separator - 1, obj);
            } else {
                assert (obj instanceof PointVar);
                list.add(obj);
            }
            return index;
        } else return -1;
    }
    
    int sendToBack(DrawableEntity obj) {
        int index = list.indexOf(obj);
        if (0 <= index){
            list.remove(index);
            if (index < separator) {
                assert !(obj instanceof PointVar);
                list.add(0, obj);
            } else {
                assert (obj instanceof PointVar);
                list.add(separator, obj);
            }
            return index;
        } else return -1;
    }
    
    int sendBackward(DrawableEntity obj) {
        int i = list.indexOf(obj);
        if (i > 0 && i != separator ) {
            list.set(i , list.get(i-1));
            list.set(i-1, obj);
            return i;
        } else return -1;
    }
    
    int sendForward( DrawableEntity obj) {
        int i = list.indexOf(obj);
        if (i >= 0 && i != separator - 1 && i < list.size() - 1) {
            list.set( i , list.get(i + 1));
            list.set(i+1, obj);
            return i;
        } else return -1;
    }
    
    int add(DrawableEntity obj){
        if (!list.contains(obj)) {
            if (obj instanceof PointVar){
                list.add(obj);
                return list.size() - 1;
            } else {
                list.add(separator++, obj);
                return separator - 1;
            }
        }
        return -1;
    }
    
    boolean add(int index, DrawableEntity obj) {
        if (!list.contains(obj) && index >= 0) {
            list.add(index, obj);
            if (!(obj instanceof PointVar)) {
                assert (index <= separator);
                separator++;
            }
            return true;
        } else return false;
    }
    
    int remove(DrawableEntity obj) {
        int index = list.indexOf(obj);
        if (0 <= index){
            list.remove(index);
            if (index < separator) {
                assert !(obj instanceof PointVar);
                separator--;
            } else {
                assert (obj instanceof PointVar);
            }
            return  index;
        } else return -1;
    }
    
    Iterator<DrawableEntity> drawableIterator(boolean topToBottom) {
        if (topToBottom){
            final ListIterator<DrawableEntity> bkToFtIterator
                    = list.listIterator(list.size());
            
            return new Iterator<DrawableEntity>() {
//                @Override
                public boolean hasNext() {
                    return bkToFtIterator.hasPrevious();
                }
//                @Override
                public DrawableEntity next() {
                    return bkToFtIterator.previous();
                }
//                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented.");
                }
                
            };
        } else {
            return list.iterator();
        }
    }
    
    Iterator<PointVar> pointIterator(boolean topToBottom) {
        if (topToBottom) {
            final ListIterator<DrawableEntity> bkToFtIterator
                    = list.listIterator(list.size());
            return new Iterator<PointVar>() {
//                @Override
                public boolean hasNext() {
                    return bkToFtIterator.previousIndex() >= separator;
                }
//                @Override
                public PointVar next() {
                    return (PointVar) bkToFtIterator.previous();
                }
//                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented.");
                }
            };
        } else {
            final ListIterator<DrawableEntity> ftToBkIterator
                    = list.listIterator(separator);
            return new Iterator<PointVar>() {
//                @Override
                public boolean hasNext() {
                    return ftToBkIterator.hasNext();
                }
//                @Override
                public PointVar next() {
                    return (PointVar) ftToBkIterator.next();
                }
//                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented.");
                }
            };
        }
    }

    int size() {
        return list.size();
    }
}

