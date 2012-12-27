import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class PolygonInputFrame extends JFrame implements ActionListener {
	PlotArea drawPanel;
	ControlArea buttonPanel;
	Point2D.Double[] polyP;
	Point2D.Double[] polyQ;
	
	public PolygonInputFrame() {
    	super("Frechet Distance - Simple Polygons");
    	drawPanel = new PlotArea();
    	this.getContentPane().add(drawPanel);
    	this.addTestPolygons();
    	ShortestPath spCalculator = new ShortestPath(polyQ, polyQ[0], polyQ[3]);
    	drawPanel.spPath = spCalculator.getPath();
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
	public PlotArea(){
		super();
		this.setSize(400, 400);
	}
	public void paint(Graphics g)
	{	
		g.setColor(Color.blue);
		drawPolygon(polyP, g);
		g.setColor(Color.red);
		drawPolygon(polyQ, g);
		if (spPath != null) {
			drawPath(spPath, g);
		}
    	super.paintComponents(g);
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