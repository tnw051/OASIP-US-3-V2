package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    @Size(max = 50)
    @NotNull
    @Column(name = "ownerEmail", nullable = false, length = 50)
    private String ownerEmail;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eventCategoryId", nullable = false)
    private EventCategory eventCategory;
}