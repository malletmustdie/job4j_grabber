package com.elias.grabber;

import java.util.List;

import com.elias.grabber.model.Post;

public interface Store {

    void save(Post post);
    List<Post> getAll();
    Post findById(int id);

}