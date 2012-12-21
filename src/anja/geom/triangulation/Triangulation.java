package anja.geom.triangulation;


import java.awt.Graphics2D;
import java.util.HashMap;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Segment2;
import anja.graph.Graph;
import anja.graph.Vertex;
import anja.graph.Edge;


/**
 * Definiert die Methoden einer Triangulation.
 * 
 * <br> Alle Triangulationen können so beispielsweise in duale Graphen
 * umgerechnet werden. Diese Eigenschaft ist momentan mit Hilfe von
 * Hashfunktionen realisiert. Sollte sich durch eine günstige Berechnung der
 * Dreiecke diese Reihenfolge implizit ergeben, so kann und sollte die Methode
 * überschrieben werden.
 * 
 * @author Andreas Lenerz
 * 
 */

public abstract class Triangulation
		implements java.io.Serializable
{

	//************************************************************
	// Public Methods
	//************************************************************

	/**
	 * Zeichnet die Triangulation.
	 * 
	 * @param g2d
	 *            Die Zeichenflaeche, auf der gezeichnet werden soll
	 */
	public abstract void draw(
			Graphics2D g2d);


	/**
	 * Berechnet aus einer gegebenen Anzahl von Dreiecken den dualen Graphen
	 * 
	 * <br>Jedes Dreieck wird durch seinen Schwerpunkt repräsentiert, der immer
	 * im Dreieck liegt. Jeder Knoten des Graphen ist somit ein Schwerpunkt
	 * eines Dreiecks. Als Referenz für jeden Knoten gibt es einen Verweis auf
	 * das Dreieck. <br>Das Verfahren läuft mit Hashlisten und hat somit eine
	 * erwartete Zeit des Einfügens und Suchens von <code>O(1)</code>. Als
	 * Vergleichsobjekte werden die Diagonalen/Dreieckskanten genutzt.
	 * 
	 * <br>Jede Kante des Graphen besitzt ein Referenzobjekt - die zugehörige
	 * Diagonale der Triangulation, welche ebenfalls im Graphen gespeichert wird
	 * und für weitere Berechnungen zur Verfügung steht.
	 * 
	 * <br> Als Diagonalen und somit Referenzobjekte kommen nur solche Diagonalen
	 * in Frage, die keine Polygonkanten sind und bis auf Start- und Endpunkt
	 * vollständig im Inneren des Polygons verlaufen. Die Kante des Graphens
	 * selbst muss hingegen NICHT vollständig im Polygon verlaufen, da die
	 * direkte Verbindung der Schwerpunkte zweier Dreiecke durchaus den
	 * Polygonrand schneiden kann.
	 * 
	 * <br>Die Hashwerte werden anhand der x- und y-Werte der Diagonalen
	 * bestimmt. Die Knoten und Kanten werden ungeordnet dem Graphen
	 * hinzugefügt.
	 * 
	 * 
	 * @param triangs
	 *            Die Dreiecke
	 * 
	 * @return Der duale Graph (eventuell auch leer)
	 */
	public static Graph getDualGraph(
			Polygon2[] triangs)
	{
		Graph g = Graph.CreateGraph();

		if (triangs != null)
		{
			//HashMap mit doppelter Anzahl von Dreieckskanten initial
			HashMap<String, Vertex> hm = new HashMap<String, Vertex>(
					2 * 3 * triangs.length);

			for (Polygon2 p : triangs)
			{
				System.out.println(p);
				Point2 cen = p.getCenterOfMass();
				Vertex v = g.addVertex(cen.x, cen.y);
				v.setReferenceObject(p);

				Segment2[] diag = p.edges();
				for (Segment2 seg : diag)
				{
					//String zum einheitlichen Erstellen des hashCodes
					Point2 a = seg.source();
					Point2 b = seg.target();

					String result = null;

					if (a.x < b.x || (a.y < b.y && a.x == b.x))
					{
						result = a.x + " " + a.y + " " + b.x + " " + b.y;
					}
					else
					{
						result = b.x + " " + b.y + " " + a.x + " " + a.y;
					}

					Vertex w = hm.put(result, v);

					if (w != null)
					{
						Edge e = g.addEdge(v,w);
						e.setReferenceObject(seg);						
					}

				}
			}
		}

		return g;
	}

	//************************************************************
	// Private Methods
	//************************************************************

} // Triangulation
