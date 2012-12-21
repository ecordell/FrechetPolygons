package anja.polyrobot;

/**
 * AngleCounter ist ein Winkelzaehler zum Aufsummieren von Drehwinkeln.
 *
 * Der Winkelzaehler wird beim Erzeugen oder durch Aufruf der Methode
 *
 *      reset( angle )
 *
 * mit einem Startwinkel initialisiert.
 * Dieser Winkel ist ueblicherweise die anfaengliche Orientierung eines
 * Objekts, welches einer Folge von Links- und Rechtsdrehungen unter-
 * worfen werden soll.
 *
 * Zum Aufsummieren eines Drehwinkels steht die Methode
 *
 *      addAngle( angle, orientation )
 *
 * bereit.
 * Dieser Methode wird neben dem vorzeichenbehafteten Drehwinkel angle
 * auch die neue Orientierung des gedrehten Objekts uebergeben.
 * Dabei handelt es sich um einen Winkel der groesser gleich 0 und
 * kleiner als 2*PI ist.
 * Anhand der Orientierung wird der Winkelzaehler "korrigiert" nachdem
 * der Drehwinkel addiert wurde. Das heisst, der Winkelzaehler ist nach
 * addAngle(..) immer so eingestellt, dass sein Wert die gleiche Richtung
 * festlegt wie die uebergebene Orientierung.
 * Dieses Vorgehen garantiert, dass der Winkelzaehler unabhaengig von
 * Anzahl, Betrag und Richtung der gezaehlten Drehwinkel immer bestmoegliche
 * Genauigkeit hat.
 * Dieser genaue Wert des Winkelzaehlers kann mit
 *
 *      getCurrentAngle()
 *
 * abgefragt werden.
 *
 *
 * Neben dem absoluten Wert des Winkelzaehlers kann auch der relative
 * Zaehlerstand, also die Summe aller Drehwinkel seit der Initialisierung,
 * mit der Methode
 *
 *      getRelValue()
 *
 * ermittelt werden.
 * Normalerweise gilt also
 *
 *      getInitAngle() + getRelValue() == getCurrentAngle().
 *
 * Allerdings nicht immer !!!
 *
 * Fuer den mit getRelValue() ermittelte Wert laesst sich naemlich
 * explizit eine Ungenauigkeit festlegen, um einen reallen Winkelzaehler
 * zu simulieren.
 * So wird durch die Methode
 *
 *      setGradation( gradation )
 *
 * bewirkt, dass der mit getRelValue() ermittelte Wert immer ein
 * Vielfaches des Wertes gradation ist.<p>
 *
 * Optional kann ein Winkelzaehler mit Kompass simuliert werden. Die
 * Kompassnadel zeigt idealerweise in die Richtung des Initialwinkels,
 * unterliegt aber bei jedem "Ablesevorgang" einer einstellbaren
 * Fehlertoleranz, sodass ein Zufallswert innerhalb des Fehlerbereichs um den
 * Initialwinkel zurueckgeliefert wird.
 *
 * @version 0.3 12.11.03
 * @author Ulrich Handel, Sascha Ternes
 */

