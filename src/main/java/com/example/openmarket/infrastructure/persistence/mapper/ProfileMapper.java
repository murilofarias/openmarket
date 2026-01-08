package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.*;
import com.example.openmarket.infrastructure.persistence.entity.BuyerEntity;
import com.example.openmarket.infrastructure.persistence.entity.ProfileEntity;
import com.example.openmarket.infrastructure.persistence.entity.SellerEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileEntity toEntity(Profile domain) {
        if (domain instanceof Buyer buyer) {
            return toEntityBuyer(buyer);
        } else if (domain instanceof Seller seller) {
            return toEntitySeller(seller);
        } else {
            throw new IllegalArgumentException("Unknown profile type: " + domain.getClass());
        }
    }

    public ProfileEntity toEntityBuyer(Buyer domain) {
        BuyerEntity buyerEntity = new BuyerEntity();

        buyerEntity.setTotalOrders(domain.getTotalOrders());
        buyerEntity.setDefaultShippingAddress(domain.getDefaultShippingAddress());

        buyerEntity.setRating(domain.getRating());
        buyerEntity.setCreatedAt(domain.getCreatedAt());

        return buyerEntity;
    }

    public ProfileEntity toEntitySeller(Seller domain) {
        SellerEntity sellerEntity = new SellerEntity();

        sellerEntity.setSellerStatus(domain.getSellerStatus());
        sellerEntity.setStoreDescription(domain.getStoreDescription());
        sellerEntity.setStoreName(domain.getStoreName());

        sellerEntity.setRating(domain.getRating());
        sellerEntity.setCreatedAt(domain.getCreatedAt());

        return sellerEntity;
    }

    public Profile toDomain(ProfileEntity entity) {
        if (entity instanceof BuyerEntity buyer) {
            return toDomainBuyer(buyer);
        } else if (entity instanceof SellerEntity seller) {
            return toDomainSeller(seller);
        } else {
            throw new IllegalArgumentException("Unknown profile type: " + entity.getClass());
        }
    }

    public Profile toDomainBuyer(BuyerEntity buyerEntity) {
        return Buyer.reconstitute(
                buyerEntity.getCreatedAt(),
                buyerEntity.getRating(),
                buyerEntity.getDefaultShippingAddress(),
                buyerEntity.getTotalOrders()
        );
    }

    public Profile toDomainSeller(SellerEntity sellerEntity) {
        return Seller.reconstitute(
                sellerEntity.getCreatedAt(),
                sellerEntity.getRating(),
                sellerEntity.getStoreName(),
                sellerEntity.getStoreDescription(),
                sellerEntity.getSellerStatus()
        );
    }
}
