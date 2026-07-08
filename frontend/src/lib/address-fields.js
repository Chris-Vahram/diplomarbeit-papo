// Shared address sub-form: five grouped <form-field> inputs (street, houseNumber,
// postalCode, city, country) composing into the structured Address shape the
// backend expects (Kunde/Partner). One place to render/read/fill/require it so
// every address form (Kunde, Partner, manual company order) stays consistent.

export function addressFieldsHtml(prefix, required = true) {
  const req = required ? "required" : "";
  return `
    <div class="entity-form--grid address-fields" style="margin-top:0">
      <form-field class="form-field--wide" label="Straße" name="${prefix}Street" ${req}></form-field>
      <form-field label="Hausnummer" name="${prefix}HouseNumber" ${req}></form-field>
      <form-field label="PLZ" name="${prefix}PostalCode" ${req}></form-field>
      <form-field label="Ort" name="${prefix}City" ${req}></form-field>
      <form-field label="Land" name="${prefix}Country" ${req}></form-field>
    </div>
  `;
}

export function readAddress(form, prefix) {
  return {
    street: form.elements[`${prefix}Street`].value,
    houseNumber: form.elements[`${prefix}HouseNumber`].value,
    postalCode: form.elements[`${prefix}PostalCode`].value,
    city: form.elements[`${prefix}City`].value,
    country: form.elements[`${prefix}Country`].value,
  };
}

export function fillAddress(form, prefix, address) {
  form.elements[`${prefix}Street`].value = address?.street ?? "";
  form.elements[`${prefix}HouseNumber`].value = address?.houseNumber ?? "";
  form.elements[`${prefix}PostalCode`].value = address?.postalCode ?? "";
  form.elements[`${prefix}City`].value = address?.city ?? "";
  form.elements[`${prefix}Country`].value = address?.country ?? "";
}

export function setAddressRequired(form, prefix, required) {
  for (const key of ["Street", "HouseNumber", "PostalCode", "City", "Country"]) {
    form.elements[`${prefix}${key}`].required = required;
  }
}
