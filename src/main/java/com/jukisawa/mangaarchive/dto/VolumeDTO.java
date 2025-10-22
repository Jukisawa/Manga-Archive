package com.jukisawa.mangaarchive.dto;

public class VolumeDTO {
    private int id;
    private int mangaId;
    private int volume;
    private String arc;
    private String note;

    public VolumeDTO(int id, int mangaId, int volume, String arc, String note) {
        this.id = id;
        this.mangaId = mangaId;
        this.volume = volume;
        this.arc = arc;
        this.note = note;
    }

    public VolumeDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMangaId() {
        return mangaId;
    }

    public void setMangaId(int mangaId) {
        this.mangaId = mangaId;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getArc() {
        return arc;
    }

    public void setArc(String arc) {
        this.arc = arc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
