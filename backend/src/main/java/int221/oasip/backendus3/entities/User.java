package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "role", nullable = false)
    private Role role;

//        @CreationTimestamp
    @Column(name = "createOn", nullable = false)
    private Instant createOn;

//        @UpdateTimestamp
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;
}