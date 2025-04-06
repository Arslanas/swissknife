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
