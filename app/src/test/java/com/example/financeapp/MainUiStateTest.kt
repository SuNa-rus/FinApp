package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Test

class MainUiStateTest {

    @Test
    fun initialState_hasExpectedBalanceAndZeroTransactions() {
        val state = MainUiState()

        assertEquals(25000, state.balance)
        assertEquals(0, state.transactionCount)
    }

    @Test
    fun addIncome_increasesBalanceAndTransactionCount() {
        val state = MainUiState()
        val transaction = Transaction(500, TransactionType.INCOME)

        val result = reduce(state, MainUiEvent.AddTransaction(transaction))

        assertEquals(25500, result.balance)
        assertEquals(1, result.transactionCount)
    }

    @Test
    fun addExpense_decreasesBalanceAndTransactionCountIncreases() {
        val state = MainUiState()
        val transaction = Transaction(250, TransactionType.EXPENSE)

        val result = reduce(state, MainUiEvent.AddTransaction(transaction))

        assertEquals(24750, result.balance)
        assertEquals(1, result.transactionCount)
    }

    @Test
    fun multipleTransactions_updateBalanceAndCount() {
        val initialState = MainUiState()
        val afterIncome = reduce(
            initialState,
            MainUiEvent.AddTransaction(
                Transaction(500, TransactionType.INCOME),
            ),
        )
        val afterExpense = reduce(
            afterIncome,
            MainUiEvent.AddTransaction(
                Transaction(250, TransactionType.EXPENSE),
            ),
        )

        assertEquals(25250, afterExpense.balance)
        assertEquals(2, afterExpense.transactionCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroAmount_isRejected() {
        reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(
                Transaction(0, TransactionType.INCOME),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeAmount_isRejected() {
        reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(
                Transaction(-100, TransactionType.EXPENSE),
            ),
        )
    }
}
