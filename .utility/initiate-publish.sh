#!/usr/bin/env bash
# This script initiates the Gradle publishing task when pushes to master occur.

if [ "$TRAVIS_REPO_SLUG" == "Rajawali/Rajawali" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
    echo -e "Starting to publish to Sonatype...\n"

    ./gradle uploadArchives -PSONATYPE_UID="${SONATYPE_USERNAME}" -PSONATYPE_PASS="${SONATYPE_PASSWORD}" -Psigning.keyId="@{SIGNING_KEY}" -Psigning.password="${SIGNING_KEY_PASSWORD}" -Psigning.secretKeyRingFile="./rajawali_secret.gpg"
    RETVAL=$?

    if [ $RETVAL -eq 0 ]; then
        echo 'Completed publish!'
    else
        echo 'Publish failed.'
        return $RETVAL
    fi
else
    echo -e "Not publishing - "
    echo $'\t'Repo: $TRAVIS_REPO_SLUG
    echo $'\t'Pull Request? $TRAVIS_PULL_REQUEST
    echo $'\t'Branch: $TRAVIS_BRANCH
fi