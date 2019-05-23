package com.ekocaman.azure;

import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;
import io.reactivex.Single;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.Locale;

public class AzureBlobService {
    private static final Logger LOGGER = LogManager.getLogger();

    private String accountName;
    private String accountKey;
    private String containerName;

    public AzureBlobService(String accountName, String accountKey, String containerName) {
        this.accountName = accountName;
        this.accountKey = accountKey;
        this.containerName = containerName;
        LOGGER.traceEntry();
        LOGGER.debug("Account Name: {}, Container Name: {}", this.accountName, this.containerName);
    }

    public String getSASToken() throws InvalidKeyException {
        final SharedKeyCredentials credentials = new SharedKeyCredentials(accountName, accountKey);

        final AccountSASSignatureValues values = new AccountSASSignatureValues();
        values.withProtocol(SASProtocol.HTTPS_HTTP).withExpiryTime(OffsetDateTime.now().plusMinutes(1));

        final AccountSASPermission permission = new AccountSASPermission().withRead(true).withWrite(true);
        values.withPermissions(permission.toString());

        final AccountSASService service = new AccountSASService().withBlob(true);
        values.withServices(service.toString());

        final AccountSASResourceType resourceType = new AccountSASResourceType().withContainer(true).withObject(true);
        values.withResourceTypes(resourceType.toString());

        final SASQueryParameters params = values.generateSASQueryParameters(credentials);

        return params.encode();
    }

    public String getURLWithSASToken(String blobPath) throws InvalidKeyException {
        return String.format(Locale.ROOT, "https://%s.blob.core.windows.net/%s/%s%s", accountName, containerName, blobPath, getSASToken());
    }

    public URL uploadFromFile(String pathFileName, String blobPath) throws InvalidKeyException, IOException {
        LOGGER.traceEntry();
        LOGGER.debug("pathFileName: {}, blobPath: {}", pathFileName, blobPath);

        final SharedKeyCredentials credentials = new SharedKeyCredentials(accountName, accountKey);
        final ServiceURL serviceURL = new ServiceURL(createBlobURL(accountName), StorageURL.createPipeline(credentials, new PipelineOptions()));

        final ContainerURL containerURL = serviceURL.createContainerURL(containerName);
        final BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobPath);

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(pathFileName))) {
            final BlobHTTPHeaders headers = new BlobHTTPHeaders();

            final TransferManagerUploadToBlockBlobOptions options = new TransferManagerUploadToBlockBlobOptions(
                    null,
                    headers,
                    new Metadata(),
                    new BlobAccessConditions(),
                    1
            );

            final Single<CommonRestResponse> commonRestResponseSingle = TransferManager.uploadFileToBlockBlob(fileChannel, blobURL, 8 * 1024 * 1024, null, options);
            final CommonRestResponse commonRestResponse = commonRestResponseSingle.blockingGet();

            if (commonRestResponse.statusCode() != 201) {
                LOGGER.warn("Could not upload the file");
            }

            LOGGER.traceExit();
        }

        return blobURL.toURL();
    }

    public URL uploadFromFileWithSASToken(String pathFileName, String blobPath, String sasToken) throws InvalidKeyException, IOException {
        LOGGER.traceEntry();
        LOGGER.debug("pathFileName: {}, blobPath: {}, SASToken : {}", pathFileName, blobPath, sasToken);

        final ServiceURL serviceURL = new ServiceURL(createBlobURLWithToken(accountName, sasToken), StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

        final ContainerURL containerURL = serviceURL.createContainerURL(containerName);
        final BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobPath);

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(pathFileName))) {
            final BlobHTTPHeaders headers = new BlobHTTPHeaders();

            final TransferManagerUploadToBlockBlobOptions options = new TransferManagerUploadToBlockBlobOptions(
                    null,
                    headers,
                    new Metadata(),
                    new BlobAccessConditions(),
                    1
            );

            final Single<CommonRestResponse> commonRestResponseSingle = TransferManager.uploadFileToBlockBlob(fileChannel, blobURL, 8 * 1024 * 1024, null, options);
            final CommonRestResponse commonRestResponse = commonRestResponseSingle.blockingGet();

            if (commonRestResponse.statusCode() != 201) {
                LOGGER.warn("Could not upload the file");
            }

            LOGGER.traceExit();
        }

        return blobURL.toURL();
    }

    private URL createBlobURLWithToken(String accountName, String sasToken) throws MalformedURLException {
        return new URL("https://" + accountName + ".blob.core.windows.net" + sasToken);
    }

    private URL createBlobURL(String accountName) throws MalformedURLException {
        return new URL("https://" + accountName + ".blob.core.windows.net");
    }
}
