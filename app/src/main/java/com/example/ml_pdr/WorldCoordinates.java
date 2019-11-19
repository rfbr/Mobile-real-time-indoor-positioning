package com.example.ml_pdr;


import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.List;
// Function to compute the new world coordinates adding the past relative translation and rotation
class WorldCoordinates {
    static List<double[]> compute(double[] pastCoordinates, double[] pastQuaternion, float[] translation, float[] quaternion) {
        double[] t = convertFloatsToDoubles(translation);
        Vector3D mPastCoordinates = new Vector3D(pastCoordinates);
        Vector3D mTranslation = new Vector3D(t);
        Rotation q = new Rotation(pastQuaternion[0], pastQuaternion[1], pastQuaternion[2], pastQuaternion[3], false);
        Vector3D newCoordinates = mPastCoordinates.add(q.applyTo(mTranslation));
        Quaternion mQ = new Quaternion(pastQuaternion[0], pastQuaternion[1], pastQuaternion[2], pastQuaternion[3]);
        Quaternion newQ = mQ.multiply(new Quaternion(quaternion[0], quaternion[1], quaternion[2], quaternion[3]).normalize());
        double[] newQuaternion = {newQ.getQ0(), newQ.getQ1(), newQ.getQ2(), newQ.getQ3()};
        return  Arrays.asList(newCoordinates.toArray(), newQuaternion);
    }

    private static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null;
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }
}
