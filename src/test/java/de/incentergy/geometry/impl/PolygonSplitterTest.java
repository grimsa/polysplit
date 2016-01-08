package de.incentergy.geometry.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import de.incentergy.geometry.PolygonSplitter;

public class PolygonSplitterTest {

    private final PolygonSplitter polygonSplitter = new GreedyPolygonSplitter();

    @Test
    public void splitTrapeziumInHalf() throws Exception {
        Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 100 0, 90 50, 10 50, 0 0))");

        List<Polygon> parts = polygonSplitter.split(polygon, 2);

        assertEquals(2, parts.size());
        assertPolygonEquals("POLYGON ((50 0, 100 0, 90 50, 50 50, 50 0))", parts.get(0));
        assertPolygonEquals("POLYGON ((50 0, 0 0, 10 50, 50 50, 50 0))", parts.get(1));
    }

    @Test
    public void splitLShapedPolygonIn4Parts() throws Exception {
        WKTReader wktReader = new WKTReader();
        Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 0, 0 30, 10 30, 10 10, 20 10, 20 0, 0 0))");

        List<Polygon> parts = polygonSplitter.split(polygon, 4);

        assertEquals(4, parts.size());
        assertPolygonEquals("POLYGON ((0 20, 0 30, 10 30, 10 20, 0 20))", parts.get(0));
        assertPolygonEquals("POLYGON ((0 10, 0 20, 10 20, 10 10, 0 10))", parts.get(1));
        assertPolygonEquals("POLYGON ((10 10, 20 10, 20 0, 10 0, 10 10))", parts.get(2));
        assertPolygonEquals("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))", parts.get(3));
    }

    @Test
    public void testSplittingPrecision() throws Exception {
        WKTReader wktReader = new WKTReader();
        Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 0, 50 -10, 100 0, 90 50, 50 60, 10 50, 0 0))");

        List<Polygon> parts = polygonSplitter.split(polygon, 3);
        assertEquals(3, parts.size());

        double expectedPartArea = polygon.getArea() / 3;
        double expectedDelta = expectedPartArea / 100 * 5;      // allow 5% delta
        assertEquals(expectedPartArea, parts.get(0).getArea(), expectedDelta);
        assertEquals(expectedPartArea, parts.get(1).getArea(), expectedDelta);
        assertEquals(expectedPartArea, parts.get(2).getArea(), expectedDelta);
    }

    private static void assertPolygonEquals(String expectedPolygonWkt, Polygon actual) {
        assertEquals(expectedPolygonWkt, actual.toString());
    }

}
