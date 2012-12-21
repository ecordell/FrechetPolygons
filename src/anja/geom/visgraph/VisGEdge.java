package anja.geom.visgraph;


import anja.geom.Segment2;
import anja.geom.Point2;


/**
 * Diese Klasse hat die gleiche Funktionalitaet, wie das Segment2. Es wurden
 * hauptsaechlich benutzt, um in der Klasse {@link anja.geom.visgraph.VisGraph}
 * die Typumwandlungen von {@link anja.geom.Point2} zu
 * {@link anja.geom.visgraph.VisGVertice} zu vermeiden.
 * 
 * <br>Es wurden auch einige fuer VisGraph nuetzliche Methoden implementiert.
 * 
 * <br>Wird von der SweepLine und VisGraph benutzt.
 * 
 * @author Alexande Tiderko
 * 
 **/
public class VisGEdge
		extends Segment2
{

	/**
	 * Erzeugt ein Segment vom ersten Eingabepunkt zum zweiten Eingabepunkt, das
	 * Segment merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>die Eingabepunkte sind ungleich null
	 * 
	 * @param src
	 *            der Startpunkt
	 * @param trg
	 *            der Zielpunkt
	 */
	public VisGEdge(
			VisGVertice src,
			VisGVertice trg)
	{
		super(src, trg);
	}


	//*************************************************************************

	/**
	 * Erzeugt ein Segment vom ersten Eingabepunkt zum zweiten Eingabepunkt, das
	 * Segment merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>die Eingabepunkte sind ungleich null
	 * 
	 * @param src
	 *            der Startpunkt
	 * @param trg
	 *            der Zielpunkt
	 */
	public VisGEdge(
			Point2 src,
			VisGVertice trg)
	{
		super(src, trg);
	}


	//*************************************************************************

	/**
	 * Erzeugt ein Segment vom ersten Eingabepunkt zum zweiten Eingabepunkt, das
	 * Segment merkt sich <B>Referenzen</B> auf diese Punkte.
	 * 
	 * <BR><B>Vorbedingungen:</B><BR>die Eingabepunkte sind ungleich null
	 * 
	 * @param src
	 *            der Startpunkt
	 * @param trg
	 *            der Zielpunkt
	 */

	public VisGEdge(
			Point2 src,
			Point2 trg)
	{
		super(src, trg);
	}


	//*************************************************************************

	/**
	 * Liefert eine <B>Referenz</B> auf den Startpunkt.
	 * 
	 * @return den Startpunkt
	 */
	public VisGVertice get_source()
	{
		return (VisGVertice) (_source);
	}


	//*************************************************************************

	/**
	 * Liefert eine <B>Referenz</B> auf den Endpunkt.
	 * 
	 * @return den Endpunkt
	 */
	public VisGVertice get_target()
	{
		return (VisGVertice) (_target);
	}


	//*************************************************************************

	/**
	 * Liefert die <B>Referenz</B> auf den linken Endpunkt des Segmentes
	 * zurueck. Ist das Segment senkrecht, so wird der obere Endpunkt
	 * zurueckgegeben.
	 * 
	 * @return VisGVertice der linken Endpunkt
	 */
	public VisGVertice getLeft()
	{
		if (get_source().x < get_target().x)
			return get_source();

		else if ((get_source().x == get_target().x)
				&& (get_source().y > get_target().y))
			return get_source();
		else
			return get_target();
	}


	//*************************************************************************

	/**
	 * Liefert die <B>Referenz</B> auf den rechten Endpunkt des Segmentes
	 * zurueck. Ist das Segment senkrecht, so wird der untere Endpunkt
	 * zurueckgegeben.
	 * 
	 * @return VisGVertice der rechte Endpunkt
	 */
	public VisGVertice getRight()
	{
		if (get_source().x > get_target().x)
			return get_source();
		else if ((get_source().x == get_target().x)
				&& (get_source().y < get_target().y))
			return get_source();
		else
			return get_target();
	}

	//*************************************************************************

}
