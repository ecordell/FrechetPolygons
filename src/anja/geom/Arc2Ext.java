package anja.geom;


import java.awt.Graphics2D;

import anja.geom.Arc2;
import anja.geom.Point2;
import anja.util.Drawable;
import anja.util.GraphicsContext;


/**
 * Diese Klasse basiert auf Arc2Ext. Im Vergleich zur Arc2 Klasse beinhaltet
 * diese Klasse Referenzen, auf _source und _target. Dadurch ist es einfacher
 * zusammenhaengende Ketten von Objekten zu erzeugen, deren source und traget
 * Punkte identisch sein sollen.
 * 
 * 
 * @author Darius Geiss
 * 
 */
public class Arc2Ext
		extends Arc2
		implements Drawable, Cloneable
{

	/** Anfangspunkt des Bogens */
	private Point2	_source	= new Point2();

	/** Endpunkt des Bogens */
	private Point2	_target	= new Point2();


	/**
	 * Copy-Konstruktor
	 * 
	 * @param a
	 *            Das zu kopierende Objekt
	 */
	public Arc2Ext(
			Arc2Ext a)
	{
		super(a);
		_source = (Point2) a.source().clone();
		_target = (Point2) a.target().clone();
		update();
	}


	/**
	 * Erzeugt einen Arc2Ext aus einem Arc2. "source" und "target" dienen als
	 * Referenz für Start- und End-Punkt des Bogens. Gegebenenfalls werden die
	 * Werte der Punkte korrigiert, damit sie mit den Werten des Bogens
	 * übereinstimmen.
	 * 
	 * @param a
	 *            Arc2-Bogen
	 * @param source
	 *            der Startpunkt des Kreisbogens gleichzeitig auch Referenz
	 * @param target
	 *            der Endpunkt des Kreisbogens gleichzeitig auch Referenz
	 */
	public Arc2Ext(
			Arc2 a,
			Point2 source,
			Point2 target)
	{
		super(a);
		_source = source;
		_target = target;
		update();
	}


	/**
	 * Erzeugt einen Bogen. "source" und "target" dienen als Referenz für Start-
	 * und End-Punkt des Bogens. Gegebenenfalls werden die Werte der Punkte
	 * korrigiert, damit sie mit den Werten des Bogens übereinstimmen.
	 * 
	 * @param input_source
	 *            der Startpunkt des Kreisbogens gleichzeitig auch Referenz
	 * @param input_target
	 *            der Endpunkt des Kreisbogens gleichzeitig auch Referenz
	 * @param input_center
	 *            der Mittelpunkt des Kreises
	 * @param input_orientation
	 *            die Orientierung Angle.ORIENTATION_LEFT etc.
	 */
	public Arc2Ext(
			Point2 input_source,
			Point2 input_target,
			Point2 input_center,
			int input_orientation)
	{
		super(input_source, input_target, input_center, input_orientation);
		_source = input_source;
		_target = input_target;
		update();
	}


	/**
	 * Erzeugt einen Bogen, ohne Referenzen auf die Start- und End-Punkte. Die
	 * Punkte sind dennoch ueber source() und target() erreichbar.
	 * 
	 * @param input_centre
	 *            der Mittelpunkt des Kreises
	 * @param input_radius
	 *            der Radius des Bogens
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 * @param input_orientation
	 *            die Orientierung Angle.ORIENTATION_LEFT etc.
	 */
	public Arc2Ext(
			Point2 input_centre,
			double input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation)
	{
		super(input_centre, input_radius, input_source_angle,
				input_target_angle, input_orientation);
		update();
	}


	/**
	 * Erzeugt einen Bogen, mit Referenzen auf die Start- und End-Punkte. Die
	 * Punkte sind dennoch ueber source() und target() erreichbar.
	 * 
	 * @param input_centre
	 *            der Mittelpunkt des Kreises
	 * @param input_radius
	 *            der Radius des Bogens
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 * @param input_orientation
	 *            die Orientierung Angle.ORIENTATION_LEFT etc.
	 * @param source
	 *            Referenz auf den Startpunkt (Punkt wird neu berechnet)
	 * @param target
	 *            Referenz auf den Endpunkt (Punkt wird neu berechnet)
	 */
	public Arc2Ext(
			Point2 input_centre,
			double input_radius,
			double input_source_angle,
			double input_target_angle,
			int input_orientation,
			Point2 source,
			Point2 target)
	{
		super(input_centre, input_radius, input_source_angle,
				input_target_angle, input_orientation);
		_source = source;
		_target = target;
		update();
	}


	/**
	 * Clont den Bogen und die Referenzen auf source und target.
	 * 
	 * @return Arc2Ext
	 */
	public Object clone()
	{
		Arc2Ext arcClone = new Arc2Ext((Point2) this.centre.clone(),
				this.radius, this.sourceAngle(), this.targetAngle(), this
						.orientation(), (Point2) _source.clone(),
				(Point2) _target.clone());
		arcClone.setLabel(this.getLabel());
		return arcClone;
	}


	/**
	 * Update fuer die Referenzen auf den Start- und End-Punkt. Wird benoetigt
	 * beim erstellen und veraendern des Bogens.
	 */
	private void update()
	{
		_source.x = super.source().x;
		_source.y = super.source().y;
		_target.x = super.target().x;
		_target.y = super.target().y;
	}


	/**
	 * Setzt den Startwinkel.
	 * 
	 * @param input_source_angle
	 *            der Startwinkel in Bogenmass
	 */
	public void setSourceAngle(
			double input_source_angle)
	{
		super.setSourceAngle(input_source_angle);
		update();
	}


	/**
	 * Setzt den Endwinkel.
	 * 
	 * @param input_target_angle
	 *            der Endwinkel in Bogenmass
	 */
	public void setTargetAngle(
			double input_target_angle)
	{
		super.setTargetAngle(input_target_angle);
		update();
	}


	/**
	 * Setzt die Referenz des Start-Punktes auf p
	 * 
	 * @param p
	 *            Der neue Startpunkt
	 */
	public void setRefSource(
			Point2 p)
	{
		_source = p;
		update();
	}


	/**
	 * Setzt die Referenz des End-Punktes auf p
	 * 
	 * @param p
	 *            Der neue Endpunkt
	 */
	public void setRefTraget(
			Point2 p)
	{
		_target = p;
		update();
	}


	/**
	 * Anfangspunkt des Bogens
	 * 
	 * @return Der Anfangspunkt
	 */
	public Point2 source()
	{
		return _source;
	}


	/**
	 * Endpunkt des Bogens
	 * 
	 * @return Der Endpunkt
	 */
	public Point2 target()
	{
		return _target;
	}


	/**
	 * Zeichnet das Objekt.
	 * 
	 * @param g
	 *            Das Grafikobjekt, in das gezeichnet wird
	 * @param gc
	 *            Die dazugehörigen Formatierungsregeln
	 * 
	 */
	public void draw(
			Graphics2D g,
			GraphicsContext gc)
	{
		super.draw(g, gc);
	}

}
