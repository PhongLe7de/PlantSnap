package com.plantsnap.data

import com.plantsnap.domain.models.DiseaseScanResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiseaseScanResultHolder @Inject constructor() {
    var result: DiseaseScanResult? = null
}
