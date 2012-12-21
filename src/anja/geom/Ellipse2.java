package anja.geom;


import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.util.*;

import java.awt.geom.Rectangle2D;
import java.awt.*;


public class Ellipse2
		implements Drawable, Cloneable, java.io.Serializable
{

	private static float				faktor		= 1.2f;			// Faktor um den die große Achse größer ist als die lineare Exzentrizität/Brennweite

	private double						PhiDeg;
	private Point2						Center;
	private double						xF1;
	private double						yF1;
	private double						xF2;
	private double						yF2;
	private Point2						F1;
	private Point2						F2;
	private int							iyRadius;
	private int							ixRadius;
	private int							iyCenter;
	private int							ixCenter;

	anja.swinggui.WorldCoorTransformer	transformer;

	public double						PhiRad;
	public double						yRadius;
	public double						xRadius;
	public double						xCenter;
	public double						yCenter;

	public double						iLineareExzentrizitaet;
	public double[]						Parameter	= new double[6];


	//	Evtl ist es sinnvoll, in die Klasse Winkelbereiche zu integrieren. Dann wäre es keine Ellipsenklasse mehr, sondern eine
	//  Ellipsenbogenklasse. Dadurch wäre der Schnitt von Ellipsen als Liste solcher Elemente darstellbar.
	//	private static int DegreeBegin;
	//	private static int DegreeEnd; 

	/**
	 * Erstellt eine Ellipse
	 * 
	 * @param centerX
	 *            x-Koordinate des Zentrums
	 * @param centerY
	 *            y-Koordinate des Zentrums
	 * @param radiusX
	 *            Radius in x-Richtung
	 * @param radiusY
	 *            Radius in y-Richtung
	 * @param phiRadiant
	 *            Winkel
	 */
	public Ellipse2(
			double centerX,
			double centerY,
			double radiusX,
			double radiusY,
			double phiRadiant)
	{
		//		PhiDeg = phiDegree;
		PhiRad = phiRadiant;
		xRadius = radiusX;
		yRadius = radiusY;
		ixRadius = (int) radiusX;
		iyRadius = (int) radiusY;
		xCenter = centerX;
		yCenter = centerY;
		Center = new Point2(xCenter, yCenter);
		ixCenter = (int) centerX;
		iyCenter = (int) centerY;
		MiniConstructor();
		System.out
				.println("!!! Achtung, dieser Konstruktor bietet noch nicht alle Funktionen !!!");
	}


	/**
	 * Erstellt eine Ellipse
	 * 
	 * @param p1
	 *            Punkt 1
	 * @param p2
	 *            Punkt 2
	 */
	public Ellipse2(
			Point2 p1,
			Point2 p2)
	{
		xF1 = p1.x; //F1X;
		yF1 = p1.y; //F1Y;
		xF2 = p2.x; //F2X;
		yF2 = p2.y; //F2Y;
		xCenter = xF1 + (xF2 - xF1) / 2;
		yCenter = yF1 + (yF2 - yF1) / 2;
		Center = new Point2(xCenter, yCenter);
		F1 = new Point2(p1);
		F2 = new Point2(p2);

		ixCenter = (int) xCenter;
		iyCenter = (int) yCenter;

		iLineareExzentrizitaet = Center.distance(F1);
		xRadius = F1.distance(F2) / 2 * faktor;
		yRadius = Math.sqrt(xRadius * xRadius - iLineareExzentrizitaet
				* iLineareExzentrizitaet);

		ixRadius = (int) xRadius;
		iyRadius = (int) yRadius;

		PhiRad = ((Point2) (new Point2(xF1, yF1))).angle(new Point2(xF2, yF2));

		// Jetzt wird vom Bogenmaß ins Gradmaß umgerechnet
		PhiDeg = 180f * PhiRad / (float) java.lang.Math.PI;
		MiniConstructor();
	}


	/**
	 * private Methode
	 */
	private void MiniConstructor()
	{
		SimpleList punkte = getPoints();
		Parameter = getBestimmungsgleichung(punkte);
	}


	/**
	 * pq-Formel für 2 Werte p und q. Es muss Nullstellen der quadratische
	 * Funktion geben, sonst funktioniert der Algorithmus nicht!
	 * 
	 * @param p
	 *            p-Wert
	 * @param q
	 *            q-Wert
	 * 
	 * @return Array mit Lösungen
	 */
	private double[] pqFormel(
			double p,
			double q)
	{
		double[] L = new double[2];
		L[0] = -p / 2 + java.lang.Math.sqrt(p * p / 4 - q);
		L[1] = -p / 2 - java.lang.Math.sqrt(p * p / 4 - q);
		return L;
	}


	/**
	 * Diese Funktion gibt das Zentrum der Ellipse zurück
	 * 
	 * @return the center of the ellipse
	 */
	public Point2 getCenter()
	{
		return Center;
	}


	/**
	 * Getter für F1
	 * 
	 * @return F1-Wert
	 */
	public Point2 getF1()
	{
		return F1;
	}


	/**
	 * Getter für F2
	 * 
	 * @return F2-Wert
	 */
	public Point2 getF2()
	{
		return F2;
	}


	/**
	 * Gibt an, ob der übergebene Punkt innerhalb oder außerhalb der Ellipse
	 * liegt
	 * 
	 * @param p
	 *            Punkt, der geprüft werden soll
	 * 
	 * @return true, wenn p innerhalb liegt, sonst false
	 */
	public boolean contains(
			Point2 p)
	{
		if (p.distance(F1) + p.distance(F2) < 2 * xRadius)
			return true;
		return false;
	}


	/**
	 * Berechnet den Winkel zum Zentrum der Ellipse im Verhältnis zu deren
	 * Schräglage
	 * 
	 * 
	 * @param p
	 *            Der Eingabepunkt
	 * 
	 * @return Winkel im Bogenmaß
	 */
	public double angle(
			Point2 p)
	{
		double result = this.Center.angle(p) - this.PhiRad;
		if (result < 0)
			return (2 * Math.PI + result);
		else
			return result;
	}


	/**
	 * Berechnet den Punkt auf der Ellipse, der in angle Radianten auf der
	 * Ellipse liegt
	 * 
	 * @param angle
	 *            Winkel in Radianten
	 * 
	 * @return Punkt auf der Ellipse
	 */
	public Point2 getPoint(
			double angle)
	{
		// Fast egal, wo man sucht, findet man die gleiche Berechnung wie beim Krei:
		// p.x = a*cos(alpha)   und   p.y = b*sin(alpha)
		// Diese Berechnung ergibt zwar sowohl beim Kreis als auch bei der Ellipse einen Punkt auf dem Rand, jedoch ist bei der Ellipse
		// der Winkel ein anderer. Daher muß der zu berechnende Winkel vorher konvertiert werden
		// a := große Halbachse der Ellipse (gleich dem Radius des Kreises)
		// b := kleine Halbachse der Ellipse
		// t := Der Winkel im Kreis
		// phi := Winkel in der Ellipse
		// Dann gilt t = arctan((a/b)*tan(phi))

		double Ellipsenwinkel = Math
				.atan((xRadius / yRadius) * Math.tan(angle));
		if (Ellipsenwinkel < 0)
			Ellipsenwinkel += 2 * Math.PI;
		if (angle > Math.PI / 2 && angle <= Math.PI)
			Ellipsenwinkel = Ellipsenwinkel - Math.PI;
		else if (angle > Math.PI && angle <= Math.PI + Math.PI / 2)
			Ellipsenwinkel = Ellipsenwinkel + Math.PI;

		Point2 result = new Point2();

		// Jetzt wird der Punkt auf der ungedrehten Ellipse berechnet, wobei die Ellipse im Mittelpunkt liegt
		double xtmp;
		double ytmp;
		xtmp = xRadius * Math.cos(Ellipsenwinkel);
		ytmp = yRadius * Math.sin(Ellipsenwinkel);

		// Zuletzt wenden wir die Rotationsmatrix an und verschieben die Punkte an den ursprünglichen Ort der Ellipse zurück
		result.x = (float) (Math.cos(PhiRad) * xtmp - Math.sin(PhiRad) * ytmp + this.xCenter);
		result.y = (float) (Math.sin(PhiRad) * xtmp + Math.cos(PhiRad) * ytmp + this.yCenter);

		return result;
	}


	/**
	 * Diese Funktion gibt zu einer Ellipse eine Liste mit 5 Punkten zurück.
	 * Diese reichen aus, um eine Ellipse eindeutig zu beschreiben.
	 * 
	 * @return SimpleList mit 5 Punkten (Point2)
	 */
	public SimpleList getPoints()
	{
		SimpleList result = new SimpleList();
		//		double Phi = -1*(1*Math.PI - PhiRad);
		double Phi = PhiRad;

		Point2 tmp = new Point2();
		Point2 p = new Point2();
		p.x = (float) (xCenter + xRadius * Math.cos(0.0));
		p.y = (float) (yCenter + yRadius * Math.sin(0.0));
		tmp.x = (float) (p.x - xCenter);
		tmp.y = (float) (p.y - yCenter);
		p.x = (float) (tmp.x * Math.cos(Phi) - tmp.y * Math.sin(Phi));
		p.y = (float) (tmp.x * Math.sin(Phi) + tmp.y * Math.cos(Phi));
		p.x = (float) (p.x + xCenter);
		p.y = (float) (p.y + yCenter);
		result.add(p);

		p = new Point2();
		p.x = (float) (xCenter + xRadius * Math.cos(Math.PI / 4));
		p.y = (float) (yCenter + yRadius * Math.sin(Math.PI / 4));
		tmp.x = (float) (p.x - xCenter);
		tmp.y = (float) (p.y - yCenter);
		p.x = (float) (tmp.x * Math.cos(Phi) - tmp.y * Math.sin(Phi));
		p.y = (float) (tmp.x * Math.sin(Phi) + tmp.y * Math.cos(Phi));
		p.x = (float) (p.x + xCenter);
		p.y = (float) (p.y + yCenter);
		result.add(p);

		p = new Point2();
		p.x = (float) (xCenter + xRadius * Math.cos(Math.PI / 2));
		p.y = (float) (yCenter + yRadius * Math.sin(Math.PI / 2));
		tmp.x = (float) (p.x - xCenter);
		tmp.y = (float) (p.y - yCenter);
		p.x = (float) (tmp.x * Math.cos(Phi) - tmp.y * Math.sin(Phi));
		p.y = (float) (tmp.x * Math.sin(Phi) + tmp.y * Math.cos(Phi));
		p.x = (float) (p.x + xCenter);
		p.y = (float) (p.y + yCenter);
		result.add(p);

		p = new Point2();
		p.x = (float) (xCenter + xRadius * Math.cos(Math.PI));
		p.y = (float) (yCenter + yRadius * Math.sin(Math.PI));
		tmp.x = (float) (p.x - xCenter);
		tmp.y = (float) (p.y - yCenter);
		p.x = (float) (tmp.x * Math.cos(Phi) - tmp.y * Math.sin(Phi));
		p.y = (float) (tmp.x * Math.sin(Phi) + tmp.y * Math.cos(Phi));
		p.x = (float) (p.x + xCenter);
		p.y = (float) (p.y + yCenter);
		result.add(p);

		p = new Point2();
		p.x = (float) (xCenter + xRadius * Math.cos(Math.PI * 1.5));
		p.y = (float) (yCenter + yRadius * Math.sin(Math.PI * 1.5));
		tmp.x = (float) (p.x - xCenter);
		tmp.y = (float) (p.y - yCenter);
		p.x = (float) (tmp.x * Math.cos(Phi) - tmp.y * Math.sin(Phi));
		p.y = (float) (tmp.x * Math.sin(Phi) + tmp.y * Math.cos(Phi));
		p.x = (float) (p.x + xCenter);
		p.y = (float) (p.y + yCenter);
		result.add(p);

		return result;
	}


	/**
	 * Diese Funktion findet zu 5 beispielhaften Punkten die
	 * Bestimmungsgleichung der Ellipse
	 * 
	 * @param punkte
	 *            SimpleList mit 5 Punkte
	 * 
	 * @return Liste mit 5 Parametern zur mathematischen Beschreibung der
	 *         Ellipse in der Form ax^2+bxy+cy^2+dx+ey+f=0
	 */
	public double[] getBestimmungsgleichung(
			SimpleList punkte)
	{
		double[] L = new double[6];

		// z[b][a] dient als Darstellung des LGS hat die Dimensionen:
		//  a: die verschiedenen Unbekannten die aufgelöst werden müssen (müßten 6 sein, aber eine Unbekannte kann man fest setzen)
		//  b: die fünf gegebenen Gleichungen.
		double[][] z = new double[5][6];

		// Nun wird das LGS anhand der Formel  f(x,y)= ax^2 + bxy + cy^2 + dx + ey + f = 0   erstellt
		for (int i = 0; i < 5; i++)
		{
			z[i][0] = ((Point2) (punkte.getKeyAt(i))).x
					* ((Point2) (punkte.getKeyAt(i))).x;
			z[i][1] = ((Point2) (punkte.getKeyAt(i))).x
					* ((Point2) (punkte.getKeyAt(i))).y;
			z[i][2] = ((Point2) (punkte.getKeyAt(i))).y
					* ((Point2) (punkte.getKeyAt(i))).y;
			z[i][3] = ((Point2) (punkte.getKeyAt(i))).x;
			z[i][4] = ((Point2) (punkte.getKeyAt(i))).y;
			z[i][5] = 1;
		}

		try
		// der Konstruktor der Klasse LGS wirft eine Exception, falls #Unbekannte!=#Gleichungen
		{

			L = getLGSLoesung(z);
			L[5] = -1;

			//			System.out.format("E: %fx^2 + %fxy + %fy^2 + %fx + %fy -1%n", L[0], L[1], L[2], L[3], L[4]);
		}
		catch (Exception ex)
		{
			// den Fall gibt es bei uns nicht.
		}

		return L;
	}


	/**
	 * Überprüft auf Schnitt mit der Eingabeellipse
	 * 
	 * @param obj
	 *            Die Eingabeellipse
	 * 
	 * @return Array der Schnittpunkte
	 */
	public Point2[] getIntersection(
			Object obj)
	{
		Ellipse2 ellipse = (Ellipse2) obj;
		Point2[] result;

		double a = this.Parameter[0];
		double b = this.Parameter[1];
		double c = this.Parameter[2];
		double d = this.Parameter[3];
		double e = this.Parameter[4];
		double f = this.Parameter[5];
		double A = ellipse.Parameter[0];
		double B = ellipse.Parameter[1];
		double C = ellipse.Parameter[2];
		double D = ellipse.Parameter[3];
		double E = ellipse.Parameter[4];
		double F = ellipse.Parameter[5];

		double x4 = A * A * c * c + C * C * a * a + a * B * B * c + A * b * b
				* C - 2 * c * C * a * A - A * b * B * c - a * b * B * C;
		double x3 = 2 * A * D * c * c + 2 * C * C * a * d + 2 * a * B * c * E
				+ B * B * c * d + b * b * C * D + 2 * A * b * C * e - 2 * c * C
				* a * D - 2 * c * C * A * d - b * B * c * D - A * b * c * E - A
				* B * c * e - a * b * C * E - b * B * C * d - a * B * C * e;
		double x2 = 2 * A * F * c * c + D * D * c * c + 2 * C * C * a * f + C
				* C * d * d + a * c * E * E + 2 * B * c * d * E + B * B * c * f
				+ b * b * C * F + 2 * b * C * D * e + A * C * e * e - 2 * c * C
				* a * F - 2 * c * C * d * D - 2 * c * C * A * f - b * B * c * F
				- b * c * D * E - B * c * D * e - A * c * e * E - b * C * d * E
				- b * B * C * f - a * C * e * E - B * C * d * e;
		double x1 = 2 * D * F * c * c + 2 * C * C * d * f + c * d * E * E + 2
				* B * c * E * f + 2 * b * C * e * F + C * D * e * e - 2 * c * C
				* d * F - 2 * c * C * D * f - b * c * E * F - B * c * e * F - c
				* D * e * E - b * C * E * f - C * d * e * E - B * C * e * f;
		double x0 = F * F * c * c + C * C * f * f + c * E * E * f + C * e * e
				* F - 2 * c * C * f * F - c * e * E * F - C * e * E * f;

		double[] roots = getRootsForQuartic(x0, x1, x2, x3, x4);

		result = new Point2[roots.length];

		// jetzt muessen zu den gerade berechneten Y-Werten noch die X-Werte berechnet werden => pq-Formel
		for (int i = 0; i < roots.length; i++)
		{
			double p = (this.Parameter[1] * roots[i] + this.Parameter[4])
					/ this.Parameter[2];
			double q = (this.Parameter[0] * roots[i] * roots[i]
					+ this.Parameter[3] * roots[i] + this.Parameter[5])
					/ this.Parameter[2];
			double[] L1 = new double[2];
			// L1 sind die y-Werte der this-Ellipse

			if (p * p / 4 - q < 0)
			{
				// wir müssen hier die komplexe Wurzel ziehen
				double root[] = getComplexRoot((p * p / 4 - q), 0);
				L1[0] = root[0];
				L1[1] = root[2];
			}
			else
			{
				L1[0] = -p / 2 + java.lang.Math.sqrt(p * p / 4 - q);
				L1[1] = -p / 2 - java.lang.Math.sqrt(p * p / 4 - q);
			}

			p = (ellipse.Parameter[1] * roots[i] + ellipse.Parameter[4])
					/ ellipse.Parameter[2];
			q = (ellipse.Parameter[0] * roots[i] * roots[i]
					+ ellipse.Parameter[3] * roots[i] + ellipse.Parameter[5])
					/ ellipse.Parameter[2];
			double[] L2 = new double[2];
			// L2 sind die y-Werte der anderen Ellipse
			if (p * p / 4 - q < 0)
			{
				// wir müssen hier die komplexe Wurzel ziehen
				double root[] = getComplexRoot((p * p / 4 - q), 0);
				L1[0] = root[0];
				L1[1] = root[2];

			}
			else
			{
				L2[0] = -p / 2 + java.lang.Math.sqrt(p * p / 4 - q);
				L2[1] = -p / 2 - java.lang.Math.sqrt(p * p / 4 - q);
			}

			// Wir haben jetzt vier y-Werte (zwei pro Ellipse), von denen zwei identisch sein sollten, d.h. wir haben drei verschiedene Werte
			// Es gilt herauszufinden, welcher y-Wert bei beiden Ellipsen vertreten ist
			// Dafür berechnen wir die absoluten Abstände der beiden L1-Werte zu den beiden L2-Werten
			// Stimmt L1 mit einem beliebigen L2-Wert überein, müssen wir uns um L2 nicht mehr kümmern, weil L1 als Ergebnis reicht
			// Da die berechneten y (bzw L)-Werte minimal abweichen können, prüfen wir Übereinstimmung mit dem min-Operator
			double diff00 = (Math.abs(L1[0] - L2[0]));
			double diff01 = (Math.abs(L1[0] - L2[1]));
			double min0 = Math.min(diff00, diff01);
			double diff10 = (Math.abs(L1[1] - L2[0]));
			double diff11 = (Math.abs(L1[1] - L2[1]));
			double min1 = Math.min(diff10, diff11);

			result[i] = new Point2();

			if (min0 < min1)
				result[i].y = (float) L1[0];
			else
				result[i].y = (float) L1[1];

			result[i].x = (float) roots[i];
		}

		int NaNs = 0;
		for (int i = 0; i < result.length; i++)
			if (java.lang.Float.isNaN(result[i].x)
					|| java.lang.Float.isNaN(result[i].y))
				NaNs++;
		if (NaNs > 0)
		{
			if (NaNs == result.length)
				return null;
			Point2[] newResult = new Point2[result.length - NaNs];
			int zaehler = 0;
			for (int i = 0; i < result.length; i++)
				if (!java.lang.Float.isNaN(result[i].x)
						&& !java.lang.Float.isNaN(result[i].y))
				{
					newResult[zaehler] = result[i];
					zaehler++;
				}
		}

		return result;
	}


	/**
	 * Zeichnet die Ellipse von einem Startpunkt zu einem Endpunkt
	 * 
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird.
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 * @param start
	 *            Startpunkt
	 * @param end
	 *            Endpunkt
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc,
			Point2 start,
			Point2 end)
	{
		//System.out.println("Ellipse::Draw(" + start.x + "/" + start.y + ") => (" + end.x + "/" + end.y + ")");

		int startWinkel = 0;
		int endWinkel = 360;

		// wir benötigen den Winkel des Startpunktes in Bezug zur Ellipse
		startWinkel = (int) (180 * angle(start) / Math.PI);
		//System.out.println("Ellipse::Draw: Startpunkt liegt im Winkel " + startWinkel + ", laut Ellipse.angle im Winkel " + (angle(start)*180/Math.PI));

		endWinkel = (int) (180 * angle(end) / Math.PI);

		if (startWinkel > endWinkel)
		{
			//System.out.println("Ellipse::Draw: Startwinkel>Endwinkel => zeichnen in zwei Etappen"); 
			draw(startWinkel, 360, gc, g2d);
			draw(0, endWinkel, gc, g2d);
		}
		else
			draw(startWinkel, endWinkel, gc, g2d);

		//System.out.println("Ellipse::Draw: Ende");
	}


	/**
	 * Zeichnet das komplette Ellipsenobjekt.
	 * 
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird.
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc)
	{
		draw(0, 360, gc, g2d);
	}


	/**
	 * Zeichnet das komplette Ellipsenobjekt.
	 * 
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird.
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 * @param nr
	 *            Diese Variable hat keine Bewandtnis
	 */
	public void draw(
			Graphics2D g2d,
			GraphicsContext gc,
			int nr)
	{
		draw(0, 360, gc, g2d);
	}


	/**
	 * Zeichnet das Ellipsenobjekt für einen konkreten Winkelbereich.
	 * 
	 * @param DegreeBegin
	 *            Startwinkel
	 * @param DegreeEnd
	 *            Endwinkel
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird.
	 * @param _gc
	 *            Die dazugehörigen Formatierungsregeln
	 * 
	 */
	public void draw(
			int DegreeBegin,
			int DegreeEnd,
			GraphicsContext _gc,
			Graphics2D g2d)
	{
		// Diese Funktion berechnet für jedes Grad den Punkt auf der Ellipse und verbindet diese mit Linien => recht ungenau
		//void DrawEllipse(int xCenter, int yCenter, double xRadius, double yRadius, double Phi, int DegreeBegin, int DegreeEnd)//, float Opacity)

		// zuerst werden alte Einstellungen gesichert
		GraphicsContext _last_gc = new GraphicsContext();
		_last_gc.setBackgroundColor(g2d.getBackground());
		_last_gc.setForegroundColor(g2d.getColor()); // is this correct ?
		_last_gc.setFillColor(g2d.getColor());

		java.awt.BasicStroke last_stroke = (java.awt.BasicStroke) g2d
				.getStroke();

		g2d.setStroke(_gc.getStroke());
		if (_gc.getFillStyle() != 0)
		{
			g2d.setColor(_gc.getFillColor());
			if (_gc.getFillColor() != _gc.getForegroundColor())
				g2d.setColor(_gc.getForegroundColor());
		}
		else
		{
			g2d.setColor(_gc.getForegroundColor());
		}

		// Jetzt geht es ans zeichnen
		long LastX = 0;
		long LastY = 0;
		long NextX;
		long NextY;
		double Pos;
		Point2 tmp;

		// Kreisbogen zwischen 'DegreeBegin' und 'DegreeEnd' zeichnen
		for (int i = DegreeBegin; i <= DegreeEnd; ++i)
		{

			// Schrittweite pro Punkt
			Pos = Math.toRadians(i); // aiLibMath::DegreeToArc(i);

			tmp = getPoint(Pos);
			NextX = (long) tmp.x;
			NextY = (long) tmp.y;

			// Linie zum Zielpunkt zeichnen
			if (i != DegreeBegin)
				g2d.drawLine((int) LastX, (int) LastY, (int) NextX, (int) NextY);//, Color, Opacity);

			LastX = NextX;
			LastY = NextY;
		}

		// Zum Schluss werden die alten Einstellungen zurückgeholt
		g2d.setBackground(_last_gc.getBackgroundColor());
		g2d.setColor(_last_gc.getFillColor());
		g2d.setStroke((Stroke) last_stroke);
	}


	/**
	 * Naiver Zeichenvorgang der Ellipse
	 * 
	 * 
	 * @param DegreeBegin
	 *            Startwinkel
	 * @param DegreeEnd
	 *            Endwinkel
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird.
	 * @param _gc
	 *            Die dazugehörigen Formatierungsregeln
	 */
	public void drawExactly(
			int DegreeBegin,
			int DegreeEnd,
			GraphicsContext _gc,
			Graphics2D g2d)
	{
		// Das Zeichnen wird in dieser Funktion naiv umgesetzt. Es wird grob der Bereich der gültigen x-Werte berechnet,
		// dafür wird angenommen, daß die Ellipse waagerecht liegt. Dann werden für alle X-Werte mit der pq-Formel die
		// entsprechenden y-Werte berechnet. Die so berechneten Punkte müssen nun noch mit einer Linie verbunden werden.

		// zuerst werden alte Einstellungen gesichert
		GraphicsContext _last_gc = new GraphicsContext();
		_last_gc.setBackgroundColor(g2d.getBackground());
		_last_gc.setForegroundColor(g2d.getColor()); // is this correct ?
		_last_gc.setFillColor(g2d.getColor());
		java.awt.BasicStroke last_stroke = (java.awt.BasicStroke) g2d
				.getStroke();
		g2d.setStroke(_gc.getStroke());
		if (_gc.getFillStyle() != 0)
		{
			g2d.setColor(_gc.getFillColor());
			if (_gc.getFillColor() != _gc.getForegroundColor())
				g2d.setColor(_gc.getForegroundColor());
		}
		else
			g2d.setColor(_gc.getForegroundColor());

		double RadBegin = DegreeBegin * Math.PI / 180;
		double RadEnd = DegreeEnd * Math.PI / 180;
		int xStart = (int) (xCenter + xRadius * Math.cos(RadBegin));
		int xEnd = (int) (xCenter + xRadius * Math.cos(RadEnd));

		int xOld = 0;
		int[] yOldArr = new int[2];

		Point2 F1 = new Point2(this.xF1, this.yF1); // Brennpunkt 1
		Point2 F2 = new Point2(this.xF2, this.yF2); // Brennpunkt 2
		Point2 tmp1, tmp2; // die für jeden X-Wert neu berechneten Punkte auf der E.

		boolean merken1 = false;
		boolean Angefangen = false;
		boolean ausgang = false;

		for (int x = xStart; x < xEnd; x++)
		{
			// Jetzt werden mit der pq-Formel die beiden y-Werte berechnet
			double p = (Parameter[1] * x + Parameter[4]) / Parameter[2];
			double q = (Parameter[0] * x * x + Parameter[3] * x + Parameter[5])
					/ Parameter[2];
			double[] y = pqFormel(p, q);

			tmp1 = new Point2(x, y[0]); // der obere neue Punkt
			tmp2 = new Point2(x, y[1]); // der untere neue Punkt

			// Da bei gekippten Ellipsen der angenommene x-Wert außerhalb der Ellipse liegen kann, 
			// muß das zuerst überprüft werden
			// zuerst für die obere Koordinate
			if (F1.distance(tmp1) + F2.distance(tmp1) <= 2 * xRadius + 0.5) // die 0.5 dient zum aufrunden, sonst wie definiert
			{
				if (!Angefangen)
				{ // bis zum ersten echten Punkt sind xOld und yOlrArr==0, was zu einer Linie zum Urspung führen würde
					xOld = x;
					yOldArr[0] = (int) y[0];
					yOldArr[1] = (int) y[1];
					Angefangen = true;
					g2d.drawLine(x, (int) y[0], xOld, (int) yOldArr[1]);
				}
				merken1 = true; // Wenn wir uns hier schon die xOld und yOld merken, funktioniert die nächste Linie nicht mehr
				// also merken wir uns hier nur, daß noch etwas zu tun ist

				g2d.drawLine(x, (int) y[0], xOld, (int) yOldArr[0]);
			}

			// dann noch für die untere Koordinate
			if (F1.distance(tmp2) + F2.distance(tmp2) <= 2 * xRadius)
			{
				g2d.drawLine(x, (int) y[1], xOld, (int) yOldArr[1]);
				ausgang = true;
				xOld = x;
				yOldArr[1] = (int) y[1];
			}
			else if (ausgang)
			{
				g2d.drawLine(x, (int) yOldArr[0], x, (int) yOldArr[1]);
				ausgang = false;
			}

			if (merken1)
			{ // Jetzt müssen die aktuellen Werte gespeichert werden, damit nächste Runde die Linie gezogen werden kann
				xOld = x;
				yOldArr[0] = (int) y[0];
			}
			merken1 = false; // nur merken, falls die Koordinate auch in der Ellipse liegt, sonst sieht es doof aus
		}

		if (ausgang) // dann liegt die Ellipse waagerecht und muß am rechten Rand noch geschlossen werden
			g2d.drawLine(xOld, yOldArr[0], xOld, yOldArr[1]);

		// Mit folgender Schleife wäre es möglich, umgekehrt zu den Y-Werten die X-Werte zu berechnen. Das ist vor allem
		// dann sinnvoll, wenn nur Punkte und keine Linien gezeichnet werden. Sonst gibt es an Stellen mit großer
		// Steigung zu große Lücken zwischen den Punkten
		/*for (int y = yStart; y < yEnd; y ++)
		{
			double p = (Parameter[1]*y + Parameter[3]) / Parameter[0];
			double q = (Parameter[2]*y*y + Parameter[4]*y + Parameter[5])/ Parameter[0];
			double[] x = pqFormel(p, q);
			tmp1 = new Point2(x[0], y);
			tmp2 = new Point2(x[1], y);

			if (ok)
			{
			    if (F1.distance(tmp1) + F2.distance(tmp1) <= 2*xRadius)
				    g2d.drawLine((int)x[0], y, (int)x[0], y);   //drawLine((int)x[0], (int)y, xOld, (int)yOldArr[0]);
			    if (F1.distance(tmp2) + F2.distance(tmp2) <= 2*xRadius)
				    g2d.drawLine((int)x[1], y, (int)x[1], y); //   drawLine((int)x[1], (int)y, xOld, (int)yOldArr[1]);

			
			}
			yOld = y;
			xOldArr[0] = (int)x[0];
			xOldArr[1] = (int)x[1];
			ok = true;
		}
		*/

		// Zum Schluss werden die alten Einstellungen zurückgeholt
		g2d.setBackground(_last_gc.getBackgroundColor());
		g2d.setColor(_last_gc.getFillColor());
		g2d.setStroke((Stroke) last_stroke);
	}


	/**
	 * Setzt einen Transformer für dieses Objekt
	 * 
	 * @param trans
	 *            Der Transformer
	 * 
	 * @see anja.swinggui.WorldCoorTransformer
	 */
	public void setTransformer(
			anja.swinggui.WorldCoorTransformer trans)
	{
		transformer = trans;
	}


	/**
	 * Berechnet die Nullstellen eines biquadratischen Polynoms x4*x^4 + x3*x3 +
	 * x2*x^2 + x1*x + x0 = 0
	 * 
	 * @param xx0
	 *            Die Koeffizienten des Polynoms (muss nicht in Normalform
	 *            vorliegen)
	 * @param xx1
	 *            Die Koeffizienten des Polynoms (muss nicht in Normalform
	 *            vorliegen)
	 * @param xx2
	 *            Die Koeffizienten des Polynoms (muss nicht in Normalform
	 *            vorliegen)
	 * @param xx3
	 *            Die Koeffizienten des Polynoms (muss nicht in Normalform
	 *            vorliegen)
	 * @param xx4
	 *            Die Koeffizienten des Polynoms (muss nicht in Normalform
	 *            vorliegen)
	 * 
	 * @return Array der Nullstellen
	 */
	private double[] getRootsForQuartic(
			double xx0,
			double xx1,
			double xx2,
			double xx3,
			double xx4)
	{
		double[] result;// = new double[4];

		// zunächst benötigen wir die Normalform. Das erreichen wir durch Division mit x4
		double x0 = xx0 / xx4;
		double x1 = xx1 / xx4;
		double x2 = xx2 / xx4;
		double x3 = xx3 / xx4;

		// Jetzt formen wir zu y^4 + py^2 + qy + r = 0 um, so daß wir das kubische Glied loswerden
		double p = x2 - 3 * x3 * x3 / 8;
		double q = x3 * x3 * x3 / 8 - x3 * x2 / 2 + x1;
		double r = -(3 * x3 * x3 * x3 * x3 - 16 * x3 * x3 * x2 + 64 * x3 * x1 - 256 * x0) / 256;

		// Nun wird die kubische Resolvente berechnet
		// wir erhalten eine kubische Gleichung in Normalform   x^3 + rx^2 + sx + t = 0
		//r = -2*p;
		double s = p * p - 4 * r;
		double t = q * q;

		// diese kubische Gleichung wird nun in die reduzierte Form  y^3 + py + q  gebracht
		r = -2 * p;
		double pneu = s - r / 3 * r;
		double qneu = (r * r / 27 * r * 2 - r * s / 3) + t;

		double R = qneu / 4 * qneu + pneu / 27 * pneu * pneu;

		double z1 = 0;
		double z2 = 0;
		double z3 = 0;
		if (R >= 0)
		{
			// Es gibt zwei Schnittpunkte; wir wenden das Verfahren von Cardano an
			result = new double[2];
			double T = Math.sqrt(R); //Math.sqrt(q/4*q + p/27*p*p)

			double u, v;
			if (-qneu / 2 + T < 0)
				u = -1 * Math.pow(qneu / 2 - T, 1.0 / 3.0);
			else
				u = Math.pow(-qneu / 2 + T, 1.0 / 3.0);
			if (-qneu / 2 - T < 0)
				v = -1 * Math.pow(qneu / 2 + T, 1.0 / 3.0);
			else
				v = Math.pow(-qneu / 2 - T, 1.0 / 3.0);

			double y1 = u + v;
			double y2real = -0.5 * y1;
			double y2imaginaer = -0.5 * (u - v)
					* 1.7320508075688772935274463415059;
			double y3real = y2real;
			double y3imaginaer = -1 * y2imaginaer;

			z1 = y1 - r / 3;
			z2 = y2real - r / 3;
			z3 = y3real - r / 3;

			//
			// Schnitt.
			//
			double z1real = z1;
			double z1imaginaer = 0;
			double z2real = z2;
			double z2imaginaer = y2imaginaer;
			double z3real = z3;
			double z3imaginaer = y3imaginaer;

			// Test mit neuer Implementierung
			// Die Problematik ist, die richtigen Vorzeichen der Wurzeln zu finden, da es hier 32 Mögichkeiten gibt			

			// Die Vorzeichen der Wurzeln müssen so gewählt werden, daß deren Produkt gleich -qneu ist
			double[] Wurzel1 = getComplexRoot(-z1real, -z1imaginaer);
			double[] Wurzel2 = getComplexRoot(-z2real, -z2imaginaer);
			double[] Wurzel3 = getComplexRoot(-z3real, -z3imaginaer);

			// Wir haben nun aus drei möglicherweise komplexen Zahlen die Wurzeln gezogen und haben 2*2*2 Möglichkeiten, die Vorzeichen zu setzen.
			// Die Aufgabe besteht darin, eine Vorzeichenkombination zu finden, deren Produkt gleich -q ist.
			double[] tmp1 = new double[2];
			double[] tmp2 = new double[2];
			;
			double[] tmp3 = new double[2];
			;

			// Idee: Für jede Wurzel gibt es zwei Ergebnisse mit komplementären Vorzeichen. Wir müssen alle Kombinationen von Vorzeichen testen,
			//       um die zu finden, deren Produkt gleich -q ist
			// Da es für jede Wurzel genau zwei Möglichkeiten zur Auswahl gibt, gehe ich die Binärdarstellungen aller Zahlen von 0-7 durch
			// Eine 1 an z.B. der zweiten Stelle bedeutet, daß für die zweite Wurzel ein negatives Vorzeichen gewählt wird
			double[] product = new double[2];
			int i;
			for (i = 0; i < 8; i++)
			{
				if ((i & 1) == 0)
				{
					tmp1[0] = Wurzel1[0];
					tmp1[1] = Wurzel1[1];
				}
				else
				{
					tmp1[0] = Wurzel1[2];
					tmp1[1] = Wurzel1[3];
				}
				if (((i >> 1) & 1) == 0)
				{
					tmp2[0] = Wurzel2[0];
					tmp2[1] = Wurzel2[1];
				}
				else
				{
					tmp2[0] = Wurzel2[2];
					tmp2[1] = Wurzel2[3];
				}
				if (((i >> 2) & 1) == 0)
				{
					tmp3[0] = Wurzel3[0];
					tmp3[1] = Wurzel3[1];
				}
				else
				{
					tmp3[0] = Wurzel3[2];
					tmp3[1] = Wurzel3[3];
				}

				product = getComplexProduct(getComplexProduct(tmp1, tmp2), tmp3);
				if (product[0] * (-1 * q) > 0)
					break;
			}

			double y1real = (tmp1[0] + tmp2[0] + tmp3[0]) / 2;
			//		double y1im   = (tmp1[1] + tmp2[1] + tmp3[1]) / 2; 
			y2real = (tmp1[0] - tmp2[0] - tmp3[0]) / 2;
			/*			double y2im   = (tmp1[1] - tmp2[1] - tmp3[1]) / 2; 
						y3real        = (-1*tmp1[0] + tmp2[0] - tmp3[0]) / 2; 
						double y3im   = (-1*tmp1[1] + tmp2[1] - tmp3[1]) / 2; 
						double y4real = (-1*tmp1[0] - tmp2[0] + tmp3[0]) / 2; 
						double y4im   = (-1*tmp1[1] - tmp2[1] + tmp3[1]) / 2; 
			*/
			result[0] = y1real - x3 / 4;
			result[1] = y2real - x3 / 4;

			return result;

		}
		else
		{
			/* Wir haben es hier mit dem "Casus irreducibilis" zu tun => Es gibt vier Nullstellen. 
			 * Zunaechst kuemmern wir uns um die Loesung der kubischen Gleichungm, 
			 * deren drei reelle Lösungen wir mithilfe trigonometrischer Funktionen finden werden
			 */
			result = new double[4];
			double u = Math.sqrt(-1 * (pneu / 27 * pneu * pneu));
			double cosw = -1 * qneu / 2 / u;
			double w = Math.acos(cosw);

			double y1 = 2 * Math.pow(u, 1.0 / 3.0) * Math.cos(w / 3);
			double y2 = 2 * Math.pow(u, 1.0 / 3.0)
					* Math.cos(w / 3 + 2.0943951023931954923084289221863);
			double y3 = 2 * Math.pow(u, 1.0 / 3.0)
					* Math.cos(w / 3 + 4.1887902047863909846168578443727);

			// Jetzt muß die Substition x=y-r/3 wieder rückgängig gemacht werden, um die Lösungen der kubischen Gleichung zu erhalten 
			z1 = y1 - r / 3;
			z2 = y2 - r / 3;
			z3 = y3 - r / 3;

			// Schließlich muß noch die biquadratische Gleichung gelöst werden
			// Dazu müssen wir erst die Vorzeichen der Wurzeln herausfinden. Das Produkt der Wurzeln muß gleich -q sein
			double WurzelZ1 = Math.sqrt(-z1);
			double WurzelZ2 = Math.sqrt(-z2);
			double WurzelZ3 = Math.sqrt(-z3);
			// Aus den vielen (6) verschiedenen Wurzeln müssen wir eine Kombination auswählen, deren Produkt gleich p ist
			// Da es nur um das Vorzeichen geht, reicht, es die erste Wurzel mit -1 zu multiplizieren
			if (Math.abs(WurzelZ1 * WurzelZ2 * WurzelZ3 + q) > Math.abs(-1
					* WurzelZ1 * WurzelZ2 * WurzelZ3 + q))
				WurzelZ1 *= -1;

			// Die Lösungen des Gleichungssystems sehen wie folgt aus
			result[0] = (WurzelZ1 + WurzelZ2 + WurzelZ3) / 2;
			result[1] = (WurzelZ1 - WurzelZ2 - WurzelZ3) / 2;
			result[2] = (-WurzelZ1 + WurzelZ2 - WurzelZ3) / 2;
			result[3] = (-WurzelZ1 - WurzelZ2 + WurzelZ3) / 2;

			// zuletzt wird noch die erste Substitution rückgängig gemacht
			result[0] -= x3 / 4;
			result[1] -= x3 / 4;
			result[2] -= x3 / 4;
			result[3] -= x3 / 4;

			return result;
		}

	}


	/**
	 * Berechnet zu der angegeben komplexen Zahl die Quadratwurzel
	 * 
	 * @param real
	 *            Realteil
	 * @param imaginaer
	 *            Imaginaerteil
	 * 
	 * @return Die Quadratwurzel
	 */
	private double[] getComplexRoot(
			double real,
			double imaginaer)
	{
		double[] result = new double[4];

		// Im Dienste der Genauigkeit führen wir eine Fallunterscheidung durch, um so in Spezialfällen Rundungsfehler auszuschließen

		// 1. Fall: Einfache reelle Wurzel
		if (real >= 0 && imaginaer == 0)
		{
			result[0] = Math.sqrt(real);
			result[1] = 0;
			result[2] = -1 * result[0];
			result[3] = 0;
			return result;
		}

		// 2. Fall: Wurzel aus negativer reeler Zahl
		if (imaginaer == 0)
		{
			result[0] = 0;
			result[1] = Math.sqrt(-real);
			result[2] = 0;
			result[3] = -1 * result[1];
			return result;
		}

		// 3. Fall: reguläre Wurzel aus komplexer Zahl mit positivem Realteil
		double r = Math.sqrt(real * real + imaginaer * imaginaer);
		double TanPhi = imaginaer / real;
		double phi = Math.atan(TanPhi);

		result[0] = Math.sqrt(r) * Math.cos(phi / 2);
		result[1] = Math.sqrt(r) * Math.sin(phi / 2);
		result[2] = Math.sqrt(r) * Math.cos((phi + 2 * java.lang.Math.PI) / 2);
		result[3] = Math.sqrt(r) * Math.sin((phi + 2 * java.lang.Math.PI) / 2);

		// 4. Fall: reguläre Wurzel aus komplexer Zahl mit negativem Realteil (es müssen nur Imaginaer- und Realteil getauscht werden
		if (real < 0)
		{
			double tmp = result[0];
			result[0] = result[1];
			result[1] = tmp;
			tmp = result[2];
			result[2] = result[3];
			result[3] = tmp;
			if (imaginaer > 0)
			{
				result[0] *= -1;
				result[2] *= -1;
			}
			else
			{
				result[1] *= -1;
				result[3] *= -1;
			}
		}

		return result;
	}


	/**
	 * Berechnet das Produkt der beiden komplexen Zahlen
	 * 
	 * Zahl1 = a[0]+a[1]i Zahl2=b[0]+b[1]i
	 * 
	 * @param a
	 *            Die erste komplexe Zahl
	 * @param b
	 *            Die zweite komplexe Zahl
	 * 
	 * @return komplexe Zahl der Form result[0] + result[1]*i
	 */
	private double[] getComplexProduct(
			double[] a,
			double[] b)
	{
		double result[] = new double[2];

		result[0] = a[0] * b[0] - a[1] * b[1];
		result[1] = a[0] * b[1] + a[1] * b[0];

		return result;
	}


	/**
	 * Textuelle Repräsentation erzeugen
	 * 
	 * @return Textuelle Repräsentation
	 */
	public String toString()
	{
		return "(F1=" + this.F1 + "; F2=" + this.F2 + "; Phi=" + this.PhiDeg
				+ ")";
	}


	/**
	 * TODO: Was wird berechnet?
	 * 
	 * @param Left
	 *            Punkt 1
	 * @param Right
	 *            Punkt 2
	 * 
	 * @return Ein Polygon
	 */
	static public Polygon2 getRestrictingPointsForEllipse(
			Point2 Left,
			Point2 Right)
	{
		//Point2[] result = new Point2[360];
		Polygon2 result = new Polygon2();
		for (int i = 0; i < 360; i++)
		{
			double rad = (double) i * Math.PI / (double) 180f;
			double c = Math.cos(rad);// * Left.distance(Right);
			double h = Math.sin(rad);// * Left.distance(Right);
			double d1 = (-72 * c + 60)
					/ 22
					+ Math.sqrt((((-72 * c + 60) / 22) * ((-72 * c + 60) / 22) - (36
							* c * c - 60 * c + 25 - 25 * h * h) / 11));

			double b = c + d1;
			Point2 vektor = new Point2((float) (Math.cos(rad) * b),
					(float) (Math.sin(rad) * b));

			// Nun haben wir den Vektor in der normierten Umgebung berechnet. Diese Normierung/Drehung wird jetzt rückgängig gemacht
			double drehung = Left.angle(Right);
			double cosDrehung = Math.cos(drehung);
			double sinDrehung = Math.sin(drehung);
			double dist = Left.distance(Right);
			result.addPoint(new Point2(
					(float) ((cosDrehung * vektor.x - sinDrehung * vektor.y)
							* dist + Left.x),
					(float) ((sinDrehung * vektor.x + cosDrehung * vektor.y)
							* dist + Left.y)));

		}
		return result;
	}


	/**
	 * TODO: Nicht implementiert
	 * 
	 * @return false
	 */
	public boolean intersects(
			Rectangle2D box)
	{
		// TODO Vererbt von Drawable, muss noch erstellt werden.
		return false;
	}


	// **************************************************
	// Private Methods
	// **************************************************

	/**
	 * Berechnet die Lösung zum im Konstruktor angegeben LGS
	 * 
	 * @param z
	 *            Das lineare GLeichungssystem
	 * 
	 * @return Lösungsvektor als double[]
	 */
	private double[] getLGSLoesung(
			double[][] z)
	{
		double[] L = new double[z[0].length]; // Lösungsvektor

		// Alle Zeilen bzw Gleichungen bearbeiten
		for (int Durchlauf = 0; Durchlauf < z[0].length - 1; Durchlauf++)
		{
			// Zuerst wird das Pivotelement normiert
			for (int i = Durchlauf + 1; i < z[0].length; i++)
				z[Durchlauf][i] /= z[Durchlauf][Durchlauf];
			z[Durchlauf][Durchlauf] = 1;

			// "Durchlauf"-tes Elemente der übrigen Zeilen auf 0 bringen 		
			for (int j = 0; j < z.length; j++)
			{
				if (j == Durchlauf)
					continue;
				double faktor = z[j][Durchlauf];

				// Hier werden die Variablen der "Durchlauf"-ten Gleichung bearbeitet
				for (int i = Durchlauf; i < z[0].length; i++)
				{
					z[j][i] -= faktor * z[Durchlauf][i];
				}
			}
		} // for-Durchlauf		

		// Zuletzt muß nur noch der Lösungsvektor ausgelesen werden
		for (int i = 0; i < z[0].length - 1; i++)
			L[i] = z[i][z[0].length - 1];

		return L;
	}

}
