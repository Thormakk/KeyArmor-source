package com.thormakk.keyarmor.data.export

object CsvFieldMapper {

    const val TITLE    = "title"
    const val USERNAME = "username"
    const val EMAIL    = "email"
    const val PASSWORD = "password"
    const val URL      = "url"
    const val NOTES    = "notes"
    const val TOTP     = "totp"
    const val FOLDER   = "folder"
    const val FAVORITE = "favorite"


    enum class CsvSource {
        KEY_ARMOR, BITWARDEN, LASTPASS, DASHLANE, KEEPER,
        NORDPASS, ROBOFORM, KEEPASS_1X, KEEPASSXC, APPLE,
        CHROME, FIREFOX, EDGE, ENPASS, STRONGBOX,
        PASSWORD_SAFE, STICKY_PASSWORD, MY_PASSWORDS, GENERIC
    }

    private val BITWARDEN = mapOf(
        "name"           to TITLE,
        "login_uri"      to URL,
        "login_username" to USERNAME,
        "login_password" to PASSWORD,
        "login_totp"     to TOTP,
        "notes"          to NOTES,
        "folder"         to FOLDER,
        "collections"    to FOLDER,
        "favorite"       to FAVORITE
    )

    private val LASTPASS = mapOf(
        "name"     to TITLE,
        "url"      to URL,
        "username" to USERNAME,
        "password" to PASSWORD,
        "totp"     to TOTP,
        "extra"    to NOTES,
        "grouping" to FOLDER,
        "fav"      to FAVORITE
    )

    private val DASHLANE = mapOf(
        "title"     to TITLE,
        "url"       to URL,
        "username"  to USERNAME,
        "username2" to EMAIL,
        "password"  to PASSWORD,
        "otpsecret" to TOTP,
        "note"      to NOTES,
        "category"  to FOLDER
    )

    private val KEEPER = mapOf(
        "title"           to TITLE,
        "website address" to URL,
        "login"           to USERNAME,
        "password"        to PASSWORD,
        "notes"           to NOTES,
        "folder"          to FOLDER,
        "\$onetimecode"   to TOTP
    )

    private val NORDPASS = mapOf(
        "name"     to TITLE,
        "url"      to URL,
        "username" to USERNAME,
        "password" to PASSWORD,
        "note"     to NOTES,
        "folder"   to FOLDER,
        "email"    to EMAIL
    )

    private val ROBOFORM = mapOf(
        "name"   to TITLE,
        "url"    to URL,
        "login"  to USERNAME,
        "pwd"    to PASSWORD,
        "note"   to NOTES,
        "folder" to FOLDER
    )

    private val KEEPASS_1X = mapOf(
        "account"    to TITLE,
        "login name" to USERNAME,
        "password"   to PASSWORD,
        "web site"   to URL,
        "comments"   to NOTES
    )

    private val KEEPASSXC = mapOf(
        "title"    to TITLE,
        "username" to USERNAME,
        "password" to PASSWORD,
        "url"      to URL,
        "notes"    to NOTES,
        "group"    to FOLDER,
        "totp"     to TOTP
    )

    private val APPLE = mapOf(
        "title"    to TITLE,
        "url"      to URL,
        "username" to USERNAME,
        "password" to PASSWORD,
        "notes"    to NOTES,
        "otpauth"  to TOTP
    )

    private val CHROME = mapOf(
        "name"     to TITLE,
        "url"      to URL,
        "username" to USERNAME,
        "password" to PASSWORD
    )

    private val FIREFOX = mapOf(
        "url"                to URL,
        "username"           to USERNAME,
        "password"           to PASSWORD,
        "formactionorigin"   to "formActionOrigin"
    )

    private val EDGE = mapOf(
        "name"     to TITLE,
        "url"      to URL,
        "username" to USERNAME,
        "password" to PASSWORD,
        "note"     to NOTES
    )

    private val ENPASS = mapOf(
        "title"           to TITLE,
        "username"        to USERNAME,
        "email"           to EMAIL,
        "password"        to PASSWORD,
        "website"         to URL,
        "totp secret key" to TOTP,
        "note"            to NOTES,
        "tags"            to FOLDER
    )

