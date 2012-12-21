package anja.swinggui;

import java.awt.Dimension;
import java.awt.Point;


/**
* Dieses Interface kann von Applets implementiert werden, die in einem
* separaten Fenster dargestellt werden, z.B. nach Klick auf einen Startknopf
* der Klasse {@link anja.swinggui.StartButtonApplet}, um zusaetzliche
* nuetzliche Methoden zur Verfuegung zu stellen.
*
* @version 0.1 03.05.2005
* @author Sascha Ternes
*/

public interface ExternalApplet {

/**
* Liefert die gewuenschte Position beim Start des Applets.
*
* @return die Startposition
*/
public Point getPreferredPosition();


} // ExternalApplet