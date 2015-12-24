package de.incentergy.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import de.incentergy.geometry.PolygonSplitter;
import de.incentergy.geometry.impl.EdgePair.EdgePairSubpolygons;
import de.incentergy.geometry.utils.GeometryUtils;

/**
 * {@link PolygonSplitter} implementation based on the algorithm by Sumit Khetarpal
 *
 * @see http://www.khetarpal.org/polygon-splitting/
 */
public class PolygonSplitterImpl {

    private static final Logger LOG = Logger.getLogger(PolygonSplitterImpl.class.getName());
    private final Polygon originalPolygon;
    private final int numberOfParts;
    private final double singlePartArea;

    public PolygonSplitterImpl(Polygon originalPolygon, int numberOfParts) {
        if (!originalPolygon.isValid()) {
            throw new IllegalArgumentException("Polygon is not valid!");
        }
        if (numberOfParts < 2) {
            throw new IllegalArgumentException("Number of parts should be greater than 1!");
        }
        this.originalPolygon = originalPolygon;
        this.numberOfParts = numberOfParts;
        this.singlePartArea = originalPolygon.getArea() / numberOfParts;
    }

    public List<Polygon> split() {
        // TODO: add validation - at least 4 sides, no holes

        List<Polygon> polygonParts = new ArrayList<>(numberOfParts);
        Polygon remainingPoly = originalPolygon;
        for (int i = 0; i < numberOfParts - 1; i++) {
            remainingPoly = split(remainingPoly, polygonParts);
        }
        return Collections.unmodifiableList(polygonParts);
    }

    private Polygon split(Polygon polygon, List<Polygon> resultList) {
        List<LineSegment> segments = GeometryUtils.getLineSegments(polygon.getExteriorRing());

        // for each unique edge pair
        for (int i = 0; i < segments.size() - 2; i++) {
            for (int j = i + 2; j < segments.size(); j++) {
                int segmentsCovered = j - i + 1;            // number of segments covered by a LineRing starting with edgeA and ending with edgeB (including)
                if (segments.size() == segmentsCovered) {
                    break;
                }

                // generate unique edge pairs (e.g. 2 in rectangle)
                LineSegment edgeA = segments.get(i);
                LineSegment edgeB = segments.get(j);
                EdgePair edgePair = new EdgePair(edgeA, edgeB);
                EdgePairSubpolygons subpolygons = edgePair.getSubpolygons();

                LOG.info("i = " + i + ", j = " + j);

                // Polygon's exterior ring is equal to [edgeA + segmentsBetweenEdgePair + edgeB + segmentsOutsideEdgePair]
                int segmentCountBetweenEdgePair = segmentsCovered - 2;
                int segmentCountOutsideEdgePair = segments.size() - segmentsCovered;
                List<Cut> cuts = subpolygons.getCuts(polygon, singlePartArea, segmentCountBetweenEdgePair, segmentCountOutsideEdgePair);

//                Polygon remainingPolygon = (Polygon) polygon.difference(cutAwayPolygon);

//                double areaNextToTriangle1 = 0;
//                double areaNextToTriangle2 = 0;
//
//                if (segmentsCovered > 3) {                      // if edges have a single segment in between them
//                    // calculate extra area adjacent to Triangle1
//                    LineSegment triangle1OutsideEdge = subpolygons.getOutsideEdge1();
//                    areaNextToTriangle1 = GeometryFactoryUtils.slicePolygon(polygon, triangle1OutsideEdge.p0, triangle1OutsideEdge.p1).getArea();
//                    LOG.info("Has extra area adjacent to Tri1 " + areaNextToTriangle1);
//                }
//
//                if (segmentsCovered < segments.size() - 1) {    // if edges have a single segment in between them on the other side
//                    // calculate extra area adjacent to Triangle2
//                    LineSegment triangle2OutsideEdge = subpolygons.getOutsideEdge2();
//                    areaNextToTriangle2 = GeometryFactoryUtils.slicePolygon(polygon, triangle2OutsideEdge.p0, triangle2OutsideEdge.p1).getArea();
//                    LOG.info("Has extra area adjacent to Tri1 " + areaNextToTriangle2);
//                }
//
//                if (Math.abs(areaNextToTriangle1 + areaNextToTriangle2 + subpolygons.getArea() - polygon.getArea()) > 0.0001) {
//                    throw new IllegalStateException();
//                }
            }
        }
        Polygon remainingPoly = null;
        LineSegment segment = null;
        return remainingPoly;
    }

}
