package stan.qodat.scene.runescape.entity

import stan.qodat.cache.Cache
import stan.qodat.cache.CacheEncoder
import stan.qodat.cache.definition.ObjectDefinition
import stan.qodat.cache.impl.oldschool.OldschoolCacheRuneLite
import java.io.UnsupportedEncodingException

/**
 * TODO: add documentation
 *
 * @author  Stan van der Bend (https://www.rune-server.ee/members/StanDev/)
 * @since   28/01/2021
 */
class Object(
    cache: Cache = OldschoolCacheRuneLite,
    definition: ObjectDefinition
) : AnimatedEntity<ObjectDefinition>(cache, definition), CacheEncoder {

    override fun encode(format: Cache) {
        throw UnsupportedEncodingException()
    }
}