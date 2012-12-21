package anja.geom;


import anja.util.Angle;
import anja.util.LimitedAngle;
import anja.util.SimpleList;


/**
 * Berechnet den Kern eines geschlossenen einfachen Polygons, das folgenden
 * Bedingungen erfuellen muss: <ul> <li>es ist geschlossen</li> <li>es enthaelt
 * mindestens drei nicht kollineare Eckpunkte</li> <li>es gibt keine
 * uebereinanderliegenden Eckpunkte</li> <li> Kanten schneiden sich nur an ihren
 * Endpunkten</li> </ul> Im Falle dass das Polygon keinen Kern hat, ist die
 * Anzahl der Eckpunkte des Kerns gleich null.
 * 
 * @version 0.5 11.03.2002
 * @author Norbert Selle, modified by Sascha Ternes
 * 
 * @see anja.geom.Polygon2#isSimple
 */

public class Kern
		extends Polygon2
{

	// ************************************************************************
	// Private constants
	// ************************************************************************

	// _pointToLine() beginnt beim Punkt mit der kleinsten x-Koordinate
	private static final int	_MIN_X_FIRST	= 421;

	// _pointToLine() beginnt beim Punkt mit der groessten x-Koordinate
	private static final int	_MAX_X_FIRST	= 422;

	// Index der linken/rechten Grenze im Rueckgabefeld von _getVerticalBorders()
	private static final int	_LEFT			= 0;
	private static final int	_RIGHT			= 1;

	// ************************************************************************
	// Private variables
	// ************************************************************************

	/** das Polygon zu dem der Kern berechnet wird */
	private Polygon2			_poly;

	/** die Erweiterung von _poly um die Drehwinkel */
	private Polygon2			_angle_poly;


	// ************************************************************************
	// Inner classes
	// ************************************************************************

	/**
	 * Erweiterung von Point2 um ein Winkel-Attribut.
	 */
	class APoint
			extends Point2
	{

		/**
		 * Der Winkel am Punkt
		 */
		LimitedAngle	_angle;


		/**
		 * Der Standardkontruktor erzeugt ein Punktobjekt und setzt den Punkt
		 * auf (0,0)
		 */
		public APoint()
		{
			x = 0;
			y = 0;
			_angle = new LimitedAngle(LimitedAngle.OPEN_CIRCLE);
		} // APoint


		/**
		 * Der Konstruktor übernimmt die Werte vom übergebenen Parameter
		 * 
		 * @param input_apoint
		 *            The input point
		 */
		public APoint(
				APoint input_apoint)
		{
			x = input_apoint.x;
			y = input_apoint.y;
			_angle = new LimitedAngle(input_apoint._angle);
		} // APoint


		/**
		 * Konstruktor, der 3 Punkte zur Berechnung des Winkels nutzt.
		 * 
		 * Die 3 Punkte bilden einen Schenkel, wobei in Punkt 2 der Winkel
		 * aufgespannt wird.
		 * 
		 * @param input_source
		 *            Punkt 1
		 * @param input_middle
		 *            Punkt 2
		 * @param input_target
		 *            Punkt 3
		 */
		public APoint(
				Point2 input_source,
				Point2 input_middle,
				Point2 input_target)
		{
			x = input_middle.x;
			y = input_middle.y;
			// angle() berechnet den Winkel im Gegenuhrzeigersinn, der Wertebereich
			// liegt zwischen 0 und 2Pi
			double ccw_angle = input_middle.angle(input_source, input_target);
			_angle = new LimitedAngle(ccw_angle - Angle.PI,
					LimitedAngle.OPEN_CIRCLE);
		} // APoint


		/**
		 * Gibt den Winkel in Bogenmass zurueck
		 * 
		 * @return Der Winkel als Bogenmass
		 */
		public double getAngle()
		{
			return _angle.rad();
		} // getAngle


		/**
		 * Setzt den Winkel in Bogenmass
		 * 
		 * @param input_rad
		 *            Der Winkel als Bogenmass
		 */
		public void setAngle(
				double input_rad)
		{
			_angle.set(input_rad);
		} // setAngle


		/**
		 * Erzeugt eine textuelle Ausgabe
		 * 
		 * @return Textuelle Ausgabe des Objekts
		 */
		public String toString()
		{
			return "( " + x + ", " + y + ", angle " + _angle + " )";
		} // toString

	} // APoint


	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt den Kern zum uebergebenen Polygon.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 */
	public Kern(
			Polygon2 input_polygon)
	{

		_poly = input_polygon;

		// folgendes ist sehr wichtig!
		if (_poly != null)
		{
			// die Orientierung muss fuer die Berechnung 'links' sein!
			_poly.setOrientation(Polygon2.ORIENTATION_LEFT);
		} // if

		_angle_poly = new Polygon2();

		_angle_poly.setClosed();
		setClosed();
		recompute();

	} // Kern


	// ************************************************************************
	// Class methods
	// ************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Gibt das Polygon zum Kern zurueck.
	 * 
	 * @return Das Polygon, zu dem der Kern berechnet wurde.
	 */
	public Polygon2 getPolygon()
	{

		return _poly;

	} // getPolygon


	// ************************************************************************

	/**
	 * Setzt das Polygon und veranlasst eine Neuberechnung.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 */
	public void setPolygon(
			Polygon2 input_polygon)
	{

		_poly = input_polygon;

		// folgendes ist sehr wichtig!
		if (_poly != null)
		{
			// die Orientierung muss fuer die Berechnung 'links' sein!
			_poly.setOrientation(Polygon2.ORIENTATION_LEFT);
		} // if

		recompute();

	} // setPolygon


	// ************************************************************************
	// Interface RecomputationListener
	// ************************************************************************

	/**
	 * Berechnet den Kern neu.
	 */
	public void recompute()
	{

		clear();

		if ((_poly == null) || (_poly.length() < 3))
		{
			return;
		} // if

		// die Eckpunkte mit ihrem Drehwinkel attributieren
		_angle_poly = _computeAngles(_poly);

		// die jeweils weiter linksherum gedrehten Kanten
		SimpleList left_edges = new SimpleList();
		double left_sum = _leftEdges(_angle_poly, left_edges);
		if (left_sum >= 3 * Angle.PI)
		{
			return; // Summe der Drehwinkel >= 3*PI ==> kein Kern
		} // if

		// die jeweils weiter rechtsherum gedrehten Kanten
		SimpleList right_edges = new SimpleList();
		double right_sum = _rightEdges(_angle_poly, right_edges);
		if (right_sum <= -3 * Angle.PI)
		{
			return; // Summe der Drehwinkel <= -3*PI ==> kein Kern
		} // if

		// untere und obere Halbebene bestimmen

		// bei den linksgedrehten Kanten zeigen die unteren nach Osten
		// und die oberen nach Westen
		SimpleList left_west = new SimpleList();
		SimpleList left_east = new SimpleList();
		SimpleList left_vertical = new SimpleList();
		_split(left_edges, left_west, left_vertical, left_east);

		// bei den rechtsgedrehten Kanten zeigen die unteren nach Westen
		// und die oberen nach Osten
		SimpleList right_west = new SimpleList();
		SimpleList right_east = new SimpleList();
		SimpleList right_vertical = new SimpleList();
		_split(right_edges, right_west, right_vertical, right_east);

		left_west.addAll(right_east);
		left_east.addAll(right_west);

		// die Geraden deren Schnittpunkte den oberen Teil des Kerns definieren
		SimpleList top = _lowerPlain(left_west);

		// die Geraden deren Schnittpunkte den unteren Teil des Kerns definieren
		_xMirror(left_east);
		SimpleList bottom = _lowerPlain(left_east);
		_xMirror(bottom);

		// die Halbebenen vereinigen
		_points = _plainsToKern(top, bottom);

		// Kern an den Senkrechten kappen
		_reverseLines(right_vertical);
		right_vertical.addAll(left_vertical);
		_cut(right_vertical);

	} // recompute


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Bestimmt die jeweils weiter linksherum gedrehten Kanten. Das sind
	 * diejenigen, bei denen sich das bisherige Maximum der Winkelsummen
	 * vergroessert. Die erste Kante wird auf jeden Fall ausgewaehlt und ihr
	 * Drehwinkel als 0 angenommen.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 * @param inout_edges
	 *            Die Liste der aus der Berechnung resultierenden Kanten
	 * 
	 * @return Die Winkelsumme
	 */
	private double _leftEdges(
			Polygon2 input_polygon,
			SimpleList inout_edges)
	{

		inout_edges.clear();

		double sum = 0.0;
		double max = 0.0;

		PointsAccess access = new PointsAccess(input_polygon);
		Point2 source = access.nextPoint();
		Point2 target = access.nextPoint();

		// erste Kante auswaehlen, Drehwinkel nicht addieren
		Line2 line = new Line2(new Point2(source), new Point2(target));
		inout_edges.add(line);

		for (int i = 1; i < input_polygon.length(); i++)
		{
			source = target;
			target = access.cyclicNextPoint();
			double phi = ((APoint) source).getAngle();
			sum += phi;
			if (sum > max)
			{
				max = sum;
				line = new Line2(new Point2(source), new Point2(target));
				inout_edges.add(line);
			} // if
		} // for

		return sum;

	} // _leftEdges


	// ************************************************************************

	/**
	 * Bestimmt die jeweils weiter rechtsherum gedrehten Kanten. Das sind
	 * diejenigen, bei denen sich das bisherige Minimum der Winkelsummen
	 * verkleinert. Die erste Kante wird auf jeden Fall ausgewaehlt und ihr
	 * Drehwinkel als 0 angenommen.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 * @param inout_edges
	 *            Die Liste der aus der Berechnung resultierenden Kanten
	 * 
	 * @return Die Winkelsumme
	 */
	private double _rightEdges(
			Polygon2 input_polygon,
			SimpleList inout_edges)
	{

		inout_edges.clear();

		double sum = 0.0;
		double min = 0.0;

		PointsAccess access = new PointsAccess(input_polygon);
		Point2 source = access.prevPoint();
		Point2 target = access.prevPoint();

		// erste Kante auswaehlen, Drehwinkel nicht addieren
		Line2 line = new Line2(new Point2(source), new Point2(target));
		inout_edges.add(line);

		for (int i = 1; i < input_polygon.length(); i++)
		{
			source = target;
			target = access.cyclicPrevPoint();
			// das negative Vorzeichen ist erforderlich, da getAngle()
			// den Winkel im Gegenuhrzeigersinn liefert
			double phi = -((APoint) source).getAngle();
			sum += phi;
			if (sum < min)
			{
				min = sum;
				line = new Line2(new Point2(source), new Point2(target));
				inout_edges.add(line);
			} // if
		} // for

		return sum;

	} // _rightEdges


	// ************************************************************************

	/**
	 * Erzeugt ein dem Eingabepolygon entsprechendes Polygon, bei dem die
	 * Eckpunkte mit ihren Drehwinkel attributiert sind.
	 * 
	 * @param input_polygon
	 *            Das Eingabepolygon
	 * 
	 * @return Das entsprechend der Berechnugn resultierende Polygon
	 */
	private Polygon2 _computeAngles(
			Polygon2 input_polygon)
	{

		Polygon2 output_polygon = new Polygon2();
		output_polygon.setClosed();

		PointsAccess access = new PointsAccess(input_polygon);
		Point2 prev = access.prevPoint();
		Point2 current = access.cyclicNextPoint();
		Point2 next = access.cyclicNextPoint();

		for (int index = 0; index < input_polygon.length(); index++)
		{
			prev = current;
			current = next;
			next = access.cyclicNextPoint();

			APoint new_point = new APoint(prev, current, next);
			output_polygon.addPoint((Point2) new_point);
		} // for

		return output_polygon;

	} // _computeAngles


	// ************************************************************************

	/**
	 * Berechnet die zu den Geraden dualen Punkte.
	 * 
	 * @param input_lines
	 *            Die zu dualisierenden Geraden
	 * 
	 * @return Die Punkteliste der dualen Punkte
	 */
	private Point2List _lineToDualPoint(
			SimpleList input_lines)
	{

		Point2List dual_points = new Point2List();

		for (int i = 0; i < input_lines.length(); i++)
		{
			Line2 current = (Line2) input_lines.getValueAt(i);
			Point2 source = current.source();
			Point2 target = current.target();
			float dy = source.y - target.y;
			float dx = source.x - target.x;
			float a = dy / dx;
			float b = ((source.x * target.y) - (source.y * target.x)) / dx;
			dual_points.addPoint(a, b);
		} // while

		return dual_points;

	} // _lineToDualPoint


	// ************************************************************************

	/**
	 * Berechnet die zu den Geraden dualen Punkte.
	 * 
	 * @param input_lines
	 *            Die zu dualisierenden Geraden
	 * @param inout_table
	 *            Die Tabelle der Geraden und Punkte
	 * 
	 * @return Die Punktliste der dualen Punkte
	 */
	private Point2List _lineToDualPoint(
			SimpleList input_lines,
			Object[][] inout_table)
	{

		Point2List dual_points = new Point2List();
		int index = 0;

		for (int i = 0; i < input_lines.length(); i++)
		{
			Line2 current = (Line2) input_lines.getValueAt(i);
			Point2 source = current.source();
			Point2 target = current.target();
			float dy = source.y - target.y;
			float dx = source.x - target.x;
			float a = dy / dx;
			float b = ((source.x * target.y) - (source.y * target.x)) / dx;
			Point2 point = new Point2(a, b);
			dual_points.addPoint(point);
			inout_table[0][index] = point;
			inout_table[1][index] = current;
			index++;
		} // while

		return dual_points;

	} // _lineToDualPoint


	// ************************************************************************

	/**
	 * Berechnet die zu den Punkten dualen Geraden.
	 * 
	 * @param input_points
	 *            Die zu dualisierenden Punkte
	 * 
	 * @return Die Liste der dualen Geraden
	 */
	private SimpleList _pointToDualLine(
			Point2List input_points)
	{

		SimpleList dual_lines = new SimpleList();

		PointsAccess access = new PointsAccess(input_points);
		while (access.hasNextPoint())
		{
			Point2 current = access.nextPoint();
			Point2 source = new Point2(0, current.y);
			Point2 target = new Point2(1, current.x + current.y);
			dual_lines.add(new Line2(source, target));
		} // while

		return dual_lines;

	} // _pointToDualLine


	// ************************************************************************

	/**
	 * Gibt die zu den Punkten dualen Geraden zurueck.
	 * 
	 * @param input_points
	 *            Die zu dualisierenden Punkte
	 * @param input_table
	 *            Die Tabelle der Punkte und dualer Geraden
	 * 
	 * @return Die Liste der dualen Geraden
	 */
	private SimpleList _pointToDualLine(
			Point2List input_points,
			Object[][] input_table)
	{

		SimpleList dual_lines = new SimpleList();

		PointsAccess access = new PointsAccess(input_points);
		while (access.hasNextPoint())
		{
			Point2 current = access.nextPoint();
			Object line = null;
			int index = 0;
			while ((index < input_table[0].length) && (line == null))
			{
				if (current.equals((Point2) input_table[0][index]))
				{
					line = input_table[1][index];
				} // if
				index++;
			} // while
			if (line == null)
			{
				System.out
						.println("_pointToDualLine(): no line for " + current);
			}
			else
			{
				dual_lines.add((Line2) line);
			} // if
		} // while

		return dual_lines;

	} // _pointToDualLine


	// ************************************************************************

	/**
	 * Berechnet den Durchschnitt der unteren Halbebenen der Geraden. Dazu
	 * werden die folgenden Zwischenergebnisse berechnet: 1. die dualen Punkte
	 * der Geraden 2. die convexe Huelle der dualen Punkte 3. der untere Rand
	 * der convexen Huelle 4. die dualen Geraden zu den Eckpunkten des unteren
	 * Rands
	 * 
	 * @param input_lines
	 *            Die Liste der Geraden
	 * 
	 * @return Die Liste der Geraden, die den Durchschnitt aufspannen
	 */
	private SimpleList _lowerPlain(
			SimpleList input_lines)
	{

		// Tabelle mit Paaren von Geraden mit ihrem dualen Punkt
		Object[][] table = new Object[2][input_lines.length()];

		// duale Punkte berechnen
		Point2List dual = _lineToDualPoint(input_lines, table);
		//      Point2List dual = _lineToDualPoint( input_lines );

		// convexe Huelle berechnen
		ConvexPolygon2 hull = new ConvexPolygon2(dual);
		//      System.out.println( hull );

		// den unteren Rand der convexen Huelle berechnen

		Point2List min_x_first = _rotate(hull, _MIN_X_FIRST);
		if (min_x_first.length() <= 2)
		{
			// zwei Punkte oder weniger liegen garantiert auf dem unteren Rand,
			// die Berechnung ist also fertig
			return _pointToDualLine(min_x_first, table);
			//         return _pointToDualLine( min_x_first );
		} // if

		// die Punkte der konvexe Huelle sind im Gegenuhrzeigersinn angeordnet,
		// angefangen beim Punkt mit der kleinsten x-Koordinate werden solange
		// sukzessive Punkte ausgewaehlt, bis die x-Koordinate des Nachfolgers
		// kleiner ist als die des aktuellen Punkts oder alle Punkte bearbeitet
		// wurden
		PointsAccess access = new PointsAccess(min_x_first);
		Point2 current = null;
		Point2 next = access.nextPoint();
		Point2List bottom = new Point2List();
		bottom.addPoint(next);
		do
		{
			current = next;
			next = access.nextPoint();
			if (current.x < next.x)
			{
				bottom.addPoint(next);
			} // if
		}
		while ((access.hasNextPoint()) && (current.x < next.x));

		//      return _pointToDualLine( bottom );
		return _pointToDualLine(bottom, table);

	} // _lowerPlain


	// ************************************************************************

	/**
	 * Erzeugt eine Liste von Geraden aus den Punkten der Eingabeliste, sie
	 * entsprechen den Kanten eines durch die Punkte definierten geschlossenen
	 * Polygons.
	 * 
	 * @param input_list
	 *            Die Liste der Eingabepunkte
	 * 
	 * @return Die Liste der berechneten Geraden
	 */

	private SimpleList _lines(
			SimpleList input_list)
	{

		SimpleList lines = new SimpleList();

		if (input_list.length() < 2)
		{
			return lines;
		} // if

		Point2 current = null;
		Point2 next = (Point2) input_list.firstValue();

		for (int i = 1; i < input_list.length(); i++)
		{
			current = next;
			next = (Point2) input_list.getValueAt(i);
			lines.add(new Line2(current, next));
		} // while

		lines.add(new Line2(next, (Point2) input_list.firstValue()));

		return lines;

	} // _lines


	// ************************************************************************

	/**
	 * Kehrt die Reihenfolge der Elemente der Liste um.
	 * 
	 * @param input_list
	 *            Die Eingabeliste
	 * 
	 * @return Die umgekehrte Liste
	 */
	private SimpleList _reverse(
			SimpleList input_list)
	{

		SimpleList reverse = input_list.copyList();
		reverse.reverse();
		return reverse;

	} // _reverse


	// ************************************************************************

	/**
	 * Rotiert die Punktliste, so dass abhaengig von input_first der Punkt mit
	 * der kleinsten oder groessten x-Koordinate am Anfang steht.
	 * 
	 * @param input_points
	 *            Die Punktliste
	 * @param input_first
	 *            _MIN_X_FIRST oder _MAX_X_FIRST
	 * 
	 * @return Die resultierende Punktliste
	 */
	private Point2List _rotate(
			Point2List input_points,
			int input_first)
	{

		// Startpunkt suchen

		PointsAccess first = new PointsAccess(input_points);
		PointsAccess search = new PointsAccess(input_points);

		first.nextPoint();
		search.nextPoint();

		if (input_first == _MIN_X_FIRST)
		{

			while (search.hasNextPoint())
			{
				search.nextPoint();
				if (search.currentPoint().x < first.currentPoint().x)
				{
					first = new PointsAccess(search);
				} // if
			} // while

		}
		else if (input_first == _MAX_X_FIRST)
		{

			while (search.hasNextPoint())
			{
				search.nextPoint();
				if (search.currentPoint().x > first.currentPoint().x)
				{
					first = new PointsAccess(search);
				} // if
			} // while

		}
		else
		{
			System.out.println("_rotate(): illegal input_first");
		} // if

		// Punktliste mit dem Startpunkt als erstem Element erzeugen

		Point2List result = new Point2List();
		Point2 current = first.currentPoint();
		for (int i = 0; i < input_points.length(); i++)
		{
			result.addPoint(current);
			current = first.cyclicNextPoint();
		} // for

		return result;

	} // _rotate


	// ************************************************************************

	/**
	 * Teilt die Eingabeliste von Geraden in ostwaerts gerichtete, westwaerts
	 * gerichtete und vertikale Geraden auf und haengt sie an die entsprechenden
	 * Ein-Ausgabelisten.
	 * 
	 * @param input_list
	 *            Die aufzuteilenden Geraden
	 * @param inout_west
	 *            Die westwaerts gerichteten Geraden
	 * @param inout_vertical
	 *            Die vertikalen Geraden
	 * @param inout_east
	 *            Die ostwaerts gerichteten Geraden
	 */
	private void _split(
			SimpleList input_list,
			SimpleList inout_west,
			SimpleList inout_vertical,
			SimpleList inout_east)
	{

		for (int i = 0; i < input_list.length(); i++)
		{

			Line2 current = (Line2) input_list.getValueAt(i);
			Point2 source = current.source();
			Point2 target = current.target();

			if (source.x < target.x)
			{
				inout_east.add(current);
			}
			else if (source.x > target.x)
			{
				inout_west.add(current);
			}
			else
			{ // source.x == target.x
				inout_vertical.add(current);
			} // if

		} // while

	} // _split


	// ************************************************************************
	// Methoden zur Berechnung des Kerns aus der oberen und unteren Huelle
	// ************************************************************************

	/**
	 * Clippt den Kern an den Vertikalen, nach unten gerichtete Vertikalen
	 * bestimmen die linke Grenze, nach oben gerichtete die rechte. Es kann auch
	 * keine oder nur eine Vertikale geben, dann wird gar nicht oder nur an der
	 * entsprechenden Seite geclippt. Als Resultat koennen auch alle Punkte
	 * geclippt werden.
	 * 
	 * @param input_vertical
	 *            Die Vertikalen
	 */
	private void _cut(
			SimpleList input_vertical)
	{

		Line2[] borders = _getVerticalBorders(input_vertical);
		Line2 left_border = borders[_LEFT];
		Line2 right_border = borders[_RIGHT];
		boolean cut_left = (left_border != null);
		boolean cut_right = (right_border != null);

		if ((!cut_left) && (!cut_right))
		{
			return;
		} // if

		// Clippen an der linken Grenze

		if (cut_left)
		{
			float x_border = left_border.source().x;

			if (length() <= 1)
			{
				if ((length() == 1) && (firstPoint().x < x_border))
				{
					clear();
				} // if
				return;
			} // if

			SimpleList new_kern = new SimpleList();
			PointsAccess access = new PointsAccess(this);
			Point2 prev = null;
			Point2 current = lastPoint();
			do
			{
				prev = current;
				current = access.nextPoint();
				if ((prev.x >= x_border) && (current.x >= x_border))
				{
					new_kern.add(current);
				}
				else if ((prev.x < x_border) && (current.x >= x_border))
				{
					Segment2 seg = new Segment2(prev, current);
					Point2 point_at_x = seg.calculatePoint(x_border);
					new_kern.add(point_at_x);
					new_kern.add(current);
				}
				else if ((prev.x >= x_border) && (current.x < x_border))
				{
					Segment2 seg = new Segment2(prev, current);
					Point2 point_at_x = seg.calculatePoint(x_border);
					new_kern.add(point_at_x);
				} // if
			}
			while (access.hasNextPoint());

			// Punkte des Kerns durch das Ergebnis des Clippens ersetzen
			clear();
			for (int i = 0; i < new_kern.length(); i++)
			{
				addPoint((Point2) new_kern.getValueAt(i));
			} // while

		} // if

		// Clippen an der rechten Grenze

		if (cut_right)
		{
			float x_border = right_border.source().x;

			if (length() <= 1)
			{
				if ((length() == 1) && (firstPoint().x > x_border))
				{
					clear();
				} // if
				return;
			} // if

			SimpleList new_kern = new SimpleList();
			PointsAccess access = new PointsAccess(this);
			Point2 prev = null;
			Point2 current = lastPoint();
			do
			{
				prev = current;
				current = access.nextPoint();
				if ((prev.x <= x_border) && (current.x <= x_border))
				{
					new_kern.add(current);
				}
				else if ((prev.x > x_border) && (current.x <= x_border))
				{
					Segment2 seg = new Segment2(prev, current);
					Point2 point_at_x = seg.calculatePoint(x_border);
					new_kern.add(point_at_x);
					new_kern.add(current);
				}
				else if ((prev.x <= x_border) && (current.x > x_border))
				{
					Segment2 seg = new Segment2(prev, current);
					Point2 point_at_x = seg.calculatePoint(x_border);
					new_kern.add(point_at_x);
				} // if
			}
			while (access.hasNextPoint());

			// Punkte des Kerns durch das Ergebnis des Clippens ersetzen
			clear();
			for (int i = 0; i < new_kern.length(); i++)
			{
				addPoint((Point2) new_kern.getValueAt(i));
			} // while
		} // if

	} // _cut


	// ************************************************************************

	/**
	 * Gibt die am weitesten rechts liegende nach unten gerichtete Gerade als
	 * linke Grenze und die am weitesten links liegende nach oben gerichtete
	 * Gerade als rechte Grenze zurueck. Wenn keine entsprechende Vertikale
	 * existiert, dann ist das entsprechende Feld gleich null.
	 * 
	 * @param input_verticals
	 *            Die vertikalen Geraden
	 * 
	 * @return Ein Array mit der linken/rechten Grenze in Feld _LEFT/_RIGHT
	 */
	private Line2[] _getVerticalBorders(
			SimpleList input_verticals)
	{

		Line2[] borders = new Line2[2];

		if ((input_verticals == null) || (input_verticals.empty()))
		{
			return borders;
		} // if

		for (int i = 0; i < input_verticals.length(); i++)
		{
			Line2 current = (Line2) input_verticals.getValueAt(i);
			if (current.source().y < current.target().y)
			{
				// current zeigt nach oben
				if ((borders[_RIGHT] == null)
						|| (borders[_RIGHT].source().x > current.source().x))
				{
					borders[_RIGHT] = current;
				} // if
			}
			else
			{
				// current zeigt nach unten
				if ((borders[_LEFT] == null)
						|| (borders[_LEFT].source().x < current.source().x))
				{
					borders[_LEFT] = current;
				} // if
			} // if
		} // while

		return borders;

	} // _getVerticalBorders


	// ************************************************************************

	/**
	 * Berechnet suzessive die Schnittpunkte benachbarter Geraden.
	 * 
	 * @param input_lines
	 *            Die Geraden
	 * 
	 * @return Die Schnittpunkte
	 */
	private SimpleList _intersectionPoints(
			SimpleList input_lines)
	{

		SimpleList result = new SimpleList();

		if (!input_lines.empty())
		{
			Line2 current = null;
			Line2 next = (Line2) input_lines.firstValue();
			for (int i = 1; i < input_lines.length(); i++)
			{
				current = next;
				next = (Line2) input_lines.getValueAt(i);
				Point2 i_point = current.intersection(next, new Intersection());
				if (i_point == null)
				{
					System.out
							.println("Kern.java: _intersectionPoints() failed");
				}
				else
				{
					result.add(i_point);
				} // if
			} // while
		} // if

		return result;

	} // _intersectionPoints


	// ************************************************************************

	/**
	 * Berechnet aus den Geraden, die die obere und untere Huelle des Kerns
	 * bilden, den Kern, der auch leer sein kann.
	 * 
	 * @param input_top
	 *            Die Geraden der oberen Halbebene
	 * @param input_bottom
	 *            Die Geraden der unteren Halbebene
	 * 
	 * @return Die Eckpunkte des Kerns
	 */
	private SimpleList _plainsToKern(
			SimpleList input_top,
			SimpleList input_bottom)
	{

		input_top = _reverse(input_top);
		input_bottom = _reverse(input_bottom);

		SimpleList top_hull = _intersectionPoints(input_top);
		SimpleList bottom_hull = _intersectionPoints(input_bottom);
		int top_it = 0; // top_hull
		int bottom_it = 0; // bottom_hull
		//      ListIterator	top_it		= top_hull.listIterator();
		//      ListIterator	bottom_it	= bottom_hull.listIterator();
		boolean is_top_end = false;
		boolean is_bottom_end = false;

		Point2 top_prev = null;
		Point2 top_current = null;
		Point2 bottom_prev = null;
		Point2 bottom_current = null;

		SimpleList kern = new SimpleList();
		Intersection set = new Intersection();

		// Gerade oder Start-Strahl der oberen Huelle

		BasicLine2 top = (BasicLine2) input_top.firstValue();
		if (top_it < top_hull.length())
		{
			// Start-Strahl
			top_current = (Point2) top_hull.getValueAt(top_it++);
			top = _startRay(top, top_current);
		}
		else
		{
			// Start-Gerade
			is_top_end = true;
		} // if

		// Gerade oder Start-Strahl der unteren Huelle

		BasicLine2 bottom = (BasicLine2) input_bottom.firstValue();
		if (bottom_it < bottom_hull.length())
		{
			// Start-Strahl
			bottom_current = (Point2) bottom_hull.getValueAt(bottom_it++);
			bottom = _startRay(bottom, bottom_current);
		}
		else
		{
			// Start-Gerade
			is_bottom_end = true;
		} // if

		boolean inc_top = false;
		boolean inc_bottom = false;

		// Suche nach dem 1. Schnittpunkt

		while (top.intersection(bottom, set) == null)
		{

			if (is_top_end && is_bottom_end)
			{
				// kein Schnittpunkt: der Kern ist leer
				return kern;
			} // if

			inc_top = false;
			inc_bottom = false;

			// Bestimmung ob top, bottom oder beide weiterzusetzen sind

			if (is_top_end)
			{
				inc_bottom = true;
			}
			else if (is_bottom_end)
			{
				inc_top = true;
			}
			else
			{
				if (bottom_current.x == top_current.x)
				{
					inc_top = true;
					inc_bottom = true;
				}
				else if (bottom_current.x < top_current.x)
				{
					inc_bottom = true;
				}
				else
				{ // bottom_current.x > top_current.x
					inc_top = true;
				} // if
			} // if

			if (inc_top)
			{
				if (top_it < top_hull.length())
				{
					top_prev = top_current;
					top_current = (Point2) top_hull.getValueAt(top_it++);
					top = new Segment2(top_prev, top_current);
				}
				else
				{
					is_top_end = true;
					top = _endRay((BasicLine2) input_top.lastValue(),
							top_current);
				} // if
			} // if

			if (inc_bottom)
			{
				if (bottom_it < bottom_hull.length())
				{
					bottom_prev = bottom_current;
					bottom_current = (Point2) bottom_hull
							.getValueAt(bottom_it++);
					bottom = new Segment2(bottom_prev, bottom_current);
				}
				else
				{
					is_bottom_end = true;
					bottom = _endRay((BasicLine2) input_bottom.lastValue(),
							bottom_current);
				} // if
			} // if
		} // while
		kern.add(set.point2);

		// Suche nach dem 2. Schnittpunkt

		do
		{
			if (is_top_end && is_bottom_end)
			{
				System.out.println("_plainsToKern(): no second intersection");
				return kern;
			} // if

			inc_top = false;
			inc_bottom = false;

			// Bestimmung ob top, bottom oder beide weiterzusetzen sind

			if (is_top_end)
			{
				inc_bottom = true;
			}
			else if (is_bottom_end)
			{
				inc_top = true;
			}
			else
			{ // weder top_end noch bottom_end
				if (bottom_current.x == top_current.x)
				{
					inc_top = true;
					inc_bottom = true;
				}
				else if (bottom_current.x < top_current.x)
				{
					inc_bottom = true;
				}
				else
				{ // bottom_current.x > top_current.x
					inc_top = true;
				} // if
			} // if

			if (inc_top)
			{
				//muell:	    kern.addFirst( top_current );
				//            kern.insert( kern.first(), top_current );
				kern.Push(top_current);
				if (top_it < top_hull.length())
				{
					top_prev = top_current;
					top_current = (Point2) top_hull.getValueAt(top_it++);
					top = new Segment2(top_prev, top_current);
				}
				else
				{
					is_top_end = true;
					top = _endRay((BasicLine2) input_top.lastValue(),
							top_current);
				} // if
			} // if

			if (inc_bottom)
			{
				kern.add(bottom_current);
				if (bottom_it < bottom_hull.length())
				{
					bottom_prev = bottom_current;
					bottom_current = (Point2) bottom_hull
							.getValueAt(bottom_it++);
					bottom = new Segment2(bottom_prev, bottom_current);
				}
				else
				{
					is_bottom_end = true;
					bottom = _endRay((BasicLine2) input_bottom.lastValue(),
							bottom_current);
				} // if
			} // if
		}
		while (top.intersection(bottom, set) == null);
		kern.add(set.point2);

		return kern;

	} // _plainsToKern


	// ************************************************************************

	/**
	 * Obere und untere Huelle werden nur durch je eine Gerade bestimmt
	 * 
	 * @param input_top
	 *            Liste der oberen Gerade
	 * @param input_bottom
	 *            Liste der unteren Geraden
	 * @param input_vertical
	 *            Liste der vertikalen Geraden
	 */
	private void _quadCase(
			SimpleList input_top,
			SimpleList input_bottom,
			SimpleList input_vertical)
	{

		if (input_vertical.length() <= 1)
		{
			System.out.println("_quadCase(): not enough lines");
			return;
		} // if

		//!!NS

	} // _quadCase


	// ************************************************************************

	/**
	 * Berechnet den Anfangsstrahl, er beginnt beim Eingabepunkt und ist
	 * parallel der Eingabegeraden nach links gerichtet.
	 * 
	 * @param input_line
	 *            Die Eingabelinie
	 * @param input_source
	 *            Der Startpunkt
	 * 
	 * @return Der berechnete Strahl
	 */
	private BasicLine2 _startRay(
			BasicLine2 input_line,
			Point2 input_source)
	{

		float dx = input_line.source().x - input_line.target().x;
		float dy = input_line.source().y - input_line.target().y;

		if (dx > 0)
		{
			dx = -dx;
			dy = -dy;
		} // if

		Point2 target = new Point2(input_source.x + dx, input_source.y + dy);

		return new Ray2(input_source, target);

	} // _startRay


	// ************************************************************************

	/**
	 * Berechnet den Endstrahl, er beginnt beim Eingabepunkt und ist parallel
	 * der Eingabegeraden nach rechts gerichtet.
	 * 
	 * @param input_line
	 *            Die Eingabelinie
	 * @param input_source
	 *            Der Startpunkt
	 * 
	 * @return Der berechnete Strahl
	 */
	private BasicLine2 _endRay(
			BasicLine2 input_line,
			Point2 input_source)
	{

		float dx = input_line.source().x - input_line.target().x;
		float dy = input_line.source().y - input_line.target().y;

		if (dx < 0)
		{
			dx = -dx;
			dy = -dy;
		} // if

		Point2 target = new Point2(input_source.x + dx, input_source.y + dy);

		return new Ray2(input_source, target);

	} // _endRay


	// ************************************************************************
	// Diverse Hilfsfunktionen
	// ************************************************************************

	/**
	 * Gibt die Listenelemente zeilenweise mit dem Prefix aus, Leerstring wenn
	 * die Eingabeliste null oder leer ist.
	 * 
	 * @param input_list
	 *            Die Eingabeliste der auszugebenden Elemente
	 * @param input_prefix
	 *            Prefix für die Ausgabe
	 * 
	 * @return Ausgabe der Listenelemente
	 */
	private String _listToString(
			SimpleList input_list,
			String input_prefix)
	{

		String result = "";

		if ((input_list == null) || (input_list.empty()))
		{
			return result;
		} // if

		for (int i = 0; i < input_list.length(); i++)
		{
			result += input_prefix + input_list.getValueAt(i) + "\n";
		} // while

		return result;

	} // _listToString


	// ************************************************************************

	/**
	 * Kehrt die Richtung der Geraden um.
	 * 
	 * @param inout_lines
	 *            Die Liste der Geraden
	 */
	private void _reverseLines(
			SimpleList inout_lines)
	{

		if ((inout_lines == null) || (inout_lines.empty()))
		{
			return;
		} // if

		Point2 memo = new Point2();
		for (int i = 0; i < inout_lines.length(); i++)
		{
			Line2 current = (Line2) inout_lines.getValueAt(i);
			memo.moveTo(current.source());
			current.source().moveTo(current.target());
			current.target().moveTo(memo);
		} // while

	} // _reverseLines


	// ************************************************************************

	/**
	 * Spiegelt die Geraden an der x-Achse.
	 * 
	 * @param inout_list
	 *            Die Liste der Geraden
	 */
	private void _xMirror(
			SimpleList inout_list)
	{

		for (int i = 0; i < inout_list.length(); i++)
		{
			Line2 current = (Line2) inout_list.getValueAt(i);
			current.source().y = -current.source().y;
			current.target().y = -current.target().y;
		} // while

	} // _xMirror

	// ************************************************************************

} // class Kern

