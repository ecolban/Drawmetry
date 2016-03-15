/*
 * PathGenerator.java
 *
 * Created on November 22, 2007, 6:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.drawmetry;

import Jama.Matrix;
import java.awt.geom.Point2D;

/**
 * This class is used to generate instances of Path objects.
 * Typical usage is
 *    <code>PathGenerator.getInstance()(path, d, threshold)</code>, where
 * <code>d</code> is an array of Point2D.Double sample points. This will
 * return a Path2D object consisting of Bezier curves.
 *
 *
 * @author Erik
 *
 * @see #getPath
 */
class PathGenerator {

    private static Point2D.Double[] samplePoints;
    private static double[][] bezier3Coeff = new double[][]{{-1.0, 3.0, -3.0, 1.0},
        {3.0, -6.0, 3.0, 0.0},
        {-3.0, 3.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0}
    };
    private static double[][] controlPoints = new double[4][2];
    private static double[][] position = new double[4][2];
    private static double[][] velocity = new double[3][2];
    private static double[] initialT;
//    private static Matrix matrixWxD;
    private static Matrix matrixWxDminusWxTxB03xP03t;
//    private static Matrix matrixWxTxB;
    private static Matrix matrixWxTxB12;
    private static Matrix matrixX;
    private static double fitToleranceSq;

    /** Creates a new instance of PathGenerator */
    private PathGenerator() {
    }

    /**
     * Generates a Path2D from an array of sample points
     *
     * @param path - an object that implements the Path interface. The result is 
     * returned by calling this argument's methods.
     * @param samplePoints - a Point2S.Double array of sampled points along a curve.
     * @param fitTolerance - The maximum tolerable distance between a sample
     * point and the returned path.
     * 
     */
    static void getPath(
            SimplePath path,
            final Point2D.Double[] samplePoints,
            final double fitTolerance, boolean closed) {
        PathGenerator.samplePoints = samplePoints;
        PathGenerator.fitToleranceSq = fitTolerance * fitTolerance;
        int n = samplePoints.length;
        if (n < 2) {
            throw new IllegalArgumentException("Argument samplePoints must contain at least 2 points");
        }
        path.reset();
        path.moveTo(samplePoints[0].x, samplePoints[0].y);
        initialT = initialParametrization();
        fitCurve(path, 0, n - 1);
        if (closed) {
            path.close();
        }
    }

    private static void fitCurve(SimplePath path, final int start, final int end) {
        assert end > start;
        int n = end - start + 1;
        if (n == 2) {
            //number of points == 2.
            //Fit 2 points with a line.
            path.lineTo(samplePoints[end].x, samplePoints[end].y);
            return;
        }
        // Adjust the initial parametrization to the current segment
        double[] t = new double[n];
        t[0] = 0.0;
        double offset = initialT[start];
        double totalTime = initialT[end] - offset;
        for (int i = start; i < end; i++) {
            t[i - start] = (initialT[i] - offset) / totalTime;
        }
        t[n - 1] = 1.0;

        if (n == 3) {
            //number of points == 3.
            //Fit 3 points with a quad curve.
            double t0 = t[1];
            double x0 = samplePoints[start].x;
            double y0 = samplePoints[start].y;
            double x2 = samplePoints[end].x;
            double y2 = samplePoints[end].y;
            double x1 = samplePoints[start + 1].x / (1 - t0) / t0 - (1 - t0) / t0 * x0 - t0 / (1 - t0) * x2;
            x1 /= 2.0;
            double y1 = samplePoints[start + 1].y / (1 - t0) / t0 - (1 - t0) / t0 * y0 - t0 / (1 - t0) * y2;
            y1 /= 2.0;
            path.quadTo(x1, y1, x2, y2);
            return;
        }
        //number of points >= 4
        //Fit 4 or more points with a cubic curve.
        boolean doneFitting = false;
        matrixWxDminusWxTxB03xP03t = new Matrix(getMatrixWxDminusWxTxB03xP03t(t, start, end));
        matrixWxTxB12 = new Matrix(getWxTxB12(t));
        int maxLoop = 1000;
        while (!doneFitting && maxLoop-- > 0) {
            findControlPoints(start, end);
            double improvement = reparametrize(t, start, end);
            if (improvement < 0.0001) {
                doneFitting = true;
            } else {
                matrixWxDminusWxTxB03xP03t = new Matrix(getMatrixWxDminusWxTxB03xP03t(t, start, end));
                matrixWxTxB12 = new Matrix(getWxTxB12(t));
            }
        }



        // Find the point farthest from the curve. If this point is farher than
        // fitToleranceSq from the curve, split the points into two subsets and fit each
        // subset
        int farthestPoint = start;
        double maxDistSq = 0.0;
        for (int i = start + 1; i < end; i++) {
            double distSq = samplePoints[i].distanceSq(position(t[i - start]));
            if (distSq > maxDistSq) {
                maxDistSq = distSq;
                farthestPoint = i;
            }

        }

        if (maxDistSq > fitToleranceSq) {
            fitCurve(path, start, farthestPoint);
            fitCurve(path, farthestPoint, end);
        } else {
            path.curveTo(
                    controlPoints[1][0],
                    controlPoints[1][1],
                    controlPoints[2][0],
                    controlPoints[2][1],
                    controlPoints[3][0],
                    controlPoints[3][1]);
        }

    }

