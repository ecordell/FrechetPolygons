package anja.geom.visgraph;


import java.util.Vector;

import anja.geom.Point2;


/**
 * Die SweepLine berechnet fuer den radialen Sweep das naechste Punktepaar in
 * der Form eines Liniensegmentes ({@link anja.geom.Segment2}). Zur Bestimmung
 * der Reihenfolge, mit der die Punktepaare ausgewaehlt werden, werden die s.g.
 * Horizontbaeume benutzt. Dazu werden die Punkte als Duale Geraden in der
 * SweepLine verwaltet. Der jeweils kleinere Schnittpunkt zwischen den dualen
 * Gerade gibt an, welche Sichtbarkeitskante als naechste zurueckgegeben wird.
 * 
 * <br>Die Klasse benutzt VisGVertice Klasse, um die duale Graden darzustellen.
 * 
 * Es wird davon ausgegangen, dass keine zwie Punkte <b>gleich</b> sind.
 * 
 * @author Alexander Tiderko
 * 
 **/
public class SweepLine
{

	private VisGVertice[]	_sweepLine;

	// Die Schnittpunkte der parallelen Geraden werden zuerst gefunden
	// und sollten vor dem eigentlichen Algorithmus ausgegeben werden
	protected Vector		_parallelLines	= new Vector();


	/**
	 * Erzeugt die SweepLine. Die Zusteande in dem oberen und unteren
	 * Horizonbaum werden initialisiert. Es werden auch die "Schnittpunkte"
	 * zwischen den parallelen Geraden bestimmt (Punkte mit der gleichen
	 * X-Koordinate) und in einem Vektor abgelegt, damit diese als erstes
	 * ausgegeben werden koennen und den eigentlichen Algorithmus nicht stoeren.
	 * 
	 * @param sweepLine
	 *            das Array aller Punkte, fuer die die Berechnugsreihenfolge
	 *            bestimmt werden soll.
	 */
	public SweepLine(
			VisGVertice[] sweepLine)
	{
		_sweepLine = sweepLine;
		initNeighbours();

		// Initialisiere den oberen Horizontbaum
		for (int i = _sweepLine.length - 1; i >= 0; i--)
		{
			_sweepLine[i].set_upperHorizontIntersection(null);
			refreshUpperHorizont(i);
		}

		// Initialisiere den unteren Horizontbaum
		for (int i = 0; i < _sweepLine.length; i++)
		{
			_sweepLine[i].set_lowerHorizontIntersection(null);
			refreshLowerHorizont(i);
		}

		// Bestimme alle parallelen dualen Geraden
		for (int i = 1; i < _sweepLine.length; i++)
		{
			if (_sweepLine[i - 1].x == _sweepLine[i].x)
				_parallelLines.add(new VisGEdge(_sweepLine[i - 1],
						_sweepLine[i]));
		}
	}


	//*************************************************************************

	/**
	 * Setzt die Nachbarschaftsbeziehungen zwischen den Geraden. Hat eine Gerade
	 * keinen oberen, bzw. unteren Nachbar, so wird der Index des Nachbarn auf
	 * -1 gesetzt, sonst auf das Index der Nachbargerade.
	 */
	private void initNeighbours()
	{
		for (int i = 0; i < _sweepLine.length; i++)
		{
			_sweepLine[i].setIndex(i);
			_sweepLine[i].setUpperNeighbourIndex(i - 1);
			if (i < _sweepLine.length - 1)
				_sweepLine[i].setLowerNeighbourIndex(i + 1);
			else
				_sweepLine[i].setLowerNeighbourIndex(-1);
		}
	}


	//*************************************************************************

