package com.jukisawa.mangaarchive.dto;

import java.util.List;

public class MangaDTO {
    private int id;
    private String name;
    private String location;
    private MangaState state;
    private List<GenreDTO> genres;
    private int rating;
    private List<VolumeDTO> volumes;
    private byte[] coverImage;
    private String related;
    private String alternateName;
    private String description;
    private String publisher;

    public MangaDTO(int id, String name, String location, MangaState state,
                    List<GenreDTO> genres, int rating, List<VolumeDTO> volumes, byte[] coverImage,
                    String related, String alternateName, String description, String publisher) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.state = state;
        this.genres = genres;
        this.rating = rating;
        this.volumes = volumes;
        this.coverImage = coverImage;
        this.related = related;
        this.alternateName = alternateName;
        this.description = description;
        this.publisher = publisher;
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

    public MangaState getState() {
        return state;
    }

    public void setState(MangaState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
