/* File: Face.java
 * Created on: Sept. 1, 2005 by birgit
 * 
 */

package anja.graph;


import java.util.Iterator;


// ************************************************************************
//  Diskussionspunkte ;) :
// ************************************************************************
// Tja, das Face an sich erscheint mir noch ein wenig ueberfluessig...
// 
// 0. Referenzen
// Braucht es noch einen Referenz-Vertex ?
//
// 1. Problem 'equals'
// Da ein Face nicht alle Edges und Vertices speichert, die es
// begrenzen,
// benoetigt man einen Iterator, um an diese zu gelangen. Da ein
// solcher
// Iterator aber eine equals-Methode fuer Faces braucht (jedenfalls,
// wenn ich die
// Datenstruktur richtig verstanden habe), darf diese nicht auf einem
// Vergleich _aller_ Vertices und Edges der Face resultieren
// (zyklische Abhaengigkeit).
// Daher baut die equals-Methode hier lediglich auf dem gespeicherten
// _edge-Verweis
// auf (aehnlich wie bei Vertex und Edge selber kann dies auf das Label
// oder das
// Referenzobjekt erweitert werden, muss aber nicht...). Dabei stellt
// sich allerdings
// die Anforderung an die DCEL, dass keine 2 Faces die gleiche
// Referenz-Edge erhalten
// duerfen - Was theoretisch moeglich sein muesste - da sie sonst als
// 'identisch' gewertet
// werden. ... Oder ? ...
//
// 2. Geometrische Methoden
// bisher sind isInside(Vertex v), onBorder(Vertex v) und onBorder(Edge e) 
// nur Vorschlaege... Loesungen - wenn moeglich - ueber anja.geom ?
// 
// 3. Allgemeine Methoden
// Wuensche ?
//
// 4. Dummie-Flag
// Das Dummie-Flag ist hinzugekommen, um die umgebende Flaeche (Plane) 
// des Graphen als solche - im Unterschied zu einer echten, abgeschlossenen
// Flaeche zu kennzeichnen. Es sollte auch in der Datenstruktur seperat 
// auftauchen, da es beim hinzufuegen und entfernen von Edges ohnehin
// benoetigt wird... ?
// ************************************************************************
//  Diskussionsende
// ************************************************************************

/**
 * This Class realizes a drawable face in the plane or in space.
 * 
 * @version 0.1 01.09.05
 * @author Birgit Engels [engels@cs.uni-bonn.de]
 * @see Graph , Edge , Vertex
 * 
 *      <p> TODO: Write documentation, Finish formatting the code...
 */

