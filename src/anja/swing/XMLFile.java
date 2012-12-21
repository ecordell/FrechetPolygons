package anja.swing;


/**
* Enthaelt eine Sammlung aller verwendeten Bezeichner fuer die Elemente und
* Attribute von XML-Szenendateien.
*
* @version 0.6 21.08.2004
* @author Sascha Ternes
*/

public class XMLFile {

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Name des Wurzelelements jeder zu <code>anja.swing</code> kompatiblen
   * XML-Szenendatei
   */
   public static final String ROOT = "scenes";
   /**
   * Wurzelelement: Name des Editor-Attributs
   */
   public static final String ROOT_EDITOR = "editor";
   /**
   * Wurzelelement: Name des Editor-Versions-Attributs
   */
   public static final String ROOT_VERSION = "version";


   /**
   * Beliebige Szene: Name des Szenenobjektfarbe-Attributs
   */
   public static final String COLOR = "color";
   /**
   * Beliebige Szene: Wert des Szenenobjektfarbe-Attributs fuer ein ungefaerbtes
   * Szenenobjekt
   */
   public static final String UNCOLORED = "uncolored";
   /**
   * Beliebige Szene: Name des Szenenobjektname-Attributs
   */
   public static final String NAME = "name";
   /**
   * Beliebige Szene: Name des Szenenobjekt-Zeichenprioritaet-Attributs
   */
   public static final String PRIORITY = "priority";


   /**
   * Name des Elements einer Funktionenszene
   */
//   public static final String FUNCTION = "functions";


   /**
   * Name des Elements einer Graphszene
   */
   public static final String GRAPH = "graph";
   /**
   * Graphszene: Name des Graphmodus-Attributs
   */
   public static final String GRAPH_MODE = "mode";
   /**
   * Graphszene: Wert des Graphmodus-Attributs fuer einen gerichteten Graph
   */
   public static final String GRAPH_MODE_DIRECTED = "directed";
   /**
   * Graphszene: Wert des Graphmodus-Attributs fuer einen ungerichteten Graph
   */
   public static final String GRAPH_MODE_UNDIRECTED = "undirected";
   /**
   * Graphszene: Name des Graphobjektfarbe-Attributs
   */
   public static final String GRAPH_COLOR = "color";
   /**
   * Graphszene: Wert des Graphobjektfarbe-Attributs fuer eine ungefaerbtes
   * Graphobjekt
   */
   public static final String GRAPH_UNCOLORED = "";
   /**
   * Graphszene: Name des Graphobjektgroesse-Attributs
   */
   public static final String GRAPH_SIZE = "size";
   /**
   * Graphszene: Name des Graphobjektdarstellung-Attributs
   */
   public static final String GRAPH_VISUAL = "visual";

   /**
   * Graphszene: Names des Elements der Graphknoten
   */
   public static final String GRAPH_VERTICES = "vertices";
   /**
   * Graphszene: Names des Elements eines Graphknotens
   */
   public static final String GRAPH_VERTEX = "vertex";
   /**
   * Graphszene: Name des Knoten-ID-Attributs
   */
   public static final String GRAPH_VERTEX_ID = "id";
   /**
   * Graphszene: Name des Knotenname-Attributs
   */
   public static final String GRAPH_VERTEX_NAME = "name";
   /**
   * Graphszene: Name des Attributs fuer die x-Koordinate der Knotenposition
   */
   public static final String GRAPH_VERTEX_X = "x";
   /**
   * Graphszene: Name des Attributs fuer die y-Koordinate der Knotenposition
   */
   public static final String GRAPH_VERTEX_Y = "y";

   /**
   * Graphszene: Names des Elements der Graphkanten
   */
   public static final String GRAPH_EDGES = "edges";
   /**
   * Graphszene: Names des Elements einer Graphkante
   */
   public static final String GRAPH_EDGE = "edge";
   /**
   * Graphszene: Name des Kantenname-Attributs
   */
   public static final String GRAPH_EDGE_NAME = "name";
   /**
   * Graphszene: Name des Kantenstartknoten-Attributs
   */
   public static final String GRAPH_EDGE_START = "start";
   /**
   * Graphszene: Name des Kantenzielknoten-Attributs
   */
   public static final String GRAPH_EDGE_TARGET = "target";
   /**
   * Graphszene: Name des Kantengewicht-Attributs
   */
   public static final String GRAPH_EDGE_WEIGHT = "weight";

   /**
   * Graphszene: Names des Elements der Graphkantenpunkte einer Kante
   */
   public static final String GRAPH_SITES = "sites";
   /**
   * Graphszene: Names des Elements eines Graphkantenpunkts
   */
   public static final String GRAPH_SITE = "site";
   /**
   * Graphszene: Name des Kantenpunktname-Attributs
   */
   public static final String GRAPH_SITE_NAME = "name";
   /**
   * Graphszene: Name des Kantenpunktposition-Attributs
   */
   public static final String GRAPH_SITE_POSITION = "position";
   /**
   * Graphszene: Name des Kantenpunktgewicht-Attributs
   */
   public static final String GRAPH_SITE_WEIGHT = "weight";


   /**
   * Name des Elements einer Punktszene
   */
//   public static final String GRAPH = "points";


   /**
   * Name des Elements einer Polygonszene
   */
//   public static final String GRAPH = "polygons";


} // XMLFile
