package com.HMSolutions.thikrallah.quran.data.page.provider

import com.HMSolutions.thikrallah.quran.data.page.provider.madani.MadaniPageProvider
import com.HMSolutions.thikrallah.quran.data.source.PageProvider
import com.HMSolutions.thikrallah.quran.page.common.draw.ImageDrawHelper
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
object QuranPageModule {

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("madani")
  fun provideMadaniPageSet(): PageProvider {
    return MadaniPageProvider()
  }

  @JvmStatic
  @Provides
  @ElementsIntoSet
  fun provideImageDrawHelpers(): Set<ImageDrawHelper> {
    return emptySet()
  }
}
