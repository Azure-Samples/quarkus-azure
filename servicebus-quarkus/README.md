# Deploy a Quarkus native image to Azure Container Apps that connects to Azure Service Bus with managed identity authentication

This guide demonstrates how to deploy a Quarkus native image on Azure Container Apps that connects to Azure Service Bus using managed identity authentication. It uses the `io.quarkiverse.azureservices:quarkus-azure-servicebus` extension that exposes `ServiceBusClientBuilder` and supports Microsoft Entra authentication in Quarkus native build. 

## Prerequisites

To successfully run this guide, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.9.8+
* Azure subscription
* Azure CLI 2.74.0+
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/majguo/quarkus-azure.git
cd quarkus-azure/servicebus-quarkus
git checkout 2025-06-26
```

## Running the Quarkus application in development mode

The latest version of the `quarkus-azure-servicebus` extension supports DevServices, you can run the Quarkus application in development mode without a real Azure Service Bus instance. The extension will automatically start a local instance of Azure Service Bus emulator for you.

```
# Create a configuration file for the emulator
mkdir -p src/main/resources
cat << EOF > src/main/resources/servicebus-config.json
{
  "UserConfig": {
    "Namespaces": [
      {
        "Name": "sbemulatorns",
        "Queues": [{
           "Name": "test-queue",
           "Properties": {
             "DeadLetteringOnMessageExpiration": false,
             "DefaultMessageTimeToLive": "PT1H",
             "DuplicateDetectionHistoryTimeWindow": "PT20S",
             "ForwardDeadLetteredMessagesTo": "",
             "ForwardTo": "",
             "LockDuration": "PT1M",
             "MaxDeliveryCount": 3,
             "RequiresDuplicateDetection": false,
             "RequiresSession": false
           }
         }],
        "Topics": []
      }
    ],
    "Logging": {
      "Type": "Console"
    }
  }
}
EOF

# The DevServices is enabled by default, but you need to accept the license agreement
export QUARKUS_AZURE_SERVICEBUS_DEVSERVICES_LICENSE_ACCEPTED=true

# Start the sample app in dev mode
mvn quarkus:dev

# Open a new terminal and run the following commands to test the sample running in dev mode
curl http://localhost:8080/quarkus-azure-servicebus/messages -X POST -d '{"message": "Hello Azure Service Bus!"}' -H "Content-Type: application/json"
curl http://localhost:8080/quarkus-azure-servicebus/messages

# Switch back to the original terminal and stop the Quarkus application, and you can clean up the emulator config file
rm -rf src/main/resources
```

The following sections guide you through deploying the Quarkus application to Azure Container Apps with a real Azure Service Bus instance, and using a user-assigned managed identity to authenticate to Azure Service Bus.

## Preparing the Azure services

Sign in to your Azure account using the Azure CLI.

```
az login
```

Create a resource group for hosting the Azure services.

```
# Replace <your-unique-prefix> with your own value
UNIQUE_PREFIX=<your-unique-prefix>

RESOURCE_GROUP_NAME=${UNIQUE_PREFIX}rg
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location westus
```

Create an Azure Service Bus namespace and a queue.

```
SERVICEBUS_NAMESPACE=${UNIQUE_PREFIX}servicebus
az servicebus namespace create \
    --name ${SERVICEBUS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME}

az servicebus queue create \
    --name test-queue \
    --namespace-name ${SERVICEBUS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME}
```

The `quarkus-azure-servicebus` extension supports Microsoft Entra authentication using [`DefaultAzureCredential`](https://learn.microsoft.com/azure/developer/java/sdk/authentication/azure-hosted-apps#defaultazurecredential). In this guide, you use a user-assigned managed identity to authenticate to Azure Service Bus for your Quarkus application.

Create a user-assigned managed identity.

```
USER_ASSIGNED_IDENTITY_NAME=${UNIQUE_PREFIX}uami
az identity create \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${USER_ASSIGNED_IDENTITY_NAME}
```

Assign the `Azure Service Bus Data Owner` role to the user-assigned managed identity for the Azure Service Bus namespace.

```
USER_ASSIGNED_IDENTITY_OBJECT_ID=$(az identity show \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --query 'principalId' \
    --output tsv)

SERVICEBUS_RESOURCE_ID=$(az servicebus namespace show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $SERVICEBUS_NAMESPACE \
    --query 'id' \
    --output tsv)

az role assignment create \
    --role "Azure Service Bus Data Owner" \
    --assignee ${USER_ASSIGNED_IDENTITY_OBJECT_ID} \
    --scope $SERVICEBUS_RESOURCE_ID
