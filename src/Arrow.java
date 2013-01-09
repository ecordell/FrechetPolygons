import java.util.ArrayList;

public class Arrow {
    ArrayList<Arrow> subArrows;
    Interval start;
    Interval end;
    @Override public String toString() {
        return "Arrow:\n\tFrom: " + start.toString() + "\n\tTo: " + end.toString();
    }

    public void setStart(Interval _start) {
        this.start = _start;
    }

    public void setEnd(Interval _end) {
        this.end = _end;
    }
}