abstract public class Face
		implements java.io.Serializable
{

	//*************************************************************************
	//                           Private Constants
	//*************************************************************************

	// For generating unique ids for every instance of Face
	private static int		_ID_GEN		= 0;

	//*************************************************************************
	//                        Private instance ariables
	//*************************************************************************

	// Unique id 
	private int				_id;

	// Reference to any object - for future use
	private Object			_reference	= null;

	// 'name' of this face ( or comment )
	private String			_label		= null;

	/* flags with specified meaning */

	private boolean			_dummie		= false;


	//*************************************************************************
	//                             Constructors
	//*************************************************************************

	/**
	 * Creates a new instance of Face
	 */

	public Face()
	{
		_id = _ID_GEN++;
	}


	//*************************************************************************

	/**
	 * Creates a new instance of Face
	 */

	public Face(
			String s)

	{
		_label = s;
		_id = _ID_GEN++;
	}


	//*************************************************************************
	//                             Class Methods
	//*************************************************************************

	//*************************************************************************
	//                        Package-visible methods
	//*************************************************************************


	//*************************************************************************
	//                             Public Methods
	//*************************************************************************

	//--------------------------- Getters / Setters ---------------------------

	/**
	 * Returns the unique ID of this Face
	 * 
	 * @return The ID
	 **/

	public int getID()
	{
		return _id;
	}


	//*************************************************************************

	/**
	 * Returns the '_label'-String of this face
	 * 
	 * @return The label
	 **/

	public String getLabel()
	{
		return _label;
	}


	//*************************************************************************

	/**
	 * Sets the '_label'-String of this face to s
	 * 
	 * @param s
	 *            The new label
	 **/

	public void setLabel(
			String s)
	{
		_label = s;
	}


	//*************************************************************************

	/**
	 * Sets the '_label'-String to NULL
	 **/

	public void removeLabel()
	{
		_label = null;
	}


	//*************************************************************************

	/**
	 * Returns the object reference stored in '_reference'
	 * 
	 * @return The reference object
	 **/

	public Object getReferenceObject()
	{
		return _reference;
	}


	//*************************************************************************

	/**
	 * Sets the object reference '_reference' to ro
	 * 
	 * @param ro
	 *            Object to which _reference is set
	 **/

	public void setReferenceObject(
			Object ro)
	{
		_reference = ro;
	}


	//*************************************************************************

	/**
	 * Sets this face to a '_dummie'-face ( perhaps surrounding plane )
	 **/

	public void setDummie()
	{
		_dummie = true;
	}


	//*************************************************************************

	/**
	 * Resets the '_dummie'-flag of this face
	 **/

	public void resetDummie()
	{
		_dummie = false;
	}


	//-------------------------- Query methods --------------------------------

	/**
	 * Returns TRUE, iff this face is a 'dummie'-face
	 * 
	 * @return true, if this is a 'dummie'-face, false else
	 **/

	public boolean isDummie()
	{
		return _dummie;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, iff this face and the given face f have the same edge
	 * reference.
	 * 
	 * @param f
	 *            Another face.
	 * 
	 * @return true, if both have the same edge reference, false else
	 **/

	public boolean equals(
			Face f)
	{
		if (f == null)
			return false;
		else
			return (_id == f.getID());

	}


	//------------------ Connectivity information access ----------------------

	/**
	 * 
	 * @return Boundary edge
	 */
	abstract public Edge getBoundaryEdge();


	/**
	 * 
	 * @return Iterator of inner faces
	 */
	abstract public Iterator getInnerFaces();


	//------------------ Arithmetic and Geometric Methods ---------------------

	/**
	 * Returns TRUE, iff the vertex v is situated inside this face
	 * 
	 * @param v
	 *            The vertex v
	 * 
	 * @return true, if the vertex is inside of this face, false else
	 **/

	public boolean isInside(
			Vertex v)
	{
		/*Not yet implemented...!*/
		return false;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, iff the vertex v is situated on this face's border (NOT
	 * IMPLEMENTED)
	 * 
	 * @param v
	 *            The vertex v
	 * 
	 * @return true, if v is situated on the border, false else
	 **/

	public boolean onBorder(
			Vertex v)
	{
		/*Not yet implemented...!*/
		return false;
	}


	//*************************************************************************

	/**
	 * Returns TRUE, iff the edge e belongs to this face's border (NOT
	 * IMPLEMENTED)
	 * 
	 * @param e
	 *            The edge e
	 * 
	 * @return true, if e belongs to the border, false else
	 **/

	public boolean onBorder(
			Edge e)
	{
		/*Not yet implemented...!*/
		return false;
	}


	//--------------------- Iterator access methods ---------------------------

	/**
	 * Returns an Iterator<Edge> for this face
	 * 
	 * @param mode
	 *            The mode
	 * 
	 * @return The Iterator<Edge>
	 **/
	public abstract Iterator<Edge> getEdgeIterator(int mode);



	//*************************************************************************

	/**
	 * Returns an Iterator<Vertex> for this face
	 * 
	 * @param mode
	 *            The mode
	 * 
	 * @return The Iterator<Vertex>
	 **/
	abstract public Iterator<Vertex> getVertexIterator(int mode);



	//*************************************************************************

	/**
	 * Returns a Iterator<Face> for this face
	 * 
	 * @param mode
	 *            The mode
	 * 
	 * @return The Iterator<Face>
	 **/
	abstract public Iterator<Face> getFaceIterator(int mode);


	//-------------------------- Visualization --------------------------------

	/**
	 * Draws this face. (NOT IMPLEMENTED)
	 **/

	public void draw(/* ? */)
	{
		// to be implemented :-)
	}


	//*************************************************************************

	/**
	 * Returns a string representation of this face
	 * 
	 * @return A textual representation of this object
	 **/

	public String toString()
	{
		String s = "Face " + _id + ": ";

		if (_label != null)
			s = s.concat("Label: " + _label);

		return s;
	}

}// Face
