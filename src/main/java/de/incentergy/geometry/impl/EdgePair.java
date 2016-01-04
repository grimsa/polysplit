package de.incentergy.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import de.incentergy.geometry.utils.GeometryFactoryUtils;
import de.incentergy.geometry.utils.GeometryUtils;
import de.incentergy.geometry.utils.GeometryUtils.IntersectionCoordinate;

/**
 * Represents a pair of edges on polygon's exterior ring.<br>
 * Warning: direction of edges is assumed to be the same as in the polygon's exterior ring.
 * <p>
 * Possible lines of cut are located in one of:
 * <ul>
 * <li>T1 - First triangle, may not exist in some cases</li>
 * <li>Trapezoid - Trapezoid, always present</li>
 * <li>T2 - Second triangle, may not exist in some cases</li>
 * <ul>
 *
 * <pre>
 *                                edgeA
 *            edgeA.p0 .____________________________. edgeA.p1
 *                    /|                            |\
 *                   /                                \
 *   outsideEdge2   /  |                            |  \   outsideEdge1
 *                 /                                    \
 *                / T2 |        Trapezoid           | T1 \
 *               /                                        \
 *              .______.____________________________|______.
 *        edgeB.p1                edgeB                    edgeB.p0
 *                     ^                            ^
 *                 projected1                  projected0
 * </pre>
 */
class EdgePair {

    private final LineSegment edgeA;
    private final LineSegment edgeB;

    private ProjectedVertex projected0;          // projected p0
    private ProjectedVertex projected1;          // projected p1

    public EdgePair(LineSegment edgeA, LineSegment edgeB) {
        // determine the point where the edges would intersect if they were infinite lines
        IntersectionCoordinate intersectionPoint = GeometryUtils.getIntersectionPoint(edgeA, edgeB);

        this.edgeA = edgeA;
        this.edgeB = edgeB;

        // there will be 2 projected points at most
        projected0 = getProjectedVertex(edgeA.p1, edgeB, intersectionPoint);
        if (projected0.isNotValid()) {
            projected0 = getProjectedVertex(edgeB.p0, edgeA, intersectionPoint);
        }
        projected1 = getProjectedVertex(edgeB.p1, edgeA, intersectionPoint);
        if (projected1.isNotValid()) {
            projected1 = getProjectedVertex(edgeA.p0, edgeB, intersectionPoint);
        }
    }

    private ProjectedVertex getProjectedVertex(Coordinate point, LineSegment edge, IntersectionCoordinate intersectionPoint) {
        Coordinate projectionPoint = GeometryUtils.getProjectedPoint(point, edge, intersectionPoint);
        return projectionPoint != null ? new ProjectedVertex(projectionPoint, edge) : ProjectedVertex.INVALID;
    }

    public EdgePairSubpolygons getSubpolygons() {
        return new EdgePairSubpolygons(edgeA, edgeB, projected0, projected1);
    }

    @Override
    public String toString() {
        return "EdgePair [edgeA=" + edgeA + ", edgeB=" + edgeB + "]";
    }

    private static class ProjectedVertex extends Coordinate {
        private static final long serialVersionUID = 1L;
        private static final ProjectedVertex INVALID = new ProjectedVertex();

        private final LineSegment edge;
        private final boolean valid;            // ProjectedVertex is invalid if the projected point lies outside of the edge (is null)

        private ProjectedVertex() {
            this.valid = false;
            this.edge = null;
        }

        public ProjectedVertex(Coordinate coord, LineSegment edge) {
            super(coord);
            this.valid = true;
            this.edge = edge;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isNotValid() {
            return !valid;
        }

        public boolean isOnEdge(LineSegment edge) {
            return valid && this.edge.equalsTopo(edge);
        }

        @Override
        public String toString() {
            return valid ? super.toString() : "(INVALID)";
        }
    }

    /**
     * This class represents the 3 possible polygons in which the minimum cut can be located
     */
    static class EdgePairSubpolygons {
        private final LineSegment edgeA;
        private final LineSegment edgeB;

        private final Polygon triangle1;
        private final Polygon trapezoid;
        private final Polygon triangle2;
        private final double triangle1Area;
        private final double trapezoidArea;
        private final double triangle2Area;

