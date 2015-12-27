package de.incentergy.geometry.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import de.incentergy.geometry.impl.EdgePair.EdgePairSubpolygons;
import de.incentergy.geometry.utils.GeometryFactoryUtils;

@RunWith(Enclosed.class)
public class EdgePairTest {
    private static final double EXACT_PRECISION = 0;
    private static final double SMALL_DELTA_PRECISION = 1e-10;

    public static class GetSubpolygonsTest {

        @Test
        public void rectangleCase() throws Exception {
            // Rectangle: (0; 0), (10; 5)
            LineSegment edgeA = new LineSegment(10, 5, 0, 5);      // horizontal line at y = 5, when x = [0; 10]
            LineSegment edgeB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0, when x = [0; 10]

            EdgePair edgePair = new EdgePair(edgeA, edgeB);
            EdgePairSubpolygons subpolygons = edgePair.getSubpolygons();

            assertNull(subpolygons.getTriangle1());
            assertNull(subpolygons.getTriangle2());

            // check total area
            Polygon expectedPolygon = GeometryFactoryUtils.createPolygon(new Coordinate(0, 5), new Coordinate(10, 5), new Coordinate(10, 0), new Coordinate(0, 0));
            assertEquals(expectedPolygon.getArea(), subpolygons.getTotalArea(), EXACT_PRECISION);
            assertTrue(subpolygons.getTrapezoid().isRectangle());
        }

        @Test
        public void trapezoidCase() throws Exception {
            LineSegment edgeA = new LineSegment(15, 5, 3, 5);      // horizontal line at y = 5, when x = [3; 15]
            LineSegment edgeB = new LineSegment(0, 0, 10, 0);      // horizontal line at y = 0, when x = [0; 10]

            EdgePair edgePair = new EdgePair(edgeA, edgeB);
            EdgePairSubpolygons subpolygons = edgePair.getSubpolygons();

            // check triangle 1
            Polygon expectedTriangle1 = GeometryFactoryUtils.createTriangle(new Coordinate(0, 0), new Coordinate(3, 0), new Coordinate(3, 5));
            assertNotNull(subpolygons.getTriangle1());
            assertTrue(expectedTriangle1.equalsNorm(subpolygons.getTriangle1()));

            // check triangle 2
            Polygon expectedTriangle2 = GeometryFactoryUtils.createTriangle(new Coordinate(10, 0), new Coordinate(10, 5), new Coordinate(15, 5));
            assertNotNull(subpolygons.getTriangle2());
            assertTrue(expectedTriangle2.equalsNorm(subpolygons.getTriangle2()));

            // check trapezoid
            Polygon expectedTrapezoid = GeometryFactoryUtils.createPolygon(new Coordinate(3, 0), new Coordinate(3, 5), new Coordinate(10, 5), new Coordinate(10, 0));
            Polygon trapezoid = subpolygons.getTrapezoid();
            assertTrue(trapezoid.isRectangle());
            assertTrue(expectedTrapezoid.equalsNorm(subpolygons.getTrapezoid()));

            // check total area
            Polygon expectedPolygon = GeometryFactoryUtils.createPolygon(new Coordinate(3, 5), new Coordinate(15, 5), new Coordinate(10, 0), new Coordinate(0, 0));
            assertEquals(expectedPolygon.getArea(), subpolygons.getTotalArea(), EXACT_PRECISION);
        }
    }

    public static class GetCutsTest {

        // Test cases for edges with no adjacent area on one side

        @Test
        public void cutInTriangle1() throws Exception {
            WKTReader wktReader = new WKTReader();

            // Given: trapezoid with parallel edges (areas of triangle1, rectangle, triangle2 are: 1000, 6000, 1000)
            Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");

            LineSegment edgeA = new LineSegment(new Coordinate(0, 100), new Coordinate(80, 100));
            LineSegment edgeB = new LineSegment(new Coordinate(100, 0), new Coordinate(20, 0));
            EdgePair p = new EdgePair(edgeA, edgeB);

            // expected
            int expectedNumberOfCuts = 1;
            double areaToCutOff = 250;
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCutAwayPolygon = (Polygon) wktReader.read("POLYGON ((80 100, 100 0, 95 0, 80 100))");

            // assert
            List<Cut> cuts = p.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals(expectedNumberOfCuts, cuts.size());
            Cut cut = cuts.get(0);
            assertEquals(expectedLengthOfCut, cut.getLength(), EXACT_PRECISION);
            assertEquals(expectedCutAwayPolygon, cut.getCutAway());
            assertEquals(areaToCutOff, cut.getCutAway().getArea(), EXACT_PRECISION);
        }

        @Test
        public void cutInRectangle() throws Exception {
            WKTReader wktReader = new WKTReader();

            // Given: trapezoid with parallel edges (areas of triangle1, rectangle, triangle2 are: 1000, 6000, 1000)
            Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");

            LineSegment edgeA = new LineSegment(new Coordinate(0, 100), new Coordinate(80, 100));
            LineSegment edgeB = new LineSegment(new Coordinate(100, 0), new Coordinate(20, 0));
            EdgePair p = new EdgePair(edgeA, edgeB);

            // expected
            double areaToCutOff = 1234;             // 1000 in the triangle + 234 in rectangle
            int expectedNumberOfCuts = 1;
            double expectedLengthOfCut = 100;
            Polygon expectedCutAwayPolygon = (Polygon) wktReader.read("POLYGON ((77.66 100, 80 100, 100 0, 77.66 0, 77.66 100))");

            // assert
            List<Cut> cuts = p.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals(expectedNumberOfCuts, cuts.size());
            Cut cut = cuts.get(0);
            assertEquals(expectedLengthOfCut, cut.getLength(), EXACT_PRECISION);
            assertEquals(expectedCutAwayPolygon, cut.getCutAway());
            assertEquals(areaToCutOff, cut.getCutAway().getArea(), SMALL_DELTA_PRECISION);
        }

        @Test
        public void cutInTriangle2() throws Exception {
            WKTReader wktReader = new WKTReader();

            // Given: trapezoid with parallel edges (areas of triangle1, rectangle, triangle2 are: 1000, 6000, 1000)
            Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");

            LineSegment edgeA = new LineSegment(new Coordinate(0, 100), new Coordinate(80, 100));
            LineSegment edgeB = new LineSegment(new Coordinate(100, 0), new Coordinate(20, 0));
            EdgePair p = new EdgePair(edgeA, edgeB);

            // expected
            int expectedNumberOfCuts = 1;
            double areaToCutOff = 7750;         // all minus 250 units
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCutAwayPolygon = (Polygon) wktReader.read("POLYGON ((5 100, 80 100, 100 0, 20 0, 5 100))");

            // assert
            List<Cut> cuts = p.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals(expectedNumberOfCuts, cuts.size());
            Cut cut = cuts.get(0);
            assertEquals(expectedLengthOfCut, cut.getLength(), EXACT_PRECISION);
            assertEquals(expectedCutAwayPolygon, cut.getCutAway());
            assertEquals(areaToCutOff, cut.getCutAway().getArea(), EXACT_PRECISION);
        }
    }
}
