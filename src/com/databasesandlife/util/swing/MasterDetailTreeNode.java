package com.databasesandlife.util.swing;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * Represents a tree node in a hierarchy/detail situation, such as in the 
 * Windows Explorer. Has following features:
 * <ul>
 * <li> Each node belongs to exactly one <code>JTree</code> with exactly one <code>TreeModel</code>.
 *      The root node stores these. Through a node's parents one gets to the root, and thus these.
 * <li> Assumes that children are cached, and that subclass implements <code>fetchChildren</code>, called once.
 * <li> Assumes that per program only one such tree exists (public static TreeModel).
 * <li> Assumes that parent is known at node creation time. Parent can be changed later.
 * <li> If <code>parent==null</code> then this node is root. In which case it stores table model and tree.
 *      All nodes have <code>getTableModel</code> etc methods, they just call parent.getTableModel until
 *      root is found.
 * <li> Has utility methods such as <code>deleteNode0</code>, <code>redraw</code>, <code>getTreePath</code> etc.
 * <li> Comes with a <code>Node.CellRenderer</code>, allowing icon,text,extra-text to be displayed
 * <li> Supports creation of context menus, through <code>newPopupMenu</code>
 * <li> Installs itself into a Tree (e.g. created in NetBeans), remembers static TreeModel.
 * <li> On tree right-click, pop-up menu opened
 * <li> On tree left-click, detail panel changed (with default "no detail panel")
 * </ul>
 * Usage:
 * <pre>
 *    Tree t = new Tree(); // from NetBeans UI designer
 *    MyRootNode n = new MyNode(null); // extends MasterDetailTreeNode, parent==null
 *    n.installIntoTreeAsRootNode(t);
 * </pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */

