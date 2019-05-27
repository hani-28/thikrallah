package com.HMSolutions.thikrallah.quran.common.search

import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import com.HMSolutions.thikrallah.quran.common.search.arabic.ArabicCharacterHelper
import java.util.regex.Pattern

class ArabicSearcher(private val defaultSearcher: Searcher,
                     private val matchStart: String,
                     private val matchEnd: String) : Searcher {
  override fun getQuery(withSnippets: Boolean,
                        hasFTS: Boolean,
                        table: String,
                        columns: String,
                        searchColumn: String): String {
    return defaultSearcher.getQuery(false, hasFTS, table, columns, searchColumn)
  }

  override fun getLimit(withSnippets: Boolean): String {
    return if (withSnippets) { "" } else { "LIMIT 250" }
  }

  override fun processSearchText(searchText: String, hasFTS: Boolean): String {
    return defaultSearcher.processSearchText(searchText.replace(arabicRegex, "_"), false)
  }

  override fun runQuery(database: SQLiteDatabase,
                        query: String,
                        searchText: String,
                        originalSearchText: String,
                        withSnippets: Boolean,
                        columns: Array<String>): Cursor {
    val matrixCursor = MatrixCursor(columns)

    val regexp = ArabicCharacterHelper.generateRegex(originalSearchText)
    val pattern = Pattern.compile(regexp)
    database.rawQuery(query, arrayOf(searchText))?.use { cursor ->
      while (cursor.moveToNext()) {
        val text = cursor.getString(3)

        val matcher = pattern.matcher(text)
        if (matcher.find()) {
          val matchText: String = if (withSnippets) {
            text.replace("($regexp)".toRegex(), "$matchStart$1$matchEnd")
          } else {
            text
          }

          matrixCursor.addRow(arrayOf(cursor.getInt(0),
              cursor.getInt(1),
              cursor.getInt(2),
              matchText))
        }
      }
    }

    return matrixCursor
  }

  companion object {
    private val arabicRegex =  "[\u0627\u0623\u0621\u062a\u0629\u0647\u0649]".toRegex()
  }
}
