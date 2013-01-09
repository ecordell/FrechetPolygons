public class Diagonal {
    int startIndex;
    int endIndex;

    public Diagonal(int start, int end) {
        this.startIndex = start;
        this.endIndex = end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Diagonal other = (Diagonal) obj;
        if (this.startIndex != other.startIndex || this.endIndex != other.endIndex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.startIndex;
        hash = 53 * hash + this.endIndex;
        return hash;
    }

    public boolean containedWithin(Diagonal other) {
        return (this.startIndex >= other.startIndex && this.endIndex <= other.endIndex);
    }
}
