package site.addzero.notes.server.testcontroller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

data class B(
    val name: String,
    val age: Int
)


@GetMapping("/djdjdjdjdd")
fun dijoaijd(msg: String): String {
    return msg
}

@PostMapping("/djdjddaoisdjoiasdjoiadsjdjdd")
fun dijoadaosidjijd(@RequestBody b: B): B {
    return b
}
