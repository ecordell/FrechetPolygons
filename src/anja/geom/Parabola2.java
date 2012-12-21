package anja.geom;


import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.util.Enumeration;
import java.util.Vector;

import anja.util.Drawable;
import anja.util.GraphicsContext;
import anja.util.List;
import anja.util.Matrix33;
import anja.util.SimpleList;


/**
 * 2-dimensionales zeichenbares Parabelstueck
 * 
 * @author Daniel Schumacher
 * @version 1.2 02.11.2005
 */
public class Parabola2
		extends QuadCurve2D.Double
		implements Drawable
{

	/**
	 * Epsilon-Umgebung fuer Schnittberechnung
	 */
	public static final double	_EPSILON	= 5.0E-3;

	/**
	 * Der Startpunkt des Parabelstuecks
	 */
	private Point2				_p1;

	/**
	 * Der Endpunkt des Parabelstuecks
	 */
	private Point2				_p2;

	/**
	 * Der Kontrollpunkt des Parabelstuecks
	 */
	private Point2				_ctrl;

	/**
	 * Leitlinie : a --- b
	 */
	private Point2				_a;

	private Point2				_b;

	/**
	 * Brennpunkt
	 */
	private Point2				_f;

	/**
	 * Parameter = Abstand Brennpunkt f <--> Lotpunkt von f auf Leitlinie l
	 * (doppelt so gross wie das p im englischen Wikipedia
	 */
	private double				_p;

	/**
	 * Normierter Richtungsvektor der Parabelachse
	 */
	private Point2				_v;


	/**
	 * Konstruktor
	 * 
	 * @param l
	 *            Leitlinie
	 * @param f
	 *            Brennpunkt
	 */
	public Parabola2(
			Segment2 l,
			Point2 f)
	{
		// Endpunkte des Segments _l
		_a = new Point2(l.source());
		_b = new Point2(l.target());

		_f = new Point2(f);

		_set_axis_vector();
		_set_curve_parameters();
	}


	/**
	 * Erzeugt eine Kopie der Parabel <c>parabola</c>
	 * 
	 * @param parabola
	 *            Das zu kopierende Objekt
	 */
	public Parabola2(
			Parabola2 parabola)
	{
		this._a = new Point2(parabola._a);
		this._b = new Point2(parabola._b);
		this._f = new Point2(parabola._f);
		_set_axis_vector();
		_set_curve_parameters();
	}


	/**
	 * Tauscht einen der Endpunkte der Parabel (p1 oder p2) durch einen neuen
	 * Punkt. Die Leitlinie, der Brennpunkt, der Parameter und die Parabelachse
	 * ändern sich dadurch nicht. Der Kontrollpunkt muss allerdings neu gesetzt
	 * werden
	 * 
	 * @param one
	 *            der auszutauschende Punkt
	 * @param other
	 *            der neue Punkt
	 */
	public void changepoint(
			Point2 one,
			Point2 other)
	{
		// Der Lotpunkr von other auf l
		Point2 p = new Line2(_a, _b).plumb(other);
		if (_p1.distance(one) < _p2.distance(one))
		{
			// _p1 wird ersetzt
			_a = p;
			_set_curve_parameters();
		}// IF
		else
		{
			// _p2 wird ersetzt
			_b = p;
			_set_curve_parameters();
		}// ELSE
	}


	/**
	 * Startpunkt ändern
	 * 
	 * @param other
	 *            Neuer Startpunkt
	 */
	public void change_start_point(
			Point2 other)
	{
		// Der Lotpunkr von other auf l
		Point2 p = new Line2(_a, _b).plumb(other);
		_a = p;
		_set_curve_parameters();
	}


	/**
	 * Endpunkt ändern
	 * 
	 * @param other
	 *            Neuer Endpunkt
	 */
	public void change_end_point(
			Point2 other)
	{
		// Der Lotpunkr von other auf l
		Point2 p = new Line2(_a, _b).plumb(other);
		_b = p;
		_set_curve_parameters();
	}


	// /**
	// * Tauscht den linken Endpunkt der Parabel. Wenn beide Punkte gleiche
	// * x-Koordinate haben, dann den unteren Punkt
	// *
	// * @param other
	// */
	// public void change_leftpoint(Point2 other) {
	// // Der Lotpunkr von other auf l
	// Point2 p = new Line2(_a, _b).plumb(other);
	// if (_p1.x < _p2.x) {
	// _a = p;
	// _set_curve_parameters();
	// } else if (_p2.x < _p1.x) {
	// _b = p;
	// } else {
	// if (_p1.y < _p2.y) {
	// _a = p;
	// _set_curve_parameters();
	// } else {
	// _b = p;
	// _set_curve_parameters();
	// }
	// }
	// }

	// /**
	// * Tauscht den rechten Endpunkt der Parabel. Wenn beide Punkte gleiche
	// * x-Koordinate haben, dann den oberen Punkt
	// *
	// * @param other
	// */
	// public void change_rightpoint(Point2 other) {
	// // Der Lotpunkr von other auf l
	// Point2 p = new Line2(_a, _b).plumb(other);
	// if (_p2.x < _p1.x) {
	// _a = p;
	// _set_curve_parameters();
	// } else if (_p1.x < _p2.x) {
	// _b = p;
	// } else {
	// if (_p2.y < _p1.y) {
	// _a = p;
	// _set_curve_parameters();
	// } else {
	// _b = p;
	// _set_curve_parameters();
	// }
	// }
	//
	// }

	/**
	 * Transformiert das Parabelstueck gemäß der Transformationsmatrix.
	 * 
	 * @param m
	 *            Die Transformationsmatrix
	 */
	public void transform(
			Matrix33 m)
	{
		_a.transform(m);
		_b.transform(m);
		_f.transform(m);
		_set_axis_vector();
		_set_curve_parameters();
	}


	/**
	 * Liefert eine transformierte Kopie der Parabel
	 * 
	 * @param m
	 *            Die Transformationsmatrix
	 * 
	 * @return Die transformierte Parabel
	 */
	public Parabola2 transformed(
			Matrix33 m)
	{
		Parabola2 result = new Parabola2(this);
		result.transform(m);
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * 
	 */
	/**
	 * Zeichnet das Objekt. Vererbt von {@link anja.util.Drawable}
	 * 
	 * @param g
	 *            Das Graphics-Objekt, in das gezeichnet wird
	 * @param gc
	 *            Der GrpahicsContext
	 * 
	 * @see anja.util.Drawable#draw(java.awt.Graphics2D,
	 *      anja.util.GraphicsContext)
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		// save old rendering attributes
		Stroke s = g.getStroke();

		g.setColor(gc.getForegroundColor());
		g.setStroke(gc.getStroke());

		g.draw(this);
		g.setStroke(s);
	}


	/**
	 * Berechnet den oder die Schnittpunkte mit einer Gerade. Wenn zwei
	 * Schnittpunkte existieren, wird eine Liste mit diesen Punkten
	 * zurückgegeben.
	 * 
	 * @param g
	 *            Die Gerade
	 * @param intersection
	 *            Das Intersection-Objekt, in dem die Schnittpunkte gespeichert
	 *            werden (reference)
	 * 
	 * @return Bei Punktschnitt wird dieser zurückgegeben
	 */
	public Point2 intersection(
			BasicLine2 g,
			Intersection intersection)
	{
		g = new Line2(g._source, g.target());

		// Normierter Vektor von _a nach _b;
		Point2 w = new Point2(_b.x - _a.x, _b.y - _a.y);
		double lenght = w.distance();
		w.moveTo(w.x / lenght, w.y / lenght);

		// Der Lotpunkt von f auf l
		Line2 l = new Line2(_a, _b);
		Point2 q = l.plumb(_f);

		// Schnittpunkt von g mit der Leitlinie l
		Point2 c = l.intersection(g, new Intersection());

		// Schnittpunkt von g mit der Parabelachse
		Line2 pa = new Line2(q, new Point2(q.x + _v.x, q.y + _v.y)); // Parabelachse
		Point2 d = pa.intersection(g, new Intersection());

		if (d == null)
		{
			// Gerade g parallel zur Parabelachse
			// -> Ein Schnittpunkt
			double a = new Point2(c.x - q.x, c.y - q.y).distance();
			double delta_y = (1.0 / (2.0 * _p)) * a * a + (_p / 2.0);
			Point2 cut = new Point2(c.x + delta_y * _v.x, c.y + delta_y * _v.y);
			intersection.set(cut);
		}// if
		else if (c == null)
		{
			// Gerade g parallel} zur Leitlinie
			double b = new Point2(d.x - q.x, d.y - q.y).distance();
			double delta = (b - new Point2(d.x - _f.x, d.y - _f.y).distance());
			if (delta >= 0 && delta < _EPSILON)
			{
				// Schnittpunkt ist der Scheitelpunkt
				double delta_y = (_p / 2.0);
				Point2 cut = new Point2(q.x + _v.x * delta_y, q.y + _v.y
						* delta_y);
				intersection.set(cut);
			}
			else if (delta > 0)
			{
				// Zwei Schnittpunkte
				double delta_x = Math.sqrt(2.0 * _p * (b - _p / 2.0));
				List list = new List();
				Point2 cut = new Point2(d.x + delta_x * w.x, d.y + delta_x
						* w.y);
				list.add(cut);
				// BUG 19.06.2006
				// cut = new Point2(d.x - delta_x * w.x, d.y + delta_x * w.y);
				cut = new Point2(d.x - delta_x * w.x, d.y - delta_x * w.y);
				list.add(cut);
				intersection.set(list);
			}
			else
			{
				// Kein Schnittpunkt
				intersection.set();
			}
		}
		else
		{
			// c != null & d != null
			double b = new Point2(d.x - q.x, d.y - q.y).distance();
			double a = new Point2(c.x - q.x, c.y - q.y).distance();

			// Vorzeichen
			if ((c.x - q.x) * w.x + (c.y - q.y) * w.y < 0)
				a *= -1;
			if ((d.x - q.x) * _v.x + (d.y - q.y) * _v.y < 0)
				b *= -1;
			/*
			 * Loesen der quadratischen Gleichung
			 */
			double disk = ((b * b) / (a * a)) + (2.0 * b / _p) - 1.0;
			if (Math.sqrt(Math.abs(disk)) <= _EPSILON)
			{
				// eine Loesung
				double delta_x = -(_p * b / a) + _p * Math.sqrt(Math.abs(disk));
				double delta_y = (1.0 / (2.0 * _p)) * delta_x * delta_x
						+ (_p / 2.0);
				Point2 cut = new Point2(q.x + delta_x * w.x + delta_y * _v.x,
						q.y + delta_x * w.y + delta_y * _v.y);
				//Point2 cut = new Point2(q.x + _v.x * delta_y, q.y + _v.y * delta_y);
				intersection.set(cut);
			}
			else if (disk < 0)
				intersection.set();
			else
			{
				// zwei Loesungen
				List list = new List();

				double delta_x = -(_p * b / a) + _p * Math.sqrt(disk);
				double delta_y = (1.0 / (2.0 * _p)) * delta_x * delta_x
						+ (_p / 2.0);
				Point2 cut = new Point2(q.x + delta_x * w.x + delta_y * _v.x,
						q.y + delta_x * w.y + delta_y * _v.y);
				list.add(cut);

				delta_x = -(_p * b / a) - _p * Math.sqrt(disk);
				delta_y = (1.0 / (2.0 * _p)) * delta_x * delta_x + (_p / 2.0);
				cut = new Point2(q.x + delta_x * w.x + delta_y * _v.x, q.y
						+ delta_x * w.y + delta_y * _v.y);
				list.add(cut);

				intersection.set(list);
			}

		}
		return (intersection.point2);
	}


	/**
	 * Berechnet die Schnittpunkte der Geraden / des Strahls / des Segments <c>g
	 * </c> mit dem Parabelstück, welche im senkrechten Streifen des Segments _a
	 * -- _b liegen.
	 * 
	 * @param g
	 *            Das Linienobjekt
	 * @param intersection
	 *            Das Intersection-Objekt, in dem die Schnittpunkte gespeichert
	 *            werden (reference)
	 * 
	 * @return Bei Punktschnitt wird dieser zurückgegeben
	 */
	public Point2 intersection_in_interval(
			BasicLine2 g,
			Intersection intersection)
	{
		this.intersection(g, intersection);
		Segment2 s = new Segment2(_a, _b);
		SimpleList to_do = new SimpleList();
		if (intersection.result == Intersection.LIST)
		{
			to_do.addAll(intersection.list);
		}
		if (intersection.result == Intersection.POINT2)
		{
			to_do.add(intersection.point2);
		}
		for (Enumeration it = to_do.values(); it.hasMoreElements();)
		{
			Point2 cut = (Point2) it.nextElement();
			boolean rem = false;
			// Testen ob der Schnittpunkt auf dem Parabelstueck liegt
			Point2 plumb = s.plumb(cut);
			if (plumb == null)
			{
				//if (! (cut.distance(_p1) <= 0.15 || cut.distance(_p2) <= 0.15 ))
				rem = true;
			}
			if (g.getClass() == Ray2.class)
			{
				plumb = ((Ray2) g).plumb(cut);
				if (plumb == null)
					//if (g.source().distance(cut) >= 0.15)
					rem = true;
			}
			if (g.getClass() == Segment2.class)
			{
				Segment2 seg = (Segment2) g;
				plumb = seg.plumb(cut);
				if (plumb == null)
					//if (!(cut.distance(seg.source()) <= 0.15 || cut.distance(seg._target) <= 0.15))
					rem = true;
			}
			if (rem)
			{
				to_do.remove(cut);
			}
		}// END FOR

		if (to_do.empty())
		{
			intersection.set();
		}
		if (to_do.length() == 1)
		{
			intersection.result = Intersection.POINT2;
			intersection.point2 = (Point2) to_do.getValueAt(0);
		}

		return intersection.point2;
	}


	// /**
	// * Liefert dem Endpunkt der Parabel, die dem Eingabepunkt <c>input </c>
	// * aehnelt.
	// *
	// * @param input
	// * @return
	// */
	// public Point2 get_parabola_point(Point2 input) {
	// if (input.equals(_a))
	// return _p1;
	// if (input.equals(_b))
	// return _p2;
	// return null;
	// }

	/**
	 * Liefert den Startpunkt des Parabelstücks
	 * 
	 * @return Der Startpunkt
	 */
	public Point2 get_start_point()
	{
		return _p1;
	}


	/**
	 * Scheitelpunkt der Parabel
	 * 
	 * @return Der Scheitelpunkt
	 */
	public Point2 get_vertex()
	{
		return new Point2(_f.x - (0.5 * _p) * _v.x, _f.y - (0.5 * _p) * _v.y);
	}


	/**
	 * Liefert den Endpunkt des Parabelstücks
	 * 
	 * @return Der Endpunkt
	 */
	public Point2 get_end_point()
	{
		return _p2;
	}


	/**
	 * Berechnet den Schnitt mit einer zweiten Parabel <c>other</c> und legt das
	 * Ergebnis in <c>intersection</c> ab. Falls ein Schnittpkt existiert wird
	 * dieser zurückgeliefert.
	 * 
	 * @param other
	 *            Die zweite Parabel
	 * @param intersection
	 *            die (etwaigen) Schnitte
	 * @param in_interval
	 *            Flag : wenn true dann werden nur die Schnittpunkte errechnet
	 *            die auf den Parabelstuecken liegen und nicht auf der
	 *            unendlichen Parabel
	 * 
	 * @return Einer der Schnittpunkte
	 */
	public Point2 intersection(
			Parabola2 other,
			Intersection intersection,
			boolean in_interval)
	{
		// Das Referenzkoordinatensystem ist das K.system dieser Parabel
		// Leitlinie und Parabelachse
		// => der Ursprung ist q
		Line2 l = new Line2(_a, _b);
		Point2 q = l.plumb(_f);

		// Winkel 1. Parabel mit Horizontaler
		double phi_1 = this.angle_with_horizont();
		// Winkel 2. Parabel mit Horizontaler
		double phi_2 = other.angle_with_horizont();
		// Winkel zur Drehung 2. Parabel ins Koordinatensystem der 1. Parabel
		double phi = phi_2 - phi_1;
		// phi ist der Winkel zur Drehung der zweiten ins Koordinatensystem der
		// ersten
		// im mathematischen Sinne also !gegen! den Uhrzeigersinn

		// Parabelachse Parabel2
		Line2 g_2 = other.parabola_axis();
		// Vertex Parabel2
		Point2 ver = other.get_vertex();
		Point2 plumb = g_2.plumb(q);
		double h_2 = q.distance(plumb);
		double k_2 = plumb.distance(ver);
		// Vorzeichen
		if (other.parabola_axis().orientation(q) == Point2.ORIENTATION_RIGHT)
			h_2 *= -1;
		if (new Line2(ver, new Point2(ver.x + other._v.y, ver.y - other._v.x))
				.orientation(q) == Point2.ORIENTATION_LEFT)
			k_2 *= -1;

		double p_1 = this._p / 2.0;
		double p_2 = other._p / 2.0;

		// a_1, b_1, c_1
		double a_1 = Math.sin(phi) * p_2;
		double b_1 = 2.0 * Math.cos(phi) * p_2;
		double c_1 = Math.cos(phi) * h_2 + Math.sin(phi) * k_2;

		// a_2, b_2, c_2
		double a_2 = Math.cos(phi) * p_2;
		double b_2 = -2.0 * Math.sin(phi) * p_2;
		double c_2 = Math.cos(phi) * k_2 - Math.sin(phi) * h_2;

		// Ausgabe der Parameter
		//		System.out.println("Itersection Parabola Parabola --- Parameters :");
		//		System.out.println("p_1 : " + p_1);
		//		System.out.println("p_2 : " + p_2 + " k_2 : " + k_2 + "h_2 : " + h_2);
		//		System.out.println("PHI : " + 180 * phi / Math.PI);

		// Koeffizienten der Gleichung die zu loesen ist
		double[] eqn = new double[] { a_1 * a_1 / (4.0 * p_1),
				(a_1 * b_1) / (2.0 * p_1),
				(2.0 * a_1 * c_1 + b_1 * b_1) / (4.0 * p_1) - a_2,
				(b_1 * c_1) / (2.0 * p_1) - b_2,
				(c_1 * c_1 / (4.0 * p_1)) - c_2 + p_1 };

		double[] result = new double[4];
		int n = solve_quartic_equation(eqn, result);
		Vector result_points = new Vector();
		for (int i = 0; i < n; i++)
		{
			System.out.println("x" + i + ":" + result[i]);
			double t_2 = result[i];
			double t_1 = (a_1 * t_2 * t_2 + b_1 * t_2 + c_1) / (2.0 * p_1);
			Point2 cut = calculate_parabola_point(t_1);
			if (in_interval)
			{
				if (this.lies_on(cut) && other.lies_on(cut))
				{
					result_points.add(calculate_parabola_point(t_1));
				}
			}
			else
				result_points.add(calculate_parabola_point(t_1));
		}

		if (result_points.size() == 0)
		{ // Keine Loesung
			intersection.set();
		}
		if (result_points.size() == 1)
		{
			intersection.set((Point2) result_points.get(0));
		}
		if (result_points.size() > 1)
		{
			List list = new List();
			for (int i = 0; i < result_points.size(); i++)
				list.add((Point2) result_points.get(i));
			intersection.set(list);
		}
		return intersection.point2;
	}


	/**
	 * Berechnet den Punkt zum Wert x
	 * 
	 * @param x
	 *            Der gegebene x-Wert
	 * 
	 * @return Der Punkt auf der Parabel
	 */
	private Point2 calculate_parabola_point(
			double x)
	{
		Point2 q = this.plumb_point();

		// Normierter Vektor in "x" Richtung
		Point2 w = new Point2(_v.y, -_v.x);

		double delta_x = _p * x;
		double delta_y = 0.5 * _p * x * x + (0.5 * _p);
		return new Point2(q.x + delta_x * w.x + delta_y * _v.x, q.y + delta_x
				* w.y + delta_y * _v.y);

	}


	/**
	 * Testet, ob der Punkt <c>check</c> auf dem Parabelstück liegt
	 * 
	 * @param check
	 *            Der Eingabepunkt
	 * 
	 * @return true, wenn dieser auf der Parabel liegt, false sonst
	 */
	private boolean lies_on(
			Point2 check)
	{

		Segment2 s = new Segment2(_a, _b);
		return (s.plumb(check) != null);
	}


	/**
	 * Überprüft, ob ein bestimmtes Intervall genutzt wird
	 * 
	 * @param t
	 *            Die Größe des Intervalls
	 * 
	 * @return true, wenn die Größe des Intervalls ausreicht, false sonst
	 */
	private boolean lies_in_interval(
			double t)
	{
		Point2 q = this.plumb_point();
		Line2 axis = this.parabola_axis();
		// Intervall bestimmen [ m, n ]
		double m = q.distance(_a) / _p;
		if (axis.orientation(_a) == Point2.ORIENTATION_LEFT)
			m *= -1;
		double n = q.distance(_b) / _p;
		if (axis.orientation(_b) == Point2.ORIENTATION_LEFT)
			n *= -1;
		if (m <= n)
			return (t <= n && t >= m);
		else
			return (t <= m && t >= n);
	}


	/**
	 * Teilt das Parabelstück in zwei Teile am <c>split_point</c>
	 * 
	 * @param split_point
	 *            Der Eingabepunkt
	 * 
	 * @return Die beiden neuen Parabelstücke
	 */
	public Parabola2[] split(
			Point2 split_point)
	{
		Line2 s = new Line2(_a, _b);
		Point2 c = s.plumb(split_point);
		Parabola2 one = new Parabola2(new Segment2(new Point2(_a),
				new Point2(c)), this._f);
		Parabola2 other = new Parabola2(new Segment2(new Point2(c), new Point2(
				_b)), this._f);
		return new Parabola2[] { one, other };
	}


	/**
	 * Liefert den Winkel der Leitgeraden mit der Horizontalen und zwar so, dass
	 * nach Drehung um <c>angle</c> der Brennpunkt ueber der Leitgeraden liegt
	 * 
	 * @return Der Winkel
	 */
	public double angle_with_horizont()
	{
		double angle = 0;
		Segment2 s = new Segment2(_a, _b);

		if (!s.isVertical())
		{
			angle = -(float) Math.atan(s.slope());
		}
		else
			angle = -(float) Math.PI / 2.0f;

		if (_f.orientation(s.getLeftPoint(), s.getRightPoint()) == Point2.ORIENTATION_RIGHT)
		{
			angle += Math.PI;
		}

		return angle;
	}


	/**
	 * Liefert die Parabelachse als <c>Line2</c>
	 * 
	 * @return Die Parabelachse
	 */
	public Line2 parabola_axis()
	{
		return new Line2(new Point2(_f), new Point2(_f.x + _v.x, _f.y + _v.y));
	}


	/**
	 * Liefert das Lot des Brennpunkts auf die Leitlinie
	 * 
	 * @return Das Lot des Brennpunkts
	 */
	public Point2 plumb_point()
	{
		return new Line2(_a, _b).plumb(_f);
	}


	/**
	 * Die Parameter der Funktion
	 * 
	 * @return Ein Array der Parameter
	 */
	public double[] parameters()
	{
		// Parabelachse
		Line2 g = this.parabola_axis();
		// Leitlinie
		Segment2 s = new Segment2(_a, _b);
		Line2 l = new Line2(s.getLeftPoint(), s.getRightPoint());

		Point2 plumb = g.plumb(new Point2());
		double k = plumb.distance(_f) - (_p / 2.0);
		double h = plumb.distance();

		// Vorzeichen
		if (g.orientation(new Point2()) == Point2.ORIENTATION_RIGHT)
			h *= -1;
		if (l.orientation(new Point2()) == Point2.ORIENTATION_LEFT)
			k *= -1;

		System.out.println(" h : " + h + " k : " + k);

		return new double[] { h, k };
	}


	/**
	 * Berechnet den Wert eines Polynoms an der Stelle <c>x </c>
	 * 
	 * @param coeff
	 *            Die Koeffizienten
	 * @param x
	 *            Der Wert x, an dem ausgewertet werden soll
	 * 
	 * @return Der y-Wert an der Stelle x
	 */
	public static double evaluate_polynom(
			double[] coeff,
			double x)
	{
		double result = 0;
		for (int i = 0; i < coeff.length; i++)
			result += coeff[0] * Math.pow(x, i);
		return result;
	}


	/**
	 * Liefert die Ableitung des Polynoms <c>coeff</c>
	 * 
	 * @param coeff
	 *            Die Koeffizienten
	 * 
	 * @return Die Koeffizienten der Ableitung
	 */
	public static double[] derivate(
			double[] coeff)
	{
		double[] result = new double[coeff.length - 1];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = ((double) (i + 1) * coeff[i]);
		}
		return result;
	}


	/**
	 * Führt eine Newtoniteration zur Nullstellenbestimmung in dem Intervall
	 * [a,b] durch Wenn eine Nullstelle gefunden wird, wird diese
	 * zurückgeliefert Wenn keine Nullstelle gefunden wird, dann wird ein Wert
	 * rechts von <c>b</c> zurückgeliefert
	 * 
	 * @param coeff
	 *            Das Polynom in Form von Koeffizienten
	 * @param a
	 *            linker Rand
	 * @param b
	 *            rechter Rand
	 * @param max_iter
	 *            Maximale Iterationen
	 * 
	 * @return Ergebnis der Iteration
	 */
	public static double newton_method(
			double[] coeff,
			double a,
			double b,
			int max_iter)
	{

		// Die Ableitung
		double[] coeff_derivation = derivate(coeff);

		double x_old = (b - a) / 2.0;
		double x_new;
		int i = 0;
		do
		{
			x_new = x_old
					- (evaluate_polynom(coeff, x_old) / evaluate_polynom(
							coeff_derivation, x_old));
			i++;
		}
		while (Math.abs(evaluate_polynom(coeff, x_new)) > Parabola2._EPSILON
				&& i < max_iter);
		if (i == max_iter)
			return b + 1.0;
		else
			return x_new;

	}


	/**
	 * Das Newton-Verfahren
	 * 
	 * @param coeff
	 *            Die Koeffizienten der Funktion
	 * @param result
	 *            Das Ergebnis
	 * 
	 * @return Anzahl der Ergebnisse
	 */
	public static int general_newton_method(
			double[] coeff,
			double[] result)
	{
		// Grad des Polynoms
		int n = coeff.length - 1;
		double a, b = 0; // Nullstellenschranken
		// Nullstellenschranken bestimmen
		// Methode 1
		double[] ab_1 = new double[2];
		double[] eqn = new double[] {
				(2.0 * (double) (n - 1) * (coeff[n - 2]) / coeff[n])
						- ((double) (n - 2) * Math.pow(coeff[n - 1] / coeff[n],
								2.0)), 2.0 * coeff[n - 1] / coeff[n],
				(double) n };
		int i = QuadCurve2D.solveQuadratic(eqn, ab_1);
		if (i == 2)
		{
			System.out.println("Methode 1 funktioniert ");
			System.out.println("[ " + ab_1[0] + ", " + ab_1[1] + " ]");

		}
		else
			System.out.println("i = " + i);
		// Methode 2
		int N = 0;
		for (int j = 0; j < coeff.length; j++)
		{
			if (coeff[j] < 0)
				N++;
		}
		double max = 0;
		double actual = 0;
		for (int j = 0; j < n; j++)
		{
			if (coeff[j] / coeff[n] < 0)
			{
				actual = Math.pow((double) N * Math.abs(coeff[j] / coeff[n]),
						(1.0 / (double) (n - j)));
				System.out.println(actual);
			}
			if (actual > max)
				max = actual;
		}
		System.out.print("Methode 2 :" + max);
		System.out.println("N : " + N);

		return 0;

	}


	/**
	 * Löst die Gleichung vierten Grades A x^4 + B x^3 + C x^2 + D x + E = 0
	 * 
	 * Diese Funktion benutzt das Verfahren von Ferrari Die genaue
	 * Vorgehensweise findet man hier :
	 * 
	 * Daniel Schumacher, ver 1.0 22.11.2005
	 * 
	 * @param coeff
	 *            {A, B, C, D, E}
	 * @param result
	 *            Die gefundenen Wurzeln
	 * 
	 * @return Die Anzahl gefundener Wurzeln
	 * 
	 * @see "http://mathworld.wolfram.com/QuarticEquation.html"
	 */
	public static int solve_quartic_equation(
			double[] coeff,
			double[] result)
	{
		// System.out.println("solving equation "+coeff[0] + " x^4 + "+ coeff[1]
		// + " x^3 + "+ coeff[2]+
		// " x^2 + "+ coeff[3] +" x + " + coeff[4]+ " = 0");
		// Degenerate Case : A = 0
		if (coeff[0] == 0)
			return CubicCurve2D.solveCubic(new double[] { coeff[4], coeff[3],
					coeff[2], coeff[1] }, result);

		// Mathworld Version
		double a_3 = coeff[1] / coeff[0];
		double a_2 = coeff[2] / coeff[0];
		double a_1 = coeff[3] / coeff[0];
		double a_0 = coeff[4] / coeff[0];

		// Solution of the resolvent cubic
		double[] res = new double[3];
		double[] eqn = new double[] {
				4.0 * a_2 * a_0 - a_1 * a_1 - a_3 * a_3 * a_0,
				a_1 * a_3 - 4.0 * a_0, -a_2, 1.0 };
		int n = CubicCurve2D.solveCubic(eqn, res);
		if (n > 0)
		{
			// Suche bestes y aus
			double R = java.lang.Double.NaN;
			double y = 0;
			for (int i = 0; java.lang.Double.compare(R, java.lang.Double.NaN) == 0
					&& i < n; i++)
			{
				y = res[i];
				R = Math.sqrt(0.25 * a_3 * a_3 - a_2 + y);
			}
			double part_0, part_1;
			if (R == 0)
			{
				part_0 = 0.75 * a_3 * a_3 - 2.0 * a_2;
				part_1 = 2.0 * Math.sqrt(y * y - 4.0 * a_0);
			}
			else
			{
				part_0 = 0.75 * a_3 * a_3 - R * R - 2.0 * a_2;
				part_1 = 0.25 * (4.0 * a_3 * a_2 - 8.0 * a_1 - a_3 * a_3 * a_3)
						/ R;
			}
			double D = Math.sqrt(part_0 + part_1);
			double E = Math.sqrt(part_0 - part_1);

			/*
			 * System.out.println("number roots of resolvent cubics: " + n + " ,
			 * chosen y: " + y); System.out.println("R: " + R);
			 * System.out.println("part_0: " + part_0);
			 * System.out.println("part_1: " + part_1);
			 */

			double p_0 = -0.25 * a_3;
			double p_1 = 0.5 * R;
			double p_2 = 0.5 * D;
			double p_3 = 0.5 * E;
			if (java.lang.Double.compare(D, java.lang.Double.NaN) != 0
					&& java.lang.Double.compare(E, java.lang.Double.NaN) != 0)
			{
				result[0] = p_0 + p_1 + p_2;
				result[1] = p_0 + p_1 - p_2;
				result[2] = p_0 - p_1 + p_3;
				result[3] = p_0 - p_1 - p_3;
				return 4;
			}
			if (java.lang.Double.compare(D, java.lang.Double.NaN) != 0)
			{
				result[0] = p_0 + p_1 + p_2;
				result[1] = p_0 + p_1 - p_2;
				return 2;
			}
			if (java.lang.Double.compare(E, java.lang.Double.NaN) != 0)
			{
				result[0] = p_0 + p_1 + p_3;
				result[1] = p_0 + p_1 - p_3;
				return 2;
			}
		}// resolvent cubic has a solution
		return 0;
	}


	/**
	 * Liefert einen Punkt "nahe" an einem Anfangspunkt des Parabelstücks
	 * 
	 * @param direction
	 *            Die Richtung
	 * @param epsilon
	 *            Die Genauigkeit
	 * 
	 * @return Den Ergebnispunkt
	 */
	public Point2 near_end_point(
			boolean direction,
			float epsilon)
	{
		double distance = 0;
		Point2 check = null;
		Point2 q = this.plumb_point();

		Point2 s = (direction ? _a : _b);
		Point2 t = (direction ? _b : _a);

		double h = 0.1d;
		boolean ready = false;

		do
		{

			Point2 n = new Point2((1.0 - h) * s.x + h * t.x, (1.0 - h) * s.y
					+ h * t.y);
			double delta_x = n.distance(q);
			// y = (1 / 2 _p) x^2 + 0.5 _p
			double delta_y = (((1.0 / (2 * _p))) * delta_x * delta_x) + 0.5
					* _p;
			check = new Point2(n.x + delta_y * _v.x, n.y + delta_y * _v.y);
			if (direction)
				distance = check.distance(_p1);
			else
				distance = check.distance(_p2);
			System.out.println(distance);
			System.out.println(h);
			System.out.println();
			if (distance / epsilon <= 2.0)
			{
				ready = true;
			}
			else
				h *= 0.5;
		}
		while (!ready && h >= _EPSILON);

		System.out.print("distance = " + distance);
		return check;
	}


	/**
	 * Ausgabe als String
	 * 
	 * @return Textuelle Ausgabe des Objekts
	 * 
	 */
	public String toString()
	{
		return "Leitlinie: " + _a + "-" + _b + " Brennpunkt: " + _f
				+ " Start: " + _p1 + " Ende: " + _p2;
	}


	/**
	 * Berechnet p1, p2, und ctrl und setzt die Kurve wenn _a, _b, _f bereits
	 * gesetzt sind.
	 */
	private void _set_curve_parameters()
	{
		// Konstruktion von P1, P2, CTRL

		Segment2 l = new Segment2(_a, _b);
		// Senkrechte in den Endpunkten des Segments l
		Line2 s1 = l.orthogonal(_a);
		Line2 s2 = l.orthogonal(_b);

		BasicLine2 b1 = new VoronoiBisector2(_f, _a).getRepresentation();
		BasicLine2 b2 = new VoronoiBisector2(_f, _b).getRepresentation();

		Intersection i = new Intersection();

		_p1 = s1.intersection(b1, i);
		_p2 = s2.intersection(b2, i);
		_ctrl = b1.intersection(b2, i);

		this.setCurve(_p1, _ctrl, _p2);
	}


	/**
	 * Berechnet den normierten Richtungsvektor der Parabelachse wenn _a, _b, _f
	 * bereits gesetzt sind.
	 */
	private void _set_axis_vector()
	{
		// Der Lotpunkt von f auf l
		Line2 l = new Line2(_a, _b);
		Point2 q = l.plumb(_f);
		_p = q.distance(_f);

		// Richtungsvektor der Parabelachse
		_v = new Point2(_f.x - q.x, _f.y - q.y);
		double length = _v.distance();
		_v.moveTo(_v.x / length, _v.y / length);

	}

}
