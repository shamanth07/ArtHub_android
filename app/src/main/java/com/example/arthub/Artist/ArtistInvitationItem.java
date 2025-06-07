package com.example.arthub.Artist;

import com.example.arthub.Admin.Event;
import com.example.arthub.Admin.Invitation;

import java.io.Serializable;

public class ArtistInvitationItem implements Serializable {
    private Invitation invitation;
    private Event event;

    public ArtistInvitationItem() {}

    public ArtistInvitationItem(Invitation invitation, Event event) {
        this.invitation = invitation;
        this.event = event;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public void setInvitation(Invitation invitation) {
        this.invitation = invitation;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
