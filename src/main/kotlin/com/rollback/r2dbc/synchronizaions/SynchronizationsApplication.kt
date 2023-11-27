package com.rollback.r2dbc.synchronizaions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SynchronizationsApplication

fun main(args: Array<String>) {
	runApplication<SynchronizationsApplication>(*args)
}
