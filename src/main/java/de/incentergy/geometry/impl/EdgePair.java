package de.incentergy.geometry.impl;

import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import de.incentergy.geometry.utils.GeometryFactoryUtils;
import de.incentergy.geometry.utils.GeometryUtils;

/**
 * Represents a pair of edges on polygon's exterior ring.<br>
 * Warning: direction of edges is assumed to be the same as in the polygon's exterior ring.
 * <p>
 * Possible lines of cut are located in one of:
 * <ul>
 * <li>T1        - First  triangle, may not exist in some cases</li>
 * <li>Trapezoid - Trapezoid, always present</li>
 * <li>T2        - Second triangle, may not exist in some cases</li>
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
        Coordinate intersectionPoint = GeometryUtils.getIntersectionPoint(edgeA, edgeB);

        this.edgeA = edgeA;
        this.edgeB = edgeB;

        // there will be 2 projected points at most
        projected0 = getProjectedVertex(edgeA.p1, edgeB, intersectionPoint);
        if (projected0.isNotValid()) {
            projected0 = getProjectedVertex(edgeB.p0, edgeA, intersectionPoint);
        }
        projected1 = getProjectedVertex(edgeA.p0, edgeB, intersectionPoint);
        if (projected1.isNotValid()) {
            projected1 = getProjectedVertex(edgeB.p1, edgeA, intersectionPoint);
        }
    }

    private ProjectedVertex getProjectedVertex(Coordinate point, LineSegment edge, Coordinate intersectionPoint) {
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

        private EdgePairSubpolygons(LineSegment edgeA, LineSegment edgeB, ProjectedVertex projected0, ProjectedVertex projected1) {
            this.edgeA = Objects.requireNonNull(edgeA, "Edge A is required");
            this.edgeB = Objects.requireNonNull(edgeB, "Edge B is required");

            // Build triangles if corresponding projected points are valid
            triangle1 = projected0.isValid() ? GeometryFactoryUtils.createTriangle(edgeA.p1, projected0, edgeB.p0) : null;
            triangle2 = projected1.isValid() ? GeometryFactoryUtils.createTriangle(edgeA.p0, projected1, edgeB.p1) : null;

            // Build a trapezoid:
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

        public boolean hasTriangle1() {
            return triangle1 != null;
        }

        public boolean hasTriangle2() {
            return triangle2 != null;
        }

        public LineSegment getOutsideEdge1() {
            return new LineSegment(edgeA.p1, edgeB.p0);
        }

        public LineSegment getOutsideEdge2() {
            return new LineSegment(edgeB.p1, edgeA.p0);
        }

        public double getArea() {
            double area = trapezoid.getArea();
            area += hasTriangle1() ? triangle1.getArea() : 0;
            area += hasTriangle2() ? triangle2.getArea() : 0;
            return area;
        }

        @Override
        public String toString() {
            return "EdgePairSubpolygons [triangle1=" + triangle1 + ", trapezoid=" + trapezoid + ", triangle2=" + triangle2 + "]";
        }
    }
}