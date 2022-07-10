package app.lawnchair.ui.preferences.components.colorpreference.pickers

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import app.lawnchair.theme.color.ColorOption
import app.lawnchair.ui.preferences.components.Chip
import app.lawnchair.ui.preferences.components.DividerColumn
import app.lawnchair.ui.preferences.components.PreferenceGroup
import app.lawnchair.ui.preferences.components.colorpreference.*
import com.android.launcher3.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

/**
 * Unlike [PresetsList] & [SwatchGrid], This Composable allows the user to select a fully custom [ColorOption] using HEX, HSB & RGB values.
 *
 * @see HexColorPicker
 * @see HsvColorPicker
 * @see RgbColorPicker
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomColorPicker(
    modifier: Modifier = Modifier,
    selectedColorOption: ColorOption,
    onSelect: (ColorOption) -> Unit,
) {

    val focusManager = LocalFocusManager.current

    val selectedColor = selectedColorOption.colorPreferenceEntry.lightColor()
    val selectedColorCompose = Color(selectedColor)

    val textFieldValue = remember {
        mutableStateOf(
            TextFieldValue(
                text = intColorToColorString(color = selectedColor),
            )
        )
    }

    Column(modifier = modifier) {

        PreferenceGroup(
            heading = stringResource(id = R.string.hex),
            modifier = Modifier.padding(top = 8.dp),
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {

                Box(
                    modifier = Modifier
                        .requiredSize(48.dp)
                        .clip(CircleShape)
                        .background(selectedColorCompose),
                )

                Spacer(modifier = Modifier.requiredWidth(16.dp))

                HexColorPicker(
                    textFieldValue = textFieldValue.value,
                    onTextFieldValueChange = { newValue ->
                        val newText = newValue.text.removePrefix("#").take(6).uppercase()
                        textFieldValue.value = newValue.copy(text = newText)
                        val newColor = colorStringToIntColor(colorString = newText)
                        if (newColor != null) {
                            onSelect(ColorOption.CustomColor(newColor))
                        }
                    },
                )

            }
        }

        val pagerState = rememberPagerState(0)
        val scope = rememberCoroutineScope()
        val scrollToPage =
            { page: Int -> scope.launch { pagerState.animateScrollToPage(page) } }

        PreferenceGroup(
            heading = stringResource(id = R.string.color_sliders),
            modifier = Modifier.padding(top = 8.dp),
        ) {

            Column {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                ) {
                    Chip(
                        label = stringResource(id = R.string.hsb),
                        onClick = { scrollToPage(0) },
                        currentOffset = pagerState.currentPage + pagerState.currentPageOffset,
                        page = 0,
                    )
                    Chip(
                        label = stringResource(id = R.string.rgb),
                        onClick = { scrollToPage(1) },
                        currentOffset = pagerState.currentPage + pagerState.currentPageOffset,
                        page = 1,
                    )
                }

                HorizontalPager(
                    modifier = Modifier.animateContentSize(),
                    count = 2,
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                ) { page ->
                    when (page) {
                        0 -> {
                            HsvColorPicker(
                                selectedColor = selectedColor,
                                onSelectedColorChange = {
                                    textFieldValue.value =
                                        textFieldValue.value.copy(
                                            text = intColorToColorString(
                                                selectedColor,
                                            ),
                                        )
                                },
                                onSliderValuesChange = { newColor ->
                                    focusManager.clearFocus()
                                    onSelect(newColor)
                                }
                            )
                        }
                        1 -> {
                            RgbColorPicker(
                                selectedColor = selectedColor,
                                onSelectedColorChange = {
                                    textFieldValue.value =
                                        textFieldValue.value.copy(
                                            text = intColorToColorString(selectedColor),
                                        )
                                },
                                onSliderValuesChange = { newColor ->
                                    focusManager.clearFocus()
                                    onSelect(newColor)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HexColorPicker(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
) {

    val focusManager = LocalFocusManager.current

    val invalidString = colorStringToIntColor(textFieldValue.text) == null

    OutlinedTextField(
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            textAlign = TextAlign.Start,
        ),
        isError = invalidString,
        value = textFieldValue,
        onValueChange = onTextFieldValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            },
        ),
        trailingIcon = {
            Crossfade(targetState = invalidString) {
                if (it) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_warning),
                        contentDescription = stringResource(id = R.string.invalid_color),
                    )
                }
            }
        },
    )
}

@Composable
private fun HsvColorPicker(
    selectedColor: Int,
    onSelectedColorChange: () -> Unit,
    onSliderValuesChange: (ColorOption.CustomColor) -> Unit,
) {

    val hue = remember { mutableStateOf(intColorToHsvColorArray(selectedColor)[0]) }
    val saturation = remember { mutableStateOf(intColorToHsvColorArray(selectedColor)[1]) }
    val brightness = remember { mutableStateOf(intColorToHsvColorArray(selectedColor)[2]) }

    DividerColumn {

        HsbColorSlider(
            type = HsbSliderType.HUE,
            value = hue.value,
            onValueChange = { newValue ->
                hue.value = newValue
            },
        )
        HsbColorSlider(
            type = HsbSliderType.SATURATION,
            value = saturation.value,
            onValueChange = { newValue ->
                saturation.value = newValue
            },
        )
        HsbColorSlider(
            type = HsbSliderType.BRIGHTNESS,
            value = brightness.value,
            onValueChange = { newValue ->
                brightness.value = newValue
            },
        )

        LaunchedEffect(key1 = selectedColor) {
            val hsv = intColorToHsvColorArray(selectedColor)
            hue.value = hsv[0]
            saturation.value = hsv[1]
            brightness.value = hsv[2]
            onSelectedColorChange()
        }

        LaunchedEffect(
            key1 = hue.value,
            key2 = saturation.value,
            key3 = brightness.value,
        ) {
            onSliderValuesChange(
                ColorOption.CustomColor(
                    hsvValuesToIntColor(
                        hue = hue.value,
                        saturation = saturation.value,
                        brightness = brightness.value,
                    ),
                ),
            )
        }

    }

}

@Composable
private fun RgbColorPicker(
    selectedColor: Int,
    selectedColorCompose: Color = Color(selectedColor),
    onSelectedColorChange: () -> Unit,
    onSliderValuesChange: (ColorOption.CustomColor) -> Unit,
) {

    val red = remember { mutableStateOf(selectedColor.red) }
    val green = remember { mutableStateOf(selectedColor.green) }
    val blue = remember { mutableStateOf(selectedColor.blue) }

    DividerColumn {

        RgbColorSlider(
            label = stringResource(id = R.string.rgb_red),
            value = red.value,
            colorStart = selectedColorCompose.copy(red = 0f),
            colorEnd = selectedColorCompose.copy(red = 1f),
            onValueChange = { newValue ->
                red.value = newValue.toInt()
            },
        )
        RgbColorSlider(
            label = stringResource(id = R.string.rgb_green),
            value = green.value,
            colorStart = selectedColorCompose.copy(green = 0f),
            colorEnd = selectedColorCompose.copy(green = 1f),
            onValueChange = { newValue ->
                green.value = newValue.toInt()
            },
        )
        RgbColorSlider(
            label = stringResource(id = R.string.rgb_blue),
            value = blue.value,
            colorStart = selectedColorCompose.copy(blue = 0f),
            colorEnd = selectedColorCompose.copy(blue = 1f),
            onValueChange = { newValue ->
                blue.value = newValue.toInt()
            },
        )

        LaunchedEffect(key1 = selectedColor) {
            red.value = selectedColor.red
            green.value = selectedColor.green
            blue.value = selectedColor.blue
            onSelectedColorChange()
        }

        LaunchedEffect(
            key1 = red.value,
            key2 = green.value,
            key3 = blue.value,
        ) {
            onSliderValuesChange(
                ColorOption.CustomColor(
                    android.graphics.Color.argb(
                        255,
                        red.value,
                        green.value,
                        blue.value,
                    )
                )
            )
        }
    }

}