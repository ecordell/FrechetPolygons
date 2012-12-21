package anja.swing.event;


/**
* Ein Listener zur Ueberwachung von {@link anja.swing.Scene Szenen}.<br>
* Ueberwacht wird das Hinzufuegen oder Entfernen von Objekten zu oder aus der
* Szene und die Aenderung eines Szenenobjekts.
*
* @version 0.9 22.07.2004
* @author Sascha Ternes
*/

public interface SceneListener {

/**
* Wird aufgerufen, wenn ein Objekt der Szene hinzugefuegt wurde. Dieses Ereignis
* impliziert die Aenderung der Zeichenprioritaeten der Szenenobjekte, daher folgt
* diesem Ereignis stets das Ereignis
* {@link #prioritiesChanged(SceneEvent) prioritiesChanged}.
*
* @param e das <code>SceneEvent</code>-Objekt
*/
public void objectAdded( SceneEvent e );

/**
* Wird aufgerufen, wenn ein Objekt aus der Szene entfernt wurde. Dieses
* Ereignis impliziert die Aenderung der Zeichenprioritaeten der Szenenobjekte,
* daher folgt diesem Ereignis stets das Ereignis
* {@link #prioritiesChanged(SceneEvent) prioritiesChanged}.
*
* @param e das <code>SceneEvent</code>-Objekt
*/
public void objectRemoved( SceneEvent e );

/**
* Wird aufgerufen, wenn ein Szenenobjekt selektiert wurde.
*
* @param e das <code>SceneEvent</code>-Objekt
*/
public void objectSelected( SceneEvent e );

/**
* Wird aufgerufen, wenn sich ein Datenfeld eines Szenenobjekts geaendert hat.
*
* @param e das <code>SceneEvent</code>-Objekt
*/
public void objectUpdated( SceneEvent e );

/**
* Wird aufgerufen, wenn die Zeichenprioritaet der Szenenobjekte geaendert wurde.
* Dieses Ereignis impliziert die Moeglichkeit einer Aenderung <b>aller</b>
* Zeichenprioritaeten, nicht nur der eines Szenenobjekts.
*
* @param e das <code>SceneEvent</code>-Objekt
*/
public void prioritiesChanged( SceneEvent e );

} // SceneListener
