package dev.blend.util.misc

import dev.blend.util.IAccessor
import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

object MiscUtil : IAccessor {

    @JvmStatic
    fun isOver(
        x: Number, y: Number,
        width: Number, height: Number,
        mouseX: Number, mouseY: Number
    ): Boolean {
        return mouseX.toDouble() > x.toDouble() &&
                mouseX.toDouble() < x.toDouble() + width.toDouble() &&
                mouseY.toDouble() > y.toDouble() &&
                mouseY.toDouble() < y.toDouble() + height.toDouble()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getResourceAsByteBuffer(resource: String, bufferSize: Int = 1024): ByteBuffer {

        MiscUtil::class.java.getResourceAsStream("/assets/byte/$resource").use { source ->
            requireNotNull(source) { "Resource not found: $resource" }

            Channels.newChannel(source).use { rbc: ReadableByteChannel ->

                var buffer = BufferUtils.createByteBuffer(bufferSize)

                while (true) {
                    val bytes = rbc.read(buffer)
                    if (bytes == -1) break

                    if (!buffer.hasRemaining()) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2)
                    }
                }

                buffer.flip()
                return buffer
            }
        }
    }

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = BufferUtils.createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }
}