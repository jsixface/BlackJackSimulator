package main


fun main(args: Array<String>) {
	val user = Player(50)
	val game = Game(user, 10)
	game.play()
	var score = 0
	game.history.forEach {
//		println(it)
		score += it[3]
	}
	println("score = ${score}")
	println("user.balance = ${user.balance}")
	println("game.history.size = ${game.history.size}")

}

class Player(var balance: Int) {
	val hand = mutableListOf<Int>()
	val splitHand = mutableListOf<Int>()
	val hasSplit: Boolean
		get() = splitHand.isNotEmpty()
	var bet = 0
	var splitBet = 0
	val handBust: Boolean
		get() = hand.softSum() > 21
	val splitBust: Boolean
		get() = splitHand.softSum() > 21
}


fun Iterable<Int>.softSum(): Int { // TODO implement properly
	return if (this.sum() > 21 && this.isSoft()) {
		this.sum() - 10
	} else
		this.sum()
}

fun Iterable<Int>.isSoft(): Boolean {
	for (element in this) {
		if (element == 11) {
			return true
		}
	}
	return false
}