<#--
  "E-Mail-Adresse bestätigen" after registration. No warning box: the visitor did nothing wrong,
  they only have one step left. The address is shown on its own line so it can be checked at a glance.
-->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false displayInfo=true; section>
    <#if section = "header">
        ${msg("emailVerifyTitle")}
    <#elseif section = "form">
        <div class="papo-info">
            <span class="papo-info__icon papo-info__icon--mail" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3.5 6.5 8.5 6.5 8.5-6.5"/></svg>
            </span>
            <p class="instruction">${msg("papoVerifySent")}</p>
            <p class="papo-info__address">${user.email}</p>
            <p class="instruction">${msg("papoVerifyNext")}</p>
        </div>
    <#elseif section = "info">
        <p class="instruction">
            ${msg("emailVerifyInstruction2")}
            <a href="${url.loginAction}">${msg("doClickHere")}</a> ${msg("emailVerifyInstruction3")}
        </p>
    </#if>
</@layout.registrationLayout>
