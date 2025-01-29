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
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    private String address;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String type;
    private String image;
    private BigDecimal score;
    private int reviewCount;

    @OneToMany(mappedBy = "place")
    private List<TravelInfoPlace> travelInfoPlaces = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    private List<UrlPlace> urlPlaces = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    private List<CoursePlace> coursePlaces = new ArrayList<>();
    
    @Builder
    public Place(String address, String title, String description, String type,
                String image, BigDecimal score) {
        this.address = address;
        this.title = title;
        this.description = description;
        this.type = type;
        this.image = image;
        this.score = score;
        this.reviewCount = 0;
    }
} 