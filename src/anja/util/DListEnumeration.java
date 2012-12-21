package anja.util;

/**
* Enumerator fuer Klasse DList
* @author Peter Koellner
* @version 1.0 24 Apr 1997
* @see DList
*/

public class DListEnumeration {
        protected DList _list;
        protected DListElement _current;

        /**
        * Konstruktor wird aus DList.elements() aufgerufen.
        */
        public DListEnumeration(DList list) {
                _list = list;
                _current = _list._first;
        }

        public DListEnumeration(DListEnumeration cp) {
                _list = cp._list;
                _current = cp._current;
        }

        /**
        * Liefert das erste Objekt in der Liste und setzt den
        * aktuellen Zeiger auf das erste Listenelement.
        */
        public synchronized Object getFirst() {
                _current = _list._first;

                if(_current != null)
                        return _current._data;
                return null;
        }

        /**
        * Liefert das letzte Objekt in der Liste und setzt den
        * aktuellen Zeiger auf das letzte Listenelement.
        */
        public synchronized Object getLast() {
                _current = _list._last;

                if(_current != null)
                        return _current._data;
                return null;
        }

        /**
        * Liefert das naechste Objekt in der Liste und setzt den
        * aktuellen Zeiger auf das naechste Listenelement. Wenn dies
        * mit einer frisch erzeugten Enumeration aufgerufen wird,
        * liefert getNext das erste Element zurueck.
        */
        public synchronized Object getNext() {
                if(_current == null)
                        _current = _list._first;
                else
                        _current = _current._nextEl;

                if(_current != null)
                        return _current._data;
                return null;
        }

        /**
        * Liefert das vorige Objekt in der Liste und setzt den
        * aktuellen Zeiger auf das vorige Listenelement bzw auf das
        * letzte Element.
        */
        public synchronized Object getPrev() {
                if(_current == null)
                        _current = _list._last;
                else
                        _current = _current._prevEl;

                if(_current != null)
                        return _current._data;
                return null;
        }

        /**
        * gibt an, ob noch Objekte nach dem aktuellen vorhanden sind.
        */

        public boolean hasNextElement() {
                if(_current == null)
                        return false;
                return (_current._nextEl != null);
        }

        /**
        * gibt an, ob noch Objekte vor dem aktuellen vorhanden sind.
        */

        public boolean hasPrevElement() {
                if(_current == null)
                        return false;
                return (_current._prevEl != null);
        }

        /**
        * liefert das aktuelle Element
        */

        public Object getCurrent() {
                return _current;
        }

        /**
        * loescht das aktuelle Element aus der Liste.
        * Der aktuelle Zeiger wird auf das naechste Element gesetzt, wenn vorhanden.
        * Sonst auf das Vorhergehende, und auf null bei leerer Liste
        * @return Das neue aktuelle Objekt oder null bei leerer Liste.
        */
        public Object removeCurrent() {
                _current = _list.remove(_current);

                if(_current != null)
                        return _current._data;
                return null;
        }

        public Object removeCurrent_() {
                        _current = _list.remove_(_current);

                        if(_current != null)
                                return _current._data;
                        return null;
                }

        /**
        * Fuegt ein Objekt vor dem aktuellen ein. Das neue Objekt wird das
        * aktuelle Objekt.
        */
        public void insertAtCurrent(Object obj) {
                _current = _list.insertAt(obj, _current);
        }

        /**
        * Fuegt ein Objekt nach dem aktuellen ein. Das neue Objekt wird das
        * aktuelle Objekt.
        */
        public void appendAtCurrent(Object obj) {
                _current = _list.appendAt(obj, _current);
        }

       public Object getAT(int i){
           return null;
       }

}
