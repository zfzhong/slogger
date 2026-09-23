package com.application.sloggerlib

import android.content.Context
import android.hardware.SensorManager

/**
 * The magnetic field in the device's own frame, in microtesla.
 *
 * Written as <device>_<protocol>_Mag_<rate>_<seq>_<expId>_<wallMs>.csv, so it
 * sorts and parses exactly like the accelerometer's files.
 *
 * Two reasons it is worth a stream of its own.
 *
 * HEADING. Gravity fixes two of the three angles of an orientation and says
 * nothing about the third: an accelerometer cannot tell north from east. The
 * magnetic field supplies that last degree of freedom, and it is the only way
 * two devices' orientations can be compared in one Earth frame rather than
 * each in its own.
 *
 * PROXIMITY. A tablet is not magnetically quiet. Measured on the tablets in
 * experiments 20-22, their own magnetometers read 104-202 uT where the Earth
 * field here is about 50 uT, because of the magnets for the dock and the
 * speakers. A watch approaching one is approaching a magnet, and a dipole
 * field falls off as 1/r^3 - far more sharply than radio, which is what makes
 * this interesting next to BLE rather than redundant with it.
 *
 * Logged at the accelerometer's rate and tied to it, like linear acceleration:
 * the field is slow, but a stream that is not sampled with the others cannot
 * be compared with them sample for sample.
 *
 * What it is NOT for: telling whether a tablet lies flat or stands up. Gravity
 * answers that to a twentieth of a degree (the tablets on the desk in those
 * same sessions read a tilt spread of 0.05 deg), and the magnetometer would
 * answer it worse, through a field bent by whatever steel is in the room.
 */
class SensorMagnetometer(
    context: Context,
    sensorManager: SensorManager,
    type: Int,
    deviceName: String,
    protocol: String,
    expId: String,
    freq: Int,
    maxRecordInFile: Int,
    batchSize: Int,
    isWearable: Boolean
) : GeneralSensor(
    context,
    sensorManager,
    type,
    deviceName,
    protocol,
    expId,
    freq,
    maxRecordInFile,
    batchSize,
    isWearable)
{}
