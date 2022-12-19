package int221.oasip.backendus3.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eventCategoryId", nullable = false)
    private EventCategory eventCategory;

    @Column(name = "bookingName", nullable = false, length = 100)
    private String bookingName;

    @Column(name = "bookingEmail", nullable = false, length = 50)
    private String bookingEmail;

    @Column(name = "eventStartTime", nullable = false)
    private Instant eventStartTime;

    @Column(name = "eventDuration", nullable = false)
    private Integer eventDuration;

    @Column(name = "eventNotes", length = 500)
    private String eventNotes;

    @Formula("TIMESTAMPADD(MINUTE, eventDuration, eventStartTime)")
    private Instant eventEndTime;

    @OneToMany(mappedBy = "event", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<File> files = new ArrayList<>();

    public Event(EventCategory eventCategory, String bookingName, String bookingEmail, Instant eventStartTime, String eventNotes) {
        this.eventCategory = eventCategory;
        this.bookingName = bookingName;
        this.bookingEmail = bookingEmail;
        this.eventStartTime = eventStartTime;
        this.eventNotes = eventNotes;
        this.eventDuration = eventCategory.getEventDuration();
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
        this.eventDuration = eventCategory.getEventDuration();
    }
}