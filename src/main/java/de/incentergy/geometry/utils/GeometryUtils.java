package de.incentergy.geometry.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public final class GeometryUtils {

    private GeometryUtils() {
    }

    /**
     * Computes the intersection between two lines extending to infinity in both directions
     *
     * @param lineA
     * @param lineB
     * @return a point where the lines intersect, or null if they don't (are parallel or overlap)
     *
     * @see https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
     */
    public static Coordinate getIntersectionPoint(LineSegment lineA, LineSegment lineB) {
        double x1 = lineA.p0.x;
        double y1 = lineA.p0.y;
        double x2 = lineA.p1.x;
        double y2 = lineA.p1.y;

        double x3 = lineB.p0.x;
        double y3 = lineB.p0.y;
        double x4 = lineB.p1.x;
        double y4 = lineB.p1.y;

        double det1And2 = det(x1, y1, x2, y2);
        double det3And4 = det(x3, y3, x4, y4);
        double x1LessX2 = x1 - x2;
        double y1LessY2 = y1 - y2;
        double x3LessX4 = x3 - x4;
        double y3LessY4 = y3 - y4;

        double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
        if (det1Less2And3Less4 == 0) {
            return null;
        }

        double x = det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4;
        double y = det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4;
        return new Coordinate(x, y);
    }

    private static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    /**
     * Determines a projection of vertex on opposing edge at an angle perpendicular to angle-bisector of the edges
     * @param vertex
     * @param opposingEdge
     * @param intersectionPoint
     * @return
     */
    public static Coordinate getProjectedPoint(Coordinate vertex, LineSegment opposingEdge, Coordinate intersectionPoint) {
        if (intersectionPoint != null) {
            // This is based on the fact, that the projection perpendicular to the angle bisector
            //     will be located an equal distance from intersection point

            double distanceOfVertex = vertex.distance(intersectionPoint);

            // check if the point falls on the edge. I.e. distance from intersection must be between distances of start and end points
            double distOfOpEdgeVertex1 = intersectionPoint.distance(opposingEdge.p0);
            double distOfOpEdgeVertex2 = intersectionPoint.distance(opposingEdge.p1);

            if (distanceOfVertex >= Math.max(distOfOpEdgeVertex1, distOfOpEdgeVertex2) || distanceOfVertex <= Math.min(distOfOpEdgeVertex1, distOfOpEdgeVertex2)) {
                // the projection falls outside of the opposing edge - ignore it
                // This also covers cases when projected point matches the vertex
                return null;
            }

            // determine a point along the opposing edge for which distance from intersection point is equal to that of vertex being projected
            Coordinate furtherPoint = getFurtherEnd(intersectionPoint, opposingEdge);
            LineSegment extendedOpposingEdge = new LineSegment(intersectionPoint, furtherPoint);
            return extendedOpposingEdge.pointAlong(distanceOfVertex / extendedOpposingEdge.getLength());
        } else {
            // In case of parallel lines, we do not have an intersection point
            Coordinate closestPointOnOpposingLine = opposingEdge.project(vertex);       // a projection onto opposingEdge (extending to infinity)
            return isPointOnLineSegment(closestPointOnOpposingLine, opposingEdge) ? closestPointOnOpposingLine : null;
        }
    }

    /**
     * Returns {@link LineSegment} vertex that is further from given point
     * @param point a point
     * @param lineSegment
     * @return
     */
    private static Coordinate getFurtherEnd(Coordinate point, LineSegment lineSegment) {
        return point.distance(lineSegment.p0) > point.distance(lineSegment.p1) ? lineSegment.p0 : lineSegment.p1;
    }

    /**
     * Checks if the point is located on the given {@link LineSegment}
     */
    public static boolean isPointOnLineSegment(Coordinate point, LineSegment line) {
        // check if the point falls on the opposingEdge (distance from both ends is less than the length of the edge)
        double lengthOfLine = line.getLength();
        double distFromEnd1 = point.distance(line.p0);
        double distFromEnd2 = point.distance(line.p1);

        return distFromEnd1 < lengthOfLine && distFromEnd2 < lengthOfLine;
    }

}