	/**
	 * Aktualisiert den oberen Horizontbaum. Dabei wird fuer die Gerade mit dem
	 * <em>iLineIndex</em> die Gerade bestimmt, die diese im oberen Horizontbaum
	 * beschraenkt. Ist die Gerade mit dem <em>iLineIndex</em> die unterste
	 * Gerade, so gibt es keine beschraenkende Gerade. Dann wird die
	 * beschraenkende Gerade auf <b>null</b> gesetzt.
	 * 
	 * @param iLineIndex
	 *            Index der Geraden, die auktualisiert werden soll.
	 */
	private void refreshUpperHorizont(
			int iLineIndex)
	{
		VisGVertice lowerLine = null;

		int lowerIndex = _sweepLine[iLineIndex].getLowerNeighbourIndex();

		if (lowerIndex == -1)
		{
			// Existiert ein unterer Nachbar?			
			_sweepLine[iLineIndex].set_upperHorizontIntersection(null);
		}
		else
		{
			lowerLine = _sweepLine[lowerIndex];

			boolean not_found = (lowerLine != null);
			while (not_found)
			{
				// bis wohin geht lowerline ?
				Point2 lowerIPoint = lowerLine
						.get_upperHorizontIntersectionPoint();

				// intersection mit lower line im Bereich?
				VisGVertice iPoint = _sweepLine[iLineIndex]
						.intersection(lowerLine);

				if ((iPoint.x != Float.NEGATIVE_INFINITY) &&

				// Schnittpunkte zwischen parallelen wurden schon behandelt
						((lowerIPoint == null) || (iPoint
								.comparePointsLexX(lowerIPoint) <= 0)) &&

						// intersection mit lower line im Bereich?
						(iLineIndex < lowerLine.getIndex()))
				{
					// Schnittpunkte mit den Geraden mit kleinerem
					// Index(Steigung) wurden schon behandelt
					// wenn ja, dann Intersection gefunden - Abbruch
					// der while-Schleife

					not_found = false;
				}
				else
				{
					// wenn nicht, dann lese aus lowerline die naechste
					// lowerline (bis null!)

					lowerLine = lowerLine.get_upperHorizontIntersectionLine();
					not_found = (lowerLine != null);
				}
			}
			_sweepLine[iLineIndex].set_upperHorizontIntersection(lowerLine);
		}
	}


	//*************************************************************************

	/**
	 * Aktualisiert den unteren Horizontbaum. Dabei wird fuer die Gerade mit dem
	 * <em>iLineIndex</em> die Gerade bestimmt, die diese im unteren
	 * Horizontbaum beschraenkt. Ist die Gerade mit dem <em>iLineIndex</em> die
	 * oberste Gerade, so gibt es keine beschraenkende Gerade. Dann wird die
	 * beschraenkende Gerade auf <b>null</b> gesetzt.
	 * 
	 * @param iLineIndex
	 *            Index der Geraden, die auktualisiert werden soll.
	 */
	private void refreshLowerHorizont(
			int iLineIndex)
	{
		VisGVertice upperLine = null;

		int upperIndex = _sweepLine[iLineIndex].getUpperNeighbourIndex();
		if (upperIndex == -1)
		{
			// Existiert ein oberer Nachbar?		
			_sweepLine[iLineIndex].set_lowerHorizontIntersection(null);
		}
		else
		{
			upperLine = _sweepLine[upperIndex];

			boolean not_found = (upperLine != null);
			while (not_found)
			{
				// bis wohin geht upperline ?
				Point2 upperIPoint = upperLine
						.get_lowerHorizontIntersectionPoint();

				// intersection mit upper line im Bereich?				
				Point2 iPoint = _sweepLine[iLineIndex].intersection(upperLine);

				if ((iPoint.x != Float.NEGATIVE_INFINITY) && // Schnittpunkte zwischen parallelen
						// wurden schon behandelt
						((upperIPoint == null) || (iPoint
								.comparePointsLexX(upperIPoint) < 0)) &&
						// intersection mit upper line im Bereich?
						(iLineIndex > upperLine.getIndex()))
				{
					// Schnittpunkte mit den Geraden mit groesserem
					// Index(Steigung) wurden schon behandelt
					// wenn ja, dann Intersection gefunden - Abbruch
					// der while-Schleife
					not_found = false;
				}
				else
				{
					// wenn nicht, dann lese aus upperline die naechste
					// upperline (bis null!)
					upperLine = upperLine.get_lowerHorizontIntersectionLine();
					not_found = (upperLine != null);
				}
			}
			_sweepLine[iLineIndex].set_lowerHorizontIntersection(upperLine);
		}
	}


