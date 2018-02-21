# Wrapper for smbj

## Introduction

This project provides an object & hierarchy based wrapper API for the smbj project (https://github.com/hierynomus/smbj). It serves as a replacement for the aging and insecure JCIFS project.

## Use Case

### Import the project via Maven

    <dependency>
        <groupId>ch.swaechter</groupId>
        <artifactId>smbjwrapper</artifactId>
        <version>0.0.1</version>
    </dependency>

### Create an authentication for the server

Via username and password:

    AuthenticationContext authenticationContext = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
    
Via anonymous user:

    AuthenticationContext authenticationContext = AuthenticationContext.anonymous();

### Access the root share and list all directories and files

    SharedDirectory rootDirectory = new SharedDirectory("127.0.0.1", "Share", authenticationContext);
    for (SharedDirectory sharedDirectory : rootDirectory.getDirectories()) {
        System.out.println(sharedDirectory.getName());
    }
    for (SharedFile sharedFile : rootDirectory.getFiles()) {
        System.out.println(sharedFile.getName());
    }

### Access a directory/file and get more information
    
    SharedFile sharedFile = new SharedFile(rootDirectory, "File.txt");
    System.out.println("Is existing: " + sharedFile.isExisting());
    System.out.println("Is directory: " + sharedFile.isDirectory());
    System.out.println("Is file: " + sharedFile.isFile());
    System.out.println("Is root path: " + sharedFile.isRootPath());
    System.out.println("Name: " + sharedFile.getName());
    System.out.println("Server name: " + sharedFile.getServerName());
    System.out.println("Share name: " + sharedFile.getShareName());
    System.out.println("Path: " + sharedFile.getPath());
    System.out.println("SMB path: " + sharedFile.getSmbPath());

### Access a subdirectory or file in a subdirectory

    SharedDirectory sharedDirectory = new SharedDirectory(rootDirectory, "Directory/Subdirectory/Subdirectory");
    SharedFile sharedFile = new SharedFile(rootDirectory, "Directory/Subdirectory/File.txt");

Please be aware, that the directory and file path of `sharedDirectory` and `sharedFile` are always absolute - the path of `rootDirectory` won't be inserted before (You can of course access the path of this object to create a hierarchy/tree).

### Access a directory/file and traverse the tree

    SharedFile sharedFile = new SharedFile(rootDirectory, "File.txt");
    System.out.println("Is root path: " + sharedFile.isRootPath());
    SharedDirectory parentDirectory = sharedFile.getParentPath();
    SharedDirectory rootDirectory = sharedFile.getRootPath();

### Create directories/files

Create a file:

    SharedFile sharedFile = new SharedFile(rootDirectory, "File.txt");
    sharedFile.createFile();

Create a directory:

    SharedDirectory sharedDirectory = new SharedDirectory(rootDirectory, "Directory");
    sharedDirectory.createDirectory();

Create a directory in the current directory (Same as createDirectory for path `Directory/Subdirectory`):

    SharedDirectory sharedDirectory = new SharedDirectory(rootDirectory, "Directory");
    sharedDirectory.createDirectoryInCurrentDirectory("Subdirectory");

Create a file in the current directory (Same as createFile for path `Directory`):

    SharedDirectory sharedDirectory = new SharedDirectory(rootDirectory, "Directory");
    sharedDirectory.createFileInCurrentDirectory("Subfile");


### Upload and download a file

Upload from an input stream:

    SharedFile sharedFile = new SharedFile(rootDirectory, "File.txt");
    InputStream inputStream = ... // Your input stream
    OutputStream outputStream = sharedFile.getOutputStream();

    IOUtils.copy(inputStream, outputStream);
    inputStream.close();
    outputStream.close();

Download to an output stream:

   
    SharedFile sharedFile = new SharedFile(rootDirectory, "File.txt");
    InputStream inputStream = sharedFile.getInputStream();
    OutputStream outputStream = ... // Your output stream

    IOUtils.copy(inputStream, outputStream);
    inputStream.close();
    outputStream.close();

## License

This project is licensed under the MIT license. For more information see the `LICENSE.md` file.

## Questions or problems

For questions and problems, please open an issue. Please also take a look at the open issues.
