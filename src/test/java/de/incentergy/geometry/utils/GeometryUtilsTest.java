package de.incentergy.geometry.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import de.incentergy.geometry.utils.GeometryUtils.IntersectionCoordinate;

@RunWith(Enclosed.class)
public class GeometryUtilsTest {
    private static final double EXACT_PRECISION = 0;

    public static class GetIntersectionTest {

        @Test
        public void nonTouchingEdges() throws Exception {
            LineSegment lineA = new LineSegment(0, 10, 1, 9);      // 45 degree angle line downwards
            LineSegment lineB = new LineSegment(0, 0, 100, 0);     // horizontal line at y = 0

            Coordinate intersection = GeometryUtils.getIntersectionPoint(lineA, lineB);
            assertEquals(10, intersection.x, EXACT_PRECISION);
            assertEquals(0, intersection.y, EXACT_PRECISION);
        }

        @Test
        public void touchingEdges() throws Exception {
            LineSegment lineA = new LineSegment(10, 1, 10, 0);     // vertical line at x = 10
            LineSegment lineB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0

            Coordinate intersection = GeometryUtils.getIntersectionPoint(lineA, lineB);
            assertEquals(10, intersection.x, EXACT_PRECISION);
            assertEquals(0, intersection.y, EXACT_PRECISION);
        }

        @Test
        public void overlappingEdges() throws Exception {
            LineSegment lineA = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0
            LineSegment lineB = new LineSegment(-1, 0, 5, 0);      // horizontal line at y = 0

            Coordinate intersection = GeometryUtils.getIntersectionPoint(lineA, lineB);
            assertNull(intersection);
        }

        @Test
        public void parallelEdges() throws Exception {
            LineSegment lineA = new LineSegment(0, 1, 10, 1);      // horizontal line at y = 1
            LineSegment lineB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0

            Coordinate intersection = GeometryUtils.getIntersectionPoint(lineA, lineB);
            assertNull(intersection);
        }
    }

    public static class GetProjectedPointTest {

