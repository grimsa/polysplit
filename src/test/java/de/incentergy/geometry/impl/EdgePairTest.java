package de.incentergy.geometry.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import de.incentergy.geometry.impl.EdgePair.EdgePairSubpolygons;
import de.incentergy.geometry.utils.GeometryFactoryUtils;

public class EdgePairTest {
    private static final double EXACT_PRECISION = 0;

    @Test
    public void testRectangle() throws Exception {
        // Rectangle: (0; 0), (10; 5)
        LineSegment edgeA = new LineSegment(10, 5, 0, 5);      // horizontal line at y = 5, when x = [0; 10]
        LineSegment edgeB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0, when x = [0; 10]

        EdgePair edgePair = new EdgePair(edgeA, edgeB);
        EdgePairSubpolygons subpolygons = edgePair.getSubpolygons();

        assertFalse(subpolygons.hasTriangle1());
        assertFalse(subpolygons.hasTriangle2());

        // check total area
        Polygon expectedPolygon = GeometryFactoryUtils.createPolygon(new Coordinate(0, 5), new Coordinate(10, 5), new Coordinate(10, 0), new Coordinate(0, 0));
        assertEquals(expectedPolygon.getArea(), subpolygons.getArea(), EXACT_PRECISION);
        assertTrue(subpolygons.getTrapezoid().isRectangle());
    }

    @Test
    public void testTrapezoid() throws Exception {
        LineSegment edgeA = new LineSegment(15, 5, 3, 5);      // horizontal line at y = 5, when x = [3; 15]
        LineSegment edgeB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0, when x = [0; 10]

        EdgePair edgePair = new EdgePair(edgeA, edgeB);
        EdgePairSubpolygons subpolygons = edgePair.getSubpolygons();

        // check triangle 1
        Polygon expectedTriangle1 = GeometryFactoryUtils.createTriangle(new Coordinate(0, 0), new Coordinate(3, 0), new Coordinate(3, 5));
        assertTrue(subpolygons.hasTriangle1());
        assertTrue(expectedTriangle1.equalsNorm(subpolygons.getTriangle1()));

        // check triangle 2
        Polygon expectedTriangle2 = GeometryFactoryUtils.createTriangle(new Coordinate(10, 0), new Coordinate(10, 5), new Coordinate(15, 5));
        assertTrue(subpolygons.hasTriangle2());
        assertTrue(expectedTriangle2.equalsNorm(subpolygons.getTriangle2()));

        // check trapezoid
        Polygon expectedTrapezoid = GeometryFactoryUtils.createPolygon(new Coordinate(3, 0), new Coordinate(3, 5), new Coordinate(10, 5), new Coordinate(10, 0));
        Polygon trapezoid = subpolygons.getTrapezoid();
        assertTrue(trapezoid.isRectangle());
        assertTrue(expectedTrapezoid.equalsNorm(subpolygons.getTrapezoid()));

        // check total area
        Polygon expectedPolygon = GeometryFactoryUtils.createPolygon(new Coordinate(3, 5), new Coordinate(15, 5), new Coordinate(10, 0), new Coordinate(0, 0));
        assertEquals(expectedPolygon.getArea(), subpolygons.getArea(), EXACT_PRECISION);
    }
}
