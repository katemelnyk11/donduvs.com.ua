environment                    = "sandpit"
dns_state_bucket               = null
dns_state_key                  = null
dns_state_role                 = null
shared_state_bucket            = "digital-identity-dev-tfstate"
test_clients_enabled           = "true"
ipv_api_enabled                = true
ipv_authorisation_callback_uri = ""
ipv_authorisation_uri          = ""
ipv_authorisation_client_id    = ""
logging_endpoint_enabled       = false
logging_endpoint_arns          = []

enable_api_gateway_execution_request_tracing = true
spot_enabled                                 = false

lambda_max_concurrency = 0
lambda_min_concurrency = 0
endpoint_memory_size   = 1024

blocked_email_duration                    = 30
otp_code_ttl_duration                     = 120
email_acct_creation_otp_code_ttl_duration = 60

txma_account_id                = "12345678"
extended_feature_flags_enabled = true