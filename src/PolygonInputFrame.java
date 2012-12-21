import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PolygonInputFrame extends JFrame implements ActionListener {
	PlotArea drawPanel;
	ControlArea buttonPanel;
	Polygon polyP;
	Polygon polyQ;
	public PolygonInputFrame() {
    	super("Frechet Distance - Simple Polygons");
    	drawPanel = new PlotArea();
    	this.getContentPane().add(drawPanel);
    	this.addTestPolygons();
    }
	
	private void addTestPolygons() {
		polyP = new Polygon();
		polyP.addPoint(drawPanel.toScreenX(-1), drawPanel.toScreenY(-1));
    	polyP.addPoint(drawPanel.toScreenX(0), drawPanel.toScreenY(1));
    	polyP.addPoint(drawPanel.toScreenX(1), drawPanel.toScreenY(-1));
    	polyP.addPoint(drawPanel.toScreenX(0), drawPanel.toScreenY(0.6));
    	
    	drawPanel.polyP = polyP;
    	
    	
    	
    	polyQ = new Polygon();
    	
    	polyQ.addPoint(drawPanel.toScreenX(-1), drawPanel.toScreenY(1));
    	polyQ.addPoint(drawPanel.toScreenX(0),  drawPanel.toScreenY(-1));
    	polyQ.addPoint(drawPanel.toScreenX(1), drawPanel.toScreenY(1));
    	polyQ.addPoint(drawPanel.toScreenX(0), drawPanel.toScreenY(-0.6));
    	
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
	public Polygon polyP;
	public Polygon polyQ;
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
		g.drawPolygon(polyP);
		g.setColor(Color.red);
		g.drawPolygon(polyQ);
    	super.paintComponents(g);
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