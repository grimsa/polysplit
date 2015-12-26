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

    public static class SlicePolygonTests {

        @Test
        public void testSimpleCase() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 1, 100 0, 90 50, 10 50, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(50, 1), new Coordinate(10, 50));
            assertEquals("POLYGON ((50 1, 100 0, 90 50, 10 50, 50 1))", result.toString());
        }

        @Test
        public void testSimpleCaseReversed() throws Exception {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 50 1, 100 0, 90 50, 10 50, 0 0))");

            Polygon result = GeometryFactoryUtils.slicePolygon(polygon, new Coordinate(10, 50), new Coordinate(50, 1));
            assertEquals("POLYGON ((10 50, 0 0, 50 1, 10 50))", result.toString());
        }

    }
}
