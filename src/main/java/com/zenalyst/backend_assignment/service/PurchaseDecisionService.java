package com.zenalyst.backend_assignment.service;

import com.zenalyst.backend_assignment.model.PurchaseDecision;
import com.zenalyst.backend_assignment.model.PurchaseRequirement;
import com.zenalyst.backend_assignment.model.Supplier;
import com.zenalyst.backend_assignment.model.SupplierOffer;
import com.zenalyst.backend_assignment.repository.ProductRepository;
import com.zenalyst.backend_assignment.repository.SupplierOfferRepository;
import com.zenalyst.backend_assignment.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseDecisionService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierOfferRepository supplierOfferRepository;

    public PurchaseDecisionService(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            SupplierOfferRepository supplierOfferRepository) {

        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.supplierOfferRepository = supplierOfferRepository;
    }

    public List<PurchaseDecision> makePurchaseDecision(
            PurchaseRequirement requirement) {

        validateRequirement(requirement);

        if (!productRepository.existsById(
                requirement.getProductId())) {

            throw new IllegalArgumentException(
                    "Product not found: "
                            + requirement.getProductId());
        }

        int remainingQuantity =
                requirement.getRequiredQuantity();

        List<PurchaseDecision> decisions =
                new ArrayList<>();

        Set<Long> selectedSupplierIds =
                new HashSet<>();

        List<SupplierOffer> offers =
                supplierOfferRepository.findByProductId(
                        requirement.getProductId());

        if (offers.isEmpty()) {

            decisions.add(
                    createUnfulfilledDecision(
                            remainingQuantity,
                            "No supplier offers were found "
                                    + "for the requested product."
                    )
            );

            return decisions;
        }

        while (remainingQuantity > 0) {

            SupplierOffer bestOffer = null;
            Supplier bestSupplier = null;
            int bestQuantity = 0;

            /*
             * Find the cheapest feasible supplier
             * for the remaining quantity.
             */
            for (SupplierOffer offer : offers) {

                if (selectedSupplierIds.contains(
                        offer.getSupplierId())) {

                    continue;
                }

                Supplier supplier =
                        supplierRepository
                                .findById(
                                        offer.getSupplierId())
                                .orElse(null);

                if (supplier == null) {
                    continue;
                }

                if (!canMeetDeadline(
                        supplier,
                        requirement.getRequiredByDate())) {

                    continue;
                }

                int maximumAvailable =
                        Math.min(
                                supplier
                                        .getMaximumSupplyQuantity(),
                                offer.getMaximumQuantity()
                        );

                int quantity =
                        Math.min(
                                remainingQuantity,
                                maximumAvailable
                        );

                int minimumRequired =
                        Math.max(
                                supplier
                                        .getMinimumOrderQuantity(),
                                offer.getMinimumQuantity()
                        );

                /*
                 * The supplier can only be selected if
                 * we can place an order satisfying both:
                 *
                 * - supplier MOQ
                 * - pricing tier minimum
                 */
                if (quantity < minimumRequired) {
                    continue;
                }

                if (bestOffer == null
                        || offer.getPrice()
                        < bestOffer.getPrice()
                        || (
                        offer.getPrice()
                                == bestOffer.getPrice()
                                && quantity > bestQuantity
                )) {

                    bestOffer = offer;
                    bestSupplier = supplier;
                    bestQuantity = quantity;
                }
            }

            /*
             * No supplier can satisfy the remaining quantity.
             */
            if (bestOffer == null) {
                break;
            }

            PurchaseDecision decision =
                    createSelectedDecision(
                            bestSupplier,
                            bestOffer,
                            bestQuantity
                    );

            decisions.add(decision);

            selectedSupplierIds.add(
                    bestSupplier.getId());

            remainingQuantity -= bestQuantity;
        }

        /*
         * Add explanations for suppliers that were rejected
         * because of deadline, MOQ, or capacity constraints.
         */
        addRejectedDecisions(
                offers,
                requirement,
                selectedSupplierIds,
                decisions
        );

        /*
         * Report any remaining quantity.
         */
        if (remainingQuantity > 0) {

            decisions.add(
                    createUnfulfilledDecision(
                            remainingQuantity,
                            "The remaining quantity could not "
                                    + "be fulfilled because available "
                                    + "suppliers could not satisfy "
                                    + "the delivery, minimum order, "
                                    + "pricing tier, or capacity "
                                    + "constraints."
                    )
            );
        }

        return decisions;
    }

    private void validateRequirement(
            PurchaseRequirement requirement) {

        if (requirement == null) {

            throw new IllegalArgumentException(
                    "Purchase requirement is required");
        }

        if (requirement.getProductId() == null) {

            throw new IllegalArgumentException(
                    "Product ID is required");
        }

        if (requirement.getRequiredQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Required quantity must be greater than zero");
        }

        if (requirement.getRequiredByDate() == null) {

            throw new IllegalArgumentException(
                    "Required by date is required");
        }
    }

    private boolean canMeetDeadline(
            Supplier supplier,
            LocalDate requiredByDate) {

        LocalDate expectedDeliveryDate =
                LocalDate.now()
                        .plusDays(
                                supplier.getDeliveryDays());

        return !expectedDeliveryDate
                .isAfter(requiredByDate);
    }

    private PurchaseDecision createSelectedDecision(
            Supplier supplier,
            SupplierOffer offer,
            int quantity) {

        PurchaseDecision decision =
                new PurchaseDecision();

        decision.setSupplierId(
                supplier.getId());

        decision.setSupplierName(
                supplier.getName());

        decision.setQuantity(
                quantity);

        decision.setPricePerUnit(
                offer.getPrice());

        decision.setTotalCost(
                quantity * offer.getPrice());

        decision.setStatus(
                "SELECTED");

        decision.setReason(
                buildSelectionReason(
                        supplier,
                        offer,
                        quantity
                )
        );

        return decision;
    }

    private PurchaseDecision createRejectedDecision(
            Supplier supplier,
            SupplierOffer offer,
            String reason) {

        PurchaseDecision decision =
                new PurchaseDecision();

        decision.setSupplierId(
                supplier.getId());

        decision.setSupplierName(
                supplier.getName());

        decision.setQuantity(0);

        decision.setPricePerUnit(
                offer.getPrice());

        decision.setTotalCost(0);

        decision.setStatus(
                "REJECTED");

        decision.setReason(
                reason);

        return decision;
    }

    private PurchaseDecision createUnfulfilledDecision(
            int quantity,
            String reason) {

        PurchaseDecision decision =
                new PurchaseDecision();

        decision.setSupplierId(null);

        decision.setSupplierName(
                "UNFULFILLED");

        decision.setQuantity(
                quantity);

        decision.setPricePerUnit(0);

        decision.setTotalCost(0);

        decision.setStatus(
                "UNFULFILLED");

        decision.setReason(
                reason);

        return decision;
    }

    private String buildSelectionReason(
            Supplier supplier,
            SupplierOffer offer,
            int quantity) {

        return "Selected because the supplier can meet the "
                + "required delivery deadline, satisfies the "
                + "minimum order quantity, has sufficient "
                + "capacity, and provides the best applicable "
                + "price among the feasible suppliers. "
                + "The applicable price is "
                + offer.getPrice()
                + " per unit for "
                + quantity
                + " units.";
    }

    private void addRejectedDecisions(
            List<SupplierOffer> offers,
            PurchaseRequirement requirement,
            Set<Long> selectedSupplierIds,
            List<PurchaseDecision> decisions) {

        Set<Long> alreadyReported =
                new HashSet<>();

        for (SupplierOffer offer : offers) {

            Supplier supplier =
                    supplierRepository
                            .findById(
                                    offer.getSupplierId())
                            .orElse(null);

            if (supplier == null) {
                continue;
            }

            if (selectedSupplierIds.contains(
                    supplier.getId())) {

                continue;
            }

            if (alreadyReported.contains(
                    supplier.getId())) {

                continue;
            }

            if (!canMeetDeadline(
                    supplier,
                    requirement.getRequiredByDate())) {

                decisions.add(
                        createRejectedDecision(
                                supplier,
                                offer,
                                "Rejected because the supplier "
                                        + "cannot meet the required "
                                        + "delivery deadline."
                        )
                );

                alreadyReported.add(
                        supplier.getId());

                continue;
            }

            int maximumAvailable =
                    Math.min(
                            supplier
                                    .getMaximumSupplyQuantity(),
                            offer.getMaximumQuantity()
                    );

            int possibleQuantity =
                    Math.min(
                            requirement.getRequiredQuantity(),
                            maximumAvailable
                    );

            int minimumRequired =
                    Math.max(
                            supplier.getMinimumOrderQuantity(),
                            offer.getMinimumQuantity()
                    );

            if (possibleQuantity < minimumRequired) {

                decisions.add(
                        createRejectedDecision(
                                supplier,
                                offer,
                                "Rejected because the available "
                                        + "purchase quantity does not "
                                        + "satisfy the supplier minimum "
                                        + "order quantity or the "
                                        + "pricing tier minimum."
                        )
                );

                alreadyReported.add(
                        supplier.getId());
            }
        }
    }
}