package anja.swinggui.point;


import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Point2List;


//import java_ersatz.java2d.Transform;

/**
 * Spezifiziert die Methoden, die ein Punktlistenalgorithmus des Editors fuer
 * die Punktliste implementieren muss.<br>
 * 
 * Neben der Hauptmethode gibt es eine Methode, mit der der Algorithmus ueber
 * die aktuelle Transformationsmatrix fuer Zeichenausgaben informiert wird, die
 * Moeglichkeit, das Kontextmenue mit eigenen Eintraegen zu erweitern (um auf
 * eigene Menuepunkte zu reagieren, muss der Algorithmus zusaetzlich das
 * Interface <code>ActionListener</code> implementieren), und eine Methode, mit
 * deren Hilfe Mausaktionen innerhalb des Zeichenfensters des Editors abgefangen
 * und verarbeitet werden koennen.
 * 
 * @see #compute(Point2List points, Graphics2D g2d)
 * 
 * @version 0.2 10.04.2003
 * @author Sascha Ternes, Andreas Lenerz
 */

public interface Point2ListAlgorithm
{

	/**
	 * Wird vom Editor aufgerufen, wenn die Punktliste veraendert wurde, um eine
	 * Neuberechnung des Algorithmus zu veranlassen.
	 * 
	 * @param points
	 *            Liste aller Punkte
	 * @param g2d
	 *            Das Grafikobjekt, in das gezeichnet wird
	 */
	public void compute(
			Point2List points,
			Graphics2D g2d);


	/**
	 * Wird vom Editor aufgerufen, nachdem das Zeichenfenster aktualisiert
	 * wurde, aber bevor die Methode {@link #compute(Point2List points,
	 * Graphics2D g2d)} aufgerufen wird.
	 * 
	 * @param matrix
	 *            die aktuelle Transformationsmatrix
	 */
	public void updateTransformMatrix(
			AffineTransform matrix);


	/**
	 * Wird vom Editor aufgerufen, bevor das Kontextmenue angezeigt wird. Dies
	 * gibt dem Algorithmus Gelegenheit, eigene Menuepunkte in das Kontextmenue
	 * einzubauen.
	 * 
	 * @param menu
	 *            Das Menü, das erweitert werden soll
	 */
	public void changeContextMenu(
			JPopupMenu menu);


	/**
	 * Wird vom Editor beim Auftreten einer Mausaktion aufgerufen. Der
	 * implementierende Algorithmus kann die Art der Mausaktion bestimmen und
	 * gegebenenfalls darauf reagieren. Wenn die Mausaktion anschliessend nicht
	 * mehr vom Editor selbst verarbeitet werden soll, muss die Methode
	 * <code>true</code> zurueckliefern.
	 * 
	 * @param event
	 *            das <code>MouseEvent</code>-Objekt des Mausereignisses
	 * @param mouse
	 *            die aktuellen Mauskoordinaten im Weltkoordinatensystem
	 * @param points
	 *            die aktuelle Punktmenge des Editors
	 * @return <code>true</code>, falls diese Methode das Mausereignis behandelt
	 *         hat und der Editor selbst nichts unternehmen soll, sonst
	 *         <code>false</code>
	 */
	public boolean processMouseEvent(
			MouseEvent event,
			Point2 mouse,
			Point2List points);


	/**
	 * Wird vom Editor beim Registrieren des Algorithmus aufgerufen und dient
	 * dazu, dem Algorithmus die Referenz auf den Editor zu uebermitteln, damit
	 * dieser <code>public</code>-Methoden des Editors aufrufen kann.
	 * 
	 * @see anja.swinggui.point.Point2ListEditor#repaint().
	 */
	public void registerEditor(
			Point2ListEditor editor);

} // Point2ListAlgorithm