```

Create an Azure Container Registry (ACR) to host the Quarkus native image.

```
ACR_NAME=${UNIQUE_PREFIX}acr
az acr create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $ACR_NAME \
    --sku Basic
```

Create an Azure Container Apps environment.

```
ACA_ENV=${UNIQUE_PREFIX}acaenv
az containerapp env create \
    --resource-group $RESOURCE_GROUP_NAME \
    --location westus \
    --name $ACA_ENV
```

## Containerizing the Quarkus application

Build the Quarkus native image, containerize it, and push the Docker image to the Azure Container Registry (ACR).

```
ACR_LOGIN_SERVER=$(az acr show \
    --name $ACR_NAME \
    --query 'loginServer' \
    --output tsv)
IMAGE_NAME=servicebus-sample
IMAGE_TAG=${ACR_LOGIN_SERVER}/${IMAGE_NAME}:1.0

mvn clean package -Dnative -Dquarkus.native.container-build -Dquarkus.container-image.build=true -Dquarkus.container-image.image=${IMAGE_TAG}

az acr login --name $ACR_NAME
docker push ${IMAGE_TAG}
```

## Deploying the Quarkus application to Azure Container Apps

Deploy the containerized Quarkus app native image to Azure Container Apps, and assign it the user-assigned managed identity created earlier for authenticating to Azure Service Bus.

```
USER_ASSIGNED_IDENTITY_ID=$(az identity show \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --query 'id' \
    --output tsv)
USER_ASSIGNED_IDENTITY_CLIENT_ID=$(az identity show \
    --name "${USER_ASSIGNED_IDENTITY_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --query 'clientId' \
    --output tsv)

ACA_NAME=${UNIQUE_PREFIX}aca
az containerapp create \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${ACA_NAME} \
    --environment ${ACA_ENV} \
    --image ${IMAGE_TAG} \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-identity system \
    --user-assigned ${USER_ASSIGNED_IDENTITY_ID} \
    --env-vars \
        QUARKUS_AZURE_SERVICEBUS_NAMESPACE=${SERVICEBUS_NAMESPACE} \
        AZURE_CLIENT_ID=${USER_ASSIGNED_IDENTITY_CLIENT_ID} \
    --target-port 8080 \
    --ingress 'external' \
    --min-replicas 1
```

The `QUARKUS_AZURE_SERVICEBUS_NAMESPACE` environment variable is used to configure the Azure Service Bus namespace in the Quarkus application. The `AZURE_CLIENT_ID` environment variable is used by the `DefaultAzureCredential` to authenticate the application with the user-assigned managed identity.

## Testing the Quarkus application

Retrieve a fully qualified url to access the app.

```
APP_URL=https://$(az containerapp show \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${ACA_NAME} \
    --query properties.configuration.ingress.fqdn \
    --output tsv)
```

Run the following commands to test the sample running on the Container Apps instance:

```
# Send/process message "Hello Azure Service Bus!" to/from Azure Service Bus. You should see {"message":"Message sent successfully","status":"success"} in the response.
curl ${APP_URL}/quarkus-azure-servicebus/messages -X POST -d '{"message": "Hello Azure Service Bus!"}' -H "Content-Type: application/json"

# Retrieve the cached message that is received from Azure Service Bus. You should see {"messages":["Hello Azure Service Bus!"],"count":1,"status":"success"} in the response.
curl ${APP_URL}/quarkus-azure-servicebus/messages
```

You should see the expected outputs described in the comments. If you don't see them, the app could still be starting up. Wait for a while and try again.

If you open the Azure Portal > your Azure Container Apps > Monitoring > Log stream, you should see the similar logs as below:

```
2025-06-05T05:49:48.9693646Z stdout F 2025-06-05 05:49:48,969 INFO  [com.azu.ide.ManagedIdentityCredential] (azure-sdk-global-thread-0) Azure Identity => Managed Identity environment: Managed Identity
2025-06-05T05:49:48.9694601Z stdout F 2025-06-05 05:49:48,969 INFO  [com.azu.ide.ChainedTokenCredential] (azure-sdk-global-thread-0) Azure Identity => Attempted credential ManagedIdentityCredential returns a token
```

This indicates that the Quarkus application is successfully using the user-assigned managed identity to authenticate to Azure Service Bus.

You can also open the Azure Portal > your Azure Service Bus namespace, you should see the number of incoming and outgoing messages in the **Messages** chart is 1 separately, which indicates that the message is sent and received successfully. Notice that it may take a few minutes for the chart to update. Wait for a while if you don't see the expected numbers, and try again.

## Cleaning up Azure resources

Run the following command to clean up the Azure resources when you are done with the guide.

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
