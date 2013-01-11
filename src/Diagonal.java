public class Diagonal {
    int startIndex;
    int endIndex;
    boolean isTrueDiagonal;

    public Diagonal(int start, int end) {
        this.startIndex = start;
        this.endIndex = end;
        this.isTrueDiagonal = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Diagonal diagonal = (Diagonal) o;

        if (endIndex != diagonal.endIndex) return false;
        if (isTrueDiagonal != diagonal.isTrueDiagonal) return false;
        if (startIndex != diagonal.startIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + (isTrueDiagonal ? 1 : 0);
        return result;
    }

    public boolean containedWithin(Diagonal other) {
        return (this.startIndex >= other.startIndex && this.endIndex <= other.endIndex);
    }
}
