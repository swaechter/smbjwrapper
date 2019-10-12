# Wrapper for smbj

## Introduction

This project provides an object & hierarchy based wrapper API for the smbj project (https://github.com/hierynomus/smbj). It serves as a replacement for the aging and insecure JCIFS project.

## Foreword
In the open source world it is hard to get to know your users and their use cases & needs. In case you are using this library and enjoy it (or had problems), feel free to send me a mail (waechter.simon@gmail.com) with your experience/thoughts. Thanks!

## Installation

Import the dependency:

```xml
<dependency>
    <groupId>ch.swaechter</groupId>
    <artifactId>smbjwrapper</artifactId>
    <version>0.0.8</version>
</dependency>
```

Provide a SLF4J logger backend implementation:

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.25</version>
</dependency>
```

Note: You can also use a different backend implementation. For more information see https://www.slf4j.org/manual.html

## Usage

### Create a shared connection to the server

Via anonymous user:

```java
AuthenticationContext authenticationContext = AuthenticationContext.anonymous();
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

Via username and password:

```java
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "");
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

Via username/domain and password:

```java
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

In case you need a custom SMB configuration, you can also pass a smbj `SmbConfig` object to the shared connection:

```java
SmbConfig smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext, smbConfig)) {
    // Do your work
}
```

Notes:

* The created connection is not thread safe. As soon you are using several threads, create a new connection for each thread.
* You don't have to use the try-with-resources statement that automatically closes the connection. You can also close the connection manually.

### Access the root share and list all directories and files

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory rootDirectory = new SharedDirectory(sharedConnection);
    for (SharedDirectory sharedDirectory : rootDirectory.getDirectories()) {
        System.out.println(sharedDirectory.getName());
    }
    for (SharedFile sharedFile : rootDirectory.getFiles()) {
        System.out.println(sharedFile.getName());
    }
}
```

### Access the root share and list all specific files and directories by regex pattern or predicate

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory rootDirectory = new SharedDirectory(sharedConnection);

    // List all directories and files of the current directory (Unfiltered, non-recursive)
    List<SharedItem> sharedItems1 = rootDirectory.listFiles();

    // List by string pattern (java.util.regex.Pattern), non-recursive tree search
    List<SharedItem> sharedItems2 = rootDirectory.listFiles("MyFile.txt", false);

    // List by predicate, recursive tree search
    List<SharedItem> sharedItems3 = rootDirectory.listFiles(sharedItem -> sharedItem.getName().contains("MyFile.txt"), true);
}
```

Note: The given entry path is fully accessed and filtered/searched on the client side (There is no support for server side filtering). This can lead to performance issue with large file trees.

### Access a directory/file and get more information

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    System.out.println("Is existing: " + sharedFile.isExisting());
    System.out.println("Is directory: " + sharedFile.isDirectory());
    System.out.println("Is file: " + sharedFile.isFile());
    System.out.println("Is hidden: " + sharedFile.isHidden());
    System.out.println("Is root path: " + sharedFile.isRootPath());
    System.out.println("Name: " + sharedFile.getName());
    System.out.println("Server name: " + sharedFile.getServerName());
    System.out.println("Share name: " + sharedFile.getShareName());
    System.out.println("Path: " + sharedFile.getPath());
    System.out.println("SMB path: " + sharedFile.getSmbPath());
    System.out.println("Creation time: " + sharedFile.getCreationTime());
    System.out.println("Last access time: " + sharedFile.getLastAccessTime());
    System.out.println("Last write time: " + sharedFile.getLastWriteTime());
    System.out.println("Change time: " + sharedFile.getChangeTime());
    System.out.println("File size: " + sharedFile.getFileSize());
}
```

### Access a subdirectory or file in a subdirectory

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory/Subdirectory/Subdirectory");
    SharedFile sharedFile = new SharedFile(sharedConnection, "Directory/Subdirectory/File.txt");
}
```

### Access a directory/file and traverse the tree

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    System.out.println("Is root path: " + sharedFile.isRootPath());
    SharedDirectory parentDirectory = sharedFile.getParentPath();
    SharedDirectory rootDirectory = sharedFile.getRootPath();
}
```

### Create directories/files

Create a file:

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    sharedFile.createFile();
}
```

Create a directory:

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory");
    sharedDirectory.createDirectory();
}
```

Create a directory in the current directory (Same as `SharedDirectory.createDirectory` for path `Directory/Subdirectory`):

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory");
    SharedDirectory newSharedDirectory = sharedDirectory.createDirectoryInCurrentDirectory("Subdirectory");
}
```

Create a file in the current directory (Same as `SharedFile.createFile` for path `Directory/File.txt`):

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory");
    SharedFile newSharedFile = sharedDirectory.createFileInCurrentDirectory("File.txt");
}
```

### Copy a file on the same server share

Copy a file on the same server share (In case they are different shares, use the download/upload bellow):

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sourceFile = new SharedFile(sharedConnection, "Screenshot1.png");
    SharedFile destinationFile = new SharedFile(sharedConnection, "Screenshot2.png");
    sourceFile.copyFileViaServerSideCopy(destinationFile);
}
```

### Rename a file or directory

Rename a file or directory with the possibility to replace it's existing pendant (The renamed file/directory will be returned as new object):

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory");
    SharedDirectory renamedSharedDirectory = sharedDirectory.renameTo("DirectoryRenamed.txt", false);

    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    SharedFile renamedSharedFile = sharedFile.renameTo("FileRenamed.txt", true); // Replace an existing file
}
```

Notes:

* You can only rename and replace a path of the same type (File/Directory). For example it's not possible to rename and replace a file to an existing directory and vica versa

### Ensure a directory exist

Ensure that a directory exists (Autocreation if required):

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedDirectory sharedDirectory = new SharedDirectory(sharedConnection, "Directory");
    sharedDirectory.ensureExists();
}
```

### Upload and download a file

Upload from an input stream and overwrite/append to the file:

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    InputStream inputStream = ... // Your input stream

    // Overwrite the file
    OutputStream outputStream = sharedFile.getOutputStream();

    // Append to the file
    OutputStream outputStream = sharedFile.getOutputStream(true);

    IOUtils.copy(inputStream, outputStream);
    inputStream.close();
    outputStream.close();
}
```

Download to an output stream from the file:

```java
try (SharedConnection sharedConnection = new SharedConnection("127.0.0.1", "Share", authenticationContext)) {
    SharedFile sharedFile = new SharedFile(sharedConnection, "File.txt");
    InputStream inputStream = sharedFile.getInputStream();
    OutputStream outputStream = ... // Your output stream

    IOUtils.copy(inputStream, outputStream);
    inputStream.close();
    outputStream.close();
}
```

## License

This project is licensed under the MIT license. For more information see the `LICENSE.md` file.

## Questions or problems

For questions and problems, please open an issue. Please also take a look at the open issues.
