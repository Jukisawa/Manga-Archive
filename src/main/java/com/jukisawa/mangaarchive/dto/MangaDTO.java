package com.jukisawa.mangaarchive.dto;

import java.util.List;

public class MangaDTO {
    private int id;
    private String name;
    private String location;
    private boolean completed;
    private boolean aborted;
    private List<GenreDTO> genres;
    private int rating;
    private List<VolumeDTO> volumes;
    private byte[] coverImage;
    private String related;
    private String alternateName;

    public MangaDTO(int id, String name, String location, boolean completed, boolean aborted, List<GenreDTO> genres,
                    int rating,
                    List<VolumeDTO> volumes, byte[] coverImage, String related, String alternateName) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.completed = completed;
        this.aborted = aborted;
        this.genres = genres;
        this.rating = rating;
        this.volumes = volumes;
        this.coverImage = coverImage;
        this.related = related;
        this.alternateName = alternateName;
    }

    public MangaDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public List<GenreDTO> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreDTO> genres) {
        this.genres = genres;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<VolumeDTO> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeDTO> volumes) {
        this.volumes = volumes;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public String getRelated() {
        return related;
    }

    public void setRelated(String related) {
        this.related = related;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }
}
