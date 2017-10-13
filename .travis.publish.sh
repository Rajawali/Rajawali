set -e

openssl aes-256-cbc -K $encrypted_64bf13095d6f_key -iv $encrypted_64bf13095d6f_iv -in rajawali_secret.gpg.enc -out rajawali_secret.gpg -d
docker run -it -u `stat -c "%u:%g" .` -v ~/.aws:/root/.aws -v ${TRAVIS_BUILD_DIR}:/workspace -w /workspace rajawali/alpine-glibc-android /bin/sh -c \
    "./gradlew build publishArtifacts -Pprofile=sources,javadoc --stacktrace --continue --console=plain"
