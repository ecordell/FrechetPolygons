package anja.polyrobot;

/**
 * MathUtil ist eine Sammlung statischer Methoden aus dem Bereich
 * der Mathematik.
 *
 * @version 0.1 07.12.01
 * @author      Ulrich Handel
 */

//****************************************************************
public class MathUtil
//****************************************************************
{

    //************************************************************
    // class methods
    //************************************************************



    /**
     * @return Der Winkel orientation, der die gleiche Richtung wie angle
     * festlegt und fuer den gilt:   0 <= orientation < 2*PI
     */

    //============================================================
    static public double getOrientation( double angle )
    //============================================================
    {
        double two_pi = Math.PI * 2;

        if( ( angle >= 0 ) && ( angle < two_pi ) )
            return angle; // Der uebergebene Winkel ist mit der zu
                          // berechnenden Orientierung identisch


        // Berechnung der dem Winkel angle entsprechenden Orientierung

        double orientation;

        double sin = Math.sin( angle );
        double cos = Math.cos( angle );

        if( Math.abs( sin ) < Math.abs( cos ) )
        {
            // Die Orientierung wird in diesem Fall aus dem sinus
            // ermittelt, da dies wahrscheinlich genauer ist, als die
            // Ermittlung aus dem cosinus

            orientation = Math.asin( sin );

            if( cos < 0 )
                orientation = Math.PI - orientation;
            else
            if( orientation < 0 )
                orientation += two_pi;
        }
        else  // abs( cos ) <= abs( sin )
        {
            orientation = Math.acos( cos );

            if( sin < 0 )
                orientation = two_pi - orientation;
        } // else

        return orientation;
    } // getOrientation


} // MathUtil
