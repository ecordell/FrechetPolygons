package anja.obstaclescene;

import java.awt.Point;

/**
 * Die Klasse Calculator enthaelt Methoden fuer diverse Berechnungen.
 *
 * @version 0.1 20.01.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class Calculator
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************

    public final static double TWO_PI  = Math.PI * 2;
    public final static double PI      = Math.PI;
    public final static double HALF_PI = Math.PI / 2;


    // Orientierung ( @see orientation(..) )
    //
    public final static int COLLINEAR = 0;
    public final static int LEFT      = 1;
    public final static int RIGHT     = 2;


    //************************************************************
    // public class methods
    //************************************************************


    /**
     * Berechnen des quadrierten Abstands zwischen den Punkten
     * (<x1>,<y1>) und (<x2>,<y2>).
     */

    //============================================================
    public static long squareDistance( int x1, int y1,
                                       int x2, int y2 )
    //============================================================
    {
        long dist_x = x2 - x1;
        long dist_y = y2 - y1;

        return ( dist_x * dist_x ) + ( dist_y * dist_y );
    } // squareDistance


    /**
     * Berechnen des Abstands zwischen den Punkten (<x1>,<y1>)
     * und (<x2>,<y2>).
     *
     * Der berechnete Abstand ist nicht unbedingt mathematisch genau.
     *  Beispiel: ( x1 == 0, y1 == 1, x2 == 1, y2 == 0 ) ==>
     *            mathematisch genauer Abstand == Wurzel aus 2
     */

    //============================================================
    public static double distance( int x1, int y1,
                                   int x2, int y2 )
    //============================================================
    {
        return Math.sqrt( squareDistance( x1, y1, x2, y2 ) );
    } // distance


    /**
     * Berechnen des Winkels ( im Bogenmass ) der Geraden vom Punkt
     * (<x1>,<y1>) zum Punkt (<x2>,<y2>)
     *
     * Fuer den ermittelten Winkel gilt immer
     *   0.0 <= angle < 2*PI
     *
     * Falls die beiden Koordinatenpaare gleich sind, wird 0.0
     * zurueckgegeben
     */

    //============================================================
    public static double getAngle( int x1, int y1, int x2, int y2 )
    //============================================================
    {
        if( x1 == x2 && y1 == y2 )
            return 0.0; // Koordinatenpaare sind identisch

        // Winkel berechnen und normalisieren
        double angle = Math.atan2( y2 - y1, x2 - x1 );
        if( angle < 0.0 )
            angle += TWO_PI;

        return angle;
    } // getAngle


    /**
     * Ermitteln des normalisierten Winkels des Winkels < angle>.
     * Ein normalisierter Winkel ist groesser gleich 0.0 und kleiner
     * als 2*PI
     *
     * @param angle  Zu normalisierender Winkel im Bogenmass
     *
     * @return  Normalisierter Winkel im Bogenmass
     *          Es gilt 0 <= norm_angle < 2*PI
     */

    //============================================================
    public static double getNormalizedAngle( double angle )
    //============================================================
    {
        if( angle >= 0.0 && angle < TWO_PI )
            return angle; // Der gegebene Winkel ist bereits
                          // normalisiert


        // Anzahl der Vollkreise berechnen, die vom Winkel
        // angle abzuziehen sind
        //
        double turn = Math.floor( angle / TWO_PI );

        // angle normalisieren
        //
        angle -= ( turn * TWO_PI );

        // Mathematisch gesehen ist angle jetzt normalisiert.
        // Trotzdem wird der Winkel noch mal getestet und ggf.
        // korrigiert, um Fehler durch double-Ungenauigkeiten
        // auszuschliessen

        if( angle < 0.0 )
            angle += TWO_PI;
        else
        if( angle >= TWO_PI )
            angle -= TWO_PI;

        return angle;
    } // getNormalizedAngle


    /**
     * Testen ob der Winkel < angle> im Bereich liegt, der von den
     * Winkeln < angle_start> und < angle_end> gegen den Uhrzeigersinn
     * aufgespannt wird.
     */

    //============================================================
    public static boolean isAngleInside( double angle,
                                         double angle_start,
                                         double angle_end )
    //============================================================
    {
        angle       = getNormalizedAngle( angle );
        angle_start = getNormalizedAngle( angle_start );
        angle_end   = getNormalizedAngle( angle_end );

        if( angle_end < angle_start )
            angle_end += TWO_PI;

        if( angle < angle_start )
            angle += TWO_PI;

        return ( angle <= angle_end );
    } // isAngleInside


    /**
     * Berechnen der doppelten Flaeche des Dreiecks
     * (<x1>,<y1>),(<x2>,<y2>),(<x3>,<y3>)
     *
     * Es wird die doppelte Flaeche berechnet, weil dieses Ergebnis
     * mathematisch genau ist. Nach dem Teilen durch 2 waere das nicht
     * mehr unbedingt der Fall.
     *
     * @return  Berechnete doppelte Dreiecksflaeche
     *
     *          ( doubleArea  > 0 ) ==> die Punkte (1,2,3) laufen gegen
     *                                  den Uhrzeigersinn
     *          ( doubleArea  < 0 ) ==> die Punkte (1,2,3) laufen im
     *                                  Uhrzeigersinn
     *          ( doubleArea == 0 ) ==> Alle 3 Punkte liegen im
     *                                  mathematisch exakten Sinne auf
     *                                  einer gemeinsamen Geraden
     */

    //============================================================
    public static long doubleArea( int x1, int y1,
                                   int x2, int y2,
                                   int x3, int y3 )
    //============================================================
    {
        long y_1_2 = y1 - y2;
        long y_2_3 = y2 - y3;
        long y_3_1 = y3 - y1;

        return ( y_2_3 * x1 ) + ( y_3_1 * x2 ) + ( y_1_2 * x3 );
    } // doubleArea


    /**
     * Testen, ob die Punkte (<x1>,<y1>), (<x2>,<y2>) und (<x3>,<y3>)
     * auf einer gemeinsamen Geraden liegen.
     *
     * @return  true <==> Die gegebenen Punkte liegen auf einer
     *                    gemeinsamen Geraden
     */

    //============================================================
    public static boolean pointsAreCollinear( int x1, int y1,
                                              int x2, int y2,
                                              int x3, int y3 )
    //============================================================
    {
        return doubleArea( x1, y1, x2, y2, x3, y3 ) == 0;
    } // pointsAreCollinear


    /**
     * Ermitteln der Orientierung des Punktes (<x,y>) relativ zur
     * Strecke (<x1>,<y1>) --> (<x2>,<y2>)
     *
     * @return  COLLINEAR, LEFT oder RIGHT
     */

    //============================================================
    public static int orientation( int x,  int y,
                                   int x1, int y1,
                                   int x2, int y2 )
    //============================================================
    {
        long a = doubleArea( x, y, x1, y1, x2, y2 );

        if( a > 0 )
            return LEFT;

        if( a < 0 )
            return RIGHT;

        // Es gilt also a == 0
        return COLLINEAR;
    } // orientation


    /**
     * Testen ob die Strecke (<x1>,<y1>) --- (<x2>,<y2>) die durch
     * die Punkte (<xmin>-0.5,<ymin>-0.5) und (<xmax>+0.5,<ymax>+0.5)
     * gegebene Rechteckflaeche schneidet.
     *
     * @return  true <==> Strecke schneidet Rechteckflaeche
     */

    //============================================================
    public static boolean lineIntersectsRect( int x1, int y1,
                                              int x2, int y2,
                                              int xmin, int ymin,
                                              int xmax, int ymax )
    //============================================================
    {
        if( x2 < x1 )
        {
            if( y2 < y1 )                       // x2 < x1  &&  y2 < y1
            {
                // Punkte (x1,y1) und (x2,y2) vertauschen und dann
                // auf Schnitt testen
                //
                return _lineIntersectsRect( x2, y2, x1, y1,
                                            xmin, ymin,
                                            xmax, ymax );
            }
            else                                // x2 < x1  && y1 <= y2
            {
                // x-Koordinaten negieren und dann auf Schnitt testen
                //
                return _lineIntersectsRect( -x1, y1, -x2, y2,
                                            -xmax, ymin,
                                            -xmin, ymax );
            } // else
        }
        else // x1 <= x2
        {
            if( y2 < y1 )                       // x1 <= x2 &&  y2 < y1
            {
                // y-Koordinaten negieren und dann auf Schnitt testen
                //
                return _lineIntersectsRect( x1, -y1, x2, -y2,
                                            xmin, -ymax,
                                            xmax, -ymin );
            }
            else                                // x1 <= x2 && y1 <= y2
            {
                // auf Schnitt testen
                //
                return _lineIntersectsRect( x1, y1, x2, y2,
                                            xmin, ymin,
                                            xmax, ymax );
            } // else
        } // else
    } // lineIntersectsRect


    /**
     * Testen ob die Strecke (<x1>,<y1>)---(<x2>,<y2>) die Strecke
     * (<x3>,<y3>)---(<x4>,<y4>) "echt" schneidet.
     *
     * @return  true <==> Die Strecke (<x1>,<y1>)---(<x2>,<y2> schneidet
     *                    die Strecke (<x3>,<y3>)---(<x4>,<y4>) "echt"
     */

    //============================================================
    public static boolean lineIntersectsLine( int x1, int y1,
                                              int x2, int y2,
                                              int x3, int y3,
                                              int x4, int y4 )
    //============================================================
    {
        long area_1 = doubleArea( x1, y1, x3, y3, x2, y2 );
        long area_2 = doubleArea( x1, y1, x2, y2, x4, y4 );

        if(    ( area_1 <= 0 && area_2 >= 0 )
            || ( area_1 >= 0 && area_2 <= 0 )
            )
            return false; // Die Strecke (x1,y1)---(x2,y2) liegt
                          // vollstaendig auf einer Seite der
                          // Strecke (x3,y3)---(x4,y4).
                          // Also kein Schnitt

        area_1 = doubleArea( x3, y3, x2, y2, x4, y4 );
        area_2 = doubleArea( x3, y3, x4, y4, x1, y1 );

        if(    ( area_1 <= 0 && area_2 >= 0 )
            || ( area_1 >= 0 && area_2 <= 0 )
            )
            return false; // Die Strecke (x3,y3)---(x4,y4) liegt
                          // vollstaendig auf einer Seite der
                          // Strecke (x1,y1)---(x2,y2).
                          // Also kein Schnitt

        return true;
    } // lineIntersectsLine







    /**
     * Testen, ob der Punkt (<x,y>) auf der Strecke von
     * (<x1,y1>) nach (<x2,y2>) liegt.
     *
     * ACHTUNG !!! Dies ist kein mathematisch exakter Test.
     *
     * Die Methode liefert genau dann true, wenn die Strecke
     * (<x1>,<y1>)---(<x2>,<y2>) den durch den Punkt (<x>,<y>)
     * gegebenen quadratischen Koordinatenbereich schneidet.
     *
     * Der quadratische Koordinatenbereich eines Punktes (x,y) ist das
     * Quadrat mit den Grenzkoordinaten  x-0.5, x+0,5, y-0.5 und y+0.5
     *
     * @return  true <=> Der Punkt (<x>,<y>) liegt auf der
     *                   Strecke (<x1>,<y1>) -- (<x2>,<y2>)
     */