        @Test
        public void projectionForEdgesFormingARectangle() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 0), new Coordinate(10, 0));       // horizontal edge at y = 0, when x = [0; 10]
            LineSegment edgeB = new LineSegment(new Coordinate(0, 1), new Coordinate(10, 1));       // horizontal edge at y = 1, when x = [0; 10]

            Coordinate projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p0, edgeA, null);
            assertNull(projectedPoint);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p1, edgeA, null);
            assertNull(projectedPoint);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p0, edgeB, null);
            assertNull(projectedPoint);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p1, edgeB, null);
            assertNull(projectedPoint);
        }

        @Test
        public void projectionForEdgesFormingATrapezoid() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 0), new Coordinate(10, 0));       // horizontal edge at y = 0, when x = [0; 10]
            LineSegment edgeB = new LineSegment(new Coordinate(5, 1), new Coordinate(15, 1));       // horizontal edge at y = 1, when x = [5; 15]

            Coordinate projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p0, edgeA, null);     // project point (5; 1) onto y = 0
            assertEquals(5, projectedPoint.x, EXACT_PRECISION);
            assertEquals(0, projectedPoint.y, EXACT_PRECISION);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p1, edgeA, null);                // project point (15; 1) onto y = 0
            assertNull(projectedPoint);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p0, edgeB, null);                // project point (0; 0) onto y = 1
            assertNull(projectedPoint);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p1, edgeB, null);                // project point (10; 0) onto y = 1
            assertEquals(10, projectedPoint.x, EXACT_PRECISION);
            assertEquals(1, projectedPoint.y, EXACT_PRECISION);
        }

        @Test
        public void projectionForPerpendicularEdges() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 1), new Coordinate(0, 10));       // vertical   edge at x = 0, when y = [1; 10]
            LineSegment edgeB = new LineSegment(new Coordinate(2, 0), new Coordinate(7, 0));        // horizontal edge at y = 0, when x = [2; 7]

            IntersectionCoordinate intersection = new IntersectionCoordinate(0, 0, edgeA, edgeB);

            Coordinate projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p0, edgeA, intersection);     // project point (2; 0) onto y axis
            assertEquals(0, projectedPoint.x, EXACT_PRECISION);
            assertEquals(2, projectedPoint.y, EXACT_PRECISION);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p1, edgeA, intersection);                // project point (7; 0) onto y axis
            assertEquals(0, projectedPoint.x, EXACT_PRECISION);
            assertEquals(7, projectedPoint.y, EXACT_PRECISION);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p0, edgeB, intersection);                // project point (0; 1) onto x axis
            assertNull(projectedPoint);
            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p1, edgeB, intersection);                // project point (0; 10) onto x axis
            assertNull(projectedPoint);
        }

        @Test
        public void projectionForIntersectingEdges() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 0), new Coordinate(0, 40));       // vertical edge at x = 0, when y = [0; 40]
            LineSegment edgeB = new LineSegment(new Coordinate(30, 10), new Coordinate(10, 20));    // sloping edge that would intersect edgeA if extended

            IntersectionCoordinate intersection = new IntersectionCoordinate(0, 25, edgeA, edgeB);

            Coordinate projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p0, edgeA, intersection);     // project point (30; 10) onto vertical edge
            assertNull(projectedPoint);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p1, edgeA, intersection);                // project point (10; 20) onto vertical edge
            assertEquals(0, projectedPoint.x, EXACT_PRECISION);
            assertEquals(13.819660112501051, projectedPoint.y, EXACT_PRECISION);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p0, edgeB, intersection);                // project point (0; 0) onto sloping edge
            assertEquals(22.360679774997898, projectedPoint.x, EXACT_PRECISION);
            assertEquals(13.819660112501051, projectedPoint.y, EXACT_PRECISION);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p1, edgeB, intersection);                // project point (0; 20) onto sloping edge
            assertNull(projectedPoint);
        }

        @Test
        public void projectionForPerpendicularIntersectingEdges() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 0), new Coordinate(0, 20));               // vertical   edge at x = 0, when y = [0; 20]
            LineSegment edgeB = new LineSegment(new Coordinate(5, 10), new Coordinate(15, 10));             // horizontal edge at y = 10 when x = [5; 15]

            IntersectionCoordinate intersection = new IntersectionCoordinate(0, 10, edgeA, edgeB);

            Coordinate projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p0, edgeA, intersection);     // project point (5; 10) onto vertical edge
            assertEquals(0, projectedPoint.x, EXACT_PRECISION);
            assertEquals(15, projectedPoint.y, EXACT_PRECISION);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeB.p1, edgeA, intersection);                // project point (15; 10) onto vertical edge
            assertNull(projectedPoint);

            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p0, edgeB, intersection);                // project point (0; 0) onto horizontal edge
            assertNull(projectedPoint);

            // TODO: need to handle this better
            projectedPoint = GeometryUtils.getProjectedPoint(edgeA.p1, edgeB, intersection);                // project point (0; 20) onto horizontal edge
            assertEquals(10, projectedPoint.x, EXACT_PRECISION);
            assertEquals(10, projectedPoint.y, EXACT_PRECISION);
        }
    }

    public static class GetLineSegmentTest {

        @Test
        public void testGetLineSegment() throws Exception {
            LineString string = (LineString) new WKTReader().read("LINESTRING(0 1, 2 3, 4 5, 6 7)");

            LineSegment result = GeometryUtils.getLineSegment(string, 0);
            assertEquals(0, result.p0.x, EXACT_PRECISION);
            assertEquals(1, result.p0.y, EXACT_PRECISION);
            assertEquals(2, result.p1.x, EXACT_PRECISION);
            assertEquals(3, result.p1.y, EXACT_PRECISION);

            result = GeometryUtils.getLineSegment(string, 1);
            assertEquals(2, result.p0.x, EXACT_PRECISION);
            assertEquals(3, result.p0.y, EXACT_PRECISION);
            assertEquals(4, result.p1.x, EXACT_PRECISION);
            assertEquals(5, result.p1.y, EXACT_PRECISION);
        }

        @Test
        public void testGetLineSegments() throws Exception {
            LineString string = (LineString) new WKTReader().read("LINESTRING(0 1, 2 3, 4 5, 6 7)");

            List<LineSegment> result = GeometryUtils.getLineSegments(string);
            assertEquals(3, result.size());
            assertEquals("LINESTRING( 0.0 1.0, 2.0 3.0)", result.get(0).toString());
            assertEquals("LINESTRING( 2.0 3.0, 4.0 5.0)", result.get(1).toString());
            assertEquals("LINESTRING( 4.0 5.0, 6.0 7.0)", result.get(2).toString());
        }
    }

    public static class IsIntersectingPolygonTest {

        @Test
        public void touchesEdgesReturnsFalse() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 10, 50 0, 100 0, 90 50, 10 50, 0 0))");

            assertFalse(GeometryUtils.isIntersectingPolygon(new LineSegment(new Coordinate(60, 0), new Coordinate(90, 50)), polygon));
            assertFalse(GeometryUtils.isIntersectingPolygon(new LineSegment(new Coordinate(0, 0), new Coordinate(75, 50)), polygon));
        }

        @Test
        public void crossingEdgeReturnsTrue() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 10, 50 0, 100 0, 90 50, 10 50, 0 0))");

            assertTrue(GeometryUtils.isIntersectingPolygon(new LineSegment(new Coordinate(0, 0), new Coordinate(100, 100)), polygon));
            assertTrue(GeometryUtils.isIntersectingPolygon(new LineSegment(new Coordinate(0, 0), new Coordinate(60, 10)), polygon));
        }
    }

    public static class IsPointOnLineSegmentTest {

        @Test
        public void includingEndpoints() throws Exception {
            Coordinate startPoint = new Coordinate(0, 0);
            Coordinate endPoint = new Coordinate(100, 200);
            LineSegment line = new LineSegment(startPoint, endPoint);

            Coordinate pointOnLine = new Coordinate(20, 40);
            Coordinate pointOnExtendedLine = new Coordinate(120, 140);
            Coordinate pointAdjacentToLine = new Coordinate(21, 40);

            assertTrue(GeometryUtils.isPointOnLineSegment(startPoint, line));
            assertTrue(GeometryUtils.isPointOnLineSegment(endPoint, line));
            assertTrue(GeometryUtils.isPointOnLineSegment(pointOnLine, line));

            assertFalse(GeometryUtils.isPointOnLineSegment(pointOnExtendedLine, line));
            assertFalse(GeometryUtils.isPointOnLineSegment(pointAdjacentToLine, line));
        }

        @Test
        public void excludingEndpoints() throws Exception {
            Coordinate startPoint = new Coordinate(0, 0);
            Coordinate endPoint = new Coordinate(100, 200);
            LineSegment line = new LineSegment(startPoint, endPoint);

            Coordinate pointOnLine = new Coordinate(20, 40);
            Coordinate pointOnExtendedLine = new Coordinate(120, 140);
            Coordinate pointAdjacentToLine = new Coordinate(21, 40);

            assertFalse(GeometryUtils.isPointOnLineSegmentExcludingEndpoints(startPoint, line));
            assertFalse(GeometryUtils.isPointOnLineSegmentExcludingEndpoints(endPoint, line));
            assertTrue(GeometryUtils.isPointOnLineSegmentExcludingEndpoints(pointOnLine, line));

            assertFalse(GeometryUtils.isPointOnLineSegmentExcludingEndpoints(pointOnExtendedLine, line));
            assertFalse(GeometryUtils.isPointOnLineSegmentExcludingEndpoints(pointAdjacentToLine, line));
        }
    }
}
