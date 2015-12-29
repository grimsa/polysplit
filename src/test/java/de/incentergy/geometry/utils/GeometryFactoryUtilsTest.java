package de.incentergy.geometry.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(Enclosed.class)
public class GeometryFactoryUtilsTest {

    public static class GetSubolygonTests {

        @Test
        public void simpleCase() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 1, 100 0, 90 50, 10 50, 0 0))");

            Polygon result = GeometryFactoryUtils.getSubpolygon(polygon, new Coordinate(50, 1), new Coordinate(10, 50));
            assertEquals("POLYGON ((50 1, 100 0, 90 50, 10 50, 50 1))", result.toString());
        }

        @Test
        public void simpleCaseReversed() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 1, 100 0, 90 50, 10 50, 0 0))");

            Polygon result = GeometryFactoryUtils.getSubpolygon(polygon, new Coordinate(10, 50), new Coordinate(50, 1));
            assertEquals("POLYGON ((10 50, 0 0, 50 1, 10 50))", result.toString());
        }
    }

    public static class SlicePolygonTests {

        @Test
        public void startPointIsStartOfRing() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 50, 100 20, 70 -20, 30 -10, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(0, 0), new Coordinate(75, 35));
            assertEquals("POLYGON ((0 0, 50 50, 75 35, 0 0))", result.toString());
        }

        @Test
        public void endPointIsStartOfRing() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 50, 100 20, 70 -20, 30 -10, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(75, 35), new Coordinate(0, 0));
            assertEquals("POLYGON ((75 35, 100 20, 70 -20, 30 -10, 0 0, 75 35))", result.toString());
        }

        @Test
        public void vertexToVertex() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 50, 100 20, 70 -20, 30 -10, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(50, 50), new Coordinate(70, -20));
            assertEquals("POLYGON ((50 50, 100 20, 70 -20, 50 50))", result.toString());
        }

        @Test
        public void pointOnEdgeToPointOnEdge() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 50, 100 20, 70 -20, 30 -10, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(25, 25), new Coordinate(50, -15));
            assertEquals("POLYGON ((25 25, 50 50, 100 20, 70 -20, 50 -15, 25 25))", result.toString());
        }

        @Test
        public void runningThroughFirstPointOfRing_triangle() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(20, 0), new Coordinate(5, 100));
            assertEquals("POLYGON ((20 0, 0 100, 5 100, 20 0))", result.toString());
        }

        @Test
        public void runningThroughFirstPointOfRing_largerPoly() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 100, 80 100, 100 0, 20 0, 0 100))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(20, 0), new Coordinate(90, 50));
            assertEquals("POLYGON ((20 0, 0 100, 80 100, 90 50, 20 0))", result.toString());
        }


    }
}
