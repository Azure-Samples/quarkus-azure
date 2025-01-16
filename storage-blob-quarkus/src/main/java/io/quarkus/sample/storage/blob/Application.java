package io.quarkus.sample.storage.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import jakarta.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@QuarkusMain
public class Application implements QuarkusApplication {

    @Inject
    BlobServiceClient blobServiceClient;

    @Override
    public int run(String... args) {

        // Create a unique name for the container
        String containerName = "quickstartblobs" + java.util.UUID.randomUUID();

        // Create the container and return a container client object
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainer(containerName);

        // Create the ./data/ directory and a file for uploading and downloading
        String localPath = "./data/";
        new File(localPath).mkdirs();
        String fileName = "quickstart" + java.util.UUID.randomUUID() + ".txt";

        // Get a reference to a blob
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

        // Write text to the file
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(localPath + fileName, true);
            writer.write("Hello, World!");
            writer.close();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }

        System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

        // Upload the blob
        blobClient.uploadFromFile(localPath + fileName);

        System.out.println("\nListing blobs...");

        // List the blob(s) in the container.
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            System.out.println("\t" + blobItem.getName());
        }

        // Download the blob to a local file

        // Append the string "DOWNLOAD" before the .txt extension for comparison purposes
        String downloadFileName = fileName.replace(".txt", "DOWNLOAD.txt");

        System.out.println("\nDownloading blob to\n\t " + localPath + downloadFileName);

        blobClient.downloadToFile(localPath + downloadFileName);

        File downloadedFile = new File(localPath + downloadFileName);
        File localFile = new File(localPath + fileName);

        // Clean up resources
        System.out.println("\nPress the Enter key to begin clean up");
        System.console().readLine();

        System.out.println("Deleting blob container...");
        blobContainerClient.delete();

        System.out.println("Deleting the local source and downloaded files...");
        localFile.delete();
        downloadedFile.delete();

        System.out.println("Done");

        return 0;
    }
}
