package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "eventCategory")
@Setter
@Getter
@NoArgsConstructor
public class EventCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventCategoryId", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "eventCategoryName", nullable = false, length = 100, unique = true)
    private String eventCategoryName;

    @Size(max = 500)
    @Column(name = "eventCategoryDescription", length = 500)
    private String eventCategoryDescription;

    @NotNull
    @Column(name = "eventDuration", nullable = false)
    private Integer eventDuration;
    @OneToMany(mappedBy = "eventCategory", fetch = FetchType.LAZY)
    private List<EventCategoryOwner> owners;
}