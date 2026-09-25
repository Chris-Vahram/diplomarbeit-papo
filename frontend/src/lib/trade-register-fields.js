export function tradeRegisterIdentifierFieldsHtml(prefix = "tradeRegisterIdentifier") {
  return `
    <div class="entity-form--grid trade-register-fields" style="margin-top:0">
      <form-field label="Registerland (ISO-Code)" name="${prefix}Country" required>
        <input name="${prefix}Country" maxlength="2" pattern="[A-Za-z]{2}"
          title="Bitte einen zweistelligen ISO-Ländercode eingeben." autocomplete="off" />
      </form-field>
      <form-field label="Registerart" name="${prefix}Type" required>
        <select name="${prefix}Type">
          <option value="GISA">GISA (Österreich)</option>
          <option value="OTHER_TRADE_REGISTER">Anderes Handelsregister</option>
        </select>
      </form-field>
      <form-field class="form-field--wide" label="Registerkennzeichen" name="${prefix}Value" required>
        <input name="${prefix}Value" maxlength="64" autocomplete="off" />
      </form-field>
    </div>
  `;
}

export function readTradeRegisterIdentifier(form, prefix = "tradeRegisterIdentifier") {
  return {
    country: form.elements[`${prefix}Country`].value,
    type: form.elements[`${prefix}Type`].value,
    value: form.elements[`${prefix}Value`].value,
  };
}

export function fillTradeRegisterIdentifier(form, identifier, prefix = "tradeRegisterIdentifier") {
  form.elements[`${prefix}Country`].value = identifier?.country ?? "";
  form.elements[`${prefix}Type`].value = identifier?.type ?? "GISA";
  form.elements[`${prefix}Value`].value = identifier?.value ?? "";
}
