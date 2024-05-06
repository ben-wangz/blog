ARG VARIANT="38"
FROM docker.io/library/fedora:${VARIANT}

# configure repos
RUN rm -f /etc/yum.repos.d/*.repo
COPY fedora.repo /etc/yum.repos.d/fedora.repo
# install basic tools
RUN dnf install -y java-11-openjdk-devel g++ gcc cmake vim curl zsh git procps-ng
# on-my-zsh
RUN [ -d ~/.oh-my-zsh ] \
    || sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
ENV SHELL /bin/zsh
# download gradle package
RUN mkdir -p /opt/gradle/packages \
    && curl -Lo /opt/gradle/packages/gradle-8.5-all.zip https://mirrors.cloud.tencent.com/gradle/gradle-8.5-all.zip
