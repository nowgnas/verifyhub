package com.verifyhub.routing.adapter.in.web;

import com.verifyhub.common.response.ApiResponse;
import com.verifyhub.routing.adapter.in.web.dto.RoutingPolicyListResponse;
import com.verifyhub.routing.adapter.in.web.dto.RoutingPolicyUpdateRequest;
import com.verifyhub.routing.application.RoutingPolicyAdminService;
import com.verifyhub.routing.domain.ProviderRoutingPolicy;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/routing-policies")
public class AdminRoutingPolicyController {

    private final RoutingPolicyAdminService routingPolicyAdminService;

    public AdminRoutingPolicyController(RoutingPolicyAdminService routingPolicyAdminService) {
        this.routingPolicyAdminService = routingPolicyAdminService;
    }

    @GetMapping
    public ApiResponse<RoutingPolicyListResponse> getRoutingPolicies() {
        List<ProviderRoutingPolicy> policies = routingPolicyAdminService.getLatestPolicies();
        return ApiResponse.success(RoutingPolicyListResponse.from(policies));
    }

    @PutMapping
    public ApiResponse<RoutingPolicyListResponse> updateRoutingPolicies(
            @Valid @RequestBody RoutingPolicyUpdateRequest request
    ) {
        List<ProviderRoutingPolicy> policies = routingPolicyAdminService.updatePolicies(request.toCommand());
        return ApiResponse.success(RoutingPolicyListResponse.from(policies));
    }
}
