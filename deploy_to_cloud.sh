export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# build jar
./gradlew build


# build docker image then upload it to google container registry
docker build . --tag europe-west1-docker.pkg.dev/datatoggle-b83b6/containers-for-cloud-run-repo/server

# Nb: it requires docker to know about this registry, you must do the following command before
gcloud auth configure-docker europe-west1-docker.pkg.dev --quiet
docker push europe-west1-docker.pkg.dev/datatoggle-b83b6/containers-for-cloud-run-repo/server

# deploy image from container registry to google cloud run
gcloud run deploy server --image europe-west1-docker.pkg.dev/datatoggle-b83b6/containers-for-cloud-run-repo/server --platform managed --memory 1G --allow-unauthenticated --max-instances 1


