import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;


import java.awt.geom.Point2D;
import java.util.*;


//TODO: only calculate columns once, then double. This saves a LOT of calculation
class Interval {
	//normalized
	Point2D.Double startGraph;
	Point2D.Double endGraph;

	@Override public String toString() {
		return "Start: (" + startGraph.x + ", " + startGraph.y + ") End: (" + endGraph.x + ", " + endGraph.y + ")";
	}

    public Interval() {
       this.startGraph = null;
       this.endGraph = null;
    }

    public Interval(Interval another) {
        this.startGraph = another.startGraph;
        this.endGraph = another.endGraph;
    }

    public Interval(Point2D.Double start, Point2D.Double end) {
        this.startGraph = start;
        this.endGraph = end;
    }

    public Point2D.Double getPolygonStart(Point2D.Double[] poly) {
        double start;
        if (startGraph.x - endGraph.x == 0) {
            start = startGraph.y;
        } else {
            start = startGraph.x;
        }
        int segmentStartIndex = (int) Math.floor(start);
        if (segmentStartIndex < 0 || segmentStartIndex >= poly.length - 1)    {
            System.out.println("Error converting graph point to polygon point.");
            return null;
        }
        Point2D.Double segmentStart = poly[segmentStartIndex];
        Point2D.Double segmentEnd = poly[segmentStartIndex + 1];
        double x = (1 - start)*segmentStart.x + start*segmentEnd.x;
        double y = (1 - start)*segmentStart.y + start*segmentEnd.y;
        return new Point2D.Double(x, y);
    }

    public Point2D.Double getPolygonEnd(Point2D.Double[] poly) {
        double end;
        if (startGraph.x - endGraph.x == 0) {
            end = endGraph.y;
        } else {
            end = endGraph.x;
        }
        int segmentEndIndex = (int) Math.floor(end);
        if (segmentEndIndex < 1 || segmentEndIndex >= poly.length)    {
            System.out.println("Error converting graph point to polygon point.");
            return null;
        }
        Point2D.Double segmentStart = poly[segmentEndIndex - 1];
        Point2D.Double segmentEnd = poly[segmentEndIndex];
        double x = (1 - end)*segmentStart.x + end*segmentEnd.x;
        double y = (1 - end)*segmentStart.y + end*segmentEnd.y;
        return new Point2D.Double(x, y);
    }

    public Point2D.Double getPolygonMidpoint(Point2D.Double[] poly) {
        double x = (this.getPolygonStart(poly).x + this.getPolygonEnd(poly).x) / 2;
        double y = (this.getPolygonStart(poly).y + this.getPolygonEnd(poly).y) / 2;
        return new Point2D.Double(x, y);
    }

    public Point2D.Double getMidpoint() {
        if (isVertical()) {
            return new Point2D.Double(startGraph.x, (startGraph.y + endGraph.y) /2);
        } else {
            return new Point2D.Double((startGraph.x + endGraph.x) / 2, startGraph.y);
        }
    }

