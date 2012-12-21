package anja.geom.random;


import anja.geom.Point2;
import anja.geom.Point2List;
import anja.geom.Segment2;
import anja.util.SimpleList;


/**
 * Erzeugung eines Zufallspolygons mit <i>2-opt-moves</i>, beschrieben in:<br>
 * <i>Auer/Held: RPG - Heuristics for the generation of random polygons</i>
 * 
 * @version 0.8 30.12.02
 * @author Thomas Kamphans, Sascha Ternes
 */

public class TwoOptMovesPolygon2
		extends RandomPolygon2
{

	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues Polygon mit <i>2-opt-moves</i> mit zufaelligen Punkten.
	 * Als Polygonparameter dienen Defaultwerte.
	 */
	public TwoOptMovesPolygon2()
	{

		super(DefaultPolyMinPoints, DefaultPolyMaxPoints, DefaultPolyWidth,
				DefaultPolyHeight);

	} // TwoOptMovesPolygon2


	/**
	 * Erzeugt ein neues Polygon mit <i>2-opt-moves</i> mit zufaelligen Punkten,
	 * wobei die Polygonparameter uebergeben werden. Die Anzahl der Punkte wird
	 * innerhalb der uebergebenen Grenzen zufaellig gewaehlt, die Ausdehnung des
	 * Polygons erreicht maximal die uebergebenen Werte fuer Hoehe und Breite.
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
	public TwoOptMovesPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height)
	{

		super(poly_min_points, poly_max_points, width, height);

	} // TwoOptMovesPolygon2


	/**
	 * Erzeugt ein neues Polygon mit <i>2-opt-moves</i> aus den uebergebenen
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
	public TwoOptMovesPolygon2(
			int poly_min_points,
			int poly_max_points,
			int width,
			int height,
			Point2List points)
	{

		super(poly_min_points, poly_max_points, width, height, points);

	} // TwoOptMovesPolygon2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus RandomPolygon2.java kopiert]
	*/
	public String toString()
	{

		return "TwoOptMovesPolygon2[n=" + _n + "]";

	} // toString


	//**************************************************************************
	// Private methods
	//**************************************************************************

	/*
	* [javadoc-Beschreibung wird aus anja.geom.random.RandomPolygon2.java kopiert]
	*/
	protected void _init()
	{

		int i, j, i2, j1, j2, k, n;
		Integer obji, objj;
		Point2 p;
		Segment2 si, sj;
		Point2 vi1, vi2, vj1, vj2;
		SimpleList queue;
		boolean ok;
		int count = 0;

		ok = false;
		queue = new SimpleList();
		setClosed();

		// init. Polygon with shuffled points:
		for (i = 0; i < _n; i++)
		{
			p = (Point2) _random_points.elementAt(i);
			this.addPoint(p);
		}

		n = _points.length();
		i = 0;
		while (i < n)
		{
			ok = true;

			// j in opposite direction to i help to avoid
			// endless loops
			j = n;

			while (j > i)
			{
				i2 = (i == n - 1) ? 0 : i + 1;
				vi1 = (Point2) _points.getValueAt(i);
				vi2 = (Point2) _points.getValueAt(i2);
				si = new Segment2(vi1, vi2);

				j1 = (j == n) ? 0 : j;
				j2 = j - 1;
				vj1 = (Point2) _points.getValueAt(j1);
				vj2 = (Point2) _points.getValueAt(j2);
				sj = new Segment2(vj1, vj2);

				if (openSegmentIntersect(si, sj))
				{
					count++;
					_points.remove(_points.at(i2));
					// _points.add( i2, vj2);
					_points.insert(_points.at(i2), vj2);
					_points.remove(_points.at(j2));
					// _points.add( j2, vi2);
					_points.insert(_points.at(j2), vi2);
					j = n;
					ok = false;
					//               _debugStep();
				}
				else
				{
					j--;
				}
			} // while j

			if (ok)
				i++;
			else
				i = 0;
		} // while i

		//      System.out.print("TwoOptMoves: " + count + " ");

	} // init

} // TwoOptMovesPolygon2