@SuppressWarnings("serial")
public abstract class MasterDetailTreeNode
implements TreeNode, java.io.Serializable {
    
    protected class CellRenderer extends JPanel implements TreeCellRenderer {
        JLabel icon = new JLabel(), text = new JLabel(), extraText = new JLabel();
        public CellRenderer() {
            setLayout(new BorderLayout());
            add(icon, BorderLayout.WEST);
            
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            p.add(text);
            p.add(extraText);
            p.setOpaque(false);
            
            add(p, BorderLayout.CENTER);
            
            icon.setPreferredSize(new Dimension(18, 200));
            text.setOpaque(true);
            extraText.setOpaque(true);
            setOpaque(false);
        }
        public Component getTreeCellRendererComponent(
            JTree jTree, Object obj, boolean isSelected, boolean isExpanded,
            boolean isLeaf, int row, boolean hasFocus
        ) {
            MasterDetailTreeNode node = (MasterDetailTreeNode) obj;
            boolean isDropTarget = (node == candidateDropTarget);
            text.setBackground(isSelected ? UIManager.getColor("Tree.selectionBackground")
                           : isDropTarget ? Color.LIGHT_GRAY
                                          : UIManager.getColor("Tree.textBackground"));
            text.setForeground(isSelected ? UIManager.getColor("Tree.selectionForeground")
                                          : UIManager.getColor("Tree.textForeground"));
            text.setText(" " + node.getTreeText());
            icon.setIcon(node.getTreeIcon());
            extraText.setText(node.getExtraText() + " ");
            extraText.setBackground(text.getBackground());
            extraText.setForeground(node.getExtraTextColor().equals(Color.black)
                ? text.getForeground() : node.getExtraTextColor());
            setPreferredSize(new Dimension(text.getPreferredSize().width
                + icon.getPreferredSize().width + extraText.getPreferredSize().width, 20)); // height irrelevant
            return this;
        }
    }
    
    protected class TreeListener extends MouseAdapter implements TreeSelectionListener, DropTargetListener {
        public JPanel detailPanelContainer, defaultDetailPanel;
        public TreeListener(JPanel cont, JPanel def) { detailPanelContainer=cont; defaultDetailPanel=def; }
        protected MasterDetailTreeNode getNodeUnderXY(int x, int y) {
            int selectedRow = tree.getRowForLocation(x, y);
            TreePath selectedPath = tree.getPathForLocation(x, y);
            if(selectedRow == -1) return null;
            return (MasterDetailTreeNode) selectedPath.getLastPathComponent();
        }
        public void mouseReleased(java.awt.event.MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                MasterDetailTreeNode node = getNodeUnderXY(evt.getX(), evt.getY());
                if (node != null) {
                    tree.setSelectionPath(node.newTreePath());
                    final JPopupMenu menu = node.newPopupMenu();

                    // if not invokeLater, then new tree selection is only shown
                    // after pop-up menu disappears
                    final MouseEvent evt2 = evt;
                    if (menu != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                tree.add(menu);
                                menu.show(tree, evt2.getX(), evt2.getY());
                            }
                        });
                    }
                }
            }
        }                                  
        /** Node selection changed */
        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
            JPanel detailPanel;
            TreePath newTP = evt.getNewLeadSelectionPath();
            if (newTP != null) {
                MasterDetailTreeNode node = (MasterDetailTreeNode) newTP.getLastPathComponent();
                detailPanel = node.newDetailJPanel();
            } else {
                detailPanel = defaultDetailPanel;
            }
            detailPanelContainer.removeAll();
            detailPanelContainer.add(detailPanel);
            detailPanel.requestFocus();
            detailPanelContainer.revalidate();
        }                                     
        protected boolean isDragOk(DropTargetDragEvent e, MasterDetailTreeNode node) {
            // 1. flavor ok ?
            boolean flavorOk = false;
            DataFlavor[] flavors = node.getAcceptableDropFlavors();
            for (int i = 0; i < flavors.length; i++)
                if (e.isDataFlavorSupported(flavors[i])) flavorOk = true;
            if ( ! flavorOk) return false;
            
            // 2. action ok ?
            if ((e.getSourceActions() & node.getAcceptableDropActions()) == 0)
                return false;
            
            return true;
        }
        protected void change(MasterDetailTreeNode newNode) {
            MasterDetailTreeNode oldNode = candidateDropTarget;
            candidateDropTarget = newNode;
            if (oldNode == newNode) return;
            if (oldNode != null) oldNode.redraw();
            if (newNode != null) newNode.redraw();
        }
        protected void accept(DropTargetDragEvent e) {
            if (candidateDropTarget != null) e.acceptDrag(candidateDropTarget.getAcceptableDropActions());
            else e.rejectDrag();
        }
        public void dragEnter(DropTargetDragEvent e) {
            dragOver(e);
        }
        public void dragOver(DropTargetDragEvent e) {
            MasterDetailTreeNode node = getNodeUnderXY((int)e.getLocation().getX(), (int)e.getLocation().getY());
            if (node == null) { change(null); accept(e); return; }
            if ( ! isDragOk(e, node)) { change(null); accept(e); return; }
            change(node); accept(e);
        }
        public void dropActionChanged(DropTargetDragEvent e) {
            accept(e);
        }
        public void dragExit(DropTargetEvent e) {
            change(null);
        }
        public void drop(DropTargetDropEvent e) {
            candidateDropTarget.transferrableHasBeenDropped(e);
            e.dropComplete(true);
            change(null);
        }
    }
    
    /** Contains children if loaded, or null meaning not yet loaded */
    protected MasterDetailTreeNode children[] = null;
    
    /** Must always be set, or null if root node */
    protected MasterDetailTreeNode parent;
    
    protected MasterDetailTreeNode(MasterDetailTreeNode parent) {
        this.parent = parent;
    }
    
    public MasterDetailTreeNode[] getChildren() {
        if (children != null) return children;
        else return children = fetchChildren();
    }
    
    public void setParent(MasterDetailTreeNode n) { parent = n; }
    
    /** if root, candidate drop target, otherwise null */
    protected MasterDetailTreeNode candidateDropTarget = null;
    
    /** if root, assigned to a tree, this is a value, otherwise null */
    protected DefaultTreeModel treeModel = null;
    
    /** if root, assigned to a tree, this is a value, otherwise null */
    protected JTree tree = null;
    
    protected JTree getTree() { if (parent == null) return tree; else return parent.getTree(); }
    protected DefaultTreeModel getTreeModel() { if (parent == null) return treeModel; else return parent.getTreeModel(); }
    protected MasterDetailTreeNode getRootNode() { if (parent == null) return this; else return parent.getRootNode(); }
    
    /** Sets <code>Node.treeModel</code> and makes this the first node 
      * of it. Also sets some properties of the tree such as the cell renderer.
      * This can only be called on root nodes (where <code>parent==null</code>)
      * @param detailPanelContainer panel which should contain the detail panels
      * @param defaultDetailPanel if no node selected, this panel is shown in <code>detailContainingPanel</code> */
    public void installIntoTreeAsRootNode(JTree tree, JPanel detailPanelContainer, JPanel defaultDetailPanel) {
        if (parent != null) throw new IllegalStateException("trying to install non-root node into tree as root node");
        this.tree = tree;
        tree.setModel(treeModel = new DefaultTreeModel(this));
        tree.setCellRenderer(new CellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setExpandsSelectedPaths(true);
        TreeListener l = new TreeListener(detailPanelContainer, defaultDetailPanel);
        tree.addMouseListener(l);
        tree.addTreeSelectionListener(l);
        new DropTarget(tree, DnDConstants.ACTION_COPY, l);
    }

    protected String getLeafNameForNodeKey(String key) {
        int dotPosition = -1;
        while (true) {
            int newDotPosition;
            if (dotPosition == -1) newDotPosition = key.indexOf(".");
            else newDotPosition = key.indexOf(".", dotPosition+1);
            if (newDotPosition == -1) break;
            else dotPosition = newDotPosition;
        }
        return key.substring(dotPosition+1);
    }
    
    /** Deletes a node, which must be a member of this's children. Tree
      * model is informed. */
    protected void deleteNode0(MasterDetailTreeNode node) {
        // create new children
        MasterDetailTreeNode[] newChildren = new MasterDetailTreeNode[children.length - 1];
        int newChildrenPos = 0, oldElementInOldChildren = -1;
        for (int childrenPos = 0; childrenPos < children.length; childrenPos++)
            if (children[childrenPos] == node)
                oldElementInOldChildren = childrenPos;
            else
                newChildren[newChildrenPos++] = children[childrenPos];
        if (oldElementInOldChildren == -1 || newChildrenPos != newChildren.length)
            throw new RuntimeException("elements found != 1");
        children = newChildren;
        
        // inform tree model
        getTreeModel().nodesWereRemoved(this, 
            new int[] { oldElementInOldChildren },
            new MasterDetailTreeNode[] { node });
    }
    
    protected void informTreeModelThatNodeHasRadicallyChanged() {
        MasterDetailTreeNode[] siblings = parent.getChildren();
        int thisIndexInParent = -1;
        for (int i = 0; i < siblings.length; i++)
            if (siblings[i] == this) thisIndexInParent = i;
        if (thisIndexInParent == -1) throw new RuntimeException("this not found");
        getTreeModel().nodesChanged(parent, 
            new int[] { thisIndexInParent });
    }

    public TreePath newTreePath() {
        Vector<MasterDetailTreeNode> path = new Vector<MasterDetailTreeNode>();
        MasterDetailTreeNode n = this;
        while (n != null) {
            path.insertElementAt(n, 0);
            n = n.parent;
        }
        return new TreePath(path.toArray());
    }
    
    public void select() {
        JTree tree = getTree();
        TreePath path = newTreePath();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }
    
    public void redraw() {
        getTreeModel().nodeChanged(this);
    }
    
    // from http://www.jguru.com/faq/view.jsp?EID=513951
    protected int expandJTreeNode (javax.swing.JTree tree,
                                   javax.swing.tree.TreeModel model,
                                   MasterDetailTreeNode node, int row)
    {
        if (node != null  &&  !model.isLeaf(node)) {
            tree.expandRow(row);
            for (int index = 0;
                 row + 1 < tree.getRowCount()  &&  
                            index < model.getChildCount(node);
                 index++)
            {
                row++;
                MasterDetailTreeNode child = (MasterDetailTreeNode) model.getChild(node, index);
                if (child == null)
                    break;
                javax.swing.tree.TreePath path;
                while ((path = tree.getPathForRow(row)) != null  &&
                        path.getLastPathComponent() != child)
                    row++;
                if (path == null)
                    break;
                row = expandJTreeNode(tree, model, child, row);
            }
        }
        return row;
    }
    
    public void expandAllChildren() {
        JTree tree = getTree();
        DefaultTreeModel model = getTreeModel();
        int thisRow = tree.getRowForPath(newTreePath());
        expandJTreeNode(tree, model, this, thisRow);
    }
    
    /** Can be called anywhere on the tree. Assumes only one node selected.
      * Returns null if no node selected. */
    public MasterDetailTreeNode getSelectedNode() {
        TreePath p = tree.getSelectionPath();
        if (p == null) return null;
        return (MasterDetailTreeNode) p.getLastPathComponent();
    }
            
    // -----------------------------------------------------------------------
    // Must be implemented by subclasses
    // -----------------------------------------------------------------------
    
    /** Create and return children. No caching need be done, this is done
      * by getChildren() */
    protected abstract MasterDetailTreeNode[] fetchChildren();
    
    /** Create and return a Panel which can be displayed on the right hand 
      * side of the window, when this node is selected. */
    public abstract JPanel newDetailJPanel();
    
    /** Create and return a JPopupMenu which can be displayed when the user
      * right-clicks on this node, or null for no pop-up menu */
    public JPopupMenu newPopupMenu() { return null; }
    
    /** Returns an ImageIcon which represents this icon, to be displayed to the
      * left of it in the tree-view */
    public abstract ImageIcon getTreeIcon();
    
    /** Returns the text which can be used to display this icon in the
      * tree-view */
    public abstract String getTreeText();
    
    /** Returns the text which is in color at the end of the name. */
    public String getExtraText() { return ""; }
    
    /** Returns the color which should be used for the extra text. */
    public Color getExtraTextColor() { return Color.black; }
    
    /** Returns array of Flavors which can be accepted, in the case a drag & drop
      * attempts to drop on this node. */
    public DataFlavor[] getAcceptableDropFlavors() { return new DataFlavor[0]; }
    
    /** Returns a bitmask of acceptable actions such as
      * <code>DnDConstants.ACTION_COPY</code>. */
    public int getAcceptableDropActions() { return 0; }
    
    /** When a <code>Transferrable</code> is dropped on this node, this method
      * is called. In the default implementation this should never be reached,
      * as a default node cannot accept any DataFlavors or accept any actions. */
    public void transferrableHasBeenDropped(DropTargetDropEvent e) {
        throw new RuntimeException("unreachable");
    }
    
    // -----------------------------------------------------------------------
    // javax.swing.tree.TreeNode API
    // -----------------------------------------------------------------------
    
    public Enumeration<MasterDetailTreeNode> children() { return new Vector<MasterDetailTreeNode>(Arrays.asList(getChildren())).elements(); }
    public TreeNode getChildAt(int param) { return getChildren()[param]; }
    public int getChildCount() { return getChildren().length; }
    public TreeNode getParent() { return parent; }
    public boolean isLeaf() { return getChildren().length == 0; }
    public int getIndex(TreeNode treeNode) {
        TreeNode[] children = getChildren();
        for (int i = 0; i < children.length; i++) 
            if (treeNode == children[i]) return i;
        return -1;
    }
}
