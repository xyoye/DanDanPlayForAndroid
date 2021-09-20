package com.xyoye.stream_component.utils.web_dav.not_strict

import com.thegrizzlylabs.sardineandroid.model.Prop
import com.thegrizzlylabs.sardineandroid.model.Property
import com.thegrizzlylabs.sardineandroid.model.Property.PropertyConverter
import com.thegrizzlylabs.sardineandroid.model.Resourcetype
import com.thegrizzlylabs.sardineandroid.util.EntityWithAnyElementConverter
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.convert.Registry
import org.simpleframework.xml.convert.RegistryStrategy
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.strategy.Strategy
import org.simpleframework.xml.stream.Format
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream

/**
 * Created by xyoye on 2021/9/20.
 */

object NotStrictSardineUtil {

    @Throws(Exception::class)
    private fun getSerializer(): Serializer {
        val format = Format("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        val registry = Registry()
        val strategy: Strategy = RegistryStrategy(registry)
        val serializer: Serializer = Persister(strategy, format)
        registry.bind(
            Prop::class.java, EntityWithAnyElementConverter(
                serializer,
                Prop::class.java
            )
        )
        registry.bind(
            Resourcetype::class.java, EntityWithAnyElementConverter(
                serializer,
                Resourcetype::class.java
            )
        )
        registry.bind(Property::class.java, PropertyConverter::class.java)
        return serializer
    }

    @Throws(IOException::class)
    fun <T> unmarshal(type: Class<out T>?, inputStream: InputStream?, strict: Boolean): T {
        return try {
            getSerializer().read(type, inputStream, strict)
        } catch (e: SAXException) {
            throw RuntimeException(e.message, e)
        } catch (e: Exception) {
            // Server does not return any valid WebDAV XML that matches our JAXB context
            throw IOException("Not a valid DAV response", e)
        }
    }
}