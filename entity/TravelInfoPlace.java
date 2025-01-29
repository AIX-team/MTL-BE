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
@Table(name = "travel_info_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelInfoPlace extends BaseTimeEntity {
    
    @EmbeddedId
    private TravelInfoPlaceId id;
    
    @MapsId("travelInfoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_info_id")
    private TravelInfo travelInfo;
    
    @MapsId("placeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
    
    @Builder
    public TravelInfoPlace(TravelInfo travelInfo, Place place) {
        this.id = new TravelInfoPlaceId(travelInfo.getId(), place.getId());
        this.travelInfo = travelInfo;
        this.place = place;
    }
} 