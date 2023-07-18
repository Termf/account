#!/usr/bin/env bash

# usage:  ./build.sh your_name;  eg: ./build.sh zzp

set -e


if [ ! $REGISTRY_HUB ];then
    echo "please add REGISTRY_HUB  environment first!"
    exit
fi


PREFIX=${1}

if [ ! $PREFIX ]; then
    PREFIX='default'
fi

TAG=${PREFIX}-`date  +%Y%m%d%H%M`


# build
mvn clean package -Dmaven.test.skip=true -U -e

cp -r Dockerfile  account-web/target/

echo "${TAG}"

cd account-web/target/ && mv *SNAPSHOT.jar app.jar
docker build -t ${REGISTRY_HUB}/account:${TAG} .

# push to registry hub
docker push ${REGISTRY_HUB}/account:${TAG}