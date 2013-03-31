package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * @param <T> The object type to be selected
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
	
	protected GroupedDropDownChoice(String wicketId) {
	    super(wicketId);
	}
	
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
				// optgroup.add(new SelectOptions<T>("select-option", new PropertyModel<List<T>>(g,"values"), renderer));
				optgroup.add(new ListView<T>("select-options",g.getValues()){

					@Override
					protected void populateItem(ListItem<T> item) {
						T value = item.getModelObject();
						SelectOption<T> opt = new SelectOption<T>("select-option", Model.of(value));
						opt.add(new AttributeModifier("class","abc"));
						opt.add(new Label("option-text",renderer.getDisplayValue(value)));
						item.add(opt);
					}
					
				});
				arg0.add(optgroup);
			}
		};	
		select.add(groups);
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
}
