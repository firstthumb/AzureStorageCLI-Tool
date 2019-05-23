package com.ekocaman.azure;

import picocli.CommandLine;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Upload files to Azure Blob Storage", name = "az-upload-cli", mixinStandardHelpOptions = true, version = "1.0")
class AzureStorageCLI implements Callable<Void> {
    @CommandLine.Parameters(index = "0", description = "generateSAS, generateURL, upload, ")
    private String command;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The file to upload Azure Storage")
    private File file;

    @CommandLine.Option(names = {"-n", "--account-name"}, description = "Account Name of Azure Storage Service")
    private String accountName;

    @CommandLine.Option(names = {"-k", "--account-key"}, description = "Account Key of Azure Storage Service")
    private String accountKey;

    @CommandLine.Option(names = {"-c", "--container"}, description = "Container Name")
    private String containerName;

    @CommandLine.Option(names = {"-p", "--path"}, description = "Blob File Path")
    private String blobFile;

    @CommandLine.Option(names = {"-t", "--sas-token"}, description = "SAS Token")
    private String token;

    public static void main(String[] args) {
        CommandLine.call(new AzureStorageCLI(), args);
    }

    @Override
    public Void call() throws Exception {
        if (command.equals("uploadWithSAS")) {
            if (accountName == null || containerName == null) {
                System.out.println("You have to give AccountName and ContainerName as parameter");
                return null;
            }
        } else {
            if (accountName == null || accountKey == null || containerName == null) {
                System.out.println("You have to give AccountName, AccountKey and ContainerName as parameter");
                return null;
            }
        }

        final AzureBlobService azureBlobService = new AzureBlobService(accountName, accountKey, containerName);

        switch (command) {
            case "generateSAS":
                System.out.println("Generating SAS Token");

                final String sasToken = azureBlobService.getSASToken();
                System.out.println(String.format("Token : %s", sasToken));
                break;
            case "generateURL":
                if (blobFile == null) {
                    System.out.println("Blob File Path parameter is missing");
                    return null;
                }

                System.out.println("Generating private URL");

                final String url = azureBlobService.getURLWithSASToken(blobFile);
                System.out.println(String.format("URL : %s", url));
                break;
            case "upload":
                System.out.println("Uploading file");

                final URL fileUrl = azureBlobService.uploadFromFile(file.getAbsolutePath(), file.getName());
                System.out.println(String.format("Uploaded. URL : %s", fileUrl.toString()));
                break;

            case "uploadWithSAS":
                if (file == null || token == null) {
                    System.out.println("File or SAS Token is missing");
                    return null;
                }

                System.out.println("Uploading file with SAS Token");

                final URL fileUrlWithSASToken = azureBlobService.uploadFromFileWithSASToken(file.getAbsolutePath(), file.getName(), token);
                System.out.println(String.format("Uploaded. URL : %s", fileUrlWithSASToken.toString()));
                break;
            default:
                System.out.println("Use correct command");
                CommandLine.usage(this, System.out);
        }

        return null;
    }
}