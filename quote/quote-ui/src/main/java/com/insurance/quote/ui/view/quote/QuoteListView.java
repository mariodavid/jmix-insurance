package com.insurance.quote.ui.view.quote;

import com.insurance.quote.api.dto.QuoteDto;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
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
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "quotes", layout = DefaultMainViewParent.class)
@ViewController(id = "quote_Quote.list")
@ViewDescriptor(path = "quote-list-view.xml")
@LookupComponent("quotesDataGrid")
@DialogMode(width = "64em")
@CssImport("./quote/styles.css")
public class QuoteListView extends StandardListView<Quote> {

  @ViewComponent private DataGrid<Quote> quotesDataGrid;

  @Autowired private QuoteService quoteService;

  @Autowired private Notifications notifications;

  @ViewComponent private MessageBundle messageBundle;

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    updateActionsState();
  }

  @Subscribe("quotesDataGrid")
  public void onQuotesDataGridSelection(final SelectionEvent<Grid<Quote>, Quote> event) {
    updateActionsState();
  }

  private void updateActionsState() {
    Quote quote = quotesDataGrid.getSingleSelectedItem();
    boolean isPending = quote != null && quote.getStatus() == QuoteStatus.PENDING;
    boolean hasPremium = quote != null && quote.getCalculatedPremium() != null;

    quotesDataGrid.getAction("acceptAction").setEnabled(isPending && hasPremium);
    quotesDataGrid.getAction("rejectAction").setEnabled(isPending);
  }

  @Subscribe("quotesDataGrid.rejectAction")
  public void onQuotesDataGridRejectAction(final ActionPerformedEvent event) {
    Quote quote = quotesDataGrid.getSingleSelectedItem();
    if (quote != null) {
      quoteService.reject(Id.of(quote));
      notifications
          .create(
              messageBundle.getMessage("quoteRejectedTitle"),
              messageBundle.getMessage("quoteRejectedMessage"))
          .withType(Notifications.Type.SUCCESS)
          .show();
      getViewData().loadAll();
      updateActionsState();
    }
  }

  @Subscribe("quotesDataGrid.acceptAction")
  public void onQuotesDataGridAcceptAction(final ActionPerformedEvent event) {
    Quote quote = quotesDataGrid.getSingleSelectedItem();
    if (quote != null) {
      QuoteDto acceptedQuote = quoteService.accept(Id.of(quote));
      notifications
          .create(
              messageBundle.getMessage("quoteAcceptedTitle"),
              messageBundle.formatMessage(
                  "quoteAcceptedMessage", acceptedQuote.getCreatedPolicyNo()))
          .withType(Notifications.Type.SUCCESS)
          .show();
      getViewData().loadAll();
      updateActionsState();
    }
  }
}
