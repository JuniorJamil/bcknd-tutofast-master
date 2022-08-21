package com.evertix.tutofastbackend.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "session_details")
@Getter
@Setter
public class SessionDetail extends AuditModel{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Boolean chosen;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@JsonIgnore
    private Session session;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@JsonIgnore
    private User teacher;

    public SessionDetail(Boolean chosen) {
        this.chosen=chosen;
    }

    public SessionDetail(Boolean chosen,Session session,User teacher) {
        this.chosen=chosen;
        this.session=session;
        this.teacher=teacher;
    }

    public SessionDetail() {

    }
}
