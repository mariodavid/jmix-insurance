package com.insurance.claim.ui.view.claim;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimPolicyReference;
import com.insurance.claim.core.service.ClaimService;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.view.*;

import java.util.Set;
import java.util.stream.Stream;

@Route(value = "claims/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_Claim.detail")
@ViewDescriptor(path = "claim-detail-view.xml")
@EditedEntityContainer("claimDc")
public class ClaimDetailView extends StandardDetailView<Claim> {

  private final PolicyService policyService;
  private final DataManager dataManager;
  private final ClaimService claimService;

  @ViewComponent private EntityComboBox<PolicyDto> policyComboBox;

  private boolean isNewEntity;

  public ClaimDetailView(
      PolicyService policyService, DataManager dataManager, ClaimService claimService) {
    this.policyService = policyService;
    this.dataManager = dataManager;
    this.claimService = claimService;
  }

  @Subscribe
  public void onInitEntity(final InitEntityEvent<Claim> event) {
    event.getEntity().setStatus(ClaimStatus.OPEN);
    isNewEntity = true;
  }

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    Claim claim = getEditedEntity();
    if (claim.getPolicy() != null && claim.getPolicy().getPolicyId() != null) {
      PolicyDto policyDto = policyService.findPolicyById(claim.getPolicy().getPolicyId());
      if (policyDto != null) {
        policyComboBox.setValue(policyDto);
      }
    }
  }


  @Install(target = Target.DATA_CONTEXT)
  private Set<Object> saveDelegate(final SaveContext saveContext) {
    Claim claim = getEditedEntity();
    Claim savedClaim = claimService.createClaim(
            claim.getPolicy().getPolicyId(),
            claim.getPolicy().getPolicyNo(),
            claim.getPolicy().getPartnerNo(),
            claim.getDateOfLoss(),
            claim.getDescription(),
            claim.getEstimatedAmount());

    return Set.of(savedClaim);
  }


  @SuppressWarnings("PMD.UnusedPrivateMethod")
  @Install(to = "policyComboBox", subject = "itemsFetchCallback")
  private Stream<PolicyDto> policyComboBoxItemsFetchCallback(
      final Query<PolicyDto, String> query) {
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
    if (value == null) {
      claim.setPolicy(null);
    } else {
      ClaimPolicyReference ref = dataManager.create(ClaimPolicyReference.class);
      ref.setPolicyId(value.getId());
      ref.setPolicyNo(value.getPolicyNo());
      ref.setPartnerNo(value.getPartnerNo());
      claim.setPolicy(ref);
    }
  }
}
