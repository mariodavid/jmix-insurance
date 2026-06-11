package com.insurance.policy.api.service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;

/**
 * Public policy API used by quote, account, and UI modules to create and read insurance policies.
 *
 * <p>The policy module owns policy numbers, coverage periods, and policy persistence. Other modules
 * should use this interface instead of depending on policy-core implementation classes.
 */
public interface PolicyService {

  /**
   * Creates a policy from a validated quote or another policy creation flow.
   *
   * <p>Implementations persist the policy, generate the policy number, calculate the coverage end
   * date, and publish the policy-created event used by downstream modules such as account.
   *
   * @param request the data required to create the policy
   * @return the created policy
   * @throws IllegalArgumentException if the request references an unknown insurance product or
   *     payment frequency
   */
  PolicyDto createPolicy(CreatePolicyRequestDto request);

  /**
   * Finds a policy by its technical identifier.
   *
   * @param id the policy UUID
   * @return the policy data, or {@code null} if no policy exists for the id
   */
  PolicyDto findPolicyById(java.util.UUID id);

  /**
   * Searches policies by policy number prefix.
   *
   * @param filter text to match against the policy number (case-insensitive prefix)
   * @param limit maximum number of results
   * @param offset pagination offset
   * @return matching policies
   */
  java.util.List<PolicyDto> findPolicies(String filter, int limit, int offset);
}
