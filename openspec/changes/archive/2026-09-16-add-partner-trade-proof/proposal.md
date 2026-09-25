## Why

Partner companies must submit a current business authorization proof for company review. Existing public article-image storage and routes are unsuitable for confidential verification documents.

## What Changes

- Let a partner upload or replace exactly one current proof for its own company.
- Let company users review, download, accept or reject each proof.
- Support PDF, JPEG and PNG up to 10 MiB with content-based validation and private download headers.
- Store upload, replacement and review metadata; keep no document history.
- Record the portal company's own trade-register/GISA identifier in its company profile without a self-approval workflow.
- Keep partner activation independent until the business explicitly chooses to make approval a prerequisite.
- Do not add OCR, multiple files, public URLs, automated register checks or direct BMD synchronization.

## Capabilities

### New Capabilities

- `partner-trade-proof`: Private current-document submission and company review workflow.

### Modified Capabilities

- `company-profile`: The portal company records its own current trade-register/GISA identifier.
- `partner-management`: Company partner management shows proof metadata and review state without exposing file bytes in ordinary partner DTOs.
- `data-import`: Preserve a bounded import request after raising the global multipart ceiling required by proof uploads.

## Impact

Affected areas are new proof persistence and company/partner endpoints, company profile, upload limits, authorization, partner and company UI, backup documentation and security tests. The existing product-image route and cache policy are not reused.
