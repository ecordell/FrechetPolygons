package anja.util;


// JDK 1.1 !!!!
/*import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;*/

/**
 * <p> Standard-Baumknoten für Binärbäume. Der Knoten basiert auf
 * BasicBinaryTreeNode und kann zusätzlich einen Wert und einen Schlüssel
 * speichern.
 * 
 * @author Thomas Wolf
 * @version 1.0
 */
public class StdTreeItem
		extends BasicTreeItem
{

	/** Der gespeicherte Schlüssel. */
	private Object	_key	= null;
	/** Das gespeicherte Objekt. */
	private Object	_value	= null;


	// Konstruktoren
	// =============

	/**
	 * Standardkonstruktor. Der gespeicherte Wert und der Schlüssel werden auf
	 * null gesetzt.
	 */
	public StdTreeItem(
			int maxrank)
	{
		super(maxrank);
		_value = null;
		_key = null;
	}


	/**
	 * Konstruktor mit Wertinitialisierung. Es wird hierbei <b>nicht</b>
	 * requestAccess aufgerufen!
	 * @param key
	 *            neuer Schlüssel
	 * @param value
	 *            neuer Wert
	 */
	public StdTreeItem(
			int maxrank,
			Object key,
			Object value)
	{
		super(maxrank);
		_key = key;
		_value = value;
	}


	// Allgemeine Routinen
	// ===================

	/**
	 * Überschreibt Object.toString().
	 */
	public String toString()
	{
		return getClass().getName() + "[key=" + _key + ",value=" + _value + "]";
	}


	/**
	 * Klont das Objekt. Dabei sollte folgendes gelten: Das neue ListItem ist
	 * <strong>nicht</strong> verknüpft und hat <strong>keinen</strong>
	 * Besitzer. Die angehängten Schlüssel und Objekte werden ebenfalls geklont.
	 * @return Kopie des ListItems
	 */
	public Object clone()
	{
		return new StdTreeItem(maxRank(), _key, _value);
	}


	/**
	 * Legt eine Kopie der im Objekt enthaltenen referenzierten Datenobjekte an.
	 * Schlüsselobjekte werden nicht geklont und Verbindungen sowie Besitzer
	 * bleiben unverändert.
	 */
	public void cloneData()
	{
		/*  if (_value==null) return;
		  // Der Zugriff auf die clone-Methode ist nicht soo einfach...
		  // (und funktioniert nur mit JDK 1.1)
		  try {
		   Method cloneMethod=_value.getClass().getDeclaredMethod("clone",new Class[0]);
		   if (Modifier.isPublic(cloneMethod.getModifiers())) {
		    _value=cloneMethod.invoke(_value,new Object[0]);
		   }
		  } catch (NoSuchMethodException e) {}
		    catch (SecurityException e) {}
		    catch (IllegalAccessException e) {}
		    catch (IllegalArgumentException e) {}
		    catch (InvocationTargetException e) {}*/
	}


	// Eigentum
	// ========

	/**
	 * Liefert den gespeicherten Schlüssel des Objektes.
	 * @return gespeicherter Schlüssel
	 */
	public Object key()
	{
		return _key;
	}


	/**
	 * Setzt den zugeordneten Schlüssel.
	 * @param key
	 *            neuer Schlüssel
	 */
	public synchronized boolean setKey(
			Object key)
	{
		if (_key == key)
			return true;
		if (!requestTreeAccess(SET_KEY, key))
			return false;
		_key = key;
		return true;
	}


	/**
	 * Liefert den gespeicherten Wert zurück.
	 * @return gespeichertes Objekt
	 */
	public Object value()
	{
		return _value;
	}


	/**
	 * Speichert das Objekt object.
	 * @param object
	 *            zu speicherndes Objekt
	 */
	public synchronized boolean setValue(
			Object object)
	{
		if (_value == object)
			return true;
		if (!requestTreeAccess(SET_VALUE, object))
			return false;
		_value = object;
		return true;
	}

}
