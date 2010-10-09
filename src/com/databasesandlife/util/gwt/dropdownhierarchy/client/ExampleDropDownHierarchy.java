package com.databasesandlife.util.gwt.dropdownhierarchy.client;

import com.databasesandlife.util.gwt.dropdownhierarchy.client.DropDownHierarchy.LeafNode;
import com.databasesandlife.util.gwt.dropdownhierarchy.client.DropDownHierarchy.Node;
import com.databasesandlife.util.gwt.dropdownhierarchy.client.DropDownHierarchy.NonLeafNode;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class ExampleDropDownHierarchy {

    static abstract class ExampleNode implements Node<String> {
        String displayName;
        ExampleNonLeafNode parent;

        ExampleNode(String n) { displayName = n; }
        void setParent(ExampleNonLeafNode p) { parent = p; }

        public ExampleNonLeafNode getParent() { return parent; }
        public String getDisplayName() { return displayName; }
    }

    static class ExampleLeafNode extends ExampleNode implements LeafNode<String> {
        String id;
        
        public ExampleLeafNode(String n, String i) { super(n); id=i; }

        public String getId() { return id; }
    }

    static class ExampleNonLeafNode extends ExampleNode implements NonLeafNode<String> {
        ExampleNode[] children;
        
        ExampleNonLeafNode(String n, ExampleNode[] c) { super(n); children=c; }

        public ExampleNode[] getChildren() { return children; }
    }

    public static DropDownHierarchy<String> createExample() {
        // root
        //   --> A    (leaf)
        //   --> B
        //     --> C  (leaf)
        //     --> D  (leaf)

        ExampleLeafNode c = new ExampleLeafNode("Node CC", "c");
        ExampleLeafNode d = new ExampleLeafNode("Node D", "d");

        ExampleNonLeafNode b = new ExampleNonLeafNode("Node B", new ExampleNode[] { c, d });
        ExampleLeafNode a = new ExampleLeafNode("Node A", "a");

        ExampleNonLeafNode root = new ExampleNonLeafNode("Node A", new ExampleNode[] { a, b });

        d.setParent(b);
        c.setParent(b);
        b.setParent(root);
        a.setParent(root);

        try { return new DropDownHierarchy<String>(root, "d"); }
        catch (DropDownHierarchy.NodeNotFoundException e) { throw new RuntimeException(e); } // can never happen
    }
}
