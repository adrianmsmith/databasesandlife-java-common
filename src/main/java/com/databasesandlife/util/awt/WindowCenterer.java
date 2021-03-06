package com.databasesandlife.util.awt;

import java.awt.*;

/**
 * Takes a Window (for example Frame or Dialog) and centers it on the screen.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */

public class WindowCenterer {

    /** Takes a window and makes ot 0.75x the size of the screen, then centers it */
    public void centerAndResizeToNearlyMaximized(Window window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point center = ge.getCenterPoint();
        Rectangle bounds = ge.getMaximumWindowBounds();
        int w = 3*bounds.width/4;
        int h = 3*bounds.height/4;
        int x = center.x - w/2, y = center.y - h/2;
        window.setBounds(x, y, w, h); 
        window.validate();  
    }

    /** Nearly-maximizes a Window in the center of the screen. If the window
      * size is bigger than this v.big size, will be kept at that size, unless
      * bigger than the size of the screen. */
    public void centerBig(Window window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point center = ge.getCenterPoint();
        Rectangle bounds = ge.getMaximumWindowBounds();
        int w = Math.max(3*bounds.width/4, Math.min(window.getWidth(), bounds.width));
        int h = Math.max(3*bounds.height/4, Math.min(window.getHeight(), bounds.height));
        int x = center.x - w/2, y = center.y - h/2;
        window.setBounds(x, y, w, h); 
        window.validate();  
    }

    /** Takes a window and centers it. If bigger than screen, cuts it down
      * to screen-size. If fits on screen, displayed at its desired size. */
    public void center(Window window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point center = ge.getCenterPoint();
        Rectangle bounds = ge.getMaximumWindowBounds();
        int w = Math.min(window.getWidth(), bounds.width);
        int h = Math.min(window.getHeight(), bounds.height);
        int x = center.x - w/2, y = center.y - h/2;
        window.setBounds(x, y, w, h); 
        window.validate();  
    }
}
