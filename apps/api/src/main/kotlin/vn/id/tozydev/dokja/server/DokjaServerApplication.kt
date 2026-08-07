package vn.id.tozydev.dokja.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class DokjaServerApplication

fun main(args: Array<String>) {
    runApplication<DokjaServerApplication>(*args)
}
