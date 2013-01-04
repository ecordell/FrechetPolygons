import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



class Interval {
	//normalized
	Point2D.Double startGraph;
	Point2D.Double endGraph;
	
	@Override public String toString() {
		return "Start: (" + startGraph.x + ", " + startGraph.y + ") End: (" + endGraph.x + ", " + endGraph.y + ")";
	}
}

class Arrow {
	ArrayList<Arrow> subArrows;
	Interval start;
	Interval end;
	@Override public String toString() {
	    return "Arrow:\n\tFrom: " + start.toString() + "\n\tTo: " + end.toString();
	}
	
	//TODO: on set start and end, check monotonicity and modify as necessary
}

class Layer {
	int level;
	
	//two dimensional array of sets of arrows, corresponding to each cell of the FSD
	ArrayList<ArrayList<Set<Arrow>>> arrows;
}

public class BaseReachabilityGraph {
	Point2D.Double[] originalPolyP;
	Point2D.Double[] originalPolyQ;
	Point2D.Double[] borderPolyP;
	Point2D.Double[] borderPolyQ;
	
	//Construct base reachability graph from polygons
	public BaseReachabilityGraph(Point2D.Double[] polyP, Point2D.Double[] polyQ, double epsilon) {
		
		//Store pointers to original polygons, and build the edge list of points.
		//P is copied twice
		originalPolyP = polyP;
		originalPolyQ = polyQ;
		borderPolyP = new Point2D.Double[polyP.length * 2];
		System.arraycopy(polyP, 0, borderPolyP, 0, polyP.length);
		System.arraycopy(polyP, 0, borderPolyP, polyP.length, polyP.length);
		borderPolyQ = new Point2D.Double[polyQ.length];
		System.arraycopy(polyQ, 0, borderPolyQ, 0, polyQ.length);
		
		
		Layer zeroLayer = createLayerZero(epsilon);
		int ctr = 0;
		for (int i = 0; i < borderPolyP.length - 1; i++) {
			for (int j = 0; j < borderPolyQ.length - 1; j++) {
				for (Arrow arrow : zeroLayer.arrows.get(i).get(j)) {
					System.out.println(arrow.toString());
					ctr ++;
				}
			}
		}
		System.out.println("Total number of intervals: " + ctr);
		
    }
	
	Layer createLayerZero(double epsilon) {
		Layer layerZero = new Layer();
		layerZero.level = 0;
		layerZero.arrows = new ArrayList<ArrayList<Set<Arrow>>>();

		//First build a list of intervals and find the arrows only within each cell
		for (int i = 0; i < borderPolyP.length - 1; i++) {
			ArrayList<Set<Arrow>> column = new ArrayList<Set<Arrow>>();
			layerZero.arrows.add(column);
			for (int j = 0; j < borderPolyQ.length - 1; j++) {
				//P on the x-axis, Q on the y-axis
				
				//get the segments for each side of a cell so we can construct the arrows
				//TODO: make calls simpler by passing an enum of L, R, T, B. Then i, j only needed to be passed once and the method can infer the rest.
				Interval left = freeSpaceForSegment(layerZero, borderPolyQ[j], borderPolyQ[j+1], borderPolyP[i], i, j, false, i, j, epsilon);
				Interval right = freeSpaceForSegment(layerZero, borderPolyQ[j], borderPolyQ[j+1], borderPolyP[i+1], i+1, j, false, i, j, epsilon);
				Interval top = freeSpaceForSegment(layerZero, borderPolyP[i], borderPolyP[i+1], borderPolyQ[j+1], i, j+1, true, i, j, epsilon);
				Interval bottom = freeSpaceForSegment(layerZero, borderPolyP[i], borderPolyP[i+1], borderPolyQ[j], i, j, true, i, j, epsilon);
				
				//Build arrows and add to appropriate place in level
				Set<Arrow> arrowSet = new HashSet<Arrow>();
				
				if (left != null) {
					if (right != null) {
						Arrow arrow = new Arrow();
						arrow.start = left;
						arrow.end = right;
						arrow.subArrows = null;
						arrowSet.add(arrow);
					}
					if (top != null) {
						Arrow arrow = new Arrow();
						arrow.start = left;
						arrow.end = top;
						arrow.subArrows = null;
						arrowSet.add(arrow);
					}
				}
				if (bottom != null) {
					if (right != null) {
						Arrow arrow = new Arrow();
						arrow.start = bottom;
						arrow.end = right;
						arrow.subArrows = null;
						arrowSet.add(arrow);
					}
					if (top != null) {
						Arrow arrow = new Arrow();
						arrow.start = bottom;
						arrow.end = top;
						arrow.subArrows = null;
						arrowSet.add(arrow);
					}
				}
				
				if (arrowSet.size() == 0) {
					arrowSet = null;
				}
				column.add(arrowSet);
			}
		}
		return layerZero;
	}
	
