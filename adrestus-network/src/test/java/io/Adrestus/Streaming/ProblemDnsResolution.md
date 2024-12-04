# Make sure ipv4 is enabled or use it programmatically
```-Djava.net.preferIPv4Stack=true```

# Steps to Change Hostname
# Open Terminal: Press Ctrl + Alt + T to open the terminal.

# Check Current Hostname: To see the current hostname, type:

```hostnamectl```
# This will display the current hostname along with other system information.

# Set New Hostname: To change the hostname, use the following command:

```sudo hostnamectl set-hostname new-hostname```
# Replace new-hostname with the desired hostname.

# Verify Change: To confirm the change, run:

```sh hostnamectl```
# You should see the new hostname listed under the "Static hostname" section.


# Go to windows and do the following:

# Open the Hosts File:

# In Notepad, click on File > Open.

Navigate to ```C:\Windows\System32\drivers\etc```.

# Select the hosts file. If you don't see it, make sure to change the file type filter to "All Files" at the bottom right1.

# Add the IP Address and Hostname:

# At the end of the file, add a new line with the IP address of your virtual machine followed by the hostname you want to map. For example:

```192.168.1.100    myvmhostname```
# Save the changes by pressing Ctrl + S or clicking on File > Save

# then run the following command in the terminal:

```pconfig /flushdns```