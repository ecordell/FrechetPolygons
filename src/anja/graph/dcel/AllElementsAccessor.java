package anja.graph.dcel;

import anja.graph.*;
import java.util.Iterator;
import java.util.List;

public class AllElementsAccessor<T> implements Iterator<T> {
	
	Iterator<T> _iter;
	T _lastElement;
	DCEL _dcel;
	
	
	public AllElementsAccessor(List source, DCEL dcel) {
		_iter = source.iterator();
		_dcel = dcel;
	}
	
	public boolean hasNext() {
		return _iter.hasNext();
	}
	
	public T next() {
		_lastElement = _iter.next();
		return _lastElement;
	}
	
	public void remove() {
		if (_lastElement instanceof Vertex)
			_dcel.deleteVertex((DCEL_Vertex)_lastElement, false);
		else if (_lastElement instanceof Edge)
			_dcel.deleteEdge((DCEL_Edge)_lastElement, false);
		_iter.remove();		
	}

}
