# Wrapper for smbj

## Introduction

This project provides an object & hierarchy based wrapper API for the smbj project (https://github.com/hierynomus/smbj). It serves as a replacement for the aging and insecure JCIFS project.

## Foreword

In the open source world it is difficult to get to know your users and their use cases & needs. In case you are using this library and enjoy it, feel free to send me an email (waechter.simon@gmail.com). For more important and time critical things like issues/bugs or feature enhancements, please use the issue tracker (For me it's not possible to keep up with all mails in a timely manner). Thanks a lot!

## Installation

Import the dependency:

```xml
<dependency>
    <groupId>ch.swaechter</groupId>
    <artifactId>smbjwrapper</artifactId>
    <version>1.2.0</version>
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

Notes:

* You can also use a different backend implementation. For more information see https://www.slf4j.org/manual.html

## Usage

### Create an SMB connection to the server via server/share name

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

The creation of the SMB connection takes some time (Maybe you are even initializing several connections in parallel). It is possible to enable the delayed initialization (disabled by default/eagerly initialized). Then, the first API call that requires a connection will establish the connection:

```java
SmbConfig smbConfig = SmbConfig.builder().build();
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
try (SmbConnection smbConnection = new SmbConnection("127.0.0.1", "Share", authenticationContext, smbConfig, true)) {
    // Do your work, first API call that requires the connection will establish it internally
}
```

For long living SMB connections, there is also the possibility to check if the connection is alive and to refresh the internal connection (E.g. you write some log entries every hour and won't/can't recreate the connection):

```java
boolean isConnectionAlive = smbConnection.isConnectionAlive();
if(!isConnectionAlive) {
    smbConnection.ensureConnectionIsAlive();
}
```

Notes:

* The created connection is not thread safe. As soon you are using several threads, create a new connection for each thread.
* You don't have to use the try-with-resources statement that automatically closes the connection. You can also close the connection manually.

### Create an SMB connection to the server via UNC path

In many cases you won't receive the server/share/path separated, but as one combined UNC path (Example: `\\127.0.0.1\Share\Directory\File`). It is possible to parse this UNC path and return an SMB item:

```java
AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
SmbItem smbItem = SmbUtils.buildSmbItemFromUncPath(authenticationContext, CreationStrategy.EXCEPTION, "\\\\127.0.0.1\\Share\\Directory\\File");
try (SmbConnection smbConnection = smbItem.getSmbConnection()) {
    if (smbItem instanceof SmbDirectory) {
        // It's a directory
        SmbDirectory smbDirectory = (SmbDirectory) smbItem;
    } else if (smbItem instanceof SmbFile) {
        // It's a file
        SmbFile smbFile = (SmbFile) smbItem;
    }
}
```

One important parameter is the `CreationStrategy`: If the path exists, it is possible to detect the type of it (File/directory, other), but hot to handle a situation where the path doesn't exist (E.g. system gets the UNC path to create the directory)? Thus, the developer has to specify the path strategy if the path doesn't exist:

* DIRECTORY: Return a `SmbDirectory` if the path does not exist
* FILE: Return a `SmbFile` if the path does not exist
* EXCEPTION: Throw an `IOException` if the path does not exist

Important: The creation strategy is only used when the path does not exist! If the path exists, the type will be determined at runtime.

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
    List<SmbItem> smbItems1 = rootDirectory.listItems();

    // List by string pattern (java.util.regex.Pattern), non-recursive tree search
    List<SmbItem> smbItems2 = rootDirectory.listItems("MyFile.txt", false);

    // List by predicate, recursive tree search
    List<SmbItem> smbItems3 = rootDirectory.listItems(smbItem -> smbItem.getName().contains("MyFile.txt"), true);
}
```

Notes:

* The given entry path is fully accessed and filtered/searched on the client side (There is no support for server side filtering). This can lead to performance issue with large file/directory trees.

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

Copy a file on the same server share (In case they are different shares, use the download/upload bellow). For more information check out the Samba documentation (https://wiki.samba.org/index.php/Server-Side_Copy):

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

* You can only rename and replace a path of the same type (File/Directory). For example it's not possible to rename and replace a file to an existing directory and vice versa

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
