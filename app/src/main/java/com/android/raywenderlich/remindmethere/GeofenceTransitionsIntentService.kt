package com.android.raywenderlich.remindmethere

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class GeofenceTransitionsIntentService : IntentService("GeoTrIntentService") {

  companion object {
    private const val LOG_TAG = "GeoTrIntentService"
  }

  override fun onHandleIntent(intent: Intent?) {
    val geofencingEvent = GeofencingEvent.fromIntent(intent)
    if (geofencingEvent.hasError()) {
      val errorMessage = GeofenceErrorMessages.getErrorString(this,
          geofencingEvent.errorCode)
      Log.e(LOG_TAG, errorMessage)
      return
    }

    handleEvent(geofencingEvent)
  }

  private fun handleEvent(event: GeofencingEvent) {
    if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
      val reminder = getFirstReminder(event.triggeringGeofences)
      val message = reminder?.message
      val latLng = reminder?.latLng
      if (message != null && latLng != null) {
        sendNotification(this, message, latLng)
      }

      val database = FirebaseDatabase.getInstance()
      val myRef = database.getReference(message.toString())
      val sdf = SimpleDateFormat("hh:mm:ss dd/M/yyyy")
      val currentDate = sdf.format(Date())
      myRef.setValue(currentDate)
    }
  }

  private fun getFirstReminder(triggeringGeofences: List<Geofence>): Reminder? {
    val firstGeofence = triggeringGeofences[0]
    return (application as ReminderApp).getRepository().get(firstGeofence.requestId)
  }
}