        private EdgePairSubpolygons(LineSegment edgeA, LineSegment edgeB, ProjectedVertex projected0, ProjectedVertex projected1) {
            this.edgeA = Objects.requireNonNull(edgeA, "Edge A is required");
            this.edgeB = Objects.requireNonNull(edgeB, "Edge B is required");

            // build triangles if corresponding projected points are valid
            triangle1 = projected0.isValid() ? GeometryFactoryUtils.createTriangle(edgeA.p1, projected0, edgeB.p0) : null;
            triangle2 = projected1.isValid() ? GeometryFactoryUtils.createTriangle(edgeA.p0, projected1, edgeB.p1) : null;
            triangle1Area = triangle1 != null ? triangle1.getArea() : 0;
            triangle2Area = triangle2 != null ? triangle2.getArea() : 0;

            // build a trapezoid:
            // 1) if projected1 is on edgeA, add projected1, else add edgeA.p0
            // 2) if projected0 is on edgeA, add projected0, else add edgeA.p1
            // 3) if projected0 is on edgeB, add projected0, else add edgeB.p0
            // 4) if projected1 is on edgeB, add projected1, else add edgeB.p1
            // 5) close the polygon
            Coordinate coord1 = projected1.isOnEdge(edgeA) ? projected1 : edgeA.p0;
            Coordinate coord2 = projected0.isOnEdge(edgeA) ? projected0 : edgeA.p1;
            Coordinate coord3 = projected0.isOnEdge(edgeB) ? projected0 : edgeB.p0;
            Coordinate coord4 = projected1.isOnEdge(edgeB) ? projected1 : edgeB.p1;
            trapezoid = GeometryFactoryUtils.createPolygon(coord1, coord2, coord3, coord4);
            trapezoidArea = trapezoid.getArea();
        }

        public Polygon getTriangle1() {
            return triangle1;
        }

        public Polygon getTrapezoid() {
            return trapezoid;
        }

        public Polygon getTriangle2() {
            return triangle2;
        }

        public double getTotalArea() {
            return triangle1Area + trapezoidArea + triangle2Area;
        }

