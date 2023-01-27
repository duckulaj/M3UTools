package com.hawkins.m3utoolsjpa.data;

import java.util.ArrayList;
import java.util.List;

public class SelectedChannelsCreationDto {

    private List<SelectedChannel> channels;

    public SelectedChannelsCreationDto() {
        this.channels = new ArrayList<>();
    }

    public SelectedChannelsCreationDto(List<SelectedChannel> channels) {
        this.channels = channels;
    }

    public List<SelectedChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<SelectedChannel> channels) {
        this.channels = channels;
    }

    public void addChannel(SelectedChannel channel) {
        this.channels.add(channel);
    }
}