    private val STRONGBOX = mapOf(
        "title"    to TITLE,
        "username" to USERNAME,
        "email"    to EMAIL,
        "password" to PASSWORD,
        "url"      to URL,
        "otpauth"  to TOTP,
        "notes"    to NOTES
    )

    private val PASSWORD_SAFE = mapOf(
        "group/title" to TITLE,
        "username"    to USERNAME,
        "password"    to PASSWORD,
        "url"         to URL,
        "notes"       to NOTES
    )

    private val STICKY_PASSWORD = mapOf(
        "nickname" to TITLE,
        "username" to USERNAME,
        "password" to PASSWORD,
        "url"      to URL,
        "note"     to NOTES
    )

    private val MY_PASSWORDS = mapOf(
        "title"    to TITLE,
        "account"  to EMAIL,
        "username" to USERNAME,
        "password" to PASSWORD,
        "website"  to URL,
        "notes"    to NOTES,
        "labels"   to FOLDER
    )

    private val KEY_ARMOR = mapOf(
        "name"       to TITLE,
        "url"        to URL,
        "username"   to USERNAME,
        "password"   to PASSWORD,
        "notes"      to NOTES,
        "category"   to FOLDER,
        "isfavorite" to FAVORITE
    )

    fun detectSource(headers: List<String>): CsvSource {
        val h = headers.map { it.trim().lowercase() }.toSet()
        return when {
            "login_username" in h                                  -> CsvSource.BITWARDEN
            "grouping" in h                                        -> CsvSource.LASTPASS
            "otpsecret" in h                                       -> CsvSource.DASHLANE
            h.any { it == "website address" }                      -> CsvSource.KEEPER
            "rffieldsv2" in h || "matchurl" in h                   -> CsvSource.ROBOFORM
            "labels" in h || ("account" in h && "website" in h)    -> CsvSource.MY_PASSWORDS
            "login name" in h || "account" in h                    -> CsvSource.KEEPASS_1X
            "httprealm" in h                                       -> CsvSource.FIREFOX
            "totp secret key" in h                                 -> CsvSource.ENPASS
            "cardholdername" in h                                  -> CsvSource.NORDPASS
            "otpauth" in h && "email" in h                         -> CsvSource.STRONGBOX
            "otpauth" in h                                         -> CsvSource.APPLE
            "group" in h && "title" in h && "url" in h             -> CsvSource.KEEPASSXC
            "isfavorite" in h || ("category" in h && "tags" in h)  -> CsvSource.KEY_ARMOR
            h.any { "group/title" in it }                          -> CsvSource.PASSWORD_SAFE
            "nickname" in h                                        -> CsvSource.STICKY_PASSWORD
            "note" in h && "url" in h && "name" in h               -> CsvSource.EDGE
            else                                                   -> CsvSource.GENERIC
        }
    }

    fun normalizeHeaders(headers: List<String>, source: CsvSource): List<String> {
        val map = getMappingFor(source)
        return headers.map { h ->
            val lower = h.trim().lowercase()
            map[lower] ?: lower
        }
    }

    private fun getMappingFor(source: CsvSource): Map<String, String> = when (source) {
        CsvSource.BITWARDEN       -> BITWARDEN
        CsvSource.LASTPASS        -> LASTPASS
        CsvSource.DASHLANE        -> DASHLANE
        CsvSource.KEEPER          -> KEEPER
        CsvSource.NORDPASS        -> NORDPASS
        CsvSource.ROBOFORM        -> ROBOFORM
        CsvSource.KEEPASS_1X      -> KEEPASS_1X
        CsvSource.KEEPASSXC       -> KEEPASSXC
        CsvSource.APPLE           -> APPLE
        CsvSource.FIREFOX         -> FIREFOX
        CsvSource.EDGE            -> EDGE
        CsvSource.CHROME          -> CHROME
        CsvSource.ENPASS          -> ENPASS
        CsvSource.STRONGBOX       -> STRONGBOX
        CsvSource.PASSWORD_SAFE   -> PASSWORD_SAFE
        CsvSource.STICKY_PASSWORD -> STICKY_PASSWORD
        CsvSource.MY_PASSWORDS    -> MY_PASSWORDS
        CsvSource.KEY_ARMOR       -> KEY_ARMOR
        CsvSource.GENERIC         -> CHROME
    }
}
