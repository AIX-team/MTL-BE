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
@Table(name = "guide")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guide extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_info_id")
    private TravelInfo travelInfo;
    
    private int courseCount;
    private int useCount;
    
    @Column(nullable = false)
    private String title;
    
    private Integer travelDays;
    private boolean bookmark;
    private boolean fixed;
    private boolean isDelete;

    @OneToMany(mappedBy = "guide")
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "guide")
    private List<CoursePlace> coursePlaces = new ArrayList<>();
    
    @Builder
    public Guide(TravelInfo travelInfo, String title, Integer travelDays) {
        this.travelInfo = travelInfo;
        this.title = title;
        this.travelDays = travelDays;
        this.courseCount = 0;
        this.useCount = 0;
        this.bookmark = false;
        this.fixed = false;
        this.isDelete = false;
    }
} 