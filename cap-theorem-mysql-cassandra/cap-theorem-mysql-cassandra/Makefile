.PHONY: build docker-up docker-down docker-build-app docker-up-all docker-down-all

build:
	./gradlew clean build -x test

docker-up:
	docker compose -f docker-compose.mysql.yml up -d

docker-down:
	docker compose -f docker-compose.mysql.yml down

# Rebuild the runnable jar before assembling the image so the image is never stale.
docker-build-app: build
	docker build -t diegolirio/cap-theorem-mysql-cassandra .

docker-up-all: docker-build-app
	docker compose -f docker-compose.all.yml up -d

docker-down-all:
	docker compose -f docker-compose.all.yml down
