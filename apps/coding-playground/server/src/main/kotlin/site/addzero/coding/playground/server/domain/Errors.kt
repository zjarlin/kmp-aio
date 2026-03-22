package site.addzero.coding.playground.server.domain

class PlaygroundValidationException(message: String) : IllegalArgumentException(message)

class PlaygroundNotFoundException(message: String) : NoSuchElementException(message)
