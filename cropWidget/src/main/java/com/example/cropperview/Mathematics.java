package com.example.cropperview;

import android.graphics.PointF;

/**
 * Created by Lenovo on 27.09.2016.
 *
 * This class contains math formulas used in this module
 */
class Mathematics {

    /**
     * @param p1 center of first circle
     * @param r1 radius of first circle
     * @param p2 center of second circle
     * @param r2 radius of second circle
     * @return Returns points of intersection of given circles if any;
     */
    public static PointF[] getPointsOfIntersection(PointF p1, float r1, PointF p2, double r2)
    {
        double x0, y0;

        double d;
        double a;
        double h;

        d= Math.sqrt(Math.pow(Math.abs(p1.x - p2.x), 2)
                + Math.pow(Math.abs(p1.y - p2.y), 2));

        if(d > r1+r2) return null;

        a= (r1*r1 - r2*r2 + d*d ) / (2*d);
        h= Math.sqrt(Math.pow(r1, 2) - Math.pow(a, 2));


        x0 = p1.x + a*( p2.x - p1.x ) / d;
        y0 = p1.y + a*( p2.y - p1.y ) / d;

        PointF[] result = new PointF[2];
        double x1= x0 + h*( p2.y - p1.y ) / d;
        double y1= y0 - h*( p2.x - p1.x ) / d;

        result[0] = new PointF((float)x1, (float)y1);
        if(a == r1 ) {
            result[1] = null;
            return result;
        }

        double x2 = x0 - h*( p2.y - p1.y ) / d;
        double y2 = y0 + h*( p2.x - p1.x ) / d;

        result[1] = new PointF((float)x2, (float)y2);

        return result;
    }


    /**
     * @param lineA coordinates of first point on a line
     * @param lineB coordinates of second point on a line
     * @param C point regarding to which we searching the closest point on the line
     * @param isSection is line limited by given points
     * @return Returns the closest point on a line to @param C
     */
    public static PointF findClosestPoint(PointF lineA, PointF lineB, PointF C, boolean isSection){
        double L=(lineA.x-lineB.x)*(lineA.x-lineB.x)+(lineA.y-lineB.y)*(lineA.y-lineB.y);
        double PR=(C.x-lineA.x)*(lineB.x-lineA.x)+(C.y-lineA.y)*(lineB.y-lineA.y);
        double cf=PR/L;
        if(isSection) {
            if(cf<0){ cf=0; return null; }
            if(cf>1){ cf=1; return null; }
        }
        double xres=lineA.x+cf*(lineB.x-lineA.x);
        double yres=lineA.y+cf*(lineB.y-lineA.y);


        return new PointF((float)xres, (float)yres);
    }

    /** Rotates a point around other point
     * @param center point of rotation
     * @param x rotating point
     * @param deg angle in degrees
     * @return Returns rotated point
     */
    public static PointF rotatePoint(PointF center, PointF x, float deg){
        double alpha = Math.toRadians(deg);
        float rx = x.x - center.x;
        float ry = x.y - center.y;
        float c = (float)Math.cos(alpha);
        float s = (float)Math.sin(alpha);
        float x1 = center.x + rx * c - ry * s;
        float y1 = center.y + rx * s + ry * c;
        return new PointF(x1, y1);
    }

    /**
     * @param A1 first line point
     * @param A2 first line point
     * @param B1 second line point
     * @param B2 second line point
     * @return Returns point of intersection of given lines.
     */
    public static PointF findCrossingPoint(PointF A1, PointF A2, PointF B1, PointF B2){

        float koef1 = (A2.y - A1.y)/(A2.x - A1.x);
        float koef2 = (B2.x - B1.x)/(B2.y - B1.y);

        float y3 = (koef1 * koef2 * B1.y - koef1 * B1.x + koef1 * A1.x - A1.y)/(koef1 * koef2 - 1);

        float x3 = koef2 * y3 - koef2 * B1.y + B1.x;

        return new PointF(x3, y3);
    }


    /**
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param х2 x coordinate of second point
     * @param y2 y coordinate of second point
     * @return
     */
    public static float distance(float x1, float y1, float х2, float y2) {

        float dx = x1 - х2;

        float dy = y1 - y2;

        return (float)Math.sqrt(dx*dx + dy*dy);

    }

    /**
     * @return Returns distance between points
     */
    public static float distance(PointF A, PointF B) {
        return distance(A.x, A.y, B.x, B.y);

    }
}
