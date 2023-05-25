package de.komantis.snacknote

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SnackNoteLayout(
    state: SnackNoteState,
    noteShape: Shape = RoundedCornerShape(16.dp),
    noteBackground: Color = Color.White,
    content: @Composable () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        content()

        AnimatedVisibility(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
            visible = state.visibleState,
            enter = slideInVertically{ height ->
                (-height * 1.1).roundToInt()
            },
            exit = slideOutVertically{ height ->
                (-height * 1.1).roundToInt()
            }
        ){
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(
                        minHeight = 64.dp
                    )
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource
                    ){
                        state.clickAction()
                     },
                shape = noteShape,
                colors = CardDefaults.cardColors(
                    containerColor = noteBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = state.text,
                        fontSize = 14.sp
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun SnackNoteLayoutPreview(

){
    val snackState = rememberSnackNoteState(
        durationMillis = 3000,
        vibrationFeedback = true,
        context = LocalContext.current
    )
    
    val scope = rememberCoroutineScope()

    SnackNoteLayout(
        state = snackState
    ){
        Button(onClick = { 
            scope.launch {
                snackState.show(
                    title = "TestTitle",
                    text = "TestText",
                    onClick = { println("Click") }
                )
            }
        }) {
            Text(text = "Show SnackNote")
        }
    }
}






class SnackNoteState(
    val durationMillis: Long,
    val vibrationFeedBack: Boolean,
    val context: Context
) {
    var visibleState by mutableStateOf(false)
    var clickAction: () -> Unit = {  }
    var title = ""
    var text = ""
    private val vibrator = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        else context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

    suspend fun show(
        title: String,
        text: String,
        onClick: () -> Unit
    ){
        clickAction = onClick
        this.title = title
        this.text = text

        val effect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        if(vibrationFeedBack){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                (vibrator as Vibrator).vibrate(effect)
            }
            else{
                (vibrator as VibratorManager).defaultVibrator.vibrate(effect)
            }
        }

        visibleState = true
        delay(durationMillis)
        visibleState = false
    }
}

@Composable
fun rememberSnackNoteState(
    durationMillis: Long,
    vibrationFeedback: Boolean,
    context: Context
): SnackNoteState{
    return remember{ SnackNoteState(
        durationMillis = durationMillis,
        vibrationFeedBack = vibrationFeedback,
        context = context
    ) }
}
