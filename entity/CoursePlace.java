import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePlace extends BaseTimeEntity {
    
    @EmbeddedId
    private CoursePlaceId id;
    
    @MapsId("placeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
    
    @MapsId("guideId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private Guide guide;
    
    @Column(nullable = false)
    private int placeNum;
    
    @Builder
    public CoursePlace(Place place, Guide guide, int placeNum) {
        this.id = new CoursePlaceId(place.getId(), guide.getId());
        this.place = place;
        this.guide = guide;
        this.placeNum = placeNum;
    }
} 