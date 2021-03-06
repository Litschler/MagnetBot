package audio;
/*
    Created by nils on 05.02.2018 at 22:36.
    
    (c) nils 2018
*/

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AudioInfo {

    private final AudioTrack TRACK;
    private final MessageReceivedEvent EVENT;


    public AudioInfo(AudioTrack track, MessageReceivedEvent event) {
        this.TRACK = track;
        this.EVENT = event;
    }

    public AudioTrack getTrack() {
        return TRACK;
    }

    public MessageReceivedEvent getEvent() {
        return EVENT;
    }
}
