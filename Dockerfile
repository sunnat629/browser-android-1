FROM ubuntu:16.04
MAINTAINER Stefano Pacifici <stefano@cliqz.com>
ENV DEBIAN_FRONTEND noninteractive

RUN dpkg --add-architecture i386 && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 git xz-utils wget unzip openjdk-8-jdk python-dev python-pip python-virtualenv && \
    apt-get clean -y && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ARG UID
ARG GID
RUN groupadd jenkins --gid $GID && \
    useradd --create-home --shell /bin/bash jenkins --uid $UID --gid $GID

ENV ANDROID_HOME /home/jenkins/android_home
ENV GRADLE_USER_HOME /home/jenkins/gradle_home
USER jenkins

RUN mkdir -p $ANDROID_HOME; \
    mkdir -p $GRADLE_USER_HOME; \
    cd $ANDROID_HOME; \
    wget --output-document=sdktools.zip --quiet 'https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip'; \
    unzip sdktools.zip; \
    rm -r sdktools.zip; \
    (while (true); do echo y; sleep 2; done) | \
      tools/bin/sdkmanager  "build-tools;26.0.2" "platforms;android-23" "platforms;android-27" "platform-tools" "tools" "platforms;android-25" "extras;google;m2repository" "extras;android;m2repository" "extras;google;google_play_services";

ENV LD_LIBRARY_PATH "/home/jenkins/android_home/emulator/lib64/qt/lib"

RUN cd /home/jenkins && \
    wget --output-document=nodejs.tgz --quiet \
      'https://nodejs.org/dist/v8.9.3/node-v8.9.3-linux-x64.tar.xz' && \
    tar xf nodejs.tgz && \
    rm -f nodejs.tgz

ENV PATH "/home/jenkins/node-v8.9.3-linux-x64/bin:$PATH"

RUN npm install -g yarn
