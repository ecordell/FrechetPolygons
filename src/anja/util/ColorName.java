package anja.util;

import java.awt.Color;

import java.awt.color.ColorSpace;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;


/**
* Diese Klasse dient dazu, Farben Namen zuzuordnen. Objekte dieser Klasse sind
* vollkommen kompatibel zu java.awt.Color-Objekten, daher kann ueberall dort, wo
* ein java.awt.Color-Objekt erwartet wird, auch ein ColorName-Objekt uebergeben
* werden.<p>
*
* Diese Klasse definiert deutlich mehr Farbkonstanten, als dies die Klasse
* java.awt.Color tut. Die englischen Farbnamen sind entnommen aus
* <a href="http://www.w3schools.com/html/html_colornames.asp">HTML Color
* Names</a> des World Wide Web Consortium (W3C), die deutschen Farbnamen sind
* z.T. freie Uebersetzungen. Farben, die in java.awt.Color bereits vorkommen,
* sind in dieser Klasse zusaetzlich definiert, um die Konsistenz in der
* Namensgebung zu wahren.<p>
*
* Weiterhin beinhaltet diese Klasse Methoden fuer geordnete und zufaellige
* Farbaufzaehlungen, die fuer verschiedene Anwendungszwecke recht praktisch sein
* koennen.
*
* @version 1.3 23.10.2004
* @author  Sascha Ternes
*/

