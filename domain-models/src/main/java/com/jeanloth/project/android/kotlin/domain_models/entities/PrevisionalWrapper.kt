package com.jeanloth.project.android.kotlin.domain_models.entities

import java.io.Serializable

data class PrevisionalWrapper(
    val id : Long = 0,
    val ingredient : Ingredient,
    var actual : Float = 0f,
    val needed : Float = 0f

)  {

    val delta : Float
    get() = actual - needed
}
