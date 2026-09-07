package com.zenalyst.backend_assignment.controller;

import com.zenalyst.backend_assignment.model.PurchaseDecision;
import com.zenalyst.backend_assignment.model.PurchaseRequirement;
import com.zenalyst.backend_assignment.service.PurchaseDecisionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseDecisionController {

    private final PurchaseDecisionService purchaseDecisionService;

    public PurchaseDecisionController(
            PurchaseDecisionService purchaseDecisionService) {

        this.purchaseDecisionService =
                purchaseDecisionService;
    }

    @PostMapping("/decide")
    public List<PurchaseDecision> makeDecision(
            @RequestBody PurchaseRequirement requirement) {

        return purchaseDecisionService
                .makePurchaseDecision(requirement);
    }
}