	//This calculates the free space for a line segment, either horizontal or vertical. 
	//The axisPoint indicates the point on the other curve which should be compared
	//i.e. if we're comparing a segment on Q, then we look at one point on P (a vertical if P is on the x axis)
	//this checks the current level and retrieves an existing interval if it's already computed
	Interval freeSpaceForSegment(Layer layer, Point2D.Double start, Point2D.Double end, Point2D.Double axisPoint, int xindex, int yindex, boolean horizontal, int xPositionFS, int yPositionFS, double epsilon) {		
		double xdiff, ydiff, root, b, divisor, t1, t2, q;
		
		//Return precomputed interval if it exists
		//Get cell to the left or bottom, depending on which side the segment is on
		//TODO: use enum for LRTB, save some unnescessary compares
		Set<Arrow> arrowSet = null;
		if (horizontal) {
			//get cell below
			if (yPositionFS - 1 >= 0) {
				arrowSet = layer.arrows.get(xPositionFS).get(yPositionFS - 1);
			}
		} else {
			//get cell to the left
			if (xPositionFS - 1 >= 0) {
				arrowSet = layer.arrows.get(xPositionFS - 1).get(yPositionFS);
			}
		}
		if (arrowSet != null) {
			for (Arrow arrow : arrowSet) {
				if ( (horizontal && arrow.end.startGraph.x == arrow.end.endGraph.x) || (!horizontal && arrow.end.startGraph.y == arrow.end.endGraph.y) ) {
					return arrow.end;
				}
			}
		}
		
		
		xdiff = end.x - start.x;
		ydiff = end.y - start.y;
		divisor = xdiff*xdiff + ydiff*ydiff;
		if(divisor == 0){
			System.out.println("divisor = 0");
		}
	    b = (axisPoint.x - start.x)*xdiff + (axisPoint.y - start.y)*ydiff;
	    q = (start.x*start.x + start.y*start.y + axisPoint.x*axisPoint.x + axisPoint.y*axisPoint.y - 2*start.x*axisPoint.x - 2*start.y*axisPoint.y - epsilon*epsilon) * divisor;
	    root= b*b-q ;
	    if (root < 0) {
	    	//all black
	    	return null;
	    }
	    root = Math.sqrt(root);
	    t2 = (b+root)/divisor;
	    t1 = (b-root)/divisor;
	    if (t1<0) t1 = 0;
	    if (t2<0) t2 = 0;
	    if (t1>1) t1 = 1;
	    if (t2>1) t2 = 1;
	    
	    if(t1 == t2){
	    	//all black
	    	return null;
	    }
	    
	    Interval result = new Interval();
	    if (!horizontal) {
	    	result.startGraph = new Point2D.Double(xindex, t1 + yindex); 
	    	result.endGraph = new Point2D.Double(xindex, t2 + yindex);
	    } else {
	    	result.startGraph = new Point2D.Double(t1 + xindex, yindex); 
	    	result.endGraph = new Point2D.Double(t2 + xindex, yindex);
	    }
	    
		return result;
	}
}
