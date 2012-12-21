package anja.obstaclescene;

/**
 * Ein Objekt der Klasse TurnAngle beschreibt einen Drehwinkel.
 *
 * Ein Drehwinkel ist gegeben durch eine Start-Strahlenrichtung,
 * eine End-Strahlenrichtung ( @see RayDirection ), eine Drehrichtung
 * ( CLOCKWISE oder COUNTERCLOCKWISE ) und der Anzahl der Vollkreise,
 * die bei der Drehung durchlaufen werden.
 *
 * @version 0.1 28.01.03
 * @author      Ulrich Handel
 */

//****************************************************************
public class TurnAngle
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


    private RayDirection _start; // Start-Strahlenrichtung
    private RayDirection _end;   // End-Strahlenrichtung

    private boolean _turn_dir;   // Drehrichtung
                                 // CLOCKWISE oder COUNTERCLOCKWISE

    private int _no_of_revolutions; // Anzahl der Vollkreise, die bei
                                    // der Drehung durchlaufen werden




    //************************************************************
    // constructors
    //************************************************************


    /**
     * Erzeugen eines neuen Drehwinkels
     *
     * @param start  Start-Strahlenrichtung
     * @param end    End-Strahlenrichtung
     *
     * @param turn_dir  CLOCKWISE oder COUNTERCLOCKWISE
     *
     * @param no_of_revolutions  Anzahl der Vollkreise, die bei der
     *                           Drehung durchlaufen werden
     */

    //============================================================
    public TurnAngle( RayDirection start,
                      RayDirection end,
                      boolean      turn_dir,
                      int          no_of_revolutions )
    //============================================================
    {
        _start = start;
        _end   = end;

        _turn_dir = turn_dir;
        _no_of_revolutions = no_of_revolutions;
    } // TurnAngle


    /**
     * Erzeugen eines neuen Drehwinkels, der eine Drehung beschreibt,
     * bei der kein Vollkreis durchlaufen wird, also bei der der Betrag
     * des durchlaufenen Winkels im Bogenmass kleiner als 2*PI ist
     *
     * @param start  Start-Strahlenrichtung
     * @param end    End-Strahlenrichtung
     *
     * @param turn_dir  CLOCKWISE oder COUNTERCLOCKWISE
     */

    //============================================================
    public TurnAngle( RayDirection start,
                      RayDirection end,
                      boolean      turn_dir )
    //============================================================
    {
        _start = start;
        _end   = end;

        _turn_dir = turn_dir;
    } // TurnAngle


    /**
     * Erzeugen eines neuen Drehwinkels, der eine Drehung beschreibt,
     * bei der man sich ausgehend von der Start-Richtung auf kuerzestem
     * Wege in die End-Richtung dreht.
     *
     * Falls Start-Richtung und End-Richtung genau entgegengesetzt
     * sind, so wird die Drehrichtung COUNTERCLOCKWISE genommen.
     *
     * @param start  Start-Strahlenrichtung
     * @param end    End-Strahlenrichtung
     */

    //============================================================
    public TurnAngle( RayDirection start,
                      RayDirection end )
    //============================================================
    {
        _start = start;
        _end   = end;

        _turn_dir = _start.getShortestTurnDirection( _end );
    } // TurnAngle




    //************************************************************
    // public methods
    //************************************************************


    /**
     * @return  Strahlenrichtung zu Beginn der durch diesen
     *          Drehwinkel beschriebenen Drehung
     */

    //============================================================
    public RayDirection getStartDirection()
    //============================================================
    {
        return _start;
    } // getStartDirection


    /**
     * @return  Strahlenrichtung am Ende der durch diesen Drehwinkel
     *          beschriebenen Drehung
     */

    //============================================================
    public RayDirection getEndDirection()
    //============================================================
    {
        return _end;
    } // getEndDirection


    /**
     * @return  CLOCKWISE oder COUNTERCLOCKWISE
     */

    //============================================================
    public boolean getTurnDirection()
    //============================================================
    {
        return _turn_dir;
    } // getTurnDirection


    /**
     * @return  Anzahl der Vollkreise, die bei der durch diesen
     *          Drehwinkel beschriebenen Drehung durchlaufen werden
     */

    //============================================================
    public int getNoOfRevolutions()
    //============================================================
    {
        return _no_of_revolutions;
    } // getNoOfRevolutions


    /**
     * Ermitteln des bei der beschriebenen Drehung insgesamt
     * durchlaufenen Winkels im Bogenmass.
     *
     * Bei einer COUNTERCLOCKWISE-Drehung ist der ermittelte Wert
     * positiv und bei einer CLOCKWISE-Drehung negativ.
     *
     * Fuer den Betrag des ermittelten Wertes gilt
     *     | radians | >= ( NoOfRevolutuions * 2*PI )
     *
     * @return  Der durchlaufene Winkel im Bogenmass
     */

    //============================================================
    public double getRadians()
    //============================================================
    {
        double radians = ( _turn_dir == CLOCKWISE ) ?
                         _end.getEnclosedAngle( _start ) :
                         _start.getEnclosedAngle( _end );

        radians += _no_of_revolutions * TWO_PI;

        if( _turn_dir == CLOCKWISE )
            return -radians;
        else
            return radians;
    } // getRadians


    /**
     * Testen, ob man waehrend der durch diesen Drehwinkel beschriebenen
     * Drehung mindestens einmal die Strahlenrichtung <ray_dir>
     * erreicht
     *
     * @return  true <==> Strahlenrichtung <ray_dir> wird bei der
     *                    Drehung mindestens einmal erreicht
     */

    //============================================================
    public boolean isInside( RayDirection ray_dir )
    //============================================================
    {
        if( ray_dir == null )
            return false;

        if( _no_of_revolutions > 0 )
            return true; // Wenn die Anzahl der Vollkreis-Umdrehungen
                         // groesser als 0 ist, wird jede Strahlen-
                         // Richtung mindestens einmal erreicht


        double start = _start.getRadians();
        double end   = _end.getRadians();
        double test  = ray_dir.getRadians();

        if( _turn_dir == CLOCKWISE )
            return Calculator.isAngleInside( test, end, start );
        else
            return Calculator.isAngleInside( test, start, end );
    } // isInside




    //************************************************************
    // private methods
    //************************************************************

} // TurnAngle
