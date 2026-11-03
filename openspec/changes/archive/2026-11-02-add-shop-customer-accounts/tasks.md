## 1. Identity (essential)

- [x] 1.1 Realm export `papo-shop`: public client with PKCE, registration, verify e-mail, reset, password policy, brute-force protection, SMTP from secrets
- [x] 1.2 Quarkus OIDC tenant `papo-shop` for `/api/shop-customer/*`; tests that portal APIs reject shop tokens and vice versa

## 2. Backend (essential)

- [x] 2.1 `ShopCustomerAccount` profile endpoints
- [x] 2.2 Link checkout orders/Kunde to account subject; own orders endpoint with ownership check
- [x] 2.3 Data export and account deletion (restricted service account); tests

## 3. Frontend (essential)

- [x] 3.1 Shop login/registration via redirect flow; logout
- [x] 3.2 "Mein Konto": profile, own orders, export, delete
- [x] 3.3 Prefilled checkout for logged-in customers

## 4. Verification

- [x] 4.1 Tests + package; `git diff --check`
- [x] 4.2 Browser: register → verify (test mailbox) → order → see order → delete account
- [x] 4.3 Security review of realm separation; update traceability
