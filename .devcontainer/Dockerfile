ARG VARIANT="bullseye"
FROM mcr.microsoft.com/vscode/devcontainers/java:11-${VARIANT}

RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends g++ gcc cmake vim curl zsh git

RUN [ -d ~/.oh-my-zsh ] \
    || sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
ENV SHELL /bin/zsh
