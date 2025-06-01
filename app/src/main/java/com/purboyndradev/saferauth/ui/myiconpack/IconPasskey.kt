package com.purboyndradev.saferauth.ui.myiconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purboyndradev.saferauth.ui.MyIconPack
import kotlin.Unit

public val MyIconPack.IconPasskey: ImageVector
    get() {
        if (_iconPasskey != null) {
            return _iconPasskey!!
        }
        _iconPasskey = Builder(name = "IconPasskey", defaultWidth = 24.0.dp, defaultHeight =
                24.0.dp, viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            path(fill = SolidColor(Color(0xFFe3e3e3)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(120.0f, 800.0f)
                verticalLineToRelative(-112.0f)
                quadToRelative(0.0f, -34.0f, 17.5f, -62.5f)
                reflectiveQuadTo(184.0f, 582.0f)
                quadToRelative(62.0f, -31.0f, 126.0f, -46.5f)
                reflectiveQuadTo(440.0f, 520.0f)
                quadToRelative(20.0f, 0.0f, 40.0f, 1.5f)
                reflectiveQuadToRelative(40.0f, 4.5f)
                quadToRelative(-4.0f, 58.0f, 21.0f, 109.5f)
                reflectiveQuadToRelative(73.0f, 84.5f)
                verticalLineToRelative(80.0f)
                lineTo(120.0f, 800.0f)
                close()
                moveTo(760.0f, 920.0f)
                lineToRelative(-60.0f, -60.0f)
                verticalLineToRelative(-186.0f)
                quadToRelative(-44.0f, -13.0f, -72.0f, -49.5f)
                reflectiveQuadTo(600.0f, 540.0f)
                quadToRelative(0.0f, -58.0f, 41.0f, -99.0f)
                reflectiveQuadToRelative(99.0f, -41.0f)
                quadToRelative(58.0f, 0.0f, 99.0f, 41.0f)
                reflectiveQuadToRelative(41.0f, 99.0f)
                quadToRelative(0.0f, 45.0f, -25.5f, 80.0f)
                reflectiveQuadTo(790.0f, 670.0f)
                lineToRelative(50.0f, 50.0f)
                lineToRelative(-60.0f, 60.0f)
                lineToRelative(60.0f, 60.0f)
                lineToRelative(-80.0f, 80.0f)
                close()
                moveTo(440.0f, 480.0f)
                quadToRelative(-66.0f, 0.0f, -113.0f, -47.0f)
                reflectiveQuadToRelative(-47.0f, -113.0f)
                quadToRelative(0.0f, -66.0f, 47.0f, -113.0f)
                reflectiveQuadToRelative(113.0f, -47.0f)
                quadToRelative(66.0f, 0.0f, 113.0f, 47.0f)
                reflectiveQuadToRelative(47.0f, 113.0f)
                quadToRelative(0.0f, 66.0f, -47.0f, 113.0f)
                reflectiveQuadToRelative(-113.0f, 47.0f)
                close()
                moveTo(740.0f, 560.0f)
                quadToRelative(17.0f, 0.0f, 28.5f, -11.5f)
                reflectiveQuadTo(780.0f, 520.0f)
                quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
                reflectiveQuadTo(740.0f, 480.0f)
                quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
                reflectiveQuadTo(700.0f, 520.0f)
                quadToRelative(0.0f, 17.0f, 11.5f, 28.5f)
                reflectiveQuadTo(740.0f, 560.0f)
                close()
            }
        }
        .build()
        return _iconPasskey!!
    }

private var _iconPasskey: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MyIconPack.IconPasskey, contentDescription = "")
    }
}
