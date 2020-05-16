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
    <version>1.0.0</version>
</dependency>
```

Provide a SLF4J logger backend implementation:

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.30</version>
</dependency>
```

Note: You can also use a different backend implementation. For more information see https://www.slf4j.org/manual.html

## Usage

### Create a SMB connection to the server

Via anonymous user:

```java
AuthenticationContext authenticationContext = AuthenticationContext.anonymous();
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

Via username and password:

```java
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "");
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

Via username/domain and password:

```java
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    // Do your work
}
```

In case you need a custom SMB configuration, you can also pass a smbj `SmbConfig` object to the SMB connection:

```java
SmbConfig smbConfig = SmbConfig.builder().withSoTimeout(3000).build();
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext, smbConfig)) {
    // Do your work
}
```

Notes:

* The created connection is not thread safe. As soon you are using several threads, create a new connection for each thread.
* You don't have to use the try-with-resources statement that automatically closes the connection. You can also close the connection manually.

### Access the root share and list all directories and files

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory rootDirectory = new SmbDirectory(smbConnection);
    for (SmbDirectory smbDirectory : rootDirectory.getDirectories()) {
        System.out.println(smbDirectory.getName());
    }
    for (SmbFile smbFile : rootDirectory.getFiles()) {
        System.out.println(smbFile.getName());
    }
}
```

### Access the root share and list all specific files and directories by regex pattern or predicate

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory rootDirectory = new SmbDirectory(smbConnection);

    // List all directories and files of the current directory (Unfiltered, non-recursive)
    List<SmbItem> smbItems1 = rootDirectory.listFiles();

    // List by string pattern (java.util.regex.Pattern), non-recursive tree search
    List<SmbItem> smbItems2 = rootDirectory.listFiles("MyFile.txt", false);

    // List by predicate, recursive tree search
    List<SmbItem> smbItems3 = rootDirectory.listFiles(smbItem -> smbItem.getName().contains("MyFile.txt"), true);
}
```

Note:

* The given entry path is fully accessed and filtered/searched on the client side (There is no support for server side filtering). This can lead to performance issue with large file trees.

### Access a directory/file and get more information

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    System.out.println("Is existing: " + smbFile.isExisting());
    System.out.println("Is directory: " + smbFile.isDirectory());
    System.out.println("Is file: " + smbFile.isFile());
    System.out.println("Is hidden: " + smbFile.isHidden());
    System.out.println("Is root path: " + smbFile.isRootPath());
    System.out.println("Name: " + smbFile.getName());
    System.out.println("Server name: " + smbFile.getServerName());
    System.out.println("Share name: " + smbFile.getShareName());
    System.out.println("Path: " + smbFile.getPath());
    System.out.println("SMB path: " + smbFile.getSmbPath());
    System.out.println("Creation time: " + smbFile.getCreationTime());
    System.out.println("Last access time: " + smbFile.getLastAccessTime());
    System.out.println("Last write time: " + smbFile.getLastWriteTime());
    System.out.println("Change time: " + smbFile.getChangeTime());
    System.out.println("File size: " + smbFile.getFileSize());
}
```

### Access a subdirectory or file in a subdirectory

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory/Subdirectory/Subdirectory");
    SmbFile smbFile = new SmbFile(smbConnection, "Directory/Subdirectory/File.txt");
}
```

### Access a directory/file and traverse the tree

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    System.out.println("Is root path: " + smbFile.isRootPath());
    SmbDirectory parentDirectory = smbFile.getParentPath();
    SmbDirectory rootDirectory = smbFile.getRootPath();
}
```

### Create directories/files

Create a file:

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    smbFile.createFile();
}
```

Create a directory:

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory");
    smbDirectory.createDirectory();
}
```

Create a directory in the current directory (Same as `SmbDirectory.createDirectory` for path `Directory/Subdirectory`):

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory");
    SmbDirectory newSmbDirectory = smbDirectory.createDirectoryInCurrentDirectory("Subdirectory");
}
```

Create a file in the current directory (Same as `SmbFile.createFile` for path `Directory/File.txt`):

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory");
    SmbFile newSmbFile = smbDirectory.createFileInCurrentDirectory("File.txt");
}
```

### Copy a file on the same server share

Copy a file on the same server share (In case they are different shares, use the download/upload bellow):

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile sourceFile = new SmbFile(smbConnection, "Screenshot1.png");
    SmbFile destinationFile = new SmbFile(smbConnection, "Screenshot2.png");
    sourceFile.copyFileViaServerSideCopy(destinationFile);
}
```

### Rename a file or directory

Rename a file or directory with the possibility to replace it's existing pendant (The renamed file/directory will be returned as new object):

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory");
    SmbDirectory renamedSmbDirectory = smbDirectory.renameTo("DirectoryRenamed.txt", false);

    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    SmbFile renamedSmbFile = smbFile.renameTo("FileRenamed.txt", true); // Replace an existing file
}
```

Notes:

* You can only rename and replace a path of the same type (File/Directory). For example it's not possible to rename and replace a file to an existing directory and vica versa

### Ensure a directory exist

Ensure that a directory exists (Autocreation if required):

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbDirectory smbDirectory = new SmbDirectory(smbConnection, "Directory");
    smbDirectory.ensureExists();
}
```

### Upload and download a file

Upload from an input stream and overwrite/append to the file:

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    InputStream inputStream = ... // Your input stream

    // Overwrite the file
    OutputStream outputStream = smbFile.getOutputStream();

    // Append to the file
    OutputStream outputStream = smbFile.getOutputStream(true);

    IOUtils.copy(inputStream, outputStream);
    inputStream.close();
    outputStream.close();
}
```

Download to an output stream from the file:

```java
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext)) {
    SmbFile smbFile = new SmbFile(smbConnection, "File.txt");
    InputStream inputStream = smbFile.getInputStream();
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
