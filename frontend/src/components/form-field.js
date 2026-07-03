// <form-field label="Nachname" name="lastName" required></form-field>
// Reads its own <input>/<select>/<textarea> child if provided, otherwise creates a text input.
// Set .error = "message" to show a validation error (FSD: understandable validation feedback).
class FormField extends HTMLElement {
  connectedCallback() {
    const label = this.getAttribute("label") ?? "";
    const name = this.getAttribute("name") ?? "";
    const required = this.hasAttribute("required");
    const existingControl = this.querySelector("input, select, textarea");

    const control = existingControl ?? document.createElement("input");
    if (!existingControl) {
      control.type = this.getAttribute("type") ?? "text";
      for (const attr of ["step", "min", "max"]) {
        if (this.hasAttribute(attr)) control.setAttribute(attr, this.getAttribute(attr));
      }
    }
    control.name = name;
    if (required) control.required = true;

    this.innerHTML = "";
    const wrapper = document.createElement("label");
    wrapper.className = "form-field";

    const labelText = document.createElement("span");
    labelText.className = "form-field__label";
    labelText.textContent = required ? `${label} *` : label;

    const errorText = document.createElement("span");
    errorText.className = "form-field__error";
    errorText.hidden = true;

    wrapper.appendChild(labelText);
    wrapper.appendChild(control);
    wrapper.appendChild(errorText);
    this.appendChild(wrapper);

    this._control = control;
    this._errorText = errorText;
  }

  get value() {
    return this._control?.value ?? "";
  }

  set value(v) {
    if (this._control) this._control.value = v;
  }

  set error(message) {
    if (!this._errorText) return;
    this._errorText.textContent = message ?? "";
    this._errorText.hidden = !message;
    this._control?.classList.toggle("form-field__control--invalid", Boolean(message));
  }
}

customElements.define("form-field", FormField);
