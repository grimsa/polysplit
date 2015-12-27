package de.incentergy.geometry.impl;

import com.vividsolutions.jts.geom.Polygon;

public class Cut {

    private final double length;        // length of cut
    private final Polygon cutAway;

    public Cut(double lengthOfCut, Polygon cutAway) {
        this.length = lengthOfCut;
        this.cutAway = cutAway;
    }

    public double getLength() {
        return length;
    }

    public Polygon getCutAway() {
        return cutAway;
    }

}
