/*
 * Created on Feb 24, 2005
 *
 * EditorEvent.java
 * 
 */
package anja.SwingFramework.event;

import anja.SwingFramework.*;

/**
 * This is the abstract base class for all editor action events. Other
 * editor action events should derive from this class so they can be
 * fired by the common editor event dispatcher.
 * 
 * <p>For an example, look at 
 * {@link anja.SwingFramework.graph.GraphEditorEvent}
 * 
 * @author ibragim kuliyev
 * @author Jörg Wegener
 */
public abstract class EditorEvent {

	protected JAbstractEditor _source; // the source editor for this event
	protected EventType _eventType;    // the type of the event

	/* everything else, like storing an event code or event related objects
	 * should be custom implemented by derived classes.
	 */
	public EditorEvent() {
		_source = null;
	}

	public EditorEvent(JAbstractEditor source) {
		_source = source;
	}

	public EditorEvent(JAbstractEditor source, EventType eventType) {
		_source = source;
		_eventType = eventType;
	}

	//*************************************************************************
	/* @return The editor instance that generated this event.
	 */
	public JAbstractEditor getSource() {
		return _source;
	}

	/**
     * @return event type
     */
	public EventType getEvent() {
		return _eventType;
	}
}




