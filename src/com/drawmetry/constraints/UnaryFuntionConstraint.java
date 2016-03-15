package com.drawmetry.constraints;

import java.util.HashMap;
import java.util.Map;

import com.drawmetry.Constraint;
import com.drawmetry.DmEntity;
import com.drawmetry.NumVar;

/**
 *
 * @author  Erik Colban
 */
public class UnaryFuntionConstraint extends Constraint {

    private static abstract class Function {

        abstract double appliedTo(double arg);
    }
    private final static Map<String, Function> functionMap = new HashMap<String, Function>();

    static {// Build the functionMap during class initialization
        functionMap.put("abs", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.abs(arg);
            }
        });
        functionMap.put("acos", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.acos(arg);
            }
        });
        functionMap.put("asin", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.asin(arg);
            }
        });
        functionMap.put("atan", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.atan(arg);
            }
        });
        functionMap.put("ceil", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.ceil(arg);
            }
        });
        functionMap.put("cos", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.cos(arg);
            }
        });
        functionMap.put("cbrt", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.cbrt(arg);
            }
        });
        functionMap.put("ceil", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.ceil(arg);
            }
        });
        functionMap.put("cosh", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.cosh(arg);
            }
        });
        functionMap.put("exp", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.exp(arg);
            }
        });
        functionMap.put("floor", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.floor(arg);
            }
        });
        functionMap.put("log", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.log(arg);
            }
        });
        functionMap.put("log10", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.log10(arg);
            }
        });
        functionMap.put("rint", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.rint(arg);
            }
        });
        functionMap.put("signum", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.signum(arg);
            }
        });
        functionMap.put("signum2", new Function() {

            @Override
            double appliedTo(double arg) {
                return arg < 0 ? -1.0 : 1.0;
            }
        });
        functionMap.put("sin", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.sin(arg);
            }
        });
        functionMap.put("sinh", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.sinh(arg);
            }
        });
        functionMap.put("sqrt", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.sqrt(arg);
            }
        });
        functionMap.put("tan", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.tan(arg);
            }
        });
        functionMap.put("tanh", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.tanh(arg);
            }
        });
        functionMap.put("toDegrees", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.toDegrees(arg);
            }
        });
        functionMap.put("toRadians", new Function() {

            @Override
            double appliedTo(double arg) {
                return Math.toRadians(arg);
            }
        });
    }
    private NumVar pred1;
    private NumVar target;
    private Function function;
    private String functionName;

    /** Creates a new instance. */
    public UnaryFuntionConstraint(String functionName) throws IllegalArgumentException {
        this.functionName = functionName;
        function = functionMap.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException(functionName + " is not a supported function.");
        }

    }

    public com.drawmetry.DmEntity.Type[] getPredTypes() {
        return new DmEntity.Type[]{DmEntity.Type.NUM_VAR};
    }

    public com.drawmetry.DmEntity.Type getTargetType() {
        return DmEntity.Type.NUM_VAR;
    }

    public DmEntity[] getPreds() {
        return new DmEntity[]{pred1};
    }

    public void setPreds(int i, DmEntity e) {
        if (i != 0 || !(e instanceof NumVar) && e != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (NumVar) e;
    }

    public void setPreds(DmEntity[] preds) {
        if (preds.length != 1 || !(preds[0] instanceof NumVar) && preds[0] != null) {
            throw new IllegalArgumentException();
        }
        pred1 = (NumVar) preds[0];
    }

    protected void setTarget(DmEntity target) {
        if (!(target instanceof NumVar) && target != null) {
            throw new IllegalArgumentException();
        }
        this.target = (NumVar) target;
        if (target != null) {
            this.target.precedence = 4;
        }
    }

    protected DmEntity getTarget() {
        return target;
    }

    public void fire() {
        if (pred1 != null) {
            target.nValue = function.appliedTo(pred1.nValue);
        }
    }

    @Override
    public String toString() {
        return functionName + "(" + pred1.toString() + ")";
    }

    @Override
    public int degree() {
        return 1;
    }
}
