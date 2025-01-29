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
@Table(name = "travel_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelInfo extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_taste_id")
    private TravelTaste travelTaste;
    
    private int placeCount;
    private int useCount;
    
    @Column(nullable = false)
    private String title;
    
    private boolean bookmark;
    private boolean fixed;
    private boolean isDelete;

    @OneToMany(mappedBy = "travelInfo")
    private List<Guide> guides = new ArrayList<>();

    @OneToMany(mappedBy = "travelInfo")
    private List<TravelInfoPlace> travelInfoPlaces = new ArrayList<>();
    
    @Builder
    public TravelInfo(User user, TravelTaste travelTaste, String title) {
        this.user = user;
        this.travelTaste = travelTaste;
        this.title = title;
        this.placeCount = 0;
        this.useCount = 0;
        this.bookmark = false;this.fixed = false;
        this.isDelete = false;
    }
} 