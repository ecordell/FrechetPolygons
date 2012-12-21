package anja.geom;


/*
import java_ersatz.java2d.BezierPath;
import java_ersatz.java2d.Graphics2D;
import java_ersatz.java2d.Rectangle2D;*/

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

//import java_ersatz.java2d.BezierPath;

import anja.util.Drawable;
import anja.util.FloatUtil;
import anja.util.GraphicsContext;


/**
 * Zweidimensionales zeichenbares achsenparalleles Rechteck.
 * 
 * @version 0.2 12.11.2003
 * @author Norbert Selle, Sascha Ternes
 */

public class Rectangle2
		extends Rectangle2D.Float
		implements Cloneable, Drawable
{

	// ************************************************************************
	// Constants
	// ************************************************************************

	// Variables
	// ************************************************************************

	// ************************************************************************
	// Constructors
	// ************************************************************************

	/**
	 * Erzeugt ein neues Rechteck an der Position (0, 0) mit der Breite 0 und
	 * der Hoehe 0.
	 */
	public Rectangle2()
	{
		super();

	} // Rectangle2


	// ********************************              

	/**
	 * Erzeugt ein neues Rechteck und initialisiert es mit den Daten des
	 * Eingaberechtecks.
	 * 
	 * @param input_rectangle
	 *            Eingaberechteck
	 */

	public Rectangle2(
			Rectangle2 input_rectangle)
	{
		super(input_rectangle.x, input_rectangle.y, input_rectangle.width,
				input_rectangle.height);

	} // Rectangle2


	// ********************************              

	/**
	 * Erzeugt ein neues Rechteck und initialisiert es mit den Daten eines
	 * Rectangle2D.
	 * 
	 * @param input_rectangle2D
	 *            Eingaberechteck
	 */

	public Rectangle2(
			Rectangle2D input_rectangle2D)
	{
		super();
		super.setRect(input_rectangle2D);

		//super( input_rectangle2D );

	} // Rectangle2


	// ********************************              

	/**
	 * Erzeugt ein neues Rechteck an der Position (0, 0) mit der eingegebenen
	 * Breite und Hoehe.
	 * 
	 * @param input_width
	 *            Breite
	 * @param input_height
	 *            Hoehe
	 */

	public Rectangle2(
			float input_width,
			float input_height)
	{
		super(0.0f, 0.0f, input_width, input_height);
	} // Rectangle2


	// ********************************              

	/**
	 * Erzeugt ein neues Rechteck und initialisiert es mit den
	 * Eingabeparametern.
	 * 
	 * @param input_x
	 *            x-Koordinate
	 * @param input_y
	 *            y-Koordinate
	 * @param input_width
	 *            Breite
	 * @param input_height
	 *            Hoehe
	 */

	public Rectangle2(
			float input_x,
			float input_y,
			float input_width,
			float input_height)
	{
		super(input_x, input_y, input_width, input_height);

	} // Rectangle2


	// ********************************              

	/**
	 * Erzeugt ein neues Rechteck, welches durch die beiden Punkte definiert
	 * ist.
	 * 
	 * @param downLeft
	 *            Punkt unten links
	 * @param upRight
	 *            Punkt oben rechts
	 */

	public Rectangle2(
			Point2 downLeft,
			Point2 upRight)
	{
		super(downLeft.x, downLeft.y, Math.abs(upRight.x - downLeft.x), Math
				.abs(upRight.y - downLeft.y));

	} // Rectangle2


	// ************************************************************************
	// Class methods
	// ************************************************************************

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	 * Liefert den Mittleren X-Wert
	 * 
	 * @return Mittlerer X-Wert
	 */
	public double getMidX()
	{
		return getMinX() + ((getMaxX() - getMinX()) / 2.0);
	}


	/**
	 * Liefert den Mittleren Y-Wert
	 * 
	 * @return Mittlerer Y-Wert
	 */
	public double getMidY()
	{
		return getMinY() + ((getMaxY() - getMinY()) / 2.0);
	}


	/**
	 * Gibt die untere Kante des Rechtecks zurueck, 'unten' ist die Kante mit
	 * der kleineren y-Koordinate.
	 * 
	 * @return Die untere Kante
	 */
	public Segment2 bottom()
	{
		if (height < 0)
		{
			return (new Segment2(x, y + height, x + width, y + height));
		}
		else
		{
			return (new Segment2(x, y, x + width, y));
		} // if

	} // bottom


	/**
	 * Liefert das Zentrum des Rechtecks
	 * 
	 * @return Das Zentrum - der Mittelpunkt
	 */
	public Point2 getCenter()
	{
		return new Point2(getCenterX(), getCenterY());
	}


	// ************************************************************************

	/**
	 * Erzeugt eine Kopie und gibt sie zurueck.
	 * 
	 * @return Eine Kopie dieses Rechtecks
	 */
	public Object clone()
	{
		return (new Rectangle2(this));

	} // clone


	// ************************************************************************

	/**
	 * Zeichnet das Rechteck.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);

		if ((width == 0) && (height == 0))
		{
			(new Point2(x, y)).draw(g, gc);
		}
		else
		{
			path.moveTo(x, y);
			path.lineTo(x + width, y);
			path.lineTo(x + width, y + height);
			path.lineTo(x, y + height);
			path.closePath();

			g.setStroke(gc.getStroke());

			if (gc.getFillStyle() != 0)
			{
				g.setColor(gc.getFillColor());
				g.fill(path);
			} // if

			g.setColor(gc.getForegroundColor());
			g.draw(path);
		} // if

	} // draw


	// ************************************************************************

	/**
	 * Testet ob das Eingaberechteck geschnitten wird.
	 * 
	 * @param input_rectangle
	 *            Das Eingaberechteck
	 * 
	 * @return true bei Schnitt, false sonst
	 */
	public boolean intersects(
			Rectangle2D input_rectangle)
	{
		return (super.intersects(input_rectangle));

	} // intersects


	/**
	 * Sets the rectangle based on source and target corners. Prior to that, the
	 * code performs some comparisons of the corners' coordinates, so that the
	 * rectangle is always correctly specified in world coordinates, independent
	 * of the spatial relationship of the corner points.<br><br>
	 * 
	 * @param source
	 *            source corner of the new rectangle
	 * @param target
	 *            target corner of the new rectangle
	 */
	public void setFromPoints(
			Point2 source,
			Point2 target)
	{

		float xx, yy;

		if (target.y < source.y)
		{
			yy = target.y;
		}
		else
		{
			yy = source.y;
		}

		if (target.x < source.x)
		{
			xx = target.x;
		}
		else
		{
			xx = source.x;
		}

		setFrame(xx, yy, Math.abs(target.x - source.x), Math.abs(target.y
				- source.y));

	}


	// ************************************************************************

	/**
	 * Gibt die linke Kante zurueck, 'links' ist die Kante mit der kleineren
	 * x-Koordinate.
	 * 
	 * @return Gibt die linkeste Kante zurück
	 */
	public Segment2 left()
	{
		if (width < 0)
		{
			return (new Segment2(x + width, y, x + width, y + height));
		}
		else
		{
			return (new Segment2(x, y, x, y + height));
		} // if

	} // left


	// ************************************************************************

	/**
	 * Gibt die rechte Kante zurueck, 'rechts' ist die Kante mit der groesseren
	 * x-Koordinate.
	 * 
	 * @return Die rechteste Kante zurück
	 */
	public Segment2 right()
	{
		if (width < 0)
		{
			return (new Segment2(x, y, x, y + height));
		}
		else
		{
			return (new Segment2(x + width, y, x + width, y + height));
		} // if

	} // right


	// ************************************************************************

	/**
	 * Gibt die obere Kante des Rechtecks zurueck, 'oben' ist die Kante mit der
	 * groesseren y-Koordinate.
	 * 
	 * @return Die oberste Kante
	 */
	public Segment2 top()
	{
		if (height < 0)
		{
			return (new Segment2(x, y, x + width, y));
		}
		else
		{
			return (new Segment2(x, y + height, x + width, y + height));
		} // if

	} // top


	// ************************************************************************

	/**
	 * Erzeugt eine textuelle Repraesentation des Rechtecks.
	 * 
	 * @return Die textuelle Repräsentation
	 */
	public String toString()
	{
		return (new String("(" + "(" + FloatUtil.floatToString(x) + ", "
				+ FloatUtil.floatToString(y) + ")" + " "
				+ FloatUtil.floatToString(width) + " x "
				+ FloatUtil.floatToString(height) + ")"));

	} // toString


	// ************************************************************************

	/**
	 * Verschiebt das Rechteck um die Eingabewerte.
	 * 
	 * @param input_horizontal
	 *            horizontale Verschiebung
	 * @param input_vertical
	 *            vertikale Verschiebung
	 */
	public void translate(
			float input_horizontal,
			float input_vertical)
	{
		x = x + input_horizontal;
		y = y + input_vertical;

	} // translate


	// ********************************

	/**
	 * Verschiebt das Rechteck um den Vektor vom Nullpunkt zum Eingabepunkt.
	 * 
	 * @param input_point
	 *            Eingabepunkt
	 */
	public void translate(
			Point2 input_point)
	{
		if (input_point != null)
		{
			translate(input_point.x, input_point.y);
		} // if

	} // translate


	/**
	 * Testet, ob das Rechteck den spezifizieren Punkt einschliesst.
	 * 
	 * @param point
	 *            der Testpunkt
	 * 
	 * @return <code>true</code>, falls der spezifizierte Punkt innerhalb des
	 *         Rechtecks oder auf einer seiner Kanten oder Eckpunkte liegt,
	 *         <code>false</code>, falls der Punkt ausserhalb liegt
	 */
	public boolean contains(
			Point2 point)
	{

		return super.contains(point.x, point.y);

	} // contains


	/* *********************************************************************************
	 * Note : The following three Methods were recycled from deprecated
	 * java_ersatz.java2D.Rectangle2D, used formerly with this library. As they
	 * did interpret touching Rectangles as intersecting ones, in contrast to
	 * java.awt.geom.Rectangle2D ( now standard in this library ), they are no
	 * longer kept as "intersects"-methods, but as
	 * "intersectsOrTouches"-methods. 11-10-2004, Birgit Engels
	 *********************************************************************************/

	/**
	 * Tests, if Rectangle2 r intersects or touches this Rectangle2
	 * 
	 * @param r
	 *            The other rectangle
	 * 
	 * @return true if r intersects or touches this, false if r neither
	 *         intersects nor touches this !
	 */
	public boolean intersectsOrTouches(
			Rectangle2 r)
	{
		return intersectsOrTouches((Rectangle2D) r);
	}


	/**
	 * Tests, if Rectangle2D r intersects or touches this Rectangle2
	 * 
	 * @param r
	 *            The other rectangle
	 * 
	 * @return true if r intersects or touches this, false if r neither
	 *         intersects nor touches this !
	 */
	public boolean intersectsOrTouches(
			Rectangle2D r)
	{
		return !(Math.min(r.getX(), r.getX() + r.getWidth()) > Math.max(x, x
				+ width)
				|| Math.max(r.getX(), r.getX() + r.getWidth()) < Math.min(x, x
						+ width)
				|| Math.min(r.getY(), r.getY() + r.getHeight()) > Math.max(y, y
						+ height) || Math.max(r.getY(), r.getY()
				+ r.getHeight()) < Math.min(y, y + height));
	}


	/**
	 * Tests, if Rectangle2D r1 intersects or touches Rectangle2D r2
	 * 
	 * @param r1
	 *            Rectangle 1
	 * @param r2
	 *            Rectangle 2
	 * 
	 * @return true if r1 intersects or touches r2, false if r1 neither
	 *         intersects nor touches r2!
	 */

	public boolean intersectsOrTouches(
			Rectangle2D r1,
			Rectangle2D r2)
	{
		Rectangle2 rect = new Rectangle2(r1);
		return rect.intersectsOrTouches(r2);

	}

	/* *********************************************************************************
	 * End of recycled method block ;) 11-10-2004, Birgit Engels
	 *********************************************************************************/

	// ************************************************************************
	// Private methods
	// ************************************************************************

} // class Rectangle2

