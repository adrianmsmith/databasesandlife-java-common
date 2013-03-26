package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class GroupedDropDownChoice<T> extends FormComponentPanel<List<T>> {

    public static class Group<T> implements Serializable {

        private List<T> values;
        private String groupName;
        
        public Group(String groupName,List<T> values) {
            this.values = values;
            this.groupName = groupName;
        }

        public List<T> getValues(){
            return values;
        }
        
        public String getGroupName(){
            return groupName;
        }
        
    }

    private List<T> selectModel = new ArrayList<T>();
	private Select<List<T>> select;
	
	public GroupedDropDownChoice(String wicketId, IModel<List<T>> model,List<Group<T>> values,final IOptionRenderer<T> renderer,String htmlId,boolean multiple){
		super(wicketId, model);
		select = new Select<List<T>>("select",new PropertyModel<List<T>>(this,"selectModel"));
		if(!htmlId.isEmpty()) select.add(new AttributeModifier("id", htmlId));
		if(multiple) select.add(new AttributeModifier("multiple","multiple"));
		select.add(new AttributeModifier("class","abc"));
		add(select);
		ListView<Group<T>> groups = new ListView<Group<T>>("groups",values) {
			@Override
			protected void populateItem(ListItem<Group<T>> arg0) {
				Group<T> g = arg0.getModelObject();
				WebMarkupContainer optgroup = new WebMarkupContainer("optgroup");
				optgroup.add(new AttributeModifier("label",g.getGroupName()));
				//for the sake of customization a list view with SelectOption objects
				//can be replaced with:
				// optgroup.add(new SelectOptions<T>("select-option", new PropertyModel<List<T>>(g,"values"), renderer));
				optgroup.add(new ListView<T>("select-options",g.getValues()){

					@Override
					protected void populateItem(ListItem<T> arg0) {
						T value = arg0.getModelObject();
						SelectOption<String> opt = new SelectOption<String>("select-option",new Model(renderer.getDisplayValue(value)));
						opt.add(new AttributeModifier("class","abc"));
						opt.add(new Label("option-text",renderer.getDisplayValue(value)));
						arg0.add(opt);
					}
					
				});
				arg0.add(optgroup);
			}
		};	
		select.add(groups);
	}
	
	@Override
	protected void convertInput(){
		setConvertedInput(select.getConvertedInput());
    }

}
