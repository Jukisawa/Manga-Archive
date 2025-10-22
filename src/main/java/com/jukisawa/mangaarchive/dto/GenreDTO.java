package com.jukisawa.mangaarchive.dto;

public class GenreDTO {
    private int id;
    private final String name;
    
    public GenreDTO(int id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return name;
    }


    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenreDTO other)) return false;
        return id == other.id;
    }
}
