FROM sbtscala/scala-sbt:graalvm-ce-22.3.3-b1-java17_1.9.6_2.13.12 as build
COPY . /persons
WORKDIR /persons

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN sbt assembly
CMD java -jar target/scala-2.13/persons-assembly-0.1.0-SNAPSHOT.jar