    public boolean contains(Point2D.Double point) {
        if (point.x == startGraph.x && point.x == endGraph.x) {
            if (point.y >= startGraph.y && point.y <= endGraph.y) {
                return true;
            }
        } else {
            if (point.x >= startGraph.x && point.x <= endGraph.x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Interval other = (Interval) obj;
        if (this.startGraph.x != other.startGraph.x || this.startGraph.y != other.startGraph.y ||
                this.endGraph.x != other.endGraph.x || this.endGraph.y != other.endGraph.y) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.startGraph.hashCode();
        hash = 53 * hash + this.endGraph.hashCode();
        return hash;
    }

    public boolean intersects(Interval other) {
        if (other.startGraph.x - other.endGraph.x == 0) {
            if (this.startGraph.x - this.startGraph.x != 0) {
                return false;
            }
            //vertical intervals
            return (this.startGraph.y <= other.endGraph.y) && (other.startGraph.y <= this.endGraph.y);
        } else {
            if (this.startGraph.y - this.startGraph.y != 0) {
                return false;
            }
            //horizontal intervals
            return (this.startGraph.x <= other.endGraph.x) && (other.startGraph.x <= this.endGraph.x);
        }
    }

    public Interval intersection(Interval other) {
        if (other.startGraph.x - other.endGraph.x == 0) {
            if (this.startGraph.x - this.startGraph.x != 0) {
                return null;
            }
            //vertical intervals
            return new Interval(new Point2D.Double(this.startGraph.x, Math.max(this.startGraph.y, other.startGraph.y)), new Point2D.Double(this.startGraph.x, Math.min(this.endGraph.y, other.endGraph.y)));
        } else {
            if (this.startGraph.y - this.startGraph.y != 0) {
                return null;
            }
            //horizontal intervals
            return new Interval(new Point2D.Double(Math.max(this.startGraph.x, other.startGraph.x), this.startGraph.y), new Point2D.Double(Math.min(this.endGraph.x, other.endGraph.x), this.startGraph.y));
        }
    }

    public boolean isOnSameSegmentAs(Interval other) {
        if (other.startGraph.x - other.endGraph.x == 0) {
            if (this.startGraph.x - this.startGraph.x != 0) {
                return false;
            }
        } else {
            if (this.startGraph.y - this.startGraph.y != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isParallelTo(Interval other) {
        return (this.startGraph.x - this.endGraph.x) + (other.startGraph.x - other.endGraph.x) == 0 ||
             (this.startGraph.y - this.endGraph.y) + (other.startGraph.y - other.endGraph.y) == 0;
    }

    public boolean isVertical() {
        return (this.startGraph.x - this.endGraph.x == 0);
    }
}



class Arrow {
	ArrayList<Arrow> subArrows;
	Interval start;
	Interval end;
	@Override public String toString() {
	    return "Arrow:\n\tFrom: " + start.toString() + "\n\tTo: " + end.toString();
	}

    public void setStart(Interval _start) {
        this.start = _start;
    }

    public void setEnd(Interval _end) {
        this.end = _end;
    }
}

class Layer {
	int level;

	//two dimensional array of sets of arrows, corresponding to each cell of the FSD
	ArrayList<ArrayList<Set<Arrow>>> arrows;

    public Layer(Layer another) {
        this.arrows = new ArrayList<ArrayList<Set<Arrow>>>(another.arrows);
    }
    public Layer() {
        this.arrows = new ArrayList<ArrayList<Set<Arrow>>>();
    }
}

//Tree for diagonal order
class DiagonalTree {
    private DiagonalNode root;

    public DiagonalTree(Diagonal rootData) {
        root = new DiagonalNode();
        root.data = rootData;
        root.children = new ArrayList<DiagonalNode>();
    }

    public DiagonalNode root(){
        return root;
    }
    public static class DiagonalNode {
        Diagonal data;
        DiagonalNode parent;
        ArrayList<DiagonalNode> children;

        public boolean hasChildren() {
            return (children.size() > 0);
        }

        public void print() {
            print("", true);
        }

        private void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + "(" + data.startIndex + " - " + data.endIndex + ")");
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
                }
                if (children.size() >= 1) {
                    children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
                }
            }
        }
    }

    public void addDiagonal(Diagonal diag) {
        DiagonalNode node = new DiagonalNode();
        node.data = diag;
        node.children = new ArrayList<DiagonalNode>();
        insert(node, root);
    }

    public void subdivideDiagonals() {
        //after all are added, split out the ends into their own nodes
        divideDiagonalsOf(root);
    }

    void divideDiagonalsOf(DiagonalNode node) {
        if (node.children.size() > 0) {
            for (DiagonalNode n : node.children) {
                divideDiagonalsOf(n);
            }
        } else {
            int span = node.data.endIndex - node.data.startIndex;
            if (span > 1) {
               for (int i = 0; i < span; i++) {
                   Diagonal newDiag = new Diagonal(node.data.startIndex + i, node.data.startIndex + i + 1);
                   DiagonalNode newNode = new DiagonalNode();
                   newNode.data = newDiag;
                   newNode.children = new ArrayList<DiagonalNode>();
                   newNode.parent = node;
                   node.children.add(newNode);
               }
            }
        }
    }

    void insert(DiagonalNode toBeInserted, DiagonalNode parentNode) {
        if (parentNode.children.size() > 0) {
            for (DiagonalNode node : parentNode.children) {
                if (toBeInserted.data.containedWithin(node.data)) {
                    insert(toBeInserted, node);
                    break;
                }
            }
        } else {
            parentNode.children.add(toBeInserted);
            toBeInserted.parent = parentNode;
        }
    }

    void print() {
        root.print();
    }
}

class Diagonal {
    int startIndex;
    int endIndex;

