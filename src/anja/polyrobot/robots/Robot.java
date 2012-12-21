package anja.polyrobot.robots;


import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;

import anja.polyrobot.AngleCounter;
import anja.polyrobot.OpticalSensor;
import anja.polyrobot.MathUtil;

import anja.geom.Polygon2Scene;
import anja.geom.Point2;
import anja.geom.Rectangle2;


/**
 * Die Klasse Robot ist die abstrakte Basisklasse fuer Roboter, die sich in
 * einer polygonalen Szene befinden ( @see anja.geom.Polygon2Scene ).
 * 
 * Als bewegliches Objekt hat jeder Roboter eine durch x-y-Koordinaten gegebene
 * aktuelle Position. Dabei handelt es sich bei einem nicht punktfoermigen Robot
 * um die Position eines besonders ausgezeichneten Punktes auf der Grundflaeche
 * des Roboters, z.B. um den Mittelpunkt bei einem kreisfoermigen oder um einen
 * speziell definierten Punkt bei einem polygonalen Roboter.
 * 
 * 
 * Die abstrakte Klasse Robot stellt den Konstruktor
 * 
 * Robot( Polygon2Scene scene, Point2 coors )
 * 
 * bereit. Dieser Konstruktor muss von den Konstruktoren der Subklassen als
 * erstes aufgerufen werden, um Szene und Anfangsposition des neu zu erzeugenden
 * Roboters zu setzen.
 * 
 * 
 * Neben der Position hat jeder Roboter eine Orientierung. Diese ist durch den
 * Winkel gegeben, den er mit der positiven x-Achse im mathematischen Sinne (
 * also gegen den Uhrzeigersinn ) bildet. Dabei gilt immer: 0.0 <= Orientierung
 * < 2*PI
 * 
 * 
 * @version 0.1 27.11.01
 * @author Ulrich Handel
 */

//****************************************************************
public abstract class Robot
		implements Cloneable
