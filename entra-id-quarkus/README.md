# Configuration reference

The following values in `application.properties` configure the application for use with Microsoft Entra ID OpenID Connect. If the value of such a property is blank in `application.properties`, Quarkus converts the property name into an environment variable and reads the value from the environment. For details on the naming conversion, see [the MicroProfile Config specification](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html#default_configsources.env.mapping).

|Property|Description|Value|
|--------|-----------|-----|
|quarkus.oidc.client-id|The client ID of the registered application.|Application (client) ID value you wrote down earlier.|
|quarkus.oidc.credentials.secret|The client secret of the registered application.|Client secret value you wrote down earlier.|
|quarkus.oidc.auth-server-url|The base URL of the OpenID Connect (OIDC) server.|https://login.microsoftonline.com/{tenant-id}/v2.0. Replace {tenant-id} with the Directory (tenant) ID value you wrote down earlier.|
|quarkus.oidc.application-type|The application type. Use web-app to tell Quarkus that you want to enable the OIDC authorization code flow so that your users are redirected to the OIDC provider to authenticate.|web-app|
|quarkus.oidc.authentication.redirect-path|The relative path for calculating a redirect_uri query parameter.|/|
|quarkus.oidc.authentication.restore-path-after-redirect|Whether to restore the path after redirect.|true|
|quarkus.oidc.roles-claim|The claim that contains the roles of the authenticated user.|roles|
|quarkus.oidc.provider|Well known OpenId Connect provider identifier.|microsoft|
|quarkus.oidc.token.customizer-name|The name of the token customizer.|azure-access-token-customizer|
|quarkus.oidc.logout.path|The relative path of the logout endpoint at the application.|/logout|
|quarkus.oidc.logout.post-logout-path|The relative path of the application endpoint where the user should be redirected to after logging out from the OpenID Connect Provider.|/|
