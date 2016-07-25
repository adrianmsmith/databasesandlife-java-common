package com.databasesandlife.util.swing;

import javax.swing.table.*;

/**
 * If you have a table model which is too slow, you can wrap it in 
 * a CachingTableModel.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */

@SuppressWarnings("serial")
public class CachingTableModel 
extends AbstractTableModel {
    
    protected TableModel source;

    protected int columnCount, rowCount;
    protected Object[][] cells;
    /** In case a cell might be null? probably better to have a separate storaeg
      * for which cells are already loaded */
    protected boolean[][] isLoaded;
    
    public CachingTableModel(TableModel source) {
        this.source = source;
        invalidateCache();
    }
    
    public void invalidateCache() {
        columnCount = source.getColumnCount();
        rowCount = source.getRowCount();
        cells = new Object[rowCount][columnCount];
        isLoaded = new boolean[rowCount][columnCount];
    }
    
    // ----------------------------------------------------------------------
    // AbstractTableModel API
    // ----------------------------------------------------------------------
    
    public int getColumnCount() { return columnCount; }
    public String getColumnName(int col) { return source.getColumnName(col); }
    public int getRowCount() { return rowCount; }
    
    public synchronized Object getValueAt(int row, int col) {
        if ( ! isLoaded[row][col]) {
            cells[row][col] = source.getValueAt(row, col);
            isLoaded[row][col] = true;
        }
        return cells[row][col];
    }
}
