package at.papo.partnerportal.commission;

import at.papo.partnerportal.order.Order;
import at.papo.partnerportal.partner.Partner;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;

@Entity
public class CommissionRecord extends PanacheEntity {

    @OneToOne(optional = false)
    public Order order;

    @ManyToOne(optional = false)
    public Partner partner;

    // Calculation basis and percentage are snapshotted at creation time so later
    // changes to Partner.commissionPercent don't retroactively alter past records.
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal orderValue;

    @Column(nullable = false, precision = 5, scale = 2)
    public BigDecimal commissionPercentUsed;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal commissionValue;
}
