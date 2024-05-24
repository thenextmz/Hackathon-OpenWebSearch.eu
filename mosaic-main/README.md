# MOSAIC

The **M**odular **O**pen Web **S**earch **A**pplication based on **I**ndex Fra**c**tions is an application created to access the Open Web Index and send queries to it through a REST API based on a modular and configurable system depending on purpose and needs. It returns search results from web documents that are indexed. The code for the REST API can be found in `search-service`.

Another library used in this package is `lucene-ciff` [Created by @gijshendriksen] which imports a CIFF index in an Apache Lucene index. Find out more about the working of the standalone application [here](https://github.com/informagi/lucene-ciff). This component allows users to insert a CIFF index in the `lucene` folder and import it into a Lucene index.

The directory `resources` contains necessary index partition data that is used by `lucene-ciff` (CIFF index files) and the `search-service` (Apache Parquet file(s)). This application already provides two indexes and associated metadata:
- `demo-simplewiki`: A demo index containing abstracts of simple wikipedia pages (241,839 documents).
- `demo-unis-graz`: A demo index containing documents related to the University of Graz and Graz University of Technology (1,935 documents).

If you want to use an additional or own index, include the CIFF index and the associated Parquet file(s) in the directory `resources/<YOUR_INDEX_NAME>/`. If available, include the Lucene index in the directory `lucene/<YOUR_INDEX_NAME>/`. Otherwise you can simply use `lucene-ciff` or the provided [scripts](#scripts) to import the CIFF index in a Lucene index. The directory name, that contains the Parquet file(s), must match the name of the associated Lucene index in the `lucene` directory. If it differs, the code has to be adapted.

The `front-end` directory offers a simple application showcasing how to utilize the search service. If you want to run the front-end locally, it is sufficient to run the search service and put the front-end application into a directory of your local web server or run the front-end application using Docker. 

## Usage
After cloning this repository, you can use one of the possibilities described below to build and start MOSAIC. Currently, MOSAIC supports a handful of [CLI options](#cli-options).

### Option 1: Use scripts
Prerequisites:
- Install Java (JDK 17+)

We provide several [scripts](#scripts) with which you can build and launch the application. To first build and then start the application, execute the following commands:
```shell
# change the directory
cd scripts
# import indexes, clean the project and create a packaged JAR
./build.sh
# run the executable
./start.sh "[OPTION]" [API_PORT]
```

If you want to build and run the application on Windows, you can use the batch files instead of the bash scripts.

### Option 2: Build and run executable manually
Prerequisites:
- Install Java (JDK 17+)
- Install Maven (3.8.2+)

To build a JAR and run the Java executable you just need to execute the following commands:
```shell
# change the directory
cd search-service
# clean the project and compile the source code
mvn clean compile
# build the executable
mvn package
# run the executable
java [-Dquarkus.http.port=<API_PORT>] -jar core/target/service.jar [OPTION]
```

A concrete example for running the executable without a custom port number and without any options is:
```shell
java -jar core/target/service.jar
```
A concrete example for running the executable with a custom port number (e.g., 8009) is:
```shell
java -Dquarkus.http.port=8009 -jar core/target/service.jar
```

Setting the port number using `-Dquarkus.http.port=<YOUR_API_PORT>` is optional. By default, the search service runs on port `8008`. Note that this approach assumes that your Lucene index(es) is already located in the directory `lucene` and its associated metadata is located in the respective directory in `resources`. If you need to import your CIFF index to a Lucene index first, you can use the [importer script](#running-the-index-importer).

### Option 3: Dev Mode
Prerequisites:
- Install Java (JDK 17+)
- Install Maven (3.8.2+)

You can run the search service in dev mode that enables live coding using:
```shell
# change the directory
cd search-service
# run MOSAIC in dev mode
mvn quarkus:dev [-Dquarkus.http.port=<API_PORT>] [-Dquarkus:args="OPTION"]
```


### Option 4: Use Docker
Prerequisites:
- Install Docker

Another option to run the application is to build a Docker image and then launch the application in a Docker container by executing the following commands:
```shell
# create an image
docker build -t mosaic .
# start a container
docker run -p 8008:8008 mosaic -p <YOUR_API_PORT>
```

Note that it is optional to pass `<YOUR_API_PORT>` as this already has a default value in the Dockerfile. Be aware that `<YOUR_API_PORT>` indicates the port which is used inside the container. If you want to change the port on your host machine, you need to modify the binding in the command above. For more information, see [Docker reference](https://docs.docker.com/engine/reference/commandline/run/#publish).

A concrete example for starting the container without parameters is:
```shell
docker run -p 8008:8008 mosaic
```

A concrete example for starting the container with parameters is: 
```shell
docker run -p 8008:8008 mosaic -p 8008
```

As soon as you build the Docker image, the CIFF file for each corresponding directory in `resources` is automatically imported to Lucene indexes in the image so you do not have to import the CIFF files to Lucene indexes manually beforehand. 

## CLI Options
To enable flexible and simple utilisation and development of MOSAIC, a handful of CLI options when starting the search service are supported:
```
Options:
  -l, --lucene-dir-path <dir>    path of directory containing the Lucene index(es)
                                 (default = lucene directory of this repository)

  -p, --parquet-dir-path <dir>   path of directory containing the Parquet file(s)

                                 (default = resources directory of this repository)

  -i, --id-column <col>          column that contains the document identifiers
                                 (default = record_id)

  -n, --num-characters <num>     number of characters selected from the plain text column
                                 to be stored in the associated DB table column
                                 (if this option is not specified, the full plain text is imported)

  -d, --db-file-path <dir>       path of directory containing the database file (file is 
                                 created when starting MOSAIC for the first time)
                                 (default = /tmp/mosaic_db)
```

## Scripts

### Building the application
- Enter the scripts directory `cd scripts`.
- Import all CIFF files and build the application using the following command: 
  ```shell
  ./build.sh
  ```
- The result will be imported indexes that are stored in the directory `lucene` and a JAR file that is stored in `search-service/core/target/` as `service.jar`.

### Running the application
- Enter the scripts directory `cd scripts`.
- Run the application using the following command: 
  ```shell
  ./start.sh "[OPTION]" [API_PORT]
  ```
- All options and the `API_PORT` parameter are optional. For the purposes of this application, the `API_PORT` parameter is set to `8008` by default.
- If you do not need any options but want to use a custom `API_PORT`, use `""` before defining the port number.
- Example script call:
  ```shell
  ./start.sh "-c id" 8008
  ```
- The service will be running on port `8008` of your machine by default.

### Running the index importer
- Enter the scripts directory `cd scripts`.
- Run the importer using the following command: 
  ```shell
  ./import_index.sh <YOUR_CIFF_FILE_NAME> <YOUR_LUCENE_INDEX_NAME>
  ``` 
- Ensure that a CIFF file with `<YOUR_CIFF_FILE_NAME>` exists in the directory `resources/<YOUR_INDEX_NAME>/`.
- For the import, the default Lucene codec (i.e., the latest version) will be used. Find out more about the `SimpleText` CODEC [here](https://blog.mikemccandless.com/2010/10/lucenes-simpletext-codec.html) which you can use with the the [standalone application](https://github.com/informagi/lucene-ciff) of `lucene-ciff`.
- The imported index will be stored in `lucene` as a directory with `<YOUR_LUCENE_INDEX_NAME>` as directory name.
- IMPORTANT: Note that this script will not run if a directory with name `<YOUR_LUCENE_INDEX_NAME>` exists already in `lucene`.

## Run MOSAIC in Docker
As an alternative to cloning the repository, you can run MOSAIC using the Docker images available from the Gitlab Container registry. 

Use the following Docker command to run MOSAIC with the indexes available in this repository:
```bash
docker run \
    --rm \
    -p 8008:8008 \
    opencode.it4i.eu:5050/openwebsearcheu-public/mosaic
```

If you want to use other indexes, you can start the MOSAIC search service with the options `--lucene-dir-path` and `--parquet-dir-path`:
```bash
docker run \
    --rm \
    -p 8008:8008 \
    opencode.it4i.eu:5050/openwebsearcheu-public/mosaic/search-service \
    --lucene-dir-path /path/to/lucene-indexes/ \
    --parquet-dir-path /path/to/metadata-directories/
```

## Service Access

### Queries
Open a web browser or use tools like cURL or Postman to send an HTTP GET request to the endpoint `/search` or `/searchxml`. Depending on the modules that are enabled, different query parameters can be used. For further information, please check the specification for each [module](#modules). In general, there is no required query parameter, although in most cases `q` will be used for query terms.

Depending on the host and port where the application is running, the format of the GET request for the endpoint `/search` with a response in JSON format could be: 
```shell
http://localhost:8008/search?q=<query>
```

To get the response in OpenSearch XML format, use the endpoint `/searchxml`:
```shell
http://localhost:8008/searchxml?q=<query>
```

A concrete example for a simple GET request only specifying the query with a response in JSON format is:
```shell
http://localhost:8008/search?q=graz
```

The same GET request with a response in OpenSearch XML format is:
```shell
http://localhost:8008/searchxml?q=graz
```

Via the API, MOSAIC will return a response containing a list of search results where each result is composed of the fields of the enabled [modules](#modules). If no index name is passed as parameter, MOSAIC searches in all available indexes and returns a list of results for each index.

#### OpenSearch
MOSAIC implements the [OpenSearch protocol](https://github.com/dewitt/opensearch/blob/master/opensearch-1-1-draft-6.md) and provides an OpenSearch description document. The actual document is created at startup and is based on the [template](https://opencode.it4i.eu/openwebsearcheu-public/mosaic/-/blob/main/search-service/core/src/main/resources/META-INF/resources/opensearch-template.xml?ref_type=heads).

Using the endpoint `/searchxml` instead of `/search`, the search results are returned in XML format. The XML document structure is:
```xml
<feed xmlns="http://www.w3.org/2005/Atom" xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/">
  <title>MOSAIC Search: {searchTerms}</title>
  <description>Search results for "{searchTerms}" at MOSAIC Search Service</description>
  <author>
    <name>OpenWebSearch.eu</name>
  </author>
  <opensearch:totalResults>1121</opensearch:totalResults>
  <opensearch:startIndex>1</opensearch:startIndex>
  <opensearch:itemsPerPage>20</opensearch:itemsPerPage>
  <opensearch:Query role="request" searchTerms="{searchTerms}" startPage="1"/>
  <link rel="alternate" href="{baseUrl}/search?q={searchTerms}&pw=1&limit=20" type="application/json"/>
  <link rel="self" href="{baseUrl}/searchxml?q={searchTerms}&pw=1&limit=20" type="application/atom+xml"/>
  <link rel="next" href="{baseUrl}/searchxml?q={searchTerms}&pw=2&limit=20" type="application/atom+xml"/>
  <link rel="last" href="{baseUrl}/searchxml?q={searchTerms}&pw=56&limit=20" type="application/atom+xml"/>
  <link rel="search" type="application/opensearchdescription+xml" href="{baseUrl}/opensearch.xml"/>
  <item>
    ...
  </item>
  <item>
    ...
  </item>
  ...
</feed>
```

### Index Information
MOSAIC provides an additional endpoint `/index-info` that returns information of the present indexes in JSON format. The endpoint expects no query parameters. For each index, the  information includes the name of the index, the number of indexed documents and a list of languages which appear in this index:
```
{
  "results": [
    {
      "core-index": {
        "documentCount": 285392,
        "languages": [
          "deu",
          "eng",
          "est",
          "fra",
          "ltz",
          "pol",
          "unknown",
          "zho"
        ]
      }
    },
    ...
  ]
}
```

### Document Full Plain Text
The CLI option `-n <number>` allows to import only a part of the full plain text of each document into the database. This is particularly beneficial for larger indexes to reduce the creation time and file size of the database. In order to get the full plain text of a web document, MOSAIC provides the endpoint `/full-text`. The endpoint expects the parameter `id`. Additionaly, the parameter `column` can be used to specify the metadata column the passed web document ID should be matched (default: `record_id`).

Depending on the host and port where the application is running, the format of the GET request for the endpoint `/full-text` with a response in JSON format could be:
```
http://localhost:8008/full-text?id=<id>&column=<column>
```

A concrete example for a simple GET request to retrieve the full plain text of a document with the ID `0f02f96c-a2da-49c2-9e6b-95e17d95cbf1` is:
```
http://localhost:8008/full-text?id=0f02f96c-a2da-49c2-9e6b-95e17d95cbf1
```

## Modules

### Core
As the only required module, the Core module provides the possibility to search in one or multiple index partitions from the [Open Web Index](http://openwebindex.eu). It is the main architectural component and all other components and modules depend on the Core module.

##### Query Parameters

| Parameter | Value | Necessity | Description |
| ------ | ------ | ------ | ------ |
| `q` | string | Optional | Search term(s) to be searched for in the Lucene index. |
| `index` | string | Optional | Specifies the Lucene index to be searched in. The passed value must match the folder name of the Lucene index. If no index is specified, a separate search in all indexes that are present is performed. |
| `lang` | string | Optional | Restricts the search result to only consider pages in the specified language (e.g., `eng`). If no language is specified, the search results are language independent. |
| `ranking` | string | Optional | Specifies the order of the search result based on the number of words a page has. Can be either `asc` or `desc`. If no ranking is specified, the order of the search result yielded by Lucene’s similarity search is used. |
| `pw` | int | Optional | Defines the page number of the set of search results desired by the search client. If no page number is specified, `1` is used
| `limit` | int | Optional | Sets the maximum number of results to be returned. If no limit is specified, a maximum of `20` results are returned by default per page. |
| `fulltext` | boolean | Optional | Loads the full plain text dynamically from the Parquet file(s) to generate the text snippet if the query term(s) are not present in the plain text which is stored in the database. If not specified, the full text is not loaded dynamically.

##### Response (JSON)
```
{
  "id": "2f3232a3-c4f3-4ae6-990b-290dde685bc7",
  "url": "http://info.cern.ch/hypertext/WWW/TheProject.html",
  "title": "The World Wide Web Project",
  "textSnippet": "The WorldWideWeb (W3) is a wide-area hypermedia information retrieval initiative aiming to give universal access to a large universe of documents.",
  "language": "eng",
  "warcDate": 1705353588000000,
  "wordCount": 129
}
```

##### Response (XML)
```xml
<item>
  <title>The World Wide Web Project</title>
  <link>http://info.cern.ch/hypertext/WWW/TheProject.html</link>
  <description>The WorldWideWeb (W3) is a wide-area hypermedia information retrieval initiative aiming to give universal access to a large universe of documents.</description>
  <id>2f3232a3-c4f3-4ae6-990b-290dde685bc7</id>
  <language>eng</language>
  <warcDate>1705353588000000</warcDate>
  <wordCount>129</wordCount>
  <index>core-index</index>
</item>
...
```

### Geo
The Geo module extends the text-based search by using geographical information that is stored in the metadata. This enables the possibility to filter for specific areas in the world using bounding boxes.

##### Query Parameters

| Parameter | Value | Necessity | Description |
| ------ | ------ | ------ | ------ |
| `east` | float | Optional | Specifies the max. longitude. |
| `west` | float | Optional | Specifies the min. longitude. |
| `north` | float | Optional | Specifies the min. latitude. |
| `south` | float | Optional | Specifies the max. latitude. |
| `operator` | string | Optional | Specifies whether all locations (i.e., `and`) or at least one (i.e., `or`) location of the search result must be inside the bounding box. Default is `or`. |

##### Response (JSON)
```
"locations": [
  {
    "locationName": "Wien",
    "locationEntries": [
      {
        "latitude": 48.2082,
        "longitude": 16.37169,
        "alpha2CountryCode": "AT"
      },
      ...
    ]
  },
  ...
]
```

##### Response (XML)
```xml
<locations>
  <location>
    <locationName>Wien</locationName>
    <locationEntries>
      <locationEntry>
        <latitude>48.2082</latitude>
        <longitude>16.37169</longitude>
        <alpha2CountryCode>AT</alpha2CountryCode>
      </locationEntry>
      ...
    </locationEntries>
  </location>
  ...
</locations>
```

### Keywords
The Keywords module adds the possibility to filter by a keyword and use the extracted keywords in the response of each search result.

##### Query Parameters

| Parameter | Value | Necessity | Description |
| ------ | ------ | ------ | ------ |
| `keyword` | string | Optional | Limits the search results to documents that contain the provided keyword. |

##### Response (JSON)
```
"keywords": [
  ...,
  ...
]
```

##### Response (XML)
```xml
<keywords>
  <keyword>...</keyword>
  ...
</keywords>
```

## Adding a new Metadata Module
MOSAIC allows developers to simply add new modules by themselves. Since the framework is based on Maven modules, these are the steps to incorporate a new metadata module:

1. Create a new Maven module with `<MODULE_NAME>` as name (replace `<MODULE_NAME>` with the actual name of the module) and `search-service` as parent module. By default, the newly created Maven module should have the same folder structure as the existing modules.

2. Add a dependency for the `shared` module in the file `pom.xml` of the newly created module:
```xml
<dependencies>
    <dependency>
        <groupId>eu.ows.mosaic</groupId>
        <artifactId>shared</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

3. Add a dependency in the file `pom.xml` of the `core` module:
```xml
<dependency>
    <groupId>eu.ows.mosaic</groupId>
    <artifactId>MODULE_NAME</artifactId>
    <version>1.0-SNAPSHOT</version>
    <optional>true</optional>
</dependency>
```

4. If not done automatically, register your new module as such in the file `pom.xml` in the parent `search-service`:
```xml
<module>MODULE_NAME</module>
```

5. Create a new Java file in `search-service/<MODULE_NAME>/src/main/java/eu/ows/mosaic/` that contains a class which extends `MetadataModule`. For example, name this Java file and class `<MODULE_NAME>Metadata`.

6. Override methods in the newly created class as you like. Particulary, override `getMetadataColumns()` and `getFilterColumns()` which are responsible for retrieving additional metadata columns and defining metadata filter columns respectively. For more information about the methods, take a look at the [abstract class MetadataModule](https://opencode.it4i.eu/openwebsearcheu-public/mosaic/-/blob/main/search-service/shared/src/main/java/eu/ows/mosaic/MetadataModule.java?ref_type=heads).

7. Add an entry in `search-service/core/src/main/resources/config.json` in the `plugins` object to enable the module for MOSAIC.

## Additional Components
MOSAIC supports additional components to customize web search aspects. Developers can use these provided components without having to touch the code around the search process directly.

### Query
The Query component allows developers to further process and modify the query, e.g., with LLMs for query expansion.

### Analyzer
The Analyzer component enables developers to use an analyzer other than the `StandardAnalyzer` that is used by default. Developers can use existing analyzers, but they can also implement an analyzer on their own.

## About
### Authors
- Sebastian Gürtl, Graz University of Technology, Graz, Austria (sebastian.guertl@tugraz.at)
- Alexander Nussbaumer, Graz University of Technology, Graz, Austria (alexander.nussbaumer@tugraz.at)
- Rohit Kaushik, University of Waterloo, Ontario, Canada (rohit.kaushik@uwaterloo.ca)

### Contact
- Sebastian Gürtl (sebastian.guertl@tugraz.at)
- Alexander Nussbaumer (alexander.nussbaumer@tugraz.at)

## Acknowledgement
This software has received funding from the European Union's
Horizon Europe research and innovation programme under grant agreement No 101070014 (OpenWebSearch.EU, https://doi.org/10.3030/101070014).