    private static void findControlPoints(int start, int end) {

        matrixX = matrixWxTxB12.solve(matrixWxDminusWxTxB03xP03t);
        double[][] controlPoints12 = matrixX.getArray();
        controlPoints[0][0] = samplePoints[start].x;
        controlPoints[0][1] = samplePoints[start].y;
        controlPoints[1][0] = controlPoints12[0][0];
        controlPoints[1][1] = controlPoints12[0][1];
        controlPoints[2][0] = controlPoints12[1][0];
        controlPoints[2][1] = controlPoints12[1][1];
        controlPoints[3][0] = samplePoints[end].x;
        controlPoints[3][1] = samplePoints[end].y;

        //recompute the position array
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) { //row
                position[j][i] = 0.0;
                for (int k = 0; k < 4; k++) { //column
                    position[j][i] +=
                            bezier3Coeff[j][k] * controlPoints[k][i];
                }
            }
        }
        //recompute the velocity array
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) { //row
                velocity[j][i] = 0.0;
                for (int k = 0; k < 4; k++) { //column
                    velocity[j][i] +=
                            (3 - j) * bezier3Coeff[j][k] * controlPoints[k][i];
                }
            }
        }
    }

    private static double[] initialParametrization() {
        int n = samplePoints.length - 1;
        double[] t = new double[n + 1];
        t[0] = 0.0;
        for (int i = 0; i < n; i++) {
            t[i + 1] = t[i] + samplePoints[i].distance(samplePoints[i + 1]);
        }
        for (int i = 0; i < n; i++) {
            t[i] /= t[n];
        }
        t[n] = 1.0;
        return t;
    }

    private static double reparametrize(final double[] t,
            int currentPoint, int nextPoint) {

        assert t[0] == 0.0;
        assert t[nextPoint - currentPoint] == 1.0;

        double tHat;
        double deltaDistSq;
        Point2D.Double p;
        Point2D.Double v;
        Point2D.Double pHat;
        double improvement = 0.0;
        for (int i = currentPoint + 1; i < nextPoint; i++) {
            double wi = weight(nextPoint - currentPoint + 1, i - currentPoint);
            double wiSq = wi * wi;
            p = position(t[i - currentPoint]);
            v = velocity(t[i - currentPoint]);
            tHat = t[i - currentPoint] + ((samplePoints[i].x - p.x) * v.x +
                    (samplePoints[i].y - p.y) * v.y) / v.distanceSq(0.0, 0.0);
            pHat = position(tHat);
            deltaDistSq = samplePoints[i].distanceSq(p) - samplePoints[i].distanceSq(pHat);
            if (deltaDistSq > 0.0) {
                t[i - currentPoint] = tHat;
                improvement += deltaDistSq * wiSq;
            }
        }
        return improvement;
    }

    private static Point2D.Double position(double t) {
        double x = 0.0;
        double y = 0.0;
        for (int k = 0; k < 4; k++) {
            x *= t;
            x += position[k][0];
        }
        for (int k = 0; k < 4; k++) {
            y *= t;
            y += position[k][1];
        }
        return new Point2D.Double(x, y);
    }

    private static Point2D.Double velocity(double t) {
        double x = 0.0;
        double y = 0.0;
        for (int k = 0; k < 3; k++) {
            x *= t;
            x += velocity[k][0];
        }
        for (int k = 0; k < 3; k++) {
            y *= t;
            y += velocity[k][1];
        }
        return new Point2D.Double(x, y);
    }

    private static double[][] getWxTxB12(double[] t) {
        double[][] a = new double[t.length][2];

        for (int i = 0; i < t.length; i++) {
            double wi = weight(t.length, i);
            for (int j = 0; j < 2; j++) {
                a[i][j] = 0.0;
                for (int k = 0; k < 4; k++) {
                    a[i][j] *= t[i];
                    a[i][j] += bezier3Coeff[k][j + 1];
                }
                a[i][j] *= wi;
            }
        }
        return a;
    }

//    private static double[][] getMatrixWxD(int start, int end) {
//
//        int n = end - start + 1;
//        double[][] a = new double[n][2];
//        for (int i = 0; i < n; i++) {
//            double wi = weight(n, i);
//            a[n][0] = wi * samplePoints[start + i].x;
//            a[n][1] = wi * samplePoints[start + i].y;
//        }
//        return a;
//    }

    private static double[][] getMatrixWxDminusWxTxB03xP03t(double[] t, int start, int end) {
        int n = end - start + 1;
        double[][] b03xP03 = new double[4][2];
        double a[][] = new double[n][2];
        for (int k = 0; k < 4; k++) {
            b03xP03[k][0] = bezier3Coeff[k][0] * samplePoints[start].x +
                    bezier3Coeff[k][3] * samplePoints[end].x;
            b03xP03[k][1] = bezier3Coeff[k][0] * samplePoints[start].y +
                    bezier3Coeff[k][3] * samplePoints[end].y;
        }
        for (int i = 0; i < n; i++) {
            double wi = weight(n, i);
            double txb03P03X = 0;
            double txb03P03Y = 0;
            for (int j = 0; j < 4; j++) {
                txb03P03X *= t[i];
                txb03P03Y *= t[i];
                txb03P03X += b03xP03[j][0];
                txb03P03Y += b03xP03[j][1];
            }
            a[i][0] = wi * (samplePoints[start + i].x - txb03P03X);
            a[i][1] = wi * (samplePoints[start + i].y - txb03P03Y);

        }
        return a;
    }

    private static double weight(final int n, final int i) {
        if (i == 0 || i == n - 1) {
            return 1E10;
        } else {
            double res = (n - 1 - 2.0 * i) / (n - 1);
            return res * res;
        }
    }
}
