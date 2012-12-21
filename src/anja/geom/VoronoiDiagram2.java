package anja.geom;


import java.awt.Color;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import anja.util.GraphicsContext;
import anja.util.Matrix33;
import anja.util.SimpleList;
import anja.util.Sorter;


/**
 * Zweidimensionales, zeichenbares Voronoi-Diagramm einer Punktmenge.
 * 
 * Die Vorgehensweise der Berechnung ist uebernommen aus:<br><i>Franco P.
 * Preparata, Michael Ian Shamos. Computational Geometry. Kapitel 5: Proximity:
 * Fundamental Algorithms, Seite 215.</i>
 * 
 * Die Laufzeit dieses Algorithmus liegt in <i>O</i>(<i>n</i> log <i>n</i>).
 * 
 * @version 0.9 15.04.2003
 * @author Sascha Ternes
 */

public class VoronoiDiagram2
		extends Point2List
{

	//**************************************************************************
	// Public constants
	//**************************************************************************

	/**
	 * Standart-Zeichenfarbe eines Punkts
	 */
	public static final Color	COLOR_POINT		= Color.BLACK;

	/**
	 * Standart-Zeichengroesse eines Punkts in Pixel
	 */
	public static final int		WIDTH_POINT		= 5;

	/**
	 * Standart-Pinselform zum Zeichnen eines Punkts
	 */
	public static final int		CAP_POINT		= BasicStroke.CAP_SQUARE;

	/**
	 * Standart-Zeichenfarbe eines Bisektors
	 */
	public static final Color	COLOR_BISECTOR	= Color.BLACK;

	/**
	 * Standart-Zeichenbreite eines Bisektors in Pixel
	 */
	public static final int		WIDTH_BISECTOR	= 1;

	/**
	 * Standart-Pinselform zum Zeichnen eines Bisektors
	 */
	public static final int		CAP_BISECTOR	= BasicStroke.CAP_SQUARE;

	/**
	 * Standart-Zeichenfarbe eines Voronoi-Knotens
	 */
	public static final Color	COLOR_VERTEX	= Color.BLUE;

	/**
	 * Standart-Zeichengroesse eines Voronoi-Knotens in Pixel
	 */
	public static final int		WIDTH_VERTEX	= 4;

	/**
	 * Standart-Pinselform zum Zeichnen eines Voronoi-Knotens
	 */
	public static final int		CAP_VERTEX		= BasicStroke.CAP_SQUARE;

	//**************************************************************************
	// Protected variables
	//**************************************************************************

	/**
	 * Menge der Voronoi-Knoten; Inhalt sind Objekte der Klasse <a
	 * href="VoronoiVertex2.html"><code>VoronoiVertex2</code></a>
	 */
	protected Vector			_vertices;

	/**
	 * Menge der Bisektoren; Inhalt sind Objekte der Klasse <a
	 * href="VoronoiBisector2.html"><code>VoronoiBisector2</code></a>
	 */
	protected Vector			_bisectors;

	/**
	 * Verzeichnis der Zuordnungen von Punkten zu Bisektoren; jedem Punkt ist
	 * eine Liste zugeordnet, die seine Bisektoren zu anderen Punkten enthaelt
	 */
	protected HashMap			_p2b			= new HashMap();

	/**
	 * Flag fuer den Merge-Schritt; beschreibt ein Voronoi-Diagramm, dessen
	 * Punktmenge nur aus Punkten mit gleicher y-Koordinate besteht; die Punkte
	 * bilden also eine horizontale Linie und ihr Voronoi-Diagramm besteht nur
	 * aus parallelen Geraden
	 */
	protected boolean			_all_points_have_same_y;

	/**
	 * Flag fuer den Merge-Schritt; beschreibt ein Voronoi-Diagramm, dessen
	 * Punkte alle auf einer Geraden liegen und dessen Bisektoren daher
	 * ausnahmslos zueinander parallele Geraden sind (bei zwei Punkten ist
	 * dieses Flag immer <code>true</code>)
	 */
	protected boolean			_all_bisectors_are_parallel;

	/**
	 * Flag fuer das Zeichnen der Punkte der Punktmenge
	 */
	protected boolean			_draw_points;

	/**
	 * Flag fuer das Zeichnen der Bisektoren
	 */
	protected boolean			_draw_bisectors;

	/**
	 * Flag fuer das Zeichnen der Voronoi-Knoten
	 */
	protected boolean			_draw_vertices;

	/**
	 * Internes Flag fuer notwendige Neusortierung der Punkte nach Einfuegen
	 */
	protected boolean			_sorted;

	/**
	 * Internes Flag fuer notwendige Neuberechnung des Voronoi-Diagramms
	 */
	protected boolean			_recompute;

	/**
	 * Aussehen der Punkte
	 */
	protected GraphicsContext	_gc_points;

	/**
	 * Aussehen der Bisektoren
	 */
	protected GraphicsContext	_gc_bisectors;

	/**
	 * Aussehen der Voronoi-Knoten
	 */
	protected GraphicsContext	_gc_vertices;


	//**************************************************************************
	// Constructors
	//**************************************************************************

	/**
	 * Erzeugt ein neues leeres Voronoi-Diagram.
	 */
	public VoronoiDiagram2()
	{

		super();
		_init();

	} // VoronoiDiagram2


	/**
	 * Erzeugt ein neues Voronoi-Diagramm mit einer Punktliste als Kopie der
	 * Eingabeliste; die Punkte der neuen Liste sind Kopien - nicht etwa
	 * Referenzen - der Punkte der Eingabeliste.
	 * 
	 * @param input_points
	 *            die Eingabe-Punktliste
	 */
	public VoronoiDiagram2(
			Point2List input_points)
	{

		this();
		appendCopy(input_points);

	} // VoronoiDiagram2


	//**************************************************************************
	// Public methods
	//**************************************************************************

	/**
	 * Zeichnet das Voronoi-Diagramm gemaess den Zeicheneinstellungen.
	 * 
	 * @param g2d
	 *            das <code>Graphics2D</code>-Objekt der Zeichenflaeche
	 */
	public void draw(
			Graphics2D g2d)
	{

		// Neuberechnung:
		recompute();

		// Zeichnen der Bisektoren:
		if (_draw_bisectors)
		{
			Enumeration e = _bisectors.elements();
			while (e.hasMoreElements())
			{
				((VoronoiBisector2) e.nextElement()).draw(g2d, _gc_bisectors);
			} // while
		} // if

		// Zeichnen der Punkte:
		if (_draw_points)
		{
			Enumeration e = _points.values();
			while (e.hasMoreElements())
			{
				((Point2) e.nextElement()).draw(g2d, _gc_points);
			} // while
		} // if

		// Zeichnen der Voronoi-Knoten:
		if (_draw_vertices)
		{
			Enumeration e = _vertices.elements();
			while (e.hasMoreElements())
			{
				((VoronoiVertex2) e.nextElement()).draw(g2d, _gc_vertices);
			} // while
		} // if

	} // draw


	/**
	 * Prueft, ob das Voronoi-Diagramm neu berechnet werden muss, und veranlasst
	 * gegebenenfalls eine Neuberechnung.
	 */
	public void recompute()
	{

		if (!_recompute)
			return;

		int n = _points.length();

		// initialisiere Datenstrukturen:
		_initDataStructures();

		// Abbruch bei maximal einem Punkt:
		if (n <= 1)
			return;

		// Punkte falls noetig nach x-Koordinate sortieren:
		if (!_sorted)
		{
			sort(new PointComparitor(PointComparitor.X_ORDER), Sorter.ASCENDING);
			_sorted = true;
		} // if

		// bei zwei Punkten den einzigen Bisektor berechnen:
		if (n == 2)
		{
			_simpleTwoPointDiagram();
		} // if

		// wenn alle Punkte die gleiche x-Koordinate haben, Parallelen berechnen:
		else if (firstPoint().x == lastPoint().x)
		{
			_simpleMultiplePointDiagram();

			// sonst rekursive Berechnung:
		}
		else
		{ // if
			// zwei Kopien der Punkte anlegen:
			Point2List copy1 = new Point2List();
			Point2List copy2 = new Point2List();
			// die Punktliste auf die beiden Listen aufteilen:
			Point2 curr = null;
			Enumeration e = _points.values();
			// die erste Haelfte der Punkte in die erste Liste stecken:
			for (int i = 0; i < n / 2; i++)
			{
				curr = (Point2) e.nextElement();
				copy1.addPoint(curr);
			} // for
			boolean cont = true;
			boolean same_x = false;
			// falls es weitere Punkte mit gleichem x gibt, diese in Liste 1:
			while (cont && e.hasMoreElements())
			{
				Point2 next = (Point2) e.nextElement();
				if (next.x == curr.x)
				{
					copy1.addPoint(next);
				}
				else
				{ // if
					copy2.addPoint(next);
					cont = false;
				} // else
			} // while
			// den Rest in die zweite Liste stecken:
			while (e.hasMoreElements())
			{
				copy2.addPoint((Point2) e.nextElement());
			} // while

			// Vor(S1) und Vor(S2) berechnen:
			VoronoiDiagram2 vor1 = null;
			VoronoiDiagram2 vor2 = null;
			if (copy2.length() == 0)
			{
				float x = copy1.lastPoint().x;
				while (copy1.firstPoint().x != x)
				{
					copy2.addPoint(copy1.firstPoint());
					copy1.removeFirstPoint();
				} // while
				vor1 = new VoronoiDiagram2(copy2);
				vor2 = new VoronoiDiagram2(copy1);
			}
			else
			{ // if
				vor1 = new VoronoiDiagram2(copy1);
				vor2 = new VoronoiDiagram2(copy2);
			} // else
			vor1.recompute();
			vor2.recompute();
			// beide Diagramme zum Gesamtdiagramm vereinigen:
			_mergeDiagrams(vor1, vor2);
		} // else

		_recompute = false;

	} // recompute


	/**
	 * Liefert das aktuelle Aussehen der Punkte.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen der Punkte verwendet wird
	 */
	public GraphicsContext getPointsGraphicsContext()
	{

		return _gc_points;

	} // getPointsGraphicsContext


	/**
	 * Liefert das aktuelle Aussehen der Bisektoren.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen der Bisektoren verwendet wird
	 */
	public GraphicsContext getBisectorsGraphicsContext()
	{

		return _gc_bisectors;

	} // getBisectorsGraphicsContext


	/**
	 * Liefert das aktuelle Aussehen der Voronoi-Knoten.
	 * 
	 * @return das aktuelle <code>GraphicsContext</code>-Objekt, das zum
	 *         Zeichnen der Voronoi-Knoten verwendet wird
	 */
	public GraphicsContext getVerticesGraphicsContext()
	{

		return _gc_vertices;

	} // getVerticesGraphicsContext


	/**
	 * Stellt das Zeichnen der Punkte an oder ab.
	 * 
	 * @param enable
	 *            <code>true</code> schaltet das Zeichnen der Punkte ein,
	 *            <code>false</code> schaltet das Zeichnen der Punkte aus
	 */
	public void setDrawPoints(
			boolean enable)
	{

		_draw_points = enable;

	} // setDrawPoints


	/**
	 * Stellt das Zeichnen der Bisektoren an oder ab.
	 * 
	 * @param enable
	 *            <code>true</code> schaltet das Zeichnen der Bisektoren ein,
	 *            <code>false</code> schaltet das Zeichnen der Bisektoren aus
	 */
	public void setDrawBisectors(
			boolean enable)
	{

		_draw_bisectors = enable;

	} // setDrawBisectors


	/**
	 * Stellt das Zeichnen der Voronoi-Knoten an oder ab.
	 * 
	 * @param enable
	 *            <code>true</code> schaltet das Zeichnen der Voronoi-Knoten
	 *            ein, <code>false</code> schaltet das Zeichnen der
	 *            Voronoi-Knoten aus
	 */
	public void setDrawVertices(
			boolean enable)
	{

		_draw_vertices = enable;

	} // setDrawVertices


	/**
	 * Setzt das Aussehen der Punkte.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen der
	 *            Punkte verwendet wird
	 */
	public void setPointsGraphicsContext(
			GraphicsContext gc)
	{

		_gc_points = gc;

	} // setPointsGraphicsContext


	/**
	 * Setzt das Aussehen der Bisektoren.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen der
	 *            Bisektoren verwendet wird
	 */
	public void setBisectorsGraphicsContext(
			GraphicsContext gc)
	{

		_gc_bisectors = gc;

	} // setBisectorsGraphicsContext


	/**
	 * Setzt das Aussehen der Voronoi-Knoten.
	 * 
	 * @param gc
	 *            das <code>GraphicsContext</code>-Objekt, das zum Zeichnen der
	 *            Voronoi-Knoten verwendet wird
	 */
	public void setVerticesGraphicsContext(
			GraphicsContext gc)
	{

		_gc_vertices = gc;

	} // setVerticesGraphicsContext


	/**
	 * Liefert eine Aufzaehlung aller Bisektoren dieses Voronoi-Diagramms.
	 * 
	 * @return eine Aufzaehlung der Bisektoren
	 */
	public Enumeration bisectors()
	{

		return _bisectors.elements();

	} // bisectors


	/**
	 * Liefert eine Aufzaehlung aller Voronoi-Knoten dieses Voronoi-Diagramms.
	 * 
	 * @return eine Aufzaehlung der Voronoi-Knoten
	 */
	public Enumeration vertices()
	{

		return _vertices.elements();

	} // bisectors


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void addPoint(
			float input_x,
			float input_y)
	{

		super.addPoint(input_x, input_y);
		_p2b.put(lastPoint(), new Vector());
		_sorted = false;
		_recompute = true;

	} // addPoint


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void addPoint(
			double input_x,
			double input_y)
	{

		super.addPoint(input_x, input_y);
		_p2b.put(lastPoint(), new Vector());
		_sorted = false;
		_recompute = true;

	} // addPoint


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void addPoint(
			Point2 input_point)
	{

		super.addPoint(input_point);
		_p2b.put(input_point, new Vector());
		_sorted = false;
		_recompute = true;

	} // addPoint


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void appendCopy(
			Point2List input_points)
	{

		super.appendCopy(input_points);
		Enumeration e = input_points.points().values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_sorted = false;
		_recompute = true;

	} // appendCopy


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void appendCopy(
			SimpleList input_points)
	{

		super.appendCopy(input_points);
		Enumeration e = input_points.values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_sorted = false;
		_recompute = true;

	} // appendCopy


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void clear()
	{

		super.clear();
		_p2b.clear();
		_sorted = false;
		_recompute = true;

	} // clear


	/**
	 * Erzeugt eine komplette Kopie des Voronoi-Diagramms.
	 * 
	 * @return eine Kopie des Voronoi-Diagramms
	 */
	public Object clone()
	{

		return _clone();

	} // clone()


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void concat(
			Point2List input_points)
	{

		super.concat(input_points);
		Enumeration e = input_points.points().values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_sorted = false;
		_recompute = true;

	} // concat


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void concat(
			SimpleList input_points)
	{

		super.concat(input_points);
		Enumeration e = input_points.values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_sorted = false;
		_recompute = true;

	} // concat


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public GraphicsContext getPointContext()
	{

		return getPointsGraphicsContext();

	} // getPointContext


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void insert(
			Point2 input_first,
			Point2 input_second,
			Point2 input_insert)
	{

		super.insert(input_first, input_second, input_insert);
		_p2b.put(input_insert, new Vector());
		_sorted = false;
		_recompute = true;

	} // insert


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void insertFront(
			float input_x,
			float input_y)
	{

		super.insertFront(input_x, input_y);
		_p2b.put(firstPoint(), new Vector());
		_sorted = false;
		_recompute = true;

	} // insertFront


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void insertFront(
			Point2 input_point)
	{

		super.insertFront(input_point);
		_p2b.put(input_point, new Vector());
		_sorted = false;
		_recompute = true;

	} // insertFront


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void removeFirstPoint()
	{

		_p2b.remove(firstPoint());
		super.removeFirstPoint();
		_recompute = true;

	} // removeFirstPoint


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void removeLastPoint()
	{

		_p2b.remove(lastPoint());
		super.removeLastPoint();
		_recompute = true;

	} // removeLastPoint


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void setPointContext(
			GraphicsContext context)
	{

		setPointsGraphicsContext(context);

	} // setPointContext


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void transform(
			Matrix33 a)
	{

		super.transform(a);
		_p2b.clear();
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_sorted = false;
		_recompute = true;

	} // transform


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void translate(
			float input_horizontal,
			float input_vertical)
	{

		super.translate(input_horizontal, input_vertical);
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_recompute = true;

	} // translate


	/*
	* [javadoc-Beschreibung wird aus Point2ListAlgorithm kopiert]
	*/
	public void translate(
			Point2 input_point)
	{

		super.translate(input_point);
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			_p2b.put(e.nextElement(), new Vector());
		} // while
		_recompute = true;

	} // translate


	//**************************************************************************
	// Protected methods
	//**************************************************************************

	/**
	 * Entfernung der alten Voronoi-Daten aus den Datenstrukturen.
	 */
	protected void _initDataStructures()
	{

		_vertices.clear();
		_bisectors.clear();
		Iterator i = _p2b.values().iterator();
		while (i.hasNext())
		{
			((Vector) i.next()).clear();
		} // while

	} // _initDataStructures


	/**
	 * Berechnet das Voronoi-Diagramm von zwei Punkten.
	 */
	protected void _simpleTwoPointDiagram()
	{

		// Erzeugung des einzigen Bisektors als Gerade:
		Point2 p1 = firstPoint();
		Point2 p2 = lastPoint();
		if (p1.y == p2.y)
		{
			_all_points_have_same_y = true;
		} // if
		_all_bisectors_are_parallel = true;
		VoronoiBisector2 bisec = new VoronoiBisector2(p1, p2);
		// fuege Bisektor hinzu und verknuepfe mit den Punkten:
		_bisectors.add(bisec);
		((Vector) _p2b.get(p1)).add(bisec);
		((Vector) _p2b.get(p2)).add(bisec);

	} // _simpleTwoPointVoronoi


	/**
	 * Berechnet das Voronoi-Diagramm einer Punktmenge, die nur aus Punkten mit
	 * der selben x-Koordinate besteht. Das Resultat sind also horizontale,
	 * parallele Bisektoren.
	 */
	protected void _simpleMultiplePointDiagram()
	{

		// Bestimmung der Bisektoren als Geraden:
		Enumeration e = _points.values();
		Point2 last = (Point2) e.nextElement();
		Point2 curr;
		VoronoiBisector2 bisec;
		float y;
		while (e.hasMoreElements())
		{
			curr = (Point2) e.nextElement();
			bisec = new VoronoiBisector2(last, curr);
			// fuege Bisektor hinzu und verknuepfe mit den Punkten:
			_bisectors.add(bisec);
			((Vector) _p2b.get(last)).add(bisec);
			((Vector) _p2b.get(curr)).add(bisec);
			last = curr;
		} // while

	} // _simpleTwoPointVoronoi


	/**
	 * Vereinigt zwei linear separierte Voronoi-Diagramme zum Gesamtdiagramm.
	 * 
	 * @param vor1
	 *            Teildiagramm 1
	 * @param vor2
	 *            Teildiagramm 2
	 */
	protected void _mergeDiagrams(
			VoronoiDiagram2 vor1,
			VoronoiDiagram2 vor2)
	{

		/* Voraussetzungen:
		   vor1 und vor2 sind bzgl. der x-Koordinate linear separiert.
		   Die Punktmengen von vor1 und vor2 sind primaer nach der x-Koordinate
		   aufsteigend geordnet, sekundaer aufsteigend nach y-Koordinate, wenn zwei
		   aufeinanderfolgende Punkte die gleiche x-Koordinate haben.
		   Die Punktmengen von vor1 und vor2 bestehen entweder aus
		   - einem einzigen Punkt,
		   - zwei Punkten, deren x- oder y-Koordinaten gleich sein koennen,
		   - oder drei oder mehr Punkten (hier ist das Flag fuer gleiche
		     x-Koordinaten gesetzt, wenn alle Punkte die gleiche x-Koordinate
		     haben). */

		// vereinige die Voronoi-Knoten:
		this._vertices.addAll(vor1._vertices);
		this._vertices.addAll(vor2._vertices);
		// vereinige vorerst alle Bisektoren:
		this._bisectors.addAll(vor1._bisectors);
		this._bisectors.addAll(vor2._bisectors);
		this._p2b.putAll(vor1._p2b);
		this._p2b.putAll(vor2._p2b);

		/* Abfangen der Spezialfaelle:
		  (0. points_n(Vor1)=1 und points_n(Vor2)=1 ist unmoeglich.)
		   1. Wenn ein Vor mit points_n(Vor)=1 beteiligt ist und das andere Vor
		      nur parallele Bisektoren hat, muss geprueft werden, ob die Punkte
		      des Gesamtdiagramms wiederum alle auf einer Geraden liegen.
		   2. Wenn beide Vor's nur parallele Bisektoren haben, muss geprueft
		      werden, ob alle Bisektoren die gleiche Steigung haben, denn dann
		      resultiert ein Gesamtdiagramm nur aus parallelen Geraden.
		  (3. Falls sonst auf den zu berechnenden unterstuetzenden Segmenten
		      weitere Punkte liegen, muessen die Segmente angepasst werden.)
		  (4. Beim spaeteren Suchen des obersten Schnittpunkts mit dem aktuellen
		      Bisektor muss der Fall beruecksichtigt werden, dass von beiden
		      Seiten je ein Schnitt an dem exakt gleichen Punkt auftritt. Dann
		      muessen beide schneidenden Bisektoren ersetzt werden, und der neue
		      Bisektor liegt zwischen zwei neuen naechsten Punkten.) */

		boolean simple_case = false;
		// Spezialfall 1:
		if (((vor1.length() == 1) || (vor2.length() == 1))
				&& (vor1._all_bisectors_are_parallel || vor2._all_bisectors_are_parallel))
		{
			simple_case = _checkAndMergeSpecialCase(vor1, vor2);
		} // if
		// Spezialfall 2:
		else if (vor1._all_bisectors_are_parallel
				&& vor2._all_bisectors_are_parallel)
		{
			simple_case = _checkAndMergeSpecialCase(vor1, vor2);
		} // if
		if (simple_case)
			return;

		/* Falls keiner der Faelle 1 und 2 zutrifft, erfolgt das Mergen der
		   Teildiagramme mittels der "dividing chain" im folgenden Verfahren. */

		_all_bisectors_are_parallel = false;
		_all_points_have_same_y = false;

		/* Bestimmung der beiden unterstuetzenden Segmente, hierbei wird der
		   Spezialfall 3 beruecksichtigt: */
		float b_x = vor1.maximumX() + (vor2.minimumX() - vor1.maximumX())
				/ 2.0f;
		Segment2 support[] = _computeSupportingSegments(vor1, vor2, b_x);
		/* support[0] oben, support[1] unten */

		// Initialisierung mit oberem unterstuetzenden Segment:
		boolean first_bisector = true;
		VoronoiVertex2 vertex = null;
		VoronoiVertex2 new_vertex[] = new VoronoiVertex2[2];
		Point2 bi_p[] = new Point2[2];
		Point2 bi_n[] = new Point2[2];
		bi_p[0] = support[0].source();
		bi_p[1] = support[0].target();
		BasicLine2 bisec = support[0].orthogonal(support[0].center());
		if (bisec.source().y < bisec.target().y)
		{
			// Korrektur des Bisektors:
			Point2 sp = bisec.source();
			bisec.setSource(bisec.target());
			bisec.setTarget(sp);
		} // if
		VoronoiBisector2 next_bisec[] = new VoronoiBisector2[2];
		boolean both;
		int upper;
		Intersection dummy = new Intersection();

		/* Schleife zum Suchen des naechsten Voronoi-Knotens, hierbei wird der
		   Spezialfall 4 beruecksichtigt: */
		while (true)
		{
			new_vertex[0] = null;
			new_vertex[1] = null;
			bi_n[0] = null;
			bi_n[1] = null;
			next_bisec[0] = null;
			next_bisec[1] = null;
			VoronoiBisector2 bi[] = new VoronoiBisector2[2];
			both = false;

			// suche obersten Schnittpunkt mit Bisektoren (links, dann rechts):
			for (int i = 0; i < 2; i++)
			{
				Enumeration e = ((Vector) _p2b.get(bi_p[i])).elements();
				while (e.hasMoreElements())
				{
					VoronoiBisector2 next = (VoronoiBisector2) e.nextElement();
					Point2 s = bisec.intersection((BasicLine2) next
							.getRepresentation(), dummy);
					if ((s != null)
							&& (!s.equals(vertex))
							&& ((new_vertex[i] == null) || (s.y > new_vertex[i].y)))
					{
						bi[i] = next;
						new_vertex[i] = new VoronoiVertex2(s);
						// ermittle den naechsten Punkt:
						bi_n[i] = next.getPoints()[0];
						if (bi_n[i] == bi_p[i])
						{
							bi_n[i] = next.getPoints()[1];
						} // if
						next_bisec[i] = next;
					} // if
				} // while
			} // for

			// feststellen, welcher Bisektor (zuerst) schneidet:
			if ((new_vertex[0] != null) && (new_vertex[1] != null))
			{
				// Spezialfall 4 - beide Bisektoren schneiden:
				if (new_vertex[0].equals(new_vertex[1]))
				{
					both = true;
					upper = 1;
				}
				else
				{ // if
					// Schnittpunkt mit einem linken Bisektor kommt zuerst:
					if (new_vertex[0].y > new_vertex[1].y)
					{
						upper = 0;
						// Schnittpunkt mit einem rechten Bisektor kommt zuerst:
					}
					else
					{ // if
						upper = 1;
					} // else
				} // else
			}
			else
			{ // if
				// Schnittpunkt mit einem linken Bisektor:
				if (new_vertex[0] != null)
				{
					upper = 0;
					// Schnittpunkt mit einem rechten Bisektor:
				}
				else
				{ // if
					upper = 1;
				} // else
			} // else

			/*
			if ( ( new_vertex[0] == null ) && ( new_vertex[1] == null ) ) {
			System.out.println( " !! no vertex found !!" );
			} // if
			*/

			// Sicherheitshalber:
			if ((new_vertex[0] == null) && (new_vertex[1] == null))
				break;

			// Hinzufuegen des neuen Voronoi-Knotens:
			_vertices.add(new_vertex[upper]);
			// Hinzufuegen des neuen Bisektors:
			VoronoiBisector2 new_bisec = null;
			if (first_bisector)
			{
				// neuer Bisektor ist ein Strahl:
				new_bisec = new VoronoiBisector2(new_vertex[upper],
						Point2.ORIENTATION_LEFT, bi_p[0], bi_p[1]);
			}
			else
			{ // if
				// neuer Bisektor ist ein Segment:
				new_bisec = new VoronoiBisector2(vertex, new_vertex[upper],
						bi_p[0], bi_p[1]);
				vertex.addBisector(new_bisec);
			} // else
			_bisectors.add(new_bisec);
			((Vector) _p2b.get(bi_p[0])).add(new_bisec);
			((Vector) _p2b.get(bi_p[1])).add(new_bisec);
			new_vertex[upper].addBisector(new_bisec);

			// Ersetzen des (der) schneidenden Bisektors (Bisektoren):
			int k = upper;
			if (both)
				k = 0;
			for (; k <= upper; k++)
			{
				_bisectors.remove(next_bisec[k]);
				((Vector) _p2b.get(bi_p[k])).remove(next_bisec[k]);
				((Vector) _p2b.get(bi_n[k])).remove(next_bisec[k]);

				/* ersetzenden Bisektor berechnen: */

				// alter Bisektor ist eine Gerade:
				if (next_bisec[k].isLine())
				{
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						new_bisec = new VoronoiBisector2(new_vertex[upper],
								Point2.ORIENTATION_RIGHT, bi_p[k], bi_n[k]);
					}
					else
					{ // if
						new_bisec = new VoronoiBisector2(new_vertex[upper],
								Point2.ORIENTATION_LEFT, bi_p[k], bi_n[k]);
					} // else
				} // if

				// alter Bisektor ist ein Strahl:
				else if (next_bisec[k].isRay())
				{
					VoronoiVertex2 old_vertex = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().source();
					old_vertex.removeBisector(next_bisec[k]);
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						// Beruecksichtigung des Voronoi-Knotens:
						if (bisec.orientation(old_vertex) == Point2.ORIENTATION_RIGHT)
						{
							// ersetzender Bisektor ist ein Segment:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex, bi_p[k], bi_n[k]);
							old_vertex.addBisector(new_bisec);
							old_vertex = null;
							// Knoten wird mitsamt anliegender Bisektoren verworfen:
						}
						else
						{
							// ersetzender Bisektor ist ein Strahl:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									Point2.ORIENTATION_RIGHT, bi_p[k], bi_n[k]);
						} // else
					}
					else
					{ // if
						// Beruecksichtigung des Voronoi-Knotens:
						if (bisec.orientation(old_vertex) == Point2.ORIENTATION_LEFT)
						{
							// ersetzender Bisektor ist ein Segment:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex, bi_p[k], bi_n[k]);
							old_vertex.addBisector(new_bisec);
							old_vertex = null;
							// Knoten wird mitsamt anliegender Bisektoren verworfen:
						}
						else
						{
							// ersetzender Bisektor ist ein Strahl:
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									Point2.ORIENTATION_LEFT, bi_p[k], bi_n[k]);
						} // else
					} // else
					// Entfernung weiterer anliegender Bisektoren:
					if (old_vertex != null)
					{
						_recursiveRemove(old_vertex, bi_p[k]);
					} // if
				} // if

				// alter Bisektor ist ein Segment:
				else
				{
					VoronoiVertex2 old_vertex1 = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().source();
					VoronoiVertex2 old_vertex2 = (VoronoiVertex2) next_bisec[k]
							.getRepresentation().target();
					old_vertex1.removeBisector(next_bisec[k]);
					old_vertex2.removeBisector(next_bisec[k]);
					VoronoiVertex2 old_vertex = null;
					// Unterscheidung zwischen der Ursprungsseite:
					if (k == 0)
					{
						// ersetzender Bisektor ist ein Segment:
						if (bisec.orientation(old_vertex1) == Point2.ORIENTATION_RIGHT)
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex1, bi_p[k], bi_n[k]);
							old_vertex1.addBisector(new_bisec);
							old_vertex = old_vertex2;
						}
						else
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex2, bi_p[k], bi_n[k]);
							old_vertex2.addBisector(new_bisec);
							old_vertex = old_vertex1;
						} // else
					}
					else
					{ // if
						// ersetzender Bisektor ist ein Segment:
						if (bisec.orientation(old_vertex1) == Point2.ORIENTATION_LEFT)
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex1, bi_p[k], bi_n[k]);
							old_vertex1.addBisector(new_bisec);
							old_vertex = old_vertex2;
						}
						else
						{
							new_bisec = new VoronoiBisector2(new_vertex[upper],
									old_vertex2, bi_p[k], bi_n[k]);
							old_vertex2.addBisector(new_bisec);
							old_vertex = old_vertex1;
						} // else
					} // else
					// Entfernung weiterer anliegender Bisektoren:
					_recursiveRemove(old_vertex, bi_p[k]);
				} // if

				// neuen Bisektor hinzufuegen:
				_bisectors.add(new_bisec);
				((Vector) _p2b.get(bi_p[k])).add(new_bisec);
				((Vector) _p2b.get(bi_n[k])).add(new_bisec);
				new_vertex[upper].addBisector(new_bisec);
			} // for

			// Vorbereiten des naechsten Durchlaufs:
			first_bisector = false;
			vertex = new_vertex[upper];
			bi_p[upper] = bi_n[upper];
			if (both)
			{
				bi_p[0] = bi_n[0];
			} // if
			// Test, ob unteres unterstuetzendes Segment erreicht:
			if ((bi_p[0] == support[1].source())
					&& (bi_p[1] == support[1].target()))
				break;
			// sonst naechsten Bisektorstrahl erzeugen:
			Point2 c = new Point2(bi_p[0].x + (bi_p[1].x - bi_p[0].x) / 2,
					bi_p[0].y + (bi_p[1].y - bi_p[0].y) / 2);
			Point2 l1 = new Point2(c.x - (bi_p[0].y - c.y), c.y
					- (c.x - bi_p[0].x));
			Point2 l2 = new Point2(c.x + (bi_p[0].y - c.y), c.y
					+ (c.x - bi_p[0].x));
			if (l2.y < l1.y)
			{
				l1 = l2;
			} // if
			l1.translate(vertex.x - c.x, vertex.y - c.y);
			bisec = new Ray2(vertex, l1);
		} // while

		// Bisektor des unteren unterstuetzenden Segments hinzufuegen:
		VoronoiBisector2 last_bisec = new VoronoiBisector2(vertex,
				Point2.ORIENTATION_RIGHT, bi_p[0], bi_p[1]);
		_bisectors.add(last_bisec);
		((Vector) _p2b.get(bi_p[0])).add(last_bisec);
		((Vector) _p2b.get(bi_p[1])).add(last_bisec);
		vertex.addBisector(last_bisec);

		// alle Voronoi-Knoten ohne anliegende Bisektoren loeschen:
		for (int j = _vertices.size() - 1; j >= 0; j--)
		{
			if (((VoronoiVertex2) _vertices.get(j)).bisectorCount() == 0)
			{
				_vertices.remove(j);
			} // if
		} // for

	} // _mergeDiagrams


	/**
	 * Fasst zwei Voronoi-Teildiagramme auf einfache Art und Weise zusammen,
	 * wenn ein Spezialfall vorliegt. Das Testresultat auf einen Spezialfall
	 * wird zurueckgeliefert und sagt gleichzeitig aus, ob die Teildiagramme mit
	 * Erfolg zusammengefasst wurden.
	 * 
	 * @param vor1
	 *            das linke Voronoi-Teildiagramm
	 * @param vor2
	 *            das rechte Voronoi-Teildiagramm
	 *            
	 * @return <code>true, wenn ein Spezialfall vorliegt
	 */
	protected boolean _checkAndMergeSpecialCase(
			VoronoiDiagram2 vor1,
			VoronoiDiagram2 vor2)
	{

		/* Voraussetzungen:
		   Fall 1: Ein Vor besteht nur aus einem Punkt, das andere hat nur
		           parallele Bisektoren.
		   Fall 2: Beide Vor's bestehen nur aus parallelen Bisektoren.
		*/

		// Fall 1:
		VoronoiDiagram2 single = null;
		VoronoiDiagram2 other = null;
		if (vor1._points.length() == 1)
		{
			single = vor1;
			other = vor2;
		}
		else if (vor2._points.length() == 1)
		{ // if
			single = vor2;
			other = vor1;
		} // else
		if (single != null)
		{
			// Test auf Kollinearitaet:
			Line2 line = new Line2(other.firstPoint(), other.lastPoint());
			Point2 point = single.firstPoint();
			if (line.isCollinear(point))
			{
				Point2 neighbor = null;
				if (other == vor1)
				{
					neighbor = other.lastPoint();
				}
				else
				{ // if
					neighbor = other.firstPoint();
				} // else
				// neuen Bisektor erzeugen:
				VoronoiBisector2 bisec = new VoronoiBisector2(point, neighbor);
				_bisectors.add(bisec);
				((Vector) _p2b.get(point)).add(bisec);
				((Vector) _p2b.get(neighbor)).add(bisec);
				_all_points_have_same_y = other._all_points_have_same_y;
				_all_bisectors_are_parallel = true;
				return true;
			} // if
		} // if

		// Fall 2:
		else
		{
			// Test auf gleiche Steigung der Bisektoren:
			Line2 left = (Line2) ((VoronoiBisector2) vor1._bisectors
					.firstElement()).getRepresentation();
			Line2 right = (Line2) ((VoronoiBisector2) vor2._bisectors
					.firstElement()).getRepresentation();
			if (left.slope() == right.slope())
			{
				// Test auf Kollinearitaet:
				Line2 line = new Line2(vor1.firstPoint(), vor1.lastPoint());
				if (line.isCollinear(vor2.firstPoint()))
				{
					// neuen Bisektor erzeugen:
					Point2 p_l = vor1.lastPoint();
					Point2 p_r = vor2.firstPoint();
					VoronoiBisector2 bisec = new VoronoiBisector2(p_l, p_r);
					_bisectors.add(bisec);
					((Vector) _p2b.get(p_l)).add(bisec);
					((Vector) _p2b.get(p_r)).add(bisec);
					_all_points_have_same_y = vor1._all_points_have_same_y;
					_all_bisectors_are_parallel = true;
					return true;
				} // if
			} // if
		} // else

		return false; // keine vereinfachte Zusammenfassung moeglich!

	} // _checkAndMergeSpecialCase


	/**
	 * Berechnet die beiden unterstuetzenden Segmente der beiden
	 * Voronoi-Teildiagramme. Zurueckgeliefert wird ein Array mit zwei
	 * Segmenten; an Index 0 steht das obere Segment; die Startpunkte der
	 * Segmente liegen im linken Teildiagramm, die Endpunkte im rechten.
	 * 
	 * @param vor1
	 *            das linke Teildiagramm
	 * @param vor2
	 *            das rechte Teildiagramm
	 * @param border
	 *            x-Koordinate der Grenze zwischen den Teildiagrammen
	 *            
	 * @return ein Array mit den beiden unterstuetzenden Segmenten
	 */
	protected Segment2[] _computeSupportingSegments(
			Point2List vor1,
			Point2List vor2,
			float border)
	{

		// es gibt zwei unterstuetzende Segmente:
		Segment2 support[] = new Segment2[2];
		// die konvexe Huelle des Gesamtdiagramms bestimmen:
		ConvexPolygon2 ch = new ConvexPolygon2(this);
		// Huellensegmente durchlaufen und unterstuetzende Segmente finden:
		Segment2 edges[] = ch.edges();
		int i = 0;
		while ((support[0] == null) || (support[1] == null))
		{
			Segment2 edge = edges[i++];
			float x_s = edge.source().x;
			float x_t = edge.target().x;
			// wenn x-Koordinaten der Segmentpunkte die Grenze ueberschreiten...:
			if (((x_s < border) && (x_t > border))
					|| ((x_s > border) && (x_t < border)))
			{
				// ...wurde ein unterstuetzendes Segment gefunden:
				if (support[0] == null)
				{
					support[0] = edge;
				}
				else
				{ // if
					support[1] = edge;
				} // else
			} // if
		} // while

		// Punktreihenfolge der Segmente von links nach rechts:
		if (support[0].source().x > support[0].target().x)
		{
			Point2 backup = support[0].source();
			support[0].setSource(support[0].target());
			support[0].setTarget(backup);
		} // if
		if (support[1].source().x > support[1].target().x)
		{
			Point2 backup = support[1].source();
			support[1].setSource(support[1].target());
			support[1].setTarget(backup);
		} // if

		// ggf. Anpassung der unterstuetzenden Segmente (Spezialfall 3):
		float start_x0 = support[0].source().x;
		float min_y0 = support[0].source().y;
		float max_y0 = support[0].target().y;
		if (max_y0 < min_y0)
		{
			float dummy = min_y0;
			min_y0 = max_y0;
			max_y0 = dummy;
		} // if
		float start_x1 = support[1].source().x;
		float min_y1 = support[1].source().y;
		float max_y1 = support[1].target().y;
		if (max_y1 < min_y1)
		{
			float dummy = min_y1;
			min_y1 = max_y1;
			max_y1 = dummy;
		} // if
		Point2 p;
		// Suche nach weiter rechts liegenden Punkten auf den Segmenten:
		Enumeration e = vor1._points.values();
		while (e.hasMoreElements())
		{
			p = (Point2) e.nextElement();
			// Suche fuer oberes Segment:
			if ((p.x > start_x0) && (p.y > min_y0) && (p.y < max_y0)
					&& (support[0].isCollinear(p)))
			{
				// Suchbereich kann verkleinert werden:
				start_x0 = p.x;
				if (support[0].slope() > 1.0)
				{
					min_y0 = p.y;
				}
				else
				{ // if
					max_y0 = p.y;
				} // else
				// ersetze Segment-Startpunkt:
				support[0].setSource(p);
			}
			else // if
			// Suche fuer unteres Segment:
			if ((p.x > start_x1) && (p.y > min_y1) && (p.y < max_y1)
					&& (support[1].isCollinear(p)))
			{
				// Suchbereich kann verkleinert werden:
				start_x1 = p.x;
				if (support[1].slope() > 1.0)
				{
					min_y1 = p.y;
				}
				else
				{ // if
					max_y1 = p.y;
				} // else
				// ersetze Segment-Startpunkt:
				support[1].setSource(p);
			} // if
		} // while
		start_x0 = support[0].target().x;
		start_x1 = support[1].target().x;
		// Suche nach weiter links liegenden Punkten auf dem Segment:
		e = vor2._points.values();
		boolean cont1 = true;
		boolean cont2 = true;
		while ((cont1 || cont2) && e.hasMoreElements())
		{
			p = (Point2) e.nextElement();
			// Suche fuer oberes Segment:
			if (cont1 && (p.x < start_x0) && (p.y > min_y0) && (p.y < max_y0)
					&& (support[0].isCollinear(p)))
			{
				// ersetze Segment-Endpunkt:
				support[0].setTarget(p);
				cont1 = false; // Suche beendet!
			}
			else // if
			// Suche fuer unteres Segment:
			if (cont2 && (p.x < start_x1) && (p.y > min_y1) && (p.y < max_y1)
					&& (support[1].isCollinear(p)))
			{
				// ersetze Segment-Endpunkt:
				support[1].setTarget(p);
				cont2 = false; // Suche beendet!
			} // if
		} // while

		// ggf. Reihenfolge der Segmente korrigieren:
		p = support[1].source();
		if (p == support[0].source())
		{
			p = support[1].target();
		} // if
		if (support[0].orientation(p) == Point2.ORIENTATION_LEFT)
		{
			Segment2 backup = support[0];
			support[0] = support[1];
			support[1] = backup;
		} // if

		// Punkte in den Segmenten durch Originalpunkte ersetzen:
		support[0].setSource(closestPoint(support[0].source()));
		support[0].setTarget(closestPoint(support[0].target()));
		support[1].setSource(closestPoint(support[1].source()));
		support[1].setTarget(closestPoint(support[1].target()));
		return support; // fertig

	} // _computeSupportingSegments


	/**
	 * Liefert eine Kopie dieses Voronoi-Diagramms. Saemtliche Daten sind Kopien
	 * - nicht etwa Referenzen - der Originaldaten.
	 * 
	 * @return eine Kopie dieses Voronoi-Diagramms
	 */
	protected Object _clone()
	{

		VoronoiDiagram2 copy = new VoronoiDiagram2();
		// kopiere Punktmenge:
		Enumeration e = _points.values();
		while (e.hasMoreElements())
		{
			copy.addPoint((Point2) ((Point2) e.nextElement()).clone());
		} // while

		// kopiere private-Variablen:
		copy._draw_points = this._draw_points;
		copy._draw_bisectors = this._draw_bisectors;
		copy._gc_points = this._gc_points;
		copy._gc_bisectors = this._gc_bisectors;

		return copy;

	} // _clone


	/**
	 * Initialisierungen der Konstruktoren.
	 */
	protected void _init()
	{

		_draw_points = true;
		_draw_bisectors = true;
		_draw_vertices = true;
		_sorted = false;
		_recompute = true;

		_vertices = new Vector();
		_bisectors = new Vector();
		_p2b = new HashMap();

		_all_points_have_same_y = false;
		_all_bisectors_are_parallel = false;

		_gc_points = new GraphicsContext();
		_gc_points.setLineWidth(WIDTH_POINT);
		_gc_points.setForegroundColor(COLOR_POINT);
		_gc_points.setEndCap(CAP_POINT);

		_gc_bisectors = new GraphicsContext();
		_gc_bisectors.setLineWidth(WIDTH_BISECTOR);
		_gc_bisectors.setForegroundColor(COLOR_BISECTOR);
		_gc_bisectors.setEndCap(CAP_BISECTOR);

		_gc_vertices = new GraphicsContext();
		_gc_vertices.setLineWidth(WIDTH_VERTEX);
		_gc_vertices.setForegroundColor(COLOR_VERTEX);
		_gc_vertices.setEndCap(CAP_VERTEX);

	} // _initGraphicsContexts


	/**
	 * Rekursives Entfernen aller Bisektoren, die einen bestimmten Punkt
	 * enthalten.
	 * 
	 * @param vertex
	 *            der Startknoten der Rekursion
	 * @param point
	 *            der enthaltene Punkt
	 */
	protected void _recursiveRemove(
			VoronoiVertex2 vertex,
			Point2 point)
	{

		// Sammeln der rekursiven Aufrufe:
		Vector recursives = new Vector();
		// Bereinigung des aktuellen Knotens:
		Iterator i = vertex.bisectors();
		while (i.hasNext())
		{
			// naechsten Bisektor ermitteln:
			VoronoiBisector2 bisec = (VoronoiBisector2) i.next();
			// Bisektor im positiven Fall entfernen:
			if (bisec.contains(point))
			{
				i.remove();
				_bisectors.remove(bisec);
				((Vector) _p2b.get(bisec.getPoints()[0])).remove(bisec);
				((Vector) _p2b.get(bisec.getPoints()[1])).remove(bisec);
				// rekursive Aufrufe sammeln:
				BasicLine2 line = bisec.getRepresentation();
				if (!bisec.isLine())
				{
					recursives.add(line.source());
				} // if
				if (bisec.isSegment())
				{
					recursives.add(line.target());
				} // if
			} // if
		} // while
		// rekursive Aufrufe durchfuehren:
		Enumeration e = recursives.elements();
		while (e.hasMoreElements())
		{
			_recursiveRemove((VoronoiVertex2) e.nextElement(), point);
		} // while

	} // _recursiveRemove

} // VoronoiDiagram2
