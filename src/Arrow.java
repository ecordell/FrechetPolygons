import java.awt.geom.Point2D;
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
            this.subArrows = new ArrayList<Arrow>();
            //deep copy
            for (Arrow sub : other.subArrows) {
                this.subArrows.add(new Arrow(sub));
            }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Arrow arrow = (Arrow) o;

        if (end != null ? !end.equals(arrow.end) : arrow.end != null) return false;
        if (start != null ? !start.equals(arrow.start) : arrow.start != null) return false;
        if (subArrows != null ? !subArrows.equals(arrow.subArrows) : arrow.subArrows != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subArrows != null ? subArrows.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
