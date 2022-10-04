package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "eventCategoryOwner")
@Setter
@Getter
@NoArgsConstructor

public class EventCategoryOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventCategoryOwnerId", nullable = false)
    private Integer id;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "eventCategoryId", nullable = false)
    private Integer eventCategoryId;



}
