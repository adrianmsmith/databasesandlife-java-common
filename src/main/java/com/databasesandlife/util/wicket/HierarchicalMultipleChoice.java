package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.arraycopy;
import static java.util.stream.Collectors.toList;

/**
 * Given a list of objects like "a", "b:c", "b:d" displays a hierarchical list.
 *    <p>
 * Displays a list such as:
 * <pre>
 *     a
 *     b
 *       c
 *       d
 * </pre>
 * The user may then either select individual items such as "c" or "d", or parents such as "b".
 * Selecting a parent results in all children being selected in the underlying model.
 * In case the component is displayed with all children of a parent being pre-selected, the parent is shown as selected in the list.
 *    </p>
 */
public class HierarchicalMultipleChoice<T> extends ListMultipleChoice<HierarchicalMultipleChoice.Choice<T>> {
    
    protected static class Choice<T> implements Serializable {
        /** For example "b" or "b:c" */ 
        protected String path;
        protected List<T> items;
        
        public Choice(String path, List<T> items) {
            this.path = path;
            this.items = items;
        }
        
        public int getIndentPixels() {
            String[] components = path.split(Pattern.quote(":"));
            return 8 + 15 * (components.length - 1);
        }

        @Override public String toString() {
            int colon = path.lastIndexOf(":");
            if (colon == -1) return path;
            else return path.substring(colon + 1);
        }
    }
    
    protected class SelectionModel implements IModel<Collection<Choice<T>>> {
        protected IModel<List<T>> underlyingModel;
        
        public SelectionModel(IModel<List<T>> u) { this.underlyingModel = u; }

        @Override public List<Choice<T>> getObject() {
            List<T> individuals = new ArrayList<>(underlyingModel.getObject());
            List<Choice<T>> result = new ArrayList<>();
            for (Choice<T> c : choices) {
                if (individuals.containsAll(c.items)) {
                    result.add(c);
                    individuals.removeAll(c.items);
                }
            }
            if ( ! individuals.isEmpty()) throw new RuntimeException();
            return result;
        }

        @Override public void setObject(Collection<Choice<T>> object) {
            Set<T> result = new HashSet<>();
            for (Choice<T> c : object)
                result.addAll(c.items);
            underlyingModel.setObject(new ArrayList<>(result));
        }

        @Override public void detach() {
            underlyingModel.detach();
        }
    }
    
    protected class ChoicesModel extends AbstractReadOnlyModel<List<Choice<T>>> {
        @Override public List<Choice<T>> getObject() {
            if (hideChildrenOfSelection)
                return choices.stream()
                    .filter(c -> selectionModel.getObject().stream().noneMatch(sel -> c.path.startsWith(sel.path + ":")))
                    .collect(Collectors.toList());
            else 
                return choices;
        }
    }

    protected SelectionModel selectionModel;
    protected List<Choice<T>> choices;
    protected boolean hideChildrenOfSelection;
    
    /** For example with "a:b" and maxLevel=1 returns "a" */
    protected @Nonnull String getLevel(@Nonnull String[] pathComponents, int maxLevel) {
        String[] result = new String[maxLevel];
        arraycopy(pathComponents, 0, result, 0, maxLevel);
        return String.join(":", result);
    }
    
    public HierarchicalMultipleChoice(
        @Nonnull String wicketId, @Nonnull IModel<List<T>> model, @Nonnull List<T> leafNodes, @Nonnull Function<T, String> toPath
    ) {
        super(wicketId);

        SortedSet<String> choiceStrings = new TreeSet<>(); // for input [a, b:c, b:d], all levels are [a, b, b:c, b:d]
        for (T leaf : leafNodes) {
            String[] components = toPath.apply(leaf).split(Pattern.quote(":"));
            for (int level = 1; level <= components.length; level++)
                choiceStrings.add(getLevel(components, level));
        }
        
        this.choices = choiceStrings.stream()
            .map(path -> new Choice<>(
                path, 
                leafNodes.stream().filter(x -> toPath.apply(x).startsWith(path)).collect(toList())))
            .collect(toList());

        setChoiceRenderer(null);
        setChoices(new ChoicesModel());
        setModel(selectionModel = new SelectionModel(model));
    }

    /**
     * Remove children of selected entries.
     *    <p>
     * The Chosen JS library removes selected elements from the drop-down.
     * This means that when a parent is selected, the children remain, which makes them appear visually under other parents.
     * This option removes children of selected parents.
     * This requires the page to be refreshed every time the selection changes.
     */
    public @Nonnull HierarchicalMultipleChoice<T> setHideChildrenOfSelection(boolean hide) {
        this.hideChildrenOfSelection = hide;
        return this;
    }

    @Override protected void setOptionAttributes(AppendingStringBuffer buffer, Choice<T> choice, int index, String selected) {
        super.setOptionAttributes(buffer, choice, index, selected);
        buffer.append(" style='padding-left: " + choice.getIndentPixels() + "px'");
    }
}
