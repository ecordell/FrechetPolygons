package anja.polyrobot.robots;


// what the hell are these ???????????

//<<<<<<< ScenePosition.java
//=======

import java.awt.Graphics;
import java.awt.Graphics2D;

//>>>>>>> 1.7

import anja.geom.Point2;
import anja.geom.Segment2;
import anja.geom.Polygon2;
import anja.geom.Intersection;

import anja.geom.Polygon2Scene;

import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * @version 0.1 05.07.03
 * @author Ulrich Handel
 */

//****************************************************************
public class ScenePosition
		extends Robot
//****************************************************************
{

	//************************************************************
	// private variables
	//************************************************************

	private Polygon2	_contact_poly;		// Polygon, an dessen Kante oder
											// Eckpunkt sich der Robot befindet,
											// oder null

	private Point2		_vertex;			// Eckpunkt, an dem sich der Robot befindet,
											// oder null.

	private Point2		_vertex_left_hand;
	// Eckpunkt, den der Robot als naechstes
	// erreicht, wenn er sich mit der linken
	// Hand am Polygon weiter um das Polygon
	// _contact_poly bewegt,
	// oder null.

	private Point2		_vertex_right_hand; // ( siehe _vertex_left_hand )


	//************************************************************
	// constructors
	//************************************************************

	/**
	 * Erzeugen eines neuen Point-Robots auf der freien Flaeche einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * <br>Wichtig !!!
	 * 
	 * <br>Es wird davon ausgegangen, dass sich die angegebene Position auf freier
	 * Flaeche befindet, d.h. sie liegt ausserhalb aller Interior-Polygone (
	 * ggf. innerhalb des Bounding-Polygons ) und beruehrt dabei weder eine
	 * Polygon-Kante noch einen Eckpunkt. Diese Vorbedingung wird durch den
	 * Konstruktor nicht geprueft.
	 * 
	 * @param scene
	 *            Polygonszene, in der sich der Robot befindet.
	 * @param position
	 *            Position des Robots in der Polygonszene.
	 * 
	 * 
	 */

	//============================================================
	public ScenePosition(
			Polygon2Scene scene,
			Point2 position)
	//============================================================
	{
		super(scene, position);
	} // ScenePosition


	/**
	 * Erzeugen eines neuen Point-Robots an einem Polygon-Eckpunkt einer
	 * Polygonszene mit der Orientierung 0.0 ( also nach Osten ).
	 * 
	 * <br>Bemerkung zur Positionierung an einem Polygon:
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
	 * ScenePosition( scene, polygon, vertex, freepoint )
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
	public ScenePosition(
			Polygon2Scene scene,
			Polygon2 polygon,
			Point2 vertex)
	//============================================================
	{
		this(scene, polygon, vertex, vertex);
	} // ScenePosition


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
	 * @see #ScenePosition(Polygon2Scene, Polygon2, Point2 )
	 */

	//============================================================
	public ScenePosition(
			Polygon2Scene scene,
			Polygon2 polygon,
			Point2 vertex,
			Point2 freepoint)
	//============================================================
	{
		super(scene, vertex);

		_setToVertex(polygon, vertex, freepoint);
	} // ScenePosition


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
	 * @see #ScenePosition(Polygon2Scene, Polygon2, Point2 )
	 * @see #ScenePosition(Polygon2Scene, Polygon2, Point2, Point2 )
	 */

	//============================================================
	public ScenePosition(
			Polygon2Scene scene,
			Polygon2 polygon,
			Segment2 edge,
			Point2 point)
	//============================================================
	{
		super(scene, edge.plumb(point));

		_setToEdge(polygon, edge, getCoors(), point);
	} // ScenePosition


	/**
     */

	//============================================================
	public ScenePosition(
			ScenePosition pos)
	//============================================================
	{
		super(pos);

		_contact_poly = pos._contact_poly;
		_vertex = pos._vertex;
		_vertex_left_hand = pos._vertex_left_hand;
		_vertex_right_hand = pos._vertex_right_hand;
	} // ScenePosition


	//************************************************************
	// public methods
	//************************************************************

	/**
	 * Aendert die Scene in der sich der Roboter befindet.
	 * 
	 * _contact_poly, _vertex ... muessen noch aktualisiert werden.
	 * 
	 */

	//============================================================
	public void changeScene(
			Polygon2Scene scene)
	//============================================================
	{

		setScene(scene);

		_contact_poly = null;

	} // ScenePosition


	/**
	 * Liefern eines neuen Robots, der ein Duplikat dieses Robots ist.
	 */

	//============================================================
	public Object clone()
	//============================================================
	{
		return new ScenePosition(this);
	} // clone


	/**
     */

	//============================================================
	public boolean isEqualTo(
			ScenePosition position)
	//============================================================
	{
		return (getScene() == position.getScene() && getX() == position.getX()
				&& getY() == position.getY()
				&& _contact_poly == position._contact_poly
				&& _vertex == position._vertex
				&& _vertex_left_hand == position._vertex_left_hand && _vertex_right_hand == position._vertex_right_hand);
	} // isEqualTo


	/**
	 * @return true <==> Robot befindet sich an einem Polygon
	 */

	//============================================================
	public boolean isAtObstacle()
	//============================================================
	{
		return (_contact_poly != null);
	} // isAtObstacle


	/**
	 * @return true <==> Robot befindet sich an einem Polygon-Eckpunkt
	 */

	//============================================================
	public boolean isAtVertex()
	//============================================================
	{
		return (_vertex != null);
	} // isAtVertex


	/**
	 * Ermitteln des Polygon-Eckpunktes an dem sich der Robot befindet.
	 * 
	 * @return Der Eckpunkt oder null, falls sich der Robot an keinem
	 *         Polygon-Eckpunkt befindet.
	 */

	//============================================================
	public Point2 getVertex()
	//============================================================
	{
		return _vertex;
	} // getVertex


	/**
	 * Ermitteln des Polygons an dem sich der Robot befindet.
	 * 
	 * @return Das Polygon oder null, falls sich der Robot an keinem Polygon
	 *         befindet.
	 */

	//============================================================
	public Polygon2 getPolygon()
	//============================================================
	{
		return _contact_poly;
	} // getPolygon


	/**
	 * Ermitteln des Eckpunktes, den der Robot als naechstes erreichen wuerde,
	 * wenn er sich in der angegebenen Richtung um das Polygon bewegen wuerde,
	 * an dem er sich befindet.
	 * 
	 * @param dir
	 *            LEFT_HAND oder RIGHT_HAND
	 * 
	 * @return Der Eckpunkt oder null, falls sich der Robot an keinem Polygon
	 *         befindet.
	 */

	//============================================================
	public Point2 getNextVertex(
			boolean dir)
	//============================================================
	{
		if (dir == LEFT_HAND)
			return _vertex_left_hand;
		else
			return _vertex_right_hand;
	} // getNextVertex


	/**
	 * Ermitteln des Winkels, den der Robot zur Kante bzw. zum Vertex des
	 * Polygons hat, mit dem er Kontakt hat.
	 * 
	 * Wichtig: Falls der Robot Kontakt zu einem Polygon hat ( isAtObstacle ),
	 * liegt seine Position genau auf einer Kante bzw. einem Vertex des
	 * Polygons. Genaugenommen befindet sich der Robot aber immer in einem
	 * infinitisimal kleinen Abstand dazu.
	 * 
	 * Der berechnete Winkel ist der Winkel von dem infinitisimal neben der
	 * Kante bzw. dem Vertex liegenden Punkt zur Kante bzw. dem Vertex.
	 * 
	 * Diese Methode wird gebraucht, um beim Zeichnen der Szene insbesondere bei
	 * offenen Polygonen darstellen zu koennen auf welcher Seite eines
	 * Kantenzugs sich der Robot befindet.
	 * 
	 * @return Der Winkel oder 0.0, falls sich der Robot an keinem Polygon
	 *         befindet
	 */

	//============================================================
	public double getContactAngle()
	//============================================================
	{
		if (_contact_poly == null)
			return 0.0;

		double angle;

		if (_vertex != null)
		{
			// Robot befindet sich am Eckpunkt, der zwischen
			// _vertex_right_hand und _vertex_left_hand liegt

			angle = _vertex_right_hand.angle(_vertex)
					- _vertex.angle(_vertex_left_hand, _vertex_right_hand) / 2;
		}
		else
		{
			// Robot befindet sich an der Kante, die
			// _vertex_right_hand mit _vertex_left_hand verbindet

			angle = _vertex_right_hand.angle(_vertex_left_hand) - Math.PI / 2;
		}

		double two_pi = 2 * Math.PI;

		if (angle < 0.0)
			angle += two_pi;
		else if (angle >= two_pi)
			angle -= two_pi;

		return angle;
	} // getContactAngle


	/**
	 * Robot, der sich an einem Hindernis befindet, in der angegebenen Richtung
	 * zum naechsten Eckpunkt bewegen
	 * 
	 * @param dir
	 *            RIGHT_HAND oder LEFT_HAND
	 */

	//============================================================
	public void moveToNextVertex(
			boolean dir)
	//============================================================
	{
		moveToNextVertex(dir, getNextVertex(dir));
	} // moveToNextVertex


	/**
	 * Robot, der sich an einem Hindernis befindet, in der angegebenen Richtung
	 * zum naechsten Eckpunkt bewegen bis der angegebene Punkt erreicht ist.
	 * 
	 * @param dir
	 *            RIGHT_HAND oder LEFT_HAND
	 * 
	 *            !!! Es wird vorrausgesetzt, dass der angegebene Punkt auf der
	 *            Strecke zum naechsten Eckpunkt liegt. Dies wird nicht
	 *            geprueft.
	 */

	//============================================================
	public void moveToNextVertex(
			boolean dir,
			Point2 pt)
	//============================================================
	{
		if (_contact_poly == null)
			return; // Robot befindet sich an keinem Hindernis

		if (pt.x == getX() && pt.y == getY())
			return; // Robot befindet sich bereits am zu erreichenden
					// Punkt

		if (_vertex != null)
		{
			// Robot befindet sich an einem Eckpunkt.
			// Dieser Punkt wird durch die Bewegung zum links bzw.
			// zum rechts liegenden Eckpunkt.
			if (dir == RIGHT_HAND)
				_vertex_left_hand = _vertex;
			else
				_vertex_right_hand = _vertex;
		} // if

		Point2 next_vertex = getNextVertex(dir);

		if (pt.equals(next_vertex))
		{
			// Der zu erreichende Punkt ist der naechste Eckpunkt
			// in der durch dir gegebenen Richtung

			_vertex = next_vertex;

			if (dir == RIGHT_HAND)
				_vertex_right_hand = _getNeighbourVertex(_contact_poly,
						_vertex, _vertex_left_hand);
			else
				_vertex_left_hand = _getNeighbourVertex(_contact_poly, _vertex,
						_vertex_right_hand);
		}
		else
		{
			// Der zu erreichende Punkt ist kein Eckpunkt, also befindet
			// sich der Robot nach der Bewegung an gar keinem Eckpunkt,
			// sondern liegt zwischen _vertex_right_hand und _vertex_left_hand;
			_vertex = null;
		} // else

		setCoors(pt);
	} // moveToNextVertex


	/**
     */

	//============================================================
	private boolean _moveToNextVertex(
			Point2 vertex)
	//============================================================
	{
		if (vertex.equals(_vertex_right_hand))
		{
			moveToNextVertex(RIGHT_HAND);
			return true;
		} // if

		if (vertex.equals(_vertex_left_hand))
		{
			moveToNextVertex(LEFT_HAND);
			return true;
		} // if

		return false;
	} // _moveToNextVertex


	/**
     */

	//============================================================
	private boolean _moveToNextVertex(
			boolean dir,
			Point2 target)
	//============================================================
	{
		Point2 vertex_next = getNextVertex(dir);

		Point2 pt = _getInsidePoint(_vertex, vertex_next, target);

		if (pt == vertex_next)
		{
			moveToNextVertex(dir);
			return false;
		} // if

		moveToNextVertex(dir, target);
		return true;
	} // _moveToNextVertex


	/**
     */

	//============================================================
	private boolean _moveAlongEdge(
			Point2 target)
	//============================================================
	{
		if (_moveToNextVertex(target))
			return true;

		int ori = _getOrientation(target, _vertex_right_hand, _vertex_left_hand);

		if (ori == Point2.ORIENTATION_LEFT)
			return true;

		if (ori == Point2.ORIENTATION_RIGHT)
			return false;

		// ori == ORIENTATION_COLLINEAR

		Point2 pt = _getInsidePoint(_vertex_right_hand, _vertex_left_hand,
				target);

		if (_moveToNextVertex(pt))
			return false;

		setCoors(target);
		return true;
	} // _moveAlongEdge


	/**
     */

	//============================================================
	private boolean _moveAlongObstacle(
			Point2 target)
	//============================================================
	{
		if (_vertex == null)
		{
			boolean finished = _moveAlongEdge(target);

			if (finished || _vertex == null)
				return finished;
		} // if

		while (true)
		{
			if (_moveToNextVertex(target))
				return true;

			int ori = _getOrientation(target, _vertex_right_hand, _vertex,
					_vertex_left_hand);

			if (ori == Point2.ORIENTATION_LEFT)
				return true;

			if (ori == Point2.ORIENTATION_RIGHT)
				return false;

			// ori == ORIENTATION_COLLINEAR

			double angle_left = _vertex.angle(target, _vertex_right_hand);
			if (angle_left > Math.PI)
				angle_left = 2 * Math.PI - angle_left;

			double angle_right = _vertex.angle(target, _vertex_left_hand);

			if (angle_right > Math.PI)
				angle_right = 2 * Math.PI - angle_right;

			if (angle_left < angle_right)
			{
				if (_moveToNextVertex(RIGHT_HAND, target))
					return true;
			}
			else
			{
				if (_moveToNextVertex(LEFT_HAND, target))
					return true;
			} // else
		} // while
	} // _moveAlongObstacle


	/**
	 * Robot in Richtung des Punktes target bewegen, bis der Punkt erreicht ist,
	 * oder der Robot an ein Hindernis stoesst.
	 */

	//============================================================
	public void move(
			Point2 target)
	//============================================================
	{
		if (target.x == getX() && target.y == getY())
			return; // Robot befindet sich bereits an der
					// gewuenschten Position

		if (_contact_poly != null)
		{
			// Robot befindet sich an einer Kante oder an einem
			// Eckpunkt des Polygons _contact_poly

			// Robot am Hindernis entlang in Richtung des Punktes
			// target bewegen.

			boolean finished = _moveAlongObstacle(target);

			if (finished)
				return;
		} // if

		Polygon2Scene scene = getScene();

		Point2 coors = getCoors();

		Segment2 seg = new Segment2(coors, target);

		Polygon2[] polygons = scene.getInteriorPolygons();
		Polygon2 bounding_poly = scene.getBoundingPolygon();

		_contact_poly = null;
		Segment2 edge_col = null;
		Point2 point_col = null;

		double min_sq_dist = Double.MAX_VALUE;

		Intersection set = new Intersection();

		for (int i = -1; i < polygons.length; i++)
		{
			Polygon2 polygon;

			if (i == -1)
				polygon = bounding_poly;
			else
				polygon = polygons[i];

			if (polygon == null)
				continue;

			Segment2[] edges = polygon.edges();

			for (int j = 0; j < edges.length; j++)
			{
				Segment2 edge = edges[j];

				if (_vertex == null)
				{
					if (edge.source() == _vertex_right_hand
							&& edge.target() == _vertex_left_hand)
						continue;

					if (edge.target() == _vertex_right_hand
							&& edge.source() == _vertex_left_hand)
						continue;
				}
				else
				{
					if (edge.source() == _vertex || edge.target() == _vertex)
						continue;
				} // else

				Point2 pt = null;

				if (edge.liesOn(target))
					pt = new Point2(target);
				else
				{
					seg.intersection(edge, set);

					if (set.result == Intersection.POINT2)
						pt = set.point2;
					else if (set.result == Intersection.SEGMENT2)
					{
						Point2 pt1 = set.segment2.source();
						Point2 pt2 = set.segment2.target();

						if (coors.squareDistance(pt1) < coors
								.squareDistance(pt2))
							pt = pt1;
						else
							pt = pt2;
					} // if
				}

				if (pt != null)
				{
					double sq_dist = coors.squareDistance(pt);
					if (sq_dist < min_sq_dist)
					{
						min_sq_dist = sq_dist;
						point_col = pt;
						edge_col = edge;
						_contact_poly = polygon;
					} // if
				} // if
			} // for
		} // for

		if (point_col != null) // Kollision mit einem Polygon
		{
			if (point_col.equals(edge_col.source())) // Kollision mit
				// Vertex edge_col.source()
				_setToVertex(_contact_poly, edge_col.source(), coors);
			else if (point_col.equals(edge_col.target())) // Kollision mit
				// Vertex edge_col.target()
				_setToVertex(_contact_poly, edge_col.target(), coors);
			else
				// Kollision mit Kante edge_col
				_setToEdge(_contact_poly, edge_col, point_col, coors);
		}
		else
		{
			// Keine Kollision
			_vertex = null;
			_vertex_right_hand = null;
			_vertex_left_hand = null;

			setCoors(target);
		} // else

	} // move


	/**
     */

	//============================================================
	public double turn(
			double turn_angle)
	//============================================================
	{
		return 0.0;
	} // turn


	/**
	 * Testen, ob eine Bewegung in Richtung des Punktes point moeglich ist,
	 * genauer, ob sich die Position des Robots durch den Aufruf von move( point
	 * ) veraendern wuerde.
	 * 
	 * @return true <==> Bewegung ist moegich
	 */

	//============================================================
	public boolean isFreeToMove(
			Point2 point)
	//============================================================
	{
		if (point.x == getX() && point.y == getY())
			return false; // Robot befindet sich bereits am angegebenen
							// Punkt. Das heisst: move( point ) wuerde die
							// Position des Robots nicht veraendern.

		if (!isAtObstacle())
			return true; // Robot hat keinen Kontakt zu einem Hindernis.
							// Er hat also in alle Richtungen Bewegungs-
							// freiheit

		if (isAtVertex())
		{

			// Robot befindet sich am Eckpunkt _vertex des Polygons
			// _contact_poly

			if (_isOnLeftSide(point, _vertex_right_hand, _vertex,
					_vertex_left_hand))
				return false; // Der Punkt point ist fuer den Robot nicht
			// erreichbar, da er sich links vom Kantenzug
			// ( _vertex_right_hand -> _vertex -> _vertex_left_hand )
			// befindet.
		}
		else
		{
			// Robot befindet sich an der Kante
			// ( _vertex_right_hand, _vertex_left_hand ) des Polygons _contact_poly.

			if (_getOrientation(point, _vertex_right_hand, _vertex_left_hand) == Point2.ORIENTATION_LEFT)
				return false; // Der Punkt point ist fuer den Robot nicht
			// erreichbar, da er sich links von der Kante
			// ( _vertex_right_hand -> _vertex_left_hand ) befindet.
		} // else

		return true;
	} // isFreeToMove


	/**
     */

	//============================================================
	public void paint(
			Graphics2D g_world,
			Graphics g_screen)
	//============================================================
	{} // paint


	//************************************************************
	// Private Methoden
	//************************************************************

	/**
	 * Ermitteln des Nachbarn des Eckpunktes vertex, der nicht identisch ist mit
	 * dem Punkt neighbour.
	 * 
	 * Eine Ausnahme bildet der Fall, dass vertex ein Endpunkt eines offenen
	 * Polygons ist. In diesem Fall wird der Punkt neighbour zurueckgegeben.
	 */

	//============================================================
	private Point2 _getNeighbourVertex(
			Polygon2 poly,
			Point2 vertex,
			Point2 neighbour)
	//============================================================
	{
		if (poly.isOpen()
				&& (vertex == poly.firstPoint() || vertex == poly.lastPoint()))
			return neighbour;

		Polygon2Scene scene = getScene();

		Point2 pt1 = scene.getNeighbourVertex(vertex, LEFT_HAND);
		if (pt1 == neighbour)
			return scene.getNeighbourVertex(vertex, RIGHT_HAND);
		else
			return pt1;
	} // _getNeighbourVertex


	/**
	 * Setzen des Robots an den Eckpunkt eines Polygons
	 */

	//============================================================
	private void _setToVertex(
			Polygon2 polygon,
			Point2 vertex,
			Point2 freepoint)
	//============================================================
	{
		_contact_poly = polygon;
		_vertex = vertex;

		if (polygon.isClosed()) // Polygon ist geschlossen
		{
			SimpleList points = polygon.points();

			double angle_left = 0;
			double angle_right = 0;

			for (ListItem i = points.first(); i != null; i = i.next())
			{
				Point2 p1 = (Point2) i.value();
				Point2 p2 = (Point2) points.cyclicRelative(i, 1).value();
				Point2 p3 = (Point2) points.cyclicRelative(i, 2).value();

				if (p2 == _vertex)
				{
					_vertex_right_hand = p1;
					_vertex_left_hand = p3;
				} // if

				double phi = p2.angle(p3, p1);
				angle_left += phi;
				angle_right += 2 * Math.PI - phi;
			} // for

			boolean bounding = (getScene().getBoundingPolygon() == polygon);

			if ((!bounding && (angle_left > angle_right))
					|| (bounding && (angle_right > angle_left)))
			{
				Point2 save = _vertex_right_hand;
				_vertex_right_hand = _vertex_left_hand;
				_vertex_left_hand = save;
			} // if
		}
		else
		// Polygon ist offen
		{
			// Die beiden Eckpunkte in _vertex_right_hand und _vertex_left_hand
			// ermitteln, die mit _vertex durch Kante verbunden sind

			Segment2 edge_s = null;
			Segment2 edge_t = null;

			Segment2[] edges = polygon.edges();
			for (int i = 0; i < edges.length; i++)
			{
				Segment2 edge = edges[i];

				if (edge.source() == _vertex)
					edge_t = edge;
				if (edge.target() == _vertex)
					edge_s = edge;
			} // for

			if (edge_s == null)
			{
				_vertex_right_hand = edge_t.target();
				_vertex_left_hand = _vertex_right_hand;
			}
			else if (edge_t == null)
			{
				_vertex_right_hand = edge_s.source();
				_vertex_left_hand = _vertex_right_hand;
			}
			else
			{
				_vertex_right_hand = edge_s.source();
				_vertex_left_hand = edge_t.target();
			} // else

			if (_isOnLeftSide(freepoint, _vertex_right_hand, _vertex,
					_vertex_left_hand))
			{
				Point2 save = _vertex_right_hand;
				_vertex_right_hand = _vertex_left_hand;
				_vertex_left_hand = save;
			} // if
		} // else

		setCoors(_vertex);
	} // _setToVertex


	/**
	 * Setzen des Robots auf die Kante eines Polygons
	 */

	//============================================================
	private void _setToEdge(
			Polygon2 polygon,
			Segment2 edge,
			Point2 position,
			Point2 freepoint)
	//============================================================
	{
		_contact_poly = polygon;
		_vertex = null;
		_vertex_right_hand = edge.source();
		_vertex_left_hand = edge.target();

		if (polygon.isClosed()) // Polygon ist geschlossen
		{
			SimpleList points = polygon.points();

			double angle_left = 0;
			double angle_right = 0;

			for (ListItem i = points.first(); i != null; i = i.next())
			{
				Point2 p1 = (Point2) i.value();
				Point2 p2 = (Point2) points.cyclicRelative(i, 1).value();
				Point2 p3 = (Point2) points.cyclicRelative(i, 2).value();

				if ((p1 == _vertex_left_hand) && (p2 == _vertex_right_hand))
				{
					_vertex_right_hand = edge.target();
					_vertex_left_hand = edge.source();
				} // if

				double phi = p2.angle(p3, p1);
				angle_left += phi;
				angle_right += 2 * Math.PI - phi;
			} // for

			boolean bounding = (getScene().getBoundingPolygon() == polygon);

			if ((!bounding && (angle_left > angle_right))
					|| (bounding && (angle_right > angle_left)))
			{
				Point2 save = _vertex_right_hand;
				_vertex_right_hand = _vertex_left_hand;
				_vertex_left_hand = save;
			} // if
		}
		else
		// Polygon ist offen
		{
			_vertex_right_hand = edge.source();
			_vertex_left_hand = edge.target();

			if (freepoint.orientation(_vertex_right_hand, _vertex_left_hand) == Point2.ORIENTATION_LEFT)
			{
				_vertex_right_hand = edge.target();
				_vertex_left_hand = edge.source();
			} // if
		} // else

		setCoors(position);
	} // _setToEdge


	/**
     */

	//============================================================
	private static int _getOrientation(
			Point2 pt1,
			Point2 pt2,
			Point2 pt3)
	//============================================================
	{
		int ori_1 = pt1.orientation(pt2, pt3);
		int ori_2 = pt2.orientation(pt3, pt1);
		int ori_3 = pt3.orientation(pt1, pt2);

		if (ori_1 == Point2.ORIENTATION_LEFT
				&& ori_2 == Point2.ORIENTATION_LEFT
				&& ori_3 == Point2.ORIENTATION_LEFT)
		{
			return Point2.ORIENTATION_LEFT;
		} // if

		if (ori_1 == Point2.ORIENTATION_RIGHT
				&& ori_2 == Point2.ORIENTATION_RIGHT
				&& ori_3 == Point2.ORIENTATION_RIGHT)
		{
			return Point2.ORIENTATION_RIGHT;
		} // if

		return Point2.ORIENTATION_COLLINEAR;
	} // _getOrientation


	/**
     */

	//============================================================
	private static int _getOrientation(
			Point2 pt,
			Point2 pt1,
			Point2 pt2,
			Point2 pt3)
	//============================================================
	{
		int ori_1 = _getOrientation(pt, pt1, pt2);
		int ori_2 = _getOrientation(pt, pt2, pt3);

		double angle = pt2.angle(pt3, pt1);

		boolean left_1 = (ori_1 == Point2.ORIENTATION_LEFT);
		boolean left_2 = (ori_2 == Point2.ORIENTATION_LEFT);

		if ((left_1 && left_2) || (angle > Math.PI && (left_1 || left_2)))
			return Point2.ORIENTATION_LEFT;

		boolean right_1 = (ori_1 == Point2.ORIENTATION_RIGHT);
		boolean right_2 = (ori_2 == Point2.ORIENTATION_RIGHT);

		if ((right_1 && right_2) || (angle < Math.PI && (right_1 || right_2)))
			return Point2.ORIENTATION_RIGHT;

		return Point2.ORIENTATION_COLLINEAR;
	} // _getOrientation


	/**
     */

	//============================================================
	private static Point2 _getInsidePoint(
			Point2 pt1,
			Point2 pt2,
			Point2 pt3)
	//============================================================
	{
		double dist_1_2 = pt1.squareDistance(pt2);
		double dist_2_3 = pt2.squareDistance(pt3);
		double dist_3_1 = pt3.squareDistance(pt1);

		if (dist_2_3 > dist_1_2 && dist_2_3 > dist_3_1)
			return pt1;

		if (dist_3_1 > dist_2_3 && dist_3_1 > dist_1_2)
			return pt2;

		return pt3;
	} // _getInsidePoint


	/**
	 * Testen ob sich der Punkt point links vom Kantenzug ( vertex1, vertex2,
	 * vertex3 ) befindet.
	 */

	//============================================================
	private static boolean _isOnLeftSide(
			Point2 point,
			Point2 vertex1,
			Point2 vertex2,
			Point2 vertex3)
	//============================================================
	{
		boolean left_1_2 = (_getOrientation(point, vertex1, vertex2) == Point2.ORIENTATION_LEFT);

		boolean left_2_3 = (_getOrientation(point, vertex2, vertex3) == Point2.ORIENTATION_LEFT);

		double angle = vertex2.angle(vertex3, vertex1);

		return ((left_1_2 && left_2_3) || (angle > Math.PI && (left_1_2 || left_2_3)));
	} // _isOnLeftSide

} // ScenePosition
