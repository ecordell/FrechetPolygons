package anja.util;


import java.io.*;


/**
 * Savable
 * 
 * Interface zum Laden und Speichern von Objekten
 * 
 * @version 0.1 25. Juli 1997
 * @author Peter Koellner
 */
public interface Savable
{

	/**
	 * Abspeichern in einen Ausgabestream
	 * 
	 * @param dio
	 *            der Ausgabestream
	 */
	public abstract void save(
			DataOutputStream dio);
}
