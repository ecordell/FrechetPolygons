package anja.geom.visgraph;


import java.util.Vector;
import java.io.DataInputStream;

import anja.geom.Point2;


/**
 * Diese Klasse stellt einen Punkt des Sichtbarkeitsgraphen dar. Sie bietet die
 * Moeglichkeit, Informationen, die fuer das Erstellen des Sichtbarkeitsgraphen
 * benoetigt werden, zu speichern. Dabei wird der Punkt <em>p</em> als duale
 * Gerade <em>p*</em> angesehen, die die Form <em>p* = {y = Px * x - Py}</em>
 * hat. Die Klasse bietet auch die Moeglichkeit, den Status der dualen Gerade in
 * dem oberen/unteren Horizontbaum zu speichern. Dabei wird der Schnittpunkt und
 * die dazugehoerige Gerade gespiechert. Der kleinere Schnittpunkt aus den
 * Horizontbaeumen wird als der naechste Schnittpunkt angesehen. Gefundene
 * Sichtbarkeitssegmente koennen als ausgehende/eingehende Segemente in einem
 * Vektor gespeichert werden. (ein Segment ist ausgehend, wenn er den Punkt als
 * <em>source</em> hat.)
 * 
 * <br>Wird von der SweepLine benutzt.
 * 
 * @author Alexander Tiderko
 * 
 **/

