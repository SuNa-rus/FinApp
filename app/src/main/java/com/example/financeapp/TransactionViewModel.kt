package com.example.financeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

internal class TransactionViewModel : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    fun onEvent(event: MainUiEvent) {
        uiState = reduce(uiState, event)
    }

    fun addTransaction() {
        val description = uiState.description.trim()
        val amount = uiState.amount.toIntOrNull()

        when {
            uiState.amount.isEmpty() || description.isEmpty() ->
                onEvent(MainUiEvent.InvalidTransaction)

            amount == null ->
                onEvent(MainUiEvent.AmountTooLarge)

            amount <= 0 ->
                onEvent(MainUiEvent.InvalidTransaction)

            else ->
                onEvent(
                    MainUiEvent.AddTransaction(
                        Transaction(
                            amount = amount,
                            type = uiState.type,
                            description = description,
                        ),
                    ),
                )
        }
    }

    private fun reduce(state: MainUiState, event: MainUiEvent): MainUiState = when (event) {
        is MainUiEvent.AmountChanged ->
            if (event.value.all(Char::isDigit)) {
                state.copy(amount = event.value, error = null)
            } else {
                state
            }

        is MainUiEvent.DescriptionChanged ->
            state.copy(description = event.value, error = null)

        is MainUiEvent.TypeChanged ->
            state.copy(type = event.type, error = null)

        MainUiEvent.InvalidTransaction ->
            state.copy(error = TransactionError.INVALID_INPUT)

        MainUiEvent.AmountTooLarge ->
            state.copy(error = TransactionError.AMOUNT_TOO_LARGE)

        is MainUiEvent.AddTransaction -> {
            val tx = event.transaction

            require(tx.amount > 0)
            require(tx.description.isNotBlank())

            val change = if (tx.type == TransactionType.INCOME) {
                tx.amount
            } else {
                -tx.amount
            }

            val next = state.balance.toLong() + change

            if (next !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
            } else {
                state.copy(
                    amount = "",
                    description = "",
                    balance = next.toInt(),
                    transactionCount = state.transactionCount + 1,
                    transactions = state.transactions + tx,
                    error = null,
                )
            }
        }
    }
}
