package com.elias.grabber;

import java.util.List;

import com.elias.grabber.model.Post;

public interface Parse {

    List<Post> list(String link);
    Post detail(String link);

}
