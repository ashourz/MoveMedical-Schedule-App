/*
 * Copyright 2023 Zakaraya Thomas Ashour
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.movemedicalscheduleapp.data.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movemedicalscheduleapp.data.dao.AppointmentDao
import com.example.movemedicalscheduleapp.data.entity.Appointment
import com.example.movemedicalscheduleapp.data.entity.ApptLocation
import com.example.movemedicalscheduleapp.extensions.toSQLLong
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AppointmentDaoTest: TestCase() {
    private lateinit var dao: AppointmentDao
    private lateinit var db: ScheduleDatabase
    private var typeConverter: TypeConverter = TypeConverter()

    @get:Rule
    val instantTasExecutorRule = InstantTaskExecutorRule()

    @Before
    public override fun setUp() {
        super.setUp()
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context = context, ScheduleDatabase::class.java).build()
        dao = db.appointmentDao()
    }

    @After
    @Throws(IOException::class)
    public override fun tearDown() {
        dao.deleteAll()
        db.close()
    }

    @Test
    fun upsertNewRead() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllTodayAppointments().first()
        assertThat(resultList.any{ it == correctedAppt }).isTrue()
    }

    @Test
    fun upsertExistingRead() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val initialAppt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(initialAppt)
        assertThat(resultId >= 0).isTrue()
        val changedAppt = initialAppt.copy(
            rowid = resultId,
            location = ApptLocation.ORLANDO
        )
        val updateResultId = dao.upsertAppointment(changedAppt)
        assertThat(updateResultId == -1L).isTrue()
        val resultList = dao.getAllTodayAppointments().first()
        assertThat(resultList.any{ it == changedAppt }).isTrue()


    }


    @Test
    fun upsertDeleteRead() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList1 = dao.getAllTodayAppointments().first()
        assertThat(resultList1.any{ it == correctedAppt }).isTrue()

        val deleteCount = dao.deleteAppointment(
            appt.copy(
                rowid = resultId
            )
        )
        assertThat(deleteCount == 1).isTrue()
        val resultList2 = dao.getAllTodayAppointments().first()
        assertThat(resultList2.none { it == correctedAppt }).isTrue()
    }

    @Test
    fun deleteInvalid() = runTest {
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val deleteCount = dao.deleteAppointment(appt)
        assertThat(deleteCount == 0).isTrue()
        val resultList2 = dao.getAllTodayAppointments().first()
        assertThat(resultList2.none { it == appt }).isTrue()
    }

    @Test
    fun deleteAll() = runTest {
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val deleteCount = dao.deleteAll()
        val resultList2 = dao.getAllTodayAppointments().first()
        assertThat(resultList2.none { it == appt }).isTrue()
    }

    @Test
    fun upsertMultipleGetAll() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt1 = Appointment(
            title = "Test Title 1",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description 1"
        )
        val resultId1 = dao.upsertAppointment(appt1)
        assertThat(resultId1 >= 0).isTrue()
        val correctedAppt1 = appt1.copy(
            rowid = resultId1
        )
        val appt2 = Appointment(
            title = "Test Title 2",
            datetime = sanitizedDateTime,
            location = ApptLocation.ORLANDO,
            duration = Duration.ofMinutes(15L),
            description = "Test Description 2"
        )
        val resultId2 = dao.upsertAppointment(appt2)
        assertThat(resultId2 >= 0).isTrue()
        val correctedAppt2 = appt2.copy(
            rowid = resultId2
        )
        val resultList = dao.getAllTodayAppointments().first()
            assertThat(resultList.containsAll(listOf(correctedAppt1, correctedAppt2)
            )).isTrue()

    }

    @Test
    fun upsertGetTodayAppt() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllTodayAppointments().first()
        assertThat(resultList.any{ it == correctedAppt }).isTrue()
    }

    @Test
    fun upsertGetTodayApptInvalid() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().plusDays(1L))
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllTodayAppointments().first()
        assertThat(resultList.any{
            it == correctedAppt
        }).isFalse()
    }

    @Test
    fun upsertGetFutureAppt() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().plusDays(1L))
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllFutureAppointments().first()
        assertThat(resultList.any{
            it == correctedAppt
        }).isTrue()
    }

    @Test
    fun upsertGetFutureApptInvalid() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllFutureAppointments().first()
        assertThat(resultList.any{
            it == correctedAppt
        }).isFalse()
    }

    @Test
    fun upsertGetPastAppt() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().minusDays(1L))
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllPastAppointments().first()
        assertThat(resultList.any{
            it == correctedAppt
        }).isTrue()
    }

    @Test
    fun upsertGetPastApptInvalid() = runTest{
        val sanitizedDateTime = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId = dao.upsertAppointment(appt)
        assertThat(resultId >= 0).isTrue()
        val correctedAppt = appt.copy(
            rowid = resultId
        )
        val resultList = dao.getAllPastAppointments().first()
        assertThat(resultList.any{
            it == correctedAppt
        }).isFalse()
    }

    @Test
    fun partialOverlappingAppointments() = runTest{
        //Insert Appt 1
        val sanitizedDateTime1 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt1 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime1,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId1 = dao.upsertAppointment(appt1)
        assertThat(resultId1 >= 0).isTrue()
        val correctedAppt1 = appt1.copy(
            rowid = resultId1
        )
       //Prep Appt 2
        val sanitizedDateTime2 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().minusMinutes(10L))
        )!!
        val appt2 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime2,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        //Confirm Overlap
        val resultList2 = dao.getOverlappingAppointments(
            rowId = appt2.rowid,
            locationInt = appt2.location.zipCode,
            apptStartSQLLong = appt2.datetime.toSQLLong(),
            apptEndSQLLong = appt2.datetime.plus(appt2.duration).toSQLLong()
        )
        assertThat(resultList2.any{
            it == correctedAppt1
        }).isTrue()
    }

    @Test
    fun fullOverlappingAppointments() = runTest{
        //Insert Appt1
        val sanitizedDateTime1 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt1 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime1,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId1 = dao.upsertAppointment(appt1)
        assertThat(resultId1 >= 0).isTrue()
        val correctedAppt1 = appt1.copy(
            rowid = resultId1
        )
        //Prep Appt 2
        val sanitizedDateTime2 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().minusMinutes(10L))
        )!!
        val appt2 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime2,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(65L),
            description = "Test Description"
        )
        //Confirm Overlap
        val resultList2 = dao.getOverlappingAppointments(
            rowId = appt2.rowid,
            locationInt = appt2.location.zipCode,
            apptStartSQLLong = appt2.datetime.toSQLLong(),
            apptEndSQLLong = appt2.datetime.plus(appt2.duration).toSQLLong()
        )
        assertThat(resultList2.any{
            it == correctedAppt1
        }).isTrue()
    }

    @Test
    fun noOverlappingAppointments() = runTest{
        //Insert Appt1
        val sanitizedDateTime1 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now())
        )!!
        val appt1 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime1,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(45L),
            description = "Test Description"
        )
        val resultId1 = dao.upsertAppointment(appt1)
        assertThat(resultId1 >= 0).isTrue()
        val correctedAppt1 = appt1.copy(
            rowid = resultId1
        )
        //Prep Appt 2
        val sanitizedDateTime2 = typeConverter.longToLocalDateTime(
            typeConverter.localDateTimeToLong(LocalDateTime.now().plusHours(2L))
        )!!
        val appt2 = Appointment(
            title = "Test Title",
            datetime = sanitizedDateTime2,
            location = ApptLocation.DALLAS,
            duration = Duration.ofMinutes(65L),
            description = "Test Description"
        )
        //Confirm Overlap
        val resultList2 = dao.getOverlappingAppointments(
            rowId = appt2.rowid,
            locationInt = appt2.location.zipCode,
            apptStartSQLLong = appt2.datetime.toSQLLong(),
            apptEndSQLLong = appt2.datetime.plus(appt2.duration).toSQLLong()
        )
        assertThat(resultList2.none{
            it == correctedAppt1
        }).isTrue()
    }
}