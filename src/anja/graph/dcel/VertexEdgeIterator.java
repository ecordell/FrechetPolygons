/*
 * File: VertexEdgeIterator.java
 * Created on Feb 14, 2006 by ibr
 *
 */
package anja.graph.dcel;

import anja.graph.*;
import java.util.Iterator;

import static anja.graph.Constants.*;

/**
 * 
 * @author ibr
 * 
 *         TODO Write documentation
 */
class VertexEdgeIterator implements Iterator<Edge> {
    // *************************************************************************

    // *************************************************************************
    // Public constants
    // *************************************************************************

    // *************************************************************************
    // Private constants
    // *************************************************************************

    // *************************************************************************
    // Class variables
    // *************************************************************************

    // *************************************************************************
    // Public instance variables
    // *************************************************************************

    // *************************************************************************
    // Protected instance variables
    // *************************************************************************

    // *************************************************************************
    // Private instance variables
    // *************************************************************************

    private boolean _hasNext; // next edge available?
    private DCEL_Vertex _centerVertex; // center vertex
    private DCEL_Edge _first_DCEL_Edge; // for stopping the iterator...
    private DCEL_Edge _next_DCEL_Edge;
    private DCEL_Edge _current_DCEL_Edge;
    private int _slingCounter = -1;
    private int _mode;

    // *************************************************************************
    // Constructors
    // *************************************************************************

    public VertexEdgeIterator(DCEL_Vertex source, int mode) {

        // TODO: traps for non-existent connectivity data!

        // setup initial values
        _hasNext = true;
        _centerVertex = source;
        _first_DCEL_Edge = _centerVertex.getDcelEdge();

        if (_first_DCEL_Edge == null || _first_DCEL_Edge.isSling())
            _slingCounter = 0;
        _current_DCEL_Edge = null;
        _next_DCEL_Edge = null;
    }

    // *************************************************************************
    // Public instance methods
    // *************************************************************************

    /**
     * Reimplemented from {@link Iterator#hasNext()}
     */
    public boolean hasNext() {
        return _slingCounter < _centerVertex._slings.size();
    }

    // *************************************************************************

    /**
     * Reimplemented from {@link Iterator#next()} TODO: write doc
     */
    public DCEL_Edge next() {
        if (_current_DCEL_Edge == null)
            _current_DCEL_Edge = _first_DCEL_Edge;
        else
            _current_DCEL_Edge = _next_DCEL_Edge;
        if (_slingCounter == -1) {
            switch (_mode) {
            case CLOCKWISE:
                _next_CW_Edge();
                break;
            case COUNTERCLOCKWISE:
                _next_CCW_Edge();
                break;
            default: // unordered
                _next_CW_Edge();
                break;
            }
            if (_next_DCEL_Edge == _first_DCEL_Edge)
                _slingCounter = 0;
            return _current_DCEL_Edge;
        }
        if (_slingCounter < _centerVertex._slings.size()) {
            _slingCounter++;
            return _centerVertex._slings.get(_slingCounter-1);
        }
        return null;
    }
    
    public void remove() {
    
    }

    // *************************************************************************
    // Package-visible methods
    // *************************************************************************

    // *************************************************************************
    // Protected instance methods
    // *************************************************************************

    // *************************************************************************
    // Private instance methods
    // *************************************************************************

    private void _next_CW_Edge() {
        if (_current_DCEL_Edge.getSource() == _centerVertex)
            // edge is directed FROM the center vertex
            _next_DCEL_Edge = _current_DCEL_Edge.getPrevDcelEdgeCW();
        else
            // edge is directed TO the center vertex
            _next_DCEL_Edge = _current_DCEL_Edge.getNextDcelEdgeCW();
    }

    // *************************************************************************

    private void _next_CCW_Edge() {
        if (_current_DCEL_Edge.getSource() == _centerVertex)
            // edge is directed FROM the center vertex
            _next_DCEL_Edge = _current_DCEL_Edge.getPrevDcelEdgeCCW();
        else
            // edge is directed TO the center vertex
            _next_DCEL_Edge = _current_DCEL_Edge.getNextDcelEdgeCCW();
    }
}
