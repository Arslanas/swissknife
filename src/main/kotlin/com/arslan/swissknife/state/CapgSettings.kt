package com.arslan.swissknife.state

import com.arslan.swissknife.enum.SettingsEnum
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "CapgSettings", storages = [Storage("capg_settings.xml")])
class CapgSettings : PersistentStateComponent<CapgSettings.State> {

    data class State(var queries: MutableMap<String, String> = mutableMapOf(),
                     var common: MutableMap<String, String> = SettingsEnum.entries
                         .associate{ it.key to "" }
                         .toMutableMap()
    )

    private var settings = State()

    override fun getState(): State {
        return settings
    }

    override fun loadState(state: State) {
        settings = state

        // Ensure all enum keys exist, even after schema change
        for (enum in SettingsEnum.entries) {
            if (!settings.common.containsKey(enum.key)) {
                settings.common[enum.key] = ""
            }
        }
    }


    // Common setting operations
    fun getCommonSettingsMap(): MutableMap<String, String>{
        return settings.common
    }

    fun updateSettings(newSettings : MutableMap<String, String>){
        settings.common = newSettings
    }

    fun get(enum : SettingsEnum): String? {
        return settings.common[enum.key]
    }

    // Transformers
    private val TRANSFORMER_PREFIX = "TRANSFORMERS."

    fun getTransformerFilePath(id : String) : String? {
        return settings.common[TRANSFORMER_PREFIX + id]
    }

    fun getTransformers() : List<String> {
        return settings.common.keys.filter { it.startsWith(TRANSFORMER_PREFIX) }.map{it.substringAfter(TRANSFORMER_PREFIX)}.sorted()
    }

    fun saveTransformer(id: String, filePath: String) {
        settings.common[TRANSFORMER_PREFIX + id] = filePath
    }


    fun hasTransformer(id: String): Boolean {
        return settings.common.containsKey(TRANSFORMER_PREFIX + id)
    }

    fun deleteTransformer(id: String) {
        settings.common.remove(TRANSFORMER_PREFIX + id)
    }

    // Sql query CRUD operations
    fun saveQuery(id: String, query: String) {
        settings.queries[id] = query
    }

    fun getQuery(id: String): String? {
        return settings.queries[id]
    }

    fun hasQuery(id: String): Boolean {
        return settings.queries.containsKey(id)
    }

    fun getQueryMap(): Map<String, String> {
        return settings.queries.toMap()
    }

    fun deleteQuery(id: String) {
        settings.queries.remove(id)
    }


}
