package me.jfenn.lidar.services

import me.jfenn.lidar.utils.client
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import kotlin.math.sin
import kotlin.random.Random

object MusicService {

    private var ticksRemaining = 1500
    private val musicList = listOf(
        "glass_temper", "acrylic", "deluge", "outrush",
        "static", "unease",
    )

    fun tick() {
        // if no world is selected, don't play any music
        val world = client.world ?: run {
            ticksRemaining = 1500
            return
        }

        val time = world.time

        // wait until the current song (+ delay) is finished before playing again
        if (ticksRemaining-- > 0) return

        // use a function of time to determine which world tick to start the music on
        val x = (time % 3000) * sin(time / 1500f)
        if (x.toInt() != 0) return

        val musicId = musicList.random(Random(time))
        val music = PositionedSoundInstance.music(
            SoundEvent.of(Identifier.of("lidar", "music.$musicId"))
        )
        client.soundManager.play(music)

        // wait a minimum of ~2min before the next song
        ticksRemaining = 2500
    }

}