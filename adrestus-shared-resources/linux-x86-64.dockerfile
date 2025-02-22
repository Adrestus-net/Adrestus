FROM ubuntu:latest

WORKDIR /build

COPY Makefile.libgmp /build/Makefile

RUN apt-get  update && apt-get install -y curl && apt-get -y  clean
RUN apt-get install -y git autoconf curl libnss3-tools gcc make tar bzip2

# Add commands for MINGW32
RUN apt-get install -y mingw-w64

CMD ["make", "install"]