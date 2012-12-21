package anja.geom;


import java.awt.Graphics2D;

import anja.util.GraphicsContext;
import anja.util.ListItem;
import anja.util.SimpleList;

import java.awt.Color;
import anja.geom.Circle2;


/**
 * VisibilityCuts berechnet die Visibility Cuts (nicht die Essential Cuts) eines
 * Polygons und stellt sie graphisch dar.
 * 
 * @author Andrea Eubeler
 */

public class VisibilityCuts
		implements java.io.Serializable
{

	//****** private variables ******************************//

	/** Liste der Visibility Cuts */
	private SimpleList	VisCuts			= new SimpleList();

	/** das Eingabepolygon */
	private Polygon2	input_polygon	= null;

	/** interne Variablen */
	private static boolean	start_point_set	= false, cuts_computed = false;

	/** Orientierung des Polygons */
	public final static byte	ORIENTATION_LEFT	= 1, ORIENTATION_RIGHT = 2;

	private byte				polygon_orientation	= ORIENTATION_LEFT;

	// Farben und Groesse fuer den Bezugspunkt:
	private static final Color	_POINT_EDGE_COLOR	= Color.black;				// Kanten
	private static final Color	_POINT_FILL_COLOR	= Color.green;				// Flaeche
	private static final int	_POINT_FILL_STYLE	= 1;						// fuellen
	private static final float	_POINT_RADIUS		= 3.0f;					// Radius
	private GraphicsContext		_point_gc;

	private boolean				show_start_point;


	//****** class constructor ******************************//

	/**
	 * Erzeugt eine neue Instanz von Visibility Cuts
	 */
	public VisibilityCuts()
	{
		_point_gc = new GraphicsContext();
		_point_gc.setForegroundColor(_POINT_EDGE_COLOR);
		_point_gc.setFillColor(_POINT_FILL_COLOR);
		_point_gc.setFillStyle(_POINT_FILL_STYLE);

		show_start_point = false;
	}


	/*
	* erzeugt eine neue Instanz von Visibility Cuts und
	* bindet die Cuts an ein Polygon
	*
	* @param Polygon2 das Eingabepolygon
	*/

	/*public VisibilityCuts (Polygon2 polygon)
	{
	   input_polygon = ((Polygon2) polygon.clone());
	} // VisibilityCuts
	*/

	//************************************************************//

	/**
	 * Setzt das Bezugspolygon der Cuts
	 */
	public void setInputPolygon(
			Polygon2 poly)
	{
		if (poly != null)
		{
			input_polygon = poly;
		}

	} // setInputPolygon


	//******************************************************************

	/**
	 * Setzt die Orientierung des Polygons, das durch den PointsAccess
	 * repraesentiert wird
	 * 
	 * @param ori
	 *            Die Orientierung
	 */
	public void setOrientation(
			byte ori)
	{

		polygon_orientation = ori;

	} // setOrientation


	// *******************************************************************

	/**
	 * Gibt die Orientierung des Polygons zurueck, das durch den PointsAccess
	 * repraesentiert wird
	 * 
	 * @return Die Orientierung
	 */
	public byte getOrientation()
	{

		return polygon_orientation;

	} // getOrientation


	//************************************************************//

	/**
	 * Ueberprueft, ob Startpunkt des Suchpfades gesetzt wurde
	 * 
	 * @return Startpunkt gesetzt true, sonst false
	 */
	public boolean start_point_is_set()
	{

		return start_point_set;

	} // start_point_is_set


	//************************************************************//

	/**
	 * Ueberprueft, ob Cuts berechnet wurden
	 * 
	 * @return Cuts berechnet true, sonst false
	 */
	public boolean cuts_already_computed()
	{

		return cuts_computed;

	} // start_point_is_set


	//************************************************************//

	/**
	 * Setzt den Startpunkt des Suchpfades und macht diesen zur Startecke des
	 * geschlossenen Polygons
	 * 
	 * @param search_path
	 *            Der Suchpfad
	 */
	public void setStartingPoint(
			Polygon2 search_path)
	{

		// Fkt. 'setStartingPoint':
		// Startpunkt wird auf Polygonrand verschoben:
		// 1. aendere search_path
		// 2. aendere geschlossenes Polygon

		int index = 0;

		if ((input_polygon != null) && (!(start_point_set)))
		{
			// 1. aendere search_path, d.h. verschiebe Startpunkt des
			// search_paths auf den Polygonrand
			ListItem sp_point = search_path.points().first();
			Point2 crossing_point = input_polygon
					.closestPoint((Point2) sp_point.value());
			((Point2) sp_point.value()).moveTo(crossing_point);

			// 2. aendere geschlossenes Polygon:
			// Punkt muss nur in Polygoneckenliste eingefuegt werden,
			// wenn er keine bereits vorhandene Ecke ist.
			// D.h. die neue Ecke liegt auf einer Polygonkante, die jetzt in
			// zwei Teile geteilt wird
			SimpleList poly_points = input_polygon.points();
			if (!poly_points.contains(sp_point))
			{
				// ListItem an der Stelle des Kantenindexes
				index = (int) crossing_point.getValue();
				index++;
				ListItem edge_end_point = poly_points.at(index);

				// fuege crossing_point vor edge_end_point in Polygoneckenliste ein
				poly_points.insert(edge_end_point, crossing_point);

			}
			else
			{
				index = poly_points.getIndex(sp_point);
			} // else

			// Umsortieren der Eckenreihenfolge, damit Startecke erste Ecke wird
			for (int runvar = 0; runvar < index; runvar++)
			{
				// Ecken von 0 bis index-Ecke-1 werden vom Anfang der Liste
				// entfernt und am Ende der Liste wieder eingefuegt
				poly_points.add(poly_points.remove(poly_points.at(0)));

			} // for

			start_point_set = true;

		} // if

	} // setStartingPoint


	//************************************************************//

	/**
	 * Berechnet die Visibility Cuts in O(n^2) (O(n log n) ist moeglich)
	 */
	public void _computeCuts()

	{

		//************************************************
		// Variablen
		//************************************************

		int i = 0, j = 0;

		// PointsAccess wird mit input_polygon uebergeben,
		// da Orientierung des Polygons fuer Berechnung der
		// reflexen Ecken benoetigt wird
		PointsAccess points = new PointsAccess(input_polygon);
		PointsAccess points2 = new PointsAccess(input_polygon);
		PointsAccess points3 = new PointsAccess(input_polygon);

		points.setOrientation(polygon_orientation);
		points2.setOrientation(polygon_orientation);
		points3.setOrientation(polygon_orientation);

		Point2 vertex = new Point2();
		Point2 vertex2 = new Point2();
		Point2 intersection = null;
		Point2 new_vertex1;
		Point2 new_vertex2;

		Segment2 new_segment = new Segment2();
		Segment2 line_i = new Segment2();
		Segment2 line_j = new Segment2();

		// Schnittpunkt der beiden Kanten i und j
		// wird in result gespeichert
		InspectResult result;

		//*************************************************

		cuts_computed = true;

		//*************************************************

		// alle Zeiger eine Stelle vorruecken, damit Ecke
		// mit Index 0 erste Ecke ist
		if (points.hasNextPoint())
			vertex = points.nextPoint();
		if (points2.hasNextPoint())
			vertex = points2.nextPoint();
		if (points3.hasNextPoint())
			vertex = points3.nextPoint();

		//*************************************
		// teste jede Kante mit jeder => O(n^2)
		//*************************************
		while (points.hasNextPoint())
		{
			vertex = points.nextPoint();
			i++;

			if (points.isReflex())
			{
				//***********************************************
				// vertex i mit vertex i-1 getestet mit j und j+1
				//***********************************************
				double min_lambda = Double.POSITIVE_INFINITY;
				int crossing_edge = -1;
				intersection = null;
				j = 0;
				vertex2 = points2.reset();

				while (vertex2 != null)
				{

					if ((j < i - 2) || (j > i))
					{
						line_i.setSource(points.getPrec());
						line_i.setTarget(vertex);

						line_j.setSource(vertex2);
						line_j.setTarget(points2.getSucc());

						// ermittelt Schnittpunkt beider Liniensegmente
						result = line_i.inspectBasicLine(line_j);

						if (!result.parallel)
						{
							if (result.lambda > 1.0
									&& result.lambda < min_lambda
									&& result.mue >= 0.0 && result.mue <= 1.0)
							{
								min_lambda = result.lambda;
								intersection = (Point2) result.intersectionPoint
										.clone();
								crossing_edge = j;
							}
						} // !parallel
					} // if j

					vertex2 = points2.nextPoint();
					j++;

				} //while points2

				if ((crossing_edge < i) && (intersection != null))
				{
					new_vertex1 = (Point2) vertex.clone();
					new_vertex2 = (Point2) intersection.clone();

					new_segment.setSource(new_vertex1);
					new_segment.setTarget(new_vertex2);
					VisCuts.add((Segment2) new_segment.clone());

				}

				//***********************************************
				// vertex i mit vertex i+1 getestet mit j und j+1
				//***********************************************
				j = 0;
				vertex2 = points3.reset();
				min_lambda = Double.NEGATIVE_INFINITY;
				crossing_edge = -1;

				while (vertex2 != null)
				{

					if ((j < i - 1) || (j > i + 1))
					{

						line_i.setSource(vertex);
						line_i.setTarget(points.getSucc());

						line_j.setSource(vertex2);
						line_j.setTarget(points3.getSucc());

						// ermittelt Schnittpunkt beider Liniensegmente
						result = line_i.inspectBasicLine(line_j);

						if (!result.parallel)
						{
							if (result.lambda < 0.0
									&& result.lambda > min_lambda
									&& result.mue >= 0.0 && result.mue <= 1.0)
							{
								min_lambda = result.lambda;
								intersection = (Point2) result.intersectionPoint
										.clone();
								crossing_edge = j;
							} // if
						} // !parallel

					} // if j

					vertex2 = points3.nextPoint();
					j++;

				} //while points3

				if (crossing_edge > i)
				{
					new_vertex1 = (Point2) vertex.clone();
					new_vertex2 = (Point2) intersection.clone();

					new_segment.setSource(new_vertex1);
					new_segment.setTarget(new_vertex2);

					VisCuts.add((Segment2) new_segment.clone());
				}

			} // if reflex
		} // while i

	} // _computeCuts


	/**
	 * Gibt die Cuts in einer Segment2 Liste zurueck.
	 * 
	 * @return Array der Cuts
	 */
	public Segment2[] getVisiCuts()

	{

		int cuts_number = VisCuts.length();
		Segment2 output_Cuts[] = new Segment2[cuts_number];

		for (int i = 0; i < cuts_number; i++)
		{

			Segment2 temp = (Segment2) VisCuts.at(i).value();
			Point2 x = temp.getLeftPoint();
			Point2 y = temp.getRightPoint();
			output_Cuts[i] = new Segment2(x, y);
		}

		return output_Cuts;

	}


	/**
	 * Gibt die Cuts in einer Segment2 Liste zurueck.
	 * 
	 * Source is equal reflex vertex
	 * 
	 * @return Visibility cuts list
	 */
	public Segment2[] getVisiCutsSourceIsReflex()

	{

		int cuts_number = VisCuts.length();
		Segment2 output_Cuts[] = new Segment2[cuts_number];

		for (int i = 0; i < cuts_number; i++)
		{

			Segment2 temp = (Segment2) VisCuts.at(i).value();
			Point2 x = temp.source();
			Point2 y = temp.target();
			output_Cuts[i] = new Segment2(x, y);
		}// for

		return output_Cuts;

	}


	/**
	 * Gibt die Anzahl der Cuts zurueck
	 */
	public int getLength()
	{
		return VisCuts.length();
	}


	//************************************************************//

	/**
	 * Zeichnet die Cuts in das Polygon
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		for (int i = 0; i < VisCuts.length(); i++)
		{
			((Segment2) VisCuts.at(i).value()).draw(g, gc);
		}

		if (show_start_point)
		{
			Circle2 circle = new Circle2(input_polygon.firstPoint(),
					_POINT_RADIUS);
			circle.draw(g, _point_gc);
		} //if 

	}// draw


	/**
	 * Setzt den Startpunkt zurueck
	 */
	public void resetStartPoint()
	{
		start_point_set = false;
	}


	/**
	 * Gibt an, ob der Startpunkt des Pfades auch eingezeichnet werden soll
	 * 
	 * @param value
	 *            true, falls der Startpunkt gezeichnet werden soll, sonst false
	 */
	public void showStartPoint(
			boolean value)
	{
		show_start_point = value;
	}


	//***********************************************************//

	/**
	 * Stringdarstellung der Visibility Cuts
	 * 
	 * @return Textuelle Repräsentation
	 */
	public String toString()
	{
		StringBuffer str = new StringBuffer("");
		str.append("VCP[");

		PointsAccess draw_points = new PointsAccess(input_polygon);
		while (draw_points.hasNextPoint())
		{
			Point2 vx = draw_points.nextPoint();
			str.append(vx.toString());
		}

		str.append("]");
		return str.toString();

	}

	//************************************************************//

} // VisibilityCuts
