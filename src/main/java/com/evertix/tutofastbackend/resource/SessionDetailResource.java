package com.evertix.tutofastbackend.resource;

import com.evertix.tutofastbackend.model.Session;
import com.evertix.tutofastbackend.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionDetailResource {
    private Long id;
    private Boolean chosen;
    private Session session;
    private User teacher;
}