    public Diagonal(int start, int end) {
        this.startIndex = start;
        this.endIndex = end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Diagonal other = (Diagonal) obj;
        if (this.startIndex != other.startIndex || this.endIndex != other.endIndex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.startIndex;
        hash = 53 * hash + this.endIndex;
        return hash;
    }

    public boolean containedWithin(Diagonal other) {
        return (this.startIndex >= other.startIndex && this.endIndex <= other.endIndex);
    }
}

public class ReachabilityStructure {
	Point2D.Double[] originalPolyP;
	Point2D.Double[] originalPolyQ;
	Point2D.Double[] borderPolyP;
	Point2D.Double[] borderPolyQ;
    ArrayList<Layer> layers;
    double _epsilon;

	//Construct base reachability graph from polygons
	public ReachabilityStructure(Point2D.Double[] polyP, Point2D.Double[] polyQ, double epsilon) {

		//Store pointers to original polygons, and build the edge list of points.
		//P is copied twice
		originalPolyP = polyP;
		originalPolyQ = polyQ;
		borderPolyP = new Point2D.Double[polyP.length * 2];
		System.arraycopy(polyP, 0, borderPolyP, 0, polyP.length);
		System.arraycopy(polyP, 0, borderPolyP, polyP.length, polyP.length);
		borderPolyQ = new Point2D.Double[polyQ.length];
		System.arraycopy(polyQ, 0, borderPolyQ, 0, polyQ.length);
        _epsilon = epsilon;

		Layer zeroLayer = createBaseLayer(borderPolyP, borderPolyQ, epsilon);
		int ctr = 0;
		for (int i = 0; i < borderPolyP.length - 1; i++) {
				for (Arrow arrow : zeroLayer.arrows.get(i).get(0)) {
					System.out.println(arrow.toString());
					ctr ++;
				}
		}
		System.out.println("Total number of intervals: " + ctr);
		layers = new ArrayList<Layer>();
        layers.add(zeroLayer);
    }

    public Point2D.Double[] getFirstReachablePath() {
        for (ArrayList<Set<Arrow>> column : layers.get(0).arrows) {
            for (Arrow arrow : column.get(0)) {
                if (!arrow.start.isVertical()) {
                    Arrow topArrow = reachabilityStructureFromPoint(arrow.start.getMidpoint());
                    return pathFromArrow(topArrow);
                }
            }
        }
        return null;
    }

    Point2D.Double[] pathFromArrow(Arrow arrow) {
        Set<Arrow> arrowSet = allSubArrows(arrow);
        Set<Point2D.Double> intervalSet = new HashSet<Point2D.Double>();

        for (Arrow a : arrowSet) {
            intervalSet.add(a.start.getMidpoint());
            intervalSet.add(a.end.getMidpoint());
        }

        Point2D.Double[] result = (Point2D.Double[]) intervalSet.toArray();
        Arrays.sort(result);
        return result;
    }

    Set<Arrow> allSubArrows(Arrow arrow) {
        if (arrow.subArrows != null && arrow.subArrows.size() > 0) {
            Set<Arrow> arrowSet = new HashSet<Arrow>();
            for (Arrow sub : arrow.subArrows) {
                arrowSet.addAll(allSubArrows(sub));
            }
            return arrowSet;
        } else {
            Set<Arrow> arrowSet = new HashSet<Arrow>();
            arrowSet.add(arrow);
            return arrowSet;
        }
    }

