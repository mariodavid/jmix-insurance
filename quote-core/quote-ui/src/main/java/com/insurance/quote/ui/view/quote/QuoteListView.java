package com.insurance.quote.ui.view.quote;

import org.springframework.beans.factory.annotation.Autowired;

import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;

import io.jmix.core.Id;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import com.vaadin.flow.router.Route;

@Route(value = "quotes", layout = DefaultMainViewParent.class)
@ViewController(id = "quote_Quote.list")
@ViewDescriptor(path = "quote-list-view.xml")
@LookupComponent("quotesDataGrid")
@DialogMode(width = "64em")
public class QuoteListView extends StandardListView<Quote> {

    @ViewComponent
    private DataGrid<Quote> quotesDataGrid;

    @Autowired
    private QuoteService quoteService;
    
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private MessageBundle messageBundle;

    @Subscribe("quotesDataGrid.rejectAction")
    public void onQuotesDataGridRejectAction(final ActionPerformedEvent event) {
        Quote quote = quotesDataGrid.getSingleSelectedItem();
        if (quote != null) {
            quoteService.reject(Id.of(quote));
            notifications.create(messageBundle.getMessage("quoteRejectedTitle"), messageBundle.getMessage("quoteRejectedMessage"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            getViewData().loadAll();
        }
    }

    @Subscribe("quotesDataGrid.acceptAction")
    public void onQuotesDataGridAcceptAction(final ActionPerformedEvent event) {
        Quote quote = quotesDataGrid.getSingleSelectedItem();
        if (quote != null) {
            Quote acceptedQuote = (Quote) quoteService.accept(Id.of(quote));
            notifications.create(
                    messageBundle.getMessage("quoteAcceptedTitle"),
                    messageBundle.formatMessage("quoteAcceptedMessage", acceptedQuote.getCreatedPolicyNo())
            )
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            getViewData().loadAll();
        }
    }
}
