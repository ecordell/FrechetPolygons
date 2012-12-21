package anja.polyrobot;

import anja.geom.Point2;
import anja.geom.Polygon2;


public class ObstacleResult {
    public Point2 hitpoint;
    public Polygon2 obstacle;
    public int edge_number;

    public ObstacleResult(Point2 _hitpoint,Polygon2 _obstacle,int _edge_number) {
        hitpoint = _hitpoint;
        obstacle = _obstacle;
        edge_number = _edge_number;
    }

}

