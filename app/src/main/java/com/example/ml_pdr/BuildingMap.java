package com.example.ml_pdr;

// Define the class representing the floor map
public class BuildingMap {
    private int[][] array;
    private int[] origin;
    private double scale;
    private int floor;

    int[][] getArray() {
        return array;
    }

    int[] getOrigin() {
        return origin;
    }

    double getScale() {
        return scale;
    }

    public int getFloor() {
        return floor;
    }


}
