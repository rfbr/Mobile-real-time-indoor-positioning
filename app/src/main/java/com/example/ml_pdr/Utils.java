package com.example.ml_pdr;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;

import java.util.Arrays;
import java.util.List;
// Define some functions used in the main file
class Utils {
    static double[] averageByColumn(float[][] array) {
        double x_sum = 0d;
        double y_sum = 0d;
        double z_sum = 0d;
        double n = array.length;
        for (float[] floats : array) {
            x_sum += floats[0];
            y_sum += floats[1];
            z_sum += floats[2];
        }
        return new double[]{x_sum / n, y_sum / n, z_sum / n};
    }
    static float[] floatAverageByColumn(float[][] array) {
        float x_sum = 0;
        float y_sum = 0;
        float z_sum = 0;
        float n = array.length;
        for (float[] floats : array) {
            x_sum += floats[0];
            y_sum += floats[1];
            z_sum += floats[2];
        }
        return new float[]{x_sum / n, y_sum / n, z_sum / n};
    }
    static double[] averageByColumn(double[][] array) {
        double x_sum = 0d;
        double y_sum = 0d;
        double z_sum = 0d;
        double n = array.length;
        for (double[] doubles : array) {
            x_sum += doubles[0];
            y_sum += doubles[1];
            z_sum += doubles[2];
        }
        return new double[]{x_sum / n, y_sum / n, z_sum / n};
    }

    static double sum(double[] array) {
        double s = 0d;
        for (double value : array) {
            s += value;
        }
        return s;
    }

    static double[][] makeClone(double[][] in) {
        int n = in.length;
        double[][] out = new double[n][in[0].length];
        for (int i = 0; i < n; i++) {
            out[i] = in[i].clone();
        }
        return out;
    }
    static double[] quaternionNorm(double[][] particles) {
        Array2DRowRealMatrix A = new Array2DRowRealMatrix(new double[4][4]);
        double sum = 0;
        int n = particles.length;
        for (double[] particle : particles) {
            ArrayRealVector q = new ArrayRealVector(Arrays.copyOfRange(particle, 6, 10));
            A = A.add((Array2DRowRealMatrix) q.outerProduct(q));
        }
        A = (Array2DRowRealMatrix)A.scalarMultiply(1./n);
        return new EigenDecomposition(A).getEigenvector(0).toArray();
    }
}
