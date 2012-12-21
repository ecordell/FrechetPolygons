package anja.swinggui;

import java.awt.*;
import java.awt.geom.*;


/**
 * Das Inteface DisplayPanelScene muss von Klassen implementiert werden,
 * deren Objekte in einem DisplayPanel graphisch dargestellt werden
 * sollen.
 *
 * Da ein DisplayPanel Scroll- und Zoomfunktionen verwaltet, muss
 * eine DisplayPanelScene definierte Abmessungen und Position
 * haben, die mit getBoundingBox() erfragt und mit fitToBox() gesetzt
 * werden koennen.
 * Dabei beziehen sich die verwendeten Koordinaten auf das Koordinaten-
 * system der Zeichenflaeche ( Canvas-Koordinaten ).
 *
 * Zum Zeichnen der Szenerie muss die Methode paint() entsprechend
 * bereitgestellt werden.
 *
 * @version 0.1 21.07.01
 * @author      Ulrich Handel
 */

//****************************************************************
public interface DisplayPanelScene
//****************************************************************
{
    /**
     * Liefert das die Szene umschliessende Rechteck
     */

    //============================================================
    public abstract Rectangle2D getBoundingBox();
    //============================================================


    /**
     * Liefert die maximale Bounding-Box bzw. null falls es keine
     * definierten Begrenzungen gibt.
     *
     * Falls es eine maximale Bounding-Box gibt, so liegt jedes
     * durch getBoundingBox() ermittelte Rechteck vollstaendig
     * innerhalb dieser Box.
     */

    //============================================================
    public abstract Rectangle2D getBoundingBoxMax();
    //============================================================


    /**
     * Setzt Position und Abmessung der Szene neu, so dass bei
     * einem nachfolgenden paint() die Szene entsprechend gezeichnet
     * wird.
     */

    //============================================================
    public abstract void fitToBox( double x,
                                   double y,
                                   double width,
                                   double height );
    //============================================================



    /**
     * Zeichnet die Szene
     */

    //============================================================
    public abstract void paint( Graphics g );
    //============================================================

} // DisplayPanelScene
