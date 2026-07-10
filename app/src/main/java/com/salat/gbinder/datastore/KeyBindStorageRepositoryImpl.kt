package com.salat.gbinder.datastore

import com.salat.gbinder.entity.KeyBindConfig
import com.salat.gbinder.entity.KeyBindPattern
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class KeyBindStorageRepositoryImpl(private val dataStore: DataStoreRepository) :
    KeyBindStorageRepository {
    override suspend fun getCode(): String {
        return dataStore.getValueFlow(GeneralPrefs.KEY_BINDS).first() ?: ""
    }

    override suspend fun applyCode(import: String) {
        val importBinds = try {
            parseBinds(import)
        } catch (_: Exception) {
            emptyMap()
        }

        val currentBinds = getBinds()

        val merged = currentBinds + importBinds
        try {
            val jsonString: String = Json.encodeToString(merged)
            dataStore.saveValue(GeneralPrefs.KEY_BINDS, jsonString)
        } catch (_: Exception) {
        }
    }

    private suspend fun getBinds(): Map<String, KeyBindConfig> {
        val bindsJson = dataStore.getValueFlow(GeneralPrefs.KEY_BINDS).first() ?: ""
        return parseBinds(bindsJson)
    }

    override fun parseBinds(bindsJson: String): Map<String, KeyBindConfig> {
        return try {
            Json.decodeFromString(bindsJson)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override suspend fun saveBinds(key: String, config: KeyBindConfig) {
        val currentBinds = getBinds()
        val updated = currentBinds + (key to config)
        try {
            val jsonString: String = Json.encodeToString(updated)
            dataStore.saveValue(GeneralPrefs.KEY_BINDS, jsonString)
        } catch (_: Exception) {
        }
    }

    // Keys edit - move the config to the new pattern keeping the bind list position
    override suspend fun renameBind(oldName: String, newName: String, config: KeyBindConfig) {
        val currentBinds = getBinds()
        if (oldName !in currentBinds) {
            saveBinds(newName, config)
            return
        }
        val updated = buildMap {
            currentBinds.forEach { (name, value) ->
                when (name) {
                    oldName -> put(newName, config)
                    // New pattern was used by another bind - the edited one takes it over
                    newName -> Unit
                    else -> put(name, value)
                }
            }
        }
        try {
            val jsonString: String = Json.encodeToString(updated)
            dataStore.saveValue(GeneralPrefs.KEY_BINDS, jsonString)
        } catch (_: Exception) {
        }
    }

    override suspend fun deleteBind(bindName: String) {
        val currentBinds = getBinds()
        val updated = currentBinds.filter { it.key != bindName }
        try {
            val jsonString: String = Json.encodeToString(updated)
            dataStore.saveValue(GeneralPrefs.KEY_BINDS, jsonString)
        } catch (_: Exception) {
        }
    }

    override fun getBindName(bind: KeyBindPattern): String {
        val bindName = when (bind) {
            is KeyBindPattern.DoubleClick -> "dc"
            is KeyBindPattern.LongPress -> "lp"
            is KeyBindPattern.MultiLong -> "ml"
            is KeyBindPattern.ShortClick -> "sc"
        }
        val keys = when (bind) {
            is KeyBindPattern.DoubleClick -> listOf(bind.keyCode)
            is KeyBindPattern.LongPress -> listOf(bind.keyCode)
            is KeyBindPattern.MultiLong -> bind.keyCodes
            is KeyBindPattern.ShortClick -> listOf(bind.keyCode)
        }
        val keyName = keys
            .sorted()
            .joinToString("+")

        return bindName + keyName
    }

    // Reverse of getBindName - restores the pattern from a stored bind key
    override fun parseBindName(bindName: String): KeyBindPattern? {
        val keys = bindName.drop(2)
            .split("+")
            .mapNotNull { it.toIntOrNull() }

        if (keys.isEmpty()) return null

        return when (bindName.take(2)) {
            "dc" -> KeyBindPattern.DoubleClick(keys.first())
            "lp" -> KeyBindPattern.LongPress(keys.first())
            "ml" -> KeyBindPattern.MultiLong(keys)
            "sc" -> KeyBindPattern.ShortClick(keys.first())
            else -> null
        }
    }
}
