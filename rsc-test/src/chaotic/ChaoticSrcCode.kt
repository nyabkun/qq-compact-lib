package chaos.chaotic

import kotlin.random.Random

class ChaoticClass {
    fun doChaoticOperation1() {
        val list = mutableListOf<Int>()
        repeat(10) {
            val randomNumber = Random.nextInt(100)
            list.add(randomNumber)
        }
        list.shuffle()
    }

    fun doChaoticOperation2() {
        val text = "Hello, World!"
        val reversedText = text.reversed()
        val uppercaseText = reversedText.toUpperCase()
        val result = uppercaseText.substring(6)
        println(result)
    }

    fun sayHello() {
        justSayHello()
    }

    fun doChaoticOperation3() {
        val map = mutableMapOf<Char, Int>()
        val alphabet = ('a'..'z').toList()
        repeat(100) {
            val randomChar = alphabet.random()
            val count = map.getOrDefault(randomChar, 0)
            map[randomChar] = count + 1
        }
        val sortedEntries = map.entries.sortedByDescending { it.value }
        println(sortedEntries)
    }

    fun doChaoticOperation4() {
        val numbers = (1..10).toList()
        val evenNumbers = numbers.filter { it % 2 == 0 }
        val squaredNumbers = evenNumbers.map { it * it }
        val sum = squaredNumbers.sum()
        println(sum)
    }
}