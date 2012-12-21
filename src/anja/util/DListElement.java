package anja.util;

/**
 * ListenElemente fuer DList
 * @author Peter Koellner
 * @version 1.0 24 Apr 1997
 * @see DList
 */

public class DListElement {
    public DListElement _prevEl = null;
    public DListElement _nextEl = null;
    public Object _data = null;

    DListElement(Object data) {
        _data = data;
    }

    /**Loescht Element.
     *
     */
    public void Reset() {

        //if (_nextEl != null) {
       //     _nextEl.Reset();
       // }
        _prevEl = null;
        _nextEl = null;
        _data = null;

    }

}
