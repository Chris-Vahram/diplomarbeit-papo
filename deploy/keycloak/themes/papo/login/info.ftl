<#--
  Info pages (e.g. "E-Mail-Adresse bestätigt" when the link was opened in another browser).
  Keycloak's base page repeats the message as heading and text; here the heading is a short
  title and the message stays the text. Without a way back (no client page known) the page says
  what to do instead of ending without a next step.
-->
<#import "template.ftl" as layout>
<#-- Done only when nothing is left to click (Keycloak also marks the confirm step as success). -->
<#assign papoDone = message?has_content && message.type = 'success' && !(actionUri?has_content)>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${kcSanitize(msg("${messageHeader}"))?no_esc}
        <#elseif papoDone>
            ${msg("papoInfoDoneTitle")}
        <#elseif actionUri?has_content>
            ${msg("papoInfoConfirmTitle")}
        <#else>
            ${msg("papoInfoTitle")}
        </#if>
    <#elseif section = "form">
        <div id="kc-info-message" class="papo-info">
            <#if papoDone>
                <span class="papo-info__icon papo-info__icon--success" aria-hidden="true">
                    <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
            </#if>
            <p class="instruction">${message.summary}<#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${kcSanitize(msg("requiredAction.${reqActionItem}"))?no_esc}<#sep>, </#items></b></#list></#if></p>
            <#if skipLink??>
            <#elseif pageRedirectUri?has_content>
                <a class="papo-button papo-button--primary papo-button--block" href="${pageRedirectUri}">${kcSanitize(msg("backToApplication"))?no_esc}</a>
            <#elseif actionUri?has_content>
                <a class="papo-button papo-button--primary papo-button--block" href="${actionUri}">${kcSanitize(msg("proceedWithAction"))?no_esc}</a>
            <#elseif (client.baseUrl)?has_content>
                <a class="papo-button papo-button--primary papo-button--block" href="${client.baseUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a>
            <#else>
                <p class="papo-info__next">${msg("papoInfoCloseHint")}</p>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>
