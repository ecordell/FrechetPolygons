package anja.geom;


import java.util.*;


/**
 * Class for assembling all parameters representing the Diameter Function of a
 * Polygon in a certain interval. ( see Class DiameterFunction )
 * 
 */
class DFPiece
		implements java.io.Serializable
{

	public int		no;

	public Point2	param1;
	public Point2	param2;

	public double	lowerAngle;
	public double	upperAngle;


	/**
	 * Setter for all parameters of the class
	 * 
	 * @param index
	 *            int
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @param low
	 *            double
	 * @param up
	 *            double
	 */
	public void setParams(
			int index,
			Point2 p1,
			Point2 p2,
			double low,
			double up)
	{

		no = index;
		param1 = p1;
		param2 = p2;
		lowerAngle = low;
		upperAngle = up;

	}

}

/**
 * Class for Calculations concerning the Diameter Function of any given
 * Polygon2.
 * 
 * The Diameter Function of a Polygon is defined piecewise as :
 * 
 * d(alpha) = d1(alpha) * sin( phi( alpha ) ), where :
 * 
 * alpha =: The angle in which the Diameter should be calculated. d1 =:
 * sqrt(pow(x(alpha)-u(alpha),2)+pow(y(alpha)-v(alpha),2)) phi(alpha) =: arccos(
 * ( cos(alpha)*(x(alpha)-u(alpha)) + sin(alpha)*(y(alpha)-v(alpha)) )/d1 )
 * 
 * With (x(alpha), y(alpha)) and (u(alpha), v(alpha)) are 2 points of the
 * polygon. The corresponding Points (x,y) and (u,v) for each interval in which
 * the above function is defined by the same parameters ( namely the points )
 * are determined by a sweep algorithm over alpha e [0,2*PI] (see Class
 * DFPiece).
 * 
 * @author Birgit Engels
 * @version 1.0
 */
