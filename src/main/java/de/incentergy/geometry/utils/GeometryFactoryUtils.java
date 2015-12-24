package de.incentergy.geometry.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
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
     * @param polygonToSplit
     * @param start a point on the exterior ring where to start
     * @param end a point on the exterior ring where to end
     * @return
     */
    public static Polygon slicePolygon(Polygon polygonToSplit, Coordinate start, Coordinate end) {
        Objects.requireNonNull(polygonToSplit, "Polygon must be provided");
        Objects.requireNonNull(start, "Start coordinate must be provided");
        Objects.requireNonNull(end, "End coordinate must be provided");
        // TODO: validate start and end points are vertices of polygon

        List<Coordinate> vertices = new ArrayList<>();

        boolean started = false;

        Coordinate[] exteriorRing = polygonToSplit.getExteriorRing().getCoordinates();
        for (int i = 0; i < exteriorRing.length; i++) {
            Coordinate coord = exteriorRing[i];
            if (coord.equals(start)) {
                started = true;                 // from here on, add all points until ending point is reached
            }
            if (started) {
                vertices.add(coord);
            }
            if (started && coord.equals(end)) {
                break;                          // polygon will be closed by #createPolygon method
            }
            if (i == exteriorRing.length - 1) { // if the last segment is reached and it is still not the end
                i = 0;                          // this will continue iteration from i = 1 (due to loop increment). This is ok, as the first point is the same as the last one
            }
        }
        return createPolygon(vertices.toArray(new Coordinate[vertices.size()]));
    }
}
