package anja.geom;


/**
 * Berechnet die L1-Route in rechtwinkeligen Polygonen on-line
 * 
 * @version 0.1 20.05.1997
 * @author Elmar Langetepe
 * @see java.lang.Object
 */

public class RectScout
		extends Polygon2
{

	// ************************************************************************
	// Inner Classes
	// ************************************************************************

	
	/**
	 * Zweidimensionaler Eckpunkt mit Richtung aus der er angelaufen wurde
	 * 
	 * @version 0.1 12.05.1997
	 * @author Elmar Langetepe
	 */
	class ExtendedEdge2
			extends Point2
	{

		// ************************************************************************
		// Constants
		// ************************************************************************

		// ************************************************************************
		// Variables
		// ************************************************************************

		/** Richtung aus der der Punkt angelaufen wurde */
		public int	reachedFrom;


		// ************************************************************************
		// Constructors
		// ************************************************************************

		/**
		 * Ruft Konstruktor von Point2 auf, legt Richtung fest.
		 * 
		 * @param input_x
		 *            X-value of the point
		 * @param input_y
		 *            Y-value of the point
		 * @param dir
		 *            The direction
		 */
		public ExtendedEdge2(
				float input_x,
				float input_y,
				int dir)
		{
			super(input_x, input_y);
			this.reachedFrom = dir;
		}

		// ************************************************************************
		// Class methods
		// ************************************************************************

		// ************************************************************************
		// Public methods
		// ************************************************************************

	} // class ExtendedEdge2


	// ************************************************************************
	// Constants
	// ************************************************************************

	/* Norden  */
	public static final int	_north				= 1;

	/* Westen  */
	public static final int	_west				= 2;

	/* Sueden  */
	public static final int	_south				= 3;

	/* Osten */
	public static final int	_east				= 4;

	/* vor  */
	private final int		_for				= 1;

	/* zurueck */
	private final int		_back				= 2;

	/* clockwise order */
	private final int		_clockwise			= 1;

	/* counterclockwise order */
	private final int		_counterclockwise	= 2;

	// ************************************************************************
	// Variables
	// ************************************************************************

	/** Der bisherige kuerzeste L1-Pfad */
	public Polygon2			shortestPath;

	/** Der bessere bisherige kuerzeste L1-Pfad */
	public Polygon2			bettershortestPath;

	/** Die bisherigen angelaufenen Punkte */
	public Point2List		forBackEdges;

	/** Aktueller Standpunkt */
	public Point2			actStartPoint;

	/** Punkt zur ersten Extension vorwaerts */
	public Point2			forwardArtEdge;

	/** Punkt zur ersten Extension rueckwaerts */
	public Point2			backwardArtEdge;

	/** der etwas bessere naechste Punkt */
	public Point2			betterPoint;

	/** der aktuelle ExtensionsKnoten */
	public ExtendedEdge2	actExtEdge;

	/** relativer Richtung */
	public int				actDirection;

	/** der naechste oder zu bearbeitende Quadrant */
	public int				startQuadrant;

	/** der naechste oder zu bearbeitende Quadrant */
	public int				absolutStartQ;

	/** Aktuelles Sichtbarkeitspolygon */
	public Polygon2			actVispoly;

	/** Aktuelles Sichtbarkeitspolygon */
	public Polygon2			actPolygon;

	/** Die Schrittweite beim Ueberlaufen der Extension */
	public float			walkAhead;

	/** Die Blickrichtung nach dem Anlaufen einer Kante */
	public int				visDirection;

	/* Aktuelles Sichtbarkeitspolygon als PointsAccess */
	private PointsAccess	_vispolyacc;

	/* Aktueller Startpunkt einer Kuenstlichen Kante */
	private Point2			actNextArtEdge;

	/* Abfrage, ob nochmal zurueck gesucht werden muss */
	private boolean			_dontSearchBack;

	/* zurueck oder vor ist klar*/
	private int				_case;

	/* der vorherige bessere Punkt */
	private Point2			_oldBetterPoint;


	// ************************************************************************
	// Constructors
	// ************************************************************************
	/**
	 * Ruft Konstruktor von Polygon2 initialisiert:
	 * 
	 * @param polygon
	 *            das zugehoerige Polygon
	 * @param startPoint
	 *            der Startpunkt
	 * @param startQuad
	 *            der Startquadrant
	 */
	public RectScout(
			Polygon2 polygon,
			Point2 startPoint,
			int startQuad)
	{
		super(polygon);

		actVispoly = new Polygon2();

		actPolygon = polygon;
		shortestPath = new Polygon2();
		shortestPath.addPoint(startPoint);
		bettershortestPath = new Polygon2();
		bettershortestPath.addPoint(startPoint);
		forBackEdges = new Point2List();
		backwardArtEdge = new Point2();
		forwardArtEdge = new Point2();
		actStartPoint = startPoint;
		betterPoint = startPoint;

		startQuadrant = _checkstartQuad(startQuad, polygon, startPoint);
		absolutStartQ = startQuadrant;
		visDirection = startQuadrant - 1;
		if (visDirection == 0)
			visDirection = 4;
		walkAhead = 1;
	}


	// ************************************************************************
	// Class methods
	// ************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Berechnet die Shortest L1-Route fuer rechtwinkelige Polygone
	 * 
	 * @param startPoint
	 *            der Startpunkt
	 */
	public Point2List calculateShortestPath(
			Point2 startPoint)
	{
		Segment2 actSegment;
		Point2 _nextPoint;

		actVispoly = this.vis(actStartPoint);
		if (actVispoly == null)
		{
			//System.out.println(" Leider ist das Vispolygon gleich null fuer den Punkt " + actStartPoint + " !");
			return null;
		}

		_nextPoint = nextPoint(startPoint);

		//System.out.println("     extEdge2:" + actExtEdge);
		//System.out.println("     naechsterPoint:" + _nextPoint);
		//System.out.println("     bessererPoint2:" + betterPoint);

		while (!_nextPoint.equals(startPoint) && !isInsideAct())
		{
			shortestPath.addPoint(_nextPoint);
			bettershortestPath.addPoint(betterPoint);
			forBackEdges.addPoint(actExtEdge);
			//System.out.println(startQuadrant);

			actStartPoint = _nextPoint;

			actVispoly = this.vis(actStartPoint);// das Sichtbarkeitspolygon 
			if (actVispoly == null)
			{
				//System.out.println(" Leider ist das Vispolygon gleich null fuer den Punkt " + actStartPoint + " !");
				return null;
			}
			//System.out.println(actVispoly);

			//System.out.println("Vispoly:" + actVispoly);
			//System.out.println("    aktueller Point2:" + actStartPoint);
			//System.out.println("     extEdge2:" + actExtEdge + " " + actExtEdge.reachedFrom);       

			_nextPoint = nextPoint(startPoint);
			//System.out.println("    naechsterPoint2:" + _nextPoint);
			//System.out.println("    bessererPoint2:" + _betterPoint);

		}// while
		Point2 lastEdge = insertLastEdge(_nextPoint, startPoint);
		shortestPath.addPoint(lastEdge);
		bettershortestPath.addPoint(betterPoint);

		return shortestPath;
	} // calculateShortestPath


	// ************************************************************************ 
	/**
	 * Berechnet den naechsten Punkt, der vom aktuellen Punkt aus angelaufen
	 * werden soll.
	 * 
	 * @param ende
	 *            der Endpunkt falls das Polygon so einfach ist und man sofort
	 *            alles sieht
	 * 
	 * @return der Punkt, der angelaufen werden soll
	 */
	public Point2 nextPoint(
			Point2 ende)
	{
		Point2 interPoint;
		Segment2 segment;
		Ray2 extRay;
		int NextQuadrant, quadrant;
		Intersection inter = new Intersection();
		boolean notRound = true;

		_oldBetterPoint = betterPoint;

		_dontSearchBack = false;
		_vispolyacc = new PointsAccess(actVispoly);

		NextQuadrant = startQuadrant;
		quadrant = startQuadrant;
		while (!_findNextArtEdge(NextQuadrant, _counterclockwise) && notRound)
		{
			NextQuadrant = _updateQuadrant(NextQuadrant);

			if (NextQuadrant == quadrant)
				notRound = false;
			//System.out.println("Hier 1");	
		}//while
		forwardArtEdge = actNextArtEdge;
		//System.out.println("    Quadrant:" + NextQuadrant );
		//System.out.println("forwardEdge:" + forwardArtEdge );
		//System.out.println("dont Search " + _dontSearchBack);

		if (notRound)
		{
			if (!_dontSearchBack)
			{
				if (_findNextArtEdge(NextQuadrant, _clockwise))
					backwardArtEdge = actNextArtEdge;
				else
					backwardArtEdge = null;
			}

			//System.out.println("backwardEdge:" + backwardArtEdge + " " + _dontSearchBack);
			//System.out.println("actPoint: " + actStartPoint );
			//System.out.println("betterPoint: " + betterPoint );
			return _updateDirectioncomputePoint(backwardArtEdge,
					forwardArtEdge, NextQuadrant);

		}
		else
			return ende;

	}// nextPoint


	// ************************************************************************ 
	/**
	 * Stellt fest, ob die Ecke bereits in dieser Richtung angelaufen wurde,
	 * wird gebraucht, wenn man den Algorithmus selbst in die Hand nimmt und
	 * beenden muss.
	 * 
	 * @return false falls einer gefunden
	 */
	public boolean isInsideAct()
	{
		PointsAccess AllEdgesacc;
		ExtendedEdge2 ExtPoint;
		int firstReached;

		AllEdgesacc = new PointsAccess(forBackEdges);
		ExtPoint = (ExtendedEdge2) (AllEdgesacc.nextPoint());
		if (ExtPoint == null)
			return false;
		firstReached = ExtPoint.reachedFrom;

		while (ExtPoint != null)
		{
			if (actExtEdge.x == ExtPoint.x && actExtEdge.y == ExtPoint.y
					&& actExtEdge.reachedFrom == ExtPoint.reachedFrom)
			{
				//System.out.println(" Ende bei:" +  ExtPoint + "Richtung:" + ExtPoint.reachedFrom);
				return true;
			}
			if (ExtPoint.reachedFrom != firstReached)
				return false;
			ExtPoint = (ExtendedEdge2) (AllEdgesacc.nextPoint());
		}// while
		return false;

	}//isInsideAct


	// ************************************************************************ 
	/**
	 * Berechnet den letzten Punkt auf dem L1-Pfad
	 * 
	 * @param nextP
	 *            der naechste Punkt, der angesteuert wird
	 * @param startPoint
	 *            der Startpunkt
	 * 
	 * @return den letzten Punkt des Pfades
	 */
	public Point2 insertLastEdge(
			Point2 nextP,
			Point2 startPoint)
	{
		Point2 interPoint;

		if (nextP.equals(startPoint))
		{
			Line2 startLine = new Line2(startPoint.x, startPoint.y,
					startPoint.x + 10, startPoint.y);
			interPoint = startLine.plumb(actStartPoint);
			betterPoint = interPoint;
			if (actDirection == _south)
				actDirection = _north;
			else if (actDirection == _north)
				actDirection = _south;
			else if (actDirection == _east)
				actDirection = _west;
			else
				actDirection = _east;
		}
		else
		{

			if ((actExtEdge.reachedFrom == _west && absolutStartQ == 3 && startPoint.x > actStartPoint.x)
					|| (actExtEdge.reachedFrom == _east && absolutStartQ == 1 && startPoint.x < actStartPoint.x))
			{
				Line2 startLine = new Line2(startPoint.x, startPoint.y,
						startPoint.x + 10, startPoint.y);
				interPoint = startLine.plumb(actStartPoint);//System.out.println("Hier 1");
				betterPoint = new Point2(_oldBetterPoint.x, interPoint.y);
				if (interPoint.y >= actStartPoint.y)
					actDirection = _north;
				else
					actDirection = _south;

			}
			else if ((actExtEdge.reachedFrom == _south && absolutStartQ == 4 && startPoint.y > actStartPoint.y)
					|| (actExtEdge.reachedFrom == _north && absolutStartQ == 2 && startPoint.y < actStartPoint.y))
			{
				Line2 startLine = new Line2(startPoint.x, startPoint.y,
						startPoint.x, startPoint.y + 10);
				interPoint = startLine.plumb(actStartPoint);//System.out.println("Hier 2");
				betterPoint = new Point2(interPoint.x, _oldBetterPoint.y);
				if (interPoint.x >= actStartPoint.x)
					actDirection = _east;
				else
					actDirection = _west;

			}
			else if (actExtEdge.reachedFrom == _south
					|| actExtEdge.reachedFrom == _north)
			{
				Line2 startLine = new Line2(startPoint.x, startPoint.y,
						startPoint.x + 10, startPoint.y);
				interPoint = startLine.plumb(actStartPoint);//System.out.println("Hier 3");
				betterPoint = new Point2(_oldBetterPoint.x, interPoint.y);
				if (interPoint.y >= actStartPoint.y)
					actDirection = _north;
				else
					actDirection = _south;
			}
			else
			{
				Line2 startLine = new Line2(startPoint.x, startPoint.y,
						startPoint.x, startPoint.y + 10);
				interPoint = startLine.plumb(actStartPoint);//System.out.println("Hier 4");
				betterPoint = new Point2(interPoint.x, _oldBetterPoint.y);
				if (interPoint.x >= actStartPoint.x)
					actDirection = _east;
				else
					actDirection = _west;
			}
		}

		return interPoint;

	}// insertLastEdge


	// ************************************************************************
	// Private methods
	// ************************************************************************

	/**
	 * Berechnet den Startquadrant, wichtig, wenn der Startpunkt auf einer Kante
	 * liegt Quadranten gemaess Orientierung
	 * 
	 * @param startQuad
	 *            der Quadrant
	 * @param poly
	 *            das Polygon
	 * @param startPoint
	 *            der Startpunkt
	 * 
	 * @return Der Startquadranten
	 */
	private int _checkstartQuad(
			int startQuad,
			Polygon2 poly,
			Point2 startPoint)
	{
		if (!(poly.locatePoint(startPoint) == Polygon2.POINT_ON_EDGE))
		{
			return startQuad;
		}
		else
		{
			PointsAccess polacc = new PointsAccess(poly);
			Point2 first = _getOrientedNext(polacc, _counterclockwise);
			Point2 next = _getOrientedNext(polacc, _counterclockwise);
			Point2 prev = first;
			Segment2 actSeg = new Segment2(prev, next);
			while (!next.equals(first))
			{
				if (actSeg.isHorizontal()
						&& startPoint.y == prev.y
						&& actSeg.inspectCollinearPoint(startPoint) == Point2.LIES_ON)
				{
					if (prev.x < next.x)
						return 1;
					else
						return 3;
				}
				else if (actSeg.isVertical()
						&& startPoint.x == prev.x
						&& actSeg.inspectCollinearPoint(startPoint) == Point2.LIES_ON)
				{
					if (prev.y < next.y)
						return 2;
					else
						return 4;
				}
				prev = next;
				next = _getOrientedNext(polacc, _counterclockwise);
				actSeg = new Segment2(prev, next);
			}
			if (actSeg.isHorizontal()
					&& startPoint.y == prev.y
					&& actSeg.inspectCollinearPoint(startPoint) == Point2.LIES_ON)
			{
				if (prev.x < next.x)
					return 1;
				else
					return 3;
			}
			else if (actSeg.isVertical()
					&& startPoint.x == prev.x
					&& actSeg.inspectCollinearPoint(startPoint) == Point2.LIES_ON)
			{
				if (prev.y < next.y)
					return 2;
				else
					return 4;
			}
		}
		return startQuad;
	}//_checkstartQuad


	/**
	 * Berechnet die naechste kuenstliche Kante innerhalb des Quadranten gemaess
	 * Orientierung
	 * 
	 * @param quadrant
	 *            der Quadrant
	 * @param orientation
	 *            die Orientierung
	 * 
	 * @return true falls existent, sonst false
	 */
	private boolean _findNextArtEdge(
			int quadrant,
			int orientation)
	{
		Point2 actPoint, prevActPoint, oldActPoint;
		Line2 actLine;
		boolean notFound = true;

		actNextArtEdge = null;
		_case = 0;
		prevActPoint = _searchStartPoint(quadrant, orientation);
		actPoint = _getOrientedNext(_vispolyacc, orientation);
		//System.out.println("dont Search11" +_dontSearchBack + quadrant);       
		//System.out.println(" StartprevActPoint:" +  prevActPoint );     
		//System.out.println(" StartactPoint:" +  actPoint );
		while (_isInsideQuadrant(actPoint, quadrant) && notFound)
		{
			//System.out.println("Hier 3");
			actLine = new Line2(prevActPoint, actPoint);

			if (actLine.liesOn(actStartPoint))
			{
				//System.out.println(" prevActPoint:" +  prevActPoint );     
				//System.out.println(" actPoint:" +  actPoint );
				Segment2 actSegment = new Segment2(prevActPoint, actStartPoint);
				if ((actLine.isVertical() || actLine.isHorizontal()))
				{
					if (!actStartPoint.equals(prevActPoint)
							&& actSegment.inspectCollinearPoint(actPoint) == Point2.LIES_ON)
					{
						oldActPoint = actPoint;
						actPoint = _getOrientedNext(_vispolyacc, orientation);
						while (actLine.liesOn(actPoint)
								&& actSegment.inspectCollinearPoint(actPoint) == Point2.LIES_ON)
						{
							oldActPoint = actPoint;
							actPoint = _getOrientedNext(_vispolyacc,
									orientation);
						}//while
						notFound = _checkOriginal(oldActPoint, prevActPoint,
								_reverse(orientation));
						if (!notFound)
						{
							backwardArtEdge = actNextArtEdge;
							actNextArtEdge = prevActPoint;
							_dontSearchBack = true;
							_case = _back;
						}
					}
					else
					{
						notFound = _checkOriginal2(prevActPoint, actPoint,
								orientation);
						if (!notFound)
						{
							backwardArtEdge = actPoint;
							// actNextArtEdge implizit gesetzt
							_dontSearchBack = true;
							//System.out.println("dont Search" +_dontSearchBack);
							_case = _for;
						}
					}
				}
				else if (actSegment.inspectCollinearPoint(actPoint) == Point2.LIES_ON)
				{
					if (orientation == _counterclockwise)
					{
						backwardArtEdge = actPoint;
						actNextArtEdge = prevActPoint;
						_dontSearchBack = true;
						_case = _back;
						notFound = false;
					}
					else
					{
						actNextArtEdge = prevActPoint;
						_case = _for;
						notFound = false;
					}
				}
				else
				{
					actNextArtEdge = prevActPoint;
					notFound = false;
				}
			}

			prevActPoint = actPoint;
			actPoint = _getOrientedNext(_vispolyacc, orientation);
			//System.out.println(" prevActPoint:" +  prevActPoint );     
			//System.out.println(" actPoint:" +  actPoint );
		}// while

		if (notFound)
			return false;
		else
			return true;
	}// _findNextArtEdge


	// ************************************************************************  
	/**
	 * Berechnet den Startknoten vorm Eintritt in den Quadranten gemaess
	 * Orientierung
	 * 
	 * @param quadrant
	 *            der Quadrant
	 * @param orientation
	 *            die Orientierung
	 * 
	 * @return true falls existent, sonst false
	 */
	private Point2 _searchStartPoint(
			int quadrant,
			int orientation)
	{
		float relativeAngle;
		Point2 point, prevpoint;
		Point2 helpPoint;
		Line2 helpLine;
		int backorientation;

		backorientation = _reverse(orientation);

		prevpoint = new Point2();
		//System.out.println("vispolyacc:" +  _vispolyacc  );

		point = _getOrientedNext(_vispolyacc, orientation);

		if (_isInsideQuadrant(point, quadrant))
		{
			while (_isInsideQuadrant(point, quadrant))
			{
				//System.out.println("Hier 4");
				point = _getOrientedNext(_vispolyacc, backorientation);
			}//while

			point = _getOrientedNext(_vispolyacc, orientation); //nochmal einen zurueck!
			return point;
		}
		else
		{
			while (!_isInsideQuadrant(point, quadrant))
			{
				//System.out.println("Hier 5"+ " " + quadrant + " " + orientation + " " + actExtEdge
				//		     + " " + point + " " + actStartPoint);

				prevpoint = point;
				point = _getOrientedNext(_vispolyacc, orientation);
			}//while

			return point;
		}
	}// _searchStartPoint


	// ************************************************************************
	/**
	 * Stellt fest ob der Punkt im Quadranten liegt, ohne den zweiten Arm
	 * 
	 * @param point
	 *            Der Punkt
	 * @param quadrant
	 *            Der Quadrant
	 * 
	 * @return true falls ja, sonst false
	 */
	private boolean _isInsideQuadrant(
			Point2 point,
			int quadrant)
	{
		if ((quadrant == 1 && point.x > actStartPoint.x && point.y >= actStartPoint.y)
				|| point.equals(actStartPoint))
			return true;
		else if ((quadrant == 2 && point.x <= actStartPoint.x && point.y > actStartPoint.y)
				|| point.equals(actStartPoint))
			return true;
		else if ((quadrant == 3 && point.x < actStartPoint.x && point.y <= actStartPoint.y)
				|| point.equals(actStartPoint))
			return true;
		else if ((quadrant == 4 && point.x >= actStartPoint.x && point.y < actStartPoint.y)
				|| point.equals(actStartPoint))
			return true;
		else
			return false;

	}// _isInsideQuadrant	


	// ************************************************************************
	/**
	 * Gibt einen Nachfolger nach Orientierung aus
	 * 
	 * @param vispolyacc
	 *            Die Punktliste
	 * @param orientation
	 *            Die Orientierung
	 * 
	 * @return Der Nachfolger gemaess Orientierung
	 */
	private Point2 _getOrientedNext(
			PointsAccess vispolyacc,
			int orientation)
	{
		if (orientation == _clockwise)
			return vispolyacc.cyclicPrevPoint();
		else
			return vispolyacc.cyclicNextPoint();
	}// _getOrientedNext


	// ************************************************************************ 
	/**
	 * Berechnet den Punkt auf den zugelaufen werden soll, datiert die Richtung
	 * und den naechsten Quadranten auf
	 * 
	 * @param backEdge
	 *            der backwardEdge
	 * @param forEdge
	 *            der forwardWdge
	 * 
	 * @return der Quadrant
	 */
	private Point2 _updateDirectioncomputePoint(
			Point2 backEdge,
			Point2 forEdge,
			int quadrant)
	{
		Segment2 actforSeg = new Segment2(actStartPoint, forEdge);
		Point2 returnPoint;

		Ray2 lotRay;

		if (quadrant == 1)
		{
			if (actforSeg.isHorizontal() && _case == _for)
			{
				actDirection = _east;
				startQuadrant = 4;
				visDirection = _south;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x,
						forEdge.y + 10);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _east);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x + walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else if (actforSeg.isHorizontal() && _case == _back)
			{
				actDirection = _east;
				startQuadrant = 1;
				visDirection = _north;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x,
						backEdge.y - 10);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _east);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x + walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
			}
			else if (forEdge.y < backEdge.y)
			{
				actDirection = _north;
				startQuadrant = 1;
				visDirection = _east;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x - 10,
						forEdge.y);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _north);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						+ walkAhead);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else
			{
				actDirection = _east;
				startQuadrant = 1;
				visDirection = _north;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x,
						backEdge.y - 10);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _east);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x + walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = backEdge;
			}
		}
		else if (quadrant == 2)
		{
			if (actforSeg.isVertical() && _case == _for)
			{
				actDirection = _north;
				visDirection = _east;
				startQuadrant = 1;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x - 10,
						forEdge.y);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _north);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						+ walkAhead);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else if (actforSeg.isVertical() && _case == _back)
			{
				actDirection = _north;
				startQuadrant = 2;
				visDirection = _west;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x + 10,
						backEdge.y);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _north);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						+ walkAhead);
				return returnPoint;
				//returnPoint = backEdge;
			}
			else if (forEdge.x > backEdge.x)
			{
				actDirection = _west;
				startQuadrant = 2;
				visDirection = _north;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x,
						forEdge.y - 10);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _west);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x - walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else
			{
				actDirection = _north;
				startQuadrant = 2;
				visDirection = _west;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x + 10,
						backEdge.y);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _north);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						+ walkAhead);
				return returnPoint;
				//returnPoint = backEdge;
			}
		}
		else if (quadrant == 3)
		{
			if (actforSeg.isHorizontal() && _case == _for)
			{
				actDirection = _west;
				startQuadrant = 2;
				visDirection = _north;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x,
						forEdge.y - 10);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _west);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x - walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else if (actforSeg.isHorizontal() && _case == _back)
			{
				actDirection = _west;
				startQuadrant = 3;
				visDirection = _south;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x,
						backEdge.y + 10);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _west);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x - walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = backEdge;
			}
			else if (forEdge.y > backEdge.y)
			{
				actDirection = _south;
				startQuadrant = 3;
				visDirection = _west;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x + 10,
						forEdge.y);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _south);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						- walkAhead);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else
			{
				actDirection = _west;
				startQuadrant = 3;
				visDirection = _south;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x,
						backEdge.y + 10);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _west);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x - walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = backEdge;
			}
		}
		else
		{
			if (actforSeg.isVertical() && _case == _for)
			{
				actDirection = _south;
				startQuadrant = 3;
				visDirection = _west;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x + 10,
						forEdge.y);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _south);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						- walkAhead);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else if (actforSeg.isVertical() && _case == _back)
			{
				actDirection = _south;
				startQuadrant = 4;
				visDirection = _east;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x - 10,
						backEdge.y);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _south);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						- walkAhead);
				return returnPoint;
				//returnPoint = backEdge;
			}
			else if (forEdge.x < backEdge.x)
			{
				actDirection = _east;
				startQuadrant = 4;
				visDirection = _south;
				lotRay = new Ray2(forEdge.x, forEdge.y, forEdge.x,
						forEdge.y + 10);
				actExtEdge = new ExtendedEdge2(forEdge.x, forEdge.y, _east);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(returnPoint.x + walkAhead,
						_oldBetterPoint.y);
				return returnPoint;
				//returnPoint = forEdge;
			}
			else
			{
				actDirection = _south;
				startQuadrant = 4;
				visDirection = _east;
				lotRay = new Ray2(backEdge.x, backEdge.y, backEdge.x - 10,
						backEdge.y);
				actExtEdge = new ExtendedEdge2(backEdge.x, backEdge.y, _south);
				returnPoint = lotRay.plumb(actStartPoint);
				betterPoint = new Point2(_oldBetterPoint.x, returnPoint.y
						- walkAhead);
				return returnPoint;
				//returnPoint = backEdge;
			}
		}

	}// _updateDirectioncomputePoint


	// ************************************************************************ 
	/**
	 * Berechnet die Umkehrung der Orientierung
	 * 
	 * @param orientation
	 *            die Orientierung
	 * 
	 * @return Die Orientierung
	 */
	private int _reverse(
			int orientation)
	{
		if (orientation == _clockwise)
			return _counterclockwise;
		else
			return _clockwise;
	}//_reverse


	// ************************************************************************ 
	/**
	 * Berechnet den naechsten Quadranten
	 * 
	 * @param oldquadrant
	 *            der vorherige Quadrant
	 * 
	 * @return Der nächste Quadrant
	 */
	public static int _updateQuadrant(
			int oldquadrant)
	{
		if (oldquadrant < 4)
			return oldquadrant + 1;
		else
			return 1;
	}//_updateQuadrant


	// ************************************************************************ 
	/**
	 * Berechnet den naechsten Quadranten
	 * 
	 * @param oldquadrant
	 *            der vorherige Quadrant
	 * @param orientation
	 *            Die Orientierung
	 * 
	 * @return Nummer des nächsten Quadranten
	 */
	private int _orientedPrevQuadrant(
			int oldquadrant,
			int orientation)
	{
		if (orientation == _clockwise)
		{
			if (oldquadrant < 4)
				return oldquadrant + 1;
			else
				return 1;
		}
		else
		{
			if (oldquadrant > 1)
				return oldquadrant - 1;
			else
				return 4;
		}
	}//_orientedPrevQuadrant


	// ************************************************************************ 
	/**
	 * Berechnet den Punkt in actNextArtEdge bei dem eine rechtwinkelige
	 * kuenstliche Kante des Ausgangspolygons startet. Sucht den naechsten Punkt
	 * der naeher an prev liegt im Originalpolygon
	 * 
	 * @param prev
	 *            Vorgaenger
	 * @param act
	 *            Nachfolger
	 * @param orient
	 *            Orientierung
	 * 
	 * @return false falls einer gefunden
	 */
	private boolean _checkOriginal2(
			Point2 prev,
			Point2 act,
			int orient)
	{

		Point2 nextOrientedPoint, oldOrientedPoint;
		PointsAccess polyacc = new PointsAccess(this);
		Line2 actLine = new Line2(prev, act);

		nextOrientedPoint = _getOrientedNext(polyacc, orient);

		while (!nextOrientedPoint.equals(prev))
		{
			//System.out.println("Hier 6"+ prev + nextOrientedPoint );
			nextOrientedPoint = _getOrientedNext(polyacc, orient);
		}// while 

		oldOrientedPoint = prev;
		nextOrientedPoint = _getOrientedNext(polyacc, orient);

		while (actLine.liesOn(nextOrientedPoint)
				&& !nextOrientedPoint.equals(act))
		{
			oldOrientedPoint = nextOrientedPoint;
			nextOrientedPoint = _getOrientedNext(polyacc, orient);
		}
		//System.out.println("prev, act,nextOrientedPoint" + prev + " " + act + " " + nextOrientedPoint);
		if (nextOrientedPoint.equals(act))
			return true;
		else
		{
			actNextArtEdge = oldOrientedPoint;
			return false;
		}

	}// _checkOriginal2


	/**
	 * Berechnet den Punkt in actNextArtEdge bei dem eine rechtwinkelige
	 * kuenstliche Kante des Ausgangspolygons startet. Sucht den naechsten Punkt
	 * der naeher an act liegt im Originalpolygon
	 * 
	 * @param prev
	 *            Vorgaenger
	 * @param act
	 *            Nachfolger
	 * @param orient
	 *            Orientierung
	 * 
	 * @return false falls einer gefunden
	 */
	private boolean _checkOriginal(
			Point2 prev,
			Point2 act,
			int orient)
	{

		Point2 nextOrientedPoint, globalResult, localResult;
		PointsAccess polyacc = new PointsAccess(this);
		boolean NoResult;
		int axis;
		Segment2 actseg = new Segment2(prev, act);
		if (actseg.isHorizontal())
			axis = 1;
		else
			axis = 2;

		nextOrientedPoint = _getOrientedNext(polyacc, orient);

		while (!nextOrientedPoint.equals(prev))
		{
			//System.out.println("Hier 6"+ prev + nextOrientedPoint );
			nextOrientedPoint = _getOrientedNext(polyacc, orient);
		}//while

		NoResult = true;
		localResult = prev;
		globalResult = prev;

		while (true)
		{
			localResult = _checkFirst(localResult, act, polyacc, orient, axis);
			if (localResult.equals(act) && NoResult)
				return true;
			else if (localResult.equals(act))
			{
				actNextArtEdge = globalResult;
				return false;
			}
			else
			{
				NoResult = false;
				globalResult = _getOrientedNext(polyacc, _reverse(orient));
				localResult = _getOrientedNext(polyacc, orient);// nur um  wieder im Tritt zu sein mit polyacc
				localResult = _checkNext(localResult, act, polyacc, orient,
						prev, actseg, axis);
				if (localResult.equals(prev) || localResult.equals(act))
				{
					actNextArtEdge = globalResult;
					return false;
				}
			}
			//System.out.println("Hier ist der Haenger "  + prev + " " + act);
		}
		//return true;		
	}


	/**
	 * Berechnet den nächsten Punkt
	 * 
	 * @param notOnLine
	 *            Point2
	 * @param act
	 *            Point2
	 * @param polyacc
	 *            PointsAccess
	 * @param orient
	 *            Orientierung
	 * @param start
	 *            Startpunkt
	 * @param segment
	 *            Segment2
	 * @param axis
	 *            int
	 * 
	 * @return Der nächste Punkt
	 */
	private Point2 _checkNext(
			Point2 notOnLine,
			Point2 act,
			PointsAccess polyacc,
			int orient,
			Point2 start,
			Segment2 segment,
			int axis)
	{
		Point2 nextOrientedPoint = notOnLine;
		if (axis == 2)
		{
			while (!(nextOrientedPoint.x == act.x && (segment
					.inspectCollinearPoint(nextOrientedPoint) == Point2.LIES_ON))
					&& !nextOrientedPoint.equals(start)
					&& !nextOrientedPoint.equals(act))
			{
				nextOrientedPoint = _getOrientedNext(polyacc, orient);
			}
		}
		else if (axis == 1)
		{
			while (!(nextOrientedPoint.y == act.y && (segment
					.inspectCollinearPoint(nextOrientedPoint) == Point2.LIES_ON))
					&& !nextOrientedPoint.equals(start)
					&& !nextOrientedPoint.equals(act))
			{
				nextOrientedPoint = _getOrientedNext(polyacc, orient);
			}
		}

		//System.out.println("Next " +  nextOrientedPoint ); 
		return nextOrientedPoint;

	}


	/**
	 * Der erste Punkt
	 * 
	 * @param liesON
	 *            Point2
	 * @param act
	 *            Point2
	 * @param polyacc
	 *            PointsAccess
	 * @param orient
	 *            Orientierung
	 * @param axis
	 *            int
	 * 
	 * @return Point2
	 */
	private Point2 _checkFirst(
			Point2 liesON,
			Point2 act,
			PointsAccess polyacc,
			int orient,
			int axis)
	{
		Point2 nextOrientedPoint = liesON;
		if (axis == 2)
		{
			while (nextOrientedPoint.x == liesON.x
					&& !(nextOrientedPoint.equals(act)))
			{
				nextOrientedPoint = _getOrientedNext(polyacc, orient);
			}
		}
		if (axis == 1)
		{
			while (nextOrientedPoint.y == liesON.y
					&& !(nextOrientedPoint.equals(act)))
			{
				nextOrientedPoint = _getOrientedNext(polyacc, orient);
			}
		}
		//System.out.println("First " +  nextOrientedPoint );     
		return nextOrientedPoint;

	}

} // RectScout
