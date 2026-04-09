package dev.blend.util.render

import FontResource

object ResourceManager {

    @JvmStatic
    fun init() {
        FontResources.init()
        ImageResources.init()
    }

    object FontResources {
        lateinit var regular: FontResource
        fun init() {
            regular = FontResource("regular")
        }
    }

    object ImageResources {
        fun init() {

        }
    }

}