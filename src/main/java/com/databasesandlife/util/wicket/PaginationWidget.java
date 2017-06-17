package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * Pagination widget compatible with twitter bootstrap.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class PaginationWidget extends Panel {
    
    public interface PaginationWidgetCallback extends Serializable {
        public Link<?> newLinkToPageIdx(String wicketId, int pageIdx);
    }
    
    public PaginationWidget(String wicketId, final int pageIdx, int resultCount, int resultsPerPage, final PaginationWidgetCallback c) {
        super(wicketId);

        int maxPageIdxIncl = (resultCount - 1) / resultsPerPage;
        setVisible(maxPageIdxIncl > 0);
        List<Integer> pages = new ArrayList<>();
        for (int p = pageIdx - 10; p < pageIdx + 10; p++) {
            if (p < 0) continue;
            if (p > maxPageIdxIncl) continue;
            pages.add(p);
        }
        add(c.newLinkToPageIdx("pagination.prev", pageIdx - 1).setVisible(pageIdx > 0));
        add(c.newLinkToPageIdx("pagination.next", pageIdx + 1).setVisible(pageIdx < maxPageIdxIncl));
        
        add(new ListView<Integer>("pagination", pages) {
            protected void populateItem(ListItem<Integer> item) {
                int destPageIdx = item.getModelObject();
                Link<?> link = c.newLinkToPageIdx("link", destPageIdx); 
                if (destPageIdx == pageIdx) item.add(new AttributeAppender("class", new Model<String>("active"), " "));
                link.add(new Label("pageNo", "" + (1 + destPageIdx)));
                item.add(link);
            } 
        });
    }
}
