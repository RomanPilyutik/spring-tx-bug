package com.rollback.r2dbc.synchronizaions.dbconfig.exception

class EntityNotFoundException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
