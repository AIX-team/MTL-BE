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
@Table(name = "travel_taste")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelTaste extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;
    
    private boolean landmark;
    private boolean relax;
    private boolean food;
    private boolean alone;
    private boolean romance;
    private boolean friend;
    private boolean child;
    private boolean parents;
    
    private Integer travelDays;
    private String optionsInput;
    
    @Builder
    public TravelTaste(User user, boolean landmark, boolean relax, boolean food,
                      boolean alone, boolean romance, boolean friend, boolean child,
                      boolean parents, Integer travelDays, String optionsInput) {
        this.user = user;
        this.landmark = landmark;
        this.relax = relax;
        this.food = food;
        this.alone = alone;
        this.romance = romance;
        this.friend = friend;
        this.child = child;
        this.parents = parents;
        this.travelDays = travelDays;
        this.optionsInput = optionsInput;
    }
} 