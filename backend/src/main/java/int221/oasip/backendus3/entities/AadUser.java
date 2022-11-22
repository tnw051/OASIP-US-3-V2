package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "aadUser")
@Getter
@Setter
@ToString
public class AadUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aadUserId", nullable = false)
    private Integer id;

    @Column(name = "tid", nullable = false, length = 36)
    private String tid;

    @Column(name = "oid", nullable = false, length = 36)
    private String oid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @ToString.Exclude
    private User user;
}
