package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {

    @Test
    fun initialState_hasZeroBalanceAndEmptyHistory() {
        val state = MainUiState()

        assertEquals(0, state.balance)
        assertEquals(0, state.transactionCount)
        assertTrue(state.transactions.isEmpty())
        assertEquals("", state.amount)
        assertEquals("", state.description)
        assertEquals(TransactionType.EXPENSE, state.type)
    }

    @Test
    fun addIncome_increasesBalance() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Зарплата"))
        viewModel.onEvent(MainUiEvent.AmountChanged("500"))
        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.addTransaction()

        assertEquals(500, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactionCount)
        assertEquals(1, viewModel.uiState.transactions.size)
    }

    @Test
    fun addExpense_decreasesBalance() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.addTransaction()

        assertEquals(-250, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactionCount)
    }

    @Test
    fun multipleTransactions_updateBalanceAndHistory() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.addTransaction()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Зарплата"))
        viewModel.onEvent(MainUiEvent.AmountChanged("500"))
        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.addTransaction()

        assertEquals(250, viewModel.uiState.balance)
        assertEquals(2, viewModel.uiState.transactionCount)
        assertEquals(2, viewModel.uiState.transactions.size)
        assertEquals("Зарплата", viewModel.uiState.transactions.last().description)
    }

    @Test
    fun successfulTransaction_clearsForm() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.addTransaction()

        assertEquals("", viewModel.uiState.amount)
        assertEquals("", viewModel.uiState.description)
        assertEquals(null, viewModel.uiState.error)
    }

    @Test
    fun emptyFields_showInvalidInput() {
        val viewModel = TransactionViewModel()

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.balance)
        assertTrue(viewModel.uiState.transactions.isEmpty())
    }

    @Test
    fun zeroAmount_isRejected() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.onEvent(MainUiEvent.AmountChanged("0"))
        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.transactionCount)
    }

    @Test
    fun nonDigitAmount_isIgnored() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.onEvent(MainUiEvent.AmountChanged("25a"))

        assertEquals("250", viewModel.uiState.amount)
    }

    @Test
    fun amountAboveIntRange_isRejected() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Покупка"))
        viewModel.onEvent(MainUiEvent.AmountChanged("2147483648"))
        viewModel.addTransaction()

        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.balance)
        assertTrue(viewModel.uiState.transactions.isEmpty())
    }

    @Test
    fun balanceOverflow_isRejected() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Доход"))
        viewModel.onEvent(MainUiEvent.AmountChanged("2147483647"))
        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.addTransaction()

        viewModel.onEvent(MainUiEvent.DescriptionChanged("Еще доход"))
        viewModel.onEvent(MainUiEvent.AmountChanged("1"))
        viewModel.addTransaction()

        assertEquals(Int.MAX_VALUE, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactionCount)
        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
    }
}
