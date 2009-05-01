package adrian.util.gui.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The same as a JLabel but the text disappears after a while. Use the method
 * setVanishingText to set the text. After 5 seconds of the method being
 * called, the text disappears.
 * 
 * @version $Id: JAutoVanishingLabel.java 438 2006-03-15 18:59:06Z adrian $
 */

public class JAutoVanishingLabel
extends JLabel {

    protected Timer timer =
        new Timer(5000, new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 setText("");
             }
        });

    public JAutoVanishingLabel() {
        super();
        setPreferredSize(new Dimension(200, 17));
    }
    
    public void setBold() {
        setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
    }

    public void setVanishingText(String val) {
        setText(val);
        timer.stop();
        timer.setRepeats(false);
        timer.start();
    }
}