public class ColorName
   extends Color
{

   // *************************************************************************
   // Public constants
   // *************************************************************************

   /**
   * Konstante fuer unbekannte Sprache
   */
   public static final int LANG_UNKNOWN = 0;
   /**
   * Konstante fuer deutsche Farbnamen
   */
   public static final int LANG_GERMAN = 1;
   /**
   * Konstante fuer englische Farbnamen
   */
   public static final int LANG_ENGLISH = 2;
   /**
   * Bezeichnungen der Sprachkonstanten in Klartext
   */
   public static final String LANGUAGE[] = { "n/a", "deutsch", "english" };

   /**
   * die voreingestellte Sprache der Farbnamen; in der vorliegenden Version ist
   * dies <a href="#LANG_GERMAN"><code>LANG_GERMAN</code></a>, also Deutsch
   */
   public static final int DEFAULT_LANGUAGE = LANG_GERMAN;

   /*
   * aus Kompatibilitaetsgruenden "umbenannte" Farbkonstanten (13 alt):
   */
   public static final Color BLACK     = new ColorName( Color.black );
   public static final Color BLUE      = new ColorName( Color.blue );
   public static final Color CYAN      = new ColorName( Color.cyan );
   public static final Color DARKGRAY  = new ColorName( Color.darkGray );
   public static final Color GRAY      = new ColorName( Color.gray );
   public static final Color GREEN     = new ColorName( Color.green );
   public static final Color LIGHTGRAY = new ColorName( Color.lightGray );
   public static final Color MAGENTA   = new ColorName( Color.magenta );
   public static final Color ORANGE    = new ColorName( Color.orange );
   public static final Color PINK      = new ColorName( Color.pink );
   public static final Color RED       = new ColorName( Color.red );
   public static final Color WHITE     = new ColorName( Color.white );
   public static final Color YELLOW    = new ColorName( Color.yellow );

   /*
   * neu definierte Farben (HTML Color Names, 127 neu von 140, 13 alt)
   */
   public static final Color ALICEBLUE            = new ColorName( 240, 248, 255 );
   public static final Color ANTIQUEWHITE         = new ColorName( 250, 235, 215 );
   public static final Color AQUAMARINE           = new ColorName( 127, 255, 212 );
   public static final Color AZURE                = new ColorName( 240, 255, 255 );
   public static final Color BEIGE                = new ColorName( 245, 245, 220 );
   public static final Color BISQUE               = new ColorName( 255, 228, 196 );
   public static final Color BLANCHEDALMOND       = new ColorName( 255, 235, 205 );
   public static final Color BLUEVIOLET           = new ColorName( 138,  43, 226 );
   public static final Color BROWN                = new ColorName( 165,  42,  42 );
   public static final Color BURLYWOOD            = new ColorName( 222, 184, 135 );
   public static final Color CADETBLUE            = new ColorName(  95, 158, 160 );
   public static final Color CHARTREUSE           = new ColorName( 127, 255,   0 );
   public static final Color CHOCOLATE            = new ColorName( 210, 105,  30 );
   public static final Color CORAL                = new ColorName( 255, 127,  80 );
   public static final Color CORNFLOWERBLUE       = new ColorName( 100, 149, 237 );
   public static final Color CORNSILK             = new ColorName( 255, 248, 220 );
   public static final Color CRIMSON              = new ColorName( 237, 164,  61 );
   public static final Color DARKBLUE             = new ColorName(   0,   0, 139 );
   public static final Color DARKCYAN             = new ColorName(   0, 139, 139 );
   public static final Color DARKGOLDENROD        = new ColorName( 184, 134,  11 );
   public static final Color DARKGREEN            = new ColorName(   0, 100,   0 );
   public static final Color DARKKHAKI            = new ColorName( 189, 183, 107 );
   public static final Color DARKMAGENTA          = new ColorName( 139,   0, 139 );
   public static final Color DARKOLIVEGREEN       = new ColorName(  85, 107,  47 );
   public static final Color DARKORANGE           = new ColorName( 255, 140,   0 );
   public static final Color DARKORCHID           = new ColorName( 153,  50, 204 );
   public static final Color DARKRED              = new ColorName( 139,   0,   0 );
   public static final Color DARKSALMON           = new ColorName( 233, 150, 122 );
   public static final Color DARKSEAGREEN         = new ColorName( 143, 188, 143 );
   public static final Color DARKSLATEBLUE        = new ColorName(  72,  61, 139 );
   public static final Color DARKSLATEGRAY        = new ColorName(  47,  79,  79 );
   public static final Color DARKTURQUOISE        = new ColorName(   0, 206, 209 );
   public static final Color DARKVIOLET           = new ColorName( 148,   0, 211 );
   public static final Color DEEPPINK             = new ColorName( 255,  20, 147 );
   public static final Color DEEPSKYBLUE          = new ColorName(   0, 191, 255 );
   public static final Color DIMGRAY              = new ColorName( 105, 105, 105 );
   public static final Color DODGERBLUE           = new ColorName(  30, 144, 255 );
   public static final Color FIREBRICK            = new ColorName( 178,  34,  34 );
   public static final Color FLORALWHITE          = new ColorName( 255, 250, 240 );
   public static final Color FORESTGREEN          = new ColorName(  34, 139,  34 );
   public static final Color GAINSBORO            = new ColorName( 220, 220, 220 );
   public static final Color GHOSTWHITE           = new ColorName( 248, 248, 255 );
   public static final Color GOLD                 = new ColorName( 255, 215,   0 );
   public static final Color GOLDENROD            = new ColorName( 218, 165,  32 );
   public static final Color GREENYELLOW          = new ColorName( 173, 255,  47 );
   public static final Color HONEYDEW             = new ColorName( 240, 255, 240 );
   public static final Color HOTPINK              = new ColorName( 255, 105, 180 );
   public static final Color INDIANRED            = new ColorName( 205,  92,  92 );
   public static final Color INDIGO               = new ColorName(  75,   0, 130 );
   public static final Color IVORY                = new ColorName( 255, 255, 240 );
   public static final Color KHAKI                = new ColorName( 240, 230, 140 );
   public static final Color LAVENDER             = new ColorName( 230, 230, 250 );
   public static final Color LAVENDERBLUSH        = new ColorName( 255, 240, 245 );
   public static final Color LAWNGREEN            = new ColorName( 124, 252,   0 );
   public static final Color LEMONCHIFFON         = new ColorName( 255, 250, 205 );
   public static final Color LIGHTBLUE            = new ColorName( 173, 216, 230 );
   public static final Color LIGHTCORAL           = new ColorName( 240, 128, 128 );
   public static final Color LIGHTCYAN            = new ColorName( 224, 255, 255 );
   public static final Color LIGHTGOLDENRODYELLOW = new ColorName( 250, 250, 210 );
   public static final Color LIGHTGREEN           = new ColorName( 144, 238, 144 );
   public static final Color LIGHTPINK            = new ColorName( 255, 182, 193 );
   public static final Color LIGHTSALMON          = new ColorName( 255, 160, 122 );
   public static final Color LIGHTSEAGREEN        = new ColorName(  32, 178, 170 );
   public static final Color LIGHTSKYBLUE         = new ColorName( 135, 206, 250 );
   public static final Color LIGHTSLATEBLUE       = new ColorName( 132, 112, 255 );
   public static final Color LIGHTSLATEGRAY       = new ColorName( 119, 136, 153 );
   public static final Color LIGHTSTEELBLUE       = new ColorName( 176, 196, 222 );
   public static final Color LIGHTYELLOW          = new ColorName( 255, 255, 224 );
   public static final Color LIMEGREEN            = new ColorName(  50, 205,  50 );
   public static final Color LINEN                = new ColorName( 250, 240, 230 );
   public static final Color MAROON               = new ColorName( 128,   0,   0 );
   public static final Color MEDIUMAQUAMARINE     = new ColorName( 102, 205, 170 );
   public static final Color MEDIUMBLUE           = new ColorName(   0,   0, 205 );
   public static final Color MEDIUMGREEN          = new ColorName(   0, 128,   0 );
   public static final Color MEDIUMORCHID         = new ColorName( 186,  85, 211 );
   public static final Color MEDIUMPURPLE         = new ColorName( 147, 112, 219 );
   public static final Color MEDIUMSEAGREEN       = new ColorName(  60, 179, 113 );
   public static final Color MEDIUMSLATEBLUE      = new ColorName( 123, 104, 238 );
   public static final Color MEDIUMSPRINGGREEN    = new ColorName(   0, 250, 154 );
   public static final Color MEDIUMTURQUOISE      = new ColorName(  72, 209, 204 );
   public static final Color MEDIUMVIOLETRED      = new ColorName( 199,  21, 133 );
   public static final Color MIDNIGHTBLUE         = new ColorName(  25,  25, 112 );
   public static final Color MINTCREAM            = new ColorName( 245, 255, 250 );
   public static final Color MISTYROSE            = new ColorName( 255, 228, 225 );
   public static final Color MOCCASIN             = new ColorName( 255, 228, 181 );
   public static final Color NAVAJOWHITE          = new ColorName( 255, 222, 173 );
   public static final Color NAVY                 = new ColorName(   0,   0, 128 );
   public static final Color OLDLACE              = new ColorName( 253, 245, 230 );
   public static final Color OLIVE                = new ColorName( 128, 128,  0  );
   public static final Color OLIVEDRAB            = new ColorName( 107, 142,  35 );
   public static final Color ORANGERED            = new ColorName( 255,  69,   0 );
   public static final Color ORCHID               = new ColorName( 218, 112, 214 );
   public static final Color PALEGOLDENROD        = new ColorName( 238, 232, 170 );
   public static final Color PALEGREEN            = new ColorName( 152, 251, 152 );
   public static final Color PALETURQUOISE        = new ColorName( 175, 238, 238 );
   public static final Color PALEVIOLETRED        = new ColorName( 219, 112, 147 );
   public static final Color PAPAYAWHIP           = new ColorName( 255, 239, 213 );
   public static final Color PEACHPUFF            = new ColorName( 255, 218, 185 );
   public static final Color PERU                 = new ColorName( 205, 133,  63 );
   public static final Color PLUM                 = new ColorName( 221, 160, 221 );
   public static final Color POWDERBLUE           = new ColorName( 176, 224, 230 );
   public static final Color PURPLE               = new ColorName( 128,   0, 128 );
   public static final Color ROSYBROWN            = new ColorName( 188, 143, 143 );
   public static final Color ROYALBLUE            = new ColorName(  65, 105, 225 );
   public static final Color SADDLEBROWN          = new ColorName( 139,  69,  19 );
   public static final Color SALMON               = new ColorName( 250, 128, 114 );
   public static final Color SANDYBROWN           = new ColorName( 244, 164,  96 );
   public static final Color SEAGREEN             = new ColorName(  46, 139,  87 );
   public static final Color SEASHELL             = new ColorName( 255, 245, 238 );
   public static final Color SIENNA               = new ColorName( 160,  82,  45 );
   public static final Color SILVER               = new ColorName( 192, 192, 192 );
   public static final Color SKYBLUE              = new ColorName( 135, 206, 235 );
   public static final Color SLATEBLUE            = new ColorName( 106,  90, 205 );
   public static final Color SLATEGRAY            = new ColorName( 112, 128, 144 );
   public static final Color SNOW                 = new ColorName( 255, 250, 250 );
   public static final Color SPRINGGREEN          = new ColorName(   0, 255, 127 );
   public static final Color STEELBLUE            = new ColorName(  70, 130, 180 );
   public static final Color TAN                  = new ColorName( 210, 180, 140 );
   public static final Color TEAL                 = new ColorName(   0, 128, 128 );
   public static final Color THISTLE              = new ColorName( 216, 191, 216 );
   public static final Color TOMATO               = new ColorName( 255,  99,  71 );
   public static final Color TURQUOISE            = new ColorName(  64, 224, 208 );
   public static final Color VIOLET               = new ColorName( 238, 130, 238 );
   public static final Color VIOLETRED            = new ColorName( 208,  32, 144 );
   public static final Color WHEAT                = new ColorName( 245, 222, 179 );
   public static final Color WHITESMOKE           = new ColorName( 245, 245, 245 );
   public static final Color YELLOWGREEN          = new ColorName( 154, 205,  50 );


   // *************************************************************************
   // Private constants
   // *************************************************************************

   // Anzahl definierter Farben:
   private static final int _COLOR_COUNT = 140; // 127 + 13
   // Anzahl definierter Sprachen:
   private static final int _LANGUAGE_COUNT = 2;

   // Text fuer undefinierte Werte:
   private static final String _NO_NAME = "n/a";

   // Praefix fuer Farbtoene in allen Sprachen:
   // private static final String _NEARLY[] = { "Farbton in ", "tone of " };
   private static final String _NEARLY[] = { "", "" };
   
   // Liste der Farben:
   private static final Color _COLOR[] = { ALICEBLUE, ANTIQUEWHITE,
      AQUAMARINE, AZURE, BEIGE, BISQUE, BLACK, BLANCHEDALMOND, BLUE,
      BLUEVIOLET, BROWN, BURLYWOOD, CADETBLUE, CHARTREUSE, CHOCOLATE, CORAL,
      CORNFLOWERBLUE, CORNSILK, CRIMSON, CYAN, DARKBLUE, DARKCYAN,
      DARKGOLDENROD, DARKGRAY, DARKGREEN, DARKKHAKI, DARKMAGENTA,
      DARKOLIVEGREEN, DARKORANGE, DARKORCHID, DARKRED, DARKSALMON,
      DARKSEAGREEN, DARKSLATEBLUE, DARKSLATEGRAY, DARKTURQUOISE, DARKVIOLET,
      DEEPPINK, DEEPSKYBLUE, DIMGRAY, DODGERBLUE, FIREBRICK, FLORALWHITE,
      FORESTGREEN, GAINSBORO, GHOSTWHITE, GOLD, GOLDENROD, GRAY,
      GREEN, GREENYELLOW, HONEYDEW, HOTPINK, INDIANRED, INDIGO, IVORY, KHAKI,
      LAVENDER, LAVENDERBLUSH, LAWNGREEN, LEMONCHIFFON, LIGHTBLUE, LIGHTCORAL,
      LIGHTCYAN, LIGHTGOLDENRODYELLOW, LIGHTGRAY, LIGHTGREEN, LIGHTPINK,
      LIGHTSALMON, LIGHTSEAGREEN, LIGHTSKYBLUE, LIGHTSLATEBLUE, LIGHTSLATEGRAY,
      LIGHTSTEELBLUE, LIGHTYELLOW, LIMEGREEN, LINEN, MAGENTA, MAROON,
      MEDIUMAQUAMARINE, MEDIUMBLUE, MEDIUMGREEN, MEDIUMORCHID, MEDIUMPURPLE,
      MEDIUMSEAGREEN, MEDIUMSLATEBLUE, MEDIUMSPRINGGREEN, MEDIUMTURQUOISE,
      MEDIUMVIOLETRED, MIDNIGHTBLUE, MINTCREAM, MISTYROSE, MOCCASIN,
      NAVAJOWHITE, NAVY, OLDLACE, OLIVE, OLIVEDRAB, ORANGE, ORANGERED, ORCHID,
      PALEGOLDENROD, PALEGREEN, PALETURQUOISE, PALEVIOLETRED, PAPAYAWHIP,
      PEACHPUFF, PERU, PINK, PLUM, POWDERBLUE, PURPLE, RED, ROSYBROWN,
      ROYALBLUE, SADDLEBROWN, SALMON, SANDYBROWN, SEAGREEN, SEASHELL, SIENNA,
      SILVER, SKYBLUE, SLATEBLUE, SLATEGRAY, SNOW, SPRINGGREEN, STEELBLUE, TAN,
      TEAL, THISTLE, TOMATO, TURQUOISE, VIOLET, VIOLETRED, WHEAT, WHITE,
      WHITESMOKE, YELLOW, YELLOWGREEN };

   // Liste der hellen Farben:
   private static final Color[] _BRIGHT = { ALICEBLUE, ANTIQUEWHITE,
      AQUAMARINE, AZURE, BEIGE, BISQUE, BLANCHEDALMOND, BURLYWOOD, CHARTREUSE,
      CORNSILK, CYAN, DARKGRAY, DARKKHAKI, DARKSALMON, DARKSEAGREEN,
      FLORALWHITE, GAINSBORO, GHOSTWHITE, GOLD, GREENYELLOW, HONEYDEW, IVORY,
      KHAKI, LAVENDER, LAVENDERBLUSH, LAWNGREEN, LEMONCHIFFON, LIGHTBLUE,
      LIGHTCYAN, LIGHTGOLDENRODYELLOW, LIGHTGRAY, LIGHTGREEN, LIGHTPINK,
      LIGHTSALMON, LIGHTSKYBLUE, LIGHTSTEELBLUE, LIGHTYELLOW, LIMEGREEN, LINEN,
      MEDIUMAQUAMARINE, MEDIUMSPRINGGREEN, MEDIUMTURQUOISE, MINTCREAM,
      MISTYROSE, MOCCASIN, NAVAJOWHITE, OLDLACE, OLIVEDRAB, ORANGE, ORCHID,
      PALEGOLDENROD, PALEGREEN, PALETURQUOISE, PAPAYAWHIP, PEACHPUFF, PERU,
      PINK, PLUM, POWDERBLUE, ROSYBROWN, SANDYBROWN, SEASHELL, SILVER, SKYBLUE,
      SLATEGRAY, SNOW, SPRINGGREEN, STEELBLUE, TAN, THISTLE, TURQUOISE, VIOLET,
      WHEAT, WHITE, WHITESMOKE, YELLOW, YELLOWGREEN };

   // Liste der dunklen Farben:
   private static final Color[] _DARK = { BLACK, BLUE, BLUEVIOLET, BROWN,
      CADETBLUE, CHOCOLATE, CORAL, CORNFLOWERBLUE, CRIMSON, DARKBLUE, DARKCYAN,
      DARKGOLDENROD, DARKGREEN, DARKMAGENTA, DARKOLIVEGREEN, DARKORANGE,
      DARKORCHID, DARKRED, DARKSLATEBLUE, DARKSLATEGRAY, DARKTURQUOISE,
      DARKVIOLET, DEEPPINK, DEEPSKYBLUE, DIMGRAY, DODGERBLUE, FIREBRICK,
      FORESTGREEN, GOLDENROD, GRAY, GREEN, HOTPINK, INDIANRED, INDIGO,
      LIGHTCORAL, LIGHTSEAGREEN, LIGHTSLATEBLUE, LIGHTSLATEGRAY, MAGENTA,
      MAROON, MEDIUMBLUE, MEDIUMGREEN, MEDIUMORCHID, MEDIUMPURPLE,
      MEDIUMSEAGREEN, MEDIUMSLATEBLUE, MEDIUMVIOLETRED, MIDNIGHTBLUE, NAVY,
      OLIVE, ORANGERED, PALEVIOLETRED, PURPLE, RED, ROYALBLUE, SADDLEBROWN,
      SALMON, SEAGREEN, SIENNA, SLATEBLUE, TEAL, TOMATO, VIOLETRED };

   // geordnete Liste mit kraeftigen bunten Farben:
   private static final Color[] _UNIVERSAL10 = { BLUE, BROWN, RED, ORANGE,
      YELLOW, GREEN, DARKGREEN, CYAN, INDIGO, MAGENTA   };

   // Liste der deutschen Farbtonnamen:
   private static final String _GERMAN_NAME[] = { "Pastellblau",
      "Antikweiss", "Aquamarin", "Azur", "Beige", "Bisque", "Schwarz",
      "Crème", "Blau", "Blauviolett", "Braun", "BurlyWood",
      "Mittelzyan", "Neongruen", "Chocolate", "Korallrot", "Kornblumenblau",
      "Cornsilk", "Karminrot", "Zyan", "Dunkelblau", "Dunkelcyan",
      "Dunkelbronze", "Dunkelgrau", "Dunkelgruen", "Dunkelkhaki",
      "Dunkelmagenta", "Dunkeloliv", "Dunkelorange", "Dunkelorchidée",
      "Dunkelrot", "Dunkellachs", "DarkSeaGreen", "DarkSlateBlue",
      "Dunkelblaugrau", "Dunkeltuerkis", "Dunkelviolett", "Tiefrosa",
      "Tiefhimmelblau", "DimGray", "DodgerBlue", "FireBrick", "FloralWhite",
      "Grasgruen", "Gainsboro", "GhostWhite", "Gold", "Bronze",
      "Grau", "Gruen", "Gruengelb", "HoneyDew", "Pink", "IndianRed", "Indigo",
      "Ivory", "Khaki", "Lavender", "LavenderBlush", "LawnGreen",
      "LemonChiffon", "Hellblau", "Hellkorallrot", "Hellzyan",
      "LightGoldenRodYellow", "Hellgrau", "Hellgruen", "Hellrosa",
      "Helllachs", "Hellseegruen", "Hellhimmelblau", "LightSlateBlue",
      "Hellblaugrau", "Hellstahlblau", "Hellgelb", "Limonengruen",
      "Leinen", "Magenta", "Marone", "Mittelaquamarin", "Mittelblau",
      "Mittelgruen", "Mittelorchid", "Mittelpurpur", "Mittelseegruen",
      "Mittelblaugrau", "MediumSpringGreen", "Mitteltuerkis",
      "Mittelviolettrot", "Schwarzblau", "Mint", "Rosse", "Pfirsisch", "Sand",
      "Marineblau", "Beigeweiss", "Oliv", "Olivgruen", "Orange", "Orangerot",
      "Orchid", "PaleGoldenRod", "Blassgruen", "Blasstuerkis",
      "Blassviolettrot", "PapayaWhip", "PeachPuff", "Rotbraun", "Rosa",
      "Pflaumenblau", "Rauchblau", "Purpur", "Rot", "RosyBrown", "Koenigsblau",
      "Dunkelbraun", "Lachs", "SandyBrown", "Seegruen", "SeaShell", "Sienna",
      "Silber", "Himmelblau", "SlateBlue", "Blaugrau", "Schneeweiss",
      "SpringGreen", "Stahlblau", "Tan", "Teal", "Blassviolett", "Tomate",
      "Tuerkis", "Violett", "Violettrot", "Weizen", "Weiss", "Rauchweiss",
      "Gelb", "Gelbgruen" };

   // Liste der englischen Farbtonnamen:
   private static final String _ENGLISH_NAME[] = { "AliceBlue", "AntiqueWhite",
      "Aquamarine", "Azure", "Beige", "Bisque", "Black",
      "BlanchedAlmond", "Blue", "BlueViolet", "Brown", "BurlyWood",
      "CadetBlue", "Chartreuse", "Chocolate", "Coral", "CornflowerBlue",
      "Cornsilk", "Crimson", "Cyan", "DarkBlue", "DarkCyan", "DarkGoldenRod",
      "DarkGray", "DarkGreen", "DarkKhaki", "DarkMagenta", "DarkOliveGreen",
      "DarkOrange", "DarkOrchid", "DarkRed", "DarkSalmon", "DarkSeaGreen",
      "DarkSlateBlue", "DarkSlateGray", "DarkTurquoise", "DarkViolet",
      "DeepPink", "DeepSkyBlue", "DimGray", "DodgerBlue", "FireBrick",
      "FloralWhite", "ForestGreen", "Gainsboro", "GhostWhite",
      "Gold", "GoldenRod", "Gray", "Green", "GreenYellow", "HoneyDew",
      "HotPink", "IndianRed", "Indigo", "Ivory", "Khaki", "Lavender",
      "LavenderBlush", "LawnGreen", "LemonChiffon", "LightBlue", "LightCoral",
      "LightCyan", "LightGoldenRodYellow", "LightGray", "LightGreen",
      "LightPink", "LightSalmon", "LightSeaGreen", "LightSkyBlue",
      "LightSlateBlue", "LightSlateGray", "LightSteelBlue", "LightYellow",
      "LimeGreen", "Linen", "Magenta", "Maroon", "MediumAquaMarine",
      "MediumBlue", "MediumGreen", "MediumOrchid", "MediumPurple",
      "MediumSeaGreen", "MediumSlateBlue", "MediumSpringGreen",
      "MediumTurquoise", "MediumVioletRed", "MidnightBlue", "MintCream",
      "MistyRose", "Moccasin", "NavajoWhite", "Navy", "OldLace", "Olive",
      "OliveDrab", "Orange", "OrangeRed", "Orchid", "PaleGoldenRod",
      "PaleGreen", "PaleTurquoise", "PaleVioletRed", "PapayaWhip", "PeachPuff",
      "Peru", "Pink", "Plum", "PowderBlue", "Purple", "Red", "RosyBrown",
      "RoyalBlue", "SaddleBrown", "Salmon", "SandyBrown", "SeaGreen",
      "SeaShell", "Sienna", "Silver", "SkyBlue", "SlateBlue", "SlateGray",
      "Snow", "SpringGreen", "SteelBlue", "Tan", "Teal", "Thistle", "Tomato",
      "Turquoise", "Violet", "VioletRed", "Wheat", "White", "WhiteSmoke",
      "Yellow", "YellowGreen" };


   // *************************************************************************
   // Private variables
   // *************************************************************************

   // aktuell eingestellte Sprache:
   private static int _language;

   // fuer jede Sprache Speicherung aller Farbnamen:
   private static HashMap _name[];


   // *************************************************************************
   // Class constructor
   // *************************************************************************

   static {

      // Sprache setzen und Namentabelle intialisieren:
      _language = DEFAULT_LANGUAGE;
      _name = new HashMap[_LANGUAGE_COUNT];

      // pro Sprache eine eigene Namentabelle:
      for ( int i = 0; i < _LANGUAGE_COUNT; i++ ) {
         _name[i] = new HashMap( _COLOR_COUNT + 1, 1.0f );
      } // for

      // verknuepft Farben mit Namen in allen Sprachen:
      for ( int i = 0; i < _COLOR_COUNT; i++ ) {
          _name[0].put( _COLOR[i], _GERMAN_NAME[i] );
          _name[1].put( _COLOR[i], _ENGLISH_NAME[i] );
      } // for

   } // static


   // *************************************************************************
   // Class methods
   // *************************************************************************

   /**
   * Liefert den Namen einer Farbe in der aktuell eingestellten Sprache.
   *
   * @param color die Farbe, zu der der Name gesucht wird
   * @return den Farbnamen
   */
   public static String name(
      Color color
   ) {

      String name = _getName( color );
      if ( name == null ) {
         name = _NO_NAME;
      } // if
      return name;

   } // name


   /**
   * Setzt die aktuelle Sprache. Hierfuer sollten die <i>Sprachkonstanten</i>
   * verwendet werden.<br>
   * Wenn die gewuenschte Sprache nicht existiert, wird stattdessen die
   * <a href="#DEFAULT_LANGUAGE">Defaultsprache</a> verwendet.
   *
   * @param language die Sprache
   */
   public static void setLanguage(
      int language
   ) {

      if ( ( language <= 0 ) || ( language > _LANGUAGE_COUNT ) ) {
         _language = DEFAULT_LANGUAGE;
      } else { // if
         _language = language;
      } // else

   } // setLanguage


   /**
   * Liefert den RGB-Wert (ohne Alpha-Komponente) als sechsstelligen
   * hexadezimalen String.
   *
   * @return den RGB-Wert als Hexstring
   */
   public static String getHexRGB(
      Color color
   ) {

      String hex = "";
      if ( color.getRed() < 16 ) hex += "0";
      hex += Integer.toHexString( color.getRed() );
      if ( color.getGreen() < 16 ) hex += "0";
      hex += Integer.toHexString( color.getGreen() );
      if ( color.getBlue() < 16 ) hex += "0";
      hex += Integer.toHexString( color.getBlue() );
      return hex;

   } // getHexRGB


   /**
   * Liefert eine zufaellige unendliche Folge von Farben.
   *
   * @return eine zufaellige Aufzaehlung von Farben
   */
   public static Enumeration randomColors() {

      return new InfiniteRandomEnumeration( _COLOR );

   } // randomColors


   /**
   * Liefert eine zufaellige unendliche Folge von "hellen" Farben, die als
   * Vordergrundfarben fuer dunkle Hintergruende geeignet sind.
   *
   * @return eine zufaellige Aufzaehlung heller Farben
   */
   public static Enumeration randomBrightColors() {

      return new InfiniteRandomEnumeration( _BRIGHT );

   } // randomBrightColors


   /**
   * Liefert eine zufaellige unendliche Folge von "dunklen" Farben, die als
   * Vordergrundfarben fuer helle Hintergruende geeignet sind.
   *
   * @return eine zufaellige Aufzaehlung dunkler Farben
   */
   public static Enumeration randomDarkColors() {

      return new InfiniteRandomEnumeration( _DARK );

   } // randomDarkColors


   /**
   * Liefert eine unendliche geordnete Folge aus 10 kraeftigen bunten Farben,
   * die universell als Vordergrundfarben fuer helle Hintergruende eingesetzt
   * werden koennen.
   *
   * @return eine Aufzaehlung kraeftiger bunter Farben
   */
   public static Enumeration universal10Colors() {

      return new InfiniteArrayEnumeration( _UNIVERSAL10 );

   } // universalColors


   /**
   * Liefert die HSB-Werte einer Farbe.
   *
   * @param color
   * 			Die Farbe
   * @return die Werte fuer Farbton, Saettigung und Helligkeit
   *         (in dieser Reihenfolge) in einem Array
   */
   public static float[] getHSB(
      Color color
   ) {

      return RGBtoHSB( color.getRed(), color.getGreen(), color.getBlue(),
                                                                        null );

   } // getHSB


   /**
   * Liefert entweder die Farbe <code>BLACK</code> oder die Farbe
   * <code>WHITE</code> zurueck, abhaengig von Helligkeit und Saettigung der
   * spezifizierten Farbe, um den besten Kontrast beispielsweise fuer
   * Beschriftungen zu erzielen.
   *
   * @param color die Farbe
   * @return <code>Color.BLACK</code> oder <code>Color.WHITE</code>
   */
   public static Color blackOrWhite(
      Color color
   ) {

      float[] hsb = getHSB( color );
      if ( ( hsb[1] >= 0.45f ) || ( hsb[2] < 0.6f ) )
         return Color.WHITE;
      return Color.BLACK;

   } // getBrightness


   /*
   * Bestimmt den Farbnamen.
   */
   private static String _getName(
      Color color
   ) {

      // zuerst exakte Farbe suchen:
      String name = (String) _name[_language-1].get( color );
      if ( name != null ) {
         return name;
      } // if

      // sonst Naeherung suchen:
      int r0 = color.getRed();
      int g0 = color.getGreen();
      int b0 = color.getBlue();
      int diff = 3 * 256; // Summe der R-G-B-Differenzen initialisieren
      Color nearest = null;
      // Aufzaehlung aller registrierten Farben durchsuchen:
      Iterator i = _name[_language-1].keySet().iterator();
      while ( i.hasNext() ) {
         // summierte Differenz der R-G-B-Werte berechnen:
         Color col = (Color) i.next();
         int r = col.getRed();
         int g = col.getGreen();
         int b = col.getBlue();
         int d = Math.abs( r - r0 ) + Math.abs( g - g0 ) + Math.abs( b - b0 );
         // naechstliegende Farbe sichern:
         if ( d < diff ) {
            // neue Naeherungsfarbe gefunden:
            diff = d;
            nearest = col;
         } // if
      } // while
      // Name der Naeherungsfarbe zurueckliefern:
      return _NEARLY[_language-1] + (String) _name[_language-1].get( nearest );

   } // _getName


   // *************************************************************************
   // Constructors
   // *************************************************************************

   /*
   * Alle Konstruktoren bis auf den letzten sind aus java.awt.Color uebernommen
   * und unveraendert in der Funktionalitaet. Es wird lediglich diese Klasse,
   * falls erforderlich, initialisiert.
   */

   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      ColorSpace cspace,
      float[] components,
      float alpha
   ) {

      super( cspace, components, alpha );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      float r,
      float g,
      float b
   ) {

      super( r, g, b );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      float r,
      float g,
      float b,
      float a
   ) {

      super( r, g, b, a );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      int rgb
   ) {

      super( rgb );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      int rgba,
      boolean hasalpha
   ) {

      super( rgba, hasalpha );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      int r,
      int g,
      int b
   ) {

      super( r, g, b );

   } // ColorName


   /**
   * Entsprechend vererbter Konstruktor aus <code>java.awt.Color</code>, siehe
   * dort.
   */
   public ColorName(
      int r,
      int g,
      int b,
      int a
   ) {

      super( r, g, b, a );

   } // ColorName


   /**
   * Konvertiert eine Farbe der <code>Color</code>-Klasse in eine
   * <code>ColorName</code>-Farbe.
   *
   * @param color das zu konvertierende <code>Color</code>-Objekt
   */
   public ColorName(
      Color color
   ) {

      this( color.getRed(), color.getGreen(), color.getBlue(),
                                                            color.getAlpha() );

   } // ColorName


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Liefert den Farbnamen.
   *
   * @return den Farbnamen
   */
   public String toString() {

      String name = _getName( this );
      if ( name == null ) {
         return _NO_NAME;
      } // if
      return name;

   } // toString


} // ColorName
