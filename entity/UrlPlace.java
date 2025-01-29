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
@Table(name = "url_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UrlPlace extends BaseTimeEntity {
    
    @EmbeddedId
    private UrlPlaceId id;
    
    @MapsId("urlId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url;
    
    @MapsId("placeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
    
    @Builder
    public UrlPlace(Url url, Place place) {
        this.id = new UrlPlaceId(url.getId(), place.getId());
        this.url = url;
        this.place = place;
    }
} 