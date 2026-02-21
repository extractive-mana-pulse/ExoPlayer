package com.example.exoplayertr.domain.model

import android.content.ComponentName

val candidates = listOf(
    ComponentName(
        "com.google.android.googlequicksearchbox",
        "com.google.android.voicesearch.serviceapi.GoogleRecognitionService"
    ),
    ComponentName(
        "com.samsung.android.bixby.agent",
        "com.samsung.android.bixby.agent.mainui.voiceinteraction.RecognitionServiceTrampoline"
    ),
    ComponentName(
        "com.samsung.android.bixby.agent",
        "com.samsung.android.bixby.agent.service.RecognitionService"
    ),
    ComponentName(
        "com.samsung.android.speech",
        "com.samsung.android.speech.SamsungRecognitionService"
    )
)
