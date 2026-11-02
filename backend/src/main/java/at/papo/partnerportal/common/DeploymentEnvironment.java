package at.papo.partnerportal.common;

// papo.environment (add-shop-legal-consent): what this deployment is for. Only PRODUCTION
// enforces the shop legal-text gate; anything that is not explicitly production stays staging.
// The value is converted at startup, so a typo fails the start instead of silently meaning staging.
public enum DeploymentEnvironment {
    STAGING,
    PRODUCTION
}
