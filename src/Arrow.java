import java.util.ArrayList;

//TODO: check for null for all arguments of constructors before setting

public class Arrow {
    ArrayList<Arrow> subArrows;
    Interval start;
    Interval end;

    public Arrow() {
        this.start = new Interval();
        this.end = new Interval();
        this.subArrows = new ArrayList<Arrow>();
    }

    public Arrow(Arrow other) {
        this.start = new Interval(other.start);
        this.end = new Interval(other.end);
        if (other.subArrows != null) {
            this.subArrows = new ArrayList<Arrow>(other.subArrows);
        } else {
            this.subArrows = new ArrayList<Arrow>();
        }
    }

    public Arrow(Interval _start, Interval _end) {
        this.start = _start;
        this.end = _end;
        this.subArrows = new ArrayList<Arrow>();
    }

    public Arrow(Interval _start, Interval _end, ArrayList<Arrow> _subs) {
        this.start = _start;
        this.end = _end;
        this.subArrows = _subs;
    }

    public boolean isNull() {
        if (this.start.startGraph == null || this.start.endGraph ==null || this.end.startGraph == null || this.end.endGraph == null) {
            return true;
        }
        return false;
    }

    @Override public String toString() {
        return "Arrow:\n\tFrom: " + start.toString() + "\n\tTo: " + end.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Arrow other = (Arrow) obj;
        if (!this.start.equals(other.start) || !this.end.equals(other.end) || !this.subArrows.equals(other.subArrows)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.start.hashCode();
        hash = 53 * hash + this.end.hashCode();
        return hash;
    }

}
