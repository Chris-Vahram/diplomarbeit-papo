package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.TradeRegisterIdentifier;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "partner_trade_proof", uniqueConstraints = @UniqueConstraint(
        name = "uk_partner_trade_proof_partner", columnNames = "partner_id"))
public class PartnerTradeProof extends PanacheEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false, unique = true)
    public Partner partner;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "register_country", length = 2, nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "register_type", length = 32, nullable = false)),
            @AttributeOverride(name = "value", column = @Column(name = "register_value", length = 64, nullable = false))
    })
    public TradeRegisterIdentifier registerIdentifier;

    @Column(nullable = false, length = 36)
    public String version;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    public byte[] data;

    @Column(nullable = false, length = 32)
    public String contentType;

    @Column(nullable = false, length = 255)
    public String filename;

    @Column(nullable = false)
    public long sizeBytes;

    @Column(nullable = false)
    public Instant uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public PartnerTradeProofStatus status;

    public Instant reviewedAt;

    @Column(length = 1000)
    public String rejectionReason;
}
