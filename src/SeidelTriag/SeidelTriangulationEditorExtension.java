/*
 * Anmerkung: Zu diesem Projekt gehoeren die Dateien:
 * SeidelTriangulationEditorExtension.java, SeidelTriangulationProg.java und
 * SeidelTriangulationApplet.java
 */

package SeidelTriag;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import anja.geom.Polygon2;
import anja.geom.Segment2;

import anja.geom.triangulation.SeidelTriangulation;
import anja.geom.triangulation.PSLGTriangulation;

import anja.swinggui.polygon.Polygon2SceneEditor;
import anja.util.GraphicsContext;


/**
 * die Klasse MinkowskiSumEditorExtension erbt die Eigenschaften von dem
 * Polygon2SceneEditor und erweitert diesen um einige Funktionen.
 * 
 * @author Marina Bachran
 * 
 * @see SeidelTriangulationEditorExtension SeidelTriangulationProg
 *      SeidelTriangulationApplet
 */

public class SeidelTriangulationEditorExtension
		extends Polygon2SceneEditor
{

	// Enthaelt die Zeicheneigenschaften fuer das Polygon
	private GraphicsContext		_gcPolygon;
	private GraphicsContext		_gcPolygonAfter;
	private GraphicsContext		_gcTriangulationDiags;
	private GraphicsContext		_gcSeidelDiags;
	private GraphicsContext		_gcPSLGDiags;
	

	private Color				myBlue		= new Color(51, 51, 102);
	private Color				myBlue2		= new Color(0, 0, 238);
	private Color				myBeige		= new Color(255, 255, 238);
	private Color				myYellow	= new Color(255, 204, 85);
	private Color				myDRed		= new Color(153, 0, 0);
	private Color				myLRed		= new Color(255, 0, 51);
	private Color				myGreen		= new Color(47, 132, 93);
	private Color				myCyan		= Color.CYAN;

	private SeidelTriangulation	tri;
	private PSLGTriangulation	pslg;


	public SeidelTriangulationEditorExtension()
	{
		_gcPolygon = new GraphicsContext();
		_gcPolygon.setForegroundColor(myBlue2);
		_gcPolygon.setFillColor(myBeige);
		_gcPolygon.setFillStyle(1);

		_gcPolygonAfter = new GraphicsContext();
		_gcPolygonAfter.setForegroundColor(myBlue2);
				
		_gcTriangulationDiags = new GraphicsContext();
		_gcTriangulationDiags.setForegroundColor(myGreen);
		_gcTriangulationDiags.setFillColor(myBeige);
		_gcTriangulationDiags.setFillStyle(1);

		_gcSeidelDiags = new GraphicsContext();
		_gcSeidelDiags.setForegroundColor(myLRed);
		_gcSeidelDiags.setFillColor(myBeige);
		_gcSeidelDiags.setFillStyle(1);

		_gcPSLGDiags = new GraphicsContext();
		_gcPSLGDiags.setForegroundColor(myCyan);
		_gcPSLGDiags.setFillColor(myBeige);
		_gcPSLGDiags.setFillStyle(1);

		setPointColor(myBlue2);
		setEdgeColor(myBlue2);
		setPolygonColor(myBeige);
		setFullRefreshMode(true); // Aktualisiert alles -> verhindert, dass nur
		// Teilbereiche aktualisiert werden und die
		// Minkowski-Summe nicht
	}


	public boolean	showTrapezoidation		= false;
	public boolean	showSeidelDiags			= false;
	public boolean	showTriangulationSeidel	= false;
	public boolean	showTriangulationPSLG	= false;


	/**
	 * Diese Methode beschraenkt die Anzahl der Polygone mit Hilfe von
	 * 'controlPolygonNumber' auf zwei. Sind zwei Polygone vorhanden, so wird
	 * die MinkowskiSumme berechnet und vor den Polygonen aus dem Editor
	 * gezeichnet.
	 */
	protected void paintPolygons(Graphics2D g2d, Graphics g)
	{
		controlPolygonNumber(1); // erlaubte Anzahl der Polygone im Editor
		super.paintPolygons(g2d, g); // Polygone des Editors zeichnen

		Polygon2 poly = getPolygon2Scene().getPolygon(0);

		if ((poly != null) && (poly.isSimple()))
		{

			tri = new SeidelTriangulation(poly);

			if (showTrapezoidation)
			{
				Polygon2[] innerTraps = tri.getInnerTrapezoids();
				for (int i = 0; i < innerTraps.length; i++)
					if (innerTraps[i] != null)
						innerTraps[i].draw(g2d, _gcPolygon);
			}
			if (showTriangulationSeidel)
			{
				Segment2[] diagonals = tri.getDiagonals();
				for (int i = 0; i < diagonals.length; i++)
					if (diagonals[i] != null)
						diagonals[i].draw(g2d, _gcTriangulationDiags);
			}
			if (showSeidelDiags)
			{
				Segment2[] seidelDiags = tri.getSeidelDiagonals();
				for (int i = 0; i < seidelDiags.length; i++)
					if (seidelDiags[i] != null)
						seidelDiags[i].draw(g2d, _gcSeidelDiags);
			}

			if (showTriangulationPSLG)
			{
				pslg = new PSLGTriangulation(poly);
				if (pslg.getTriangles() != null)
					pslg.draw(g2d, _gcPSLGDiags);
			}			
			
			poly.draw(g2d, _gcPolygonAfter);
		}
	}


	/**
	 * Diese Methode kontrolliert die Anzahl der einfachen Polygone. Diese darf
	 * 'number' nicht Ueberschreiten. Alles Ueberzaehlige wird geloescht.
	 */
	private void controlPolygonNumber(int number)
	{
		Polygon2 existingPolygons[] = getPolygon2Scene().getPolygons();
		Polygon2 correctPolygons[] = new Polygon2[number];
		int count = 0;
		int i;
		for (i = 0; i < existingPolygons.length; i++)
		{
			if (existingPolygons[i].isSimple())
			{
				correctPolygons[count] = existingPolygons[i];
				count++;
				if (count == number)
					break;
			}
		}
		if (count == number)
		{
			for (i = existingPolygons.length - 1; i >= 0; i--)
				getPolygon2Scene().remove(i);
			for (i = 0; i < correctPolygons.length; i++)
				getPolygon2Scene().add(correctPolygons[i]);
		}
	}


	public void repaint()
	{
		refresh();
	}

}
