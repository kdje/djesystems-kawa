# Domain model

Customer: internalId, publicId, identityProviderSubject, status.

Retailer: id, code, name, country, status.

LoyaltyAccount: id, customerInternalId, retailerId, retailerCode, loyaltyIdentifier, status.

PartnerAuthorization: oauthClientId, partnerName, allowed retailer(s), enabled.

No points/rewards/promotions domain in MVP.
