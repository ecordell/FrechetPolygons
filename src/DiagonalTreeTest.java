import org.junit.Test;

public class DiagonalTreeTest {
    @Test
    public void testAddDiagonal() throws Exception {
        System.out.println("Test Add Diagonal: ");
        DiagonalTree testTree = new DiagonalTree(new Diagonal(0, 9));
        testTree.addDiagonal(new Diagonal(2, 4));
        testTree.addDiagonal(new Diagonal(5, 8));
        testTree.addDiagonal(new Diagonal(6, 7));
        testTree.print();
    }

    @Test
    public void testSubdivideDiagonals() throws Exception {
        System.out.println("Test Subdivide Diagonals: ");
        DiagonalTree testTree = new DiagonalTree(new Diagonal(0, 9));
        testTree.addDiagonal(new Diagonal(2, 4));
        testTree.addDiagonal(new Diagonal(5, 8));
        testTree.addDiagonal(new Diagonal(6, 7));
        testTree.subdivideDiagonals();
        testTree.print();
    }
}
