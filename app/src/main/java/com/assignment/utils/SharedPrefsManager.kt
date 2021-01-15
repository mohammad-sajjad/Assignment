package com.assignment.utils


object SharedPrefsManager {

    private const val prefsName: String = "prefsName"

    private enum class StorageKey {
        IsLoggedIn,
        Username,
        Email,
        Image
    }

    //IsLoggedIn
    fun setIsLoggedIn(value: Boolean) {
        setSharedPrefs(StorageKey.IsLoggedIn.toString(), value)
    }

    fun getIsLoggedIn(): Boolean {
        return getSharePrefs(StorageKey.IsLoggedIn.toString(), false)
    }

    fun setUserName(value: String) {
        setSharedPrefs(StorageKey.Username.toString(), value)
    }

    fun getUserName(): String = getSharePrefs(StorageKey.Username.toString())


    fun setUserEmail(value: String) {
        setSharedPrefs(StorageKey.Email.toString(), value)
    }

    fun getUserEmail(): String = getSharePrefs(StorageKey.Email.toString())

    fun setUserImage(value: String) {
        setSharedPrefs(StorageKey.Image.toString(), value)
    }

    fun getUserImage(): String = getSharePrefs(StorageKey.Image.toString())

    private fun<T: Any> setSharedPrefs(key: String, value: T) {
        val sharedPreferences = AppController.get().getProperContext().getSharedPreferences(prefsName, 0)
        val editor = sharedPreferences.edit()
        when(value) {
            is String   -> editor.putString(key, value as String)
            is Int      -> editor.putInt(key, value as Int)
            is Boolean  -> editor.putBoolean(key, value as Boolean)
            is Float    -> editor.putFloat(key, value as Float)
            is Long    -> editor.putLong(key, value as Long)
        }
        editor.apply()
    }

    private inline fun<reified T: Any> getSharePrefs(key: String, defaultValue: T? = null): T {
        val sharedPreferences = AppController.get().getProperContext().getSharedPreferences(prefsName, 0)
        return when(T::class) {
            String::class   -> sharedPreferences.getString(key, defaultValue.toString()) as T
            Int::class      -> sharedPreferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class  -> sharedPreferences.getBoolean(key, false) as T
            Float::class    -> sharedPreferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class     -> sharedPreferences.getLong(key, defaultValue as? Long ?: -1) as T
            else            -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    private fun removeSharedPrefs(key: String) {
        val sharedPreferences = AppController.get().getProperContext().getSharedPreferences(prefsName, 0)
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clearCache() {
        SharedPrefsManager.setIsLoggedIn(false)
    }

}