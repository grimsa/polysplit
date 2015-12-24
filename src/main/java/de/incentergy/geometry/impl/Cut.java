package de.incentergy.geometry.impl;

import com.vividsolutions.jts.geom.Polygon;

public class Cut {

    private final double lengthOfCut;
    private final Polygon cutAway;

    public Cut(double lengthOfCut, Polygon cutAway) {
        this.lengthOfCut = lengthOfCut;
        this.cutAway = cutAway;
    }



}
