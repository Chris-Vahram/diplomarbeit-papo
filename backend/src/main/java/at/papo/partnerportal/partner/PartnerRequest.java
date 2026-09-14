package at.papo.partnerportal.partner;

import at.papo.partnerportal.business.BusinessTaxIdentifierRequest;
import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Company-side create/edit — full record incl. commissionPercent and active
// (never exposed on the partner self-edit path, see PartnerContactRequest / FR-08).
public class PartnerRequest {
    @NotBlank(message = "Partnernummer ist erforderlich.")
    private String partnerNumber;
    @NotBlank(message = "Name ist erforderlich.")
    private String name;
    @NotNull(message = "Adresse ist erforderlich.")
    @Valid
    private AddressRequest address;
    private String email;
    private String phone;
    @NotNull(message = "Provision ist erforderlich.")
    @DecimalMin(value = "0.0", message = "Provision darf nicht negativ sein.")
    @DecimalMax(value = "100.0", message = "Provision darf 100% nicht überschreiten.")
    private BigDecimal commissionPercent;
    @Valid
    private BusinessTaxIdentifierRequest taxIdentifier;
    @Size(max = 4000, message = "Interne Notiz darf höchstens 4.000 Zeichen enthalten.")
    private String internalNote;
    private Boolean active;
    private boolean internalNoteProvided;

    public PartnerRequest() {
    }

    public String partnerNumber() {
        return partnerNumber;
    }

    public String name() {
        return name;
    }

    public AddressRequest address() {
        return address;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public BigDecimal commissionPercent() {
        return commissionPercent;
    }

    public BusinessTaxIdentifierRequest taxIdentifier() {
        return taxIdentifier;
    }

    public String internalNote() {
        return internalNote;
    }

    public boolean internalNoteProvided() {
        return internalNoteProvided;
    }

    public Boolean active() {
        return active;
    }

    public void setPartnerNumber(String partnerNumber) {
        this.partnerNumber = partnerNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(AddressRequest address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public void setTaxIdentifier(BusinessTaxIdentifierRequest taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    public void setInternalNote(String internalNote) {
        this.internalNoteProvided = true;
        this.internalNote = internalNote;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
