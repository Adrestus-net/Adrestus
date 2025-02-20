docker build -f DockerfileVMBuild -t dockerfile-vm-build .
docker build --build-arg APP_NAME=io.Adrestus.consensus.ConsensusVRFTest2 -f DockerfileConsensusTests -t image-name .
docker run dockerfile-vm-build

docker build -f DockerfileConsensusTests -t dockerfile-consensus-tests .
docker run dockerfile-consensus-tests

//Thiss command must be run from the wsl ubuntu machine to make sure docker is listening
sudo update-alternatives --set iptables /usr/sbin/iptables-legacy
sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy
sudo dockerd &
sudo docker system prune -a --volumes
ln -s /mnt/c/Users/User/Documents/GitHub/Adrestus /home/Documents/Adrestus
cp /mnt/c/Users/User/Documents/GitHub/Adrestus /home/Documents/Adrestus

//use this command if already bind address is in use
net stop winnat
net start winnat

//Execute the following command to allow connections using WSL:
New-NetFirewallRule -DisplayName "WSL" -Direction Inbound -InterfaceAlias "vEthernet (WSL)"  -Action Allow

Then execute the command to renew the firewall rules:
Get-NetFirewallProfile -Name Public | Get-NetFirewallRule | where DisplayName -ILike "IntelliJ IDEA*" |
Disable-NetFirewallRule

