package net.canvoki.shared.test

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
