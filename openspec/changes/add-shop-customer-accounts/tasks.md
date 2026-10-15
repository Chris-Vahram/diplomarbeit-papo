## 1. Identity (essential)

- [ ] 1.1 Realm export `papo-shop`: public client with PKCE, registration, verify e-mail, reset, password policy, brute-force protection, SMTP from secrets
- [ ] 1.2 Quarkus OIDC tenant `papo-shop` for `/api/shop-customer/*`; tests that portal APIs reject shop tokens and vice versa

## 2. Backend (essential)

- [ ] 2.1 `ShopCustomerAccount` profile endpoints
- [ ] 2.2 Link checkout orders/Kunde to account subject; own orders endpoint with ownership check
- [ ] 2.3 Data export and account deletion (restricted service account); tests

## 3. Frontend (essential)

- [ ] 3.1 Shop login/registration via redirect flow; logout
- [ ] 3.2 "Mein Konto": profile, own orders, export, delete
- [ ] 3.3 Prefilled checkout for logged-in customers

## 4. Verification

- [ ] 4.1 Tests + package; `git diff --check`
- [ ] 4.2 Browser: register → verify (test mailbox) → order → see order → delete account
- [ ] 4.3 Security review of realm separation; update traceability
