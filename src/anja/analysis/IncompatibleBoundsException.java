
package anja.analysis;


/**
 * Signalisiert die Inkompatibilitaet mehrerer {@link Bound}-Objekte
 * untereinander, die von unterschiedlichen Dimensionen ihrer
 * Schrankenwerte herruehrt.
 * 
 * @version 0.9 20.11.03
 * @author Sascha Ternes
 */

public class IncompatibleBoundsException extends RuntimeException
{

    // *************************************************************************
    // Constructors
    // *************************************************************************

    /**
     * Erzeugt eine neue <code>IncompatibleBoundsException</code>
     * ohne detaillierte Fehlermeldung.
     */
    public IncompatibleBoundsException()
    {
        super();
    } // IncompatibleBoundsException


    /**
     * Erzeugt eine neue <code>IncompatibleBoundsException</code>
     * mit der uebergebenen Fehlermeldung.
     * 
     * @param message die Fehlermeldung
     */
    public IncompatibleBoundsException(String message)
    {
        super(message);
    } // IncompatibleBoundsException

} // IncompatibleBoundsException
