package de.incentergy.geometry;

import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

public interface PolygonSplitter {

    /**
     * Splits the polygon into parts of equal area
     *
     * @param polygon - polygon to split
     * @param parts - number of equal area parts that must be produced
     * @return
     */
    List<Polygon> split(Polygon polygon, int parts);

}
