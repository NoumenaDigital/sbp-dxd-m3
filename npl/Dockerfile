FROM ghcr.io/noumenadigital/packages/engine:2023.1.7

LABEL org.opencontainers.image.source = "https://github.com/noumenadigital/sbp-dxd"

ENV ENGINE_NPL_MIGRATION_DIRECTORY_PATH="/npl" \
    FEATURE_APPLY_NPL_MIGRATIONS_AT_STARTUP="true"

COPY src/main/yaml /npl/yaml
COPY src/main/npl  /npl/npl-1.0.0
