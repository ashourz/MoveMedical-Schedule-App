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

package com.example.movemedicalscheduleapp.ui.components.cards

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.movemedicalscheduleapp.R
import com.example.movemedicalscheduleapp.data.entity.Appointment
import com.example.movemedicalscheduleapp.data.entity.ApptLocation
import com.example.movemedicalscheduleapp.ui.theme.MoveMedicalScheduleAppTheme
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class AppointmentCardKtTest{
    private lateinit var activity: Activity

    private val appointmentTitle = "TEST TITLE"
    private val appointmentDateTime = LocalDateTime.now()
    private val appointmentDuration = Duration.ofHours(2L)
    private val appointmentLocation = ApptLocation.PARK_CITY
    private val appointmentDescription = "THIS IS A DESCRIPTION"
    private val appointment: Appointment = Appointment(
        title = appointmentTitle,
        datetime = appointmentDateTime,
        duration = appointmentDuration,
        location = appointmentLocation,
        description = appointmentDescription
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun before() {
        activity = composeTestRule.activity
    }

    @Test
    fun unexpandedCardClicked(){
        var cardClicked = false
        var updateClicked = false
        var cancelClicked = false
        // Start the app
        composeTestRule.setContent {
            MoveMedicalScheduleAppTheme() {
                AppointmentCard(
                    appointment = appointment,
                    expanded = false,
                    onExpandAppointmentCard = { cardClicked = true},
                    onUpdateAppointment = {updateClicked = true},
                    onDeleteAppointment = {cancelClicked = true}
                )
            }
        }
        val card = composeTestRule.onNodeWithText(appointmentTitle).assertIsDisplayed().assertHasClickAction()
        card.performClick()
        Truth.assertThat(cardClicked && !updateClicked && !cancelClicked).isTrue()
    }

    @Test
    fun expandedCardClicked(){
        var cardClicked = false
        var updateClicked = false
        var cancelClicked = false
        // Start the app
        composeTestRule.setContent {
            MoveMedicalScheduleAppTheme() {
                AppointmentCard(
                    appointment = appointment,
                    expanded = true,
                    onExpandAppointmentCard = { cardClicked = true},
                    onUpdateAppointment = {updateClicked = true},
                    onDeleteAppointment = {cancelClicked = true}
                )
            }
        }
        val updateLabel = activity.getString(R.string.update_text)
        val deleteLabel = activity.getString(R.string.delete_text)
        val card = composeTestRule.onNodeWithText(appointmentTitle).assertIsDisplayed().assertHasClickAction()
        val update = composeTestRule.onNodeWithText(updateLabel).assertIsDisplayed().assertHasClickAction()
        val delete = composeTestRule.onNodeWithText(deleteLabel).assertIsDisplayed().assertHasClickAction()

        card.performClick()
        Truth.assertThat(cardClicked && !updateClicked && !cancelClicked).isTrue()
    }

    @Test
    fun expandedCardCancelClicked(){
        var cardClicked = false
        var updateClicked = false
        var cancelClicked = false
        // Start the app
        composeTestRule.setContent {
            MoveMedicalScheduleAppTheme() {
                AppointmentCard(
                    appointment = appointment,
                    expanded = true,
                    onExpandAppointmentCard = { cardClicked = true},
                    onUpdateAppointment = {updateClicked = true},
                    onDeleteAppointment = {cancelClicked = true}
                )
            }
        }
        val updateLabel = activity.getString(R.string.update_text)
        val deleteLabel = activity.getString(R.string.delete_text)
        val card = composeTestRule.onNodeWithText(appointmentTitle).assertIsDisplayed().assertHasClickAction()
        val update = composeTestRule.onNodeWithText(updateLabel).assertIsDisplayed().assertHasClickAction()
        val delete = composeTestRule.onNodeWithText(deleteLabel).assertIsDisplayed().assertHasClickAction()

        delete.performClick()
        Truth.assertThat(!cardClicked && !updateClicked && cancelClicked).isTrue()
    }

    @Test
    fun expandedCardUpdateClicked(){
        var cardClicked = false
        var updateClicked = false
        var cancelClicked = false
        // Start the app
        composeTestRule.setContent {
            MoveMedicalScheduleAppTheme() {
                AppointmentCard(
                    appointment = appointment,
                    expanded = true,
                    onExpandAppointmentCard = { cardClicked = true},
                    onUpdateAppointment = {updateClicked = true},
                    onDeleteAppointment = {cancelClicked = true}
                )
            }
        }
        val updateLabel = activity.getString(R.string.update_text)
        val deleteLabel = activity.getString(R.string.delete_text)
        val card = composeTestRule.onNodeWithText(appointmentTitle).assertIsDisplayed().assertHasClickAction()
        val update = composeTestRule.onNodeWithText(updateLabel).assertIsDisplayed().assertHasClickAction()
        val delete = composeTestRule.onNodeWithText(deleteLabel).assertIsDisplayed().assertHasClickAction()

        update.performClick()
        Truth.assertThat(!cardClicked && updateClicked && !cancelClicked).isTrue()
    }
}