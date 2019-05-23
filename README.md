# Azure Storage CLI

Azure Storage CLI is tool for generating Shared Access Signatures and uploading file to Azure Cloud Storage

## Installation

Use the package manager [maven](https://maven.apache.org/) to build the project.

```bash
mvn clean install
```

## Usage

```bash

# prints SAS Token
java com.ekocaman.azure.AzureStorageCLI generateSAS -n {ACCOUNT_NAME} -k {ACCOUNT_KEY} -c {CONTAINER_NAME} -p {REMOTE_FILE_PATH}

# uploads local file to Azure Storage with SAS Token
java com.ekocaman.azure.AzureStorageCLI uploadWithSAS -n {ACCOUNT_NAME} -t {SAS_TOKEN} -c {CONTAINER_NAME} -p {REMOTE_FILE_PATH} -f {LOCAL_FILE_PATH}

```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)