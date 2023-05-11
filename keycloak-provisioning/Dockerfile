FROM hashicorp/terraform:latest

LABEL org.label-schema.name="keycloak-provisioning" \
      org.label-schema.vendor="Noumena Digital" \
      org.label-schema.build-date="${BUILD_DATE}" \
      org.label-schema.vcs-ref="${GIT_REV}" \
      org.label-schema.version="${VERSION}" \
      org.opencontainers.image.source="https://github.com/noumenadigital/sbp-dxd"

ENV KEYCLOAK_USER="admin" \
    KEYCLOAK_PASSWORD="Sbp123!" \
    KEYCLOAK_URL=http://keycloak:11000

WORKDIR /terraform
COPY providers.tf terraform.tf /terraform/
COPY *.sh /
VOLUME /state

RUN terraform init
ENTRYPOINT []
CMD ["local.sh"]