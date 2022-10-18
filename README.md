# Omni Trade Application

This project is part of the final assignment submission for the Graduate
Certificate in Securing Ubiquitous Systems

## The Tech Stack™

### Backend

- [Spring Boot](https://spring.io/) for Orders Service
- [Helidon SE](https://helidon.io/) for Forums Service
- [Keycloak](https://www.keycloak.org/) for OAuth 2.0
- [PostgreSQL](https://www.postgresql.org/) for Database
- [ElasticMQ](https://github.com/softwaremill/elasticmq) as a replacement of AWS
  SQS for local development/vendor-neutral configuration

### Frontend

- [VueJS](https://vuejs.org/)

### Supporting Applications

- [Caddy Server](https://caddyserver.com/) for TLS provisioning, termination and
free certificate issuance

### Cloud Platforms and Infrastructure

- AWS SQS
- CockroachDB Serverless
- Cloudflare DNS
- GitHub Actions
- Oracle Cloud
- Postmark E-Mail Service

## Development Setup

### Common (Backend + Frontend)

- [Docker](https://www.docker.com/)

### Backend

- [JDK 11+](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
- [Maven 3.8+](https://maven.apache.org/download.html)
- [Helidon CLI](https://helidon.io/) - only for Forums service

If you are using VS Code, the
[Lombok Annotations](https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok)
extension is required.

### Frontend

- [Node.js v16 LTS](https://nodejs.org/en/download/)
- [Vue CLI](https://cli.vuejs.org/)

## Development Loop

Simply run `docker-compose -f ./docker-compose.yml up -d` in the root project
folder to start the containers.

To develop on the frontend, simply run `npm run serve` in the `frontend` folder.
(Assuming you've already installed the dependencies with `npm install`.)
