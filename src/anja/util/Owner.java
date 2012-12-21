package anja.util;

/**<p>
* Objekte die Owner implementieren, koennen von Owned-Objekten bei
* bestimmten Aktionen um Zustimmung gebeten werden.
*
* @author Thomas Wolf
* @version 1.0
*/
public interface Owner {
 /**
 * Das Owned-Objekt who bittet den Owner um Genehmigung einer 
 * Aktion vom Typ accesstype mit dem Argument argument.
 * @param accesstype Typ der Aktion (des Zugriffs)
 * @param who Objekt, welches den Zugriff erbittet
 * @param argument Argument der Aktion
 * @return true, falls der Owner die Aktion erlaubt, sonst false
 */
 public boolean requestAccess(int accesstype,Object who,Object argument);
}
