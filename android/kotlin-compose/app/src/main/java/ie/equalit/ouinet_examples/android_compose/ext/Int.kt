package ie.equalit.ouinet_examples.android_compose.ext

import android.annotation.SuppressLint
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

private fun log2(n: Int): Double {
    return ln(n.toDouble()) / ln(2.0)
}

@SuppressLint("DefaultLocale")
fun Int.bytesToString(): String {
    // originally from <https://stackoverflow.com/a/42408230>
    // ported from extension JS code to kotlin
    if (this == 0) {
        return "0 B"
    }
    val i = floor(log2(this) / 10).toInt()
    val v = this / 1024.0.pow(i)
    val u = "KMGTPEZY"[i - 1] + "iB";
    return String.format("%.2f %s", v, u)
}

