package de.incentergy.geometry.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

public class PolygonSplitterTest {


    @Test
    public void testSplitting() throws Exception {
        WKTReader wktReader = new WKTReader();
        Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 0, 100 0, 90 50, 10 50, 0 0))");

        PolygonSplitterImpl polySplitter = new PolygonSplitterImpl(polygon, 2);
        List<Polygon> parts = polySplitter.split();

        assertEquals(2, parts.size());
    }

    @Test
    public void testSplitting2() throws Exception {
        WKTReader wktReader = new WKTReader();
        Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 0, 50 -10, 100 0, 90 50, 10 50, 0 0))");

        PolygonSplitterImpl polySplitter = new PolygonSplitterImpl(polygon, 2);
        List<Polygon> parts = polySplitter.split();

        assertEquals(2, parts.size());
    }


    @Test
    public void testSplitting3() throws Exception {
        WKTReader wktReader = new WKTReader();
        Polygon polygon = (Polygon) wktReader.read("POLYGON ((0 0, -10 50, 100 0, 90 50, 50 60, 10 50, 0 0))");

        PolygonSplitterImpl polySplitter = new PolygonSplitterImpl(polygon, 2);
        List<Polygon> parts = polySplitter.split();

        assertEquals(2, parts.size());
    }

}
