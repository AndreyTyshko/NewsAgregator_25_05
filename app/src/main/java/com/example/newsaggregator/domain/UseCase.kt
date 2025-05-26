package com.example.newsaggregator.domain


import com.example.newsaggregator.AppModule
import com.example.newsaggregator.data.rss.dto.RssDto
import javax.inject.Inject

class UseCase @Inject constructor(
   private val app: AppModule
) {

   suspend fun getData(): RssDto {
      return app.getRssDTO()
   }


}