//****************************************************************
public class AngleCounter
//****************************************************************
{
    //************************************************************
    // private variables
    //************************************************************


    private double _angle_init;
        // Winkel mit dem der Zaehler initialisiert wurde

    private double _angle_curr;
        // genauer aktueller Winkel des gedrehten Objekts

    private double _value;
        // relativer Zaehlerstand der Winkelzaehlers
        // ( ist ggf. fehlerbehaftet )


    private double _gradation;

    // aktueller Wert der Kompassnadel:
    private double _needle;
    // Fehlerbereich der Kompassnadel (+/- Winkel um Initialwinkel):
    private double _error;
    // Flag, ob die Kompassnadel benutzt wird:
    private boolean _use_compass;


    //************************************************************
    // constructors
    //************************************************************



    /**
     * Erzeugen eines Winkelzaehlers.
     * Der Zaehler wird mit 0.0 initialisiert.
     * Der Fehlerbereich der Kompassnadel wird auf +/- 90° festgesetzt.
     */

    //============================================================
    public AngleCounter()
    //============================================================
    {
        this( 0.0 );
    } // AngleCounter



    /**
     * Erzeugen eines Winkelzaehlers und initialisieren mit dem
     * Startwinkel angle. Der Fehlerbereich der Kompassnadel wird auf +/- 90°
     * festgesetzt.
     */

    //============================================================
    public AngleCounter( double angle )
    //============================================================
    {
        reset( angle );
        _error = 0.5 * Math.PI;
        _use_compass = false;
    } // AngleCounter



    /**
     * Erzeugen eines Winkelzaehlers und initialisieren mit dem
     * Startwinkel angle. Der Fehlerbereich der Kompassnadel wird auf den
     * angegebenen Wert festgesetzt.
     */

    //============================================================
    public AngleCounter( double angle, double error )
    //============================================================
    {
        reset( angle );
        _error = error;
        _use_compass = false;
    } // AngleCounter



    /**
     * Erzeugen eines Winkelzaehlers als Kopie des uebergebenen
     * Winkelzaehlers.
     */

    //============================================================
    public AngleCounter( AngleCounter counter )
    //============================================================
    {
        _angle_init = counter._angle_init;
        _angle_curr = counter._angle_curr;

        _value = counter._value;

        _gradation = counter._gradation;

        _needle = counter._needle;
        _error = counter._error;
        _use_compass = false;
    } // AngleCounter





    //************************************************************
    // public methods
    //************************************************************



    /**
     * @return Der relative Zaehlerstand des Winkelzaehlers
     *
     * Dieser Wert ist ggf. fehlerbehaftet
     */

    //============================================================
    public double getRelValue()
    //============================================================
    {
        return _value;
    } // getRelValue



    /**
     * @return Der Startwinkel mit dem der Zaehler initialisiert wurde
     */

    //============================================================
    public double getInitAngle()
    //============================================================
    {
        return _angle_init;
    } // getInitAngle



    /**
     * @return Der genaue aktuelle Winkel des gedrehten Objekts
     */

    //============================================================
    public double getCurrentAngle()
    //============================================================
    {
        return _angle_curr;
    } // getCurrentAngle


   /**
   * Liest die Kompassnadel ab und speichert den "abgelesenen" Wert. Dieser
   * kann dann mit {@link #getNeedleValue() getNeedleValue()} ermittelt werden.
   */
   public void readNeedle() {

      if ( _use_compass )
         _needle = Math.random() * 2.0 * _error - _error;
      else
         _needle = 0.0;

   } // getNeedleValue


   /**
   * Liefert den aktuellen Stand der Kompassnadel als Zufallswert aus dem
   * Fehlerbereich um den Initialwinkel, wenn der Kompass benutzt wird, sonst
   * wird immer <code>0.0</code> zurueckgegeben.
   *
   * @return den Wert der Kompassnadel als vorzeichenbehafteten Differenzwinkel
   *         um den Initialwinkel
   */
   public double getNeedleValue() {

      return _needle;

   } // getNeedleValue


   /**
   * Liefert den aktuellen Stand der Kompassnadel.
   */
   public double getNeedleAngle() {

      return MathUtil.getOrientation( _angle_init + _needle );

   } // getNeedleAngle


    /**
     * @return Die Abstufung, mit der der Winkelzaehler messen kann
     */

    //============================================================
    public double getGradation()
    //============================================================
    {
        return _gradation;
    } // getGradation


    /**
    * Liefert zurueck, ob der Kompass benutzt wird.
    */
    public boolean isCompassEnabled() {

       return _use_compass;

    } // isCompassEnabled


    /**
     * Setzen der Abstufung, mit der der Winkelzaehler messen kann
     */

    //============================================================
    public void setGradation( double gradation )
    //============================================================
    {
        if( gradation < 0 )
            gradation = 0;

        _gradation = gradation;
    } // setGradation


    public double getDeviation() {

       return _error;

    } // getDeviation


    public void setDeviation(
       double deviation
    ) {

       _error = deviation;

    } // setDeviation


   /**
   * Schaltet den Kompass ein oder aus.
   *
   * @param enable <code>true</code>, wenn der Kompass benutzt werden soll,
   *        sonst <code>false</code>
   */
   public void setCompassEnabled(
      boolean enable
   ) {

      _use_compass = enable;

   } // setCompassEnabled



    /**
     * Zuruecksetzen des Winkelzaehlers auf den Wert angle.
     */

    //============================================================
    public void reset( double angle )
    //============================================================
    {
        _value = 0.0;
        _angle_init = angle;
        _angle_curr = angle;
    } // reset



    /**
     * Addieren des Drehwinkels angle zum aktuellen Wert des
     * Winkelzaehlers.
     *
     * @param angle Vorzeichenbehafteter Drehwinkel.
     *
     * @param orientation Aktuelle Richtung des gedrehten Objekts
     * ( zur Fehlerkorrektur )
     */

    //============================================================
    public void addAngle( double angle, double orientation )
    //============================================================
    {
        // Drehwinkel aufsummieren
        _angle_curr += angle;


        // Den Winkel _curr_angle so korrigieren, dass durch ihn und
        // den Winkel orientation die gleiche Richtung festgelegt wird.

        double diff = MathUtil.getOrientation( orientation ) -
                      MathUtil.getOrientation( _angle_curr );

        if( diff < - Math.PI )
            diff += 2 * Math.PI;
        else
        if( diff > Math.PI )
            diff -= 2 * Math.PI;

        _angle_curr += diff;

        double turn = angle + diff; // Der Winkel, der insgesamt zu
                                    // _angle_curr addiert wurde

        if( _gradation > 0 )
        {
            _value += Math.round( turn / _gradation ) * _gradation;
        }
        else
            _value += turn;
    } // addAngle


} // AngleCounter
