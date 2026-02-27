package com.weatherdrive.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Wraps the SQLDelight-generated [Downloads] database, providing typed CRUD
 * operations for persisting favourite show IDs.
 */
class FavouriteDatabase(driver: SqlDriver) {
    private val db = Downloads(driver)

    fun insert(showId: Long, title: String) {
        db.favouriteShowsQueries.insertFavouriteShow(showId = showId, title = title)
    }

    fun delete(showId: Long) {
        db.favouriteShowsQueries.deleteFavouriteShow(showId)
    }

    fun getAll(): Set<Long> {
        return db.favouriteShowsQueries.getAllFavouriteShows().executeAsList().toSet()
    }

    fun isFavourite(showId: Long): Boolean {
        return db.favouriteShowsQueries.isFavouriteShow(showId).executeAsOne() > 0
    }
}
