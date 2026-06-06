package net.canvoki.shared.test

private val UUID_REGEX =
    Regex("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""")

fun assertIsUUID(value: String, message: String? = null) =
    assertMatches(UUID_REGEX, value, message)

fun assertMatches(
    regex: Regex,
    value: String,
    message: String? = null,
) {
    check(value.matches(regex)) {
        buildString {
            if (message != null) {
                appendLine(message)
            }
            append("'$value' does not match pattern $regex")
        }
    }
}