/*
    //============================================================
    public static boolean pointLiesOnSegment( int x,  int y,
                                              int x1, int y1,
                                              int x2, int y2 )
    //============================================================
    {
        if( x2 < x1 )
        {
            // x-Koordinaten umkehren, so dass gilt x1 <= x2
            x  = -x;
            x1 = -x1;
            x2 = -x2;
        } // if

        if( y2 < y1 )
        {
            // y-Koordinaten umkehren, so dass gilt y1 <= y2
            y  = -y;
            y1 = -y1;
            y2 = -y2;
        } // if

        // Es gilt jetzt x1 <= x2 und y1 <= y2

        if( x < x1 || x > x2 || y < y1 || y > y2 )
            return false; // Der Punkt (x,y) liegt ausserhalb des
                          // Rechtecks, welches die Strecke umgibt,
                          // also nicht auf der Strecke

        long d_x2 = x2 - x1;
        long d_y2 = y2 - y1;

        long d_x  = x - x1;
        long d_y  = y - y1;

        long value = 2 * ( d_y * d_x2 - d_x * d_y2 );

        return ( value < 0 ) ?
                -value <= d_x2 + d_y2 :
                 value <= d_x2 + d_y2 ;
    } // pointLiesOnSegment
*/

    /**
     * Testen, ob die Strecke (<x1>,<y1>)---(<x2>,<y2>) den durch den
     * Punkt (<x>,<y>) gegebenen quadratischen Koordinatenbereich
     * schneidet.
     *
     * Der quadratische Koordinatenbereich eines Punktes (x,y) ist das
     * Quadrat mit den Grenzkoordinaten  x-0.5, x+0,5, y-0.5 und y+0.5
     *
     * @param  ignore_1  true ==>
     *                   Falls der Punkt (<x>,<y>) mit dem Punkt
     *                   (<x1>,<y1>) uebereinstimmt, so wird false
     *                   zurueckgegeben
     *
     * @param  ignore_2  wie ignore_1 fuer (<x2>,<y2>)
     *
     * @return  true <=> Die Strecke (<x1>,<y1>)---(<x2>,<y2>) schneidet
     *                   den quadratischen Bereich des Punktes (<x>,<y>)
     */
