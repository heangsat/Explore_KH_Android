.PHONY: run seed test tidy fmt vet docker-up docker-down

run:
	go run ./cmd/api

seed:
	go run ./cmd/seed -file seeds/destinations.json

test:
	go test ./...

tidy:
	go mod tidy

fmt:
	gofmt -w ./cmd ./internal

vet:
	go vet ./...

docker-up:
	docker compose up --build

docker-down:
	docker compose down
