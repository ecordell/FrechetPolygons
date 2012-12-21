package anja.polyrobot.robots;


import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.geom.AffineTransform;

import anja.polyrobot.MathUtil;

import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Polygon2;

import anja.geom.Polygon2Scene;


/**
 * @version 0.1 19.09.01
 * @author Ulrich Handel
 */

//****************************************************************
public class PointRobot
		extends ScenePosition
//****************************************************************
{

	//************************************************************
	// public class variables
	//************************************************************

	// Farben die beim Zeichnen des Roboters benutzt werden
	// ( @see paint(..) )

	public static Color	color_outline	= Color.red;

	public static Color	color_free		= Color.green.darker();
	public static Color	color_vertex	= Color.black;
	public static Color	color_edge		= Color.blue;


	//************************************************************
	// constructors
	//************************************************************

	/**
     */

	//============================================================
	public PointRobot(
			PointRobot robot)
	//============================================================
	{
		super(robot);
	} // PointRobot


	/**
	 * Erzeugen eines neuen Point-Robots auf der freien Flaeche einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * <br>Wichtig !!!
	 * 
	 * <br>Es wird davon ausgegangen, dass sich die angegebene Position auf
	 * freier Flaeche befindet, d.h. sie liegt ausserhalb aller
	 * Interior-Polygone ( ggf. innerhalb des Bounding-Polygons ) und beruehrt
	 * dabei weder eine Polygon-Kante noch einen Eckpunkt. Diese Vorbedingung
	 * wird durch den Konstruktor nicht geprueft.
	 * 
	 * @param scene
	 *            Polygonszene, in der sich der Robot befindet.
	 * @param position
	 *            Position des Robots in der Polygonszene.
	 * 
	 * 
	 */

	//============================================================
	public PointRobot(
			Polygon2Scene scene,
			Point2 position)
	//============================================================
	{
		super(scene, position);
	} // PointRobot


	/**
	 * Erzeugen eines neuen Point-Robots an einem Polygon-Eckpunkt einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * <br> Bemerkung zur Positionierung an einem Polygon:
	 * 
	 * Wenn der Robot sich an einem Eckpunkt oder auf einer Kante eines
	 * geschlossenes Polygons befindet, so beruehrt er dieses zwar, befindet
	 * sich aber dennoch ausserhalb. Das heisst, er kann sich vom Polygon weg in
	 * den freien Raum, nicht aber in das Innere des Polygons bewegen.
	 * 
	 * Der Robot verwaltet also im Falle, dass er auf einer Kante oder einem
	 * Eckpunkt positioniert ist, zusaetzliche Informationen darueber, auf
	 * "welcher Seite" des "beruehrten" Kantenzugs er sich befindet. Dies ist
	 * bei geschlossenen Polygonen eindeutig. Bei offenen Polygonen jedoch
	 * nicht.
	 * 
	 * Soll beim Erzeugen eines Robots am Eckpunkt eines offenen Polygons
	 * explizit angegeben werden, auf welcher Seite des Kantenzugs sich der
	 * Robot befindet, so ist der Konstruktor
	 * 
	 * PointRobot( scene, polygon, vertex, freepoint )
	 * 
	 * zu benutzen.
	 * 
	 * @param scene
	 *            Polygonszene, in der sich der Robot befindet.
	 * @param polygon
	 *            Polygon, an dem sich der Robot befindet.
	 * @param vertex
	 *            Polyon-Eckpunkt, an dem sich der Robot befindet.
	 * 
	 * 
	 * 
	 */

	//============================================================
	public PointRobot(
			Polygon2Scene scene,
			Polygon2 polygon,
			Point2 vertex)
	//============================================================
	{
		this(scene, polygon, vertex, vertex);
	} // PointRobot


	/**
	 * Erzeugen eines neuen Point-Robots an einem Polygon-Eckpunkt einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * @param scene
	 *            Polygonszene, in der sich der Robot befindet.
	 * @param polygon
	 *            Polygon, an dem sich der Robot befindet.
	 * @param vertex
	 *            Polyon-Eckpunkt, an dem sich der Robot befindet.
	 * 
	 * @param freepoint
	 *            Punkt, in dessen Richtung der Robot Bewegungs- freiheit hat (
	 *            siehe weiteren Kommentar ).
	 * 
	 *            Durch die Angabe des Parameters freepoint wird bei der
	 *            Beruehrung mit einem offenen Polygon festgelegt, auf welcher
	 *            Seite des Kanten- zugs sich der Robot befindet. Bei der
	 *            Beruehrung mit einem geschlossenen Polygon wird freepoint
	 *            ignoriert, da sich der Robot immer ausserhalb des Polygons
	 *            befindet, wodurch seine Bewegungsfreiheit eindeutig festgelegt
	 *            ist.
	 * 
	 * @see #PointRobot(Polygon2Scene, Polygon2, Point2 )
	 */

	//============================================================
	public PointRobot(
			Polygon2Scene scene,
			Polygon2 polygon,
			Point2 vertex,
			Point2 freepoint)
	//============================================================
	{
		super(scene, polygon, vertex, freepoint);
	} // PointRobot


	/**
	 * Erzeugen eines neuen Point-Robots auf einer Polygon-Kante einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * @param scene
	 *            Polygonszene, in der sich der Robot befindet.
	 * @param polygon
	 *            Polygon, an dem sich der Robot befindet.
	 * @param edge
	 *            Polyon-Kante, auf der sich der Robot befindet.
	 * 
	 * @param point
	 *            Punkt, der zur Festlegung der genauen Position auf der Kante
	 *            dient ( siehe weiteren Kommentar ).
	 * 
	 *            Die genaue Position des Robots auf der Kante ist der
	 *            Schnittpunkt zwischen der Kante und der zu dieser senkrechten
	 *            durch den Punkt point verlaufenden Geraden. Im Falle, dass das
	 *            beruehrte Polygon offen ist. legt point zusaetzlich fest, auf
	 *            welcher Seite der Kante sich der Robot befindet.
	 * 
	 * @see #PointRobot(Polygon2Scene, Polygon2, Point2 )
	 * @see #PointRobot(Polygon2Scene, Polygon2, Point2, Point2 )
	 */

	//============================================================
	public PointRobot(
			Polygon2Scene scene,
			Polygon2 polygon,
			Segment2 edge,
			Point2 point)
	//============================================================
	{
		super(scene, polygon, edge, point);
	} // PointRobot


	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Liefern eines neuen Robots, der ein Duplikat dieses Robots ist.
	 */

	//============================================================
	public Object clone()
	//============================================================
	{
		return new PointRobot(this);
	} // clone


	/**
	 * Testen, ob der Robot in Richtung des naechsten Eckpunktes in angegebener
	 * Umlaufrichtung orientiert ist
	 * 
	 * @param dir
	 *            LEFT_HAND oder RIGHT_HAND
	 * 
	 * @return true ==> Robot ist in Richtung des naechsten Eckpunktes
	 *         orientiert false ==> Robot ist nicht in Richtung des naechsten
	 *         Eckpunktes orientiert oder befindet sich gar nicht an einem
	 *         Polygon
	 */

	//============================================================
	public boolean isOrientatedToNextVertex(
			boolean dir)
	//============================================================
	{
		Point2 vertex = getNextVertex(dir);

		if (vertex == null)
			return false;

		return getOrientation() == MathUtil.getOrientation(getCoors().angle(
				vertex));
	} // isOrientatedToNextVertex


	/**
     */

	//============================================================
	public double turn(
			double turn_angle)
	//============================================================
	{
		setOrientation(getOrientation() + turn_angle, turn_angle);

		return turn_angle;
	} // turn


	/**
	 * Zeichnen des Roboters
	 */

	//============================================================
	public void paint(
			Graphics2D g_world,
			Graphics g_screen)
	//============================================================
	{
		// Roboterposition in Bildkoordinaten ermitteln
		Point2 point = getCoors();

		//AffineTransform transform = g_world.getTransform();
		//transform.transform( point, point );

		Color center_col = color_free;
		Polygon2 poly = getPolygon();

		if (poly != null)
		{
			if (poly.isOpen())
			{
				double angle = getContactAngle();

				point.moveTo(point.x + 4 * Math.cos(angle),
						point.y - 4 * Math.sin(angle));
			} // if

			if (isAtVertex())
				center_col = color_vertex;
			else
				center_col = color_edge;
		} // if

		// Roboter zeichnen
		Robot.drawRobotCenter(g_screen, Math.round(point.x),
				Math.round(point.y), getOrientation(), color_outline,
				center_col);

	} // paint


	//************************************************************
	// public Methoden, die den Winkelzaehler betreffen
	//************************************************************

	/**
	 * Robot in Richtung des Eckpunktes drehen, den er als naechstes erreichen
	 * wuerde, wenn er sich in der angegebenen Richtung um das Polygon bewegen
	 * wuerde an dem er sich befindet.
	 * 
	 * Diese Methode aktualisiert den Winkelzaehler.
	 * 
	 * @param dir
	 *            RIGHT_HAND oder LEFT_HAND
	 * 
	 * @return Der Winkel um den sich der Robot gedreht hat oder 0.0 falls sich
	 *         der Robot an keinem Hindernis befindet.
	 */

	//============================================================
	public double turnToNextVertex(
			boolean dir)
	//============================================================
	{
		if (isAtObstacle() == false)
			return 0.0; // Robot befindet sich an keinem Polygon,
						// also dreht er sich auch nicht.

		double new_ori;

		double left_turn;
		double right_turn;

		Point2 vertex_left_hand = getNextVertex(Robot.LEFT_HAND);
		Point2 vertex_right_hand = getNextVertex(Robot.RIGHT_HAND);

		Point2 coors = getCoors();

		if (dir == RIGHT_HAND)
		{
			// Berechnen des Winkels um den der Robot im Uhrzeigersinn
			// gedreht werden muss, um ihn in Richtung des rechts
			// liegenden Eckpunktes ( _vertex_left_hand ) zu orientieren.
			//
			// Dieser Winkel ist immer kleiner gleich 0 ( rechts-drehung )
			//
			right_turn = coors.angle(vertex_left_hand) - getOrientation();
			while (right_turn > 0.0)
				right_turn -= Math.PI * 2;

			// Berechnen des Winkels um den der in Richtung des rechts
			// liegenden Eckpunktes orientierte Robot gegen den Uhrzeigersinn
			// gedreht werden muss, um ihn in Richtung des links liegenden
			// Eckpunktes ( _vertex_right_hand ) zu orientieren.
			//
			// Dieser Winkel ist immer groesser gleich 0 ( links-drehung )
			//
			left_turn = coors.angle(vertex_left_hand, vertex_right_hand);

			// Robot in Richtung _vertex_right_hand drehen

			new_ori = coors.angle(vertex_right_hand);
		}
		else
		// dir == LEFT_HAND
		{
			// Berechnen des Winkels um den der Robot gegen den Uhrzeigersinn
			// gedreht werden muss, um ihn in Richtung des
			// links liegenden Eckpunktes ( _vertex_right_hand ) zu orientieren.
			//
			// Dieser Winkel ist immer groesser gleich 0 ( links-drehung )
			//
			left_turn = coors.angle(vertex_right_hand) - getOrientation();

			while (left_turn < 0.0)
				left_turn += Math.PI * 2;

			// Berechnen des Winkels um den der in Richtung des links
			// liegenden Eckpunktes orientierte Robot im Uhrzeigersinn gedreht
			// werden muss, um ihn in Richtung des rechts liegenden Eckpunktes
			// ( _vertex_left_hand ) zu orientieren.
			//
			// Dieser Winkel ist immer kleiner gleich 0 ( rechts-drehung )
			//
			right_turn = -coors.angle(vertex_left_hand, vertex_right_hand);

			// Robot in Richtung _vertex_left_hand drehen

			new_ori = coors.angle(vertex_left_hand);
		} // else

		double turn = left_turn + right_turn;

		if (turn == 0.0)
			return 0.0;

		boolean turn_dir = (turn > 0.0) ? COUNTERCLOCKWISE : CLOCKWISE;

		turnTo(new_ori, turn_dir);

		return turn;
	} // turnToNextVertex

	//************************************************************
	// Private Methoden
	//************************************************************

} // PointRobot
