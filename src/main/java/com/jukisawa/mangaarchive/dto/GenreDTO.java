package com.jukisawa.mangaarchive.dto;

public class GenreDTO {
    private int Id;
    private final String name;
    
    public GenreDTO(int id, String name) {
        Id = id;
        this.name = name;
    }
    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
