/*
 * Copyright 2021 SOUP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package soup.movie.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.ChronoUnit
import soup.movie.ext.isBest
import soup.movie.model.Movie
import soup.movie.model.repository.MoopRepository
import soup.movie.notification.NotificationBuilder
import soup.movie.util.currentTime
import soup.movie.util.plusDaysTo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class LegacyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MoopRepository,
    private val notificationBuilder: NotificationBuilder
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("doWork: start!")
        return try {
            val movieList = getRecommendedMovieList()
            if (movieList.isNotEmpty()) {
                notificationBuilder.showLegacyNotification(movieList)
            }
            Result.success()
        } catch (t: Throwable) {
            Timber.w(t)
            if (runAttemptCount <= 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun getRecommendedMovieList(): List<Movie> {
        return withContext(Dispatchers.IO) {
            repository.updateAndGetNowMovieList().asSequence()
                .filter {
                    it.theater.run {
                        cgv != null && lotte != null && megabox != null
                    }
                }
                .filterIndexed { index, movie -> index < 3 || movie.isBest() }
                .take(6)
                .toList()
        }
    }

    companion object {

        private const val DEBUG = false
        private const val TAG = "legacy"

        fun enqueueWork(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, createRequest())
        }

        private fun createRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<LegacyWorker>()
                .setInitialDelay(calculateInitialDelayMinutes(), TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
        }

        private fun calculateInitialDelayMinutes(): Long {
            if (DEBUG) {
                return 0
            }
            val current = currentTime()
            val rebirth = current
                .withHour(14)
                .plusDaysTo(DayOfWeek.FRIDAY)
                .plusWeeks(3)
            return current.until(rebirth, ChronoUnit.MINUTES)
        }
    }
}
