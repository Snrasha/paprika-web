#docker-machine create --driver generic --generic-ip-address=spirals-somca --generic-ssh-key ~/.ssh/id_rsa --generic-ssh-user=gwillefe spirals-somca

docker system prune -f
docker-compose build neoj
docker-compose up -d neoj
docker build --file Dockerfile-web -t paprika-web .
docker build --file Dockerfile-analyze -t paprika-analyze .
docker-compose build web
docker-compose up web

#--verbose

#docker system prune   delete all not-used things
#if problem you can use --verbose :  exemple: docker-compose --verbose up neo4j
#you can also docker rm/rmi containers/images


#for stop a container: docker stop neo4j-paprika
#for start a container:docker start neo4j-paprika

#For get the id adress of a container:
#docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' neo4j-paprika
