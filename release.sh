#!/bin/bash
cd `dirname $0`
OS=$1
if [ -z "$1" ]; then
    cat <<- EOF
        Desc: need set target os;
        Usage: ./release.sh window
               ./release.sh linux
               ./release.sh macos
EOF
    exit 0
fi
./package -a adrestus-config
./package -a adrestus-consenus
./package -a adrestus-core
./package -a adrestus-crypto
./package -aadrestus-network
./package -aadrestus-protocol
./package -aadrestus-util
PACKAGE_VERSION=`cat Adrestus/version`
eval "sed -e 's/%PACKAGE_VERSION%/${PACKAGE_VERSION}/g' config/Adrestus.ncf >Adrestus_Wallet/Adrestus.ncf"
tar -czf Adrestus_Wallet_${OS}_v2.6.0.1.tar Adrestus_Wallet
rm -rf Adrestus_Wallet