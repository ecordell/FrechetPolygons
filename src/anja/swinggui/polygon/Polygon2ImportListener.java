package anja.swinggui.polygon;

import anja.geom.Polygon2;


/**
* Dieses Interface definiert die noetigen Methoden, die Klassen implementieren
* muessen, um sich als Listener bei der Klasse <a href="RandomPolygon2Tool.html"
* ><code>RandomPolygon2Tool</code></a> registrieren zu koennen.
*
* @version 0.9 08.02.05
* @author Sascha Ternes
*/

public interface Polygon2ImportListener
{

   // *************************************************************************
   // Interface-Methoden
   // *************************************************************************

   /**
   * Wird aufgerufen, wenn ein Polygon zum Import bereitsteht.
   *
   * @param polygon das zu importierende Polygon
   * @param exporter die exportierende Instanz
   */
   public void importPolygon( Polygon2 polygon, Object exporter );

   /**
   * Wird aufgerufen, wenn die exportierende Instanz (z.B. eine Instanz von
   * <a href="RandomPolygon2Tool.html"><code>RandomPolygon2Tool</code></a>)
   * beendet wird. Mit dieser Methode wird der Listener also darueber
   * benachrichtigt, dass ab sofort keine Polygone mehr importiert werden
   * koennen und der Listener nicht mehr bei der exportierenden Instanz als
   * <code>Polygon2ImportListener</code> registriert ist.
   *
   * @param exporter die exportierende Instanz
   */
   public void instanceClosed( Object exporter );

   /**
   * Wird aufgerufen, wenn die exportierende Instanz (z.B. eine Instanz von
   * <a href="RandomPolygon2Tool.html"><code>RandomPolygon2Tool</code></a>)
   * erfolgreich initialisiert wurde und fuer den Export von Polygonen bereit
   * ist. Diese Methode dient also als Bestaetigung fuer den
   * <code>Polygon2ImportListener</code>, dass ab sofort Polygone importiert
   * werden koennen.
   *
   * @param exporter die exportierende Instanz
   */
   public void instanceOpened( Object exporter );


} // Polygon2ImportListener
