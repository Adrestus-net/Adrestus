# Use a Windows base image (e.g., Windows Server Core)
FROM mcr.microsoft.com/windows:ltsc2019

# Set working directory (Windows-style path)
WORKDIR C:/build

# Copy Makefile.libgmp from the host to the container
COPY Makefile.libgmp C:/build/Makefile

# Install Chocolatey (Windows package manager)
RUN powershell -Command \
    Set-ExecutionPolicy Bypass -Scope Process -Force; \
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; \
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install tools using Chocolatey (equivalent of apt-get)
RUN choco install -y git autoconf curl make tar 7zip mingw --no-progress

# Cleanup (optional)
RUN powershell -Command \
    Remove-Item -Path C:\ProgramData\chocolatey\logs -Recurse -Force

# Default command
CMD ["make", "install"]