/*
    //============================================================
    public static boolean segmentIntersectsPoint( int x1, int y1,
                                                  int x2, int y2,
                                                  int x,  int y,
                                                  boolean ignore_1,
                                                  boolean ignore_2 )
    //============================================================
    {
        if( x == x1 && y == y1 )
            return ( ! ignore_1 );

        if( x == x2 && y == y2 )
            return ( ! ignore_2 );

        return pointLiesOnSegment( x, y, x1, y1, x2, y2 );
    } // segmentIntersectsPoint
*/

    /**
     * Testen ob die Strecke (<x1>,<y1>)---(<x2>,<y2>) die Strecke
     * (<x3>,<y3>)---(<x4>,<y4>) schneidet.
     *
     * @param  ignore_1  true ==>
     *                   Falls sich die beiden Strecken nur im Punkt
     *                   (<x1>,<y1>) beruehren, so wird false
     *                   zurueckgegeben
     *
     * @param  ignore_2  wie ignore_1 fuer (<x2>,<y2>)
     *
     * @return  true <==> Die Strecke (<x1>,<y1>)---(<x2>,<y2> schneidet
     *                    die Strecke (<x3>,<y3>)---(<x4>,<y4>)
     */
/*
    //============================================================
    public static boolean segmentIntersectsSegment( int x1, int y1,
                                                    int x2, int y2,
                                                    int x3, int y3,
                                                    int x4, int y4,
                                                    boolean ignore_1,
                                                    boolean ignore_2 )
    //============================================================
    {
        return intersectionSegmentSegment( x1, y1,
                                           x2, y2,
                                           x3, y3,
                                           x4, y4,
                                           ignore_1,
                                           ignore_2,
                                           null,
                                           null );
    } // segmentIntersectsSegment
*/

    /**
     * Berechnen des Schnittpunktes bzw. der Schnittpunkte zwischen den
     * Strecken (<x1>,<y1>)---(<x2>,<y2>) und (<x3>,<y3>)---(<x4>,<y4>)
     *
     * Es werden 2 Schnittpunkte berechnet ( @see <first> und <last> ).
     * Wenn die beiden Strecken nicht auf einer gemeinsamen Geraden
     * liegen, so sind diese identisch.
     *
     * @param  ignore_1  true ==>
     *                   Falls sich die beiden Strecken nur im Punkt
     *                   (<x1>,<y1>) beruehren, so wird dieser
     *                   Schnittpunkt ignoriert
     *
     * @param  ignore_2  wie ignore_1, aber fuer (<x2>,<y2>)
     *
     * @param  first     Falls nicht <null> werden in diesen Punkt
     *                   die Koordinaten des erster Schnittpunkts auf
     *                   dem Weg von (<x1>,<y1>) nach (<x2>,<y2>)
     *                   eingetragen
     *
     * @param  last      wie first, aber fuer letzten Schnittpunkt
     *
     * @return false <==> Es gibt keinen Schnittpunkt zwischen den
     *                    Strecken
     */
