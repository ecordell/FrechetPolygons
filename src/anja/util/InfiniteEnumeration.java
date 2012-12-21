package anja.util;

import java.util.Enumeration;


/**
* Dieses Interface spezifiziert eine
* <code>java.util.Enumeration</code>-kompatible Aufzaehlung, die eine unendliche
* Folge von Objekten liefern kann. Die Folge darf sich beliebig wiederholen
* (insbesondere muss nicht jeder Wiederholungsdurchlauf gleich sein), und
* koennte durch Verwendung einer Queue (bei linearer Aufzaehlung) implementiert
* werden, oder durch Verwendung eines Arrays in Verbindung mit einem
* Zufallsalgorithmus, der so eine zufaellige Folge liefern kann
*
* @version 0.9 18.11.03
* @author Sascha Ternes
*/

public interface InfiniteEnumeration
extends Enumeration
{

/**
* Liefert immer <code>true</code>, da diese Aufzaehlung unendlich ist.
*
* @return <code>true</code>
*/
public boolean hasMoreElements();


/**
* Liefert das naechste Element der Aufzaehlung. Dieses Element kann identisch mit
* seinem Vorgaenger sein, falls die Aufzaehlung lediglich ein einziges Element
* wiederholt.<p>
*
* Hierbei kann keine <code>NoSuchElementException</code> auftreten, daher ist
* es nicht noetig diese abzufangen.
*
* @return das naechste Element der Aufzaehlung
*/
public Object nextElement();


} // InfiniteEnumeration
