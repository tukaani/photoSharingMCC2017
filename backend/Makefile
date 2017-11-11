IMAGE:= mysterious_mind:latest

all: build run

build:
	docker build -t $(IMAGE) .

run:
	docker run -d -p 8080:8080 $(IMAGE)