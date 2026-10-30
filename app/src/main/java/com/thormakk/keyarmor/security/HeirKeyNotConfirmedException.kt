package com.thormakk.keyarmor.security

class HeirKeyNotConfirmedException(heirEmail: String) : IllegalStateException(
    "La clave pública de $heirEmail no fue confirmada por el titular — sellado bloqueado"
)
