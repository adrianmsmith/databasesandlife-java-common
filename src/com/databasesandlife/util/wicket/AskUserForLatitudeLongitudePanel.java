package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

/** 
 * Asks the user for latitude and longitude via the HTML5 Geolocation API.
 *    <p>
 * To use:
 * <ol>
 * <li>Include JQuery in your page
 * <li>Put a &lt;wicket:container&gt; on your page somewhere, which is this panel (everything in this panel is "display:none")
 * <li>Override isVisible and make this panel invisible (= will not be rendered) if you know the user's geo location
 * (otherwise this panel will submit again the geolocation, i.e. infinite loop.)
 * <li>The user loads your page, this widget determines the location (if the user allows) and submits a form,
 * this widget calls your {@link LatitudeLongitudeListener}.
 * </ol>
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class AskUserForLatitudeLongitudePanel extends Panel {
    
    public interface LatitudeLongitudeListener {
        public void latitudeLongitudeWasDetermined(double latitude, double longitude);
    }
    
    protected double latitude, longitude;
    
    public AskUserForLatitudeLongitudePanel(String wicketId, final LatitudeLongitudeListener listener) {
        super(wicketId);
        
        Form<Void> form = new Form<>("form");
        add(form);
        
        form.add(new TextField<>("latitude",  new PropertyModel<>(this, "latitude")));
        form.add(new TextField<>("longitude", new PropertyModel<>(this, "longitude")));
        form.add(new Button("submit") {
            @Override public void onSubmit() {
                listener.latitudeLongitudeWasDetermined(latitude, longitude);
            }
        });
    }
}
