package anja.swinggui.polygon;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.*;
import java.awt.FlowLayout;

import javax.swing.JPopupMenu;

import anja.geom.Point2;
import anja.geom.Polygon2;
import anja.geom.Polygon2Scene;
import anja.geom.Segment2;


/**
* Diese Klasse implementiert einen einfachen Editor fuer Polygonszenen.
* Dem Konstruktor wird ein Extension-Objekt uebergeben, das sich um das
* Einzeichnen weiterer Objekte (z.B. des Polygonkerns) kuemmert.
* Extension-Objekte sind Instanzen, die das Interface <i>Extendable</i>
* implementieren.
*
* @version      1.2, Juli 2009
* @author       Sascha Ternes, Andreas Lenerz, Jörg Wegener
*/

public class ExtendablePolygonEditor
 extends     Polygon2SceneEditor
 implements  Runnable
{
	/**
	 * Zeigt einen Forschrittsbalken in einem neuen Fenster an,
	 * das mittig im Editorfenster positioniert ist.
	 *
	 */
	public class Progress extends JFrame implements Runnable {
		
		private JProgressBar _bar;
		private boolean _abort;
		private JLabel _label;		
		
		/**
		 * Erstellt ein Fenster ohne Rahmen mit Fortschrittsbalken
		 */
	    public Progress()
	    {
	        super("Calculating...");	        
	        JPanel pane = new JPanel();
	        pane.setLayout(new FlowLayout());
	        _label = new JLabel("Calculating...");
	        pane.add(_label);
	        _bar = new JProgressBar(0, 100);
	        _bar.setValue(0);
	        _bar.setStringPainted(true);
	        pane.add(_bar);
	        setContentPane(pane);
	        this.setSize(200,60);
	        this.setUndecorated(true);
	    }
	    
	    /**
	     * Startet einen Thread, der alle 0.2 Sekunden den Fortschrittsbalken aktualisiert
	     */
	    public void start()
	    {
	    	//Position der Progress Bar wird hier festgelegt, weil beim Erstellen der Progress Bar
	    	//das Display Panel des Editors noch nicht existiert
	    	this.setLocationRelativeTo(getDisplayPanel());
	    	Thread t = new Thread(this);
	    	t.start();
	    }
	    
	    /**
	     * Aktualisiert den Fortschrittsbalken.
	     */
	    public void run()
	    {
	    	_abort = false;
	    	this.setVisible(true);
	    	_bar.setValue(0);
	    	while (!_abort && !_testarter._finished)
	    	{
	    		int percent = ((ThreadedExtendable)_extension).getCalculationPercent();
	    		if (percent>=0)
	    		{
	    			_bar.setIndeterminate(false);
	    			_bar.setValue(percent);
	    		}
	    		else _bar.setIndeterminate(true);
	    		try
	    		{
	    			Thread.sleep(200);
	    		}
	    		catch (Exception e)
	    		{
	    			_abort = true;
	    		}
	    	}
	    	this.setVisible(false);
	    	_bar.setValue(0);
	    }
	    
	    public void halt()
	    {
	    	_abort = true;	    
	    }

	} 

	
	/**
	 * Startet die run()-Methode eines ThreadedExtendable als Thread.
	 * Wartet zuerst auf die Unterbrechung des alten Threads.
	 */
	class ThreadedExtendableStarter
		implements Runnable
	{
		public Polygon2Scene _oldScene;
		
		// Vorher gestarteter Thread
		private Thread _oldThread;
		
		public boolean _abort = false;
		
		//zeigt an, dass die Berechnung erfolgreich war,
		//wird benötigt, weil der Thread nicht unbedingt vor dem repaint beendet ist
		public boolean _finished = false;
		
		public ThreadedExtendableStarter()
		{
			_oldScene = (Polygon2Scene)(getPolygon2Scene().clone());
		}
		
		/**
		 * Inherited from {@link java.lang.Runnable}
		 * Wartet auf die Beendigung des alten Threads und
		 * führt dann die run()-Methode des ThreadedExtendable aus und
		 * löst ein repaint() aus, falls der Thread nicht unterbrochen wurde.
		 */
		public void run()
		{
			if (!_abort)
			{
				((ThreadedExtendable)_extension).prepareCalculation();
				((ThreadedExtendable)_extension).calculateData(getPolygon2Scene());
			}
			if (!_abort) {
				_finished = true;
				refresh();
			}
		} // run()
		
		
		/**
		 * Markiert die aktuelle Berechnung als angehalten.
		 */
		public void halt()
		{
			_abort = true;
			((ThreadedExtendable)_extension).halt();
		} // stop()
	}
	

   // *************************************************************************
   // Private variables
   // *************************************************************************

   // das Extension-Objekt:
   private Extendable _extension;

   // die aktuellen Mauskoordinaten bei einem Klick (in Weltkoordinaten):
   private Point2 _mouse_coor;

   // Anzahl geschlossene Polygone in der gewuenschten Szene
   private int _numClosedPolys = 1;

   // Anzahl geoeffneter Polygone in der gewuenschten Szene
   private int _numOpenPolys = 0;

   // Wenn TRUE ist das erste geschl. Polygon ein aeusseres Polygon, siehe setSceneContainsBoundingPolygon()
   private boolean _containsBoundingPoly = false;

   // true bei gedrückter Maustaste, falls eingestellt wird das Zeichnen der Extension
   // bei gedrückter Maustaste unterbunden
   private boolean _isMousePressed = false;

   // wenn true, wird Extension auch bei gedrückter Maustaste gezeichnet, _isMousePressed wird
   // in diesem Fall nicht mehr gesetzt
   private boolean _dontDrawWhileMousePressed = false;
   
   // Zeichenreihenfolge
   private boolean _extension_first = false;
   
   // Ruft die Extension immer unabhängig vom Inhalt der Szene auf
   private boolean _draw_extension_always = false;
   
   // Startet das Extendable
   private ThreadedExtendableStarter _testarter = null;
   
   // Thread für die Ausführung des Starters
   private Thread _tethread = null;
   
   //Progress Bar
   private Progress _progress = new Progress();
   
   //true während eine Animation läuft
   private boolean _animating = false;
   
   //Geschwindigkeit der Animation, Standard 30 fps
   private long _speed = 1000/30;
   
   

   // *************************************************************************
   // Constructors
   // *************************************************************************

   /**
   * Der Konstruktor registriert das uebergebene Extension-Objekt, welches die
   * automatische Ergaenzung von Grafikobjekten in der Polygonszene uebernimmt.
   *
   * @param extension das EditorExtension-Objekt
   * @see Extendable
   */
   public ExtendablePolygonEditor(
        Extendable extension
   ) {

      super();

      // initialisiert die Extension:
      _extension = extension;
      _extension.registerPolygonEditor( this );
      
      _mouse_coor = new Point2( 0.0, 0.0 ); // zur Initialisierung

   } // ExtendablePolygonEditor


   // *************************************************************************
   // Public methods
   // *************************************************************************

   /**
   * Zeichnet alle Polygone der Szene. Wenn mehr als ein "einfaches" Polygon
   * existiert (d.h. das letzte Editorkommando hat das erste "einfache" Polygon
   * erzeugt), werden alle anderen Polygone, Segmente und Punkte geloescht.
   * Anschliessend wird die paint-Methode der EditorExtension aufgerufen.
   *
   * @see Extendable#paint
   */
   protected void paintPolygons(Graphics2D g2d, Graphics g) 
   {
	   if (_dontDrawWhileMousePressed && _isMousePressed) return;
       boolean justOne=false;
       if (!_dontDrawWhileMousePressed || !_isMousePressed) {
    	  justOne = _controlScene(); // alle anderen Polygone loeschen
       }  // if
       if ( ! _extension_first ) {
    	   super.paintPolygons( g2d, g ); // Polygon zeichnen
       } // if
       if ( _draw_extension_always || justOne ) { // falls geschlossenes Polygon vorhanden,
    	   
    	   if (_extension instanceof ThreadedExtendable && justOne) {
    		   if (!_animating && (_testarter == null || !_testarter._oldScene.equals(getPolygon2Scene()))) {
    	    	   //neue Threads nicht bei gedrückter Maustaste starten, da davon auszugehen ist
    	    	   //dass sich das Polygon vor Beendigung des Threads ohnehin wieder geändert hat.
    			   //während einer Animation auch keine Threads starten.
    			   if (!_isMousePressed) {
	    			   if (_testarter != null) {
	    				   _progress.halt();
	    				   _testarter.halt();
	    					try {
	    						if (_tethread.isAlive()) _tethread.join();
	    					}
	    					catch (Exception e) {}
	    			   }
	    			   _testarter = new ThreadedExtendableStarter();
	    			   _tethread = new Thread(_testarter);
	    			   _tethread.start();
	    			   _progress.start();
    			   }
    		   }
    		   else {
    			   //wenn der Berechnungsthread beendet ist und die PolygonSzene sich nicht geändert hat,
    			   //ist der Thread erfolgreich beendet und es gibt Daten zum Zeichnen
    			   //paintData auch während einer Animation aufrufen, falls der vorherige
    			   //Berechnungsthread erfolgreich durchgelaufen ist
    			   if (_testarter._finished) ((ThreadedExtendable)_extension).paintData(getPolygon2Scene(), g2d);
    		   }
    	   }
   		   _extension.paint( getPolygon2Scene(), g2d ); // Erweiterung zeichnen
       } // if
       if ( _extension_first ) {
          super.paintPolygons( g2d, g ); // Polygon zeichnen
       } // if

   } // paintPolygons

   /**
   * Diese Methode wird geerbt und ueberschrieben, um die Erweiterung des
   * Kontextmenues zu ermoeglichen.
   */
   protected void buildPopupMenu(
      JPopupMenu menu
   ) {

      _extension.popupMenu( menu );
      super.buildPopupMenu( menu );

   } // buildPopupMenu

   /**
   * Diese Methode wird geerbt und sichert die aktuellen Mauskoordinaten, um
   * sie als Parameter bei der Kontextmenue-Auswahl uebergeben zu koennen.
   * Ausserdem uebergibt sie die aktuellen Mauskoordinaten als Parameter an die
   * Methode
   * <A HREF="../../anja/swinggui/Extendable.html#processMousePressed
   *(java.awt.event.MouseEvent, anja.geom.Point2)">processMousePressed</A>
   * des Extension-Objekts, um diesem eigene Reaktionen beim Auftreten dieses
   * Mausereignisses zu ermoeglichen, beispielsweise, um eine eigene Drag-Phase
   * zu starten.
   *
   * @see Extendable#processMousePressed
   */
   public void mousePressed(
      MouseEvent e
   ) {
	  _animating = false;
      _isMousePressed = true;
      // aktuelle Mauskoordinaten in Weltkoordinaten umwandeln und speichern:
      _mouse_coor.moveTo( transformScreenToWorld( e.getX(), e.getY() ) );
      // an Extension-Objekt uebergeben:
      boolean continue_proc = _extension.processMousePressed( e, _mouse_coor );
      refresh(); // Szene auf alle Faelle neuzeichnen

      // wenn Extension nicht abfaengt, MouseEvent an Editor weiterreichen:
      if ( continue_proc ) {
         super.mousePressed( e );
      } // if

   } //mousePressed

   /**
   * Diese Methode wird geerbt und uebergibt die aktuellen Mauskoordinaten als
   * Parameter an die Methode
   * <A HREF="../../anja/swinggui/Extendable.html#processMouseReleased
   *(java.awt.event.MouseEvent, anja.geom.Point2)">processMouseReleased</A>
   * des Extension-Objekts, um diesem eigene Reaktionen beim Auftreten dieses
   * Mausereignisses zu ermoeglichen, beispielsweise, um eine eigene Drag-Phase
   * korrekt zu beenden.
   *
   * @see Extendable#processMouseReleased
   */
   public void mouseReleased(
      MouseEvent e
   ) {
      _isMousePressed = false;
      // aktuelle Mauskoordinaten in Weltkoordinaten umwandeln und speichern:
      _mouse_coor.moveTo( transformScreenToWorld( e.getX(), e.getY() ) );
      // an Extension-Objekt uebergeben:
      boolean continue_pro = _extension.processMouseReleased( e, _mouse_coor );
      refresh(); // Szene auf alle Faelle neuzeichnen

      // wenn Extension nicht abfaengt, MouseEvent an Editor weiterreichen:
      if ( continue_pro ) {
         super.mouseReleased( e );
      } // if

   } // mouseReleased

   /**
   * Diese Methode wird geerbt und uebergibt die aktuellen Mauskoordinaten als
   * Parameter an die Methode
   * <A HREF="../../anja/swinggui/Extendable.html#processMouseDragged
   *(java.awt.event.MouseEvent, anja.geom.Point2)">processMouseDragged</A>
   * des Extension-Objekts, um diesem z.B. zu ermoeglichen, eigene Grafikobjekte
   * im Editor verschieben zu koennen.
   *
   * @see Extendable#processMouseDragged
   */
   public void mouseDragged(
      MouseEvent e
   ) {
	   _animating = false;
	   //Wenn die Maus, z.b. ein Punkt oder ein Segment, gezogen wird, auf jeden Fall sämtliche Berechnungen
	   //abbrechen
	   if (_testarter != null) _testarter.halt();
	   if (_progress != null) _progress.halt();
      // aktuelle Mauskoordinaten in Weltkoordinaten umwandeln und speichern:
      _mouse_coor.moveTo( transformScreenToWorld( e.getX(), e.getY() ) );
      // an Extension-Objekt uebergeben:
      boolean continue_proc = _extension.processMouseDragged( e, _mouse_coor );
      refresh(); // Szene auf alle Faelle neuzeichnen

      // wenn Extension nicht abfaengt, MouseEvent an Editor weiterreichen:
      if ( continue_proc ) {
         super.mouseDragged( e );
      } // if

   } // mouseDragged

   /**
   * Diese Methode wird geerbt und ueberschrieben, um der Erweiterung das
   * Abfangen von Kontextmenue-Auswahlen zu ermoeglichen.
   *
   * @see Extendable#processPopup
   */
   public void actionPerformed(
      ActionEvent e
   ) {

      // Extension-Ereignisbehandlung
      boolean continue_proc = _extension.processPopup( e, _mouse_coor );
      refresh(); // Szene auf alle Faelle neuzeichnen

      // wenn Extension nicht abfaengt, ActionEvent an Editor weiterreichen:
      if ( continue_proc ) {
         super.actionPerformed( e );
      } // if

   } // actionPerformed


    /**
     * Setzt die Anzahl der geschlossenen Polygone, die die Szene
     * enthalten soll. Default: 0
     */

   //============================================================
   public void setNumClosedPolygons(int num)
   //============================================================
   {
       _numClosedPolys = num;
   } // setNumClosedPolygons


    /**
     * Setzt die Anzahl der ge&ouml;ffneten Polygone, die die Szene
     * enthalten soll. Default: 1
     */

   //============================================================
   public void setNumOpenPolygons(int num)
   //============================================================
   {
       _numOpenPolys = num;
   } // setNumOpenPolygons


    /**
     * Teilt der Szene mit, da&szlig; das erste geschlossene
     * Polygon der Szene ein &auml;&szlig;eres Polygon sein soll,
     * also ein Polygon, das die anderen enth&auml;lt.
     * Per default sind alle Polygone innere; enthalten also keine
     * anderen. Das Bounding Polygon wird bei den Anzahlen an offenen
     * und geschlossenen Polygonen (siehe setNumClosedPolygons und
     * setNumOpenPolygons) <em>nicht</em> mitgezaehlt.
     */

   //============================================================
   public void setSceneContainsBoundingPolygon()
   //============================================================
   {
       _containsBoundingPoly = true;
   } // setSceneContainsBoundingPolygon


   /**
   * Legt die Zeichenreihenfolge von Polygon und Erweiterung fest.
   *
   * @param order <code>true</code> bewirkt, dass die paint()-Methode der
   *        Erweiterung zuerst gezeichnet wird, bei <code>false</code> wird
   *        das Polygon zuerst gezeichnet.
   */
   public void setPaintOrder(
      boolean order
   ) {

      _extension_first = order;
                 
   } // setPaintOrder
   
   
   /**
    * Bei Aktivierung wird die Extension immer aufgerufen, unabhängig vom Inhalt der Szene.
    * Ansonsten wird die Extension erst aufgerufen, wenn die voreingestellte Anzahl
    * an Polygonen erreicht ist und keine Maustaste gedrückt wird.
    * 
    * @param draw Bei true wird die Extension immer aufgerufen, Standard ist false.
    */
   public void setDrawExtensionAlways(boolean draw)
   {
       _draw_extension_always = draw;
   }


    public Point2 insertPoint( Polygon2 poly,
		   Segment2 segment,
		   Point2   point )
   {

	   return super.insertPoint(poly, segment, point);
   }
    
	/**
	 * Setzt, ob die Extension auch bei gedrückter Maustaste (z.B. beim Ziehen eines Punktes)
	 * gezeichnet wird. Standardmäßig wird die Extension auch bei gedrückter Maustaste aufgerufen.
	 * 
	 * @param dontDraw Bei true wird die Extension nur bei losgelassener Maustaste gezeichnet
	 */
   public void setDontDrawWhileMousePressed(boolean dontDraw) {
	   _dontDrawWhileMousePressed = true;
   }
    /**
     * Ist die Berechnung des Extendables abgeschlossen?
     * 
     * @return true bei abgeschlossener Berechnung, false sonst
     */
    public boolean isCalculationCompleted()
    {
    	if (_testarter == null)
    		return false;
    		
    	return _testarter._finished;
    }
    
   
    /**
     * Setzt die Frames per Second einer Animation
     * @param fps Frames per Second
     */
    public void setFPS(long fps) {
    	_speed = 1000/fps;
    }
    
    /**
     * Gibt an, ob eine Animation läuft
     * @return true, wenn eine Animation läuft
     */
    public boolean isAnimating() {
    	return _animating;
    }
    
    /**
     * Versetzt den Editor in den Animationsmodus
     */
    public void startAnimation() {
    	Thread t = new Thread(this);
    	t.start();
    }
    
    /**
     * Bricht den Animationsmodus ab
     */
    public void stopAnimation() {
    	_animating = false;
    }
    
    
    /** Run-Methode zur Animation, zeichnet in regelmässigen Abständen einfach die Zeichenfläche neu
     */
    public void run() {
    	_animating = true;
    	while (_animating) {
    		long time = System.nanoTime();
	    	this.refresh();
	    	time = System.nanoTime() - time;
	    	try {Thread.sleep(_speed-time/1000000);}
	    	catch (Exception e) {};
    	}
    }
    
    
    
    // #########################################################################

    
    
 // ************************************************************************
    // Private methods
    // ************************************************************************

    /*
    * Kontrolliert die Polygonszene. Wenn ein "einfaches Polygon" (>=3 Punkte,
    * geschlossen, Kanten ueberschneiden sich nicht) vorkommt, werden alle
    * anderen Polygone bis auf dieses eine geloescht.
    */
    private boolean _controlScene()
    {

        // ermittle alle Polygone und pruefe jedes auf Einfachheit:
        Polygon2 poly[] = getPolygon2Scene().getPolygons();
        int i = 0;
        int open = 0;
        int closed = 0;
        int removed = 0;
        int addtoopen = 0;
        
        // Ist ein Bounding Polygon gefordert: Es muss vor allen anderen Polygonen gezeichnet werden
        // und muss einfach sein
        if (_containsBoundingPoly) {
            if (poly.length>1)
                for (i=1; i<poly.length; i++)
                    removePolygon(poly[i], false);
                 
            if (poly.length > 0 && poly[0].isClosed()) {               
                removePolygon(poly[0], false);
                if (poly[0].isSimple()) {
                    setBoundingPolygon(poly[0], false, true);
                    _containsBoundingPoly = false;
                }
                return _draw_extension_always;
            }
        }

        for (i=0; (open<_numOpenPolys || closed<_numClosedPolys) && i<poly.length; i++) {
            if ( poly[i].isClosed() ) {
                closed++;
            } else {
                if ( poly[i].isOpen() ) {
                    open++;
                } // if
            } // else
        } // while

        // wenn noch geschlossene Polygone erlaubt sind:
        // diese zu den erlaubten offenen addieren
        addtoopen = ( _numClosedPolys - closed );
        addtoopen = ( addtoopen > 0) ? addtoopen : 0;

        if ( closed >= _numClosedPolys ) {
            // es existieren alle geschl. Polygon -> loesche alle anderen:
            closed = 0;
            for (i = 0; i < poly.length; i++) {
                if ( poly[i].isClosed() ) {
                    if ( closed == _numClosedPolys ) {
                        getPolygon2Scene().remove( i - removed );
                        removed++;
                    } else {
                        closed++;
                    } // else
                } //if
            } // for
        } // if


        if ( open >= _numOpenPolys + addtoopen) {
            // es existieren alle offenen Polygon -> loesche alle anderen:
            open = 0;
            for (i = 0; i < poly.length; i++) {
                if ( poly[i].isOpen() ) {
                    if ( open == _numOpenPolys + addtoopen ) {
                        getPolygon2Scene().remove( i - removed );
                        removed++;
                    } else {
                        open++;
                    } // else
                } // if
            } // for
        } // if
       
       // wenn _containsBoundPoly true ist, dann wird noch am ersten Polygon gezeichnet, das
       // das BoundingPolygon wird, sobald es geschlossen wird. Bis hierher ist es dann aber zu 
       // den offenen Polygonen gezählt worden, daher muss in dem Fall von open 1 abgezogen werden,
       // da ansonsten die Extension gezeichnet wird, wenn _numClosedPolys = 0 und
       // _numOpenPolys = 1
       if (_containsBoundingPoly) open--;
       return ( open == _numOpenPolys ) && ( closed == _numClosedPolys);

    } // _controlScene
    
  
    
    
} // ExtendablePolygonEditor
