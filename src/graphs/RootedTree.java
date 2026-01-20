package graphs;

import java.util.ArrayList;
import java.util.List;

// Just to show concept of the rooted tree -> optimized for Kruskall in class DSU
class RootedTree<T> {

    private TreeNode<T> root;

    RootedTree(T rootValue) {
        root = new TreeNode<>(rootValue);
    }

    TreeNode<T> getRoot() {
        return root;
    }
    
    void addChild(T x) {
            TreeNode<T> n = new TreeNode<>(x);
            n.parent = root;
            root.children.add(n);
    }
    
    public void printTree() {
        System.out.println( root );
    }
    
    public static void main(String[] args) {

        RootedTree<String> t1 = new RootedTree<>("A");
        
        RootedTree<String> t2 = new RootedTree<>("Q");

        t1.addChild("B");
        t1.addChild("C");
        t1.addChild("D");

        t2.addChild("R");
        t2.addChild("S");
        t2.addChild("T");

        t1.printTree();
        t2.printTree();
    }

    class TreeNode<T> {

        T value;
        TreeNode<T> parent;
        List<TreeNode<T>> children;

        TreeNode(T value) {
            this.value = value;
            this.parent = null;
            this.children = new ArrayList<>();
        }

        void addChild(TreeNode<T> child) {
            child.parent = this;
            children.add(child);
        }
        
        public String toString() {
            String r = "(" + value + ")-> [ ";
            for( TreeNode c : children )
                r += c + " ";
            return r + "]";
        }
    }
}
