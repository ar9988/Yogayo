package com.d104.yogaapp.utils

import com.d104.yogaapp.R

object ImageResourceMapper {
    fun getImageResource(poseId:Long):Int=
        when(poseId){
            1L-> R.drawable.img_bhujangasana
            2L-> R.drawable.img_adhomukhasvanasana
            3L-> R.drawable.img_ustrasana
            4L-> R.drawable.img_virabhadrasana_two
            5L-> R.drawable.img_navasana
            6L-> R.drawable.img_virabhadrasana_three
            7L-> R.drawable.img_halasana
            else->R.drawable.img_sample_pose
        }

    fun getAnimationResource(poseId:Long):Int=
        when(poseId){
            1L-> R.drawable.anim_bhujangasana
            2L-> R.drawable.anim_adhomukhasvanasana
            3L-> R.drawable.anim_ustrasana
            4L-> R.drawable.anim_virabhadrasana_two
            5L-> R.drawable.anim_navasana
            6L-> R.drawable.anim_virabhadrasana_three
            7L-> R.drawable.anim_halasana
            else->R.drawable.anim_bhujangasana
        }
}

