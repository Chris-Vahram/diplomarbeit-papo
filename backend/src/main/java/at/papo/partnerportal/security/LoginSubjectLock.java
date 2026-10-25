package at.papo.partnerportal.security;

import io.quarkus.hibernate.orm.panache.Panache;

// One transactional lock per OIDC subject, shared by partner bindings and the company
// provisioning script (deploy/tenancy/provision-company.sql uses the same key), so a subject
// can never end up bound on both sides by two concurrent requests.
public final class LoginSubjectLock {

    private LoginSubjectLock() {
    }

    public static void lock(String subject) {
        Panache.getEntityManager()
                .createNativeQuery("select pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, "login_subject:" + subject)
                .getSingleResult();
    }
}
