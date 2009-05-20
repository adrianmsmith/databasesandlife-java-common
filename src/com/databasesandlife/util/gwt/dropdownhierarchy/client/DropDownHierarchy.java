package com.databasesandlife.util.gwt.dropdownhierarchy.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
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

<p>&nbsp;&nbsp;&nbsp;&nbsp;<img src="doc-files/dropdowns.png" width=68 height=39></p>

<p>Changing options in the drop-down:</p>

<ul>
   <li>Changing the top drop-down to A would select the leaf node A, and the second drop-down would disappear.
   <li>Thereafter, selecting B from the single (top) drop-down would select C (the first leaf node accessible under B) and display two drop-downs: the first displaying B (parent node) and the second displaying C (the selected leaf node).
</ul>

<p>The interfaces {@link Node Node}, {@link LeafNode LeafNode} and {@link NonLeafNode NonLeafNode} must be implemented by the application program. They have methods such as getParent and getChildren etc.</p>

<p>The application should register a {@link ChangeListener ChangeListener} with the object to be notified when the user makes a selection.</p>

<p>The display texts of the nodes can be language-specific. Languages can be identified by arbitrary objects (e.g. Strings, Locales, etc.) The DropDownHierarchy takes a language object to its constructor. The Node interface requires node objects provide the method {@link Node#getDisplayNameForLanguage getDisplayNameForLanguage}. The LANG_ID generic parameter specifies what sort of objects identify the language.</p>

<p>A node has an <b>identity</b>. For example, in the above example the nodes are objects, but the client may request that node with an internal String id, "D", be selected. In this case "D" is the identity of the node. The identity may be any object type; The NODE_ID generic parameter specifies what type identifies nodes.</p>

<p>A DropDownHierarchy is a GWT widget which may be included in any GWT application. Make sure that the source is available to the GWT compiler (the source is included in the databasesandlife-util.jar) and add the following line to the application's GWT XML file:</p>

<pre>
  &lt;inherits name="com.databasesandlife.util.gwt.dropdownhierarchy.DropdownHierarchy"/&gt;
</pre>

@param <L> Language Identifier
@param <N> Node Identifier

 * @version $Revision$
 * @author Adrian Smith &lt;adrian.m.smith@gmail.com&gt;
 */
public class DropDownHierarchy<L,N> extends SimplePanel {

    // ------------------------------------------------------------------------------------------------------------
    // Interfaces, that the client must implement
    // ------------------------------------------------------------------------------------------------------------

    public interface Node<L,N> {
        public NonLeafNode<L,N> getParent();
        public String getDisplayNameForLanguage(L lang);
    }

    public interface LeafNode<L,N> extends Node<L,N> {
        N getId();
    }

    public interface NonLeafNode<L,N> extends Node<L,N> {
        public Node<L,N>[] getChildren();
    }

    public interface ChangeListener<L,N> {
        /** The implementation does not need to call hier.setSelected(newId) with the new node */
        public void onChange(N newId, LeafNode<L,N> newSelected);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------------------------------------------

    NonLeafNode<L,N> rootNode;
    L lang;
    ChangeListener<L,N> changeListener = null;

    // ------------------------------------------------------------------------------------------------------------
    // Constructors & Methods
    // ------------------------------------------------------------------------------------------------------------

    protected LeafNode<L,N> findLeafForId(NonLeafNode<L,N> node, N id) {
        for (Node<L,N> child : node.getChildren()) {
            if (child instanceof LeafNode)
                if (((LeafNode<L,N>) child).getId().equals(id)) return (LeafNode<L,N>) child;
            if (child instanceof NonLeafNode) {
                LeafNode<L,N> result = findLeafForId((NonLeafNode<L,N>) child, id);
                if (result != null) return result;
            }
        }
        return null;
    }

    protected LeafNode<L,N> findAnyLeafNodeUnder(Node<L,N> parent) {
        if (parent instanceof LeafNode)    return (LeafNode<L,N>) parent;
        if (parent instanceof NonLeafNode) return findAnyLeafNodeUnder(((NonLeafNode<L,N>) parent).getChildren()[0]);
        throw new RuntimeException();
    }

    protected boolean isChildOf(Node<L,N> child, Node<L,N> potentialParent) {
        if (potentialParent.equals(child)) return true;
        else if (child.getParent() == null) return false;
        else return isChildOf(child.getParent(), potentialParent);
    }

    public DropDownHierarchy(NonLeafNode<L,N> rootNode, L lang, N selectedNodeId) {
        this.rootNode = rootNode;
        this.lang = lang;
        setSelected(selectedNodeId);
    }

    /**
     * When the user makes a selection, this change listener should be called.
     * Any previous change listener is deleted.
     */
    public void setChangeListener(ChangeListener<L,N> c) {
        changeListener = c;
    }

    /**
     * Changes which node is selected.
     * Updates the user-interface to reflect the newly selected node.
     * The change listener is not called.
     */
    public void setSelected(N newSelectedNodeId) {
        LeafNode<L,N> selectedNode = findLeafForId(rootNode, newSelectedNodeId);

        Vector<NonLeafNode<L,N>> fromRootToLeaf = new Vector<NonLeafNode<L,N>>();
        for (NonLeafNode<L,N> n = selectedNode.getParent(); n != null; n = n.getParent()) fromRootToLeaf.add(0, n);

        VerticalPanel container = new VerticalPanel();
        for (NonLeafNode<L,N> n : fromRootToLeaf) {
            final ListBox dropdown = new ListBox();

            final Node<L,N>[] children = n.getChildren();
            int selectedIndex = -1;
            for (int idx = 0; idx < children.length; idx++) {
                Node<L,N> optionNode = children[idx];
                dropdown.addItem(optionNode.getDisplayNameForLanguage(lang));
                if (isChildOf(selectedNode, optionNode)) selectedIndex = idx;
            }
            assert selectedIndex != -1;
            dropdown.setSelectedIndex(selectedIndex);

            dropdown.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    Node<L,N> selectedAtThisLevel = children[dropdown.getSelectedIndex()];
                    LeafNode<L,N> leafNodeForNewOption = findAnyLeafNodeUnder(selectedAtThisLevel);
                    setSelected(leafNodeForNewOption.getId());
                    if (changeListener != null) changeListener.onChange(leafNodeForNewOption.getId(), leafNodeForNewOption);
                }
            });

            container.add(dropdown);
        }

        setWidget(container);
    }
}
