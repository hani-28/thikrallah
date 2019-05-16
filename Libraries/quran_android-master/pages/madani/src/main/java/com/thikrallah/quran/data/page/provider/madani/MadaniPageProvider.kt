package com.thikrallah.quran.data.page.provider.madani

import com.thikrallah.quran.data.pageinfo.common.MadaniDataSource
import com.thikrallah.quran.data.pageinfo.common.size.DefaultPageSizeCalculator
import com.thikrallah.quran.data.source.DisplaySize
import com.thikrallah.quran.data.source.PageProvider
import com.thikrallah.quran.data.source.PageSizeCalculator
import com.thikrallah.quran.labs.androidquran.pages.madani.R

class MadaniPageProvider : PageProvider {
  private val baseUrl = "https://android.quran.com/data"
  private val dataSource = MadaniDataSource()

  override fun getDataSource() = dataSource

  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      DefaultPageSizeCalculator(displaySize)

  override fun getImageVersion() = 6

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  override fun getAyahInfoDirectoryName() = getDatabaseDirectoryName()

  override fun getDatabasesBaseUrl() = "$baseUrl/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = ""

  override fun getPreviewTitle() = R.string.madani_title

  override fun getPreviewDescription() = R.string.madani_description
}