    Layer createBaseLayer(Point2D.Double[] polyX, Point2D.Double[] polyY, double epsilon) {
        Layer layerZero = new Layer();
        layerZero.level = 0;
        layerZero.arrows = new ArrayList<ArrayList<Set<Arrow>>>();

        //First build a list of intervals and find the arrows only within each cell
        for (int i = 0; i < polyX.length - 1; i++) {
            ArrayList<Set<Arrow>> column = new ArrayList<Set<Arrow>>();
            layerZero.arrows.add(column);
            for (int j = 0; j < polyY.length - 1; j++) {
                //P on the x-axis, Q on the y-axis

                //get the segments for each side of a cell so we can construct the arrows
                //TODO: make calls simpler by passing an enum of L, R, T, B. Then i, j only needed to be passed once and the method can infer the rest.
                Interval left = freeSpaceForSegment(layerZero, polyY[j], polyY[j+1], polyX[i], i, j, false, i, j, epsilon);
                Interval right = freeSpaceForSegment(layerZero, polyY[j], polyY[j+1], polyX[i+1], i+1, j, false, i, j, epsilon);
                Interval top = freeSpaceForSegment(layerZero, polyX[i], polyX[i+1], polyY[j+1], i, j+1, true, i, j, epsilon);
                Interval bottom = freeSpaceForSegment(layerZero, polyX[i], polyX[i+1], polyY[j], i, j, true, i, j, epsilon);

                //Build arrows and add to appropriate place in level
                Set<Arrow> arrowSet = new HashSet<Arrow>();

                if (left != null) {
                    if (right != null) {
                        Arrow arrow = new Arrow();
                        arrow.setStart(new Interval(left));
                        arrow.setEnd(new Interval(right));
                        arrow.subArrows = null;
                        enforceMonotonicity(arrow);
                        arrowSet.add(arrow);
                    }
                    if (top != null) {
                        Arrow arrow = new Arrow();
                        arrow.setStart(new Interval(left));
                        arrow.setEnd(new Interval(top));
                        arrow.subArrows = null;
                        enforceMonotonicity(arrow);
                        arrowSet.add(arrow);
                    }
                }
                if (bottom != null) {
                    if (right != null) {
                        Arrow arrow = new Arrow();
                        arrow.setStart(new Interval(bottom));
                        arrow.setEnd(new Interval(right));
                        arrow.subArrows = null;
                        enforceMonotonicity(arrow);
                        arrowSet.add(arrow);
                    }
                    if (top != null) {
                        Arrow arrow = new Arrow();
                        arrow.setStart(new Interval(bottom));
                        arrow.setEnd(new Interval(top));
                        arrow.subArrows = null;
                        enforceMonotonicity(arrow);
                        arrowSet.add(arrow);
                    }
                }

                if (arrowSet.size() == 0) {
                    arrowSet = null;
                }
                column.add(arrowSet);
            }
            mergeCellsIntoColumn(column);
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
		//TODO: use enum for LRTB, save some unnecessary compares
        //TODO: in order to use this, need to change it so that cells are merged into columns AFTER all cells are calculated
		/*Set<Arrow> arrowSet = null;
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
		}*/


		xdiff = end.x - start.x;
		ydiff = end.y - start.y;
		divisor = xdiff*xdiff + ydiff*ydiff;
		if(divisor == 0){
			System.out.println("Error calculating free space for cell: Divisor = 0");
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

    void enforceMonotonicity(Arrow arrow) {
        boolean changed = false;
        if (arrow.end != null && arrow.start != null) {
            if (arrow.start.isParallelTo(arrow.end)) {
                //either left to right or bottom to top
                if (arrow.start.isVertical()) {
                    //left interval pointing to right interval
                    if (arrow.start.startGraph.y > arrow.end.startGraph.y) {
                        arrow.end.startGraph.y = arrow.start.startGraph.y;
                        changed = true;
                    }
                    if (arrow.end.endGraph.y < arrow.start.endGraph.y) {
                        arrow.start.endGraph.y = arrow.end.endGraph.y;
                        changed = true;
                    }
                } else {
                    //bottom interval pointing to top interval
                    if (arrow.start.startGraph.x > arrow.end.startGraph.x) {
                        arrow.end.startGraph.x = arrow.start.startGraph.x;
                        changed = true;
                    }
                    if (arrow.end.endGraph.x < arrow.start.endGraph.x) {
                        arrow.start.endGraph.x = arrow.end.endGraph.x;
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            //check to make sure SOMETHING is still reachable, if not arrow = null
            if (arrow.start.isVertical()) {
                if (arrow.start.startGraph.y >= arrow.start.endGraph.y ||
                        arrow.end.startGraph.y >= arrow.end.endGraph.y) {
                    arrow = null;
                }
            } else {
                if (arrow.start.startGraph.x >= arrow.start.endGraph.x ||
                        arrow.end.startGraph.x >= arrow.end.endGraph.x) {
                    arrow = null;
                }
            }
            //if we changed the outer arrows, we need to recursively change the subarrows
            if (arrow != null && arrow.subArrows != null && arrow.subArrows.size() > 0) {
                for (Arrow sub : arrow.subArrows) {
                    enforceMonotonicity(sub);
                }
            }
        }
    }

    void mergeCellsIntoColumn(ArrayList<Set<Arrow>> column) {

        if (column.size() == 1) {
            return;
        }

        Set<Arrow> topCell = column.get(column.size() - 1);
        Set<Arrow> bottomCell = column.get(column.size() - 2);


        Set<Arrow> mergedCell = new HashSet<Arrow>();
        //loop through arrows in adjacent cells and find the ones that connect
        for (Arrow topArrow : topCell) {
            for (Arrow bottomArrow : bottomCell) {
                if (topArrow != null && bottomArrow != null && topArrow.start.isOnSameSegmentAs(bottomArrow.end)){
                    if(topArrow.start.intersects(bottomArrow.end)) {
                       //merge arrows
                        if(topArrow.end.isParallelTo(bottomArrow.start)) {
                            //if end and middle are parallel, we need to project monotonicity
                            //monotonicity is already enforced within a single cell
                            Interval middle = topArrow.start.intersection(bottomArrow.end);
                            Arrow newArrow = new Arrow();
                            if (middle != null) {
                                topArrow.setStart(middle);
                                bottomArrow.setEnd(middle);
                                enforceMonotonicity(topArrow); //TODO: do we need these since enforce is recursive?
                                enforceMonotonicity(bottomArrow);
                                if (topArrow != null && bottomArrow != null) {
                                    newArrow.subArrows = new ArrayList<Arrow>();
                                    newArrow.subArrows.add(topArrow);
                                    newArrow.subArrows.add(bottomArrow);
                                    newArrow.setStart(bottomArrow.start);
                                    newArrow.setEnd(topArrow.end);
                                    enforceMonotonicity(newArrow);
                                }
                            } else {
                                topArrow = null;
                                bottomArrow = null;
                            }
                            if (newArrow != null) {
                                mergedCell.add(newArrow);
                            }
                        }
                    } else {
                       //null it out
                       // (this works because each arrow on level zero gets its own copy of the original interval, no stepping on other arrow's toes)
                       //but if this causes problems just removing it should be fine, the garbage collector should take care of them
                       topArrow = null;
                       bottomArrow = null;
                    }
                }
            }
        }

        //remove constituent cells and add new one
        column.remove(topCell);
        column.remove(bottomCell);
        column.add(mergedCell);

        //recursively merge
        mergeCellsIntoColumn(column);
    }

    ArrayList<Set<Arrow>> mergeTwoColumns(ArrayList<Set<Arrow>> left, ArrayList<Set<Arrow>> right) {

        //columns should only have one cell in them at this point, but many arrows
        Set<Arrow> leftColumn = left.get(0);
        Set<Arrow> rightColumn = right.get(0);


        Set<Arrow> mergedColumn = new HashSet<Arrow>();
        //loop through arrows in adjacent columns and find the ones that connect
        for (Arrow leftArrow : leftColumn) {
            for (Arrow rightArrow : rightColumn) {
                if (leftArrow.start.isOnSameSegmentAs(rightArrow.end)){
                    if(leftArrow.start.intersects(rightArrow.end)) {
                        //merge arrows
                        if(leftArrow.end.isParallelTo(rightArrow.start)) {
                            //if end and middle are parallel, we need to project monotonicity
                            //monotonicity is already enforced within a single cell
                            Interval middle = leftArrow.start.intersection(rightArrow.end);
                            Arrow newArrow = new Arrow();
                            if (middle != null) {
                                leftArrow.setStart(middle);
                                rightArrow.setEnd(middle);
                                enforceMonotonicity(leftArrow); //TODO: do we need these since enforce is recursive?
                                enforceMonotonicity(rightArrow);
                                if (leftArrow != null && rightArrow != null) {
                                    newArrow.subArrows = new ArrayList<Arrow>();
                                    newArrow.subArrows.add(leftArrow);
                                    newArrow.subArrows.add(rightArrow);
                                    newArrow.setStart(rightArrow.start);
                                    newArrow.setEnd(leftArrow.end);
                                    enforceMonotonicity(newArrow);
                                }
                            } else {
                                leftArrow = null;
                                rightArrow = null;
                            }
                            if (newArrow != null) {
                                mergedColumn.add(newArrow);
                            }

                        }
                    } else {
                        //null it out
                        // (this works because each arrow on level zero gets its own copy of the original interval, no stepping on other arrow's toes)
                        //but if this causes problems just removing it should be fine, the garbage collector should take care of them
                        leftArrow = null;
                        rightArrow = null;
                    }
                }
            }
        }
        return new ArrayList(mergedColumn);
    }

    DiagonalTree diagonalTreeForPoint(Point2D.Double startPoint) {
        ArrayList<Diagonal> diagonals = orderedDiagonalsForPolygon(originalPolyP);
        int startIndex =  (int) Math.floor(startPoint.x);
        Set<Arrow> column = layers.get(0).arrows.get(startIndex).get(0);
        DiagonalTree diagonalTree;

        boolean validStart = false;
        for (Arrow arrow : column) {
            if (arrow.start.contains(startPoint)) {
                validStart = true;
                break;
            }
        }
        if (validStart) {
            ArrayList<Diagonal> diagonalsCopy = new ArrayList<Diagonal>(diagonals);
            //trim invalid diagonals
            for (Diagonal d : diagonalsCopy) {
                if (d.startIndex <= startIndex || d.endIndex <= startIndex ||
                        d.endIndex > startIndex + originalPolyP.length || d.startIndex >= startIndex + originalPolyP.length) {
                    diagonals.remove(d);
                }
            }

            //put those left into tree
            diagonalTree = new DiagonalTree(new Diagonal(startIndex, startIndex + originalPolyP.length));

            for (Diagonal d : diagonals) {
                diagonalTree.addDiagonal(d);
            }
            diagonalTree.subdivideDiagonals();

            diagonalTree.print();

            return diagonalTree;
        }
        return null;
    }

    //this is for the polygon on the x-axis (it doubles the length)
    ArrayList<Diagonal> orderedDiagonalsForPolygon(Point2D.Double[] poly) {
        int length = poly.length;
        ArrayList<Diagonal> diagonals = new ArrayList<Diagonal>();

        //get the triangulation for the polygon
        ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
        for (int i = 0; i < length; i++) {
            Point2D.Double point = poly[i];
            points.add(new PolygonPoint(point.x, point.y));
        }

        Polygon converted = new Polygon(points);
        Poly2Tri.triangulate(converted);

        ArrayList<DelaunayTriangle> triangulation;
        triangulation = (ArrayList<DelaunayTriangle>) converted.getTriangles();

        //now we need to extract the diagonals from the triangulation
        //NOTE: it's not clear how edges correspond to points here, but it's in the poly2tri source (no docs)
        for (DelaunayTriangle triangle : triangulation) {
            for (int i = 0; i < 3; i++) {
                boolean isDiagonal = !triangle.cEdge[i];
                if (isDiagonal) {
                    Point2D.Double first = new Point2D.Double(triangle.points[(i + 1) % 3].getX(), triangle.points[(i + 1) % 3].getY());
                    Point2D.Double second = new Point2D.Double(triangle.points[(i + 2) % 3].getX(), triangle.points[(i + 2) % 3].getY());

                    int[] indices = new int[2];
                    indices[0] = java.util.Arrays.asList(poly).indexOf(first);
                    indices[1] = java.util.Arrays.asList(poly).indexOf(second);

                    java.util.Arrays.sort(indices);

                    Diagonal newDiagonal = new Diagonal(indices[0], indices[1]);

                    if(diagonals.indexOf(newDiagonal) == -1) {
                        diagonals.add(newDiagonal);
                    }

                }
            }
        }

        System.out.println("Number of diagonals: "  + diagonals.size());

        int max = diagonals.size();
        //since P is doubled, add other possible diagonal indices
        for (int i = 0; i < max; i++) {
            Diagonal d = diagonals.get(i);
            diagonals.add(new Diagonal(d.endIndex, length + d.startIndex));
            diagonals.add(new Diagonal(length + d.startIndex, length + d.endIndex));
        }

        //sort by first index, then by reverse by second
        Collections.sort(diagonals, new Comparator<Diagonal>() {
            public int compare(Diagonal a, Diagonal b) {
                //indices are nonnegative and ordered
                if (a.startIndex == b.startIndex) {
                    return (b.endIndex - a.endIndex);
                }
                return (a.startIndex - b.startIndex);
            }
        });

        return diagonals;
    }

    Arrow reachabilityStructureFromPoint(Point2D.Double startPoint) {
        DiagonalTree diagonalTree = diagonalTreeForPoint(startPoint);
        this.layers.add(1, new Layer(layers.get(0)));

        //TODO: rework the layers idea. don't really need them now, just need one base layer and create a new top layer for each query
        if (diagonalTree != null) {
            return (Arrow) mergeChildren(diagonalTree.root()).get(0).toArray()[0];
        } else {
            return null;
        }
    }

    ArrayList<Set<Arrow>> mergeChildren(DiagonalTree.DiagonalNode node){
        if (node.hasChildren()) {
            for (DiagonalTree.DiagonalNode child : node.children) {
                return mergeChildren(child);
            }
        } else {
            //get list of columns
            ArrayList<ArrayList<Set<Arrow>>> columns = new ArrayList<ArrayList<Set<Arrow>>>();
            for (DiagonalTree.DiagonalNode child : node.parent.children) {
                columns.add(layers.get(1).arrows.get(child.data.startIndex));
            }
            if (node.parent.parent == null) { //root node, don't prune
                return mergeColumns(columns);
            } else {
                return pruneInvalidIntervalsFromColumn(mergeColumns(columns), node.parent.data);
            }
        }
        return null;
    }

    ArrayList<Set<Arrow>> mergeColumns(ArrayList<ArrayList<Set<Arrow>>> columns) {
        ArrayList<Set<Arrow>> finalColumn = columns.get(0);
        for (int i = 1; i < columns.size(); i++) {
            finalColumn = mergeTwoColumns(finalColumn, columns.get(i));
        }

        return finalColumn;
    }

    ArrayList<Set<Arrow>> pruneInvalidIntervalsFromColumn(ArrayList<Set<Arrow>> column, Diagonal diagonal) {
        ArrayList<Set<Arrow>> columnCopy = new ArrayList<Set<Arrow>>(column);

        for (Arrow arrow : columnCopy.get(0)) {
            if (arrow.start.isVertical() && arrow.end.isVertical()) {
                Point2D.Double startPoint = arrow.start.getPolygonMidpoint(borderPolyQ);
                Point2D.Double endPoint = arrow.end.getPolygonMidpoint(borderPolyQ);
                ShortestPath spCalculator = new ShortestPath(insertPointIntoPolygon(insertPointIntoPolygon(originalPolyQ, startPoint), endPoint), startPoint, endPoint);
                //TODO: can this fail for midpoints but not for other points in the interval?
                if (spCalculator.getPath() != null) {
                    //get diagonal path
                    Point2D.Double[] diagonalPath = {borderPolyP[diagonal.startIndex], borderPolyP[diagonal.endIndex]};
                    //get shortest path
                    Point2D.Double[] shortestPath = spCalculator.getPath();

                    //find frechet distance between them
                    Layer reachability = createBaseLayer(diagonalPath, shortestPath, _epsilon);

                    Set<Arrow> finalArrows;
                    for (ArrayList<Set<Arrow>> c : reachability.arrows) {
                        mergeCellsIntoColumn(c);
                    }
                    finalArrows = mergeColumns(reachability.arrows).get(0);

                    boolean isPath = false;
                    for (Arrow a : finalArrows) {
                        if (a.start.contains(new Point2D.Double(0, 0)) && a.end.contains(new Point2D.Double(1, 1))) {
                              //exists a path from start to end
                              isPath = true;
                        }
                    }
                    if (!isPath) {
                        //if frechet distance is too big between diagonal and sp, then we remove the arrow.
                        column.remove(arrow);
                    }
                }
            }
        }
        return column;
    }

    private Point2D.Double[] insertPointIntoPolygon(Point2D.Double[] poly, Point2D.Double point) {
        //Iterate through all edges
        for (int i = 0; i < poly.length; i++) {
            int j = i + 1;
            if (j >= poly.length) {
                j = 0;
            }
            //if distance from point to edge is small, insert point between the vertices making up that edge
            double denominator = Math.sqrt((poly[j].x - poly[i].x)*(poly[j].x - poly[i].x) + (poly[j].y - poly[i].y)*(poly[j].y - poly[i].y));
            double numerator = Math.abs((poly[j].x - poly[i].x)*(poly[i].y - point.y) - (poly[i].x - point.x)*(poly[j].y - poly[i].y));

            Point2D.Double[] newPolygon = new Point2D.Double[poly.length + 1];
            if (getZero(numerator/denominator) == 0) {
                System.arraycopy(poly, 0, newPolygon, 0, j);
                newPolygon[j] = point;
                System.arraycopy(poly, j, newPolygon, j+1, poly.length-j);
                return newPolygon;

            }
        }
        return poly;
    }

    private double getZero(double x) {
        double testPositiveZero = 0.00000000001;
        double testNegativeZero = -0.00000000001;
        if (x >= testNegativeZero && x <= testPositiveZero) {
            x = 0;
        }
        return x;
    }

}
