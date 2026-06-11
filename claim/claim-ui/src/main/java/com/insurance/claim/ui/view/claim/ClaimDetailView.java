package com.insurance.claim.ui.view.claim;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.api.dto.ReserveStatus;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimPolicyReference;
import com.insurance.claim.core.entity.ClaimReserve;
import com.insurance.claim.core.service.ClaimService;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.util.Set;
import java.util.stream.Stream;

@Route(value = "claims/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_Claim.detail")
@ViewDescriptor(path = "claim-detail-view.xml")
@EditedEntityContainer("claimDc")
public class ClaimDetailView extends StandardDetailView<Claim> {

  private final TimeSource timeSource;
  private final PolicyService policyService;
  private final DataManager dataManager;
  private final ClaimService claimService;

  @ViewComponent private EntityComboBox<PolicyDto> policyComboBox;

  @ViewComponent private DataGrid<ClaimReserve> reservesDataGrid;

  public ClaimDetailView(
      TimeSource timeSource,
      PolicyService policyService,
      DataManager dataManager,
      ClaimService claimService) {
    this.timeSource = timeSource;
    this.policyService = policyService;
    this.dataManager = dataManager;
    this.claimService = claimService;
  }

  @Subscribe
  public void onInitEntity(final InitEntityEvent<Claim> event) {
    Claim claim = event.getEntity();
    claim.setReportDate(timeSource.now().toLocalDate());
    claim.setClaimStatus(ClaimStatus.OPEN);
  }

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    Claim claim = getEditedEntity();
    if (claim.getPolicyId() != null) {
      PolicyDto policyDto = policyService.findPolicyById(claim.getPolicyId());
      if (policyDto != null) {
        policyComboBox.setValue(policyDto);
      }
    }
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  @Install(to = "policyComboBox", subject = "itemsFetchCallback")
  private Stream<PolicyDto> policyComboBoxItemsFetchCallback(final Query<PolicyDto, String> query) {
    String filter = query.getFilter().orElse("");
    int limit = query.getLimit();
    int offset = query.getOffset();
    return policyService.findPolicies(filter, limit, offset).stream();
  }

  @Subscribe("policyComboBox")
  public void onPolicyComboBoxComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<EntityComboBox<PolicyDto>, PolicyDto> event) {
    PolicyDto value = event.getValue();
    Claim claim = getEditedEntity();
    ClaimPolicyReference policyRef = claim.getPolicy();
    if (policyRef == null) {
      policyRef = dataManager.create(ClaimPolicyReference.class);
      claim.setPolicy(policyRef);
    }
    if (value == null) {
      policyRef.setPolicyId(null);
      policyRef.setPolicyNo(null);
    } else {
      policyRef.setPolicyId(value.getId());
      policyRef.setPolicyNo(value.getPolicyNo());
    }
  }

  @Subscribe
  public void onValidation(final ValidationEvent event) {
    Claim claim = getEditedEntity();
    if (claim.getPolicy() == null || claim.getPolicyId() == null) {
      event.getErrors().add("A policy must be selected");
    }
  }

  @Subscribe("reservesDataGrid.approve")
  public void onReservesDataGridApprove(final ActionPerformedEvent event) {
    ClaimReserve selected = reservesDataGrid.getSingleSelectedItem();
    if (selected != null && ReserveStatus.PENDING.equals(selected.getReserveStatus())) {
      selected.setReserveStatus(ReserveStatus.APPROVED);
    }
  }

  @Install(target = io.jmix.flowui.view.Target.DATA_CONTEXT)
  private Set<Object> saveDelegate(final io.jmix.core.SaveContext saveContext) {
    Claim claim = getEditedEntity();
    return claimService.saveClaim(claim);
  }
}
