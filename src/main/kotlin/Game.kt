package main

import java.util.*
import kotlin.random.Random


class Game(
	private val player: Player, private val maxRounds: Int,
	val hardStop: Boolean = true, private val minBet: Int = 10
) {
	private val deck: Stack<Int> = Stack()
	private var cutPoint = 0

	val history = mutableListOf<List<Int>>()

	init {
		shuffleDeck()
	}

	fun play() {
		var round = 1
		while (round <= maxRounds && player.balance >= minBet) {
			round++
			player.hand.clear()
			player.splitHand.clear()
			if (deck.size < cutPoint) shuffleDeck()

			player.hand.add(deck.pop())
			player.hand.add(deck.pop())

			val dealerHand = mutableListOf(deck.pop(), deck.pop())

			player.balance -= minBet
			player.bet = minBet

			splitIfNeeded(dealerHand)

			var addBet = playHand(player.hand, dealerHand)
			player.balance -= addBet
			player.bet += addBet

			if (player.hasSplit) {
				addBet = playHand(player.splitHand, dealerHand)
				player.balance -= addBet
				player.splitBet += addBet
			}

			if (player.splitBust && player.handBust && player.hasSplit) {
				writeHistory(2, dealerHand.softSum(), player.hand.softSum(), 0)
				writeHistory(2, dealerHand.softSum(), player.splitHand.softSum(), 0)
				continue
			}

			if (player.handBust && !player.hasSplit) {
				writeHistory(2, dealerHand.softSum(), player.hand.softSum(), 0)
				continue
			}

			while (dealerHand.sum() < 17) { // TODO factor in hard/soft stop
				dealerHand.add(deck.pop())
			}

			val outcome = determineOutcome(dealerHand, player.hand)
			player.balance += player.bet * outcome
			writeHistory(2 - outcome, dealerHand.softSum(), player.hand.softSum(), outcome)

			if (player.hasSplit) {
				val splitOutcome = determineOutcome(dealerHand, player.splitHand)
				player.balance += player.splitBet * splitOutcome
				writeHistory(2 - splitOutcome, dealerHand.softSum(), player.splitHand.softSum(), splitOutcome)
			}
		}
	}

	private fun writeHistory(dScore: Int, dSum: Int, pSum: Int, pScore: Int) {
		val stat = listOf(dScore, dSum, pSum, pScore)
		println(stat)
		history.add(stat)
	}

	private fun splitIfNeeded(dealerHand: MutableList<Int>) {
		// Splitting if we get Duplicate card.
		val pFirstCard = player.hand[0]
		val dOpenCard = dealerHand[0]
		if (player.balance >= minBet &&
			pFirstCard == player.hand[1] &&
			(pFirstCard == 8 || pFirstCard == 11 ||
					(pFirstCard == 9 && !listOf(7, 10, 11).contains(dOpenCard)) ||
					(listOf(2, 3, 7).contains(pFirstCard) && listOf(2, 3, 4, 5, 6, 7).contains(dOpenCard)) ||
					(pFirstCard == 6 && listOf(2, 3, 4, 5, 6).contains(dOpenCard)))
		) {
			println("Splitting ${player.hand}")
			player.hand[1] = deck.pop()
			player.splitHand.add(pFirstCard)
			player.splitHand.add(deck.pop())
			player.balance -= minBet
			player.splitBet = minBet
		}
	}

	private fun playHand(hand: MutableList<Int>, dealerHand: MutableList<Int>): Int {
		var additionalBet = 0
		val openCard = dealerHand[0]
		while (hand.softSum() < 17) {
			// Hard double for 9,10,11
			if ((setOf(3, 4, 5, 6).contains(openCard) && hand.sum() == 9) ||
				(openCard in 2..9 && hand.sum() == 10) ||
				(openCard in 2..10 && hand.sum() == 11) &&
				player.balance >= minBet
			) { // Double down
				additionalBet = minBet
				hand.add(deck.pop())
				println("Doubling down on cards $hand against ${dealerHand[0]}")
				break
			}
			if ((hand.sum() == 12 && openCard in 4..6)
				|| (hand.sum() in 13..16 && openCard in 2..6)
			) {
				break
			}
			if (hand.isSoft()) {
				if ((hand.sum() in 13..14 && openCard in 5..6)
					|| (hand.sum() in 15..16 && openCard in 4..6)
					|| (hand.sum() in 17..18 && openCard in 3..6)
					&& player.balance >= minBet
				) { // Double down
					additionalBet = minBet
					hand.add(deck.pop())
					println("Doubling down on cards $hand against ${dealerHand[0]}")
					break
				}
				if ((hand.sum() == 18 && setOf(2,7,8).contains(openCard))
					&& player.balance >= minBet) {
					break // Stand
				}

			}
			hand.add(deck.pop())
		}
		return additionalBet
	}

	private fun determineOutcome(dealerHand: MutableList<Int>, hand: MutableList<Int>): Int {
		return when {
			hand.softSum() > 21 -> 0
			dealerHand.softSum() < hand.softSum() || dealerHand.softSum() > 21 -> 2
			dealerHand.softSum() == hand.softSum() -> 1
			else -> 0
		}
	}


	private fun shuffleDeck() {
		// Initialise the desk with 6 deck of cards
		val cards = mutableListOf<Int>()
		for (i in 1..6) { // 6 Decks
			for (face in 1..4) { // 4 faces per deck
				for (card in 2..9) cards.add(card)
				for (tens in 1..4) cards.add(10)
				cards.add(11)
			}
		}
		for (i in 1..3) { // Shuffle thrice
			for (index in cards.indices) {
				val r = Random.nextInt(index, cards.size)
				cards[index] = cards[r].also { cards[r] = cards[index] }
			}
		}
		deck.addAll(cards)
		cutPoint = 50 + Random.nextInt(6 * 52) / 12
		println("Shuffled cards")
	}
}
