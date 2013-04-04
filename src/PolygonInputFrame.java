import javax.imageio.ImageIO;
import javax.swing.*;

import org.poly2tri.*;
import org.poly2tri.geometry.polygon.*;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class PolygonInputFrame extends JFrame implements ActionListener {

	PlotArea drawPanel;
	ControlArea buttonPanel;
	Point2D.Double[] polyP;
	Point2D.Double[] polyQ;

	public PolygonInputFrame() {
    	super("Frechet Distance - Simple Polygons");
    	drawPanel = new PlotArea();
        drawPanel.setBackground(Color.white);
    	this.getContentPane().add(drawPanel);
    	this.addTestPolygons();

        //TODO: exception if no path

        double epsilon = criticalEpsilon(polyP, polyQ, 0, 5.0);
    	System.out.println("Epsilon: " + epsilon);
        ReachabilityStructure graph = new ReachabilityStructure(polyP, polyQ, epsilon);

        ArrayList<Point2D.Double[]> possibles = graph.possiblePaths();
        Point2D.Double[] path = possibles.get(0);

        graph.saveFSD();
        graph.saveAllPaths();
        graph.saveAllPathsSeperately();


        if (path != null)
        {

            double spStart = 0;
            double spEnd = 0;

            //TODO: clean up this conversion to display SP. variable names are horrible and this should be a separate function
            Diagonal d = graph.trueDiagonalsForPolygon(polyP).get(0);
            for (Point2D.Double point : path) {
                if ((int)point.x == d.startIndex) {
                    spStart = point.y;
                }
                if ((int)point.x == d.endIndex) {
                    spEnd = point.y;
                }
            }

            if (spStart != spEnd) {
                int startIndex = (int) Math.floor(spStart);

                Point2D.Double endsegmentStart = polyQ[startIndex % polyQ.length];
                Point2D.Double endsegmentEnd = polyQ[(startIndex + 1) % polyQ.length];
                double startend = spEnd - Math.floor(spEnd);
                double x = (1 - startend)*endsegmentStart.x + startend*endsegmentEnd.x;
                double y = (1 - startend)*endsegmentStart.y + startend*endsegmentEnd.y;
                Point2D.Double startForSP = new Point2D.Double(x, y);


                int endIndex = (int) Math.floor(spEnd);

                Point2D.Double startsegmentStart = polyQ[endIndex % polyQ.length];
                Point2D.Double startsegmentEnd = polyQ[(endIndex + 1) % polyQ.length];
                double endend = spEnd - Math.floor(spEnd);
                double ex = (1 - endend)*startsegmentStart.x + endend*startsegmentEnd.x;
                double ey = (1 - endend)*startsegmentStart.y + endend*startsegmentEnd.y;
                Point2D.Double endForSP = new Point2D.Double(ex, ey);

                ShortestPath spCalculator = new ShortestPath(insertPointIntoPolygon(insertPointIntoPolygon(polyQ, startForSP), endForSP), startForSP, endForSP);
                drawPanel.spPath = spCalculator.getPath();
            }


            //draw triangulation
            ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
            for (int i = 0; i < polyP.length; i++) {
                Point2D.Double point = polyP[i];
                points.add(new PolygonPoint(point.x, point.y));
            }

            Polygon poly = new Polygon(points);
            Poly2Tri.triangulate(poly);

            ArrayList<DelaunayTriangle> triangulation = new ArrayList<DelaunayTriangle>();
            triangulation = (ArrayList<DelaunayTriangle>) poly.getTriangles();

            drawPanel.triangulation = triangulation;
        }

        ArrayList<Point2D.Double> sampledPath = new ArrayList<Point2D.Double>();

        if (path != null) {
            sampledPath = samplePath(path, drawPanel);
            drawPanel.interpolation = interpolatedPolygons(samplePath(path, drawPanel), 0.3);
        }

        int ctr = 0;
        for (double i = 0.0; i<= 1.0; i += 0.01) {
            drawPanel.interpolation = interpolatedPolygons(sampledPath, i);

            BufferedImage bImg = new BufferedImage(drawPanel.getWidth(), drawPanel.getWidth(), BufferedImage.TYPE_INT_RGB);
            Graphics2D cg = bImg.createGraphics();
            drawPanel.paint(cg);
            try {
                if (ImageIO.write(bImg, "png", new File("./animation/output_image" + ctr + ".png")))
                {
                    System.out.println("-- saved");
                    ctr++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (double i = 1.0; i>= 0.0; i -= 0.01) {
            drawPanel.interpolation = interpolatedPolygons(sampledPath, i);

            BufferedImage bImg = new BufferedImage(drawPanel.getWidth(), drawPanel.getWidth(), BufferedImage.TYPE_INT_RGB);
            Graphics2D cg = bImg.createGraphics();
            drawPanel.paint(cg);
            try {
                if (ImageIO.write(bImg, "png", new File("./animation/output_image" + ctr + ".png")))
                {
                    System.out.println("-- saved");
                    ctr++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

    double criticalEpsilon(Point2D.Double[] polyP, Point2D.Double[] polyQ, double min, double max) {
        if (Math.abs(min - max) < 0.0001 ) {
            return max ;
        }
        ReachabilityStructure graph = new ReachabilityStructure(polyP, polyQ, (min + max) / 2);
        Point2D.Double[] path = graph.getFirstReachablePath();
        graph = null;
        System.gc();
        if (path != null) {
            return criticalEpsilon(polyP, polyQ, min, (min + max) / 2.0);
        } else {
            return criticalEpsilon(polyP, polyQ, (min + max) / 2.0, max);
        }
    }

    private ArrayList<Point2D.Double> samplePath(Point2D.Double[] path, PlotArea plotArea) {
        ArrayList<Point2D.Double> sampledPath = new ArrayList<Point2D.Double>();
        double dx = plotArea.toMathX(1) - plotArea.toMathX(0);
        for (int i = 0; i < path.length - 1; i++) {
            Point2D.Double current = path[i];
            while (current.x <= path[i+1].x) {
                sampledPath.add(new Point2D.Double(current.x, current.y));
                double nextx = current.x + dx;

                double nexty = path[i].y + (path[i+1].y - path[i].y)*((nextx - path[i].x)/(path[i+1].x - path[i].x));
                current.x = nextx;
                current.y = nexty;

            }
        }
        sampledPath.add(path[path.length - 1]);
        System.out.println("Sampled Path: " + sampledPath);
        return sampledPath;
    }

    Point2D.Double[] interpolatedPolygons(ArrayList<Point2D.Double> sampledPath, double fraction) {
        ArrayList<Point2D.Double> interpolatedPath = new ArrayList<Point2D.Double>();
        for (Point2D.Double point : sampledPath) {

            Point2D.Double pointInP; //from x value
            Point2D.Double pointInQ; //from y value

            //Point in P
            int polyPStartIndex = (int) Math.floor(point.x);
            double polyPAdjustedParameter = point.x - polyPStartIndex;
            polyPStartIndex %= (polyP.length);
            int polyPEndIndex = (polyPStartIndex + 1) % polyP.length;

            Point2D.Double polyPStart = polyP[polyPStartIndex];
            Point2D.Double polyPEnd = polyP[polyPEndIndex];

            double px = (1 - polyPAdjustedParameter)*polyPStart.x + polyPAdjustedParameter*polyPEnd.x;
            double py = (1 - polyPAdjustedParameter)*polyPStart.y + polyPAdjustedParameter*polyPEnd.y;

            pointInP = new Point2D.Double(px, py);

            //Point in Q
            int polyQStartIndex = (int) Math.floor(point.y);
            double polyQAdjustedParameter = point.y - polyQStartIndex;
            polyQStartIndex %= (polyQ.length);
            int polyQEndIndex = (polyQStartIndex + 1) % polyQ.length;

            Point2D.Double polyQStart = polyQ[polyQStartIndex];
            Point2D.Double polyQEnd = polyQ[polyQEndIndex];

            double qx = (1 - polyQAdjustedParameter)*polyQStart.x + polyQAdjustedParameter*polyQEnd.x;
            double qy = (1 - polyQAdjustedParameter)*polyQStart.y + polyQAdjustedParameter*polyQEnd.y;

            pointInQ = new Point2D.Double(qx, qy);

            //In between the two
            double interX = (1 - fraction)*pointInP.x + fraction*pointInQ.x;
            double interY =  (1 - fraction)*pointInP.y + fraction*pointInQ.y;
            Point2D.Double interpolatedPoint = new Point2D.Double(interX, interY);
            interpolatedPath.add(interpolatedPoint);
        }
        Point2D.Double[] result = new Point2D.Double[interpolatedPath.size()];
        for (int i = 0; i < result.length; i++) {
             result[i] = interpolatedPath.get(i);
        }
        return result;
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

	private void addTestPolygons() {
		polyP = new Point2D.Double[4];
		polyP[0] = new Point2D.Double(-1, -1);
		polyP[1] = new Point2D.Double(0, 1);
		polyP[2] = new Point2D.Double(1, -1);
		polyP[3] = new Point2D.Double(0, 0.6);

		drawPanel.polyP = polyP;

		polyQ = new Point2D.Double[4];
		polyQ[0] = new Point2D.Double(-1, 1);
		polyQ[1] = new Point2D.Double(0, -1);
		polyQ[2] = new Point2D.Double(1, 1);
		polyQ[3] = new Point2D.Double(0, -0.6);

    	drawPanel.polyQ = polyQ;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PolygonInputFrame me = new PolygonInputFrame();
    	me.setDefaultCloseOperation(EXIT_ON_CLOSE);
    	me.setSize(400,400);
    	me.setVisible(true);
	}

	public void actionPerformed(ActionEvent e){

    }

	public void mouseClicked(MouseEvent e) {

	}
}

class ControlArea extends JPanel{
	public ControlArea(){

	}
}

class PlotArea extends JPanel{
	public Point2D.Double[] polyP;
	public Point2D.Double[] polyQ;
	public Point2D.Double[] spPath;
	private double xmin = -2;
	private double xmax = 2;
	private double ymin = -2;
	private double ymax = 2;
	public ArrayList<DelaunayTriangle> triangulation;
    public Point2D.Double[] interpolation;

	public PlotArea(){
		super();
		this.setSize(400, 400);
	}
	public void paint(Graphics g)
	{
        super.paint(g);
		g.setColor(Color.blue);
		drawPolygon(polyP, g);
		g.setColor(Color.red);
		drawPolygon(polyQ, g);
		if (spPath != null) {
            g.setColor(Color.blue);
			drawPath(spPath, g);
		}
		g.setColor(Color.orange);
		if (triangulation != null) {
			Iterator<DelaunayTriangle> itr = triangulation.iterator();
			while(itr.hasNext()) {
				DelaunayTriangle t = (DelaunayTriangle) itr.next();
				Point2D.Double[] converted = {new Point2D.Double(t.points[0].getX(), t.points[0].getY()), new Point2D.Double(t.points[1].getX(), t.points[1].getY()), new Point2D.Double(t.points[2].getX(), t.points[2].getY())};
	    	    drawPolygon(converted, g);
	    	    System.out.println("Triangulation Triangle: " + Arrays.toString(converted));
			}
		}
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g.setColor(Color.green);
        if (interpolation != null) {
            drawPolygon(interpolation, g2);
        }
    	super.paintComponents(g2);
	}

	public void drawPolygon (Point2D.Double[] points, Graphics g) {
		Point[] screenPoints = new Point[points.length];
		for (int i = 0; i<points.length; i++) {
			screenPoints[i] = new Point(toScreenX(points[i].x), toScreenY(points[i].y));
		}
		for (int i = 0; i<screenPoints.length; i++) {
			int j = i + 1;
			if (i == screenPoints.length - 1) {
				j = 0;
			}
			g.drawLine(screenPoints[i].x, screenPoints[i].y, screenPoints[j].x, screenPoints[j].y);
		}
	}

	public void drawPath(Point2D.Double[] points, Graphics g) {
		g.setColor(Color.green);
		for (int i = 0; i< points.length - 1; i++) {
			g.drawLine(toScreenX(points[i].x), toScreenY(points[i].y), toScreenX(points[i+1].x), toScreenY(points[i+1].y));
		}
	}

	public double toMathX(int x)
	{
	    return (x - (this.getWidth()) / 2) * ((xmax - xmin) / (this.getWidth())) + ((xmax + xmin) / 2);
	}
	public double toMathY(int y)
	{
	    return ((this.getHeight()) / 2 - y) * ((ymax - ymin) / (this.getHeight())) + ((ymax + ymin) / 2);
	}
	public int toScreenX(double x)
	{
	    return (int)((this.getWidth() / 2) + (x - (xmax + xmin) / 2) * (this.getWidth() / (xmax - xmin)));
	}
	public int toScreenY(double y)
	{
	    return (int)((this.getHeight() / 2) - (y - (ymax + ymin) / 2) * (this.getHeight() / (ymax - ymin)));
	}
}
