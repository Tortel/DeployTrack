/*
 * Copyright (C) 2013-2023 Scott Warner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tortel.deploytrack.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.database.Exclude
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Entity
@TypeConverters(DateConverter::class)
class Deployment : Comparable<Deployment> {
    var name: String? = null
    var startDate: Date? = null
    var endDate: Date? = null
    var completedColor = 0
    var remainingColor = 0

    @PrimaryKey
    var uuid = UUID.randomUUID().toString()

    /**
     * Update the fields to match the other deployment
     */
    fun updateData(other: Deployment) {
        name = other.name
        startDate = other.startDate
        endDate = other.endDate
        completedColor = other.completedColor
        remainingColor = other.remainingColor
    }

    override fun compareTo(other: Deployment): Int {
        /*
         * Compare start dates, and if they are the same, use end dates
         */
        val startCompare = startDate!!.compareTo(other.startDate)
        return if (startCompare == 0) {
            endDate!!.compareTo(other.endDate)
        } else startCompare
    }

    override fun toString(): String {
        return "[Deployment $uuid $name]"
    }

    @get:Exclude
    @get:Ignore
    val formattedStart: String
        get() {
            if (Companion.format == null) {
                this.format
            }
            return Companion.format!!.format(startDate!!)
        }

    @get:Exclude
    @get:Ignore
    val formattedEnd: String
        get() {
            if (Companion.format == null) {
                this.format
            }
            return Companion.format!!.format(endDate!!)
        }

    @get:SuppressLint("SimpleDateFormat")
    private val format: Unit
        private get() {
            Companion.format = SimpleDateFormat("MMM dd, yyyy")
        }

    @get:Exclude
    @get:Ignore
    val start: DateTime
        get() = DateTime(startDate)

    @get:Exclude
    @get:Ignore
    val end: DateTime
        get() = DateTime(endDate)

    /**
     * Returns the length of the deployment, in days
     *
     * @return the length
     */
    @get:Exclude
    @get:Ignore
    val length: Int
        get() = Days.daysBetween(start, end).days

    /**
     * Get the number of days completed so far
     */
    @get:Exclude
    @get:Ignore
    val completed: Int
        get() {
            val start = start
            //Check if it has even started
            return if (start.isAfterNow) {
                0
            } else Days.daysBetween(start, DateTime()).days.coerceAtMost(length)
        }

    /**
     * Get the remaining time, in days
     */
    @get:Exclude
    @get:Ignore
    val remaining: Int
        get() = length - completed

    /**
     * Gets the percentage completed, as a whole number (0-100)
     */
    @get:Exclude
    @get:Ignore
    val percentage: Int
        get() = (completed.toDouble() / length.toDouble() * 100).toInt()

    @Ignore
    @Exclude
    fun setUuid(uuid: UUID) {
        this.uuid = uuid.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Deployment) {
            return uuid == other.uuid
        }
        return false
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    companion object {
        private var format: SimpleDateFormat? = null
    }
}

internal object DateConverter {
    @JvmStatic
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @JvmStatic
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}