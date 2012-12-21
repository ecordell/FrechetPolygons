package anja.util;

/**<p>
* ListExceptions werden von BasicList und abgeleiteten Klassen erzeugt.
* Errorcode gibt einen genauere Beschreibung des aufgetretenen Fehlers.
* Da ListException eine Unterklasse von RuntimeException ist, muss dieser
* Fehler nicht mit try...catch abgefangen werden.
*/
public class ListException extends RuntimeException {
 
 // Konstanten fuer die verschiedenen Fehlerarten
 // Fehler beim Zugriff auf einzelne Elemente
 public final static int NO_ERROR=0;
 public final static int CONNECT=1;
 public final static int SET_OWNER=2;
 public final static int REMOVE=3;
 // Andere Zugriffsfehler
 public final static int EMPTY_LIST=1000;
 public final static int NO_SUCH_ELEMENT=1001;
 public final static int ILLEGAL_ACCESS=1002;
 public final static int ILLEGAL_OBJECT_FORMAT=1003;
 public final static int INSERTION_ERROR=1004;
 
 /** ListItems, bei denen ein Problem auftrat. */
 public ListItem i,j;
 /** Fehlercode. Eine entsprechende Fehlerkonstante. */
 public int errorcode=NO_ERROR;

 /**
 * Liefert die Fehlermeldung zum uebergebenen Fehlercode.
 * @param i und
 * @param j ListItems, bei denen der Fehler auftrat
 * @param errorcode Fehlercode
 * @return Fehlermeldung
 */
 private static String getErrorString(ListItem i,ListItem j,int errorcode) {
  String s="Unknown Error";
  switch (errorcode) {
   case NO_ERROR:
    s="No Error.";
    break;
   case CONNECT:
    s="Error connecting "+i+" to "+j+".";
    break;
   case SET_OWNER:
    s="Error setting Owner on "+i+"."; 
    break;
   case REMOVE:
    s="Error removing "+i+"."; 
    break;
   case EMPTY_LIST:
    s="Empty Datastructure.";
    break;
   case NO_SUCH_ELEMENT:
    s="No such Element ind Datastructure.";
    break;
   case ILLEGAL_ACCESS:
    s="Illegal Access Error.";
    break;
   case ILLEGAL_OBJECT_FORMAT:
    s="Illegal Object Format.";
    break;
   case INSERTION_ERROR:
    s="Insertion Error.";
    break;
  }
  return s;
 }
 /**
 * Konstruktor.
 * @param i und
 * @param j ListItems, bei denen das Problem auftrat
 * @param errorcode das Problem als Fehlercode
 */
 public ListException(ListItem i,ListItem j,int errorcode) {
  super(getErrorString(i,j,errorcode));
  this.i=i; this.j=j; this.errorcode=errorcode;
 }
 /**
 * Konstruktor.
 * @param i ListItem, bei dem das Problem auftrat
 * @param errorcode das Problem als Fehlercode
 */
 public ListException(ListItem i,int errorcode) {
  super(getErrorString(i,null,errorcode));
  this.i=i; this.j=null; this.errorcode=errorcode;
 }
 /**
 * Konstruktor.
 * @param errorcode das Problem als Fehlercode
 */
 public ListException(int errorcode) {
  super(getErrorString(null,null,errorcode));
  this.i=null; this.j=null; this.errorcode=errorcode;
 }
}
