import java.util.ArrayList;
import java.util.Set;

public class Layer {
    int level;

    //two dimensional array of sets of arrows, corresponding to each cell of the FSD
    ArrayList<ArrayList<Set<Arrow>>> arrows;

    public Layer(Layer another) {
        this.arrows = new ArrayList<ArrayList<Set<Arrow>>>(another.arrows);
    }
    public Layer() {
        this.arrows = new ArrayList<ArrayList<Set<Arrow>>>();
    }
}