        /**
         * Produces a a collection of possible cuts located in one of {@link EdgePairSubpolygons}.
         * @param polygon The polygon from which the area should be cut away
         * @param singlePartArea area to cut away
         * @param segmentCountBetweenEdgePair number of line segments between edgeA and edgeB (exclusive)
         * @param segmentCountOutsideEdgePair number of line segments between edgeB and edgeA (exclusive)
         * @return A list of 0, 1 or 2 possible cuts
         */
        public List<Cut> getCuts(Polygon polygon, double singlePartArea) {
            // sanity-check
            if (!polygon.contains(trapezoid) || (triangle1 != null && !polygon.contains(triangle1)) || (triangle2 != null && !polygon.contains(triangle2))) {
                // FIXME: some part of subpolygon falls outside of the actual polygon. This can happen for some convex polygons.
                // A proper solution might likely be to update the subpolygon with the actual part of the polygon covered, but this needs to be researched.
                return Collections.emptyList();
            }

            List<Cut> cuts = new ArrayList<>(2);

            List<LineSegment> segments = GeometryUtils.getLineSegments(polygon.getExteriorRing());
            int indexOfEdgeA = segments.indexOf(edgeA);
            int indexOfEdgeB = segments.indexOf(edgeB);
            int segmentsCovered = indexOfEdgeB - indexOfEdgeA + 1;            // number of segments covered by a LineRing starting with edgeA and ending with edgeB (including)

            // Polygon's exterior ring is equal to [edgeA + segmentsBetweenEdgePair + edgeB + segmentsOutsideEdgePair]
            int segmentCountBetweenEdgePair = segmentsCovered - 2;
            int segmentCountOutsideEdgePair = segments.size() - segmentsCovered;

            // if edges are not connected directly, polygon has extra area adjacent to them
            Polygon polygonOutside1 = null;
            Polygon polygonOutside2 = null;
            if (segmentCountBetweenEdgePair > 1) {
                // calculate extra area bounded by segmentsBetweenEdgePair
                polygonOutside1 = GeometryFactoryUtils.getSubpolygon(polygon, edgeA.p1, edgeB.p0);

                // TODO: determine if this is always correct
                // short circuit for when the area between edgePoints contains some which is not part of
                if (!polygon.contains(polygonOutside1)) {
                    return Collections.emptyList();
                }
            }
            if (segmentCountOutsideEdgePair > 1) {
                // calculate extra area bounded by segmentsOutsideEdgePair
                polygonOutside2 = GeometryFactoryUtils.getSubpolygon(polygon, edgeB.p1, edgeA.p0);

                // TODO: determine if this is always correct
                // short circuit for when the area between edgePoints contains some which is not part of
                if (!polygon.contains(polygonOutside2)) {
                    return Collections.emptyList();
                }
            }
            double areaOutside1 = polygonOutside1 != null ? polygonOutside1.getArea() : 0;
            double areaOutside2 = polygonOutside2 != null ? polygonOutside2.getArea() : 0;

            // check first direction (areaOutside1 + T1 + Trapezoid + T2)
            if (areaOutside1 <= singlePartArea) {
                LineSegment lineOfCut = null;                       // line of cut goes from edgeA to edgeB

                if (areaOutside1 + triangle1Area > singlePartArea) {
                    // produce a Cut in Triangle1

                    double areaToCutAwayInTriangle = singlePartArea - areaOutside1;
                    double fraction = areaToCutAwayInTriangle / triangle1Area;

                    ProjectedVertex projected0 = (ProjectedVertex) triangle1.getCoordinates()[1];
                    LineSegment edgeWithPointOfCut = projected0.isOnEdge(edgeA) ? new LineSegment(edgeA.p1, projected0) : new LineSegment(edgeB.p0, projected0);
                    Coordinate pointOfCut = edgeWithPointOfCut.pointAlong(fraction);
                    lineOfCut = GeometryUtils.isPointOnLineSegment(pointOfCut, edgeA) ? new LineSegment(pointOfCut, edgeB.p0) : new LineSegment(edgeA.p1, pointOfCut);

                } else if (areaOutside1 + triangle1Area + trapezoidArea >= singlePartArea) {
                    // produce cut in Trapezoid

                    double areaToCutAway = singlePartArea - (areaOutside1 + triangle1Area);
                    double fraction = areaToCutAway / trapezoidArea;

                    LineSegment trapezoidEdgeOnEdgeA = GeometryUtils.getLineSegment(trapezoid.getExteriorRing(), 0, true); // this edge is reversed so it has the same direction as edgeB
                    LineSegment trapezoidEdgeOnEdgeB = GeometryUtils.getLineSegment(trapezoid.getExteriorRing(), 2);

                    Coordinate pointOfCutOnEdgeA = trapezoidEdgeOnEdgeA.pointAlong(fraction);
                    Coordinate pointOfCutOnEdgeB = trapezoidEdgeOnEdgeB.pointAlong(fraction);
                    lineOfCut = new LineSegment(pointOfCutOnEdgeA, pointOfCutOnEdgeB);

                } else if (areaOutside1 + getTotalArea() >= singlePartArea) {
                    // produce cut in Triangle2

                    double areaToCutAwayInTriangle = singlePartArea - (areaOutside1 + triangle1Area + trapezoidArea);
                    double fraction = areaToCutAwayInTriangle / triangle2Area;

                    ProjectedVertex projected1 = (ProjectedVertex) triangle2.getCoordinates()[1];
                    LineSegment edgeWithPointOfCut = projected1.isOnEdge(edgeA) ? new LineSegment(projected1, edgeA.p0) : new LineSegment(projected1, edgeB.p1);
                    Coordinate pointOfCut = edgeWithPointOfCut.pointAlong(fraction);
                    lineOfCut = GeometryUtils.isPointOnLineSegment(pointOfCut, edgeA) ? new LineSegment(pointOfCut, edgeB.p1) : new LineSegment(edgeA.p0, pointOfCut);
                }

                if (lineOfCut != null && !GeometryUtils.isIntersectingPolygon(lineOfCut, polygon)) {
                    // only consider cuts that do not intersect the exterior ring of the polygon
                    Polygon cutAwayPolygon = GeometryFactoryUtils.slicePolygon(polygon, lineOfCut.p0, lineOfCut.p1);
                    cuts.add(new Cut(lineOfCut.getLength(), cutAwayPolygon));
                }
            }

            // check another direction (areaOutside2 + T2 + Trapezoid + T1)
            if (areaOutside2 <= singlePartArea) {
                LineSegment lineOfCut = null;                       // line of cut goes from edgeB to edgeA

                if (areaOutside2 + triangle2Area > singlePartArea) {
                    // produce a Cut in Triangle2
                    double areaToCutAwayInTriangle = singlePartArea - areaOutside2;
                    double fraction = areaToCutAwayInTriangle / triangle2Area;

                    ProjectedVertex projected1 = (ProjectedVertex) triangle2.getCoordinates()[1];
                    LineSegment edgeWithPointOfCut = projected1.isOnEdge(edgeA) ? new LineSegment(edgeA.p0, projected1) : new LineSegment(edgeB.p1, projected1);
                    Coordinate pointOfCut = edgeWithPointOfCut.pointAlong(fraction);
                    lineOfCut = GeometryUtils.isPointOnLineSegment(pointOfCut, edgeA) ? new LineSegment(edgeB.p1, pointOfCut) : new LineSegment(pointOfCut, edgeA.p0);

                } else if (areaOutside2 + triangle2Area + trapezoidArea >= singlePartArea) {
                    // produce cut in Trapezoid

                    double areaToCutAway = singlePartArea - (areaOutside2 + triangle2Area);
                    double fraction = areaToCutAway / trapezoidArea;

                    LineSegment trapezoidEdgeOnEdgeA = GeometryUtils.getLineSegment(trapezoid.getExteriorRing(), 0);
                    LineSegment trapezoidEdgeOnEdgeB = GeometryUtils.getLineSegment(trapezoid.getExteriorRing(), 2, true);  // this edge is reversed so it has the same direction as edgeA

                    Coordinate pointOfCutOnEdgeA = trapezoidEdgeOnEdgeA.pointAlong(fraction);
                    Coordinate pointOfCutOnEdgeB = trapezoidEdgeOnEdgeB.pointAlong(fraction);
                    lineOfCut = new LineSegment(pointOfCutOnEdgeB, pointOfCutOnEdgeA);

                } else if (areaOutside2 + getTotalArea() >= singlePartArea) {
                    // produce cut in Triangle1

                    double areaToCutAwayInTriangle = singlePartArea - (areaOutside2 + triangle2Area + trapezoidArea);
                    double fraction = areaToCutAwayInTriangle / triangle1Area;

                    ProjectedVertex projected0 = (ProjectedVertex) triangle1.getCoordinates()[1];
                    LineSegment edgeWithPointOfCut = projected0.isOnEdge(edgeA) ? new LineSegment(projected0, edgeA.p1) : new LineSegment(projected0, edgeB.p0);
                    Coordinate pointOfCut = edgeWithPointOfCut.pointAlong(fraction);
                    lineOfCut = GeometryUtils.isPointOnLineSegment(pointOfCut, edgeA) ? new LineSegment(edgeB.p0, pointOfCut) : new LineSegment(pointOfCut, edgeA.p1);
                }

                if (lineOfCut != null && !GeometryUtils.isIntersectingPolygon(lineOfCut, polygon)) {
                    // only consider cuts that do not intersect the exterior ring of the polygon
                    Polygon cutAwayPolygon = GeometryFactoryUtils.slicePolygon(polygon, lineOfCut.p0, lineOfCut.p1);
                    cuts.add(new Cut(lineOfCut.getLength(), cutAwayPolygon));
                }
            }

            // TODO: remove this
            // sanity check
            if (!GeometryUtils.equalWithinDelta(areaOutside1 + areaOutside2 + getTotalArea(), polygon.getArea())) {
                throw new IllegalStateException();
            }

            return Collections.unmodifiableList(cuts);
        }

        @Override
        public String toString() {
            return "EdgePairSubpolygons [triangle1=" + triangle1 + ", trapezoid=" + trapezoid + ", triangle2=" + triangle2 + "]";
        }
    }
}