public class DiameterFunction
		implements java.io.Serializable
{

	/**********************/
	/* Private Varriables */
	/**********************/

	// Polygon, dessen Diameter-Function berechnet werden soll 
	private Polygon2	_polygon;

	// Enthalten maximalen/minimalen Polygondurchmesser :
	double				_maxDia;
	double				_minDia;

	// Enhalten die Winkel in denen maximaler/minimaler Polygondurchmesser
	// angenommen wird :
	double				_minAngle;
	double				_maxAngle;

	// Speicherung der Winkelgrenzen fuer die stueckweise definierten 
	// Funtkion auf dem Intervall [ 0, 2*PI ]
	private double[]	_angles;

	// Anzahl der ermittelten Intervalle
	private int			_intno		= 0;

	// Zu obigen Intervallgrenzen gehoerende Punkte, deren Koordinaten
	// pro Intervall die Parameter der Funktion stellen
	private Vector		_point1		= new Vector();
	private Vector		_point2		= new Vector();

	// Interne Hilfsvariablen 
	private double[]	_distances;
	private double		_currAng	= 0;

	private int			_segno1		= 0;
	private int			_segno2		= 0;

	private int			_lastChoice	= 0;
	private double		_restAng	= 0;


	//****************/
	//* Constructors */
	//****************/

	/**
	 * Constructor. Prepares the polygon, that is handed over, for further
	 * computations
	 * 
	 * @param poly
	 *            The polygon
	 */
	public DiameterFunction(
			Polygon2 poly)
	{

		_polygon = new ConvexPolygon2(poly);
		_polygon.setOrientation(Polygon2.ORIENTATION_LEFT);
		_angles = new double[2 * (int) (Math.pow(_polygon.edges().length, 2))];

	}


	//*******************/
	//* Public  Methods */
	//*******************/

	/**
	 * Attention : This function has to be called at least once before the
	 * values of the Diameter Function for the Polygon2 _polygon are ready.
	 * 
	 */
	public void calcDiameter()
	{
		if (_angles != null && _angles.length > 0)
		{
			_calcIntervals();
			_calcDistancesAndDenominators();
			_calcMinDiameter();
			_calcMaxDiameter();
		}
	}


	/**
	 * Returns the Diameter of the Polygon2 _polygon in angle degrees.
	 * 
	 * @param angle
	 *            The angle (in degrees)
	 * 
	 * @return The diameter
	 */
	public double calcDiaInDegAngle(
			double angle)
	{

		double radangle = angle * Math.PI / 180;
		return calcDiaInAngle(radangle);
	}


	/**
	 * Returns the Diameter of the Polygon2 _polygon in angle radiants.
	 * 
	 * @param angle
	 *            The angle
	 * 
	 * @return The diameter
	 */
	public double calcDiaInAngle(
			double angle)
	{
		double diameter = 0.0;
		if (_angles.length > 0)
		{
			int interval = _determinInterval(angle, 0, _intno);
			Point2 first, second;
			first = (Point2) _point1.get(interval);
			second = (Point2) _point2.get(interval);
			double d, phi;
			d = _distances[interval];
			phi = Math
					.acos(((first.x - second.x) * Math.cos(angle) + (first.y - second.y)
							* Math.sin(angle))
							/ d);
			diameter = d * Math.sin(phi);
		}

		return diameter;

	}


	/**
	 * Returns the value of the Polygon2 _polygon's Diameter Function in angle
	 * radiants as defined within the interval, wether angle lies in this
	 * interval or not!
	 * 
	 * @param angle
	 *            The angle radiant
	 * @param interval
	 *            The interval
	 * 
	 * @return The diameter
	 */
	public double calcDiaInAngle(
			double angle,
			int interval)
	{

		Point2 first, second;
		first = (Point2) _point1.get(interval);
		second = (Point2) _point2.get(interval);
		double diameter, d, phi;
		d = _distances[interval];
		phi = Math
				.acos(((first.x - second.x) * Math.cos(angle) + (first.y - second.y)
						* Math.sin(angle))
						/ d);
		diameter = d * Math.sin(phi);
		return diameter;

	}


	/**
	 * Returns the value of the maximum Diameter. Attention : The Function
	 * calcDiameter() has to be called at least once before this value is valid
	 * !
	 * 
	 * @return The maxiimum diameter
	 */
	public double getMaxDiameter()
	{

		return _maxDia;
	}


	/**
	 * Returns the angle corresponding to the value of the maximum Diameter in
	 * radiants . Attention : The Function calcDiameter() has to be called at
	 * least once before this value is valid!
	 * 
	 * @return The maximum angle
	 */
	public double getMaxDiaAngle()
	{

		return _maxAngle;
	}


	/**
	 * Returns the value of the minimum Diameter. Attention : The Function
	 * calcDiameter() has to be called at least once before this value is valid
	 * !
	 * 
	 * @return The minimum diameter
	 */
	public double getMinDiameter()
	{

		return _minDia;
	}


	/**
	 * Returns the angle corresponding to the value of the minimum Diameter in
	 * radiants. Attention : The Function calcDiameter() has to be called at
	 * least once before this value is valid!
	 * 
	 * @return The minimum angle
	 */
	public double getMinDiaAngle()
	{

		return _minAngle;
	}


	/**
	 * Returns the lower interval bound of the interval index.
	 * 
	 * @param index
	 *            The interval to check for lower bound
	 * 
	 * @return The lower bound
	 */
	public double getLowerIntervalBound(
			int index)
	{

		return _angles[index];
	}


	/**
	 * Returns the upper interval bound of the interval index.
	 * 
	 * @param index
	 *            The interval to check for upper bound
	 * 
	 * @return The upper bound
	 * 
	 */
	public double getUpperIntervalBound(
			int index)
	{

		return _angles[index + 1];
	}


	/**
	 * Returns the first Point whose coordinates are responsible for the
	 * Diameter Function of the Polygon2 _polygon
	 * 
	 * Returns the number of calulated intervals.
	 * 
	 * @return The number of calculated intervals
	 */
	public int getNoIntervals()
	{

		return _intno;
	}


	/**
	 * Returns an DFPiece containing all relevant parameters for the Diameter
	 * Function of the Polygon2 _polygon defined an the interval index.
	 * 
	 * @param index
	 *            The interval
	 * 
	 * @return The DFPiece object and all relevant parameters
	 * 
	 */
	public DFPiece getAllParamsForOneInt(
			int index)
	{

		DFPiece dfp = new DFPiece();
		dfp.setParams(index, (Point2) _point1.get(index),
				(Point2) _point2.get(index), _angles[index - 1], _angles[index]);
		return dfp;
	}


	//*******************/
	//* Private Methods */
	//*******************/

	/**
	 * Calculate max angle
	 * 
	 * @return The maximum angle
	 */
	private double _calcMaxDiaAngle()
	{

		double maxAng = 0;
		double maxDia = 0;
		double angle;
		double dia;
		Point2 p1, p2;

		for (int i = 0; i < _intno; i++)
		{
			p1 = (Point2) _point1.get(i);
			p2 = (Point2) _point2.get(i);

			angle = Math.atan((p1.x - p2.x) / (p1.y - p2.y));
			if (angle < 0)
				angle = 2 * Math.PI + angle;
			if ((angle > _angles[i]) && (angle < _angles[i + 1]))
			{
				dia = calcDiaInAngle(angle);
				if (dia > maxDia)
				{
					maxDia = dia;
					maxAng = angle;
				}

			}
			angle = Math.atan(-(p1.x - p2.x) / (p1.y - p2.y));
			if (angle < 0)
				angle = 2 * Math.PI + angle;
			if ((angle > _angles[i]) && (angle < _angles[i + 1]))
			{
				dia = calcDiaInAngle(angle);
				if (dia > maxDia)
				{
					maxDia = dia;
					maxAng = angle;
				}
			}

			dia = calcDiaInAngle(_angles[i], i);
			if (dia > maxDia)
			{
				maxDia = dia;
				maxAng = _angles[i];
			}

		}
		_maxAngle = maxAng;
		return maxAng;

	}//calcMaxDiaAngle();


	/**
	 * Calculate maximum diameter
	 * 
	 * @return The maximum diameter
	 */
	private double _calcMaxDiameter()
	{

		_maxDia = calcDiaInAngle(_calcMaxDiaAngle());
		return _maxDia;

	}//calcMaxDiameter


	/**
	 * Calculate the minimum diameter angle
	 * 
	 * @return The minimum angle
	 */
	private double _calcMinDiaAngle()
	{

		double minAng = 0;
		double minDia = _calcMaxDiameter();
		double angle;
		double dia;
		Point2 p1, p2;

		for (int i = 0; i < _intno; i++)
		{
			p1 = (Point2) _point1.get(i);
			p2 = (Point2) _point2.get(i);

			angle = Math.atan((p1.x - p2.x) / (p1.y - p2.y));
			if (angle < 0)
				angle = 2 * Math.PI + angle;
			if ((angle > _angles[i]) && (angle < _angles[i + 1]))
			{
				dia = calcDiaInAngle(angle);
				if (dia < minDia)
				{
					minDia = dia;
					minAng = angle;
				}

			}
			angle = Math.atan(-(p1.x - p2.x) / (p1.y - p2.y));
			if (angle < 0)
				angle = 2 * Math.PI + angle;
			if ((angle > _angles[i]) && (angle < _angles[i + 1]))
			{
				dia = calcDiaInAngle(angle);
				if (dia < minDia)
				{
					minDia = dia;
					minAng = angle;
				}
			}

			dia = calcDiaInAngle(_angles[i], i);
			if (dia < minDia)
			{
				minDia = dia;
				minAng = _angles[i];
			}

		}
		_minAngle = minAng;
		return minAng;

	}//calcMaxDiaAngle();


	/**
	 * Calculate the minimum Diameter of the Polygon2 _polygon; sets the _minDia
	 * to it and returns it. Attention : use getMinDiameter() to read out
	 * _minDia, because this function calculates _minDia new for any call.
	 * 
	 * @return The minimum diameter
	 */
	private double _calcMinDiameter()
	{

		_minDia = calcDiaInAngle(_calcMinDiaAngle());
		return _minDia;
	}


	/**
	 * Binary Search to determine the interval I with angle e I
	 * 
	 * @param angle
	 *            The angle to search for
	 * @param min
	 *            The left border
	 * @param max
	 *            The right border
	 * 
	 * @return The result of the search (as index)
	 */
	private int _determinInterval(
			double angle,
			double min,
			double max)
	{

		// Binary search yet to be implemented ... ;)
		int i = 0;
		while ((angle > _angles[i]) && (i < _intno))
		{
			i++;
		}
		if (i > 0)
			i--;
		return i;

	}


	/**
	 * Calculates the intervals where the piecewise defined Diameter Function of
	 * Polygon2 _polygon remains the same depending on the angle. Use
	 * getLowerIntervalBound() and getUpperIntervalBound() to get the interval
	 * bounds of a certain interval !
	 */
	private void _calcIntervals()
	{

		// Initialer Winkel
		_angles[0] = 0;

		// Initiale verantwortliche Punkte mit minimaler / maximaler y-Koordinate
		float minY = _polygon.minimumY();
		float maxY = _polygon.maximumY();

		Point2List points = (Point2List) _polygon.clone();
		Segment2[] segs = _polygon.edges();
		Point2 currFirst = null;
		Point2 currSec = null;
		for (int i = 0; i < segs.length; i++)
		{
			if (segs[i].source().y == minY)
			{
				// Falls mehrere Punkte mit gleicher y-Koord. existieren,
				// wird sicher gestellt, dass das "richtige" Segment gefunden wird...
				if ((currFirst == null) || (segs[i].source().x < currFirst.x))
				{
					currFirst = segs[i].source();
					_point1.clear();
					_point1.add(currFirst);
					_segno1 = i;
				}
			}
			if (segs[i].source().y == maxY)
			{
				// Falls mehrere Punkte mit gleicher y-Koord. existieren,
				// wird sicher gestellt, dass das "richtige" Segment gefunden wird...
				if ((currSec == null) || (segs[i].source().x > currSec.x))
				{
					currSec = segs[i].source();
					_point2.clear();
					_point2.add(currSec);
					_segno2 = i;
				}
			}
			points.removeFirstPoint();
		}

		// Berechne initiale Winkel
		double alpha1 = segs[_segno1].source().angle(segs[_segno1].target());
		double alpha2 = segs[_segno2].source().angle(segs[_segno2].target())
				- Math.PI;

		Segment2 act1, act2, pre1, pre2;
		double alphaInQ1, alphaInQ2;
		int index;

		// Berechne alle Intervalle zwischen 0 und 2*Pi
		while (_currAng < 2 * Math.PI)
		{
			// Berechne das naechste Intervall
			_calcInterval(alpha1, alpha2);

			// Berechne die aktuellen Winkel
			act1 = segs[_segno1];
			index = _segno1 - 1;
			if (index < 0)
				index = segs.length - 1;
			pre1 = segs[index];

			if ((_lastChoice == 1) || (_lastChoice == 0))
			{
				alphaInQ1 = act1.source().angle(pre1.source(), act1.target());
				alphaInQ2 = act1.source().angle(act1.target(), pre1.source());
				if (alphaInQ1 < alphaInQ2)
					alpha1 = Math.PI - alphaInQ1;
				else
					alpha1 = Math.PI - alphaInQ2;
			}
			else
				alpha1 = _restAng;

			act2 = segs[_segno2];
			index = (_segno2 - 1) % segs.length;
			if (index < 0)
				index = segs.length - 1;
			pre2 = segs[index];

			if ((_lastChoice == 2) || (_lastChoice == 0))
			{
				alphaInQ1 = act2.source().angle(pre2.source(), act2.target());
				alphaInQ2 = act2.source().angle(act2.target(), pre2.source());
				if (alphaInQ1 < alphaInQ2)
					alpha2 = Math.PI - alphaInQ1;
				else
					alpha2 = Math.PI - alphaInQ2;
			}
			else
				alpha2 = _restAng;
		}

	}//_calcIntervals


	/**
	 * Calculates the responsible points for the next interval, saves them at
	 * the respektive position in the _point1 and _point2 Vector and updates
	 * counters and current variables
	 * 
	 * @param alpha1
	 *            First value
	 * @param alpha2
	 *            Second value
	 */
	private void _calcInterval(
			double alpha1,
			double alpha2)
	{

		// Das naechste Intervall wird bestimmt...
		_intno++;

		// Die verantwortlichen Segmente zu den verantwortlichen Punkten
		Segment2[] segs = _polygon.edges();
		Segment2 resp1 = segs[_segno1];
		Segment2 resp2 = segs[_segno2];

		if (alpha2 - alpha1 > 0.0001)
		{
			_point1.add(resp1.target());
			_point2.add(resp2.source());
			_currAng += alpha1;
			_restAng = alpha2 - alpha1;
			_angles[_intno] = _currAng;
			_segno1 = (_segno1 + 1) % segs.length;
			_lastChoice = 1;

		}
		else if (alpha1 - alpha2 > 0.0001)
		{
			_point1.add(resp1.source());
			_point2.add(resp2.target());
			_currAng += alpha2;
			_restAng = alpha1 - alpha2;
			_angles[_intno] = _currAng;
			_segno2 = (_segno2 + 1) % segs.length;
			_lastChoice = 2;
		}
		else
		{
			_point1.add(resp1.target());
			_point2.add(resp2.target());
			_currAng += (alpha1 + alpha2) / 2;
			_restAng = 0;
			_angles[_intno] = _currAng;
			_segno1 = (_segno1 + 1) % segs.length;
			_segno2 = (_segno2 + 1) % segs.length;
			_lastChoice = 0;
		}

	}//_calcInterval


	/**
	 * Calculates and the distance between every two responsible points and the
	 * denominators for each interval and saves them into the distances-array
	 * _distances respectively the denominators-array _denominators at the
	 * position respektive to the interval.
	 */
	private void _calcDistancesAndDenominators()
	{

		_distances = new double[_intno + 1];
		Point2 first, second;
		for (int i = 0; i < _intno; i++)
		{
			first = (Point2) _point1.get(i);
			second = (Point2) _point2.get(i);
			_distances[i] = Math.sqrt(Math.pow((first.x - second.x), 2)
					+ Math.pow((first.y - second.y), 2));
		}
	}//_calcDistancesandDenominators

}//DiameterFunction
