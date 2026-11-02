<#--
  PAPO page frame for every Keycloak login page of the realm papo-shop (add-shop-customer-accounts).
  Same contract as Keycloak's base template.ftl (macro name, parameters and nested sections
  "header", "show-username", "form", "socialProviders", "info"), so every base page renders in it.
  Differences: PAPO wordmark instead of the realm banner, one card, messages with drawn icons,
  no language switch (German only).
-->
<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" lang="${(locale.currentLanguageTag)!'de'}">
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow">
    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" type="image/svg+xml" href="${url.resourcesPath}/img/favicon.svg" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <script type="importmap">
        { "imports": { "rfc4648": "${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js" } }
    </script>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="module">
        import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";
        startSessionPolling("${url.ssoLoginInOtherTabsUrl?no_esc}");
    </script>
</head>

<body class="${properties.kcBodyClass!} ${bodyClass}">
<div class="${properties.kcLoginClass!}">
    <header class="papo-brand">
        <span class="papo-brand__mark">PAPO</span>
        <span class="papo-brand__name">${msg("papoBrandName")}</span>
    </header>

    <main class="${properties.kcFormCardClass!}">
        <div class="${properties.kcFormHeaderClass!}">
            <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
                <h1 id="kc-page-title"><#nested "header"></h1>
            <#else>
                <#nested "show-username">
                <div id="kc-username" class="papo-username">
                    <span class="papo-username__label">${msg("papoSignedInAs")}</span>
                    <span id="kc-attempted-username" class="papo-username__value">${auth.attemptedUsername}</span>
                    <a id="reset-login" class="papo-username__change" href="${url.loginRestartFlowUrl}">${msg("papoChangeAccount")}</a>
                </div>
                <h1 id="kc-page-title"><#nested "header"></h1>
            </#if>
            <#if displayRequiredFields>
                <p class="papo-required-hint"><span class="required">*</span> ${msg("requiredFields")}</p>
            </#if>
        </div>

        <div id="kc-content">
            <#-- App-initiated actions should not see warnings about the action they just started. -->
            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                <div class="${properties.kcAlertClass!} papo-alert--${message.type}" role="<#if message.type = 'error'>alert<#else>status</#if>">
                    <svg class="papo-alert__icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <#if message.type = 'success'><circle cx="12" cy="12" r="9"/><path d="m8 12.5 2.5 2.5L16 9.5"/>
                        <#elseif message.type = 'error'><circle cx="12" cy="12" r="9"/><path d="M12 7.5v5.5M12 16.5h.01"/>
                        <#elseif message.type = 'warning'><path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/>
                        <#else><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 7.5h.01"/></#if>
                    </svg>
                    <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                </div>
            </#if>

            <#nested "form">

            <#if auth?has_content && auth.showTryAnotherWayLink()>
                <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                    <input type="hidden" name="tryAnotherWay" value="on"/>
                    <button type="submit" class="papo-link-button" id="try-another-way">${msg("doTryAnotherWay")}</button>
                </form>
            </#if>

            <#nested "socialProviders">

            <#if displayInfo>
                <div id="kc-info" class="${properties.kcSignUpClass!}">
                    <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                        <#nested "info">
                    </div>
                </div>
            </#if>
        </div>
    </main>

    <footer class="papo-footer">
        <p>${msg("papoFooter")}</p>
    </footer>
</div>
</body>
</html>
</#macro>