	//*************************************************************************

	/**
	 * Gibt das naechste Punktepaar in Form eines Segmentes zurueck.
	 * 
	 * @return Segment, welches fuer den Sichtbarkeitsgraphen als eine Kante in
	 *         Frage kommt. Gibt es keine Punktepaare mehr, so wird <b>null</b>
	 *         zurueck geliefert.
	 */
	public VisGEdge getNextSegment()
	{
		VisGEdge iSeg = null;
		// Gebe zuerst die "Scnittpunkte" zwischen den Parallelen
		// (Punkte mit der gleichen X-Koordinate)
		if (!_parallelLines.isEmpty())
		{
			iSeg = (VisGEdge) _parallelLines.get(0);
			_parallelLines.remove(0);
		}
		else
		{
			// finde den naechsten Schnittpunkt in der Sweepline...
			Point2 iPoint = null;
			int iLineIndex = -1; // Der erste Index der
			// VisGVertice, die spaeter
			// getauscht werden soll
			int iLineIndex2 = -1; // Der zweite Index der
			// VisGVertice, die spaeter
			// getauscht werden soll

			for (int index = 0; index < _sweepLine.length; index++)
			{
				VisGVertice iLine = _sweepLine[index].get_IntersectionLine();

				// finde zwei Geraden, die den gleichen naechsten
				// Schnittpunkt haben
				if (iLine != null)
				{
					int iIndex = iLine.getIndex();
					if (iIndex > index)
					{
						// um doppelbetrachtungen vorzubeugen (z.B.
						// (1,2), (2,1))
						VisGVertice iiLine = _sweepLine[iIndex]
								.get_IntersectionLine();
						if ((iiLine != null) && (index == iiLine.getIndex()))
						{
							Point2 intersectPoint = _sweepLine[index]
									.get_IntersectionPoint();
							// ist der neue der kleinerer
							// Schnittpunkt?
							if ((iPoint == null)
									|| (intersectPoint
											.comparePointsLexX(iPoint) <= 0))
							{
								iPoint = intersectPoint;
								// Segment fuer die Rueckgabe
								iSeg = new VisGEdge(_sweepLine[index],
										_sweepLine[iIndex]);
								// fuer die aktualisierung der
								// Nachbarschaftsbeziehungen
								iLineIndex = index;
								iLineIndex2 = iIndex;
							}
						}
					}
				}
			}

			// Aktualisierung der Nachbarschaftsbeziehungen in den
			// Horizonbaeumen

			if ((iLineIndex != -1)
					&& ((iPoint != null) && (iPoint.x != Float.NEGATIVE_INFINITY)))
			{
				// Zwischen den Parallelen aendert sich nichts
				_sweepLine[iLineIndex]
						.setLowerNeighbourIndex(_sweepLine[iLineIndex2]
								.getLowerNeighbourIndex());

				_sweepLine[iLineIndex2]
						.setUpperNeighbourIndex(_sweepLine[iLineIndex]
								.getUpperNeighbourIndex());

				_sweepLine[iLineIndex].setUpperNeighbourIndex(iLineIndex2);
				_sweepLine[iLineIndex2].setLowerNeighbourIndex(iLineIndex);

				// unbeteiligte Geraden
				int index = _sweepLine[iLineIndex2].getUpperNeighbourIndex();

				if (index != -1)
					_sweepLine[index].setLowerNeighbourIndex(iLineIndex2);

				index = _sweepLine[iLineIndex].getLowerNeighbourIndex();

				if (index != -1)
					_sweepLine[index].setUpperNeighbourIndex(iLineIndex);

				//Aktualisierung der Schnittpunkte in den Horizontbaeumen
				refreshUpperHorizont(iLineIndex);
				refreshLowerHorizont(iLineIndex2);
			}
		}
		return iSeg;
	}

	//*************************************************************************
}
