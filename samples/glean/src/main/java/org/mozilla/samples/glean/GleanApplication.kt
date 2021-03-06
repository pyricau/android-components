/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.glean

import android.app.Application
import android.content.Intent
import mozilla.components.lib.fetch.httpurlconnection.HttpURLConnectionClient
import mozilla.components.service.glean.Glean
import mozilla.components.service.glean.config.Configuration
import mozilla.components.service.glean.net.ConceptFetchHttpUploader
import mozilla.components.service.nimbus.Nimbus
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.rusthttp.RustHttpConfig
import mozilla.components.support.rustlog.RustLog
import org.mozilla.samples.glean.GleanMetrics.Basic
import org.mozilla.samples.glean.GleanMetrics.Test
import org.mozilla.samples.glean.GleanMetrics.Custom
import org.mozilla.samples.glean.GleanMetrics.Pings

class GleanApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // We want the log messages of all builds to go to Android logcat
        Log.addSink(AndroidLogSink())

        // Register the sample application's custom pings.
        Glean.registerPings(Pings)

        // Initialize the Glean library. Ideally, this is the first thing that
        // must be done right after enabling logging.
        val client by lazy { HttpURLConnectionClient() }
        val httpClient = ConceptFetchHttpUploader.fromClient(client)
        val config = Configuration(httpClient = httpClient)
        Glean.initialize(applicationContext, uploadEnabled = true, configuration = config)

        /** Begin Nimbus component specific code. Note: this is not relevant to Glean */
        initNimbus()
        /** End Nimbus specific code. */

        Test.timespan.start()

        Custom.counter.add()

        // Set a sample value for a metric.
        Basic.os.set("Android")
    }

    /**
     * Initialize the Nimbus experiments library and pass in the callback that will generate a
     * broadcast Intent to signal the application that experiments have been updated. This is
     * only relevant to the Nimbus library, aside from recording the experiment in Glean.
     */
    private fun initNimbus() {
        RustLog.enable()
        RustHttpConfig.setClient(lazy { HttpURLConnectionClient() })
        Nimbus.init(this) {
            val intent = Intent()
            intent.action = "org.mozilla.samples.glean.experiments.updated"
            sendBroadcast(intent)
        }
    }
}
