## Building Native Library

Below are instructions for building the libgmp library that ships with this module

#### Linux

There is an included Dockerfile that can be used to compile libgmp with

First build the Docker image

    docker build -t jnagmp-linux-x86-64 -f linux-x86-64.dockerfile .

Next run the Docker image which will execute the [Makefile](Makefile.libgmp) and output the compiled library in to
_src/main/resources_

    docker run -v "$(pwd)/src/main/resources:/build/src/main/resources" -t jnagmp-linux-x86-64

#### Windows

## 1 OPTION: Using cygwin

- i686-w64-mingw32-gcc: For 32-bit Windows applications. Not exist on packet manger run version command to check it on
  cygwin
- x86_64-w64-mingw32-gcc: For 64-bit Windows applications. Not exist on packet manger run version command to check it on
  cygwin

- mingw64-i686-gcc-g++ - The GNU Compiler Collection (C++ compiler). For 32-bit Windows applications.
- mingw64-x86_64-gcc-g++ - The GNU Compiler Collection (C++ compiler). For 64-bit Windows applications.

- install cygwin on setup make sure all this installed
- gcc-core - The GNU Compiler Collection (C compiler).
- gcc-g++ - The GNU Compiler Collection (C compiler).
- libgcc - The GNU Compiler Collection (C compiler).
- make - The GNU version of the 'make' utility.
- autoconf - A tool for automatically configuring source code.
- automake - A tool for automatically generating Makefile.in files.
- libtool - A generic library support script.
- m4 - A macro processing language.
- curl - A tool to transfer data from or to a server.

Add the following to your PATH environment variable

    C:\cygwin64\bin

Make sure you have the following run:

    i686-w64-mingw32-gcc --version For 32-bit Windows applications
    x86_64-w64-mingw32-gcc --version For 64-bit Windows applications

Navigate to the directory where you want to install run commands

    cd /cygdrive/c/Users/User/Documents/GitHub/Adrestus/adrestus-shared-resources

Runs the following command to compile the library

     cd gmp-6.3.0
    ./configure --disable-static --enable-shared --host=x86_64-w64-mingw32
     OR
     make -f WindowsMakefile.libgmp install
     mv libgmp-10.dll libgmp.dll

## 1 OPTION: Using Windows Docker machine

First build the Docker image

    docker build -t jnagmp-windows-x86-64 -f win32.dockerfile .

Next run the Docker image which will execute the [Makefile](Makefile.libgmp) and output the compiled library in to
_src/main/resources_

    docker run -v "$(pwd)/src/main/resources:/build/src/main/resources" -t jnagmp-windows-x86-64