//****************************************************************
{

	//************************************************************
	// public constants
	//************************************************************

	// IDs aller verfuegbaren Robotermodelle
	// ( nicht-abstrakte Subklassen )
	//
	public final static int		NONE				= 0;
	public final static int		POINT				= 1;						// Klasse PointRobot
	public final static int		CONVEX				= 2;						//        ConvexRobot

	// Namen aller verfuegbaren Robotermodelle ( z.B. fuer Menues )
	//
	public final static String	NAME_POINT			= "Point";
	public final static String	NAME_CONVEX			= "Convex";

	// Richtungen fuer Polygonumlaeufe
	//
	public final static boolean	LEFT_HAND			= Polygon2Scene.LEFT_HAND;
	public final static boolean	RIGHT_HAND			= Polygon2Scene.RIGHT_HAND;

	// Richtungen fuer Drehungen
	//
	public final static boolean	CLOCKWISE			= true;
	public final static boolean	COUNTERCLOCKWISE	= false;


	//************************************************************
	// class methods
	//************************************************************

	/**
	 * Ermitteln des Namen des Robotermodells mit der gegebenen ID-Nummer.
	 * 
	 * @param id
	 *            POINT etc.
	 */

	//============================================================
	public static String getName(
			int id)
	//============================================================
	{
		switch (id)
		{
			case POINT:
				return NAME_POINT;
			case CONVEX:
				return NAME_CONVEX;

			default:
				return null;
		} // switch
	} // getName


	/**
	 * Hilfsmethode zur Darstellung von Roboter-Position und Orientierung beim
	 * Zeichnen eines Roboters.
	 * 
	 * @param g_screen
	 *            Graphics-Objekt zum Zeichnen
	 * 
	 * @param x
	 *            X-Koordinate des Roboters in Bildkoordinaten
	 * @param y
	 *            Y-Koordinate des Roboters in Bildkoordinaten
	 * 
	 * @param angle
	 *            Bogenmass-Winkel, durch den die Orientierung des Roboters
	 *            gegeben ist
	 * 
	 * @param col
	 *            Farbe fuer Richtungspfeil und aeusseren Kreis
	 * @param center_col
	 *            Fuellfarbe fuer inneren Markierungskreis
	 * 
	 *            13.08.2004 Note from Ibragim:
	 * 
	 *            Since Graphics2D is a superset of Graphics, and there's no
	 *            longer any direct way to specify coordinates directly in
	 *            screen space, g_screen is actually of no use - all coordinates
	 *            will STILL be transformed by the currently assigned affine
	 *            transform in a Graphics2D rendering context. Be careful with
	 *            coordinate values you use here! Maybe we will modify the
	 *            entire thing to use only Graphics2D one day :)
	 * 
	 */

	//============================================================
	public static void drawRobotCenter(
			Graphics g_screen,
			int x,
			int y,
			double angle,
			Color col,
			Color center_col)
	//============================================================
	{

		g_screen.setColor(col);
		g_screen.drawOval(x - 8, y - 8, 16, 16);

		//        int x_arrow = ( int )Math.round( x + Math.cos( angle ) * 25.0 );
		//        int y_arrow = ( int )Math.round( y - Math.sin( angle ) * 25.0 );

		int x_arrow = (int) Math.round(x + Math.cos(angle) * 25.0);
		int y_arrow = (int) Math.round(y + Math.sin(angle) * 25.0);

		angle += Math.PI / 2.0;

		double dx = Math.cos(angle) * 3;
		double dy = Math.sin(angle) * 3;

		int x_pos = (int) Math.round(x + dx);
		int y_pos = (int) Math.round(y - dy);
		int x_neg = (int) Math.round(x - dx);
		int y_neg = (int) Math.round(y + dy);

		g_screen.drawLine(x_pos, y_pos, x_arrow, y_arrow);
		g_screen.drawLine(x_neg, y_neg, x_arrow, y_arrow);

		g_screen.setColor(center_col);
		g_screen.fillOval(x - 3, y - 3, 7, 7);

	} // drawRobotCenter


	//************************************************************
	// private variables
	//************************************************************

	private Polygon2Scene	_scene;			// Polygonszene, in der sich der
												// Roboter befindet

	private Point2			_coors;			// aktuelle Koordinaten des Roboters
												// innerhalb der Szene

	private double			_orientation;		// Orientierung des Roboters
												// Es gilt immer
												//  0.0 <= _orientation < 2*PI

	private double			_path_length;		// Laenge des zurueckgelegten Weges

	private AngleCounter	_angle_counter;	// Winkelzaehler

	private OpticalSensor	_optical_sensor;	// optischer Sensor

	private Rectangle2		_move_rect;		// Bewegungsgrenzen fuer die Methoden
												// move() und move( double angle )


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Erzeugen eines neuen Robots mit der Orientierung 0.0
	 * 
	 * Dieser Konstruktor muss von den Konstruktoren der Subklassen als erstes
	 * aufgerufen werden, um Polygonszene und Anfangsposition des zu erzeugenden
	 * Roboters zu setzen.
	 * 
	 * Beispiel:
	 * 
	 * public SubRobot( Polygon2Scene scene, Point2 coors, ... ) { super( scene,
	 * coors );
	 * 
	 * // ggf. weitere Initialisierungen der SubRobot- // Datenelemente }
	 * 
	 * @param scene
	 *            Szene in der sich der Roboter befindet
	 * @param coors
	 *            Anfangskoordinaten des Robots
	 */

	//============================================================
	protected Robot(
			Polygon2Scene scene,
			Point2 coors)
	//============================================================
	{
		_scene = scene;
		_coors = new Point2(coors);
	} // Robot


	/**
	 * Erzeugen eines neuen Roboters als Kopie des gegebenen Roboters.
	 * 
	 * Dieser Konstruktor muss von den entsprechenden Konstruktoren der
	 * Subklassen als erstes aufgerufen werden. ( siehe auch Kommentar zur
	 * Methode clone() ).
	 */

	//============================================================
	protected Robot(
			Robot robot)
	//============================================================
	{
		_scene = robot._scene;
		_coors = new Point2(robot._coors);
		_orientation = robot._orientation;

		_path_length = robot._path_length;

		if (robot._move_rect != null)
			_move_rect = new Rectangle2(robot._move_rect);

		if (robot._angle_counter != null)
			_angle_counter = new AngleCounter(robot._angle_counter);

		if (robot._optical_sensor != null)
			_optical_sensor = new OpticalSensor(robot._optical_sensor);

	} // Robot


	//************************************************************
	// public methods
	//************************************************************

	//------------------------------------------------------------
	// Methoden zum Ermitteln diverser Informationen ueber den
	// Roboter
	//------------------------------------------------------------

	/**
	 * Ermitteln der Polygon-Szene in der sich der Roboter befindet.
	 */

	//============================================================
	public Polygon2Scene getScene()
	//============================================================
	{
		return _scene;
	} // getScene


	/**
	 * Ermitteln der Positionskoordinaten des Roboters.
	 * 
	 * Die Position eines Roboters innerhalb seiner Polygonszene ist durch ein
	 * (x,y)-Koordinatenpaar gegeben. Dabei handelt es sich bei einem nicht
	 * punktfoermigen Roboter um die Koordinaten eines festgelegten
	 * Bezugspunktes auf der Grundflaeche des Roboters.
	 */

	//============================================================
	public Point2 getCoors()
	//============================================================
	{
		return new Point2(_coors); // Es wird ein neues Point2-Objekt
									// erzeugt, damit der Aufrufer
									// keinen veraendernden Zugriff auf
									// die Robot-Koordinaten hat
	} // getCoors


	/**
	 * Ermitteln der X-Koordinate des Roboters
	 */

	//============================================================
	public double getX()
	//============================================================
	{
		return _coors.x;
	} // getX


	/**
	 * Ermitteln der Y-Koordinate des Roboters
	 */

	//============================================================
	public double getY()
	//============================================================
	{
		return _coors.y;
	} // getY


	/**
	 * Ermitteln der aktuellen Orientierung des Roboters.
	 * 
	 * Die Orientierung eines Roboters ist durch einen Bogenmass-Winkel alpha
	 * gegeben, fuer welchen immer gilt
	 * 
	 * 0.0 <= alpha < 2*PI.
	 * 
	 * Dieser Winkel legt die Orientierung des Roboters im mathematischen Sinn
	 * fest ( Winkel mit positiver x-Achse gegen den Uhrzeigersinn ), das heisst
	 * es gilt z.B.:
	 * 
	 * alpha == 0.0 ==> Roboter ist in Richtung Osten orientiert. alpha == PI
	 * ==> Roboter ist in Richtung Norden orientiert. alpha == PI*2 ==> Roboter
	 * ist in Richtung Westen orientiert. alpha == PI*3 ==> Roboter ist in
	 * Richtung Sueden orientiert.
	 */

	//============================================================
	public double getOrientation()
	//============================================================
	{
		return _orientation;
	} // getOrientation


	/**
	 * Ermitteln der Laenge Weges, den der Roboter seit seiner Erzeugung oder
	 * seit dem letzten Aufruf von resetPathLength() zurueckgelegt hat.
	 */

	//============================================================
	public double getPathLength()
	//============================================================
	{
		return _path_length;
	} // getPathLength


	/**
	 * Ermitteln des Winkelzaehlers des Roboters.
	 * 
	 * Ein Roboter verfuegt nur ueber einen Winkelzaehler, wenn die Methode
	 * useAngleCounter( true ) aufgerufen wurde. In diesem Fall summiert der
	 * Winkelzaehler bei Roboterdrehungen bzw. Aenderungen der
	 * Roboterorientierung die zugehoerigen Drehwinkel.
	 * 
	 * @return Winkelzaehler des Roboters oder <null>, falls der Roboter keinen
	 *         Winkelzaehler hat.
	 */

	//============================================================
	public AngleCounter getAngleCounter()
	//============================================================
	{
		return _angle_counter;
	} // getAngleCounter


	/**
	 * Ermitteln des optischen Sensors des Roboters.
	 * 
	 * Ein Roboter verfuegt nur ueber einen optischen Sensor, wenn die Methode
	 * useOpticalSensor( true ) aufgerufen wurde.
	 * 
	 * @return optischer Sensor des Roboters oder <null>, falls der Roboter
	 *         keinen optischen Sensor hat.
	 */

	//============================================================
	public OpticalSensor getOpticalSensor()
	//============================================================
	{
		return _optical_sensor;
	} // getOpticalSensor


	/**
	 * Ermitteln des aktuellen Winkelzaehler-Wertes des Roboters.
	 * 
	 * Dabei handelt es sich um die als Bogenmass-Winkel gegebene Summe aller
	 * Drehungen ( Orientierungsaenderungen ) des Roboters seit dem Aktivieren
	 * des Winkelzaehlers mit useAngleCounter( true ) oder seit dem letzten
	 * Aufruf von resetAngleCounter().
	 * 
	 * @return aktueller Winkelzaehlerwert ( Bogenmass ) oder 0.0, falls der
	 *         Roboter keinen Winkelzaehler hat.
	 */

	//============================================================
	public double getAngleCounterValue()
	//============================================================
	{
		return (_angle_counter != null) ? _angle_counter.getRelValue() : 0.0;
	} // getAngleCounterValue


	/**
	 * Ermitteln der Bewegungsgrenzen des Roboters.
	 * 
	 * Die Bewegungsgrenzen werden fuer einige move-Methoden benoetigt ( @see
	 * move() ) und muessen bei Bedarf mit der Methode setMoveRect(..) gesetzt
	 * werden.
	 * 
	 * @return Bewegungsgrenzen des Roboters oder <null>, falls keine
	 *         Bewegungsgrenzen gesetzt sind.
	 */

	//============================================================
	public Rectangle2 getMoveRect()
	//============================================================
	{
		if (_move_rect == null)
			return null;
		else
			return new Rectangle2(_move_rect);
	} // getMoveRect


	//------------------------------------------------------------
	// Verschiedene Methoden
	// ( Weglaenge, Winkelzaehler, Bewegungsgrenzen )
	//------------------------------------------------------------

	/**
	 * Zuruecksetzen der bisherigen Weglaenge des Roboters auf 0.0.
	 * 
	 * Nach dem Aufruf dieser Methode wird durch getPathLength() die Laenge des
	 * seither zurueckgelegten Weges geliefert.
	 */

	//============================================================
	public void resetPathLength()
	//============================================================
	{
		_path_length = 0.0;
	} // resetPathLength


	/**
	 * Aktivieren oder Deaktivieren eines Winkelzaehlers.
	 * 
	 * Falls aktiviert summiert der Winkelzaehler bei Roboterdrehungen ( also
	 * bei Aenderungen der Roboterorientierung ) die zugehoerigen Drehwinkel.
	 */

	//============================================================
	public void useAngleCounter(
			boolean use)
	//============================================================
	{
		if (use == false)
			_angle_counter = null;
		else if (_angle_counter == null)
		{
			_angle_counter = new AngleCounter();
			_angle_counter.reset(_orientation);
		} // if
	} // useAngleCounter


	/**
	 * Aktivieren oder Deaktivieren eines optischen Sensors.
	 */

	//============================================================
	public void useOpticalSensor(
			boolean use)
	//============================================================
	{
		if (use == false)
			_optical_sensor = null;
		else if (_optical_sensor == null)
		{
			_optical_sensor = new OpticalSensor(this);

		} // if
	} // useOpticalSensor


	/**
	 * Zuruecksetzen des Winkelzaehler-Wertes auf 0.0, falls der Roboter einen
	 * Winkelzaehler hat.
	 */

	//============================================================
	public void resetAngleCounter()
	//============================================================
	{
		if (_angle_counter != null)
			_angle_counter.reset(_orientation);
	} // resetAngleCounter


	/*
	 *  Sets the current scene
	 * 
	 */
	public void setScene(
			Polygon2Scene scene)
	{
		_scene = scene;
	}


	/**
	 * Setzten der Bewegungsgrenzen des Roboters fuer die Methoden move() und
	 * move( double angle ).
	 */

	//============================================================
	public void setMoveRect(
			Rectangle2 rect)
	//============================================================
	{
		if (rect == null)
			_move_rect = null;
		else
			_move_rect = new Rectangle2(rect);
	} // setMoveRect


	//------------------------------------------------------------
	// Methoden zum geradlinigen Bewegen des Roboters
	//------------------------------------------------------------

	/**
	 * Unbedingtes Bewegen des Roboters
	 * 
	 * Diese Methode bewegt den Roboter geradlinig an die durch <coors> gegebene
	 * neue Position.
	 * 
	 * !!! Dabei wird nicht geprueft, ob es sich bei der Zielposition um eine
	 * erlaubte Position des Roboters handelt und der Weg dorthin auch
	 * tatsaechlich ohne Hindernisse ist.
	 * 
	 * Es wird also vorrausgesetzt, dass die geradlinige Bewegung des Roboters
	 * zum Punkt <coors> moeglich ist, ohne dass dieser dabei an ein Hindernis
	 * stoesst.
	 */

	//============================================================
	public void setCoors(
			Point2 coors)
	//============================================================
	{
		// Streckenzaehler aktualisieren
		//
		_path_length += _coors.distance(coors);

		// Roboter-Koordinaten aktualisieren
		//
		_coors.moveTo(coors);
	} // setCoors


	/**
	 * Roboter in Richtung des Punktes <point> bewegen bis dieser erreicht ist
	 * oder der Roboter an ein Hindernis stoesst.
	 * 
	 * Diese Methode ist von Subklassen entsprechend zu ueberschreiben. Dabei
	 * ist die Methode setCoors(..) zur Ausfuehrung der Roboter- Bewegung zu
	 * benutzen.
	 * 
	 * <br>Beispiel:
	 * 
	 * <br>public void move( Point2 point ) { // Punkt ermitteln bis zu dem der
	 * Roboter tatsaechlich // bewegt werden kann // Point2 pt = ...;
	 * 
	 * // Bewegung ausfuehren // setCoors( pt ); }
	 */

	//============================================================
	abstract public void move(
			Point2 point);


	//============================================================

	/**
	 * Roboter um die Distanz <distance> in die durch den Bogenmass- Winkels <
	 * angle> gegebene Richtung bewegen, oder soweit bis er an ein Hindernis
	 * stoesst.
	 */

	//============================================================
	public void move(
			float distance,
			double angle)
	//============================================================
	{
		double dist_x = Math.cos(angle) * distance;
		double dist_y = Math.sin(angle) * distance;

		Point2 pt = new Point2(_coors.x + dist_x, _coors.y + dist_y);

		move(pt);
	} // move


	/**
	 * Roboter um die Distanz <distance> in Richtung des Punktes <point>
	 * bewegen, oder soweit bis er an ein Hindernis stoesst.
	 */

	//============================================================
	public void move(
			float distance,
			Point2 point)
	//============================================================
	{
		if (_coors.equals(point))
			return;
		double angle = _coors.angle(point);
		move(distance, angle);

	} // move


	/**
	 * Roboter in die durch den Bogenmass-Winkel < angle> gegebene Richtung
	 * bewegen, bis er an ein Hindernis stoesst oder eine der durch die Methode
	 * setMoveRect(..) gesetzten Bewegungsgrenzen erreicht hat. Diese Methode
	 * fuehrt eine Roboterbewegung nur dann aus, wenn Bewegungsgrenzen gesetzt
	 * sind.
	 * 
	 * Es ist zu beachten, dass die Grenzen der Bewegung abhaengig von der
	 * Bewegungsrichtung sind. Z.B. sind die Grenzen fuer eine Bewegung nach
	 * nord-ost die noerdliche und die oestliche Kante des Rechtecks.
	 * 
	 * @return false, falls eine Bewegungsgrenze erreicht wurde oder gar keine
	 *         Grenzen gesetzt sind, andernfalls true ( Roboter ist an ein
	 *         Hindernis gestossen )
	 */

	//============================================================
	public boolean move(
			double angle)
	//============================================================
	{
		if (_move_rect == null)
			return false; // Keine Bewegungsgrenzen gesetzt

		Point2 pt_move = _getMovePoint(angle);

		// Robot in Richtung des ermittelten Punktes bewegen.
		move(pt_move);

		return !_coors.equals(pt_move);
	} // move


	/**
	 * Roboter in Richtung seiner Orientierung bewegen, bis er an ein Hindernis
	 * stoesst oder eine der durch die Methode setMoveRect(..) gesetzten
	 * Bewegungsgrenzen erreicht hat.
	 * 
	 * @see #move(double )
	 * 
	 * @return false, falls eine Bewegungsgrenze erreicht wurde oder gar keine
	 *         Grenzen gesetzt sind, andernfalls true ( Roboter ist an ein
	 *         Hindernis gestoßen )
	 */

	//============================================================
	public boolean move()
	//============================================================
	{
		return move(_orientation);
	} // move


	//------------------------------------------------------------
	// Methoden zum Testen der Bewegungsfreiheit des Roboters
	//------------------------------------------------------------

	/**
	 * Testen, ob eine Bewegung des Roboters in der durch seine Orientierung
	 * gegebenen Richtung moeglich ist, genauer, ob sich die Position des
	 * Roboters durch den Aufruf von move() veraendern wuerde.
	 * 
	 * @return true <==> Bewegung ist moegich
	 */

	//============================================================
	public boolean isFreeToMove()
	//============================================================
	{
		return isFreeToMove(_orientation);
	} // isFreeToMove


	/**
	 * Testen, ob eine Bewegung des Roboters in der durch den Bogenmass- Winkel
	 * < angle> gegebenen Richtung moeglich ist, genauer, ob sich die Position
	 * des Roboters durch den Aufruf von move( angle ) veraendern wuerde.
	 * 
	 * @return true <==> Bewegung ist moegich
	 */

	//============================================================
	public boolean isFreeToMove(
			double angle)
	//============================================================
	{
		if (_move_rect == null)
			return false; // Keine Bewegungsgrenzen gesetzt

		return isFreeToMove(_getMovePoint(angle));
	} // isFreeToMove


	/**
	 * Testen, ob eine Bewegung des Roboters in Richtung des Punktes <point>
	 * moeglich ist, genauer, ob sich die Position des Roboters durch den Aufruf
	 * von move( point ) veraendern wuerde.
	 * 
	 * @return true <==> Bewegung ist moegich
	 */

	//============================================================
	public boolean isFreeToMove(
			Point2 point)
	//============================================================
	{
		Robot robot = (Robot) this.clone();

		robot.move(point);

		return !_coors.equals(robot._coors);
	} // isFreeToMove


	//------------------------------------------------------------
	// Methoden zum Drehen des Roboters
	//------------------------------------------------------------

	/**
	 * Unbedingtes Drehen des Roboters
	 * 
	 * !!! Dabei wird nicht geprueft, ob die Drehung auch tatsaechlich
	 * ausfuehrbar ist.
	 * 
	 * Es wird also vorrausgesetzt, dass die verlangte Drehung des Roboters
	 * moeglich ist, ohne dass dieser dabei an ein Hindernis stoesst.
	 * 
	 * @param angle
	 *            Durch Bogenmass-Winkel gegebene Richtung in die der Roboter
	 *            drehen ist.
	 * 
	 * @param turned_angle
	 *            Bogenmass-Winkel, um den der Roboter dabei gedreht wird.
	 *            Dieser Parameter ist im Zusammenhang mit der Benutzung eines
	 *            Winkelzaehlers von Bedeutung, und entscheidet in welcher
	 *            Richtung ( Uhrzeiger oder gegen den Uhrzeiger ) die Drehung
	 *            erfolgt und ob und wie viele Volldrehungen dabei ausgefuehrt
	 *            werden.
	 * 
	 *            turned_angle < 0 ==> Drehung gegen den Uhrzeigersinn.
	 *            turned_angle > 0 ==> Drehung im Uhrzeigersinn.
	 * 
	 *            Es wird vorrausgesetzt, dass der Winkel getOrientation() +
	 *            <turned_angle> die gleiche Richtung festgelegt wie der Winkel
	 *            < angle>.
	 */

	//============================================================
	public void setOrientation(
			double angle,
			double turned_angle)
	//============================================================
	{
		_orientation = MathUtil.getOrientation(angle);

		if (_angle_counter != null)
			_angle_counter.addAngle(turned_angle, _orientation);

	} // setOrientation


	/**
	 * Unbedingtes Drehen des Roboters in die durch den Bogenmasswinkel < angle>
	 * festgelegte Richtung.
	 * 
	 * Der Roboter wird auf kuerzestem Weg in die verlangte Richtung gedreht.
	 * Das heisst, die Drehrichtung ( Uhrzeigersinn oder gegen den Uhrzeigersinn
	 * ) wird so festgelegt, dass der Betrag des Drehwinkels minimal ist ( nur
	 * bei Benutzung eines Winkelzaehlers von Bedeutung ).
	 * 
	 * !!! Dabei wird nicht geprueft, ob die Drehung auch tatsaechlich
	 * ausfuehrbar ist.
	 * 
	 * Es wird also vorrausgesetzt, dass die verlangte Drehung des Roboters
	 * moeglich ist, ohne dass dieser dabei an ein Hindernis stoesst.
	 * 
	 * @param angle
	 *            Durch Bogenmass-Winkel gegebene Richtung in die der Roboter zu
	 *            drehen ist.
	 */

	//============================================================
	public void setOrientation(
			double angle)
	//============================================================
	{
		double new_ori = MathUtil.getOrientation(angle);

		if (new_ori == _orientation)
			return; // Roboter ist bereits in die verlangte Richtung
					// orientiert.

		// Drehwinkel ermitteln mit dem der Roboter auf kuerzestem Weg
		// in die verlangte Richtung gedreht wird.
		//
		double turn_angle = _getTurnAngle(new_ori);

		setOrientation(new_ori, turn_angle);
	} // setOrientation


	/**
	 * Unbedingtes Drehen des Roboters in Richtung des Punktes <point>.
	 * 
	 * @see #setOrientation( double )
	 * 
	 * @param point
	 *            Punkt, in dessen Richtung der Roboter zu drehen ist.
	 */

	//============================================================
	public void setOrientation(
			Point2 point)
	//============================================================
	{
		if (point.equals(_coors))
			return; // keine eindeutige Richtung

		setOrientation(_coors.angle(point));
	} // setOrientation


	/**
	 * Roboter um den Winkel <turn_angle> drehen, aber nur hoechstens soweit bis
	 * er dabei an ein Hindernis stoesst.
	 * 
	 * Diese Methode ist von Subklassen entsprechend zu ueberschreiben. Dabei
	 * ist die Methode setOrientation( double angle, double turned_angle ) zur
	 * Ausfuehrung der Roboter-Drehung zu benutzen.
	 * 
	 * Beispiel:
	 * 
	 * public double turn( double turn_angle ) { // Winkel ermitteln um den der
	 * Roboter tatsaechlich // gedreht werden kann, ohne dabei an ein Hindernis
	 * // zu stossen // double angle = ...;
	 * 
	 * // Drehung ausfuehren // setOrientation( getOrientation() + angle, angle
	 * );
	 * 
	 * return angle; }
	 * 
	 * @param turn_angle
	 *            Bogenmasswinkel um den der Roboter gedreht werden soll. Es
	 *            gilt turn_angle > 0 ==> Drehung gegen den Uhrzeigersinn
	 *            turn_angle < 0 ==> Drehung im Uhrzeigersinn
	 * 
	 * @return Bogenmasswinkel um den der Roboter tatsaechlich gedreht wurde.
	 */

	//============================================================
	abstract public double turn(
			double turn_angle);


	//============================================================

	/**
	 * Roboter auf kuerzestem Weg ( im oder gegen den Uhrzeigersinn ) in die
	 * durch den Winkel < angle> gegebene Richtung drehen, aber nur hoechstens
	 * soweit bis er dabei an ein Hindernis stoesst.
	 * 
	 * @param angle
	 *            Durch Bogenmass-Winkel gegebene Richtung in die der Roboter zu
	 *            drehen ist.
	 */

	//============================================================
	public void turnTo(
			double angle)
	//============================================================
	{
		double new_ori = MathUtil.getOrientation(angle);

		if (new_ori == _orientation)
			return; // Roboter ist bereits in der verlangten
					// Richtung orientiert

		// Drehwinkel ermitteln mit dem der Roboter auf kuerzestem Weg
		// in die verlangte Richtung gedreht wird.
		//
		double turn_angle = _getTurnAngle(new_ori);

		// Drehung ausfuehren
		//
		double turned_angle = turn(turn_angle);

		if (turned_angle == turn_angle)
			_orientation = new_ori;

	} // turnTo


	/**
	 * Roboter auf kuerzestem Weg ( im oder gegen den Uhrzeigersinn ) in die
	 * Richtung des Punktes <point> drehen, aber nur hoechstens soweit bis er
	 * dabei an ein Hindernis stoesst.
	 * 
	 * @param point
	 *            Punkt, in dessen Richtung der Roboter zu drehen ist.
	 */

	//============================================================
	public void turnTo(
			Point2 point)
	//============================================================
	{
		if (_coors.equals(point))
			return; // Keine eindeutige Richtung

		turnTo(_coors.angle(point));
	} // turnTo


	/**
	 * Roboter in die durch den Winkel < angle> gegebene Richtung drehen, aber
	 * nur hoechstens soweit bis er dabei an ein Hindernis stoesst. Die
	 * Drehrichtung ( im oder gegen den Uhrzeigersinn ) wird dabei durch
	 * <turn_dir> festgelegt.
	 * 
	 * @param angle
	 *            Durch Bogenmass-Winkel gegebene Richtung in die der Roboter zu
	 *            drehen ist.
	 * 
	 * @param turn_dir
	 *            CLOCKWISE oder COUNTERCLOCKWISE
	 */

	//============================================================
	public void turnTo(
			double angle,
			boolean turn_dir)
	//============================================================
	{
		double new_ori = MathUtil.getOrientation(angle);

		if (new_ori == _orientation)
			return; // Roboter ist bereits in der verlangten
					// Richtung orientiert

		// Drehwinkel ermitteln
		//
		double turn_angle = new_ori - _orientation;

		if (turn_dir == CLOCKWISE)
		{
			if (turn_angle >= 0.0)
				turn_angle -= Math.PI * 2;
		}
		else
		// turn_dir == COUNTERCLOCKWISE
		{
			if (turn_angle <= 0.0)
				turn_angle += Math.PI * 2;
		} // else

		// Drehung ausfuehren
		//
		double turned_angle = turn(turn_angle);

		if (turned_angle == turn_angle)
			_orientation = new_ori;
	} // turnTo


	/**
	 * Diese Methode hat nur Bedeutung wenn der Roboter einen Winkelzaehler hat.
	 * 
	 * Der Roboter wird soweit gedreht, bis der Winkelzaehler wieder den Wert
	 * 0.0 hat, aber nur hoechstens soweit bis der Roboter dabei an ein
	 * Hindernis stoesst.
	 * 
	 * Die Drehrichtung ( im oder gegen den Uhrzeigersinn ) ergibt sich dabei
	 * eindeutig aus dem aktuellen Wert des Winkelzaehlers.
	 */

	//============================================================
	public void turnToInitAngle()
	//============================================================
	{
		if (_angle_counter == null)
			return;

		double angle = -_angle_counter.getRelValue();

		turn(angle);

		_angle_counter.reset(_orientation);
	} // turnToInitAngle


	//------------------------------------------------------------
	// Methoden zum Testen der Orientierung des Roboters
	//------------------------------------------------------------

	/**
	 * Testen, ob der Roboter in der durch den Bogenmasswinkel < angle>
	 * gegebenen Richtung orientiert ist.
	 * 
	 * @return true <==> Roboter ist in Richtung < angle> orientiert
	 */

	//============================================================
	public boolean isOrientatedTo(
			double angle)
	//============================================================
	{
		return (_orientation == MathUtil.getOrientation(angle));
	} // isOrientatedTo


	/**
	 * Testen, ob der Roboter in der Richtung des Punktes <point> orientiert
	 * ist.
	 * 
	 * @return true <==> Roboter ist in Richtung <point> orientiert
	 */

	//============================================================
	public boolean isOrientatedTo(
			Point2 point)
	//============================================================
	{
		if (point.equals(_coors))
			return false; // Keine eindeutige Richtung

		return isOrientatedTo(_coors.angle(point));
	} // isOrientatedTo


	//************************************************************
	// abstract public methods
	//************************************************************

	/**
	 * Liefert einen neuen Roboter, der ein Duplikat dieses Roboters ist.
	 * 
	 * Diese Methode muss von Subklassen entsprechend ueberschrieben werden.
	 * 
	 * Sei SubRobot eine von Robot abgeleitete nicht-abstrakte Klasse. Dann
	 * sollte nach folgendem Muster vorgegangen werden:
	 * 
	 * 
	 * Bereitstellung eines Konstruktors zum Erzeugen der Kopie
	 * 
	 * public SubRobot( SubRobot robot ) { super( robot );
	 * 
	 * // ggf. weitere Datenelemente der Klasse SubRobot // kopieren; }
	 * 
	 * 
	 * Implementierung der Methode clone()
	 * 
	 * public Object clone() { return new SubRobot( this ); }
	 */

	//============================================================
	abstract public Object clone();


	//============================================================

	/**
	 * Zeichnen des Roboters.
	 * 
	 * Diese Methode muss von Subklassen entsprechend ueberschrieben werden.
	 * 
	 * Dabei kann sowohl in Weltkoordinaten als auch in Bildkoordinaten
	 * gezeichnet werden
	 * 
	 * Beispiel:
	 * 
	 * public void paint( Graphics2D g_world, Graphics g_screen ) { Point2 coors
	 * = getCoors();
	 * 
	 * 
	 * // Roboter-Position durch blauen Punkt markieren // ( Zeichnen auf
	 * g_world )
	 * 
	 * GraphicsContext gc = new GraphicsContext(); gc.setForegroundColor(
	 * Color.blue );
	 * 
	 * coors.draw( g_world, gc );
	 * 
	 * 
	 * // Weltkoordinaten in Bildkoordinaten umwandeln
	 * 
	 * Transform transform = g_world.getTransform(); transform.transform( coors,
	 * coors ); int x_screen = Math.round( coors.x ); int y_screen = Math.round(
	 * coors.y );
	 * 
	 * 
	 * // Roboter-Position durch roten Kreis markieren // ( Zeichnen auf
	 * g_screen )
	 * 
	 * g_screen.setColor( Color.red ); g_screen.fillOval( x_screen-3,
	 * y_screen-3, 7, 7 ); }
	 * 
	 * @param g_world
	 *            Graphics-Objekt zum Zeichnen in Weltkoordinaten.
	 * @param g_screen
	 *            Graphics-Objekt zum Zeichnen in Bildkoordinaten.
	 */

	//============================================================
	abstract public void paint(
			Graphics2D g_world,
			Graphics g_screen);


	//============================================================

	//************************************************************
	// private methods
	//************************************************************

	/**
	 * Ermittlung des Schnittpunktes des vom Roboter in Richtung des Winkels <
	 * angle> ausgehenden Strahls mit den durch das Rechteck _move_rect
	 * gegebenen Bewegungsgrenzen des Roboters.
	 */

	//============================================================
	private Point2 _getMovePoint(
			double angle)
	//============================================================
	{
		angle = MathUtil.getOrientation(angle);
		// Es gilt jetzt: 0 <= angle < 2*pi

		// linke, rechte, untere und obere Bewegungsgrenzen ermitteln
		double xmin = _move_rect.x;
		double xmax = xmin + _move_rect.width;
		double ymin = _move_rect.y;
		double ymax = ymin + _move_rect.height;

		double pi_0_5 = Math.PI * 0.5;
		double pi = Math.PI;
		double pi_1_5 = Math.PI * 1.5;

		// Zielpunkt der Bewegung als Schnittpunkt des Bewegungsstrahls
		// mit Bewegungsgrenzen ermitteln. Dabei werden die Faelle
		//
		//          0 <= angle < 0.5*pi
		//     0.5*pi <= angle < pi
		//         pi <= angle < 1.5*pi
		//     1.5*pi <= angle < 2*pi
		//
		// unterschieden. Dies ist noetig, da die Bewegungsgrenzen
		// je nach Bewegungsrichtung festgelegt werden muessen.
		// ( z.B. sind xmin und ymax die Grenzen fuer eine Bewegung nach
		//   links-oben )

		Point2 pt_move;

		double x = _coors.x;
		double y = _coors.y;

		if (angle < pi_0_5)
		{
			// 0 <= angle < 0.5*pi        Bewegung in Richtung Quadrant1

			Point2 pt = _getMovePoint(x, y, angle, xmax, ymax);

			pt_move = new Point2(pt.x, pt.y);
		}
		else if (angle < pi)
		{
			// 0.5*pi <= angle < pi       Bewegung in Richtung Quadrant2

			Point2 pt = _getMovePoint(y, -x, angle - pi_0_5, ymax, -xmin);

			pt_move = new Point2(-pt.y, pt.x);
		}
		else if (angle < pi_1_5)
		{
			// pi <= angle < 1.5*pi       Bewegung in Richtung Quadrant3

			Point2 pt = _getMovePoint(-x, -y, angle - pi, -xmin, -ymin);

			pt_move = new Point2(-pt.x, -pt.y);
		}
		else
		{
			// 1.5*pi <= angle < 2*pi     Bewegung in Richtung Quadrant4

			Point2 pt = _getMovePoint(-y, x, angle - pi_1_5, -ymin, xmax);

			pt_move = new Point2(pt.y, -pt.x);
		} // else

		return pt_move;
	} // _getMovePoint


	/**
	 * Ermitteln des Schnittpunktes des von ( x, y ) in Richtung angle
	 * ausgehenden Strahles mit der durch xmax bzw ymax definierten vertikalen
	 * bzw. horizontalen Gerade.
	 * 
	 * Fuer den Winkel angle muss gelten 0 <= angle < PI/2
	 * 
	 * Die Methode wird in move( double ) verwendet, um die Bewegung des Robots
	 * zu begrenzen. Deshalb werden xmax, ymax in den Kommentaren dieser Methode
	 * als Bewegungsgrenzen bezeichnet.
	 */

	//============================================================
	private Point2 _getMovePoint(
			double x,
			double y,
			double angle,
			double xmax,
			double ymax)
	//============================================================
	{
		if (x >= xmax)
			return new Point2(x, y); // Die horizontale Bewegungsgrenze
		// ist bereits ueberschritten ( also keine Bewegung )

		// Es gilt: x < xmax

		if (angle == 0.0)
			return new Point2(xmax, y); // Die vertikale Gerade xmax
		// ist die Grenze der horizontalen Bewegung

		// Es gilt: 0 < angle < pi/2

		if (y >= ymax)
			return new Point2(x, y); // Die vertikale Bewegungsgrenze
		// ist bereits ueberschritten ( also keine Bewegung )

		// Es gilt: x < xmax
		//          y < ymax
		//          0 < angle < pi/2
		//
		// Es ist also eine Bewegung in Richtung angle erlaubt

		// Winkel des Segments ( ( x, y ), ( xmax, ymax ) ) ermitteln
		Point2 pt1 = new Point2(x, y);
		Point2 pt2 = new Point2(xmax, ymax);
		double alpha = pt1.angle(pt2);

		if (angle == alpha)
			return new Point2(xmax, ymax); // Der Punkt ( xmax, ymax )
		// ist die Grenze fuer die Bewegung in Richtung angle

		if (angle > alpha)
		{
			// Die horizontale Gerade ymax ist die Bewegungsgrenze
			double cotan = Math.tan(Math.PI / 2 - angle);
			double xmove = x + (ymax - y) * cotan;
			return new Point2(xmove, ymax);
		} // if

		// Es gilt: angle < alpha

		// Die vertikale Gerade xmax ist die Bewegungsgrenze
		double ymove = y + (xmax - x) * Math.tan(angle);
		return new Point2(xmax, ymove);

	} // _getMovePoint


	/**
	 * Ermitteln des Drehwinkels mit dem der Roboter auf kuerzestem Wege (
	 * entweder im oder gegen den Uhrzeigersinn ) in die Richtung der
	 * Orientierung <ori> gedreht wird.
	 */

	//============================================================
	private double _getTurnAngle(
			double ori)
	//============================================================
	{
		double turn_angle = ori - _orientation;

		if (turn_angle > Math.PI)
			turn_angle -= 2 * Math.PI;
		else if (turn_angle <= -Math.PI)
			turn_angle += 2 * Math.PI;

		return turn_angle;
	} // _getTurnAngle

} // Robot
