import java.util.ArrayList;

//Tree for diagonal order
class DiagonalTree {
    private DiagonalNode root;

    public DiagonalTree(Diagonal rootData) {
        root = new DiagonalNode();
        root.data = rootData;
        root.children = new ArrayList<DiagonalNode>();
    }

    public DiagonalNode root(){
        return root;
    }
    public static class DiagonalNode {
        Diagonal data;
        DiagonalNode parent;
        ArrayList<DiagonalNode> children;

        public boolean hasChildren() {
            return (children.size() > 0);
        }

        public void print() {
            print("", true);
        }

        private void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + "(" + data.startIndex + " - " + data.endIndex + ")");
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
                }
                if (children.size() >= 1) {
                    children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
                }
            }
        }
    }

    public void addDiagonal(Diagonal diag) {
        DiagonalNode node = new DiagonalNode();
        node.data = diag;
        node.children = new ArrayList<DiagonalNode>();
        insert(node, root);
    }

    public void subdivideDiagonals() {
        //after all are added, split out the ends into their own nodes
        divideDiagonalsOf(root);
    }

    void divideDiagonalsOf(DiagonalNode node) {
        if (node.children.size() > 0) {
            //add extra children if necessary
            addMissingChildren(node);
            for (DiagonalNode n : node.children) {
                divideDiagonalsOf(n);
            }
        } else {
            int span = node.data.endIndex - node.data.startIndex;
            if (span > 1) {
                for (int i = 0; i < span; i++) {
                    Diagonal newDiag = new Diagonal(node.data.startIndex + i, node.data.startIndex + i + 1);
                    DiagonalNode newNode = new DiagonalNode();
                    newNode.data = newDiag;
                    newNode.children = new ArrayList<DiagonalNode>();
                    newNode.parent = node;
                    node.children.add(newNode);
                }
            }
        }
    }

    void insert(DiagonalNode toBeInserted, DiagonalNode parentNode) {
        if (parentNode.children.size() > 0) {
            for (DiagonalNode node : parentNode.children) {
                if (toBeInserted.data.containedWithin(node.data)) {
                    insert(toBeInserted, node);
                    return;
                }
            }
            parentNode.children.add(toBeInserted);
            toBeInserted.parent = parentNode;
        } else {
            parentNode.children.add(toBeInserted);
            toBeInserted.parent = parentNode;
        }
    }

    void addMissingChildren(DiagonalNode baseNode) {
         //Children are always ordered?

        if (baseNode.data.startIndex < baseNode.children.get(0).data.startIndex) {
            Diagonal startDiag = new Diagonal(baseNode.data.startIndex, baseNode.children.get(0).data.startIndex);
            DiagonalNode startNode = new DiagonalNode();
            startNode.parent = baseNode;
            startNode.children = new ArrayList<DiagonalNode>();
            startNode.data = startDiag;
            baseNode.children.add(0, startNode);
        }

        if (baseNode.data.endIndex > baseNode.children.get(baseNode.children.size() - 1).data.endIndex) {
            Diagonal endDiag = new Diagonal(baseNode.children.get(baseNode.children.size() - 1).data.endIndex, baseNode.data.endIndex);
            DiagonalNode endNode = new DiagonalNode();
            endNode.parent = baseNode;
            endNode.children = new ArrayList<DiagonalNode>();
            endNode.data = endDiag;
            baseNode.children.add(endNode);
        }

        if (baseNode.children.size() > 1) {
            for (int i = 0; i < baseNode.children.size() - 1; i++) {
                DiagonalNode firstNode = baseNode.children.get(i);
                DiagonalNode secondNode = baseNode.children.get(i+1);
                if (firstNode.data.endIndex < secondNode.data.startIndex) {
                    Diagonal newDiagonal = new Diagonal(firstNode.data.endIndex, secondNode.data.startIndex);
                    DiagonalNode newNode = new DiagonalNode();
                    newNode.parent = baseNode;
                    newNode.children = new ArrayList<DiagonalNode>();
                    newNode.data = newDiagonal;
                    baseNode.children.add(i+1, newNode);
                }
            }
        }
    }

    void print() {
        root.print();
    }
}
