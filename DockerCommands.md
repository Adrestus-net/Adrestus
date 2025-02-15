docker build -f DockerfileVMBuild -t dockerfile-vm-build .
docker build --build-arg APP_NAME=io.Adrestus.consensus.ConsensusVRFTest2 -f DockerfileConsensusTests -t image-name .
docker run dockerfile-vm-build

docker build -f DockerfileConsensusTests -t dockerfile-consensus-tests .
docker run dockerfile-consensus-tests