package com.example.ml_pdr;

class MapMatching {
    // Check if there is a wall between 2 given points on a map
    static boolean check(double[] pastPosition, double[] position, BuildingMap mapp) {
        int[][] map = mapp.getArray();
        double scale = mapp.getScale();
        int[] origin = mapp.getOrigin();

        int pix_x1 = (int) (-Math.round(pastPosition[0] / scale) + origin[0]);
        int pix_y1 = (int) (Math.round(pastPosition[1] / scale) + origin[1]);

        int pix_x2 = (int) (-Math.round(position[0] / scale) + origin[0]);
        int pix_y2 = (int) (Math.round(position[1] / scale) + origin[1]);
        boolean wall = false;
        int x = pix_x1;
        int y = pix_y1;
        if (Math.abs(pix_x1 - pix_x2) < Math.abs(pix_y1 - pix_y2)) {
            while (x != pix_x2 || y != pix_y2) {
                if (y < pix_y2) {
                    y += 1;
                }
                if (y > pix_y2) {
                    y -= 1;
                }
                wall = (map[x][y] == 1);
                if (wall) break;
                if (x < pix_x2) {
                    x += 1;
                }
                if (x > pix_x2) {
                    x -= 1;
                }
                wall = (map[x][y] == 1);
                if (wall) break;
            }
        } else {
            while (x != pix_x2 || y != pix_y2) {
                if (x < pix_x2) {
                    x += 1;
                }
                if (x > pix_x2) {
                    x -= 1;
                }
                wall = (map[x][y] == 1);
                if (wall) break;

                if (y < pix_y2) {
                    y += 1;
                }
                if (y > pix_y2) {
                    y -= 1;
                }
                wall = (map[x][y] == 1);
                if (wall) break;

            }
        }
        return wall;
    }

    // Given a position, check if the pedestrian is on a zone where he can change floor
    static boolean checkStairs(double[] position, BuildingMap mapp) {

        int[][] map = mapp.getArray();
        double scale = mapp.getScale();
        int[] origin = mapp.getOrigin();

        int pix_x = (int) (-Math.round(position[0] / scale) + origin[0]);
        int pix_y = (int) (Math.round(position[1] / scale) + origin[1]);
        return (map[pix_x][pix_y] == 2);
    }
}