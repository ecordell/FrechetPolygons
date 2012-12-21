package anja.obstaclescene;

/**
 * Ein Objekt der Klasse RayDirection beschreibt die Richtung eines
 * Strahls in der Ebene. Diese ist durch den Winkel gegeben, den der
 * Strahl entgegen dem Uhrzeigersinn mit der positiven x-Achse bildet
 *
 * @version 0.1 28.01.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class RayDirection
//****************************************************************
{
    //************************************************************
    // public constants
    //************************************************************


    // Drehrichtungen
    //
    public final static boolean CLOCKWISE  = ObstacleScene.CLOCKWISE;
    public final static boolean COUNTERCLOCKWISE = ! CLOCKWISE;


    public final static double TWO_PI  = Calculator.TWO_PI;
    public final static double PI      = Calculator.PI;
    public final static double HALF_PI = Calculator.HALF_PI;




    //************************************************************
    // private variables
    //************************************************************


    private double _radians; // Normalisierter Winkel im Bogenmass
                             // Es gilt 0 <= _radians < 2*PI



    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen einer neuen Strahlenrichtung
     *
     * @param radians  Winkel im Bogenmass, den der Strahl mit der
     *                 positiven x-Achse bildet.
     *                 Es sind beliebige double-Werte ( also auch
     *                 negative Werte und Werte ueber 2*PI ) erlaubt
     */

    //============================================================
    public RayDirection( double radians )
    //============================================================
    {
        _radians = Calculator.getNormalizedAngle( radians );
    } // RayDirection


    /**
     * Erzeugen einer neuen Strahlenrichtung, die der Richtung des
     * Strahls entspricht, der vom Punkt ( <x1>,<y1> ) auf den Punkt
     * ( <x2>,<y2> ) zeigt.
     *
     * Falls die beiden Koordinatenpaare identisch sind, wird die
     * Strahlenrichtung der positiven x-Achse erzeugt.
     */

    //============================================================
    public RayDirection( int x1, int y1, int x2, int y2 )
    //============================================================
    {
        _radians = Calculator.getAngle( x1, y1, x2, y2 );
    } // RayDirection




    //************************************************************
    // public methods
    //************************************************************


    /**
     * @return  Winkel dieser Strahlenrichtung im Bogenmass
     *          Es gilt  0 <= radians < 2*PI
     */

    //============================================================
    public double getRadians()
    //============================================================
    {
        return _radians;
    } // getRadians


    /**
     * @return  Winkel dieser Strahlenrichtung in Grad
     *          Es gilt  0 <= degrees < 360
     */

    //============================================================
    public double getDegrees()
    //============================================================
    {
        return Math.toDegrees( _radians );
    } // getDegrees


    /**
     * @return  true <==> Der Winkel der Strahlenrichtung <dir> ist
     *                    gleich dem Winkel dieser Strahlenrichtung.
     */

    //============================================================
    public boolean isEqualTo( RayDirection dir )
    //============================================================
    {
        if( dir == null )
            return false;

        return _radians == dir._radians;
    } // isEqualTo


    /**
     * @return  Strahlenrichtung, die entgegengesetzt zu dieser
     *          Strahlenrichtung ist
     */

    //============================================================
    public RayDirection getOpposite()
    //============================================================
    {
        double angle = ( _radians >= PI ) ? -PI : PI;

        return new RayDirection( _radians + angle );
    } // getPerpendicular


    /**
     * @return  Strahlenrichtung, die sich durch eine Viertelkreis-
     *          Drehung dieser Strahlenrichtung in Drehrichtung
     *          <turn_dir> ergibt
     */

    //============================================================
    public RayDirection getPerpendicular( boolean turn_dir )
    //============================================================
    {
        double angle = ( turn_dir == CLOCKWISE ) ? -HALF_PI : HALF_PI;

        return new RayDirection( _radians + angle );
    } // getPerpendicular


    /**
     * Ermitteln des Bogenmass-Winkels der durchlaufen wird, wenn man
     * sich von dieser Richtung gegen den Uhrzeigersinn in die Richtung
     * <ray_dir> dreht.
     *
     * @return  Der ermittelte Winkel.
     *          Es gilt   0 <= angle < 2*PI
     */

    //============================================================
    public double getEnclosedAngle( RayDirection ray_dir )
    //============================================================
    {
        if( ray_dir == null )
            return 0.0;

        double angle = ray_dir._radians - _radians;

        if( angle < 0.0 )
            angle += TWO_PI;

        return angle;
    } // getEnclosedAngle


    /**
     * Ermitteln des komplementaeren Winkels zu dem Winkel, der
     * durchlaufen wird, wenn man sich von dieser Richtung gegen den
     * Uhrzeigersinn in die Richtung <ray_dir> dreht.
     *
     * Es gilt also immer
     *             getExcludedAngle( ray_dir ) ==
     *             2*PI - getEnclosedAngle( ray_dir )
     *
     * @return  Der ermittelte Winkel.
     *          Es gilt   0 < angle <= 2*PI !!!
     */

    //============================================================
    public double getExcludedAngle( RayDirection ray_dir )
    //============================================================
    {
        return TWO_PI - getEnclosedAngle( ray_dir );
    } // getExcludedAngle


    /**
     * Ermitteln der Richtung der Winkelhalbierenden des Winkels, der
     * durchlaufen wird, wenn man sich von dieser Richtung gegen den
     * Uhrzeigersinn in die Richtung <ray_dir> dreht.
     *
     * @return  Richtung der Winkelhalbierenden des Winkels
     *          getEnclosedAngle( ray_dir )
     */

    //============================================================
    public RayDirection getBisectorEnclosed( RayDirection ray_dir )
    //============================================================
    {
        if( ray_dir == null )
            return null;

        double radians = _radians + getEnclosedAngle( ray_dir ) / 2.0;

        return new RayDirection( radians );
    } // getBisectorEnclosed


    /**
     * Ermitteln des Richtung der Winkelhalbierenden des komplementaeren
     * Winkels zu dem Winkel, der durchlaufen wird, wenn man sich von
     * dieser Richtung gegen den Uhrzeigersinn in die Richtung <ray_dir>
     * dreht.
     *
     * @return  Richtung der Winkelhalbierenden des Winkels
     *          getExcludedAngle( ray_dir )
     */

    //============================================================
    public RayDirection getBisectorExcluded( RayDirection ray_dir )
    //============================================================
    {
        if( ray_dir == null )
            return null;

        double radians = _radians - getExcludedAngle( ray_dir ) / 2.0;

        return new RayDirection( radians );
    } // getBisectorExcluded


    /**
     * Ermitteln der Drehrichtung, die einen Strahl mit dieser
     * Strahlenrichtung auf kuerzestem Wege in die Richtung <ray_dir>
     * dreht.
     *
     * Falls die Richtung <ray_dir> genau entgegengesetzt zu dieser
     * Richtung ist, sind beide Drehrichtungen gleichwertig. In diesem
     * Fall wird COUNTERCLOCKWISE zurueckgegeben.
     *
     * @return  CLOCKWISE oder COUNTERCLOCKWISE
     */

    //============================================================
    public boolean getShortestTurnDirection( RayDirection ray_dir )
    //============================================================
    {
        if( ray_dir == null )
            return COUNTERCLOCKWISE; // irgendwas muss man auch in
                                     // diesem Fall zurueckgeben

        return ( getEnclosedAngle( ray_dir ) <= PI ) ?
               COUNTERCLOCKWISE :
               CLOCKWISE;
    } // getShortestTurnDirection




    //************************************************************
    // private methods
    //************************************************************

} // RayDirection
