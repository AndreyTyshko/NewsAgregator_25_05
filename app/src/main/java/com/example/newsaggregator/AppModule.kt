package com.example.newsaggregator


import com.example.newsaggregator.data.rss.dto.RssDto
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)

class AppModule @Inject constructor(

)  {

   private fun getRetrofit() = Retrofit.Builder()
        .baseUrl("https://www.theguardian.com")
        .addConverterFactory(
            XML.asConverterFactory(
                "application/xml; charset=UTF8".toMediaType()
            )
        ).build()

    interface RssFeed {
        @GET("/{query}/rss")
        suspend fun getRss(
            @Path("query") query: String = "international"
        ): RssDto
    }


    private fun provideRssFeed(): RssFeed = getRetrofit()!!.create(RssFeed::class.java)

    @Provides
    suspend fun getRssDTO(): RssDto {
        return provideRssFeed().getRss()
    }

}