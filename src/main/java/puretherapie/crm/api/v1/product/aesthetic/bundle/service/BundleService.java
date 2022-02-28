package puretherapie.crm.api.v1.product.aesthetic.bundle.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundleDTO;
import puretherapie.crm.data.product.aesthetic.bundle.AssociationBundleAestheticCarePackage;
import puretherapie.crm.data.product.aesthetic.bundle.AssociationBundleAestheticCarePackageId;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;
import puretherapie.crm.data.product.aesthetic.bundle.repository.AssociationBundleAestheticCarePackageRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundleRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCarePackageRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.util.Map;
import java.util.Random;

@Slf4j
@AllArgsConstructor
@Service
public class BundleService {

    // Variables.

    private final BundleRepository bundleRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final AestheticCarePackageRepository aestheticCarePackageRepository;
    private final AssociationBundleAestheticCarePackageRepository associationBundleAestheticCarePackageRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public void createBundle(String bundleName, double price, Map<Integer, Integer> mapAcStock) {
        if (mapAcStock != null && !mapAcStock.isEmpty()) {
            Bundle bundle = saveBundle(buildBundle(bundleName, price));
            createAllACPackages(mapAcStock, bundle);
        } else
            throw new BundleServiceException("Map AC stock must be not null and not empty");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateBundle(BundleDTO bundle) {
        Bundle b = bundle.transform();
        updateBundle(b);
        updateAllBundleACP(b);
    }

    private void updateBundle(Bundle b) {
        Bundle bSaved = bundleRepository.save(b);
        log.info("Update bundle => {}", bSaved);
    }

    private void updateAllBundleACP(Bundle b) {
        for (AestheticCarePackage acP : b.getAestheticCarePackages()) {
            acP = aestheticCarePackageRepository.save(acP);
            log.info("Update ACP => {}", acP);
        }
    }


    private Bundle buildBundle(String bundleName, double price) {
        return Bundle.builder()
                .name(bundleName)
                .price(price)
                .build();
    }

    private Bundle saveBundle(Bundle bundle) {
        bundle = bundleRepository.save(bundle);
        log.info("Save new bundle => {}", bundle);
        return bundle;
    }

    private void createAllACPackages(Map<Integer, Integer> mapAcStock, Bundle bundle) {
        for (Map.Entry<Integer, Integer> mapEntry : mapAcStock.entrySet()) {
            int idAC = mapEntry.getKey();
            int acQuantity = mapEntry.getValue();

            AestheticCare aestheticCare = aestheticCareRepository.findByIdAestheticCare(idAC);
            createACPackage(bundle, acQuantity, aestheticCare);
        }
    }

    private void createACPackage(Bundle bundle, int acQuantity, AestheticCare aestheticCare) {
        if (aestheticCare != null) {
            AestheticCarePackage aestheticCarePackage = saveACPackage(buildACPackage(acQuantity, aestheticCare));
            createBundleACPackageAssociation(bundle, aestheticCarePackage);
        } else
            throw new BundleServiceException("Unknown ac id");
    }

    private AestheticCarePackage buildACPackage(int acQuantity, AestheticCare aestheticCare) {
        if (acQuantity <= 0)
            throw new BundleServiceException("AC Package quantity cannot be less or equal to 0");

        return AestheticCarePackage.builder()
                .aestheticCare(aestheticCare)
                .name("gen_auto_ac_package_" + aestheticCare.getIdAestheticCare() + "_" + Math.sqrt(Math.pow(new Random().nextInt(), 2)))
                .numberAestheticCare(acQuantity)
                .build();
    }

    private AestheticCarePackage saveACPackage(AestheticCarePackage aestheticCarePackage) {
        aestheticCarePackage = aestheticCarePackageRepository.save(aestheticCarePackage);
        log.info("Save new ACPackage => {}", aestheticCarePackage);
        return aestheticCarePackage;
    }

    private void createBundleACPackageAssociation(Bundle bundle, AestheticCarePackage aestheticCarePackage) {
        AssociationBundleAestheticCarePackage association = buildBundleACPackageAssociation(bundle, aestheticCarePackage);
        saveBundleACPackageAssociation(association);
    }

    private AssociationBundleAestheticCarePackage buildBundleACPackageAssociation(Bundle bundle, AestheticCarePackage aestheticCarePackage) {
        return AssociationBundleAestheticCarePackage.builder()
                .id(buildBundleACPackageId(bundle, aestheticCarePackage))
                .bundle(bundle)
                .aestheticCarePackage(aestheticCarePackage)
                .build();
    }

    private AssociationBundleAestheticCarePackageId buildBundleACPackageId(Bundle bundle, AestheticCarePackage aestheticCarePackage) {
        return AssociationBundleAestheticCarePackageId.builder()
                .idBundle(bundle.getIdBundle())
                .idAestheticCarePackage(aestheticCarePackage.getIdAestheticCarePackage())
                .build();
    }

    private void saveBundleACPackageAssociation(AssociationBundleAestheticCarePackage association) {
        association = associationBundleAestheticCarePackageRepository.save(association);
        log.info("Save new AssociationBundleACPackage => {}", association);
    }

    // Exception

    public static class BundleServiceException extends RuntimeException {
        public BundleServiceException(String message) {
            super(message);
        }
    }

}
