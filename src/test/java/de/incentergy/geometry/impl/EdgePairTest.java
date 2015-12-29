package de.incentergy.geometry.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
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
        /* TODO: Various edge cases:
         * - Area to cut away is exactly equal to area outside, triangle or trapezoid
         * - Area to cut away is larger than total bound by edge pair
         */

        // Trapezoid with parallel edges (areas of triangle1, rectangle, triangle2 are: 1000, 6000, 1000)
        private static final Polygon PARALLEL_TRAPEZOID;
        private static final Polygon EXTRA_AREA_ADJACENT_TO_TRIANGLE1;          // extra area of 1000
        private static final Polygon EXTRA_AREA_ADJACENT_TO_TRIANGLE2;          // extra area of 1000

        private static final Polygon EXPECTED_CUTAWAY_1_AFTER_TRIANGLE1_CUT;
        private static final Polygon EXPECTED_CUTAWAY_1_AFTER_TRAPEZOID_CUT;
        private static final Polygon EXPECTED_CUTAWAY_1_AFTER_TRIANGLE2_CUT;

        private static final Polygon EXPECTED_CUTAWAY_2_AFTER_TRIANGLE1_CUT;
        private static final Polygon EXPECTED_CUTAWAY_2_AFTER_TRAPEZOID_CUT;
        private static final Polygon EXPECTED_CUTAWAY_2_AFTER_TRIANGLE2_CUT;

        private static EdgePair edgePair;

        static {
            try {
                WKTReader wktReader = new WKTReader();
                PARALLEL_TRAPEZOID = (Polygon) wktReader.read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");
                EXTRA_AREA_ADJACENT_TO_TRIANGLE1 = (Polygon) wktReader.read("POLYGON ((80 100, 100 100, 100 0, 80 100))");
                EXTRA_AREA_ADJACENT_TO_TRIANGLE2 = (Polygon) wktReader.read("POLYGON ((0 100, 20 0, 0 0, 0 100))");

                EXPECTED_CUTAWAY_1_AFTER_TRIANGLE1_CUT = (Polygon) wktReader.read("POLYGON ((80 100, 100 0, 95 0, 80 100))");
                EXPECTED_CUTAWAY_1_AFTER_TRAPEZOID_CUT = (Polygon) wktReader.read("POLYGON ((77.66 100, 80 100, 100 0, 77.66 0, 77.66 100))");
                EXPECTED_CUTAWAY_1_AFTER_TRIANGLE2_CUT = (Polygon) wktReader.read("POLYGON ((5 100, 80 100, 100 0, 20 0, 5 100))");

                EXPECTED_CUTAWAY_2_AFTER_TRIANGLE2_CUT = (Polygon) wktReader.read("POLYGON ((0 100, 5 100, 20 0, 0 100))");
                EXPECTED_CUTAWAY_2_AFTER_TRAPEZOID_CUT = (Polygon) wktReader.read("POLYGON ((0 100, 22.34 100, 22.34 0, 20 0, 0 100))");
                EXPECTED_CUTAWAY_2_AFTER_TRIANGLE1_CUT = (Polygon) wktReader.read("POLYGON ((0 100, 80 100, 95 0, 20 0, 0 100))");

            } catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }

        @BeforeClass
        public static void setUp() {
            LineSegment edgeA = new LineSegment(new Coordinate(0, 100), new Coordinate(80, 100));
            LineSegment edgeB = new LineSegment(new Coordinate(100, 0), new Coordinate(20, 0));
            edgePair = new EdgePair(edgeA, edgeB);
        }

        // Test cases for edges with no adjacent area on the sides

        @Test
        public void cutInFirstTriangle() throws Exception {
            double areaToCutOff = 250;              // 250 in triangle1
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCut1Shape = EXPECTED_CUTAWAY_1_AFTER_TRIANGLE1_CUT;
            Polygon expectedCut2Shape = EXPECTED_CUTAWAY_2_AFTER_TRIANGLE2_CUT;

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(PARALLEL_TRAPEZOID, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        @Test
        public void cutsInRectangle() throws Exception {
            double areaToCutOff = 1234;             // 1000 in the triangle + 234 in rectangle
            double expectedLengthOfCut = 100;
            Polygon expectedCut1Shape = EXPECTED_CUTAWAY_1_AFTER_TRAPEZOID_CUT;
            Polygon expectedCut2Shape = EXPECTED_CUTAWAY_2_AFTER_TRAPEZOID_CUT;

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(PARALLEL_TRAPEZOID, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        @Test
        public void cutsInSecondTriangle() throws Exception {
            double areaToCutOff = 7750;         // all minus 250 units
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCut1Shape = EXPECTED_CUTAWAY_1_AFTER_TRIANGLE2_CUT;
            Polygon expectedCut2Shape = EXPECTED_CUTAWAY_2_AFTER_TRIANGLE1_CUT;

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(PARALLEL_TRAPEZOID, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        // Test cases for edges with adjacent area on both sides

        @Test
        public void cutsInFirstTriangleWithAdjacentAreas() throws Exception {
            Polygon polygon = (Polygon) PARALLEL_TRAPEZOID.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1).union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);

            double areaToCutOff = 1250;         //  1000 adjacent + 250 in triangle1
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCut1Shape = (Polygon) EXPECTED_CUTAWAY_1_AFTER_TRIANGLE1_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1);
            Polygon expectedCut2Shape = (Polygon) EXPECTED_CUTAWAY_2_AFTER_TRIANGLE2_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        @Test
        public void cutsInRectangleWithAdjacentAreas() throws Exception {
            Polygon polygon = (Polygon) PARALLEL_TRAPEZOID.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1).union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);
            double areaToCutOff = 2234;             // 1000 adjacent + 1000 in the triangle + 234 in rectangle
            double expectedLengthOfCut = 100;
            Polygon expectedCut1Shape = (Polygon) EXPECTED_CUTAWAY_1_AFTER_TRAPEZOID_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1);
            Polygon expectedCut2Shape = (Polygon) EXPECTED_CUTAWAY_2_AFTER_TRAPEZOID_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        @Test
        public void cutsInSecondTriangleWithAdjacentAreas() throws Exception {
            Polygon polygon = (Polygon) PARALLEL_TRAPEZOID.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1).union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);

            double areaToCutOff = 8750;         // all minus 250 units
            double expectedLengthOfCut = Math.sqrt(100 * 100 + 15 * 15);
            Polygon expectedCut1Shape = (Polygon) EXPECTED_CUTAWAY_1_AFTER_TRIANGLE2_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE1);
            Polygon expectedCut2Shape = (Polygon) EXPECTED_CUTAWAY_2_AFTER_TRIANGLE1_CUT.union(EXTRA_AREA_ADJACENT_TO_TRIANGLE2);

            List<Cut> cuts = edgePair.getSubpolygons().getCuts(polygon, areaToCutOff);
            assertEquals("Expected cut count", 2, cuts.size());
            assertCutEquals(expectedLengthOfCut, expectedCut1Shape, areaToCutOff, cuts.get(0));
            assertCutEquals(expectedLengthOfCut, expectedCut2Shape, areaToCutOff, cuts.get(1));
        }

        private void assertCutEquals(double expectedCutLength, Polygon expectedCutawayShape, double expectedCutawayArea, Cut actualCut) {
            assertEquals("Expected cut length", expectedCutLength, actualCut.getLength(), EXACT_PRECISION);
            assertTrue("Expected cutaway shape", expectedCutawayShape.equalsTopo(actualCut.getCutAway()));
            assertEquals("Expected cutaway area", expectedCutawayArea, actualCut.getCutAway().getArea(), SMALL_DELTA_PRECISION);
        }

    }
}
