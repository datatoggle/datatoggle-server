package com.datatoggle.server.tools

import java.io.InputStream

class FileLoader {

    companion object {
        fun getFileFromResource(fileName: String): InputStream {
            val classLoader: ClassLoader = FileLoader::class.java.classLoader
            val resource: InputStream = classLoader.getResourceAsStream(fileName)!!
            return resource
        }
    }
}
