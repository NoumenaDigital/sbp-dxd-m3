FROM amazoncorretto:17
ARG BUILD_DATE
ARG GIT_REV
ARG VERSION

WORKDIR /api

USER nobody

LABEL org.label-schema.name="api" \
      org.label-schema.vendor="Noumena Digital" \
      org.label-schema.build-date="${BUILD_DATE}" \
      org.label-schema.vcs-ref="${GIT_REV}" \
      org.label-schema.version="${VERSION}" \
      org.opencontainers.image.source="https://github.com/noumenadigital/sbp-dxd"

COPY target/dependency /api/lib
COPY target/api.jar /api/api.jar

CMD ["java", "-cp", "/api/api.jar:/api/lib/*", \
"-Dlogback.configurationFile=/api/logback-json.xml", \
"-XX:MaxRAMPercentage=75", \
"sbp.MainKt"]
