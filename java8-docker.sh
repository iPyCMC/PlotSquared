docker rm jdk8
docker run -ti --volumes-from java8 --name jdk8  openjdk:8u111-jdk /bin/bash