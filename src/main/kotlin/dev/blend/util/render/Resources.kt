import com.syuto.bytes.Byte
import dev.blend.util.misc.MiscUtil
import dev.blend.util.render.DrawUtil
import org.lwjgl.nanovg.NanoVG
import java.nio.ByteBuffer

class FontResource(fontName: String) {

    private val resource: ByteBuffer = MiscUtil.getResourceAsByteBuffer("fonts/regular.ttf")

    val identifier: String = fontName

    init {
        val handle = NanoVG.nvgCreateFontMem(
            DrawUtil.context,
            identifier,
            resource,
            true
        )

        if (handle == -1) {
            Byte.LOGGER.info("CRITICAL ERROR: NanoVG failed to load font $identifier from memory.")
        } else {
            Byte.LOGGER.info("Loaded font $identifier")
        }

    }
}