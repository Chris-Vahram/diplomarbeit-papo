// Shared required business tax identifier form. The backend validates the
// country/type/value combination again; browser constraints only give quick,
// understandable feedback before submission.
export function taxIdentifierFieldsHtml(prefix = "taxIdentifier") {
  return `
    <div class="entity-form--grid tax-identifier-fields" style="margin-top:0">
      <form-field label="Steuerland (ISO-Code)" name="${prefix}Country" required>
        <input name="${prefix}Country" maxlength="2" pattern="[A-Za-z]{2}" title="Bitte einen zweistelligen ISO-Ländercode eingeben." />
      </form-field>
      <form-field label="Steuerart" name="${prefix}Type" required>
        <select name="${prefix}Type">
          <option value="VAT_ID">Umsatzsteuer-ID (UID)</option>
          <option value="NATIONAL_TAX_ID">Nationale Steuernummer</option>
        </select>
      </form-field>
      <form-field class="form-field--wide" label="Steueridentifikationswert" name="${prefix}Value" required>
        <input name="${prefix}Value" maxlength="64" autocomplete="off" />
      </form-field>
    </div>
  `;
}

export function readTaxIdentifier(form, prefix = "taxIdentifier") {
  return {
    country: form.elements[`${prefix}Country`].value,
    type: form.elements[`${prefix}Type`].value,
    value: form.elements[`${prefix}Value`].value,
  };
}

export function fillTaxIdentifier(form, identifier, prefix = "taxIdentifier") {
  form.elements[`${prefix}Country`].value = identifier?.country ?? "";
  form.elements[`${prefix}Type`].value = identifier?.type ?? "VAT_ID";
  form.elements[`${prefix}Value`].value = identifier?.value ?? "";
}
