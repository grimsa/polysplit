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

                List<Cut> cuts = subpolygons.getCuts(polygon, singlePartArea);
            }
        }
        Polygon remainingPoly = null;
        LineSegment segment = null;
        return remainingPoly;
    }

}
