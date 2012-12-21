package anja.geom.random;


import java.util.Vector;

import anja.geom.ConvexPolygon2;
import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.PointsAccess;
import anja.geom.Polygon2;


/**
 * Erzeugung eines Zufallspolygons durch <i>steady growth</i>, beschrieben
 * in:<br> <i>Auer/Held: RPG - Heuristics for the generation of random
 * polygons</i>
 * 
 * @version 0.8 30.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class SteadyGrowthPolygon2
		extends RandomPolygon2
{

	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Polygon durch <i>steady growth</i> mit zufaelligen
	 * Punkten. Als Polygonparameter dienen Defaultwerte.
	 */
	public SteadyGrowthPolygon2()
	{

		super(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // SteadyGrowthPolygon2


	/**
	 * Erzeugt ein neues Polygon durch <i>steady growth</i> mit zufaelligen
	 * Punkten, wobei die Polygonparameter uebergeben werden. Die Anzahl der
	 * Punkte wird innerhalb der uebergebenen Grenzen zufaellig gewaehlt, die
	 * Ausdehnung des Polygons erreicht maximal die uebergebenen Werte fuer
	 * Hoehe und Breite.
	 * 
	 * @param poly_min_points
	 *            Untergrenze der Punktanzahl
	 * @param poly_max_points
	 *            Obergrenze der Punktanzahl
	 * @param width
	 *            maximale horizontale Ausdehnung des Polygons
	 * @param height
	 *            maximale vertikale Ausdehnung des Polygons
	 */
	public SteadyGrowthPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		super(poly_min_points, poly_max_points, width, height);

	} // SteadyGrowthPolygon2


	/**
	 * Erzeugt ein neues Polygon durch <i>steady growth</i> aus den uebergebenen
	 * Punkten.
	 * 
	 * @param poly_min_points
	 *            Untergrenze der Punktanzahl
	 * @param poly_max_points
	 *            Obergrenze der Punktanzahl
	 * @param width
	 *            maximale horizontale Ausdehnung des Polygons
	 * @param height
	 *            maximale vertikale Ausdehnung des Polygons
	 * @param points
	 *            die Punkte des Polygons
	 */
	public SteadyGrowthPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super(poly_min_points, poly_max_points, width, height, points);

	} // SteadyGrowthPolygon2


	//************************************************************
	// Public methods
	//************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	public String toString()
	{

		return "SteadyGrowthPolygon2[n=" + _n + "]";

	} // toString


	//************************************************************
	// Private methods
	//************************************************************

	/*
	* [javadoc-Beschreibung wird aus anja.geom.random.RandomPolygon2.java kopiert]
	*/
	protected void _init()
	{

		Point2 point;

		// init. polygon with 2 points:
		point = (Point2) _random_points.firstElement();
		_random_points.removeElementAt(0);
		this.addPoint(point);

		point = (Point2) _random_points.firstElement();
		_random_points.removeElementAt(0);
		this.addPoint(point);

		// generate polygon:

		while (_random_points.size() != 0)
		{
			point = _choosePoint(this, _random_points);
			if (!_insertAtVisibleEdge(point))
			{
				System.out.println("!!!No edge found: " + point.toString());
				/*
				            Circle2 c;
				            c = new Circle2 ( point, 10.0f );
				            _point_layer.add(c);
				            _point_layer.add(point);
				*/
			}
		}

		this.setClosed();

	} // _init


	/**
	 * Waehlt einen zufaelligen Punkt aus S so dass kein Punkt aus S\{point} in
	 * CH(P u {point}) liegt.
	 * 
	 * s ist die Liste der uebrigbleibenden Punkte, wird zu S\{point} geaendert
	 * 
	 * @param p
	 *            Das Polygon
	 * @param s
	 *            Liste der übrigen Punkte
	 * 
	 * @return Der zufällige Punkt
	 */
	private Point2 _choosePoint(
			Polygon2 p,
			Vector s)
	{

		Vector stemp;
		Point2 point, t;
		ConvexPolygon2 convhull;
		boolean found = false;
		int i, j;

		stemp = (Vector) s.clone();

		do
		{
			// get random point from stemp
			i = dice(0, stemp.size() - 1);

			point = (Point2) stemp.elementAt(i);
			stemp.removeElementAt(i);

			// temporarily add point to p and calculate convex hull
			p.addPoint(point);
			convhull = new ConvexPolygon2(p);

			// update stemp
			for (j = 0; j < stemp.size(); j++)
			{
				t = (Point2) stemp.elementAt(j);

				if (!convhull.contains(t))
				{
					stemp.removeElementAt(j);
				}
			} // for j

			p.removeLastPoint();

			// was chosen point valid?
			if (stemp.size() == 0)
			{
				s.removeElement(point);
				found = true;
			}

		}
		while (!found);

		return point;

	} // _choosePoint


	/**
	 * Fuegt einen Punkt p dem Polygon hinzu. Sucht eine Kante (v1,v2), die von
	 * p aus vollstaendig sichtbar ist und ersetzt sie durch die Kanten (v1,p)
	 * und (p,v2).
	 * 
	 * Liefert false wenn eine solche Kante nicht existiert.
	 * 
	 * @param point
	 *            Der einzufügende Punkt
	 * 
	 * @return false, wenn die Kante nicht existiert, true sonst
	 */
	protected boolean _insertAtVisibleEdge(
			Point2 point)
	{

		PointsAccess pan, pap;
		Point2 pnext, pprev, ptemp, pnextold;
		boolean found = false;
		boolean correct = true;
		int i;

		ptemp = new Point2(0, 0); // dummy  

		pan = this.closestPointAccess(point);
		//      pap = this.closestPointAccess( point );

		pnext = pan.currentPoint();
		//      pprev = pap.currentPoint();
		pprev = pnext;

		do
		{
			pnext = pan.cyclicNextPoint();

			if (!openIntersect(pnext, point))
			{
				ptemp = pnext;
				pnext = pan.cyclicNextPoint();
				if (!openIntersect(pnext, point))
				{
					found = true;
				}
			}

			if (!found)
			{
				if (pprev.equals(pnext) || pprev.equals(ptemp))
				{
					// _markPoint( pprev, 1 );
					// _markPoint( ptemp, 3 );
					// _markPoint( pnext, 5 );
					found = true;
					correct = false; // no edge found
				}
			}
			/*
			         if (! found) {
			            pprev = pap.cyclicPrevPoint();
			            if ( ! openIntersect( pprev, point )) {
			               ptemp = pprev;
			               pprev = pap.cyclicPrevPoint();
			               if ( ! openIntersect( pprev, point )) {
			                  found = true;
			               }
			            }
			         }
			*/
		}
		while (!found);

		if (correct)
		{
			i = _points.getIndex(_points.find(ptemp)); //indexOf( ptemp );
			//_points.add( i+1, point );
			_points.insert(_points.at(i + 1), point);
		}

		return correct;

	} // _insertAtVisibleEdge

} // SteadyGrowthPolygon2
