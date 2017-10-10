set -e

# Prepare gradle.properties
echo -e "" >> "gradle.properties"
echo -e "NEXUS_USERNAME=${SONATYPE_USERNAME}" >> "gradle.properties"
echo -e "NEXUS_PASSWORD=${SONATYPE_PASSWORD}" >> "gradle.properties"
echo -e "signing.keyId=${SIGNING_KEY}" >> "gradle.properties"
echo -e "signing.password=${SIGNING_KEY_PASSWORD}" >> "gradle.properties"
echo -e "signing.secretKeyRingFile=../rajawali_secret.gpg" >> "gradle.properties"

# Prepare AWS
mkdir ~/.aws
echo -e "" >> ~/.aws/credentials
echo -e "[default]" >> ~/.aws/credentials
echo -e "aws_access_key_id=${AWS_ACCESS_KEY}" >> ~/.aws/credentials
echo -e "aws_secret_access_key=${AWS_SECRET_ACCESS_KEY}" >> ~/.aws/credentials
