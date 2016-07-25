package com.databasesandlife.util.gwt.dropdownhierarchy.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Vector;

/**
<p>Displays a hierarchical choice as a series of drop-downs.

<p>For example consider the tree of options</p>

<pre>
    +-- A  (can be selected)
    +-- B
        +-- C (can be selected)
        +-- <b>D</b> (can be selected)
</pre>

<p>Only leaf nodes can be selected. In the above example only A, C and D can be selected. If D is selected then two drop-downs are displayed:</p>

<p>&nbsp;&nbsp;&nbsp;&nbsp;<img src="doc-files/dropdowns.png" width=68 height=39 alt="Screenshot"></p>

<p>Changing options in the drop-down:</p>

<ul>
   <li>Changing the top drop-down to A would select the leaf node A, and the second drop-down would disappear.
   <li>Thereafter, selecting B from the single (top) drop-down would select C (the first leaf node accessible under B) and display
 two drop-downs: the first displaying B (parent node) and the second displaying C (the selected leaf node).
</ul>

<p>The interfaces {@link Node Node}, {@link LeafNode LeafNode} and {@link NonLeafNode NonLeafNode} must be implemented
 by the application program. They have methods such as getParent and getChildren etc.</p>

<p>The application should register a {@link ChangeListener ChangeListener} with the object to be notified when the user makes a selection.</p>

<p>A node has an <b>identity</b>. For example, in the above example the nodes are objects, but the client may request that
 node with an internal String id, "D", be selected. In this case "D" is the identity of the node. The identity may be any object type;
 The NODE_ID generic parameter specifies what type identifies nodes.</p>

<p>A DropDownHierarchy is a GWT widget which may be included in any GWT application. Make sure that the source is available to the
 GWT compiler (the source is included in the databasesandlife-util.jar) and add the following line to the application's GWT XML file:</p>

<pre>
  &lt;inherits name="com.databasesandlife.util.gwt.dropdownhierarchy.DropdownHierarchy"/&gt;
</pre>

@param <N> Node Identifier

 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class DropDownHierarchy<N> extends Composite {

    // ------------------------------------------------------------------------------------------------------------
    // Interfaces, that the client must implement
    // ------------------------------------------------------------------------------------------------------------

    public interface Node<N> {
        public NonLeafNode<N> getParent();
        public String getDisplayName();
    }

    public interface LeafNode<N> extends Node<N> {
        N getId();
    }

    public interface NonLeafNode<N> extends Node<N> {
        public Node<N>[] getChildren();
    }

    public interface ChangeListener<N> {
        /** The implementation does not need to call hier.setSelected(newId) with the new node */
        public void onDropDownHierarchyChange(DropDownHierarchy<N> source, N newId, LeafNode<N> newSelected);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Exceptions
    // ------------------------------------------------------------------------------------------------------------

    public static class NodeNotFoundException extends RuntimeException {
        public NodeNotFoundException(String nodeId) { super(nodeId); }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------------------------------------------

    final NonLeafNode<N> rootNode;
    ChangeListener<N> changeListener = null;
    final VerticalPanel container = new VerticalPanel();

    // ------------------------------------------------------------------------------------------------------------
    // Constructors & Methods
    // ------------------------------------------------------------------------------------------------------------

    protected LeafNode<N> findLeafForId(NonLeafNode<N> node, N id) throws NodeNotFoundException {
        for (Node<N> child : node.getChildren()) {
            if (child instanceof LeafNode)
                if (((LeafNode<N>) child).getId().equals(id)) return (LeafNode<N>) child;
            if (child instanceof NonLeafNode)
                try { return findLeafForId((NonLeafNode<N>) child, id); }
                catch (NodeNotFoundException e) { }
        }
        throw new NodeNotFoundException("" + id);
    }

    protected static <N> LeafNode<N> findAnyLeafNodeUnder(Node<N> parent) {
        if (parent instanceof LeafNode)    return (LeafNode<N>) parent;
        if (parent instanceof NonLeafNode) return findAnyLeafNodeUnder(((NonLeafNode<N>) parent).getChildren()[0]);
        throw new RuntimeException();
    }

    protected boolean isChildOf(Node<N> child, Node<N> potentialParent) {
        if (potentialParent.equals(child)) return true;
        else if (child.getParent() == null) return false;
        else return isChildOf(child.getParent(), potentialParent);
    }

    public DropDownHierarchy(NonLeafNode<N> rootNode, N selectedNodeId) throws NodeNotFoundException {
        this.rootNode = rootNode;
        setSelected(selectedNodeId);
        initWidget(container);
    }

    public static <N> DropDownHierarchy<N> newIgnoreNotFound(NonLeafNode<N> rootNode, N selectedNodeId) {
        try {
            return new DropDownHierarchy<N>(rootNode, selectedNodeId);
        }
        catch (NodeNotFoundException e) {
            return new DropDownHierarchy<N>(rootNode, findAnyLeafNodeUnder(rootNode).getId());
        }
    }

    /**
     * When the user makes a selection, this change listener should be called.
     * Any previous change listener is deleted.
     */
    public void setChangeListener(ChangeListener<N> c) {
        changeListener = c;
    }

    /**
     * Changes which node is selected.
     * Updates the user-interface to reflect the newly selected node.
     * The change listener is not called.
     */
    public void setSelected(N newSelectedNodeId) throws NodeNotFoundException {
        LeafNode<N> selectedNode = findLeafForId(rootNode, newSelectedNodeId);

        final Vector<NonLeafNode<N>> fromRootToLeaf = new Vector<NonLeafNode<N>>();
        for (NonLeafNode<N> n = selectedNode.getParent(); n != null; n = n.getParent()) fromRootToLeaf.add(0, n);

        container.clear();
        for (final NonLeafNode<N> n : fromRootToLeaf) {
            final ListBox dropdown = new ListBox();

            final Node<N>[] children = n.getChildren();
            int selectedIndex = -1;
            for (int idx = 0; idx < children.length; idx++) {
                Node<N> optionNode = children[idx];
                dropdown.addItem(optionNode.getDisplayName());
                if (isChildOf(selectedNode, optionNode)) selectedIndex = idx;
            }
            assert selectedIndex != -1;
            dropdown.setSelectedIndex(selectedIndex);

            dropdown.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    Node<N> selectedAtThisLevel = children[dropdown.getSelectedIndex()];
                    LeafNode<N> leafNodeForNewOption = findAnyLeafNodeUnder(selectedAtThisLevel);
                    setSelected(leafNodeForNewOption.getId());
                    ((ListBox) container.getWidget(fromRootToLeaf.indexOf(n))).setFocus(true);
                    if (changeListener != null) changeListener.onDropDownHierarchyChange(DropDownHierarchy.this,
                        leafNodeForNewOption.getId(), leafNodeForNewOption);
                }
            });

            container.add(dropdown);
        }
    }
}
