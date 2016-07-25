package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Drop-down where options are in groups; user may select only one element or multiple elements.
 * @param <T> The object type to be selected
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class GroupedDropDownChoice<T extends Serializable> extends FormComponentPanel<List<T>> {

    public static class DropDownChoiceGroup<T> implements Serializable {
        private List<T> values;
        private String groupName;
        public DropDownChoiceGroup(String groupName,List<T> values) { this.values = values; this.groupName = groupName; }
        public DropDownChoiceGroup(String groupName) { this(groupName, new ArrayList<T>()); }
        public List<T> getValues(){ return values; }
        public String getGroupName(){ return groupName; }
        public void add(T item) { values.add(item); }
    }

    /** Used by wicket */ 
    protected List<T> selectModel = new ArrayList<T>();
    private Select<List<T>> select;
    
    protected void init(IModel<List<T>> model, List<DropDownChoiceGroup<T>> values, final IOptionRenderer<T> renderer, String htmlId) {
        setModel(model);
        select = new Select<List<T>>("select",new PropertyModel<List<T>>(this,"selectModel"));
        if(!htmlId.isEmpty()) select.add(new AttributeModifier("id", htmlId));
        if(isMultiple()) select.add(new AttributeModifier("multiple","multiple"));
        add(select);
        ListView<DropDownChoiceGroup<T>> groups = new ListView<DropDownChoiceGroup<T>>("groups",values) {
            @Override
            protected void populateItem(ListItem<DropDownChoiceGroup<T>> arg0) {
                DropDownChoiceGroup<T> g = arg0.getModelObject();
                WebMarkupContainer optgroup = new WebMarkupContainer("optgroup");
                optgroup.add(new AttributeModifier("label",g.getGroupName()));
                //for the sake of customization a list view with SelectOption objects
                //can be replaced with:
                 optgroup.add(new SelectOptions<T>("select-options", new PropertyModel<List<T>>(g,"values"), renderer));
//              optgroup.add(new ListView<T>("select-options",g.getValues()){
//
//                  @Override
//                  protected void populateItem(ListItem<T> item) {
//                      T value = item.getModelObject();
//                      SelectOption<T> opt = new SelectOption<T>("select-option", Model.of(value));
//                      opt.add(new Label("option-text",renderer.getDisplayValue(value)));
//                      item.add(opt);
//                  }
//                  
//              });
                arg0.add(optgroup);
            }
        };  
        select.add(groups);
    }
    
    protected GroupedDropDownChoice(String wicketId) {
        super(wicketId);
    }
    
    /**
     * @param model allow multiple selections, unless model is a {@link SingleEntryModelAdaptor}.
     */
    public GroupedDropDownChoice(
        String wicketId, IModel<List<T>> model, List<DropDownChoiceGroup<T>> values, final IOptionRenderer<T> renderer, String htmlId
    ){
        super(wicketId);
        init(model, values, renderer, htmlId);
    }
    
    public boolean isMultiple() {
        return ! (getModel() instanceof SingleEntryModelAdaptor);
    }
    
    @Override protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.setName("span");  // So that clients can write <select wicket:id="xx"> and we generate <select> etc.
    }
    
    @Override
    protected void convertInput(){
        setConvertedInput(select.getConvertedInput());
    }
    
    @Override
    protected void onBeforeRender(){
        selectModel = getModelObject(); //to pre-select the passed value
        
        // Select object tries to determine if it should set a List<X> or just an X, by inspecting the current model.
        // If it's a Collection it sets List<X> otherwise X. But if it's null, it doesn't know, so just sets X.
        // But our model is always List<X> so we get a ClassCastException if Select gives us an X.
        // We could just say "if (null) { model=new List(); }" but that is unclean; what meaning does it have, if you
        // want to select a list of things, for that list to be null? The correct value is, if nothing is selected, the empty list.
        if (selectModel == null) throw new NullPointerException("model is null; use empty list instead");
        
        super.onBeforeRender();
    }
}
