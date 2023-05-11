# reference: https://registry.terraform.io/providers/mrparkers/keycloak/latest/docs/resources/openid_client

variable "default_password" {
  type = string
}

resource "keycloak_realm" "realm" {
  realm = "sbp"
}

resource "keycloak_role" "nm_user" {
  realm_id    = keycloak_realm.realm.id
  name        = "NM_USER"
  description = "Required role for accessing the platform"
}

resource "keycloak_default_roles" "default_roles" {
  realm_id      = keycloak_realm.realm.id
  default_roles = ["offline_access", "uma_authorization", keycloak_role.nm_user.name]
}

resource "keycloak_openid_client" "client" {
  realm_id                     = keycloak_realm.realm.id
  client_id                    = "sbp"
  access_type                  = "PUBLIC"
  direct_access_grants_enabled = true
}

resource "keycloak_user" "user1" {
  realm_id   = keycloak_realm.realm.id
  username   = "user1"
  attributes = {
    "party" = jsonencode(["unused"])
  }
  initial_password {
    value     = var.default_password
    temporary = false
  }
}

resource "keycloak_user" "user2" {
  realm_id   = keycloak_realm.realm.id
  username   = "user2"
  attributes = {
    "party" = jsonencode(["unused"])
  }
  initial_password {
    value     = var.default_password
    temporary = false
  }
}

resource "keycloak_user" "user3" {
  realm_id   = keycloak_realm.realm.id
  username   = "user3"
  attributes = {
    "party" = jsonencode(["unused"])
  }
  initial_password {
    value     = var.default_password
    temporary = false
  }
}

resource "keycloak_user" "sbp" {
  realm_id   = keycloak_realm.realm.id
  username   = "sbp"
  attributes = {
    "party" = jsonencode(["sbp"])
  }
  initial_password {
    value     = var.default_password
    temporary = false
  }
}

resource "keycloak_openid_user_attribute_protocol_mapper" "party_mapper" {
  realm_id  = keycloak_realm.realm.id
  client_id = keycloak_openid_client.client.id
  name      = "party-mapper"

  user_attribute   = "party"
  claim_name       = "party"
  claim_value_type = "JSON"
}

resource "keycloak_openid_user_attribute_protocol_mapper" "uuid_mapper" {
  realm_id  = keycloak_realm.realm.id
  client_id = keycloak_openid_client.client.id
  name      = "uuid-mapper"

  user_attribute   = "uuid"
  claim_name       = "uuid"
  claim_value_type = "String"
}
