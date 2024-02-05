import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}


sealed interface CounterEvent : CircuitUiEvent {
    data object Increase : CounterEvent
    data object Decrease : CounterEvent
}

data class CounterState(
    val count: Int,
    val eventSink: (CounterEvent) -> Unit
) : CircuitUiState

class CounterPresenter : Presenter<CounterState> {

    @Composable
    override fun present(): CounterState {
        var count by remember { mutableStateOf(0) }
        return CounterState(
            count = count,
            eventSink = { event ->
                when(event) {
                    CounterEvent.Decrease -> count--
                    CounterEvent.Increase -> count++
                }
            }
        )
    }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelize()

// For Android @TypeParceler
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.SOURCE)
@Repeatable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
expect annotation class CommonTypeParceler<T, P : CommonParceler<in T>>()

// For Android Parceler
expect interface CommonParceler<T>

@CommonParcelize
object CounterScreen : Screen

@Composable
fun CounterUI(modifier: Modifier = Modifier, state: CounterState) {
    Column {
        Button(
            onClick = {
                state.eventSink(CounterEvent.Decrease)
            },
            content = {
                Text("-")
            }
        )
        Text(text = "Count ${state.count}")
        Button(
            onClick = {
                state.eventSink(CounterEvent.Increase)
            },
            content = {
                Text("+")
            }
        )
    }
}

class PresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext
    ): Presenter<*>? {
        return when(screen) {
            is CounterScreen -> presenterOf { CounterPresenter().present() }
            else -> null
        }
    }
}

class UiFactory() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is CounterScreen -> ui<CounterState> { state, modifier ->
                CounterUI(state = state, modifier = modifier)
            }
            else -> null
        }
    }
}