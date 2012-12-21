package anja.geom;


import anja.util.List;
import anja.util.ListException;
import anja.util.ListItem;
import anja.util.SimpleList;


/**
 * Diese Klasse dient zur Berechnung des Sichtbarkeitspolygons mittels des
 * Algorithmus aus 'Rolf Klein. Algorithmische Geometrie. KE 4. Hagen 1996' bzw.
 * 'B.Joe. & R. B. Simpson Corrections to Lee's Visibility Polygon Algorithm.
 * BIT(27), 1987.'
 */
public class visPolygon
		extends Object
		implements java.io.Serializable
{

	private Polygon2			_polygon			= null; // verknuepftes Polygon
	private SimpleList			_todo				= null, _done = null;	// besuchende und bereits besuchte Segmente
	private Segment2			_akt, _next;								// Das aktuelle Segment und das naechste
	private Point2				_p					= null, _r0 = null;	// Der Sichtpunkt und der Startpunkt
	private Point2				_r					= null, _rl = null;	// Der aktuelle und letzte Punkt
	private byte				_vis				= 0;					// Sichtbarkeitszustand des aktuellen Segmentes
	private byte				_lvis				= 0;					// Sichtbarkeitszustand des letzten Segmentes
	private boolean				_cutOnr0			= false;				// merkt sich ob Polygon an r0 auseinandergesaegt wurde

	//Sichtbarkeitskonstanten
	private static final byte	_VISIBLE			= 0;
	private static final byte	_UNVISIBLE			= 1;
	private static final byte	_START_COVERED		= 3;
	private static final byte	_END_COVERED		= 4;
	private static final byte	_STARTEND_COVERED	= 5;
	private static final byte	_IN_LINE_VISIBLE	= 6;
	private static final byte	_IN_LINE_UNVISIBLE	= 7;
	private static final byte	_ENTER_SHADOW		= 8;
	private static final byte	_ON_LINE			= 9;


	/**
	 * Konstruktor. Verknuepft das visPolygon mit dem Polygon2 pol.
	 */
	visPolygon(
			Polygon2 pol)
	{
		_polygon = pol;
		_todo = new List();
		_done = new List();
	}


	/**
	 * Erzeugt ein neues Polygon aus der Eingabeliste. In dieser duerfen Punkte
	 * und Segment (und Strahlen) enthalten sein.
	 * 
	 * @param L
	 *            Liste mit Punkten und/oder Segmenten
	 * 
	 * @return Das Polygon
	 */
	public static Polygon2 createPolygon(
			SimpleList L)
	{
		Polygon2 new_poly = new Polygon2();
		ListItem i = L.first();
		Object O;
		Point2 pl = null, p1, p2;
		// Letzten Punkt ermitteln...
		i = L.last();
		while ((i != null) && (pl == null))
		{
			O = i.value();
			if (O instanceof Point2)
				pl = (Point2) O;
			if (O instanceof Segment2)
				pl = ((Segment2) O).target();
			if (O instanceof Ray2)
				pl = ((Ray2) O).source();
			i = i.prev();
		}
		if (i == null)
		{ // Sollte nicht der Regelfall sein...
			if (pl != null)
				new_poly.addPoint(pl);
			return new_poly;
		}
		i = L.first();
		while (i != null)
		{
			O = i.value();
			if (O != null)
			{
				if (O instanceof Point2)
				{
					p1 = (Point2) O;
					if ((pl != null) && (!pl.equals(p1)))
						new_poly.addPoint(p1);
				}
				if (O instanceof Segment2)
				{
					p1 = ((Segment2) O).source();
					p2 = ((Segment2) O).target();
					if ((p1 != null) && (p2 != null))
					{
						if (!pl.equals(p1))
							new_poly.addPoint(p1);
						if (!p1.equals(p2))
							new_poly.addPoint(p2);
					}
				}
				if (O instanceof Ray2)
				{
					p1 = ((Ray2) O).source();
					if ((p1 != null) && (!pl.equals(p1)))
						new_poly.addPoint(p1);
				}
			}
			if (new_poly.length() > 0)
				pl = new_poly.lastPoint();
			i = i.next();
		}
		return new_poly;
	}


	/**
	 * Initialisiert die todo und done-Listen. Der sichtbare Anfangspunkt r0 auf
	 * dem Polygonrand muss vorher bestimmt worden sein.
	 */
	private void initializeToDo()
	{
		// Initialisiert die ToDo-Liste
		_cutOnr0 = false;
		if ((_polygon == null) || (_r0 == null))
			return;
		_todo.clear();
		_done.clear();
		Segment2 s = null;
		SimpleList L = _polygon.points();
		ListItem i = L.first(), j = null, k;
		Point2 p, pl = (Point2) L.lastValue();
		while (i != null)
		{
			p = (Point2) i.value();
			s = new Segment2(pl, p);
			k = _todo.Push(s);
			if ((s.liesOn(_r0)) && (!_r0.equals(s.target())))
				j = k;
			i = i.next();
			pl = p;
		}
		if (j == null)
			return; // Sollte eigentlich nicht passieren...
		_todo.cycle(_todo.cyclicRelative(j, 1)); // Liste so drehen, dass das Segment mit r0 am Ende steht...
		// Jetzt muss noch des Segment, auf dem r0 liegt geteilt werden...
		s = (Segment2) j.value();
		if (_r0.equals(s.source()))
			return; // in diesem Fall ist alles schon fertig
		_todo.remove(j);
		_todo.Push(new Segment2(s.source(), _r0)); // ersten Segmentteil ans Ende
		_todo.push(new Segment2(_r0, s.target())); // zweiten Segmentteil an den Anfang
		_cutOnr0 = true;
	}


	/**
	 * Sucht einen von p aus sichtbaren Punkt auf dem Polygonrand. Hier gehen
	 * wir uebrigens davon aus, das intersection des Polygons eine Punktliste
	 * oder einen einzelnen Punkt liefert und nix anderes...
	 * 
	 * @return Ein sichtbarer Punkt
	 */
	private Point2 getFirstBorderPoint()
	{
		Ray2 ray = new Ray2((Point2) _p.clone(), 0, Ray2.LEFT);
		Intersection inter = new Intersection();
		_polygon.intersection(ray, inter);
		if (inter.result == Intersection.EMPTY)
			return null; // ???
		if (inter.result == Intersection.POINT2)
			return inter.point2; // Nur ein Schnittpunkt
		SimpleList L;
		if (inter.result == Intersection.SEGMENT2)
		{
			L = new SimpleList();
			L.push(inter.segment2);
		}
		else
			L = inter.list;
		// Zuerst machen wir eine reine Punktliste daraus...
		ListItem i = null, j = null;
		i = L.first();
		while (i != null)
		{
			if (i.value() instanceof Segment2)
			{
				j = i.next();
				L.remove(i);
				L.push(((Segment2) i.value()).source());
				L.push(((Segment2) i.value()).target());
				i = j;
			}
			else
				i = i.next();
		}
		// ab hier der kompliziertere Fall...
		PointComparitor pc = new PointComparitor();
		pc.setOrder(PointComparitor.X_ORDER);
		SimpleList polyPoints = _polygon.points();
		//Point2 p;
		boolean schlecht = true;
		while ((!L.empty()) && (schlecht))
		{
			schlecht = false;
			i = L.max(pc); // Der Punkt, der am naechsten an p liegt, also der rechteste!
			j = polyPoints.first();
			while ((j != null)
					&& (!((Point2) j.value()).equals((Point2) i.value())))
			{
				j = j.next();
			}
			if (j != null)
			{
				if (_p.orientation((Point2) j.value(), (Point2) polyPoints
						.cyclicRelative(j, 1).value()) != Point2.ORIENTATION_LEFT)
				{
					L.remove(i);
					schlecht = true;
				}
			}
		}
		if ((i != null) && (!schlecht))
			return (Point2) i.value();
		else
			return null;
	}


	/**
	 * Testet, ob der Punkt q echt im Dreieck tria(a,b,c) liegt.
	 * 
	 * @param a
	 *            Punkt a des Dreiecks
	 * @param b
	 *            Punkt b des Dreiecks
	 * @param c
	 *            Punkt c des Dreiecks
	 * @param q
	 *            Der zu testende Punkt
	 * 
	 * @return true wenn q im Dreieck liegt, false sonst
	 */
	private boolean triaCheck(
			Point2 a,
			Point2 b,
			Point2 c,
			Point2 q)
	{
		return ((q.orientation(a, b) == Point2.ORIENTATION_LEFT)
				&& (q.orientation(b, c) == Point2.ORIENTATION_LEFT) && (q
				.orientation(c, a) == Point2.ORIENTATION_LEFT));
	}


	/**
	 * Ueberprueft die Sichtbarkeit des aktuellen Segments und setzt vis
	 * entsprechend.
	 */
	private void checkVisibility()
	{
		if ((_akt == null) || (_r == null) || (_rl == null))
			return;
		_lvis = _vis;
		_vis = _VISIBLE;
		if (_p.orientation(_rl, _r) == Point2.ORIENTATION_RIGHT)
			_vis = _UNVISIBLE;
		if (_p.orientation(_rl, _r) == Point2.ORIENTATION_COLLINEAR)
		{
			if (_akt.liesOn(_p))
				_vis = _ON_LINE;
			else
			{
				if ((_lvis == _IN_LINE_UNVISIBLE) || (_lvis == _UNVISIBLE))
					_vis = _IN_LINE_UNVISIBLE;
				else
					_vis = _IN_LINE_VISIBLE;
			}
		}
		if ((_vis == _VISIBLE) && (!_done.empty()))
		{
			//Segment2 v=new Segment2(_p,_rl),w=new Segment2(_p,_r);
			Segment2 l = (Segment2) _done.lastValue(), f = (Segment2) _done
					.firstValue();
			// Check auf einseitige Ueberdeckungen
			if (triaCheck(_p, _rl, _r, l.target()))
				_vis = _START_COVERED;
			if ((triaCheck(_p, _rl, _r, f.source()))
					&& (f.target().orientation(_p, _r) == Point2.ORIENTATION_LEFT))
			{
				if (_vis == _START_COVERED)
					_vis = _STARTEND_COVERED;
				else
					_vis = _END_COVERED;
			}
		}
	}


	/**
	 * Beschneidet das aktuelle Segment, falls notwendig.
	 */
	private void trimAkt()
	{
		if ((_vis == _STARTEND_COVERED) || (_vis == _START_COVERED)
				|| (_vis == _END_COVERED))
		{
			Point2 sp = ((Segment2) _done.lastValue()).target();
			Point2 ep = ((Segment2) _done.firstValue()).source();
			Ray2 s = null;
			Intersection inter = new Intersection();
			if ((_vis == _START_COVERED) || (_vis == _STARTEND_COVERED))
			{
				s = new Ray2(_p, sp);
				_akt.intersection(s, inter);
				if ((inter.result == Intersection.POINT2)
						&& (!inter.point2.equals(_akt.target())))
					_akt = new Segment2(inter.point2, _akt.target());
			}
			if ((_vis == _END_COVERED) || (_vis == _STARTEND_COVERED))
			{
				s = new Ray2(_p, ep);
				_akt.intersection(s, inter);
				if ((inter.result == Intersection.POINT2)
						&& (!inter.point2.equals(_akt.source())))
					_akt = new Segment2(_akt.source(), inter.point2);
			}
		}
	}


	/**
	 * Der schwierigste Teil.
	 */
	private void shadow()
	{
		boolean ra = false, rb = false;
		Ray2 t = new Ray2(_p, _r);
		Segment2 s = null, ss, sa = null;
		Intersection inter = new Intersection();
		while ((!_done.empty()) && (!ra) && (!rb))
		{
			sa = s;
			s = (Segment2) _done.pop();
			//System.out.println("sa,s:"+sa+":"+s);
			if ((s != null) && (sa != null)
					&& (!s.target().equals(sa.source())))
			{
				// Falls die Segmente nicht zusammenhaengend sind, so muessen wir auch die
				// Verbindungssegmente beachten...
				ss = new Segment2(s.target(), sa.source()); // die Endpunkte liegen in einer Flucht mit p!

				_done.push(s); // erst beim naechsten Mal wird s behandelt!!!
				s = ss;
				ss.intersection(_akt, inter);
				// rb ist wahr, wenn ein Verbindungssegment das aktuelle Segment schneidet
				rb = (inter.result == Intersection.POINT2);
			}
			else
			{
				s.intersection(t, inter);
				// ra ist wahr, wenn der Strahl t ein Segment in done schneidet
				ra = (inter.result == Intersection.POINT2);
			}
		}
		if (ra)
		{
			// Dieser Fall ist einfach. Wir muessen nur noch das restliche Stueck des Segments
			// wieder auf done tun.
			//if (!inter.point2.equals(s.source()))
			_done.push(new Segment2(s.source(), inter.point2));
		}
		if (rb)
		{
			// Hier stimmt done schon, aber wir muessen soweit vorlaufen, bis wir wieder p sehen
			// koennen, denn RAkt wird vom letzten Segment auf done verdeckt.
			// es ist notwendig, das Segment von q bis r auf todo zu legen, auch wennn es 
			// unsichtbar ist. Nur so funktioniert handleEnterShadow() richtig!!!
			//if (!inter.point2.equals(_r)) _todo.push(new Segment2(inter.point2,_r));
			_todo.push(_akt);
			_vis = _ENTER_SHADOW;
		}
	}


	/**
	 * Aktualisiert die Done-Liste, indem das aktuelle Segment hinzugefuegt
	 * wird, bzw. verdeckte Segmente entfernt werden.
	 */
	private void updateDone()
	{
		switch (_vis)
		{
			case _VISIBLE:
			case _ON_LINE:
			case _START_COVERED:
			case _END_COVERED:
			case _STARTEND_COVERED:
				// das aktuelle Segment ist sichtbar (bis jetzt)...
				//if (!_akt.source().equals(_akt.target()))
				_done.push(_akt);
				break;
			case _UNVISIBLE:
				// das aktuelle Segment verdeckt andere, bis jetzt sichtbare Segment,
				// die schon in done sind. die muessen wieder entfernt werden.
				shadow();
				break;
			case _IN_LINE_VISIBLE:
			case _IN_LINE_UNVISIBLE:
				// wir machen nichts...
				break;
		}
	}


	/**
	 * ENTER_SHADOW wird erst erkannt, nachdem wir schon andere Ereignisse
	 * bearbeitet haben. Deshalb ist Akt eventuell ein ganz anderes Segment. Wir
	 * gehen davon aus, dass das letzte Segment in todo einen Schnitt mit dem
	 * Strahl t hat und dass dieses Segment den Strahl von links schneidet!
	 */
	private void handleEnterShadow()
	{
		//System.out.println(_vis);
		if ((_vis == _ENTER_SHADOW) && (!_todo.empty()) && (!_done.empty()))
		{
			Segment2 s = (Segment2) _done.lastValue();
			Ray2 t = new Ray2(_p, s.target());
			s = (Segment2) _todo.pop();
			Intersection inter = new Intersection();
			s.intersection(t, inter);
			if (inter.result != Intersection.POINT2)
				return; // irgendetwas ist schief gelaufen...
			if (inter.point2.equals(s.target()))
				s = (Segment2) _todo.pop();
			if (s != null)
				s = (Segment2) _todo.pop(); // Das Segment mit dem Schnitt kann's nicht sein...
			Segment2 g = new Segment2(_p, inter.point2);
			g.intersection(s, inter);
			while ((s != null) && (inter.result != Intersection.POINT2))
			{
				s = (Segment2) _todo.pop();
				g.intersection(s, inter);
			}
			//System.out.println(""+inter.point2+"-"+s.target());
			//if ((s!=null) && (!inter.point2.equals(s.target()))) _todo.push(new Segment2(inter.point2,s.target()));
			if (s != null)
				_todo.push(new Segment2(inter.point2, s.target()));
			//System.out.println(_todo);
		}
	}


	/**
	 * Testmethode
	 * 
	 * @return true oder false, je nach Situation
	 */
	private boolean testRightTurnOutside()
	{
		if ((_next == null) || (_vis == _IN_LINE_UNVISIBLE)
				|| (_vis == _UNVISIBLE))
			return false;
		Point2 rn = _next.target();
		// Einen RightTurnOutside gibt's auch, wenn p,rl,r auf einer Linie liegen!!
		boolean ro = ((_rl.orientation(_p, _r) != Point2.ORIENTATION_LEFT) && (rn
				.orientation(_p, _r) == Point2.ORIENTATION_RIGHT));
		ro = ro
				&& ((_p.orientation(_rl, _r) != Point2.ORIENTATION_RIGHT) && (rn
						.orientation(_rl, _r) == Point2.ORIENTATION_RIGHT));
		return ro;
	}


	/**
	 * Behandelt die entsprechende Situation handleRightTurnOutside
	 */
	private void handleRightTurnOutside()
	{
		if (_todo.empty())
			return;
		Ray2 t = new Ray2(_p, _r);
		Segment2 s = (Segment2) _todo.pop(); // das erste Segment wird 'eh weggeschmissen
		Intersection inter = new Intersection(); // inter.result=Intersection.EMPTY;
		while ((s != null) && (inter.result != Intersection.POINT2))
		{
			s = (Segment2) _todo.pop();
			if (s != null)
			{
				s.intersection(t, inter);
				if ((inter.result == Intersection.POINT2)
						&& (inter.point2.equals(s.target()))
						&& (_todo.length() != 0))
				{
					Segment2 ss = (Segment2) _todo.peek();
					if (s.source().orientation(_p, s.target()) == ss.target()
							.orientation(_p, s.target()))
					{
						// In dem Fall beruehren die Segmente den Strahl nur und wir zaehlen es nicht als Schnittpunkt
						_todo.pop();
						inter.result = Intersection.EMPTY;
					}
				}
			}
		}
		if (s != null)
			if (_p.orientation(s.source(), s.target()) == Point2.ORIENTATION_LEFT)
			{
				// Segment kommt mit der richtigen Seite zum Vorschein!
				// Also muss s nur noch gekuerzt werden...
				if (!inter.point2.equals(s.target()))
					_todo.push(new Segment2(inter.point2, s.target()));
			}
			else
			{
				// Segment kommt mit der falschen Seite zum Vorschein! 2 Faelle:
				if (_p.squareDistance(inter.point2) < _p.squareDistance(_r))
				{
					// Segment wirft beim naechsten Mal Schatten...
					if (!inter.point2.equals(s.target()))
						_todo.push(new Segment2(inter.point2, s.target()));
				}
				else
				{
					// Segment betritt den Schatten
					_todo.push(s);
					_vis = _ENTER_SHADOW;
				}
			}
	}


	/**
	 * Testmethode
	 * 
	 * @return true oder false, je nach Situation
	 */
	private boolean testLeftTurnOutside()
	{
		if ((_vis == _ON_LINE) || (_vis == _ENTER_SHADOW) || (_vis == _VISIBLE)
				|| (_vis == _IN_LINE_VISIBLE) || (_next == null))
			return false;
		Point2 rn = _next.target();
		// Einen LeftTurnOutside gibt's auch, wenn p,rl,r auf einer Linie liegen!!
		boolean ro = ((_rl.orientation(_p, _r) != Point2.ORIENTATION_RIGHT) && (rn
				.orientation(_p, _r) == Point2.ORIENTATION_LEFT));
		ro = ro
				&& ((_p.orientation(_rl, _r) != Point2.ORIENTATION_LEFT) && (rn
						.orientation(_rl, _r) == Point2.ORIENTATION_LEFT));
		return ro;
	}


	/**
	 * Behandelt die entsprechende Situation handleLeftTurnOutside
	 */
	private void handleLeftTurnOutside()
	{
		Ray2 t = new Ray2(_p, _r);
		Segment2 s;
		try
		{
			s = (Segment2) _todo.pop(); // dieses Segment kann keinen Schnitt mit dem Strahl t haben!
		}
		catch (ListException e)
		{
			s = null;
		}
		Intersection inter = new Intersection(); // inter.result=Intersection.EMPTY;
		while ((s != null) && (inter.result != Intersection.POINT2))
		{
			try
			{
				s = (Segment2) _todo.pop();
			}
			catch (ListException e)
			{
				s = null;
			}
			if (s != null)
				s.intersection(t, inter);
		}
		if ((s != null) && (!inter.point2.equals(s.target())))
		{
			_todo.push(new Segment2(inter.point2, s.target()));
		}
	}


	/**
	 * Behandelt die Situation handleCovering
	 */
	private void handleCovering()
	{
		if ((_vis == _END_COVERED) || (_vis == _STARTEND_COVERED))
		{
			Ray2 t = new Ray2(_p, ((Segment2) _done.firstValue()).source());
			Segment2 s;
			Intersection inter = new Intersection();
			_akt.intersection(t, inter);
			if (inter.result != Intersection.POINT2)
				return; // Dieser Fall sollte eigentlich nicht eintreten
			Segment2 l = new Segment2(((Segment2) _done.firstValue()).source(),
					inter.point2);
			// Wir suchen nun nach dem Segment, das ZWISCHEN dem ersten und dem aktuellen Segment
			// zum Vorschein kommt...
			boolean schnitt = false;
			do
			{
				try
				{
					s = (Segment2) _todo.pop();
				}
				catch (ListException e)
				{
					s = null;
				}
				if (s == null)
					break;
				schnitt = (l.intersection(s, inter) != null);
				// In diesem Fall schneidet das Segment l nicht richtig, sondern beruehrt es nur.
				if ((schnitt)
						&& (s.target().orientation(_p, inter.point2) != Point2.ORIENTATION_RIGHT))
					schnitt = false;
			}
			while ((s != null) && (!schnitt));
			if (s != null)
				_todo.push(new Segment2(inter.point2, s.target()));
		}
	}


	/**
	 * Sichtbarkeit des Punktes
	 * 
	 * @param q
	 *            Der Punkt
	 * 
	 * @return Das Ergebnispolygon
	 */
	public Polygon2 vis(
			Point2 q)
	{
		if ((_polygon == null) || (_polygon.length() < 3)
				|| (_polygon.isOpen()) || (q == null))
			return null;
		boolean polyWasRight = (_polygon.getOrientation() == Polygon2.ORIENTATION_RIGHT); //Um später Ursprungsorientierung zurückzugeben
		_polygon.setOrientation(Polygon2.ORIENTATION_LEFT); // Polygon muss entgegen dem Uhrzeigersinn orientiert sein!
		_p = q; // p setzen
		byte p_loc = _polygon.locatePoint(_p); // p lokalisieren
		if (p_loc == Polygon2.POINT_OUTSIDE)
			return null; // wenn p ausserhalb, ist nichts zu machen...
		if (p_loc == Polygon2.POINT_ON_EDGE)
		{
			_r0 = new Point2(_p); // in dem Fall muss r0 nicht berechnet werden
		}
		else
		{
			_r0 = getFirstBorderPoint(); // r0 auf Randpunkt setzen
		}
		initializeToDo(); // ToDo und Done-Listen initialisieren
		while (!_todo.empty())
		{
			// Variablen fuer Berechnungen initialisieren
			_akt = (Segment2) _todo.pop();
			if (!_todo.empty())
				_next = (Segment2) _todo.lastValue();
			else
				_next = null;
			_r = _akt.target();
			_rl = _akt.source();
			// Die Sichtbarkeit des aktuellen Segmentes testen und vis entsprechend setzen
			checkVisibility();
			// Das aktuelle Segment gegebenenfalls modifizieren
			trimAkt();
			// Das aktuelle Segment in done speichern, bzw. verdeckte Teile aus done entfernen
			updateDone();
			// Den aktuellen Punkt entsprechend der naechsten Ecke vorruecken
			if (testRightTurnOutside())
			{
				handleRightTurnOutside();
			}
			if (testLeftTurnOutside())
			{
				handleLeftTurnOutside();
			}
			handleCovering();
			handleEnterShadow(); // dies muss NACH handleRightTurnOutside() stehen!!!
		}
		if (_done.length() < 2)
			return visPolygon.createPolygon(_done);
		// Das moeglicherweise auseinandergesaegte Anfangssegment sollte wieder
		// zusammengeschweisst werden...
		Segment2 s = (Segment2) _done.firstValue(), e = (Segment2) _done
				.lastValue();
		if ((s != e) && (_cutOnr0) && (s.source().equals(e.target())))
		{
			_done.Pop();
			_done.pop(); // erstes und letztes Segment entfernen...
			_done.push(new Segment2(e.source(), s.target()));
		}
		if (polyWasRight)
			_polygon.setOrientation(Polygon2.ORIENTATION_RIGHT);
		return visPolygon.createPolygon(_done);
	}
}