/*
    //============================================================
    public static boolean
        intersectionSegmentSegment( int x1, int y1,
                                    int x2, int y2,
                                    int x3, int y3,
                                    int x4, int y4,
                                    boolean ignore_1,
                                    boolean ignore_2,
                                    Point   first,
                                    Point   last )
    //============================================================
    {
        int xmin = ( x1 <= x2 ) ? x1 : x2;
        int xmax = ( x1 <= x2 ) ? x2 : x1;
        int ymin = ( y1 <= y2 ) ? y1 : y2;
        int ymax = ( y1 <= y2 ) ? y2 : y1;

        if(    ( x3 < xmin && x4 < xmin )
            || ( x3 > xmax && x4 > xmax )
            || ( y3 < ymin && y4 < ymin )
            || ( y3 > ymax && y4 > ymax )
            )
            return false; // Segment 3---4 liegt vollstaendig ausserhalb
                          // des Rechtecks welches Segment 1---2
                          // umgibt, also kein Schnitt


        // In folgendem werden alle Faelle behandelt, in denen
        // ein Endpunkt einer Strecke genau auf der anderen Strecke
        // liegt

        // i---j bedeutet: erster  Schnittpunkt ist (xi,yi)
        //                 letzter Schnittpinkt ist (xj,yj)

        boolean i_1 = pointLiesOnSegment( x1, y1, x3, y3, x4, y4 );
        boolean i_2 = pointLiesOnSegment( x2, y2, x3, y3, x4, y4 );

        boolean i_3 = pointLiesOnSegment( x3, y3, x1, y1, x2, y2 );
        boolean i_4 = pointLiesOnSegment( x4, y4, x1, y1, x2, y2 );

        if( i_1 )
        {
            _setPointCoors( first, x1, y1 );

            if( i_2 && ( x1 != x2 || y1 != y2 ) )
            {
                                                    // 1---2
                _setPointCoors( last, x2, y2 );
                return true;
            } // if

            if( i_3 && ( x1 != x3 || y1 != y3 ) )
            {
                                                    // 1---3
                _setPointCoors( last, x3, y3 );
                return true;
            } // if

            if( i_4 && ( x1 != x4 || y1 != y4 ) )
            {
                                                    // 1---4
                _setPointCoors( last, x4, y4 );
                return true;
            } // if
                                                    // 1---1
            _setPointCoors( last, x1, y1 );
            return ( ! ignore_1 );
        } // if

        if( i_2 )
        {
            _setPointCoors( last, x2, y2 );

            if( i_3 && ( x2 != x3 || y2 != y3 ) )
            {
                                                    // 3---2
                _setPointCoors( first, x3, y3 );
                return true;
            } // if

            if( i_4 && ( x2 != x4 || y2 != y4 ) )
            {
                                                    // 4---2
                _setPointCoors( first, x4, y4 );
                return true;
            } // if
                                                    // 2---2
            _setPointCoors( first, x2, y2 );
            return ( ! ignore_2 );
        } // if

        if( i_3 )
        {
            _setPointCoors( first, x3, y3 );
            _setPointCoors( last,  x3, y3 );

            if( i_4 && ( x3 != x4 || y3 != y4 ) )
            {
                if( pointLiesOnSegment( x3, y3, x1, y1, x4, y4 ) )
                {
                                                    // 3---4
                    _setPointCoors( last,  x4, y4 );
                    return true;
                } // if
                                                    // 4---3
                _setPointCoors( first, x4, y4 );
                return true;
            } // if
                                                    // 3---3
            return true;
        } // if

        if( i_4 )
        {
                                                    // 4---4
            _setPointCoors( first, x4, y4 );
            _setPointCoors( last,  x4, y4 );
            return true;
        } // if


        // Es sind jetzt nur noch die Faelle moeglich, dass es
        // entweder einen "echten" Schnitt gibt, oder gar keinen

        long area_1 = doubleArea( x1, y1, x3, y3, x2, y2 );
        long area_2 = doubleArea( x1, y1, x2, y2, x4, y4 );

        if(    ( area_1 <= 0 && area_2 >= 0 )
            || ( area_1 >= 0 && area_2 <= 0 )
            )
            return false; // Die Strecke (x1,y1)---(x2,y2) liegt
                          // vollstaendig auf einer Seite der
                          // Strecke (x3,y3)---(x4,y4).
                          // Also kein Schnitt

        area_1 = doubleArea( x3, y3, x2, y2, x4, y4 );
        area_2 = doubleArea( x3, y3, x4, y4, x1, y1 );

        if(    ( area_1 <= 0 && area_2 >= 0 )
            || ( area_1 >= 0 && area_2 <= 0 )
            )
            return false; // Die Strecke (x3,y3)---(x4,y4) liegt
                          // vollstaendig auf einer Seite der
                          // Strecke (x1,y1)---(x2,y2).
                          // Also kein Schnitt

        // Es ist jetzt sicher, dass die beiden Strecken sich
        // schneiden.
        // Falls first oder last nicht null sind, muessen die
        // Schnittpunkt-Koordinaten noch berechnet und eingetragen
        // werden
        //
        if( first != null || last != null )
        {
            double factor = ( double )( area_1 ) /
                            ( double )( area_1 + area_2 );

            int xs = ( int )( Math.round( factor * ( x2 - x1 ) ) ) + x1;
            int ys = ( int )( Math.round( factor * ( y2 - y1 ) ) ) + y1;

            _setPointCoors( first, xs, ys );
            _setPointCoors( last,  xs, ys );
        } // if

        return true;
    } // intersectionSegmentSegment
*/


    //************************************************************
    // private class methods
    //************************************************************



    /**
     * Testen ob die Strecke (<x1>,<y1>)---(<x2>,<y2>) die durch
     * die Punkte (<xmin>-0.5,<ymin>-0.5) und (<xmax>+0.5,<ymax>+0.5)
     * gegebene Rechteckflaeche schneidet.
     *
     * Es gilt  ( x1 <= x2 && y1 <= y2 )
     *
     * @return  true <==> Strecke schneidet Rechteckflaeche
     */

    //============================================================
    private static boolean _lineIntersectsRect( int x1, int y1,
                                                int x2, int y2,
                                                int xmin, int ymin,
                                                int xmax, int ymax )
    //============================================================
    {
        if(    x1 > xmax
            || x2 < xmin
            || y1 > ymax
            || y2 < ymin
            )
            return false; // Linie liegt vollstaendig ausserhalb
                          // des Rechtecks

        if(    ( x1 >= xmin || y2 <= ymax )
            && ( y1 >= ymin || x2 <= xmax )
            )
            return true;
            // Die Linie schneidet die Rechteckflaeche
            // ( Das resultiert aus der Tatsache, dass der Fall der
            //   vollstaendig ausserhalb des Rechtecks verlaufenden
            //   Linie bereits ausgeschlossen wurde )


        // Es gilt jetzt folgendes
        //
        //      Die Punkte (x1,y1) und (x2,y2) liegen beide ausserhalb
        //      des Rechtecks
        //
        //      Die Linie schneidet mindestens eine vertikale
        //      und eine horizontale Begrenzungsgerade des Rechtecks


        long d_x2 = x2 - x1;
        long d_y2 = y2 - y1;

        return (   2 * ( ( xmin - x1 ) * d_y2 - ( ymax - y1 ) * d_x2 )
                   <= d_x2 + d_y2
                &&
                   2 * ( ( ymin - y1 ) * d_x2 - ( xmax - x1 ) * d_y2 )
                   <= d_x2 + d_y2
               );
    } // _lineIntersectsRect


    /**
     * Eintragen der Koordinaten <x> und <y> in den Punkt <point>,
     * falls dieser nicht <null> ist
     */
/*
    //============================================================
    private static void _setPointCoors( Point point,
                                        int   x,
                                        int   y )
    //============================================================
    {
        if( point != null )
        {
            point.x = x;
            point.y = y;
        } // if
    } // _setPointCoors
*/
} // Calculator
