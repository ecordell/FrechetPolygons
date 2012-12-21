package anja.util;


/**
 * TreeExceptions werden von BasicTree und abgeleiteten Klassen erzeugt.
 * Errorcode gibt einen genauere Beschreibung des aufgetretenen Fehlers. Da
 * TreeException eine Unterklasse von RuntimeException ist, muss dieser Fehler
 * nicht mit try...catch abgefangen werden.
 */
public class TreeException
		extends RuntimeException
{

	// Konstanten fuer die verschiedenen Fehlerarten
	// Fehler beim Zugriff auf einzelne Elemente
	public final static int	NO_ERROR	= 0;
	public final static int	CONNECT		= 1;
	public final static int	SET_OWNER	= 2;
	public final static int	REMOVE		= 3;
	public final static int	SET_RANK	= 4;

	/** ListItems, bei denen ein Problem auftrat. */
	public TreeItem			i, j;
	/** Fehlercode. Eine entsprechende Fehlerkonstante. */
	public int				errorcode	= NO_ERROR;


	/**
	 * Liefert die Fehlermeldung zum uebergebenen Fehlercode.
	 * 
	 * @param i
	 *            TreeItems, bei denen der Fehler auftrat
	 * @param j
	 *            TreeItems, bei denen der Fehler auftrat
	 * @param errorcode
	 *            Fehlercode
	 * @return Fehlermeldung
	 */
	private static String getErrorString(
			TreeItem i,
			TreeItem j,
			int errorcode)
	{
		String s = "Unknown Error";
		switch (errorcode)
		{
			case NO_ERROR:
				s = "No Error.";
				break;
			case CONNECT:
				s = "Error connecting " + i + " to " + j + ".";
				break;
			case SET_OWNER:
				s = "Error setting Owner on " + i + ".";
				break;
			case REMOVE:
				s = "Error removing " + i + ".";
				break;
			case SET_RANK:
				s = "Error setting maximal Rank on " + i + ".";
				break;
		}
		return s;
	}


	/**
	 * Konstruktor.
	 * 
	 * @param i
	 *            und
	 * @param j
	 *            TreeItems, bei denen das Problem auftrat
	 * @param errorcode
	 *            das Problem als Fehlercode
	 */
	public TreeException(
			TreeItem i,
			TreeItem j,
			int errorcode)
	{
		super(getErrorString(i, j, errorcode));
		this.i = i;
		this.j = j;
		this.errorcode = errorcode;
	}


	/**
	 * Konstruktor.
	 * 
	 * @param i
	 *            TreeItem, bei dem das Problem auftrat
	 * @param errorcode
	 *            das Problem als Fehlercode
	 */
	public TreeException(
			TreeItem i,
			int errorcode)
	{
		super(getErrorString(i, null, errorcode));
		this.i = i;
		this.j = null;
		this.errorcode = errorcode;
	}


	/**
	 * Konstruktor.
	 * 
	 * @param errorcode
	 *            das Problem als Fehlercode
	 */
	public TreeException(
			int errorcode)
	{
		super(getErrorString(null, null, errorcode));
		this.i = null;
		this.j = null;
		this.errorcode = errorcode;
	}
}
