package xyz.olympusblog.models

data class ValidationErrors(val errors: List<ValidationError>) {
    data class ValidationError(val field: String, val message: String)
}