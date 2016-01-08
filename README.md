# polysplit

## Synopsis

The purpose of this project is to split a given [JTS](http://www.vividsolutions.com/jts/JTSHome.htm) polygon into any number of equal areas, while ensuring minimum length of line based cuts. The solution is based on [this algorithm by Sumit Khetarpal](http://www.khetarpal.org/polygon-splitting/). It works for both convex and concave polygons, as long as they don't have any intersecting edges and are defined by a single exterior ring.

## Code Example
```
    Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 100 0, 90 50, 10 50, 0 0))");
    List<Polygon> parts = new GreedyPolygonSplitter().split(polygon, 2);
```

## Known issues

**Caution: carefuly test the code before considering it production-ready!**

A list of known issues can be found under the issues tab.

## Build

The project is built using Maven.

Currently requires JDK 8.

## Tests

Unit tests are present for most methods.

Test cases covering real-world or randomly generated scenarios could be added.

## Contributors

Developed by: Gediminas Rimša

Sponsored by: Incentergy GmbH 

**Contributions are welcome!**

## Licence

Licensed under the [MIT license](https://github.com/grimsa/polysplit/blob/master/LICENSE).