public class VisGVertice
		extends Point2
{

	// Informationen fuer die Struktur des Sichtbarkeitsgraphen
	protected VisGEdge		_prevPEdge						= null;
	protected VisGEdge		_nextPEdge						= null;

	// Sichtbarkeitskanten, die im Sichtbarkeitsgraphen enthalten sind 
	protected Vector		_inVisEdges						= new Vector();
	protected Vector		_outVisEdges					= new Vector();

	// false gibt an, dass ins innere des Polygons gezeigt wird
	protected boolean		_radSweepActive					= false;

	// naechste sichtbare Kante
	protected VisGEdge		_radSweepPointer				= null;

	// der naechste Schnittpunkt
	protected Point2		_intersectionPoint				= null;

	// Schnittpunkte in dem Oberen/Unteren Horizontbaum und in der SweppLine
	protected Point2		_upperHorizontIntersectionPoint	= null;
	protected Point2		_lowerHorizontIntersectionPoint	= null;

	// Die Gerade, mit der diese Gerade den naechsten Schnittpunkt gemeinsam hat 
	protected VisGVertice	_intersectionLine				= null;
	protected VisGVertice	_upperHorizontIntersectionLine	= null;
	protected VisGVertice	_lowerHorizontIntersectionLine	= null;

	protected int			_index;										// Identifizierung anhand der Position in dem Array
	// Nachbarn in der SweepLine
	protected int			_upperNeighbourIndex			= -1;
	protected int			_lowerNeighbourIndex			= -1;


	//*************************************************************************

	/**
	 * Erzeugt eine Gerade zu dem Punkt(0,0)
	 */
	public VisGVertice()
	{
		super();
	}


	//*************************************************************************

	/**
	 * Liest den Punkt aus einer Datei ein.
	 * 
	 * @param dis
	 *            Input-Stream
	 */
	public VisGVertice(
			DataInputStream dis)
	{
		super(dis);
	}


	//*************************************************************************

	/**
	 * Erzeugt eine Gerade durch den Punkt, der durch die x und y Koordinate
	 * gegeben ist.
	 * 
	 * @param input_x
	 *            Der X-Wert
	 * @param input_y
	 *            Der Y-Wert
	 */
	public VisGVertice(
			float input_x,
			float input_y)
	{
		super(input_x, input_y);
	}


	//*************************************************************************

	/**
	 * Erzeugt die Gerade aus double-Koordinaten.
	 * 
	 * @param inx
	 *            Der X-Wert
	 * @param iny
	 *            Der Y-Wert
	 */
	public VisGVertice(
			double inx,
			double iny)
	{
		super(inx, iny);
	}


	//*************************************************************************

	/**
	 * Erzeugt eine Gerade mit den Koordinaten des Eingabepunktes.
	 * 
	 * @param input_point
	 *            Der Eingabepunkt
	 */
	public VisGVertice(
			Point2 input_point)
	{
		super(input_point);
	}


	//*************************************************************************

	/**
	 * Berechnet den Schnittpunkt zwischen zwei dualen Geraden. Um den
	 * Rundungsfehlern vorzubeugen, wird der Schnittpunkt zwischen
	 * <em>this->input_point</em> und dann <em>input_point->this</em> berechnet.
	 * Der groessere von den beiden wird als Schnittpunkt erzeugt und
	 * ausgegeben.
	 * 
	 * @param input_point
	 *            duale Gerade, mit der der Schnittpunkt berechnet werden soll
	 * 
	 * @return Gibt den Schnittpunkt mit der anderen Gerade zurueck. Sind die
	 *         Geraden parallel, oder liegen diese aufeinander, so wird
	 *         <b>null</b> zureuckgegeben.
	 */
	public VisGVertice intersection(
			VisGVertice input_point)
	{
		VisGVertice intersectionPoint = null;

		if (input_point != null)
		{
			double x1 = (double) x;
			double x2 = (double) input_point.x;
			double y1 = (double) y;
			double y2 = (double) input_point.y;

			if ((x1 - x2) != 0)
			{
				/* bestimme den groesseren Schnittpunkt 
				 * this->input_line oder input_line->line
				 */

				double cut_x1 = (y2 - y1) / (x2 - x1);
				double cut_y1 = x1 * cut_x1 - y1;
				double cut_x2 = (y1 - y2) / (x1 - x2);
				double cut_y2 = x2 * cut_x2 - y2;

				if (cut_x1 < cut_x2)
				{
					intersectionPoint = new VisGVertice(cut_x2, cut_y2);
				}
				else if ((cut_x1 == cut_x2) && (cut_y1 < cut_y2))
				{
					intersectionPoint = new VisGVertice(cut_x2, cut_y2);
				}
				else
				{
					intersectionPoint = new VisGVertice(cut_x1, cut_y1);
				}
			}
			else if ((input_point != null))
			{
				// wenn Geraden Parallel -> setze auf (-unendl., y)
				intersectionPoint = new VisGVertice(
						java.lang.Float.NEGATIVE_INFINITY, y);
			}
		}
		return intersectionPoint;
	}


	//*************************************************************************

	/**
	 * Gibt die Gerade mit dem naechsten Schnittpunkt zurueck.
	 * 
	 * <br>Dies ist eine der Geraden <em>_upperHorizontIntersectionLine</em> und
	 * <em>_lowerHorizontIntersectionLine</em>, deren Schnittpunkt die kleinere
	 * x-Koordinate hat.
	 * 
	 * @return Die Gerade, mit der sich diese als naechstes schneidet oder null
	 */
	public VisGVertice get_IntersectionLine()
	{
		return _intersectionLine;
	}


	//*************************************************************************

	/**
	 * Gibt den naechsten Schnittpunkt zurueck.
	 * 
	 * <br>Dies ist der Schnittpunkt mit der kleiner x-Koordinate der
	 * <em>_upperHorizontIntersectionPoint</em> und
	 * <em>_lowerHorizontIntersectionPoint</em>.
	 * 
	 * @return null, wenn kein Schnittpunkt existiert.
	 */
	public Point2 get_IntersectionPoint()
	{
		return _intersectionPoint;
	}


	//*************************************************************************

	/**
	 * Setzt die Schnittgerade, die im unteren Horizontbaum fuer diese Gerade
	 * gueltig waere.
	 * 
	 * Ist die x-Koordinate des Schnittpunktes kleiner als die im oberen
	 * Horizontbaum, so wird auch die naechste Schnittgerade, sowie naechster
	 * Schnittpunkt aktualisiert.
	 * 
	 * @param line
	 *            begrenzende Gerade im unteren Horizontbaum
	 */
	public void set_lowerHorizontIntersection(
			VisGVertice line)
	{
		_lowerHorizontIntersectionLine = line;
		_lowerHorizontIntersectionPoint = intersection(line);

		// aktualisiere den naechsten Schnittpunkt
		checkLowerAndUpperHorizont();
	}


	//*************************************************************************

	/**
	 * Setzt die Schnittgerade, die im oberen Horizontbaum fuer diese Gerade
	 * gueltig waere.
	 * 
	 * Ist die x-Koordinate des Schnittpunktes kleiner als die im unteren
	 * Horizontbaum, so wird auch die naechste Schnittgerade, sowie naechster
	 * Schnittpunkt aktualisiert.
	 * 
	 * @param line
	 *            begrenzende Gerade im oberen Horizontbaum
	 */
	public void set_upperHorizontIntersection(
			VisGVertice line)
	{
		_upperHorizontIntersectionLine = line;
		_upperHorizontIntersectionPoint = intersection(line);

		// aktualisiere den naechsten Schnittpunkt
		checkLowerAndUpperHorizont();
	}


	//*************************************************************************

	/**
	 * Testet, welcher der Geraden <em>_upperHorizontIntersectionLine</em> und
	 * <em>_lowerHorizontIntersectionLine</em> den Schnittpunkt mit der
	 * kleineren x-Koordinate hat. Diese Gerade wird als naechste Schnittgerade
	 * (_intersectionLine) gesetzt. Der dazugehoerige Schnittpunkt wird auch
	 * gesetzt.
	 */
	private void checkLowerAndUpperHorizont()
	{
		if ((_lowerHorizontIntersectionPoint == null)
				&& (_upperHorizontIntersectionPoint == null))
		{
			_intersectionLine = null;
			_intersectionPoint = null;
		}
		else if (_lowerHorizontIntersectionPoint == null)
		{
			_intersectionLine = _upperHorizontIntersectionLine;
			_intersectionPoint = _upperHorizontIntersectionPoint;
		}
		else if (_upperHorizontIntersectionPoint == null)
		{
			_intersectionLine = _lowerHorizontIntersectionLine;
			_intersectionPoint = _lowerHorizontIntersectionPoint;
		}
		else
		{
			if (_upperHorizontIntersectionPoint
					.comparePointsLexX(_lowerHorizontIntersectionPoint) < 0)
			{
				_intersectionLine = _upperHorizontIntersectionLine;
				_intersectionPoint = _upperHorizontIntersectionPoint;
			}
			else
			{
				_intersectionLine = _lowerHorizontIntersectionLine;
				_intersectionPoint = _lowerHorizontIntersectionPoint;
			}
		}
	}


	//*************************************************************************

	/**
	 * Berechnet die Schnittgerade, die diese Gerade im oberen Horizontbaum
	 * beschränkt.
	 * 
	 * @return Die Schnittgerade
	 */
	public VisGVertice get_upperHorizontIntersectionLine()
	{
		return _upperHorizontIntersectionLine;
	}


	//*************************************************************************

	/**
	 * Berechnet die Schnittgerade, die diese Gerade im unteren Horizontbaum
	 * beschränkt.
	 * 
	 * @return Die Schnittgerade
	 */
	public VisGVertice get_lowerHorizontIntersectionLine()
	{
		return _lowerHorizontIntersectionLine;
	}


	//*************************************************************************

	/**
	 * Berechnet den Schnittpunkt mit der Schnittgeraden, die diese Gerade im
	 * unteren Horizontbaum beschraenkt.
	 * 
	 * @return Der Schnittpunkt
	 */
	public Point2 get_lowerHorizontIntersectionPoint()
	{
		return _lowerHorizontIntersectionPoint;
	}


	//*************************************************************************

	/**
	 * Berechnet den Schnittpunkt mit der Schnittgeraden, die diese Gerade im
	 * oberen Horizontbaum beschraenkt.
	 * 
	 * @return Der Schnittpunkt
	 */
	public Point2 get_upperHorizontIntersectionPoint()
	{
		return _upperHorizontIntersectionPoint;
	}


	//*************************************************************************

	/**
	 * Setzt den Index, der die Position dieser Gerade im Array der SweepLine
	 * angibt. Der Index wird bei der Initialisierung gesetzt und sollte sich
	 * nicht aendern.
	 * 
	 * @param i
	 *            Index in dem Array der SweepLine-Klasse
	 */
	public void setIndex(
			int i)
	{
		_index = i;
	}


	//*************************************************************************

	/**
	 * Gibt die Position im Array der SweepLine zurueck.
	 * 
	 * @return Die Position im Array
	 */
	public int getIndex()
	{
		return _index;
	}


	//*************************************************************************

	/**
	 * Gibt den unteren Nachbarn in der SweepLine zurueck. Die Nachbarn werden
	 * bei der Initialisierung der SweepLine gesetzt und nach jedem Schnittpunkt
	 * aktualisiert.
	 * 
	 * @return Index der unteren Nachbargerade
	 */
	public int getLowerNeighbourIndex()
	{
		return _lowerNeighbourIndex;
	}


	//*************************************************************************

	/**
	 * Gibt den oberen Nachbarn in der SweepLine zurueck. Die Nachbarn werden
	 * bei der Initialisierung der SweepLine gesetzt und nach jedem Schnittpunkt
	 * aktualisiert.
	 * 
	 * @return Index der oberen Nachbargerade, oder -1, wenn keine Nachbargerade
	 *         existiert.
	 */
	public int getUpperNeighbourIndex()
	{
		return _upperNeighbourIndex;
	}


	//*************************************************************************

	/**
	 * Setzt den Index des unteren Nachbarn in der SweepLine. Dieser wird nach
	 * jedem Schnittpunkt aktualisiert.
	 * 
	 * @param i
	 *            Index der unteren Nachbargerade, oder -1, wenn keine
	 *            Nachbargerade existiert.
	 */
	public void setLowerNeighbourIndex(
			int i)
	{
		_lowerNeighbourIndex = i;
	}


	//*************************************************************************

	/**
	 * Setzt den Index des oberen Nachbarn in der SweepLine. Dieser wird nach
	 * jedem Schnittpunkt aktualisiert.
	 * 
	 * @param i
	 *            Index der oberen Nachbargerade
	 */
	public void setUpperNeighbourIndex(
			int i)
	{
		_upperNeighbourIndex = i;
	}


	//*************************************************************************

	/**
	 * Gibt die naechste Polygon-Kante zurueck.
	 * 
	 * In der SweepLine wird davon ausgegangen, dass das Polygon linksorientiert
	 * ist.
	 * 
	 * @return Die naechste Polygon-Kante
	 */
	public VisGEdge get_nextPEdge()
	{
		return _nextPEdge;
	}


	//*************************************************************************

	/**
	 * Gibt die vorherige Polygon-Kante zurueck.
	 * 
	 * In der SweepLine wird davon ausgegangen, dass das Polygon linksorientiert
	 * ist.
	 * 
	 * @return Die vorherige Polygon-Kante
	 */
	public VisGEdge get_prevPEdge()
	{
		return _prevPEdge;
	}


	//*************************************************************************

	/**
	 * Setzt die naechste Polygon-Kante fuer diesen Punkt. In der SweepLine wird
	 * davon ausgegangen, dass das Polygon linksorientiert ist.
	 * 
	 * @param edge
	 *            Kante mit dem <b>this</b> als Quelle.
	 */
	public void set_nextPEdge(
			VisGEdge edge)
	{
		_nextPEdge = edge;
	}


	//*************************************************************************

	/**
	 * Setzt die vorherige Polygon-Kante fuer diesen Punkt. In der SweepLine
	 * wird davon ausgegangen, dass das Polygon linksorientiert ist.
	 * 
	 * @param edge
	 *            Kante mit dem <b>this</b> als Ziel.
	 */
	public void set_prevPEdge(
			VisGEdge edge)
	{
		_prevPEdge = edge;
	}


	//*************************************************************************

	/**
	 * Berechnet die nächste sichtbare Kante von diesem Punkt aus
	 * 
	 * @return Die naechste sichtbare Kante
	 */
	public VisGEdge get_radSweepPointer()
	{
		return _radSweepPointer;
	}


	//*************************************************************************

	/**
	 * Setzt die von diesem Punkt aus naechste sichtbare Kante
	 * 
	 * @param edge
	 *            Die Kante
	 */
	public void set_radSweepPointer(
			VisGEdge edge)
	{
		_radSweepPointer = edge;
	}


	//*************************************************************************

	/**
	 * Gibt den Status des Punktes zurueck.
	 * 
	 * Wenn der <em>_radSweepPointer</em> ins innere eines Polygons zeigt, dann
	 * muessen von diesem Punkt aus die Segmente nicht ueberprueft werden, da
	 * sie sowieso einen Schnitt mit einer der Polygonkante haben.
	 * 
	 * @return <b>true</b>, wenn der Punkt aktiv ist, sonst <b>false</b>
	 */
	public boolean is_active()
	{
		return _radSweepActive;
	}


	//*************************************************************************

	/**
	 * Setzt den Status des Punktes.
	 * 
	 * Wenn der <em>_radSweepPointer</em> ins innere eines Polygons zeigt, dann
	 * muessen von diesem Punkt aus die Segmente nicht ueberprueft werden, da
	 * sie sowieso einen Schnitt mit einer der Polygonkante haben.
	 * 
	 * @param b
	 *            <b>true</b>, wenn der Punkt aktiv ist, sonst <b>false</b>
	 */
	public void set_active(
			boolean b)
	{
		_radSweepActive = b;
	}


	//*************************************************************************

	/**
	 * Fuegt eine eingehende Sichtbarkeitskante hinzu. Die Kante ist eingehend,
	 * wenn diese den Punkt <em>this</em> als Zielpunkt hat.
	 * 
	 * @param edge
	 *            Sichtbarkeitskante, die im Sichtbarkeitsgraph enthalten ist.
	 */
	public boolean addInVisEdge(
			VisGEdge edge)
	{
		return _inVisEdges.add(edge);
	}


	//*************************************************************************

	/**
	 * Gibt eine eingehende Sichtbarkeitskante an der stelle <em>index</em>
	 * zurueck. Die Kante ist eingehend, wenn diese den Punkt <em>this</em> als
	 * Zielpunkt hat.
	 * 
	 * @param index
	 *            Position innerhalb des Vektors. Sollte mit
	 *            <em>sizeInVisEdges()</em> ueberprueft werden, damit keine
	 *            Bereichtsueberschreitung stattfindet.
	 * 
	 * @return Sichtbarkeitskante, die im Sichtbarkeitsgraph enthalten ist.
	 */
	public VisGEdge getInVisEdge(
			int index)
	{
		return (VisGEdge) _inVisEdges.get(index);
	}


	//*************************************************************************

	/**
	 * Gibt die Anzahl der eingehenden Sichtbarkeitskanten zurück.
	 * 
	 * @return Die Anzahl der eingehenden VisEdges
	 */
	public int sizeInVisEdges()
	{
		return _inVisEdges.size();
	}


	//*************************************************************************

	/**
	 * Fuegt eine ausgehende Sichtbarkeitskante hinzu.
	 * 
	 * Die Kante ist ausgehend, wenn diese den Punkt <em>this</em> als
	 * Quellpunkt hat.
	 * 
	 * @param edge
	 *            Sichtbarkeitskante, die im Sichtbarkeitsgraph enthalten ist.
	 */
	public boolean addOutVisEdge(
			VisGEdge edge)
	{
		return _outVisEdges.add(edge);
	}


	//*************************************************************************

	/**
	 * Gibt eine ausgehende Sichtbarkeitskante an der stelle <em>index</em>
	 * zurueck. Die Kante ist ausgehend, wenn diese den Punkt <em>this</em> als
	 * Quellpunkt hat.
	 * 
	 * @param index
	 *            Position innerhalb des Vektors. Sollte mit
	 *            <em>sizeOutVisEdges()</em> ueberprueft werden, damit keine
	 *            Bereichtsueberschreitung stattfindet.
	 * 
	 * @return Sichtbarkeitskante, die im Sichtbarkeitsgraph enthalten ist.
	 */
	public VisGEdge getOutVisEdge(
			int index)
	{
		return (VisGEdge) _outVisEdges.get(index);
	}


	//*************************************************************************

	/**
	 * Gibt die Anzahl der ausgehenden Sichtbarkeitskanten zurück.
	 * 
	 * @return Die Anzahl der ausgehenden VisEdges
	 */
	public int sizeOutVisEdges()
	{
		return _outVisEdges.size();
	}

	//*************************************************************************
}
