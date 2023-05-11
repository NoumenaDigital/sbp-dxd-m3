GITHUB_SHA=HEAD
VERSION=1.0.0-SNAPSHOT
MAVEN_CLI_OPTS?=-s .m2/settings.xml
MVN=mvn $(MAVEN_CLI_OPTS)
LEVANT_VERSION=0.3.2

.PHONY:	clean
clean:
	docker-compose down --remove-orphans --volumes
	$(MVN) clean

.PHONY: copy-openapi
copy-openapi:
	rm -f frontend/src/data/api/openapi.yml
	cp api/src/main/resources/openapi.yml frontend/src/data/api/

.PHONY: install
install: copy-openapi
	$(MVN) install
	docker-compose -f docker-compose.yml build --build-arg VERSION="$(VERSION)" --build-arg GIT_REV="$(GITHUB_SHA)" --build-arg BUILD_DATE="$(shell date)"

.PHONY:	format
format:
	$(MVN) ktlint:format

.PHONY:	run-only
run-only:
	docker-compose up -d

.PHONY:	run
run: format install run-only

.PHONY:	stop
stop:
	docker-compose down

.PHONY: run-integration-test
run-integration-test: run-only
	${MVN} -am clean integration-test verify -Pintegration-test -pl it-test
	docker-compose down --volumes

.PHONY: integration-test
integration-test: clean install run-integration-test

.PHONY:	login
login:
	echo $(GITHUB_USER_PASS) | docker login ghcr.io -u $(GITHUB_USER_NAME) --password-stdin
