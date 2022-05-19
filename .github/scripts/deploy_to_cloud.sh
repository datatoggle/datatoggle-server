# build jar
# TODO: could be replaced with 2-step docker build (cf. cloud run quick start for java/spring)
./gradlew build

# build docker image then upload it to google container registry
docker build . --tag gcr.io/datatoggle-b83b6/server
docker push gcr.io/datatoggle-b83b6/server

# deploy image from container registry to google cloud run
gcloud run deploy server --region europe-west1 --image gcr.io/datatoggle-b83b6/server --platform managed --memory 1G --allow-unauthenticated --max-instances 1
