package de.incentergy.geometry.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

public final class GeometryFactoryUtils {

    private GeometryFactoryUtils() {
    }

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Creates a triangle polygon with vertices in the provided order
     * @return
     */
    public static Polygon createTriangle(Coordinate vertex1, Coordinate vertex2, Coordinate vertex3) {
        Coordinate[] coordinates = new Coordinate[4];
        coordinates[0] = vertex1;
        coordinates[1] = vertex2;
        coordinates[2] = vertex3;
        coordinates[3] = coordinates[0];
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    /**
     * Creates a polygon with vertices in the provided order
     * @return
     */
    public static Polygon createPolygon(Coordinate... vertices) {
        if (vertices.length < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertices!");
        }

        Coordinate[] coordinates = new Coordinate[vertices.length + 1];
        for (int i = 0; i < vertices.length; i++) {
            coordinates[i] = vertices[i];
        }
        coordinates[vertices.length] = coordinates[0];          // close the polygon
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    /**
     * Creates a polygon from an existing one, starting at start point, traversing along the exterior ring until end point is reached and then
     * connecting it to start point to close the polygon.<br>
     * Note: possibly could be simplified by using {@link Polygonizer}
     *
     * @param polygonToSplit
     * @param startVertex a vertex of the exterior ring where to start
     * @param endVertex a vertex of the exterior ring where to end
     * @return
     */
    public static Polygon getSubpolygon(Polygon polygonToSplit, Coordinate startVertex, Coordinate endVertex) {
        Objects.requireNonNull(polygonToSplit, "Polygon must be provided");
        Objects.requireNonNull(startVertex, "Start coordinate must be provided");
        Objects.requireNonNull(endVertex, "End coordinate must be provided");

        Coordinate[] exteriorRing = polygonToSplit.getExteriorRing().getCoordinates();
        if (!Arrays.stream(exteriorRing).anyMatch(startVertex::equals) || !Arrays.stream(exteriorRing).anyMatch(endVertex::equals)) {
            throw new IllegalArgumentException("Start or end vertex is not one of the edges of polygon");
        }

        List<Coordinate> vertices = new ArrayList<>();
        boolean started = false;

        for (int i = 0; i < exteriorRing.length; i++) {
            Coordinate coord = exteriorRing[i];
            if (coord.equals(startVertex)) {
                started = true;                 // from here on, add all points until ending point is reached
            }
            if (started) {
                vertices.add(coord);

                if (coord.equals(endVertex)) {
                    break;                          // polygon will be closed by #createPolygon method
                }
            }
            if (i == exteriorRing.length - 1) { // if the last segment is reached and it is still not the end
                i = 0;                          // this will continue iteration from i = 1 (due to loop increment). This is ok, as the first point is the same as the last one
            }
        }
        return createPolygon(vertices.toArray(new Coordinate[vertices.size()]));
    }

    /**
     * Creates a polygon from an existing one, starting at start point, traversing along the exterior ring until end point is reached and then
     * connecting it to start point to close the polygon.<br>
     * The difference from {@link #getSubpolygon(Polygon, Coordinate, Coordinate)} is that for this method start and end points can not only be vertices,
     * but also anywhere along the edges.<br>
     * Note: possibly could be simplified by using {@link Polygonizer}
     *
     * @param polygonToSlice
     * @param startPoint a point along the exterior ring where to start
     * @param endPoint a point along the exterior ring where to end
     * @return
     */
    public static Polygon slicePolygon(Polygon polygonToSlice, Coordinate startPoint, Coordinate endPoint) {
        Objects.requireNonNull(polygonToSlice, "Polygon must be provided");
        Objects.requireNonNull(startPoint, "Start coordinate must be provided");
        Objects.requireNonNull(endPoint, "End coordinate must be provided");

        List<Coordinate> vertices = new ArrayList<>();

        boolean started = false;
        boolean finished = false;

        List<LineSegment> edges = GeometryUtils.getLineSegments(polygonToSlice.getExteriorRing());
        for (LineSegment edge : edges) {
            if (!started && GeometryUtils.isPointOnLineSegment(startPoint, edge) && !startPoint.equals(edge.p1)) {
                // if startPoint is on the edge, start building up the sliced part
                // if it is the endpoint, it will be considered as part of the next edge
                vertices.add(startPoint);
                started = true;
                continue;
            }

            if (started) {
                vertices.add(edge.p0);

                if (GeometryUtils.isPointOnLineSegment(endPoint, edge)) {
                    vertices.add(endPoint);
                    finished = true;
                    break;
                }
            }
        }

        if (started && !finished) {
            // polygon runs through the first point - continue until endPoint is reached

            for (LineSegment edge : edges) {
                vertices.add(edge.p0);
                if (GeometryUtils.isPointOnLineSegment(endPoint, edge)) {
                    vertices.add(endPoint);
                    finished = true;
                    break;
                }
            }
        }

        return createPolygon(vertices.toArray(new Coordinate[vertices.size()]));
    }
}
