# Tutorial: Connect to a PostgreSQL database from a Quarkus web app on Azure App Service without secrets using a managed identity

This tutorial shows you how to connect to a PostgreSQL database from a Quarkus web app on Azure App Service using a managed identity, instead of using secrets. [Azure App Service](https://learn.microsoft.com/azure/app-service/overview) provides a highly scalable, self-patching web hosting service in Azure. It also provides a managed identity for your app, which is a turn-key solution for securing access to [Azure Database for PostgreSQL](https://learn.microsoft.com/azure/postgresql/) flexible server and other Azure services. Managed identities in App Service make your app more secure by eliminating secrets - such as credentials in the environment variables - from your app. For more information, see [Use managed identities for App Service and Azure Functions](https://learn.microsoft.com/azure/app-service/overview-managed-identity). In this tutorial, you learn how to perform the following tasks:

> [!div class="checklist"]
> * Create an Azure Database for PostgreSQL flexible server.
> * Deploy a sample app to Azure App Service.
> * Configure a Quarkus web application to use Microsoft Entra authentication with PostgreSQL Database.
> * Connect to PostgreSQL Database with Managed Identity using Service Connector.
## Prerequisites

* An Azure subscription. [Create one for free.](https://azure.microsoft.com/free/)
* A Unix-like operating system - for example, Ubuntu, Azure Linux, macOS, or Windows Subsystem for Linux (WSL2) - installed locally.
* [Git](https://git-scm.com/)
* [Java Development Kit (JDK) 17](https://learn.microsoft.com/java/openjdk/install)
* [Maven](https://maven.apache.org)
* [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) version 2.71.0 or higher
* [Docker](https://docs.docker.com/get-docker/)

## Clone the repo and prepare the sample app

Use the following steps to clone the repo and prepare the sample app:

1. Use the following commands to clone the repo and navigate to the sample app directory **app-service-quarkus**:

    ```bash
    git clone https://github.com/Azure-Samples/quarkus-azure.git
    cd quarkus-azure
    git checkout 2025-04-15
    cd app-service-quarkus
    ```

1. Make sure Docker is running, and then run the sample app with Quarkus development mode by using the following command:

    ```bash
    mvn quarkus:dev
    ```

1. After the application starts, it's accessible at `http://localhost:8080`. Open the URL in your browser and try the app.
1. Access the REST API for the application by using the following commands:
    ```bash
    export WEBAPP_URL=http://localhost:8080
    curl -v -X POST ${WEBAPP_URL}/resources/todo -H "Content-Type: application/json" -d '
    {
    "description": "Test REST API",
    "completed": "true"
    }'
    curl ${WEBAPP_URL}/resources/todo
    ```
    The following response is typical:
    ```json
    {
      "id": 1,
      "description": "Test REST API",
      "completed": true
    }
    ```
1. Press <kbd>q</kbd> to stop the application.
## Create an Azure Database for PostgreSQL flexible server
Use the following steps to create an Azure Database for Postgres flexible server instance in your subscription. The Quarkus app connects to this database.
1. Use the following commands to sign in to the Azure CLI and optionally set your subscription, if you have more than one connected to your sign-in credentials:
    ```azurecli
    az login
    az account set --subscription <subscription-id>
    ```
1. Use the following command to create an Azure resource group, noting the resource group name:
    ```azurecli
    export RESOURCE_GROUP=<resource-group-name>
    export LOCATION=eastus2
    az group create --name $RESOURCE_GROUP --location $LOCATION
    ```
1. Use the following command to create an Azure Database for PostgreSQL flexible server instance with Microsoft Entra authentication enabled, and a database:
    ```azurecli
    # Register Microsoft.DBforPostgreSQL provider and wait until completion
    az provider register --namespace Microsoft.DBforPostgreSQL --wait
    # Create an Azure PostgreSQL server with Microsoft Entra authentication enabled and database
    export POSTGRESQL_HOST=<unique-postgresql-host-name>
    export DATABASE_NAME=demodb
    az postgres flexible-server create \
        --resource-group $RESOURCE_GROUP \
        --name $POSTGRESQL_HOST \
        --location $LOCATION \
        --database-name $DATABASE_NAME \
        --public-access 0.0.0.0 \
        --sku-name Standard_B1ms \
        --tier Burstable \
        --version 17 \
        --microsoft-entra-auth Enabled
    ```
## Create an Azure App Service Web App
Use the following commands to create an Azure App Service web app on Linux with Java 17 and Java Standard Edition (SE) as the runtime stack. The sample app is deployed to the web app later.
```azurecli
export APPSERVICE_NAME=<unique-app-service-name>
export APPSERVICE_PLAN=asp-$APPSERVICE_NAME
# Create an App Service plan
az appservice plan create \
    --resource-group $RESOURCE_GROUP \
    --name $APPSERVICE_PLAN \
    --location $LOCATION \
    --sku B1 \
    --is-linux
# Create an App Service web app with Java 17 and Java SE
az webapp create \
    --resource-group $RESOURCE_GROUP \
    --name $APPSERVICE_NAME \
    --plan $APPSERVICE_PLAN \
    --runtime "JAVA:17-java17"
# Configure the web app
az webapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $APPSERVICE_NAME \
    --settings \
        WEBSITE_SKIP_AUTOCONFIGURE_DATABASE=true \
        PORT=8080 \
        WEBSITES_PORT=8080
```
## Connect the Postgres database with identity connectivity
Next, you connect the Azure App Service web app to the Azure Database for PostgreSQL flexible server instance using [Service Connector](https://learn.microsoft.com/azure/service-connector/overview) by using the following steps:
1. Install the Service Connector passwordless extension for the Azure CLI by using the following command:
    ```azurecli
    az extension add --name serviceconnector-passwordless --upgrade
    ```
1. Connect your app to a Postgres database with a system-assigned managed identity using Service Connector by using the following command:
    ```azurecli
    az webapp connection create postgres-flexible \
        --resource-group $RESOURCE_GROUP \
        --name $APPSERVICE_NAME \
        --target-resource-group $RESOURCE_GROUP \
        --server $POSTGRESQL_HOST \
        --database $DATABASE_NAME \
        --system-identity \
        --client-type java \
        --yes
    ```
    This command creates a connection between your web app and your PostgreSQL server, manages authentication through a system-assigned managed identity, and injects an `AZURE_POSTGRESQL_CONNECTIONSTRING` application setting into your web app.
    > [!NOTE]
    > This command can fail to run locally because of various reasons, like missing dependencies or network issues. You can create the connection in the Azure portal as a workaround by using the following steps:
    >
    > 1. Open the Azure portal in your browser and navigate to the Azure App Service web app you created in the previous step.
    > 1. In the navigation pane, select **Settings** > **Service Connector**.
    > 1. Select **Create**. You should see the **Create connection** popup window.
    > 1. In the **Basic** pane, for **Service type**, select **DB for PostgreSQL flexible server**. For **PostgreSQL database**, select the **demodb** database you created in the previous step. For **Client type**, select **Java**. Leave other fields at their default values, and then select **Next: Authentication**.
    > 1. In the **Authentication** pane, for **Authentication type**, select **System assigned managed identity**, and then select **Next: Networking**.
    > 1. In the **Networking** pane, select **Next: Review + create**.
    > 1. In the **Review + create** pane, wait for the validation to pass, and then select **Create on Cloud Shell**. The Cloud Shell opens and then executes the commands to create the connection. Wait for the commands to finish, and then close the Cloud Shell.
1. Add a new application setting, `quarkus.datasource.jdbc.url`, to the web app by using the following commands:
    > [!NOTE]
    > This setting consists of the injected application setting, `AZURE_POSTGRESQL_CONNECTIONSTRING`, and the authentication plugin class name. The value of the setting is the Java Database Connectivity (JDBC) URL for the PostgreSQL database, which is used by the Quarkus application to connect to the database.
    ```azurecli
    # Retrieve the value of the injected application setting AZURE_POSTGRESQL_CONNECTIONSTRING
    export AZURE_POSTGRESQL_CONNECTIONSTRING=$(az webapp config appsettings list \
        --resource-group $RESOURCE_GROUP \
        --name $APPSERVICE_NAME \
        --query "[?name=='AZURE_POSTGRESQL_CONNECTIONSTRING'].value" \
        --output tsv)
    # Set the JDBC URL for the PostgreSQL database that contains the connection string and authentication plugin class name
    az webapp config appsettings set \
        --resource-group $RESOURCE_GROUP \
        --name $APPSERVICE_NAME \
        --settings 'quarkus.datasource.jdbc.url='${AZURE_POSTGRESQL_CONNECTIONSTRING}'&authenticationPluginClassName=com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin'
    ```
## Deploy the application to App Service
You're now ready to deploy the sample app to App Service. Use the following command to build a Java Archive (JAR) file from the sample app and deploy it to the Azure App Service web app you created in the previous step:

```azurecli
mvn clean package
az webapp deploy \
    --resource-group $RESOURCE_GROUP \
    --name $APPSERVICE_NAME \
    --src-path target/todo-runner.jar \
    --type jar \
    --restart true
```

Successful deployment should include output similar to the following example:

```json
{
  "id": "/subscriptions/<subscription-id>/resourceGroups/<resource-group-name>/providers/Microsoft.Web/sites/<app-service-name>/deploymentStatus/<deployment-id>",
  "location": "<location>",
  "name": "<deployment-id>",
  "properties": {
    "deploymentId": "<deployment-id>",
    "errors": null,
    "failedInstancesLogs": null,
    "numberOfInstancesFailed": 0,
    "numberOfInstancesInProgress": 0,
    "numberOfInstancesSuccessful": 1,
    "status": "RuntimeSuccessful"
  },
  "resourceGroup": "<resource-group-name>",
  "type": "Microsoft.Web/sites/deploymentStatus"
}
```

## Test the sample web app

Use the following command to retrieve the URL of your web app:

```azurecli
export WEBAPP_URL=https://$(az webapp show \
    --resource-group $RESOURCE_GROUP \
    --name $APPSERVICE_NAME \
    --query defaultHostName \
    --output tsv)
echo $WEBAPP_URL
```

The app you deployed to Azure App Service is accessible at the URL you retrieved. You can open the URL in your browser to test the app. Alternatively, use the similar `curl` commands in the previous section to test the app, but substitute the URL of your web app for `http://localhost:8080`.

## Clean up resources

In the preceding steps, you created Azure resources in a resource group. If you don't expect to need these resources in the future, delete the resource group by using the following command:
```azurecli
az group delete \
    --name $RESOURCE_GROUP \
    --yes --no-wait
```
