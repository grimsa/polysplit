package de.incentergy.geometry.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

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
        public void projectionForIntersectingPerpendicularEdges() throws Exception {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 1), new Coordinate(0, 10));       // vertical   edge at x = 0, when y = [1; 10]
            LineSegment edgeB = new LineSegment(new Coordinate(2, 0), new Coordinate(7, 0));        // horizontal edge at y = 0, when x = [2; 7]

            Coordinate intersection = new Coordinate(0, 0);

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

    }
}
